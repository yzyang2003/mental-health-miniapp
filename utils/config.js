/**
 * 后端根地址（与 Spring Boot server.port 一致，默认 8080）。
 *
 * 优先级（从高到低）：
 * 1. 本文件 FORCE_BASE_URL（填完整根地址，适合真机调试时一次配对）
 * 2. 本地缓存 apiBaseUrlOverride（开发者工具控制台可 wx.setStorageSync('apiBaseUrlOverride','http://ip:8080') 免改代码）
 * 3. 模拟器 → 127.0.0.1；真机 → LAN_HOST
 *
 * 若列表始终无新数据，多半是连到了别的机器/旧后端：先核对本地址与电脑 ipconfig 的 IPv4、端口与 Spring 一致。
 */
const FORCE_BASE_URL = ''

const LAN_HOST = '192.168.3.8'
const LOCAL_HOST = '127.0.0.1'
const PORT = 8080

const STORAGE_BASE_URL_KEY = 'apiBaseUrlOverride'

function readStorageBaseUrlOverride() {
  try {
    if (typeof wx !== 'undefined' && typeof wx.getStorageSync === 'function') {
      const v = wx.getStorageSync(STORAGE_BASE_URL_KEY)
      if (v && typeof v === 'string' && v.trim()) {
        return v.trim().replace(/\/$/, '')
      }
    }
  } catch (error) {
    console.warn('[config] 读取 apiBaseUrlOverride 失败', error)
  }
  return ''
}

/**
 * 是否为真实手机端（真机预览 / 真机调试）。
 * 注意：开发者工具模拟器里 platform 多为 devtools / windows / mac，不能依赖「非 devtools」判断为真机，
 * 否则会把模拟器误判成真机去连 LAN_HOST，导致列表永远空白。
 */
function isRealMobileDevice() {
  try {
    if (typeof wx !== 'undefined' && typeof wx.getSystemInfoSync === 'function') {
      const p = wx.getSystemInfoSync().platform
      return p === 'ios' || p === 'android'
    }
  } catch (error) {
    console.warn('[config] 获取 platform 失败，将按本机 127.0.0.1 兜底', error)
  }
  return false
}

function buildBaseUrl() {
  const forced = (FORCE_BASE_URL || '').trim()
  if (forced) {
    return forced.replace(/\/$/, '')
  }
  const fromStorage = readStorageBaseUrlOverride()
  if (fromStorage) {
    return fromStorage
  }
  const host = isRealMobileDevice() ? (LAN_HOST || '').trim() : LOCAL_HOST
  if (!host) {
    console.warn('[config] 请检查 LAN_HOST 是否已填写为电脑当前 IPv4')
  }
  return `http://${host}:${PORT}`
}

module.exports = {
  LAN_HOST,
  PORT,
  STORAGE_BASE_URL_KEY,
  getBaseUrl: buildBaseUrl,
  baseUrl: buildBaseUrl(),
}
