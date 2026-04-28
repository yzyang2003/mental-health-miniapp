const { request, formatRequestError } = require('../../utils/request')
const { normalizeQuestionnaireList, normalizeQuizResult } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')
const { sortQuestionnaireList } = require('../../utils/quizCatalog')
const config = require('../../utils/config')

Page({
  data: {
    loading: false,
    questionnaireList: [],
    historyList: [],
    errorMessage: '',
    apiBaseUrl: '',
  },

  refreshApiHint() {
    const url = typeof config.getBaseUrl === 'function' ? config.getBaseUrl() : ''
    const app = getApp()
    if (app && app.globalData) {
      app.globalData.baseUrl = url
    }
    this.setData({ apiBaseUrl: url })
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.refreshApiHint()
    this.loadPageData()
  },

  onPullDownRefresh() {
    this.loadPageData()
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
    this.refreshApiHint()
    this.fetchHistory()
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  loadPageData() {
    this.setData({
      loading: true,
      errorMessage: '',
    })

    return Promise.all([
      this.fetchQuestionnaires(),
      this.fetchHistory(),
    ])
      .catch(() => {})
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  fetchQuestionnaires() {
    return request({
      url: '/api/consult/quiz/list',
      method: 'GET',
    })
      .then((data) => {
        const questionnaireList = sortQuestionnaireList(normalizeQuestionnaireList(data || []))
        this.setData({
          questionnaireList,
        })
      })
      .catch((error) => {
        const base = typeof config.getBaseUrl === 'function' ? config.getBaseUrl() : ''
        this.setData({
          errorMessage:
            formatRequestError(error, '获取问卷列表失败', { appendLanSetupHint: true }) +
            (base ? `\n当前请求根地址：${base}` : ''),
        })
        this.refreshApiHint()
      })
  },

  fetchHistory() {
    return request({
      url: '/api/consult/quiz/history',
      method: 'GET',
    })
      .then((data) => {
        const raw = data || []
        this.setData({
          historyList: raw.map((item) => normalizeQuizResult(item)).filter(Boolean),
        })
      })
      .catch(() => {
        this.setData({
          historyList: [],
        })
      })
  },

  openQuestionnaire(e) {
    const id = e.currentTarget.dataset.id
    if (!id) {
      return
    }

    wx.navigateTo({
      url: `/pages/quiz-detail/index?id=${id}`,
    })
  },

  openLatestResult(e) {
    const resultId = Number(e.currentTarget.dataset.id || 0)
    if (!resultId) {
      return
    }

    const target = this.data.historyList.find((item) => Number(item.id || 0) === resultId)
    if (!target) {
      return
    }

    getApp().globalData.latestQuizResult = normalizeQuizResult(target)
    wx.navigateTo({
      url: `/pages/quiz-result/index?resultId=${resultId}`,
    })
  },

})
