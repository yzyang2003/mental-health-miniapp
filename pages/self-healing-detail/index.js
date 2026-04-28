const { EXERCISE_BY_ID, writeCompleted } = require('../../utils/selfHealingCatalog')
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
  },

  onLoad(options) {
    if (!this.ensureLogin()) {
      return
    }

    const id = Number(options.id || 0)
    if (!id) {
      this.setData({
        errorMessage: '无效的练习 ID',
      })
      return
    }

    const exercise = EXERCISE_BY_ID[id]
    if (!exercise) {
      this.setData({
        errorMessage: '未找到对应练习内容',
      })
      return
    }

    const steps = Array.isArray(exercise.steps) ? exercise.steps : []
    const detail = {
      ...exercise,
      steps,
    }

    this.setData({
      exerciseId: id,
      detail,
      activeStepIndex: 0,
      completedStepIndexes: [],
      completionMessage: '',
      principleExpanded: false,
      progressPercent: steps.length ? Math.round((1 / steps.length) * 100) : 0,
      errorMessage: '',
      stepTabs: this.buildStepTabs(steps, 0, []),
      ...this.computeActionUi({
        activeStepIndex: 0,
        completedStepIndexes: [],
        steps,
      }),
    })
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  togglePrinciple() {
    this.setData({
      principleExpanded: !this.data.principleExpanded,
    })
  },

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
    if (!steps.length) {
      return
    }
    const index = this.data.activeStepIndex
    if (!this.isStepCompleted(index)) {
      wx.showToast({
        title: '请先完成本步',
        icon: 'none',
      })
      return
    }
    if (index >= steps.length - 1) {
      return
    }
    this.updateActiveStep(index + 1)
  },

  updateActiveStep(index) {
    const steps = (this.data.detail && this.data.detail.steps) || []
    const safeIndex = Math.min(Math.max(index, 0), Math.max(steps.length - 1, 0))
    const progressPercent = steps.length
      ? Math.round(((safeIndex + 1) / steps.length) * 100)
      : 0
    const completedStepIndexes = this.data.completedStepIndexes || []
    this.setData({
      activeStepIndex: safeIndex,
      completionMessage: '',
      progressPercent,
      stepTabs: this.buildStepTabs(steps, safeIndex, completedStepIndexes),
      ...this.computeActionUi({
        activeStepIndex: safeIndex,
        completedStepIndexes,
        steps,
      }),
    })
  },

  isStepCompleted(index) {
    return (this.data.completedStepIndexes || []).includes(index)
  },

  markStepDone() {
    const index = this.data.activeStepIndex
    const steps = (this.data.detail && this.data.detail.steps) || []
    const totalSteps = steps.length
    if (!totalSteps) {
      return
    }

    if (this.isStepCompleted(index)) {
      wx.showToast({
        title: '本步已完成',
        icon: 'none',
      })
      return
    }

    const doneSet = new Set(this.data.completedStepIndexes || [])
    doneSet.add(index)
    const completedStepIndexes = Array.from(doneSet).sort((a, b) => a - b)

    const isLast = index >= totalSteps - 1
    const completionMessage = isLast
      ? '已完成最后一步：第三格已切换为「完成练习」，请点击结束本次练习。'
      : '本步已完成，可点击「下一步」继续。'

    this.setData({
      completedStepIndexes,
      completionMessage,
      stepTabs: this.buildStepTabs(steps, index, completedStepIndexes),
      ...this.computeActionUi({
        activeStepIndex: index,
        completedStepIndexes,
        steps,
      }),
    })
  },

  completeExercise() {
    const steps = (this.data.detail && this.data.detail.steps) || []
    if (!steps.length) {
      return
    }
    if (this.data.activeStepIndex !== steps.length - 1) {
      wx.showToast({
        title: '请先完成到最后一步',
        icon: 'none',
      })
      return
    }
    if (!this.isStepCompleted(this.data.activeStepIndex)) {
      wx.showToast({
        title: '请先完成当前步骤',
        icon: 'none',
      })
      return
    }

    writeCompleted(this.data.exerciseId, true)
    wx.showToast({
      title: '练习完成',
      icon: 'none',
    })
    setTimeout(() => {
      wx.navigateBack()
    }, 450)
  },

  restartPractice() {
    writeCompleted(this.data.exerciseId, false)
    const steps = (this.data.detail && this.data.detail.steps) || []
    this.setData({
      activeStepIndex: 0,
      completedStepIndexes: [],
      completionMessage: '已重置练习，从第一步开始。',
      principleExpanded: false,
      progressPercent: steps.length ? Math.round((1 / steps.length) * 100) : 0,
      stepTabs: this.buildStepTabs(steps, 0, []),
      ...this.computeActionUi({
        activeStepIndex: 0,
        completedStepIndexes: [],
        steps,
      }),
    })
  },

  buildStepTabs(steps, activeIndex, completedStepIndexes) {
    const completedSet = new Set(completedStepIndexes || [])
    return (steps || []).map((step, index) => {
      const isActive = index === activeIndex
      const isDone = completedSet.has(index)
      const tabClass = `step-tab${isActive ? ' step-tab-active' : ''}${isDone ? ' step-tab-done' : ''}`
      return {
        index,
        label: String(index + 1),
        tabClass,
      }
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

    return {
      actionSlot3,
      nextDisabled,
      finishStepDisabled,
    }
  },
})
