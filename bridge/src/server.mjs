/**
 * Claude Bridge HTTP 服务。
 *
 * 路由一览（除 /api/ping 外均需 Authorization: Bearer <token>）：
 *   GET    /api/ping                     健康检查
 *   GET    /api/sessions                 会话列表
 *   POST   /api/sessions                 新建会话 {title, cwd}
 *   GET    /api/sessions/:id/messages    会话历史事件
 *   GET    /api/sessions/:id/events      事件流（SSE，支持 afterSeq 断线重放）
 *   POST   /api/sessions/:id/messages    发送一轮用户输入 {text}
 *   POST   /api/sessions/:id/interrupt   中断当前轮次
 *   POST   /api/permissions/:requestId   权限审批作答 {allow, remember}
 *
 * @author 豆包&Big Pickle&MiMo&ZhanGSKen
 * @since 1.0.0
 */
import http from 'node:http';
import fs from 'node:fs';
import path from 'node:path';
import { loadConfig } from './config.mjs';
import { SessionStore } from './store.mjs';
import { AgentRunner } from './agent.mjs';

const config = loadConfig();
const store = new SessionStore(config);
const runner = new AgentRunner(store, config);

/** 简单日志：控制台 + data/bridge.log */
function log(...args) {
  const line = `[${new Date().toISOString()}] ${args.join(' ')}`;
  console.log(line);
  try {
    fs.appendFileSync(path.join(config.dataDir, 'bridge.log'), line + '\n', 'utf8');
  } catch {
    // 日志写盘失败不影响服务
  }
}

function sendJson(res, status, payload) {
  const body = JSON.stringify(payload);
  res.writeHead(status, {
    'Content-Type': 'application/json; charset=utf-8',
    'Content-Length': Buffer.byteLength(body),
    'Access-Control-Allow-Origin': '*',
    'Cache-Control': 'no-store',
  });
  res.end(body);
}

function sendSseHeaders(res) {
  res.writeHead(200, {
    'Content-Type': 'text/event-stream; charset=utf-8',
    'Cache-Control': 'no-store, no-cache',
    Connection: 'keep-alive',
    'Access-Control-Allow-Origin': '*',
    'X-Accel-Buffering': 'no',
  });
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    const chunks = [];
    let size = 0;
    req.on('data', (chunk) => {
      size += chunk.length;
      if (size > 2 * 1024 * 1024) {
        reject(new Error('请求体过大'));
        req.destroy();
        return;
      }
      chunks.push(chunk);
    });
    req.on('end', () => {
      const raw = Buffer.concat(chunks).toString('utf8');
      if (!raw) {
        resolve({});
        return;
      }
      try {
        resolve(JSON.parse(raw));
      } catch (err) {
        reject(new Error('JSON 解析失败: ' + err.message));
      }
    });
    req.on('error', reject);
  });
}

function isAuthorized(req, url) {
  const header = req.headers['authorization'] || '';
  const bearer = header.startsWith('Bearer ') ? header.slice(7).trim() : '';
  const queryToken = url.searchParams.get('token') || '';
  const provided = bearer || queryToken;
  return provided && provided === config.token;
}

const server = http.createServer(async (req, res) => {
  const url = new URL(req.url, 'http://' + (req.headers.host || 'localhost'));
  const pathname = url.pathname;
  const method = req.method;

  if (method === 'OPTIONS') {
    res.writeHead(204, {
      'Access-Control-Allow-Origin': '*',
      'Access-Control-Allow-Headers': 'Authorization, Content-Type',
      'Access-Control-Allow-Methods': 'GET, POST, DELETE, OPTIONS',
    });
    res.end();
    return;
  }

  try {
    if (pathname === '/api/ping' && method === 'GET') {
      sendJson(res, 200, {
        ok: true,
        service: 'claude-bridge',
        version: '1.0.0',
        model: config.model,
        claudeExecutableFound: fs.existsSync(config.claudeExecutable),
        serverTime: Date.now(),
      });
      return;
    }

    if (!isAuthorized(req, url)) {
      sendJson(res, 401, { error: 'unauthorized' });
      return;
    }

    if (pathname === '/api/sessions' && method === 'GET') {
      sendJson(res, 200, { sessions: store.list() });
      return;
    }

    if (pathname === '/api/sessions' && method === 'POST') {
      const body = await readBody(req);
      const cwd = body.cwd || config.defaultCwd;
      const resolved = path.resolve(cwd);
      const allowed = config.allowedRoots.some((root) =>
        resolved.toLowerCase().startsWith(path.resolve(root).toLowerCase())
      );
      if (!allowed) {
        sendJson(res, 400, { error: 'cwd 不在白名单: ' + config.allowedRoots.join(', ') });
        return;
      }
      const meta = store.create({ title: body.title, cwd: resolved });
      log('新建会话', meta.id, meta.cwd);
      sendJson(res, 200, { session: meta });
      return;
    }

    const sessionMatch = pathname.match(/^\/api\/sessions\/([0-9a-f]{16})(\/.*)?$/);
    if (sessionMatch) {
      const sessionId = sessionMatch[1];
      const sub = sessionMatch[2] || '';
      const item = store.get(sessionId);
      if (!item) {
        sendJson(res, 404, { error: '会话不存在' });
        return;
      }

      if (sub === '' && method === 'DELETE') {
        store.remove(sessionId);
        sendJson(res, 200, { ok: true });
        return;
      }

      if (sub === '/messages' && method === 'GET') {
        sendJson(res, 200, { session: item.meta, events: store.allEvents(sessionId) });
        return;
      }

      if (sub === '/events' && method === 'GET') {
        const afterSeq = Number(url.searchParams.get('afterSeq') || '0');
        sendSseHeaders(res);
        res.write(': connected\n\n');
        let lastSent = afterSeq;
        const push = (event) => {
          if (event.seq <= lastSent) {
            return;
          }
          res.write(`id: ${event.seq}\ndata: ${JSON.stringify(event)}\n\n`);
          lastSent = event.seq;
        };
        const replay = store.eventsAfter(sessionId, afterSeq);
        // 回放历史时，已处理过的权限请求降级为普通历史事件，避免安卓端重复弹窗
        const resolvedRequestIds = new Set();
        for (const event of replay) {
          if (event.type === 'permission_resolved' || event.type === 'permission_timeout') {
            resolvedRequestIds.add(event.requestId);
          }
        }
        for (const event of replay) {
          const isPending = event.type === 'permission_request'
            && runner.pendingPermissions.has(event.requestId);
          if (event.type === 'permission_request' && (!isPending || resolvedRequestIds.has(event.requestId))) {
            push({ ...event, type: 'permission_history' });
          } else {
            push(event);
          }
        }        const unsubscribe = store.subscribe(sessionId, push);
        const heartbeat = setInterval(() => res.write(': ping\n\n'), 15000);
        req.on('close', () => {
          clearInterval(heartbeat);
          unsubscribe();
        });
        return;
      }

      if (sub === '/messages' && method === 'POST') {
        const body = await readBody(req);
        const text = (body.text || '').trim();
        if (!text) {
          sendJson(res, 400, { error: 'text 不能为空' });
          return;
        }
        if (runner.isBusy(sessionId)) {
          sendJson(res, 409, { error: 'busy' });
          return;
        }
        sendJson(res, 202, { accepted: true });
        runner.runTurn(sessionId, text).catch((err) => {
          log('runTurn 异常', err.message);
          store.append(sessionId, { type: 'error', message: err.message });
        });
        log('发送输入', sessionId, text.slice(0, 60));
        return;
      }

      if (sub === '/interrupt' && method === 'POST') {
        const ok = await runner.interrupt(sessionId);
        sendJson(res, 200, { ok });
        return;
      }

      sendJson(res, 404, { error: '未知接口' });
      return;
    }

    const permMatch = pathname.match(/^\/api\/permissions\/([0-9a-f]{6,32})$/);
    if (permMatch && method === 'POST') {
      const requestId = permMatch[1];
      const body = await readBody(req);
      const ok = runner.respondPermission(requestId, !!body.allow, body.remember ? body.toolName : null);
      sendJson(res, ok ? 200 : 404, { ok });
      return;
    }

    sendJson(res, 404, { error: '未找到接口: ' + method + ' ' + pathname });
  } catch (err) {
    log('请求处理异常', err.message);
    if (!res.headersSent) {
      sendJson(res, 500, { error: err.message });
    } else {
      res.end();
    }
  }
});

server.listen(config.port, config.host, () => {
  log(`Claude Bridge 已启动: http://${config.host}:${config.port}`);
  log(`模型: ${config.model}  接口: ${config.anthropicBaseUrl}`);
  log(`允许目录: ${config.allowedRoots.join(', ')}`);
  log('token 见 config.json，安卓端需填入同一 token');
});
