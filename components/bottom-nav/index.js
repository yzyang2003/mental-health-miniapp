Component({
  properties: {
    current: {
      type: String,
      value: 'station',
    },
  },

  data: {
    isAndroid: false,
    items: [
      { key: 'station', label: '驿站', path: '/pages/station/index' },
      { key: 'consult', label: '咨询', path: '/pages/consult/index' },
      { key: 'topic', label: '树洞', path: '/pages/topic-list/index' },
      { key: 'profile', label: '我的', path: '/pages/profile/index' },
    ],
  },
  lifetimes: {
    attached() {
      let isAndroid = false
      try {
        const info = wx.getSystemInfoSync()
        isAndroid = (info && info.platform) === 'android'
      } catch (error) {
        isAndroid = false
      }
      this.setData({ isAndroid })
    },
  },

  methods: {
    handleNavigate(e) {
      const { key, path } = e.currentTarget.dataset
      if (!path || key === this.properties.current) {
        return
      }

      wx.reLaunch({
        url: path,
      })
    },
  },
})
