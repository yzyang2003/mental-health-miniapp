const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    modules: [
      {
        key: 'article',
        title: '心理文章',
        desc: '查看焦虑、压力、情绪调节等主题文章。',
        path: '/pages/article-list/index',
      },
      {
        key: 'music',
        title: '音乐疗愈',
        desc: '按情绪场景筛选音乐，点击即可记录播放次数。',
        path: '/pages/music-list/index',
      },
      {
        key: 'selfHealing',
        title: '自我疗愈',
        desc: '按问题类型查看练习卡片，进入详情完成步骤练习。',
        path: '/pages/self-healing-list/index',
      },
    ],
  },

  onShow() {
    ensurePageLogin()
  },

  openModule(e) {
    const { path } = e.currentTarget.dataset
    if (!path) {
      return
    }

    wx.navigateTo({
      url: path,
    })
  },
})
