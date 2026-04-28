Component({
  data: {
    canGoBack: false,
    topPx: 16,
    leftPx: 12,
    btnSizePx: 32,
    radiusPx: 16,
    iconSizePx: 20,
  },

  lifetimes: {
    attached() {
      const pages = getCurrentPages()
      const canGoBack = pages.length > 1
      let topPx = 16
      let leftPx = 12
      let btnSizePx = 32
      let radiusPx = 16
      let iconSizePx = 20

      if (wx.getMenuButtonBoundingClientRect) {
        const rect = wx.getMenuButtonBoundingClientRect()
        if (rect && rect.top >= 0 && rect.height > 0) {
          const systemInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
          const windowWidth = systemInfo && systemInfo.windowWidth ? systemInfo.windowWidth : 375
          const rightGap = Math.max(8, Math.round(windowWidth - rect.right))
          topPx = Math.round(rect.top)
          leftPx = rightGap
          btnSizePx = Math.round(rect.height)
          radiusPx = Math.round(rect.height / 2)
          iconSizePx = Math.round(rect.height * 0.62)
        }
      }

      this.setData({
        canGoBack,
        topPx,
        leftPx,
        btnSizePx,
        radiusPx,
        iconSizePx,
      })
    },
  },

  methods: {
    handleBack() {
      if (getCurrentPages().length > 1) {
        wx.navigateBack()
      }
    },
  },
})
