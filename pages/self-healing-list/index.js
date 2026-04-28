const {
  CATEGORIES,
  EXERCISES,
  decorateExercises,
  mergeCompleted,
  filterExercises,
} = require('../../utils/selfHealingCatalog')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    categories: CATEGORIES,
    selectedCategoryId: 0,
    exercises: [],
    filteredExercises: [],
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.refreshList()
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
    this.refreshList()
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  refreshList() {
    const base = decorateExercises(EXERCISES)
    const merged = mergeCompleted(base)
    const filtered = filterExercises(merged, this.data.selectedCategoryId)
    this.setData({
      exercises: merged,
      filteredExercises: filtered,
    })
  },

  selectCategory(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    this.setData({
      selectedCategoryId: id,
      filteredExercises: filterExercises(this.data.exercises, id),
    })
  },

  openDetail(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) {
      return
    }
    wx.navigateTo({
      url: `/pages/self-healing-detail/index?id=${id}`,
    })
  },
})
