/**
 * 会话存储与事件总线。
 *
 * - 会话元数据保存在 data/sessions.json
 * - 每个会话的事件流按 seq 递增写入 data/transcripts/<sessionId>.jsonl
 * - 内存中保留最近若干条事件，供安卓端断线后按 afterSeq 重放
 *
 * @author 豆包&Big Pickle&MiMo&ZhanGSKen
 * @since 1.0.0
 */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

export class SessionStore {
  constructor(config) {
    this.config = config;
    this.sessionsFile = path.join(config.dataDir, 'sessions.json');
    this.transcriptsDir = path.join(config.dataDir, 'transcripts');
    /** sessionId -> { meta, seq, events: [], listeners: Set } */
    this.sessions = new Map();
    this._load();
  }

  _load() {
    if (fs.existsSync(this.sessionsFile)) {
      try {
        const raw = JSON.parse(fs.readFileSync(this.sessionsFile, 'utf8'));
        for (const meta of raw.sessions || []) {
          this.sessions.set(meta.id, {
            meta,
            seq: 0,
            events: [],
            listeners: new Set(),
          });
        }
      } catch (err) {
        console.error('[store] sessions.json 解析失败，按空库启动:', err.message);
      }
    }
  }

  _flushMeta() {
    const sessions = [];
    for (const item of this.sessions.values()) {
      sessions.push(item.meta);
    }
    fs.writeFileSync(this.sessionsFile, JSON.stringify({ sessions }, null, 2), 'utf8');
  }

  list() {
    return [...this.sessions.values()]
      .map((item) => item.meta)
      .sort((a, b) => (b.updatedAt || 0) - (a.updatedAt || 0));
  }

  get(id) {
    return this.sessions.get(id) || null;
  }

  create({ title, cwd }) {
    const id = crypto.randomBytes(8).toString('hex');
    const meta = {
      id,
      title: title && title.trim() ? title.trim() : '新会话',
      cwd,
      claudeSessionId: null,
      createdAt: Date.now(),
      updatedAt: Date.now(),
      turnCount: 0,
    };
    this.sessions.set(id, { meta, seq: 0, events: [], listeners: new Set() });
    this._flushMeta();
    return meta;
  }

  update(id, patch) {
    const item = this.sessions.get(id);
    if (!item) {
      return null;
    }
    item.meta = { ...item.meta, ...patch, updatedAt: Date.now() };
    this._flushMeta();
    return item.meta;
  }

  remove(id) {
    const item = this.sessions.get(id);
    if (!item) {
      return false;
    }
    this.sessions.delete(id);
    const file = this._transcriptFile(id);
    if (fs.existsSync(file)) {
      fs.unlinkSync(file);
    }
    this._flushMeta();
    return true;
  }

  _transcriptFile(id) {
    return path.join(this.transcriptsDir, id + '.jsonl');
  }

  /** 追加事件：分配 seq、写盘、广播给订阅者 */
  append(sessionId, event) {
    const item = this.sessions.get(sessionId);
    if (!item) {
      return null;
    }
    const seq = ++item.seq;
    const payload = { ...event, seq, ts: Date.now() };
    item.events.push(payload);
    if (item.events.length > this.config.maxEventsInMemory) {
      item.events.shift();
    }
    fs.appendFileSync(this._transcriptFile(sessionId), JSON.stringify(payload) + '\n', 'utf8');
    item.meta.updatedAt = Date.now();
    for (const listener of item.listeners) {
      try {
        listener(payload);
      } catch (err) {
        console.error('[store] 事件广播失败:', err.message);
      }
    }
    return payload;
  }

  /** 取 seq > afterSeq 的事件，用于断线重放 */
  eventsAfter(sessionId, afterSeq) {
    return this.allEvents(sessionId).filter((e) => e.seq > afterSeq);
  }

  subscribe(sessionId, listener) {
    const item = this.sessions.get(sessionId);
    if (!item) {
      return () => {};
    }
    item.listeners.add(listener);
    return () => item.listeners.delete(listener);
  }

  /** 会话全部历史事件（用于打开会话时渲染） */
  allEvents(sessionId) {
    const item = this.sessions.get(sessionId);
    if (!item) {
      return [];
    }
    if (item.events.length === 0) {
      // 进程重启后内存为空，从磁盘恢复最近 500 条
      const file = this._transcriptFile(sessionId);
      if (fs.existsSync(file)) {
        const lines = fs.readFileSync(file, 'utf8').split('\n').filter(Boolean).slice(-500);
        for (const line of lines) {
          try {
            const parsed = JSON.parse(line);
            item.events.push(parsed);
            item.seq = Math.max(item.seq, parsed.seq || 0);
          } catch {
            // 单行损坏忽略
          }
        }
      }
    }
    return item.events.slice();
  }
}
