const { formatRequestError } = require('../../utils/request')

Page({
  data: {
    loading: false,
    errorMessage: '',
    successMessage: '',
  },

  onShow() {
    const app = getApp()
    const token = app.globalData.token || wx.getStorageSync('token') || ''
    if (token) {
      wx.reLaunch({
        url: '/pages/station/index',
      })
    }
  },

  handleLogin() {
    const app = getApp()

    this.setData({
      loading: true,
      errorMessage: '',
      successMessage: '',
    })

    app.login()
      .then(() => {
        this.setData({
          successMessage: '登录成功，正在进入驿站',
        })
        wx.reLaunch({
          url: '/pages/station/index',
        })
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '登录失败，请检查后端服务是否启动', {
            appendLanSetupHint: true,
          }),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },
})
