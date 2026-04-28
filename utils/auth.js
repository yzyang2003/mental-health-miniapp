function getAppSafe() {
  if (typeof getApp === 'function') {
    try {
      return getApp()
    } catch (error) {
      return null
    }
  }
  return null
}

function getAccessToken() {
  const app = getAppSafe()
  const globalToken = app && app.globalData ? app.globalData.token : ''
  return globalToken || wx.getStorageSync('token') || ''
}

function ensurePageLogin(options = {}) {
  const { redirectUrl = '/pages/login/index' } = options
  const token = getAccessToken()
  if (token) {
    return true
  }

  const app = getAppSafe()
  if (app && typeof app.clearAuthSession === 'function') {
    app.clearAuthSession({ redirect: true })
    return false
  }

  wx.reLaunch({
    url: redirectUrl,
  })
  return false
}

function buildAuthorizationHeader() {
  const token = getAccessToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

module.exports = {
  getAccessToken,
  ensurePageLogin,
  buildAuthorizationHeader,
}
