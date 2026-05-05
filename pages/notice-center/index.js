Page({
  data: {
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    loading: true,
    notices: [],
    defaultNotices: [
      {
        id: 1,
        title: '公告模块建设中',
        content: '后续系统公告、运营通知和版本更新会统一展示在这里。',
        createTime: '即将上线',
      },
      {
        id: 2,
        title: '当前可用功能',
        content: '你现在可以使用树洞、驿站、心理测评和 AI 咨询师"小爱"模块。',
        createTime: '当前版本',
      },
    ],
  },

  onShow() {
    this.calcCapsule()
    this.fetchNotices()
  },

  calcCapsule() {
    try {
      const info = wx.getMenuButtonBoundingClientRect()
      this.setData({
        capsuleTopPx: info.top,
        capsuleRightPx: wx.getSystemInfoSync().windowWidth - info.right + 4,
      })
    } catch (e) {
      this.setData({ capsuleTopPx: 24, capsuleRightPx: 16 })
    }
  },

  fetchNotices() {
    this.setData({ loading: true })
    // 暂无后端接口，使用默认数据模拟加载
    setTimeout(() => {
      this.setData({
        notices: this.data.defaultNotices,
        loading: false,
      })
    }, 300)
  },
})