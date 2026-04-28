Page({
  data: {
    url: '',
  },

  onLoad(options) {
    const rawUrl = decodeURIComponent((options && options.url) || '')
    if (!/^https?:\/\//.test(rawUrl)) {
      wx.showToast({
        title: '链接地址无效',
        icon: 'none',
      })
      return
    }
    this.setData({
      url: rawUrl,
    })
  },
})
