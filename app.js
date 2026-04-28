const config = require('./utils/config')

App({
  globalData: {
    baseUrl: config.getBaseUrl(),
    token: wx.getStorageSync('token') || '',
    openid: wx.getStorageSync('openid') || '',
    userInfo: null,
    latestQuizResult: null,
    aiChatPrefill: '',
    /** 从测评结果页「和小爱聊聊」进入时置 true，聊天页 onShow 按需拉一次历史 */
    aiChatNeedHistoryRefresh: false,
    /** 离开聊天页时暂存界面状态，返回或重新进入时恢复「思考中」气泡 */
    aiChatPageState: null,
    authRedirecting: false,
  },

  onLaunch() {
    const token = wx.getStorageSync('token') || ''
    const openid = wx.getStorageSync('openid') || ''
    this.globalData.baseUrl = config.getBaseUrl()
    this.globalData.token = token
    this.globalData.openid = openid
    this.globalData.authRedirecting = false
  },

  clearAuthSession(options = {}) {
    const { redirect = false, title = '' } = options
    this.globalData.token = ''
    this.globalData.openid = ''
    wx.removeStorageSync('token')
    wx.removeStorageSync('openid')

    if (!redirect || this.globalData.authRedirecting) {
      return
    }

    this.globalData.authRedirecting = true
    if (title) {
      wx.showToast({
        title,
        icon: 'none',
        duration: 1800,
      })
    }

    setTimeout(() => {
      wx.reLaunch({
        url: '/pages/login/index',
        complete: () => {
          this.globalData.authRedirecting = false
        },
      })
    }, title ? 300 : 0)
  },

  login() {
    return new Promise((resolve, reject) => {
      wx.login({
        success: (loginRes) => {
          if (!loginRes.code) {
            reject(new Error('微信登录失败，未获取到 code'))
            return
          }

          wx.request({
            url: `${config.getBaseUrl()}/api/login`,
            method: 'POST',
            header: {
              'content-type': 'application/json',
            },
            data: {
              code: loginRes.code,
            },
            success: (res) => {
              const data = res.data || {}
              if (res.statusCode !== 200 || !data.token) {
                reject(new Error(data.message || '后端登录失败'))
                return
              }

              this.globalData.token = data.token
              this.globalData.openid = data.openid || ''
              this.globalData.authRedirecting = false
              wx.setStorageSync('token', data.token)
              wx.setStorageSync('openid', data.openid || '')
              resolve(data)
            },
            fail: (error) => {
              reject(error)
            },
          })
        },
        fail: (error) => {
          reject(error)
        },
      })
    })
  },
})
