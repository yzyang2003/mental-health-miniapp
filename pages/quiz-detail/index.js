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
  },

  onLoad(options) {
    if (!this.ensureLogin()) {
      return
    }

    const questionnaireId = Number(options.id || 0)
    if (!questionnaireId) {
      this.setData({
        errorMessage: '无效的问卷 ID',
      })
      return
    }

    this.setData({
      questionnaireId,
    })
    this.fetchDetail(questionnaireId)
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchDetail(id) {
    this.setData({
      loading: true,
      errorMessage: '',
    })

    request({
      url: `/api/consult/quiz/${id}`,
      method: 'GET',
    })
      .then((data) => {
        this.setData({
          detail: normalizeQuizDetail(data),
        })
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取问卷详情失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  preventTouchMove() {
    /* 遮罩层阻止穿透滚动 */
  },

  selectOption(e) {
    const questionId = e.currentTarget.dataset.questionId
    const optionIndex = Number(e.currentTarget.dataset.optionIndex)
    if (!questionId && questionId !== 0) {
      return
    }

    this.setData({
      [`selectedAnswers.${questionId}`]: optionIndex,
    })
  },

  fillAllMiddle() {
    const detail = this.data.detail
    if (!detail || !detail.questions || !detail.questions.length) {
      return
    }
    const selectedAnswers = {}
    detail.questions.forEach((q) => {
      // 选项第三项为“中等”（索引 2）
      selectedAnswers[q.id] = 2
    })
    this.setData({ selectedAnswers })
    wx.showToast({
      title: '已填充为中等（测试）',
      icon: 'none',
    })
  },

  submitQuiz() {
    const detail = this.data.detail
    if (!detail || !detail.questions || !detail.questions.length) {
      this.setData({
        errorMessage: '当前问卷暂无题目',
      })
      return
    }

    const answers = detail.questions.map((question) => ({
      questionId: question.id,
      selectedOptionIndex: this.data.selectedAnswers[question.id],
    }))

    const hasUnanswered = answers.some((item) => item.selectedOptionIndex === undefined)
    if (hasUnanswered) {
      this.setData({
        errorMessage: '请完成全部题目后再提交',
      })
      return
    }

    this.setData({
      submitting: true,
      errorMessage: '',
    })
    const submitStartTs = Date.now()
    const waitMinOverlay = () =>
      new Promise((resolve) => {
        const remain = MIN_SUBMIT_OVERLAY_MS - (Date.now() - submitStartTs)
        setTimeout(resolve, remain > 0 ? remain : 0)
      })

    request({
      url: '/api/consult/quiz/submit',
      method: 'POST',
      // 后端 AI 生成含重试（最长可接近 80s），前端超时需留足缓冲避免提前失败
      timeout: 95000,
      data: {
        questionnaireId: this.data.questionnaireId,
        answers,
      },
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
        this.setData({
          errorMessage: formatRequestError(error, '提交测评失败'),
        })
      })
      .finally(() => {
        this.setData({
          submitting: false,
        })
      })
  },

})
