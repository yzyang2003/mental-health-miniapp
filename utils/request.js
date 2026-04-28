const config = require('./config')

/** 每次请求重新解析 baseUrl（支持 apiBaseUrlOverride 缓存后立即生效），并同步到 globalData */
function resolveBaseUrl(app) {
  const url = typeof config.getBaseUrl === 'function' ? config.getBaseUrl() : ''
  if (app && app.globalData && url) {
    app.globalData.baseUrl = url
  }
  return url || ''
}

function buildApiUrl(path = '') {
  let app = null
  if (typeof getApp === 'function') {
    try {
      app = getApp()
    } catch (error) {
      app = null
    }
  }
  const baseUrl = resolveBaseUrl(app)
  if (!path) {
    return baseUrl
  }
  if (/^https?:\/\//i.test(path)) {
    return path
  }
  const normalizedPath = String(path).startsWith('/') ? String(path) : `/${String(path)}`
  return `${baseUrl}${normalizedPath}`
}

function normalizeResponseError(res) {
  return {
    statusCode: res && res.statusCode,
    data: res && res.data,
    header: res && res.header,
    errMsg: res && res.errMsg,
  }
}

function shouldAutoRedirectOn401(options) {
  return !options || options.autoRedirectOn401 !== false
}

function handleUnauthorized(app, options) {
  if (!app || typeof app.clearAuthSession !== 'function' || !shouldAutoRedirectOn401(options)) {
    return
  }
  app.clearAuthSession({
    redirect: true,
    title: '登录已失效，请重新登录',
  })
}

function unwrapSuccessBody(res, resolveRawResponse) {
  if (resolveRawResponse) {
    return res
  }

  const body = res.data || {}
  if (
    body &&
    typeof body === 'object' &&
    Object.prototype.hasOwnProperty.call(body, 'code') &&
    Object.prototype.hasOwnProperty.call(body, 'message') &&
    Object.prototype.hasOwnProperty.call(body, 'data')
  ) {
    if (body.code >= 200 && body.code < 300) {
      return body.data
    }
    throw normalizeResponseError({
      ...res,
      data: body,
    })
  }

  return body
}

function createRequest(options = {}, requestOptions = {}) {
  const app = getApp()
  const token = app.globalData.token || wx.getStorageSync('token') || ''
  let requestTask = null

  const promise = new Promise((resolve, reject) => {
    requestTask = wx.request({
      url: buildApiUrl(options.url),
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || 15000,
      header: {
        'content-type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
        ...(options.header || {}),
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          try {
            resolve(unwrapSuccessBody(res, requestOptions.resolveRawResponse))
          } catch (error) {
            if (error && error.statusCode === 401) {
              handleUnauthorized(app, options)
            }
            reject(error)
          }
          return
        }

        const normalizedError = normalizeResponseError(res)
        if (normalizedError.statusCode === 401) {
          handleUnauthorized(app, options)
        }
        reject(normalizedError)
      },
      fail: (error) => {
        reject(error)
      },
    })
  })

  return {
    task: requestTask,
    promise,
  }
}

function request(options) {
  return createRequest(options).promise
}

function requestRaw(options) {
  return createRequest(options, { resolveRawResponse: true })
}

/**
 * 统一将 wx.request 失败或业务错误对象转为可读文案（各页面 .catch 共用）。
 * @param {*} error
 * @param {string} fallbackMessage
 * @param {{ unauthorizedMessage?: string, appendLanSetupHint?: boolean }} [options]
 */
function formatRequestError(error, fallbackMessage = '请求失败', options = {}) {
  const { unauthorizedMessage, appendLanSetupHint = false } = options || {}

  if (!error) {
    return fallbackMessage
  }
  if (typeof error === 'string') {
    return error
  }

  const statusCode = error.statusCode
  if (typeof statusCode === 'number' && (statusCode < 200 || statusCode >= 300)) {
    if (statusCode === 401) {
      return unauthorizedMessage || '登录已失效，请重新登录'
    }
    if (statusCode === 502 || statusCode === 503 || statusCode === 504) {
      return `${fallbackMessage}，服务暂时不可用，请确认后端已启动或网络地址正确`
    }
    if (error.data && typeof error.data === 'object' && error.data.message) {
      return `${error.data.message}（HTTP ${statusCode}）`
    }
    return `${fallbackMessage}（HTTP ${statusCode}）`
  }

  if (error.data && typeof error.data === 'object' && error.data.message) {
    return error.data.message
  }

  const rawMessage = error.errMsg || error.message || ''
  const lowerMessage = rawMessage.toLowerCase()
  if (lowerMessage.includes('timeout')) {
    return `${fallbackMessage}，等待有点久，请稍后重试`
  }
  if (
    lowerMessage.includes('request:fail') ||
    lowerMessage.includes('connection refused') ||
    lowerMessage.includes('unable to connect') ||
    lowerMessage.includes('fail -102') ||
    lowerMessage.includes('err_connection') ||
    (lowerMessage.includes('network') && !lowerMessage.includes('request:ok'))
  ) {
    let msg = '网络连接有点不稳定，请检查网络后重试'
    if (appendLanSetupHint) {
      msg +=
        '。真机请：① 手机与电脑连同一 Wi-Fi（关闭蜂窝）；② utils/config.js 中 USE_REAL_DEVICE=true 并填写 LAN_HOST=电脑 ipconfig 的 IPv4；③ 后端已启动。'
    }
    return msg
  }

  if (error.errMsg && error.errMsg !== 'request:ok') {
    return error.errMsg
  }
  if (error.message) {
    return error.message
  }
  return fallbackMessage
}

module.exports = {
  request,
  requestRaw,
  formatRequestError,
  buildApiUrl,
}
