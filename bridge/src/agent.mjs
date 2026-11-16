/**
 * Claude Code 会话执行器。
 *
 * 通过官方 @anthropic-ai/claude-agent-sdk 驱动本机 claude.exe，
 * 底层模型为 Ollama 的 gemma4:12b（走 Ollama 的 Anthropic 兼容接口）。
 *
 * 每轮对话独立起一个 query 进程，用 resume 续接 Claude Code 的会话上下文，
 * 好处是单轮崩溃不会污染整个会话，且随时可以中断。
 *
 * @author 豆包&Big Pickle&MiMo&ZhanGSKen
 * @since 1.0.0
 */
import crypto from 'node:crypto';
import path from 'node:path';
import { query } from '@anthropic-ai/claude-agent-sdk';

export class AgentRunner {
  constructor(store, config) {
    this.store = store;
    this.config = config;
    /** sessionId -> { busy, query, autoAllowTools:Set<string> } */
    this.runtime = new Map();
    /** requestId -> { resolve, timer, sessionId } */
    this.pendingPermissions = new Map();
  }

  _rt(sessionId) {
    let rt = this.runtime.get(sessionId);
    if (!rt) {
      rt = { busy: false, query: null, autoAllowTools: new Set() };
      this.runtime.set(sessionId, rt);
    }
    return rt;
  }

  isBusy(sessionId) {
    return this._rt(sessionId).busy;
  }

  _assertInsideRoots(cwd) {
    const resolved = path.resolve(cwd);
    const ok = this.config.allowedRoots.some((root) => {
      const r = path.resolve(root);
      return resolved.toLowerCase().startsWith(r.toLowerCase());
    });
    if (!ok) {
      throw new Error('工作目录不在白名单内: ' + resolved);
    }
    return resolved;
  }

  /** 安卓端对权限请求作答 */
  respondPermission(requestId, allow, rememberToolName) {
    const pending = this.pendingPermissions.get(requestId);
    if (!pending) {
      return false;
    }
    clearTimeout(pending.timer);
    this.pendingPermissions.delete(requestId);
    if (allow && rememberToolName) {
      this._rt(pending.sessionId).autoAllowTools.add(rememberToolName);
    }
    // 记录已处理状态，安卓端回放历史时据此跳过弹窗
    this.store.append(pending.sessionId, {
      type: 'permission_resolved',
      requestId,
      allow: !!allow,
    });
    pending.resolve(!!allow);
    return true;
  }

  async interrupt(sessionId) {
    const rt = this._rt(sessionId);
    if (rt.query && typeof rt.query.interrupt === 'function') {
      try {
        await rt.query.interrupt();
        return true;
      } catch (err) {
        this.store.append(sessionId, { type: 'error', message: '中断失败: ' + err.message });
        return false;
      }
    }
    return false;
  }

  _askPermission(sessionId, toolName, input) {
    const rt = this._rt(sessionId);
    if (rt.autoAllowTools.has(toolName)) {
      return Promise.resolve(true);
    }
    const requestId = crypto.randomBytes(6).toString('hex');
    this.store.append(sessionId, {
      type: 'permission_request',
      requestId,
      toolName,
      input: input || {},
    });
    return new Promise((resolve) => {
      const timer = setTimeout(() => {
        this.pendingPermissions.delete(requestId);
        this.store.append(sessionId, {
          type: 'permission_timeout',
          requestId,
          toolName,
        });
        resolve(false);
      }, this.config.permissionTimeoutMs);
      this.pendingPermissions.set(requestId, { resolve, timer, sessionId });
    });
  }

  /** 执行一轮用户输入 */
  async runTurn(sessionId, text) {
    const item = this.store.get(sessionId);
    if (!item) {
      throw new Error('会话不存在');
    }
    const rt = this._rt(sessionId);
    if (rt.busy) {
      const err = new Error('该会话上一轮仍在执行');
      err.code = 'BUSY';
      throw err;
    }
    rt.busy = true;
    this.store.append(sessionId, { type: 'user', text });
    this.store.append(sessionId, { type: 'state', busy: true });

    const cwd = this._assertInsideRoots(item.meta.cwd || this.config.defaultCwd);
    const canUseTool = async (toolName, input) => {
      const allowed = await this._askPermission(sessionId, toolName, input);
      if (!allowed) {
        return { behavior: 'deny', message: '用户在手机上拒绝了该操作' };
      }
      return { behavior: 'allow', updatedInput: input };
    };

    const options = {
      cwd,
      model: this.config.model,
      maxTurns: this.config.maxTurns,
      includePartialMessages: true,
      permissionMode: 'default',
      canUseTool,
      pathToClaudeCodeExecutable: this.config.claudeExecutable,
      settingSources: [],
      env: {
        ...process.env,
        ANTHROPIC_BASE_URL: this.config.anthropicBaseUrl,
        ANTHROPIC_AUTH_TOKEN: this.config.authToken,
        ANTHROPIC_MODEL: this.config.model,
        ANTHROPIC_SMALL_FAST_MODEL: this.config.model,
        CLAUDE_CODE_DISABLE_NONESSENTIAL_TRAFFIC: '1',
      },
    };
    if (item.meta.claudeSessionId) {
      options.resume = item.meta.claudeSessionId;
    }

    const streamedText = [];

    try {
      const q = query({ prompt: text, options });
      rt.query = q;
      for await (const message of q) {
        this._handleMessage(sessionId, message, streamedText);
      }
      this.store.update(sessionId, { turnCount: (item.meta.turnCount || 0) + 1 });
      this.store.append(sessionId, { type: 'turn_end' });
    } catch (err) {
      this.store.append(sessionId, { type: 'error', message: err && err.message ? err.message : String(err) });
    } finally {
      rt.query = null;
      rt.busy = false;
      this.store.append(sessionId, { type: 'state', busy: false });
    }
  }

  _handleMessage(sessionId, message, streamedText) {
    if (!message || !message.type) {
      return;
    }
    if (message.type === 'system' && message.subtype === 'init') {
      this.store.update(sessionId, { claudeSessionId: message.session_id });
      this.store.append(sessionId, {
        type: 'init',
        claudeSessionId: message.session_id,
        model: message.model,
        tools: message.tools || [],
      });
      return;
    }
    if (message.type === 'stream_event') {
      const ev = message.event || {};
      if (ev.type === 'content_block_delta' && ev.delta && ev.delta.type === 'text_delta') {
        streamedText.push(ev.delta.text);
        this.store.append(sessionId, { type: 'delta', text: ev.delta.text });
      }
      return;
    }
    if (message.type === 'assistant') {
      const content = (message.message && message.message.content) || [];
      for (const block of content) {
        if (block.type === 'text' && block.text) {
          this.store.append(sessionId, { type: 'assistant_message', text: block.text });
        } else if (block.type === 'tool_use') {
          this.store.append(sessionId, {
            type: 'tool_use',
            toolUseId: block.id,
            name: block.name,
            input: block.input || {},
          });
        }
      }
      return;
    }
    if (message.type === 'user') {
      const content = (message.message && message.message.content) || [];
      if (Array.isArray(content)) {
        for (const block of content) {
          if (block.type === 'tool_result') {
            this.store.append(sessionId, {
              type: 'tool_result',
              toolUseId: block.tool_use_id,
              isError: !!block.is_error,
              content: this._stringifyToolResult(block.content),
            });
          }
        }
      }
      return;
    }
    if (message.type === 'result') {
      this.store.append(sessionId, {
        type: 'result',
        subtype: message.subtype || 'success',
        durationMs: message.duration_ms || 0,
        turns: message.num_turns || 0,
        usage: message.usage || null,
      });
    }
  }

  _stringifyToolResult(content) {
    if (content == null) {
      return '';
    }
    if (typeof content === 'string') {
      return content;
    }
    if (Array.isArray(content)) {
      return content
        .map((part) => (typeof part === 'string' ? part : part && part.text ? part.text : JSON.stringify(part)))
        .join('\n');
    }
    return JSON.stringify(content);
  }
}
