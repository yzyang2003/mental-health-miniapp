const { request, formatRequestError } = require('../../utils/request')
const { normalizeQuizDetail, normalizeQuizResult } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')

const MIN_SUBMIT_OVERLAY_MS = 2000

Page({
  data: {
    loading: false,
    submitting: false,
    questionnaireId: null,
    detail: null,
    selectedAnswers: {},
    errorMessage: '',
    answeredCount: 0,
    totalQuestions: 0,
    progressPercent: 0,
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    currentQuestionIndex: 0,
    isTransitioning: false,
    viewMode: 'single',
    // 未完成题目导航模式
    unansweredNavMode: false,
    unansweredQueue: [],
    currentQueueIdx: 0,
    // 是否全部答完
    allAnswered: false,
  },
  _autoAdvanceTimer: null,
  _capsuleCached: false,

  onLoad(options) {
    if (!this.ensureLogin()) {
      return
    }

    const questionnaireId = Number(options.id || 0)
    if (!questionnaireId) {
      this.setData({ errorMessage: '无效的问卷 ID' })
      return
    }

    this.setData({ questionnaireId })
    this.fetchDetail(questionnaireId)
  },

  onShow() {
    if (!this._capsuleCached) {
      this.calcCapsule()
    }
  },

  onUnload() {
    if (this._autoAdvanceTimer) {
      clearTimeout(this._autoAdvanceTimer)
      this._autoAdvanceTimer = null
    }
  },

  calcCapsule() {
    try {
      const info = wx.getMenuButtonBoundingClientRect()
      this.setData({
        capsuleTopPx: info.top,
        capsuleRightPx: wx.getSystemInfoSync().windowWidth - info.right + 4,
      })
      this._capsuleCached = true
    } catch (e) {
      this.setData({ capsuleTopPx: 24, capsuleRightPx: 16 })
      this._capsuleCached = true
    }
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchDetail(id) {
    this.setData({ loading: true, errorMessage: '' })

    request({
      url: `/api/consult/quiz/${id}`,
      method: 'GET',
    })
      .then((data) => {
        const detail = normalizeQuizDetail(data)
        const totalQuestions = detail.questions ? detail.questions.length : 0
        this.setData({
          detail,
          totalQuestions,
          answeredCount: 0,
          progressPercent: 0,
        })
        this.updateCurrentQuestion()
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取问卷详情失败'),
        })
      })
      .finally(() => {
        this.setData({ loading: false })
      })
  },

  preventTouchMove() {
    /* 遮罩层阻止穿透滚动 */
  },

  // 更新当前题目缓存（避免 WXML 深层访问）
  updateCurrentQuestion() {
    const { detail, currentQuestionIndex, selectedAnswers } = this.data
    if (!detail || !detail.questions) {
      this.setData({ currentQuestion: null, currentSelectedOption: -1 })
      return
    }
    const question = detail.questions[currentQuestionIndex]
    if (question) {
      this.setData({
        currentQuestion: question,
        currentSelectedOption: selectedAnswers[question.id] !== undefined ? selectedAnswers[question.id] : -1,
      })
    }
  },

  selectOption(e) {
    const questionId = e.currentTarget.dataset.questionId
    const optionIndex = Number(e.currentTarget.dataset.optionIndex)
    if (!questionId && questionId !== 0) {
      return
    }

    const { detail, unansweredNavMode, currentQuestionIndex, selectedAnswers } = this.data
    const updates = {}
    let unansweredQueue = this.data.unansweredQueue
    let currentQueueIdx = this.data.currentQueueIdx
    let navMode = unansweredNavMode

    // 判断该题目是否之前已答过（修改答案 vs 首次作答）
    const alreadyAnswered = selectedAnswers[questionId] !== undefined

    // 保存答案
    updates[`selectedAnswers.${questionId}`] = optionIndex

    // 更新进度：重新计算已答数量，避免重复计数
    const newSelectedAnswers = { ...selectedAnswers, [questionId]: optionIndex }
    const answeredCount = Object.keys(newSelectedAnswers).length
    const totalQuestions = this.data.totalQuestions
    const allAnswered = answeredCount >= totalQuestions
    updates.answeredCount = answeredCount
    updates.totalQuestions = totalQuestions
    updates.progressPercent = Math.round(answeredCount / totalQuestions * 100)
    updates.allAnswered = allAnswered

    // 更新未完成队列
    let wasNavMode = navMode
    if (navMode) {
      const clickedIndex = detail.questions.findIndex((q) => q.id === questionId)
      unansweredQueue = unansweredQueue.filter((idx) => idx !== clickedIndex)

      if (unansweredQueue.length === 0) {
        navMode = false
        currentQueueIdx = 0
      } else {
        currentQueueIdx = Math.min(currentQueueIdx, unansweredQueue.length - 1)
      }
      updates.unansweredQueue = unansweredQueue
      updates.currentQueueIdx = currentQueueIdx
      updates.unansweredNavMode = navMode
    }

    this.setData(updates)

    // 自动跳转（仅单题模式）
    if (this.data.viewMode === 'single') {
      // 如果刚刚从导航模式退出（答完最后一道未答题），不自动跳转
      if (wasNavMode && !navMode) {
        // 不跳转，让用户看到当前题目和提交按钮
      } else if (navMode && currentQueueIdx < unansweredQueue.length) {
        // 未完成导航模式，还有下一题
        this.scheduleAutoAdvance(unansweredQueue[currentQueueIdx])
      } else if (!navMode && currentQuestionIndex < totalQuestions - 1) {
        // 普通模式
        this.scheduleAutoAdvance(currentQuestionIndex + 1)
      }
    }

    // 更新当前题目缓存
    this.updateCurrentQuestion()
  },

  scheduleAutoAdvance(targetIndex) {
    if (this._autoAdvanceTimer) {
      clearTimeout(this._autoAdvanceTimer)
    }
    this._autoAdvanceTimer = setTimeout(() => {
      this._autoAdvanceTimer = null
      this.setData({ currentQuestionIndex: targetIndex })
      this.updateCurrentQuestion()
    }, 300)
  },

  // 通用跳转方法
  jumpToQuestion(targetIndex, options = {}) {
    const { withTransition = true, callback } = options
    if (withTransition) {
      this.setData({ isTransitioning: true })
      setTimeout(() => {
        this.setData({ currentQuestionIndex: targetIndex, isTransitioning: false })
        this.updateCurrentQuestion()
        if (callback) callback()
      }, 150)
    } else {
      this.setData({ currentQuestionIndex: targetIndex })
      this.updateCurrentQuestion()
      if (callback) callback()
    }
  },

  // 下一题
  goNext() {
    const { unansweredNavMode, unansweredQueue, currentQueueIdx, currentQuestionIndex, totalQuestions } = this.data

    if (unansweredNavMode && unansweredQueue.length > 0) {
      const nextIdx = currentQueueIdx + 1
      if (nextIdx < unansweredQueue.length) {
        this.jumpToQuestion(unansweredQueue[nextIdx], {
          callback: () => this.setData({ currentQueueIdx: nextIdx }),
        })
      }
      return
    }

    if (currentQuestionIndex < totalQuestions - 1) {
      this.jumpToQuestion(currentQuestionIndex + 1)
    }
  },

  // 上一题
  goPrev() {
    const { unansweredNavMode, unansweredQueue, currentQueueIdx, currentQuestionIndex } = this.data

    if (unansweredNavMode && unansweredQueue.length > 0) {
      const prevIdx = currentQueueIdx - 1
      if (prevIdx >= 0) {
        this.jumpToQuestion(unansweredQueue[prevIdx], {
          callback: () => this.setData({ currentQueueIdx: prevIdx }),
        })
      }
      return
    }

    if (currentQuestionIndex > 0) {
      this.jumpToQuestion(currentQuestionIndex - 1)
    }
  },

  // 跳转到指定题目
  goToQuestion(e) {
    const index = e.currentTarget.dataset.index
    if (index >= 0 && index < this.data.totalQuestions) {
      this.jumpToQuestion(index)
    }
  },

  // 跳转到未答题目
  goToUnansweredQuestion(index) {
    if (index < 0 || index >= this.data.totalQuestions) {
      return
    }

    if (this.data.viewMode === 'single') {
      const queue = this.buildUnansweredQueue(index)
      this.jumpToQuestion(queue[0], {
        callback: () => {
          this.setData({
            unansweredNavMode: true,
            unansweredQueue: queue,
            currentQueueIdx: 0,
          })
        },
      })
    } else {
      // 瀑布流模式：滚动到该题目（定位到屏幕上四分之一处）
      this.scrollToQuestion(index)
    }
  },

  // 滚动到指定题目（定位到屏幕上四分之一处）
  scrollToQuestion(index) {
    const query = wx.createSelectorQuery()
    query.select(`#question-${index}`).boundingClientRect()
    query.selectViewport().scrollOffset()
    query.exec((res) => {
      if (!res || !res[0] || !res[1]) return
      const elementTop = res[0].top
      const scrollTop = res[1].scrollTop
      // 目标位置：屏幕上四分之一处（胶囊位置 + 页面 padding）
      const targetOffset = wx.getSystemInfoSync().windowHeight * 0.25
      const scrollTo = scrollTop + elementTop - targetOffset
      wx.pageScrollTo({
        scrollTop: Math.max(0, scrollTo),
        duration: 300,
      })
    })
  },

  // 构建未答题目队列
  buildUnansweredQueue(startIndex) {
    const { detail, selectedAnswers } = this.data
    if (!detail || !detail.questions) {
      return []
    }

    const unanswered = detail.questions
      .map((q, idx) => ({ idx, answered: selectedAnswers[q.id] !== undefined }))
      .filter((item) => !item.answered)
      .map((item) => item.idx)
      .sort((a, b) => a - b)

    const startIdx = unanswered.indexOf(startIndex)
    if (startIdx < 0) {
      return unanswered
    }

    return [...unanswered.slice(startIdx), ...unanswered.slice(0, startIdx)]
  },

  toggleViewMode() {
    const nextMode = this.data.viewMode === 'single' ? 'waterfall' : 'single'
    const { unansweredNavMode, unansweredQueue, currentQueueIdx } = this.data

    if (unansweredNavMode && unansweredQueue.length > 0) {
      const queueIdx = Math.min(currentQueueIdx, unansweredQueue.length - 1)
      const targetIndex = unansweredQueue[queueIdx]

      if (nextMode === 'single') {
        // 切回单题模式，恢复导航状态并跳转
        this.jumpToQuestion(targetIndex, {
          callback: () => this.setData({ currentQueueIdx: queueIdx }),
        })
        this.setData({ viewMode: nextMode })
      } else {
        // 切到瀑布流，滚动到当前题目（定位到屏幕上四分之一处）
        this.setData({ viewMode: nextMode })
        setTimeout(() => {
          this.scrollToQuestion(targetIndex)
        }, 100)
      }
    } else {
      this.setData({
        viewMode: nextMode,
        unansweredNavMode: false,
        unansweredQueue: [],
        currentQueueIdx: 0,
      })
    }
  },

  fillAllMiddle() {
    const { detail } = this.data
    if (!detail || !detail.questions || !detail.questions.length) {
      return
    }

    const selectedAnswers = {}
    detail.questions.forEach((q) => {
      const middleIndex = Math.min(2, (q.options ? q.options.length : 1) - 1)
      selectedAnswers[q.id] = Math.max(0, middleIndex)
    })

    this.setData({
      selectedAnswers,
      answeredCount: detail.questions.length,
      progressPercent: 100,
      allAnswered: true,
    })

    this.updateCurrentQuestion()

    wx.showToast({ title: '已填充为中等（测试）', icon: 'none' })
  },

  submitQuiz() {
    const { detail, questionnaireId, selectedAnswers } = this.data

    if (!detail || !detail.questions || !detail.questions.length) {
      this.setData({ errorMessage: '当前问卷暂无题目' })
      return
    }

    const unansweredQuestions = detail.questions
      .map((q, index) => ({ id: q.id, index, answered: selectedAnswers[q.id] !== undefined }))
      .filter((item) => !item.answered)

    if (unansweredQuestions.length > 0) {
      const firstUnanswered = unansweredQuestions[0]
      wx.showModal({
        title: '温馨提示',
        content: `还有 ${unansweredQuestions.length} 道题目未完成，先完成第 ${firstUnanswered.index + 1} 题吧～`,
        confirmText: '去完成',
        cancelText: '再想想',
        confirmColor: '#4f46e5',
        cancelColor: '#6b7280',
        success: (res) => {
          if (res.confirm) {
            this.goToUnansweredQuestion(firstUnanswered.index)
          }
        },
      })
      return
    }

    this.setData({ submitting: true, errorMessage: '' })

    const answers = detail.questions.map((q) => ({
      questionId: q.id,
      selectedOptionIndex: selectedAnswers[q.id],
    }))

    const submitStartTs = Date.now()
    const waitMinOverlay = () =>
      new Promise((resolve) => {
        const remain = MIN_SUBMIT_OVERLAY_MS - (Date.now() - submitStartTs)
        setTimeout(resolve, remain > 0 ? remain : 0)
      })

    request({
      url: '/api/consult/quiz/submit',
      method: 'POST',
      timeout: 95000,
      data: { questionnaireId, answers },
    })
      .then(async (data) => {
        await waitMinOverlay()
        const normalized = normalizeQuizResult(data)
        getApp().globalData.latestQuizResult = normalized
        wx.redirectTo({
          url: `/pages/quiz-result/index?resultId=${(normalized && normalized.id) || ''}`,
        })
      })
      .catch(async (error) => {
        await waitMinOverlay()
        this.setData({ errorMessage: formatRequestError(error, '提交测评失败') })
      })
      .finally(() => {
        this.setData({ submitting: false })
      })
  },
})
