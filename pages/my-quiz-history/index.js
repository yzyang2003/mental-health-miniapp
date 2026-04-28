const { request, formatRequestError } = require('../../utils/request')
const { normalizeQuizResult } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    loading: false,
    historyList: [],
    errorMessage: '',
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.fetchHistory()
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
    this.fetchHistory()
  },

  onPullDownRefresh() {
    this.fetchHistory()
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchHistory() {
    this.setData({
      loading: true,
      errorMessage: '',
    })

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
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取测评记录失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  openResult(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) {
      return
    }

    const target = this.data.historyList.find((item) => Number(item.id || 0) === id)
    if (!target) {
      return
    }

    getApp().globalData.latestQuizResult = normalizeQuizResult(target)
    wx.navigateTo({
      url: `/pages/quiz-result/index?resultId=${id}`,
    })
  },

})
