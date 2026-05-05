const { request, formatRequestError } = require('../../utils/request')
const { normalizeQuizResult } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')

// 得分等级配置
const LEVEL_CONFIG = {
  PHQ9_DEMO: [
    { max: 4, level: 'normal', label: '正常', icon: '🟢', color: '#10b981', bg: '#ecfdf5' },
    { max: 9, level: 'mild', label: '轻度', icon: '🟡', color: '#eab308', bg: '#fefce8' },
    { max: 14, level: 'moderate-mild', label: '中轻度', icon: '🟠', color: '#f59e0b', bg: '#fffbeb' },
    { max: 19, level: 'moderate', label: '中度', icon: '🟠', color: '#ea580c', bg: '#fff7ed' },
    { max: 24, level: 'moderate-severe', label: '中重度', icon: '🔴', color: '#dc2626', bg: '#fef2f2' },
    { max: Infinity, level: 'severe', label: '重度', icon: '🔴', color: '#b91c1c', bg: '#fef2f2' },
  ],
  GAD7_DEMO: [
    { max: 4, level: 'normal', label: '正常', icon: '🟢', color: '#10b981', bg: '#ecfdf5' },
    { max: 7, level: 'mild', label: '轻度', icon: '🟡', color: '#eab308', bg: '#fefce8' },
    { max: 11, level: 'moderate-mild', label: '中轻度', icon: '🟠', color: '#f59e0b', bg: '#fffbeb' },
    { max: 14, level: 'moderate', label: '中度', icon: '🟠', color: '#ea580c', bg: '#fff7ed' },
    { max: 17, level: 'moderate-severe', label: '中重度', icon: '🔴', color: '#dc2626', bg: '#fef2f2' },
    { max: Infinity, level: 'severe', label: '重度', icon: '🔴', color: '#b91c1c', bg: '#fef2f2' },
  ],
  SCL90_DEMO: [
    { max: 160, level: 'normal', label: '正常', icon: '🟢', color: '#10b981', bg: '#ecfdf5' },
    { max: 200, level: 'mild', label: '轻度', icon: '🟡', color: '#eab308', bg: '#fefce8' },
    { max: 240, level: 'moderate-mild', label: '中轻度', icon: '🟠', color: '#f59e0b', bg: '#fffbeb' },
    { max: 270, level: 'moderate', label: '中度', icon: '🟠', color: '#ea580c', bg: '#fff7ed' },
    { max: 300, level: 'moderate-severe', label: '中重度', icon: '🔴', color: '#dc2626', bg: '#fef2f2' },
    { max: Infinity, level: 'severe', label: '重度', icon: '🔴', color: '#b91c1c', bg: '#fef2f2' },
  ],
  SDS_DEMO: [
    { max: 33, level: 'normal', label: '正常', icon: '🟢', color: '#10b981', bg: '#ecfdf5' },
    { max: 45, level: 'mild', label: '轻度', icon: '🟡', color: '#eab308', bg: '#fefce8' },
    { max: 55, level: 'moderate-mild', label: '中轻度', icon: '🟠', color: '#f59e0b', bg: '#fffbeb' },
    { max: 66, level: 'moderate', label: '中度', icon: '🟠', color: '#ea580c', bg: '#fff7ed' },
    { max: 75, level: 'moderate-severe', label: '中重度', icon: '🔴', color: '#dc2626', bg: '#fef2f2' },
    { max: Infinity, level: 'severe', label: '重度', icon: '🔴', color: '#b91c1c', bg: '#fef2f2' },
  ],
  SAS_DEMO: [
    { max: 33, level: 'normal', label: '正常', icon: '🟢', color: '#10b981', bg: '#ecfdf5' },
    { max: 45, level: 'mild', label: '轻度', icon: '🟡', color: '#eab308', bg: '#fefce8' },
    { max: 55, level: 'moderate-mild', label: '中轻度', icon: '🟠', color: '#f59e0b', bg: '#fffbeb' },
    { max: 66, level: 'moderate', label: '中度', icon: '🟠', color: '#ea580c', bg: '#fff7ed' },
    { max: 75, level: 'moderate-severe', label: '中重度', icon: '🔴', color: '#dc2626', bg: '#fef2f2' },
    { max: Infinity, level: 'severe', label: '重度', icon: '🔴', color: '#b91c1c', bg: '#fef2f2' },
  ],
}

function getScoreLevel(type, score) {
  const config = LEVEL_CONFIG[type]
  if (!config) {
    return { level: 'normal', label: '', icon: '', color: '#6b7280', bg: '#f9fafb' }
  }
  for (const item of config) {
    if (score <= item.max) {
      return item
    }
  }
  return config[config.length - 1]
}

Page({
  data: {
    resultId: 0,
    result: null,
    loading: false,
    errorMessage: '',
    retryingAiGuidance: false,
    retryCooldownSec: 0,
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    // 得分等级
    scoreLevel: null,
  },
  retryCooldownTimer: null,
  aiPollingTimer: null,
  aiPollingAttempts: 0,
  maxAiPollingAttempts: 40,
  _resultDetailPromise: null,
  _resultDetailId: 0,
  _capsuleCached: false,
  _wasPollingActive: false,

  refreshResultFromGlobal(expectedResultId = 0, options = {}) {
    const { showMissingToast = true } = options
    const raw = getApp().globalData.latestQuizResult
    const result = normalizeQuizResult(raw)
    if (!result || (expectedResultId && Number(result.id || 0) !== Number(expectedResultId))) {
      if (!showMissingToast) {
        this.setData({ result: null, scoreLevel: null })
        return null
      }
      wx.showToast({
        title: '暂无测评结果',
        icon: 'none',
      })
      this.setData({ result: null, scoreLevel: null })
      return null
    }
    const scoreLevel = getScoreLevel(result.questionnaireType, result.score)
    this.setData({
      resultId: Number(result.id || expectedResultId || 0),
      result,
      scoreLevel,
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
    if (!silent) {
      this.setData({ loading: true, errorMessage: '' })
    }
    const requestPromise = request({
      url: `/api/consult/quiz/result/${resultId}`,
      method: 'GET',
      timeout: 15000,
    })
      .then((data) => {
        const normalized = normalizeQuizResult(data)
        if (!normalized) {
          if (!silent) {
            this.setData({ errorMessage: '暂无测评结果' })
          }
          return null
        }
        getApp().globalData.latestQuizResult = normalized
        const scoreLevel = getScoreLevel(normalized.questionnaireType, normalized.score)
        this.setData({
          resultId: Number(normalized.id || resultId || 0),
          result: normalized,
          scoreLevel,
          loading: false,
        })
        return normalized
      })
      .catch((error) => {
        if (!silent) {
          this.setData({
            errorMessage: formatRequestError(error, '获取测评结果失败'),
            loading: false,
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
    // 胶囊位置缓存：只计算一次
    if (!this._capsuleCached) {
      this.calcCapsule()
      this._capsuleCached = true
    }
    this.ensureResultLoaded({
      showMissingToast: !this.data.result,
      silentFetch: !!this.data.result,
    }).finally(() => {
      // AI 轮询恢复：如果之前在轮询且有结果，恢复轮询
      if (this._wasPollingActive && this.data.result) {
        this.startAiGuidancePollingIfNeeded()
      }
      this._wasPollingActive = false
    })
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

  onUnload() {
    this.clearRetryCooldownTimer()
    this.clearAiGuidancePolling()
  },

  onHide() {
    this.clearRetryCooldownTimer()
    // AI 轮询暂停：记录状态并暂停
    this._wasPollingActive = !!this.aiPollingTimer
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
        const scoreLevel = getScoreLevel(normalized.questionnaireType, normalized.score)
        this.setData({
          resultId: Number(normalized.id || id || 0),
          result: normalized,
          scoreLevel,
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
