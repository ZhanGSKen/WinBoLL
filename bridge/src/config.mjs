/**
 * 配置加载与默认值。
 *
 * 配置文件 config.json 位于 bridge 目录，首次启动若不存在会自动生成，
 * 其中 token 为随机生成，安卓端需要在设置页填入同一个 token。
 *
 * @author 豆包&Big Pickle&MiMo&ZhanGSKen
 * @since 1.0.0
 */
import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';
import { fileURLToPath } from 'node:url';

const ROOT = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..');
const CONFIG_PATH = path.join(ROOT, 'config.json');
const DATA_DIR = path.join(ROOT, 'data');

/** 默认配置：端口、监听地址、工作目录白名单、模型等 */
const DEFAULTS = {
  host: '0.0.0.0',
  port: 8787,
  /** 允许安卓端发起会话的目录白名单（安全边界，防止越界读写） */
  allowedRoots: ['C:\\0WINBOT', 'C:\\Users\\ZhanGSKen'],
  /** 默认工作目录 */
  defaultCwd: 'C:\\0WINBOT',
  /** Claude Code 可执行文件绝对路径 */
  claudeExecutable: 'C:\\Users\\ZhanGSKen\\AppData\\Local\\ClaudeCode\\node_modules\\@anthropic-ai\\claude-code\\bin\\claude.exe',
  /** 底层模型（Ollama 中的模型名） */
  model: 'gemma4:12b',
  /** Anthropic 兼容接口（Ollama 原生支持 /v1/messages） */
  anthropicBaseUrl: 'http://127.0.0.1:11434',
  authToken: 'ollama',
  /** 权限审批等待上限（毫秒），超时按拒绝处理 */
  permissionTimeoutMs: 300000,
  /** 单轮最大对话轮次 */
  maxTurns: 40,
  /** 内存中保留的事件条数（超出后仍会写盘，只是不再用于重放） */
  maxEventsInMemory: 2000,
};

export function ensureDir(dir) {
  if (!fs.existsSync(dir)) {
    fs.mkdirSync(dir, { recursive: true });
  }
}

/** 首次运行生成配置文件 */
export function loadConfig() {
  ensureDir(DATA_DIR);
  ensureDir(path.join(DATA_DIR, 'transcripts'));

  let config = { ...DEFAULTS };
  if (fs.existsSync(CONFIG_PATH)) {
    const raw = fs.readFileSync(CONFIG_PATH, 'utf8');
    config = { ...DEFAULTS, ...JSON.parse(raw) };
  } else {
    config.token = crypto.randomBytes(24).toString('hex');
    fs.writeFileSync(CONFIG_PATH, JSON.stringify(config, null, 2), 'utf8');
  }
  if (!config.token) {
    throw new Error('config.json 缺少 token 字段');
  }
  config.rootDir = ROOT;
  config.dataDir = DATA_DIR;
  return config;
}

export { ROOT, DATA_DIR, CONFIG_PATH };
