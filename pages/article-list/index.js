const { request } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

// 模糊搜索关键词表
const MENTAL_HEALTH_KEYWORDS = [
  '焦虑', '压力', '抑郁', '情绪', '睡眠', '自我', '关系',
  '内耗', '崩溃', '疗愈', '冥想', '放松', '宣泄', '创伤',
  '孤独', '减压', '关怀', '正念', '呼吸', '认知',
]

const FAVORITES_KEY = 'articleFavorites'
const READ_HISTORY_KEY = 'articleReadHistory'
const SEARCH_HISTORY_KEY = 'articleSearchHistory'

function formatTimestamp(ts) {
  const d = new Date(ts)
  const M = d.getMonth() + 1
  const D = d.getDate()
  const h = String(d.getHours()).padStart(2, '0')
  const m = String(d.getMinutes()).padStart(2, '0')
  return M + '月' + D + '日 ' + h + ':' + m
}

/** 将 ISO 时间字符串格式化为可读格式，去掉 T */
function formatIsoTime(isoStr) {
  if (!isoStr) return ''
  // 尝试匹配 2024-03-15T10:30:00 格式
  const match = isoStr.match(/^(\d{4})-(\d{1,2})-(\d{1,2})[T ](\d{1,2}):(\d{2})/)
  if (match) {
    const [, y, M, D, h, m] = match
    return y + '-' + M.padStart(2, '0') + '-' + D.padStart(2, '0') + ' ' + h.padStart(2, '0') + ':' + m
  }
  // 非 ISO 格式原样返回，去掉 T
  return isoStr.replace('T', ' ')
}

function extractKeywords(input) {
  if (!input) return []
  const parts = input.split(/[,，。、；;！!？?\s]+/).filter(Boolean)
  const result = [...parts]
  for (const kw of MENTAL_HEALTH_KEYWORDS) {
    if (input.includes(kw) && !result.includes(kw)) {
      result.push(kw)
    }
  }
  return result
}

/** 从文章列表中提取分类（取第一个 tag） */
function buildCategories(articles) {
  const seen = new Set()
  const cats = [{ id: 0, name: '全部', icon: '📋' }]
  const iconMap = {
    '焦虑调节': '🌊', '压力管理': '🧘', '情绪管理': '🌈',
    '自我关怀': '💛', '人际关系': '🤝', '睡眠改善': '🌙',
  }
  for (const a of articles) {
    const tag = (a.tags && a.tags[0]) || ''
    if (tag && !seen.has(tag)) {
      seen.add(tag)
      cats.push({ id: cats.length, name: tag, key: tag, icon: iconMap[tag] || '📄' })
    }
  }
  return cats
}

/** 适配后端 ArticleVO 为前端格式 */
function adaptArticle(raw) {
  const tag = (raw.tags && raw.tags[0]) || ''
  return {
    id: raw.id,
    title: raw.title || '',
    cover: raw.cover || '',
    summary: raw.summary || '',
    content: raw.content || '',
    contentUrl: raw.contentUrl || '',
    category: tag,
    viewCount: raw.viewCount || 0,
    updateTime: formatIsoTime(raw.updateTime || raw.createTime || ''),
  }
}

Page({
  data: {
    articleList: [],
    filteredArticles: [],
    categories: [{ id: 0, name: '全部', icon: '📋' }],
    selectedCategoryId: 0,
    filterMode: 'all',
    favorites: [],
    favoritesCount: 0,
    readHistory: [],
    searchKeyword: '',
    searchHistory: [],
    searchFocused: false,
    keyboardHeight: 0,
    capsuleTopPx: 24,
    capsuleRightPx: 16,
    isLoading: true,
    listTransition: false,
    showEmpty: false,
    scrollTop: 0,
    filterSectionTop: 400,
    showBackTop: false,
  },

  _loaded: false,

  onLoad() {
    this._heroBottomAbs = 0
    this.loadFavorites()
    this.loadReadHistory()
    this.loadSearchHistory()
    this.calcCapsule()
    this.loadArticles()
  },

  onShow() {
    if (!ensurePageLogin()) return
    this.calcCapsule()
    this.loadReadHistory()
    if (this.data.filterMode === 'history') {
      this.filterArticles()
    }
    if (!this.data.isLoading) {
      this._measureTimer = setTimeout(() => this.measureLayout(), 100)
    }
  },

  onReady() {
    this._measureTimer = setTimeout(() => this.measureLayout(), 300)
  },

  onUnload() {
    if (this._measureTimer) clearTimeout(this._measureTimer)
    if (this._backTopTimer) clearTimeout(this._backTopTimer)
  },

  onPullDownRefresh() {
    this.loadArticles().then(() => wx.stopPullDownRefresh())
  },

  async loadArticles() {
    this.setData({ isLoading: true })
    try {
      const res = await request({ url: '/api/article/list?page=1&size=50', timeout: 10000 })
      const rawList = res.records || []
      const articleList = rawList.map(adaptArticle)
      const categories = buildCategories(rawList)
      this.setData({ articleList, categories })
      this.filterArticles()
    } catch (e) {
      console.error('[article-list] 加载失败', e)
      this.setData({ articleList: [], filteredArticles: [] })
    } finally {
      this.setData({ isLoading: false })
      this._measureTimer = setTimeout(() => this.measureLayout(), 100)
    }
  },

  calcCapsule() {
    try {
      const info = wx.getMenuButtonBoundingClientRect()
      this.setData({
        capsuleTopPx: info.top,
        capsuleRightPx: wx.getSystemInfoSync().windowWidth - info.right + 4,
      })
      this._capsuleBottomAbs = info.top + info.height
    } catch (e) {
      this.setData({ capsuleTopPx: 24, capsuleRightPx: 16 })
      this._capsuleBottomAbs = 56
    }
  },

  // === 收藏 ===
  loadFavorites() {
    try {
      let favorites = wx.getStorageSync(FAVORITES_KEY) || []
      if (favorites.length > 0 && typeof favorites[0] === 'number') {
        favorites = favorites.map(id => ({ id, time: Date.now() }))
        wx.setStorageSync(FAVORITES_KEY, favorites)
      }
      this.setData({ favorites, favoritesCount: favorites.length })
    } catch (e) {
      this.setData({ favorites: [], favoritesCount: 0 })
    }
  },

  saveFavorites() {
    try { wx.setStorageSync(FAVORITES_KEY, this.data.favorites) } catch (e) {}
  },

  // === 阅读历史 ===
  loadReadHistory() {
    try {
      const history = wx.getStorageSync(READ_HISTORY_KEY) || []
      this.setData({ readHistory: history })
    } catch (e) {
      this.setData({ readHistory: [] })
    }
  },

  saveReadHistory(articleId) {
    try {
      let history = wx.getStorageSync(READ_HISTORY_KEY) || []
      history = history.filter(h => h.id !== articleId)
      history.unshift({ id: articleId, time: Date.now() })
      if (history.length > 50) history = history.slice(0, 50)
      wx.setStorageSync(READ_HISTORY_KEY, history)
      this.setData({ readHistory: history })
    } catch (e) {}
  },

  // === 搜索历史 ===
  loadSearchHistory() {
    try {
      this.setData({ searchHistory: wx.getStorageSync(SEARCH_HISTORY_KEY) || [] })
    } catch (e) {
      this.setData({ searchHistory: [] })
    }
  },

  saveSearchHistory(keyword) {
    try {
      let history = wx.getStorageSync(SEARCH_HISTORY_KEY) || []
      history = history.filter(k => k !== keyword)
      history.unshift(keyword)
      if (history.length > 10) history = history.slice(0, 10)
      wx.setStorageSync(SEARCH_HISTORY_KEY, history)
      this.setData({ searchHistory: history })
    } catch (e) {}
  },

  clearSearchHistory() {
    this.setData({ searchHistory: [] })
    try { wx.removeStorageSync(SEARCH_HISTORY_KEY) } catch (e) {}
  },

  // === 滚动 ===
  onPageScroll(e) {
    const scrollTop = e.scrollTop
    this._lastScrollTop = scrollTop
    if (this._scrollThrottled) return
    this._scrollThrottled = true
    setTimeout(() => { this._scrollThrottled = false }, 100)
    this._checkBackTop(scrollTop)
    if (this._backTopTimer) clearTimeout(this._backTopTimer)
    this._backTopTimer = setTimeout(() => this._checkBackTop(this._lastScrollTop), 100)
  },

  _checkBackTop(scrollTop) {
    if (this._heroBottomAbs > 0 && this._capsuleBottomAbs > 0) {
      const threshold = this._heroBottomAbs - this._capsuleBottomAbs
      const hide = scrollTop <= threshold
      if (hide !== !this.data.showBackTop) {
        this.setData({ showBackTop: !hide })
      }
    } else {
      if (scrollTop <= 10 && this.data.showBackTop) {
        this.setData({ showBackTop: false })
      }
    }
  },

  measureLayout() {
    const query = this.createSelectorQuery()
    query.select('.hero-card').boundingClientRect()
    query.select('.filter-section').boundingClientRect()
    query.exec((res) => {
      if (res && res[0]) {
        const st = this._lastScrollTop || 0
        this._heroBottomAbs = res[0].bottom + st
      }
      if (res && res[1]) {
        const st = this._lastScrollTop || 0
        const filterAbsTop = res[1].top + st
        this.setData({ filterSectionTop: filterAbsTop > 0 ? filterAbsTop : 300 })
      }
    })
  },

  scrollToTop() {
    this.setData({ showBackTop: false })
    wx.pageScrollTo({ scrollTop: 0, duration: 300 })
  },

  // === 搜索 ===
  onSearchFocus() { this.setData({ searchFocused: true }) },
  onSearchBlur() { this.setData({ searchFocused: false, keyboardHeight: 0 }) },
  onKeyboardHeightChange(e) { this.setData({ keyboardHeight: e.detail.height || 0 }) },

  onSearchInput(e) {
    const value = e.detail.value || ''
    this.setData({ searchKeyword: value })
    if (!value) {
      this.triggerListTransition(() => this.filterArticles())
    }
  },

  onSearchConfirm() { this.executeSearch() },
  onSearchSubmit() { this.executeSearch() },

  executeSearch() {
    const kw = this.data.searchKeyword.trim()
    if (kw) this.saveSearchHistory(kw)
    this.triggerListTransition(() => this.filterArticles())
  },

  clearSearch() {
    this.setData({ searchKeyword: '' })
    this.triggerListTransition(() => this.filterArticles())
  },

  onHistoryTagTap(e) {
    const keyword = e.currentTarget.dataset.keyword
    this.setData({ searchKeyword: keyword })
    this.triggerListTransition(() => {
      this.filterArticles()
      this.saveSearchHistory(keyword)
    })
  },

  // === 分类/筛选 ===
  selectCategory(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    this.triggerListTransition(() => {
      this.setData({ selectedCategoryId: id, filterMode: 'all' })
      this.filterArticles()
    })
  },

  switchFilterMode(e) {
    const mode = e.currentTarget.dataset.mode
    this.triggerListTransition(() => {
      if (mode === this.data.filterMode) {
        this.setData({ filterMode: 'all' })
      } else {
        this.setData({ filterMode: mode, selectedCategoryId: 0 })
      }
      this.filterArticles()
    })
  },

  triggerListTransition(callback) {
    this.setData({ listTransition: true })
    setTimeout(() => {
      if (callback) callback()
      setTimeout(() => this.setData({ listTransition: false }), 50)
    }, 200)
  },

  // === 筛选逻辑 ===
  filterArticles() {
    let articles = [...this.data.articleList]

    // 分类筛选
    if (this.data.selectedCategoryId > 0) {
      const category = this.data.categories.find(c => c.id === this.data.selectedCategoryId)
      if (category && category.key) {
        articles = articles.filter(a => a.category === category.key)
      }
    }

    // 搜索
    if (this.data.searchKeyword) {
      const inputKws = extractKeywords(this.data.searchKeyword)
      articles = articles.filter(a => {
        const haystack = (a.title + a.summary + a.category).toLowerCase()
        if (haystack.includes(this.data.searchKeyword.toLowerCase())) return true
        for (const kw of inputKws) {
          if (haystack.includes(kw.toLowerCase())) return true
        }
        return false
      })
    }

    // 收藏/历史
    if (this.data.filterMode === 'favorites') {
      const favIds = this.data.favorites.map(f => f.id)
      articles = articles.filter(a => favIds.includes(a.id))
    } else if (this.data.filterMode === 'history') {
      const histIds = this.data.readHistory.map(h => h.id)
      articles = articles
        .filter(a => histIds.includes(a.id))
        .sort((a, b) => {
          const tA = (this.data.readHistory.find(h => h.id === a.id) || {}).time || 0
          const tB = (this.data.readHistory.find(h => h.id === b.id) || {}).time || 0
          return tA - tB
        })
    }

    // 添加时间戳
    if (this.data.filterMode === 'favorites') {
      const favMap = {}
      this.data.favorites.forEach(f => { favMap[f.id] = f.time })
      articles = articles.map(a => ({
        ...a,
        _favTime: favMap[a.id] ? formatTimestamp(favMap[a.id]) : '',
        _histTime: '',
      }))
    } else if (this.data.filterMode === 'history') {
      const histMap = {}
      this.data.readHistory.forEach(h => { histMap[h.id] = h.time })
      articles = articles.map(a => ({
        ...a,
        _favTime: '',
        _histTime: histMap[a.id] ? formatTimestamp(histMap[a.id]) : '',
      }))
    }

    this.setData({ filteredArticles: articles, showEmpty: true })
  },

  // === 收藏操作 ===
  toggleFavorite(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    let favorites = [...this.data.favorites]
    const index = favorites.findIndex(f => f.id === id)
    if (index > -1) {
      favorites.splice(index, 1)
      wx.showToast({ title: '已取消收藏', icon: 'none' })
    } else {
      favorites.unshift({ id, time: Date.now() })
      wx.showToast({ title: '已收藏', icon: 'success' })
    }
    this.setData({ favorites, favoritesCount: favorites.length })
    this.saveFavorites()
    if (this.data.filterMode === 'favorites') this.filterArticles()
  },

  getTimestamp(articleId) {
    if (this.data.filterMode === 'favorites') {
      const fav = this.data.favorites.find(f => f.id === articleId)
      return fav ? formatTimestamp(fav.time) : ''
    }
    if (this.data.filterMode === 'history') {
      const hist = this.data.readHistory.find(h => h.id === articleId)
      return hist ? formatTimestamp(hist.time) : ''
    }
    return ''
  },

  // === 点击文章 → 跳转详情页 ===
  onArticleTap(e) {
    const item = e.currentTarget.dataset.item
    if (!item || !item.id) {
      wx.showToast({ title: '文章无效', icon: 'none' })
      return
    }
    this.saveReadHistory(item.id)
    wx.navigateTo({ url: `/pages/article-detail/index?id=${item.id}` })
  },
})
