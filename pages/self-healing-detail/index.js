const { adaptExercise, writeCompleted } = require('../../utils/selfHealingCatalog')
const { request } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    exerciseId: 0,
    detail: null,
    stepTabs: [],
    activeStepIndex: 0,
    completedStepIndexes: [],
    completionMessage: '',
    actionSlot3: 'next',
    nextDisabled: true,
    finishStepDisabled: false,
    principleExpanded: false,
    progressPercent: 0,
    errorMessage: '',
    loading: true,
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    // 计时器
    timerRunning: false,
    timerSeconds: 0,
    timerDisplay: '00:00',
    // 庆祝
    showCelebration: false,
    totalTime: 0,
  },

  _timerInterval: null,
  _stepStartTime: 0,

  onLoad(options) {
    if (!ensurePageLogin()) return

    const id = Number(options.id || 0)
    if (!id) {
      this.setData({ errorMessage: '无效的练习 ID', loading: false })
      return
    }
    this.setData({ exerciseId: id })
    this.calcCapsule()
    this.fetchDetail(id)
  },

  onUnload() {
    this._clearTimer()
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

  async fetchDetail(id) {
    this.setData({ loading: true })
    try {
      const raw = await request({ url: `/api/selfHealing/${id}`, timeout: 10000 })
      const detail = adaptExercise(raw)
      const steps = detail.steps || []

      this.setData({
        detail,
        activeStepIndex: 0,
        completedStepIndexes: [],
        completionMessage: '',
        principleExpanded: false,
        progressPercent: steps.length ? Math.round((1 / steps.length) * 100) : 0,
        errorMessage: '',
        stepTabs: this.buildStepTabs(steps, 0, []),
        ...this.computeActionUi({ activeStepIndex: 0, completedStepIndexes: [], steps }),
      })
    } catch (e) {
      console.error('[self-healing-detail] 加载失败', e)
      this.setData({ errorMessage: '加载练习内容失败，请重试' })
    } finally {
      this.setData({ loading: false })
    }
  },

  // === 原理展开 ===
  togglePrinciple() {
    this.setData({ principleExpanded: !this.data.principleExpanded })
  },

  // === 步骤导航 ===
  selectStep(e) {
    const index = Number(e.currentTarget.dataset.index || 0)
    this.updateActiveStep(index)
  },

  prevStep() {
    const prevIndex = Math.max(this.data.activeStepIndex - 1, 0)
    this.updateActiveStep(prevIndex)
  },

  nextStep() {
    const steps = (this.data.detail && this.data.detail.steps) || []
    if (!steps.length) return
    const index = this.data.activeStepIndex
    if (!this.isStepCompleted(index)) {
      wx.showToast({ title: '请先完成本步', icon: 'none' })
      return
    }
    if (index >= steps.length - 1) return
    this.updateActiveStep(index + 1)
  },

  updateActiveStep(index) {
    const steps = (this.data.detail && this.data.detail.steps) || []
    const safeIndex = Math.min(Math.max(index, 0), Math.max(steps.length - 1, 0))
    const progressPercent = steps.length
      ? Math.round(((safeIndex + 1) / steps.length) * 100)
      : 0
    const completedStepIndexes = this.data.completedStepIndexes || []

    this._clearTimer()

    this.setData({
      activeStepIndex: safeIndex,
      completionMessage: '',
      progressPercent,
      timerRunning: false,
      timerSeconds: 0,
      timerDisplay: '00:00',
      stepTabs: this.buildStepTabs(steps, safeIndex, completedStepIndexes),
      ...this.computeActionUi({ activeStepIndex: safeIndex, completedStepIndexes, steps }),
    })
  },

  isStepCompleted(index) {
    return (this.data.completedStepIndexes || []).includes(index)
  },

  // === 计时器 ===
  toggleTimer() {
    if (this.data.timerRunning) {
      this._clearTimer()
      this.setData({ timerRunning: false })
    } else {
      this._stepStartTime = Date.now()
      this._timerInterval = setInterval(() => {
        const elapsed = Math.floor((Date.now() - this._stepStartTime) / 1000)
        const mins = String(Math.floor(elapsed / 60)).padStart(2, '0')
        const secs = String(elapsed % 60).padStart(2, '0')
        this.setData({
          timerSeconds: elapsed,
          timerDisplay: `${mins}:${secs}`,
        })
      }, 1000)
      this.setData({ timerRunning: true })
    }
  },

  _clearTimer() {
    if (this._timerInterval) {
      clearInterval(this._timerInterval)
      this._timerInterval = null
    }
  },

  // === 完成步骤 ===
  markStepDone() {
    const index = this.data.activeStepIndex
    const steps = (this.data.detail && this.data.detail.steps) || []
    const totalSteps = steps.length
    if (!totalSteps) return

    if (this.isStepCompleted(index)) {
      wx.showToast({ title: '本步已完成', icon: 'none' })
      return
    }

    this._clearTimer()

    const doneSet = new Set(this.data.completedStepIndexes || [])
    doneSet.add(index)
    const completedStepIndexes = Array.from(doneSet).sort((a, b) => a - b)

    const isLast = index >= totalSteps - 1
    const completionMessage = isLast
      ? '已完成最后一步，点击「完成练习」结束。'
      : '本步已完成，可点击「下一步」继续。'

    this.setData({
      completedStepIndexes,
      completionMessage,
      timerRunning: false,
      stepTabs: this.buildStepTabs(steps, index, completedStepIndexes),
      ...this.computeActionUi({ activeStepIndex: index, completedStepIndexes, steps }),
    })
  },

  // === 完成练习 ===
  completeExercise() {
    const steps = (this.data.detail && this.data.detail.steps) || []
    if (!steps.length) return
    if (this.data.activeStepIndex !== steps.length - 1) {
      wx.showToast({ title: '请先完成到最后一步', icon: 'none' })
      return
    }
    if (!this.isStepCompleted(this.data.activeStepIndex)) {
      wx.showToast({ title: '请先完成当前步骤', icon: 'none' })
      return
    }

    writeCompleted(this.data.exerciseId, true)

    const totalTime = this.data.completedStepIndexes.length > 0
      ? this.data.timerSeconds
      : 0

    this.setData({
      showCelebration: true,
      totalTime,
    })
  },

  onCelebrationBack() {
    wx.navigateBack()
  },

  onCelebrationRestart() {
    this.setData({ showCelebration: false })
    this.restartPractice()
  },

  // === 重新练习 ===
  restartPractice() {
    writeCompleted(this.data.exerciseId, false)
    this._clearTimer()
    const steps = (this.data.detail && this.data.detail.steps) || []
    this.setData({
      activeStepIndex: 0,
      completedStepIndexes: [],
      completionMessage: '',
      principleExpanded: false,
      progressPercent: steps.length ? Math.round((1 / steps.length) * 100) : 0,
      timerRunning: false,
      timerSeconds: 0,
      timerDisplay: '00:00',
      showCelebration: false,
      stepTabs: this.buildStepTabs(steps, 0, []),
      ...this.computeActionUi({ activeStepIndex: 0, completedStepIndexes: [], steps }),
    })
  },

  // === 工具方法 ===
  buildStepTabs(steps, activeIndex, completedStepIndexes) {
    const completedSet = new Set(completedStepIndexes || [])
    return (steps || []).map((step, index) => {
      const isActive = index === activeIndex
      const isDone = completedSet.has(index)
      const tabClass = `step-tab${isActive ? ' step-tab-active' : ''}${isDone ? ' step-tab-done' : ''}`
      return { index, label: String(index + 1), tabClass }
    })
  },

  computeActionUi({ activeStepIndex, completedStepIndexes, steps }) {
    const totalSteps = (steps || []).length
    const isLast = totalSteps ? activeStepIndex >= totalSteps - 1 : false
    const doneList = Array.isArray(completedStepIndexes) ? completedStepIndexes : []
    const currentDone = doneList.includes(activeStepIndex)

    const actionSlot3 = isLast && currentDone ? 'finishExercise' : 'next'
    const nextDisabled = isLast ? true : !currentDone
    const finishStepDisabled = isLast && currentDone

    return { actionSlot3, nextDisabled, finishStepDisabled }
  },

  // === 分享 ===
  onShareAppMessage() {
    return {
      title: '自我疗愈 - ' + (this.data.detail ? this.data.detail.title : '心灵驿站'),
      path: '/pages/self-healing-detail/index?id=' + this.data.exerciseId,
    }
  },
})
