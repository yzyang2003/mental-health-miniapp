const MOCK_PLAYLISTS = [
  {
    id: 1,
    categoryName: '深度睡眠',
    categoryDesc: '助眠白噪音与舒缓旋律',
    coverImage: '/images/music/sleep_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=8656502115',
  },
  {
    id: 2,
    categoryName: '专注工作',
    categoryDesc: '提升效率的轻音乐',
    coverImage: '/images/music/focus_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=8614268177',
  },
  {
    id: 3,
    categoryName: '缓解焦虑',
    categoryDesc: '平复心情的治愈旋律',
    coverImage: '/images/music/anxiety_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=8451005609',
  },
  {
    id: 4,
    categoryName: '冥想放松',
    categoryDesc: '瑜伽与冥想专用音乐',
    coverImage: '/images/music/meditation_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=8353278325',
  },
  {
    id: 5,
    categoryName: '情绪释放',
    categoryDesc: '允许悲伤的疗愈歌单',
    coverImage: '/images/music/release_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=9584686054',
  },
  {
    id: 6,
    categoryName: '晨间唤醒',
    categoryDesc: '开启活力一天的轻快乐章',
    coverImage: '/images/music/morning_cover.png',
    shareUrl: 'https://music.163.com/#/playlist?id=8463165696',
  },
]

Page({
  data: {
    playlists: MOCK_PLAYLISTS.map((item) => ({
      ...item,
      coverFailed: !item.coverImage,
    })),
  },

  onCoverError(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) {
      return
    }
    const playlists = (this.data.playlists || []).map((item) => {
      if (item.id === id) {
        return {
          ...item,
          coverFailed: true,
        }
      }
      return item
    })
    this.setData({ playlists })
  },

  onCopyPlaylist(e) {
    const url = e.currentTarget.dataset.url || ''
    if (!url) {
      wx.showToast({
        title: '链接无效',
        icon: 'none',
      })
      return
    }
    if (!/^https?:\/\//.test(url)) {
      wx.showToast({
        title: '链接格式不正确',
        icon: 'none',
      })
      return
    }

    wx.showModal({
      title: '复制歌单链接',
      content:
        '将复制网易云音乐的第三方网页链接。复制后请打开手机浏览器，在地址栏粘贴并打开。',
      confirmText: '复制链接',
      cancelText: '取消',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        wx.setClipboardData({
          data: url,
        })
      },
    })
  },
})
