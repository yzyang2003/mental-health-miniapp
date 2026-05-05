const { request } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    capsuleHeightPx: 0,
    modules: [
      {
        key: 'article',
        title: '心理文章',
        desc: '焦虑、压力、情绪调节等主题文章',
        icon: '📖',
        path: '/pages/article-list/index',
      },
      {
        key: 'music',
        title: '音乐疗愈',
        desc: '按情绪场景筛选，点击即可播放',
        icon: '🎵',
        path: '/pages/music-list/index',
      },
      {
        key: 'selfHealing',
        title: '自我疗愈',
        desc: '呼吸法、接地法、正念练习',
        icon: '🧘',
        path: '/pages/self-healing-list/index',
      },
    ],
    recommendArticles: [],
    recommendExercises: [],
    recommendMusic: [],
  },

  _allArticles: [],
  _allExercises: [],
  _allMusic: [],
  _offsets: { article: 0, exercise: 0, music: 0 },

  onShow() {
    if (!ensurePageLogin()) return
    this.calcCapsule()
    this.loadAllData()
  },

  calcCapsule() {
    try {
      const info = wx.getMenuButtonBoundingClientRect()
      this.setData({
        capsuleTopPx: info.top,
        capsuleRightPx: wx.getSystemInfoSync().windowWidth - info.right + 4,
        capsuleHeightPx: info.height,
      })
    } catch (e) {
      this.setData({ capsuleTopPx: 24, capsuleRightPx: 16, capsuleHeightPx: 32 })
    }
  },

  async loadAllData() {
    try {
      const [articlesRes, exercisesRes, musicRes] = await Promise.all([
        request({ url: '/api/article/list?page=1&size=50', timeout: 8000 }).catch(() => ({ records: [] })),
        request({ url: '/api/selfHealing/list?page=1&size=50', timeout: 8000 }).catch(() => ({ records: [] })),
        request({ url: '/api/music/playlist/list?page=1&size=50', timeout: 8000 }).catch(() => ({ records: [] })),
      ])

      this._allArticles = (articlesRes.records || []).map(a => ({
        id: a.id,
        title: a.title || '',
        summary: (a.summary || '').slice(0, 20),
      }))

      this._allExercises = (exercisesRes.records || []).map(e => ({
        id: e.id,
        title: e.title || '',
        durationText: e.duration ? `${Math.round(e.duration / 60)}分钟` : '',
      }))

      this._allMusic = (musicRes.records || []).map(m => ({
        id: m.id,
        name: m.name || '',
        emotionType: m.emotionType || '',
      }))

      const day = new Date().getDate()
      this._offsets = { article: day, exercise: day, music: day }
      this.updateRecommend('article')
      this.updateRecommend('exercise')
      this.updateRecommend('music')
    } catch (e) {
      console.error('[station] 加载推荐数据失败', e)
    }
  },

  updateRecommend(type) {
    const offset = this._offsets[type]
    let list = []
    if (type === 'article') {
      list = this.pickItems(this._allArticles, offset, 3)
      this.setData({ recommendArticles: list })
    } else if (type === 'exercise') {
      list = this.pickItems(this._allExercises, offset, 3)
      this.setData({ recommendExercises: list })
    } else if (type === 'music') {
      list = this.pickItems(this._allMusic, offset, 3)
      this.setData({ recommendMusic: list })
    }
  },

  pickItems(arr, offset, count) {
    if (!arr.length) return []
    const result = []
    for (let i = 0; i < Math.min(count, arr.length); i++) {
      result.push(arr[(offset + i) % arr.length])
    }
    return result
  },

  onRefresh(e) {
    const type = e.currentTarget.dataset.type
    if (!type) return
    this._offsets[type] = (this._offsets[type] || 0) + 3
    this.updateRecommend(type)
  },

  onModuleTap(e) {
    const { path } = e.currentTarget.dataset
    if (!path) return
    wx.navigateTo({ url: path })
  },

  onArticleTap(e) {
    const { id } = e.currentTarget.dataset
    if (id) {
      wx.navigateTo({ url: `/pages/article-detail/index?id=${id}` })
    } else {
      wx.navigateTo({ url: '/pages/article-list/index' })
    }
  },

  onExerciseTap(e) {
    const { id } = e.currentTarget.dataset
    if (!id) return
    wx.navigateTo({ url: `/pages/self-healing-detail/index?id=${id}` })
  },

  onMusicTap() {
    wx.navigateTo({ url: '/pages/music-list/index' })
  },
})