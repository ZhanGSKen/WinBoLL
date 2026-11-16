/**
 * 桥接服务端到端测试客户端。
 *
 * 用法：node tools/test-client.mjs "测试用的用户输入"
 *
 * @author 豆包&Big Pickle&MiMo&ZhanGSKen
 * @since 1.0.0
 */
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const config = JSON.parse(fs.readFileSync(path.join(root, 'config.json'), 'utf8'));
const base = `http://127.0.0.1:${config.port}`;
const headers = { Authorization: `Bearer ${config.token}`, 'Content-Type': 'application/json' };
const text = process.argv[2] || '请用一句中文回复：远程桥接已经接通。';

const created = await (await fetch(base + '/api/sessions', {
  method: 'POST',
  headers,
  body: JSON.stringify({ title: 'E2E 测试', cwd: 'C:\\0WINBOT' }),
})).json();
const sessionId = created.session.id;
console.log('会话:', sessionId);

const stream = await fetch(`${base}/api/sessions/${sessionId}/events?afterSeq=0`, { headers });
const reader = stream.body.getReader();
const decoder = new TextDecoder();
let buffer = '';
let finished = false;

const sendResult = await fetch(`${base}/api/sessions/${sessionId}/messages`, {
  method: 'POST',
  headers,
  body: JSON.stringify({ text }),
});
console.log('发送结果:', sendResult.status);

while (!finished) {
  const { value, done } = await reader.read();
  if (done) {
    break;
  }
  buffer += decoder.decode(value, { stream: true });
  const chunks = buffer.split('\n\n');
  buffer = chunks.pop() || '';
  for (const chunk of chunks) {
    for (const line of chunk.split('\n')) {
      if (!line.startsWith('data: ')) {
        continue;
      }
      const event = JSON.parse(line.slice(6));
      const preview = JSON.stringify(event).slice(0, 200);
      console.log(`#${event.seq} ${event.type} ${preview}`);
      if (event.type === 'permission_request') {
        // 自动允许，模拟手机端点击
        await fetch(`${base}/api/permissions/${event.requestId}`, {
          method: 'POST',
          headers,
          body: JSON.stringify({ allow: true }),
        });
        console.log('  -> 已自动允许工具:', event.toolName);
      }
      if (event.type === 'turn_end' || event.type === 'error') {
        finished = true;
      }
    }
    if (finished) {
      break;
    }
  }
}
await reader.cancel();
console.log('测试结束');
