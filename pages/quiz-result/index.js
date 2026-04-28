const { request, formatRequestError } = require('../../utils/request')
const { normalizeQuizResult } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    resultId: 0,
    result: null,
    retryingAiGuidance: false,
    retryCooldownSec: 0,
  },
  retryCooldownTimer: null,
  aiPollingTimer: null,
  aiPollingAttempts: 0,
  maxAiPollingAttempts: 40,
  _resultDetailPromise: null,
  _resultDetailId: 0,

  refreshResultFromGlobal(expectedResultId = 0, options = {}) {
    const { showMissingToast = true } = options
    const raw = getApp().globalData.latestQuizResult
    const result = normalizeQuizResult(raw)
    if (!result || (expectedResultId && Number(result.id || 0) !== Number(expectedResultId))) {
      if (!showMissingToast) {
        this.setData({ result: null })
        return null
      }
      wx.showToast({
        title: '暂无测评结果',
        icon: 'none',
      })
      this.setData({ result: null })
      return null
    }
    this.setData({
      resultId: Number(result.id || expectedResultId || 0),
      result,
    })
    return result
  },

  fetchResultDetail(resultId, options = {}) {
    const { silent = false } = options
    if (!resultId) {
      return Promise.resolve(null)
    }
    if (this._resultDetailPromise && this._resultDetailId === resultId) {
      return this._resultDetailPromise
    }
    const requestPromise = request({
      url: `/api/consult/quiz/result/${resultId}`,
      method: 'GET',
      timeout: 15000,
    })
      .then((data) => {
        const normalized = normalizeQuizResult(data)
        if (!normalized) {
          return null
        }
        getApp().globalData.latestQuizResult = normalized
        this.setData({
          resultId: Number(normalized.id || resultId || 0),
          result: normalized,
        })
        return normalized
      })
      .catch((error) => {
        if (!silent) {
          wx.showToast({
            title: formatRequestError(error, '获取测评结果失败'),
            icon: 'none',
            duration: 2500,
          })
        }
        return null
      })
      .finally(() => {
        if (this._resultDetailPromise === requestPromise) {
          this._resultDetailPromise = null
          this._resultDetailId = 0
        }
      })
    this._resultDetailPromise = requestPromise
    this._resultDetailId = resultId
    return requestPromise
  },

  ensureResultLoaded(options = {}) {
    const { showMissingToast = true, silentFetch = false } = options
    const resultId = Number(this.data.resultId || 0)
    const result = this.refreshResultFromGlobal(resultId, { showMissingToast: false })
    if (result) {
      return Promise.resolve(result)
    }
    if (!resultId) {
      if (showMissingToast) {
        this.refreshResultFromGlobal(0, { showMissingToast: true })
      }
      return Promise.resolve(null)
    }
    return this.fetchResultDetail(resultId, { silent: silentFetch })
  },

  onLoad(options) {
    if (!ensurePageLogin()) {
      return
    }
    const resultId = Number((options && options.resultId) || 0)
    if (resultId) {
      this.setData({ resultId })
    }
    this.ensureResultLoaded({
      showMissingToast: true,
      silentFetch: false,
    }).finally(() => {
      this.startAiGuidancePollingIfNeeded()
    })
  },

  onShow() {
    if (!ensurePageLogin()) {
      return
    }
    this.ensureResultLoaded({
      showMissingToast: !this.data.result,
      silentFetch: !!this.data.result,
    }).finally(() => {
      this.startAiGuidancePollingIfNeeded()
    })
  },

  onUnload() {
    this.clearRetryCooldownTimer()
    this.clearAiGuidancePolling()
  },

  onHide() {
    this.clearRetryCooldownTimer()
    this.clearAiGuidancePolling()
  },

  goToStation() {
    wx.navigateTo({
      url: '/pages/station/index',
      fail: () => {
        wx.reLaunch({
          url: '/pages/station/index',
        })
      },
    })
  },

  goToTreeHole() {
    wx.navigateTo({
      url: '/pages/topic-list/index',
      fail: () => {
        wx.reLaunch({
          url: '/pages/topic-list/index',
        })
      },
    })
  },

  goToAi() {
    const result = this.data.result
    if (result && result.aiChatHint) {
      getApp().globalData.aiChatPrefill = result.aiChatHint
    }
    getApp().globalData.aiChatNeedHistoryRefresh = true
    wx.navigateTo({
      url: '/pages/ai-chat/index',
    })
  },

  retryAiGuidance() {
    const result = this.data.result
    if (!result || !result.id || this.data.retryingAiGuidance || this.data.retryCooldownSec > 0) {
      return
    }
    this.setData({ retryingAiGuidance: true })
    request({
      url: `/api/consult/quiz/result/${result.id}/retry-ai-guidance`,
      method: 'POST',
      timeout: 60000,
    })
      .then((data) => {
        const normalized = normalizeQuizResult(data)
        getApp().globalData.latestQuizResult = normalized
        this.setData({
          resultId: Number((normalized && normalized.id) || (this.data.result && this.data.result.id) || this.data.resultId || 0),
          result: normalized || this.data.result,
        })
        wx.showToast({
          title: normalized && normalized.aiGuidance ? 'AI 指导已更新' : '已完成重试',
          icon: 'none',
        })
      })
      .catch((error) => {
        wx.showToast({
          title: formatRequestError(error, '重试生成失败'),
          icon: 'none',
          duration: 2500,
        })
      })
      .finally(() => {
        this.setData({ retryingAiGuidance: false })
        this.startRetryCooldown()
        this.startAiGuidancePollingIfNeeded(true)
      })
  },

  startAiGuidancePollingIfNeeded(forceRestart = false) {
    const result = this.data.result
    if (!result || !result.id || result.aiGuidance) {
      this.clearAiGuidancePolling()
      return
    }
    if (forceRestart) {
      this.clearAiGuidancePolling()
    }
    if (this.aiPollingTimer) {
      return
    }
    this.aiPollingAttempts = 0
    this.aiPollingTimer = setInterval(() => {
      if (this.aiPollingAttempts >= this.maxAiPollingAttempts) {
        this.clearAiGuidancePolling()
        return
      }
      this.aiPollingAttempts += 1
      this.fetchLatestResultSilently(result.id)
    }, 3000)
  },

  clearAiGuidancePolling() {
    if (this.aiPollingTimer) {
      clearInterval(this.aiPollingTimer)
      this.aiPollingTimer = null
    }
    this.aiPollingAttempts = 0
  },

  fetchLatestResultSilently(id) {
    request({
      url: `/api/consult/quiz/result/${id}`,
      method: 'GET',
      timeout: 15000,
    })
      .then((data) => {
        const normalized = normalizeQuizResult(data)
        if (!normalized) {
          return
        }
        getApp().globalData.latestQuizResult = normalized
        this.setData({
          resultId: Number(normalized.id || id || 0),
          result: normalized,
        })
        if (normalized.aiGuidance) {
          this.clearAiGuidancePolling()
          wx.showToast({
            title: 'AI 指导已生成',
            icon: 'none',
          })
        }
      })
      .catch(() => {})
  },

  startRetryCooldown(seconds = 10) {
    this.clearRetryCooldownTimer()
    this.setData({ retryCooldownSec: seconds })
    this.retryCooldownTimer = setInterval(() => {
      const next = this.data.retryCooldownSec - 1
      if (next <= 0) {
        this.clearRetryCooldownTimer()
        this.setData({ retryCooldownSec: 0 })
        return
      }
      this.setData({ retryCooldownSec: next })
    }, 1000)
  },

  clearRetryCooldownTimer() {
    if (this.retryCooldownTimer) {
      clearInterval(this.retryCooldownTimer)
      this.retryCooldownTimer = null
    }
  },
})
