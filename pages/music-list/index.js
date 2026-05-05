const { request } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

const MOOD_TABS = [
  { id: 0, name: '全部', icon: '🎵', color: '#6eaa8a' },
  { id: 1, name: '放松', icon: '😌', color: '#6eaa8a', emotionType: '放松' },
  { id: 2, name: '专注', icon: '🎯', color: '#60a5fa', emotionType: '专注' },
  { id: 3, name: '助眠', icon: '🌙', color: '#a78bfa', emotionType: '助眠' },
  { id: 4, name: '释放', icon: '💨', color: '#f59e0b', emotionType: '释放' },
  { id: 5, name: '活力', icon: '☀️', color: '#f97316', emotionType: '振奋' },
]

Page({
  data: {
    playlists: [],
    filteredPlaylists: [],
    moodTabs: MOOD_TABS,
    selectedMoodId: 0,
    loading: true,
    featured: null,
    capsuleTopPx: 0,
    capsuleRightPx: 0,
  },

  _loaded: false,

  onShow() {
    if (!ensurePageLogin()) return
    this.calcCapsule()
    if (!this._loaded) {
      this._loaded = true
      this.loadPlaylists()
    }
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

  onPullDownRefresh() {
    this.loadPlaylists().then(() => wx.stopPullDownRefresh())
  },

  async loadPlaylists() {
    this.setData({ loading: true })
    try {
      const res = await request({ url: '/api/music/playlist/list?page=1&size=50', timeout: 10000 })

      const records = (res.records || []).map((item) => ({
        ...item,
        coverFailed: !item.coverImage,
      }))

      const featured = records.length > 0
        ? records.reduce((a, b) => (a.playCount || 0) >= (b.playCount || 0) ? a : b)
        : null

      this.setData({ playlists: records, featured })
      this.filterPlaylists()
    } catch (e) {
      console.error('[music-list] 加载失败', e)
      this.setData({ playlists: [], filteredPlaylists: [], featured: null })
    } finally {
      this.setData({ loading: false })
    }
  },

  selectMood(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    this.setData({ selectedMoodId: id })
    this.filterPlaylists()
  },

  filterPlaylists() {
    let list = [...this.data.playlists]

    if (this.data.selectedMoodId > 0) {
      const tab = MOOD_TABS.find((t) => t.id === this.data.selectedMoodId)
      if (tab && tab.emotionType) {
        list = list.filter((p) => p.emotionType === tab.emotionType)
      }
    }

    this.setData({ filteredPlaylists: list })
  },

  onCoverError(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    const playlists = this.data.playlists.map((item) => {
      if (item.id === id) return { ...item, coverFailed: true }
      return item
    })
    this.setData({ playlists })
    this.filterPlaylists()
  },

  onPlaylistTap(e) {
    const item = e.currentTarget.dataset.item
    if (!item || !item.shareUrl) {
      wx.showToast({ title: '链接无效', icon: 'none' })
      return
    }
    this.copyAndPlay(item)
  },

  onFeaturedTap() {
    if (!this.data.featured) return
    this.copyAndPlay(this.data.featured)
  },

  copyAndPlay(item) {
    this.incrementPlayCount(item.id)

    wx.setClipboardData({
      data: item.shareUrl,
      success: () => {
        wx.showToast({
          title: '链接已复制，请在浏览器打开',
          icon: 'none',
          duration: 2500,
        })
      },
    })
  },

  incrementPlayCount(id) {
    request({ url: `/api/music/playlist/play/${id}`, method: 'POST' }).catch(() => {})

    const playlists = this.data.playlists.map((item) => {
      if (item.id === id) return { ...item, playCount: (item.playCount || 0) + 1 }
      return item
    })
    const featured = playlists.length > 0
      ? playlists.reduce((a, b) => (a.playCount || 0) >= (b.playCount || 0) ? a : b)
      : null
    this.setData({ playlists, featured })
    this.filterPlaylists()
  },

  onShareAppMessage() {
    return {
      title: '心灵驿站 · 音乐疗愈',
      path: '/pages/music-list/index',
    }
  },

  onShareTimeline() {
    return {
      title: '心灵驿站 · 音乐疗愈',
      path: '/pages/music-list/index',
    }
  },
})
