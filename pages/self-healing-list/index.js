const {
  adaptExercise,
  buildCategories,
  mergeCompleted,
  filterExercises,
} = require('../../utils/selfHealingCatalog')
const { request } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

const COMPLETED_COUNT_KEY = 'selfHealingCompletedCount'
const STREAK_KEY = 'selfHealingStreak'
const LAST_COMPLETION_KEY = 'selfHealingLastCompletion'

Page({
  data: {
    categories: [{ id: 0, name: '全部练习' }],
    selectedCategoryId: 0,
    exercises: [],
    filteredExercises: [],
    completedCount: 0,
    streak: 0,
    todayCompleted: false,
    showReminder: false,
    reminderTime: '21:00',
    loading: true,
    capsuleTopPx: 0,
    capsuleRightPx: 0,
  },

  _loaded: false,

  onShow() {
    if (!ensurePageLogin()) return
    this.calcCapsule()
    this.loadStats()
    if (!this._loaded) {
      this._loaded = true
      this.loadExercises()
    } else {
      this.refreshList()
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
    this.loadExercises().then(() => wx.stopPullDownRefresh())
  },

  loadStats() {
    try {
      const completedCount = wx.getStorageSync(COMPLETED_COUNT_KEY) || 0
      const streak = wx.getStorageSync(STREAK_KEY) || 0
      const lastCompletion = wx.getStorageSync(LAST_COMPLETION_KEY) || ''
      const today = new Date().toDateString()
      this.setData({
        completedCount,
        streak,
        todayCompleted: lastCompletion === today,
      })
    } catch (e) {
      this.setData({ completedCount: 0, streak: 0, todayCompleted: false })
    }
  },

  async loadExercises() {
    this.setData({ loading: true })
    try {
      const res = await request({ url: '/api/selfHealing/list?page=1&size=50', timeout: 10000 })
      const rawList = res.records || []
      const exercises = rawList.map(adaptExercise)
      const categories = buildCategories(rawList)
      this.setData({ exercises, categories })
      this.refreshList()
    } catch (e) {
      console.error('[self-healing-list] 加载失败', e)
      this.setData({ exercises: [], filteredExercises: [] })
    } finally {
      this.setData({ loading: false })
    }
  },

  refreshList() {
    const cat = this.data.categories.find((c) => c.id === this.data.selectedCategoryId)
    const issueType = cat && cat.issueType ? cat.issueType : ''
    const merged = mergeCompleted(this.data.exercises)
    const filtered = filterExercises(merged, issueType)
    this.setData({ filteredExercises: filtered })
  },

  selectCategory(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    this.setData({ selectedCategoryId: id })
    this.refreshList()
  },

  openDetail(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) return
    wx.navigateTo({ url: `/pages/self-healing-detail/index?id=${id}` })
  },

  toggleReminder() {
    this.setData({ showReminder: !this.data.showReminder })
  },

  onReminderTimeChange(e) {
    this.setData({ reminderTime: e.detail.value })
  },

  setReminder() {
    wx.showToast({ title: `已设置每日 ${this.data.reminderTime} 提醒`, icon: 'success' })
    this.setData({ showReminder: false })
  },

  getRandomRecommendation() {
    const uncompleted = this.data.exercises.filter((e) => !e.completed)
    const pool = uncompleted.length > 0 ? uncompleted : this.data.exercises
    return pool[Math.floor(Math.random() * pool.length)]
  },

  onRecommendTap() {
    const recommend = this.getRandomRecommendation()
    if (recommend) {
      this.openDetail({ currentTarget: { dataset: { id: recommend.id } } })
    }
  },

  updateStreak() {
    const today = new Date().toDateString()
    const lastCompletion = wx.getStorageSync(LAST_COMPLETION_KEY) || ''
    if (lastCompletion === today) return

    const yesterday = new Date()
    yesterday.setDate(yesterday.getDate() - 1)
    const yesterdayStr = yesterday.toDateString()

    let newStreak = this.data.streak
    if (lastCompletion === yesterdayStr) {
      newStreak += 1
    } else {
      newStreak = 1
    }

    try {
      wx.setStorageSync(LAST_COMPLETION_KEY, today)
      wx.setStorageSync(STREAK_KEY, newStreak)
      this.setData({
        streak: newStreak,
        todayCompleted: true,
        completedCount: this.data.completedCount + 1,
      })
      wx.setStorageSync(COMPLETED_COUNT_KEY, this.data.completedCount)
    } catch (e) { /* ignore */ }
  },
})
