/**
 * 状态管理模块 —— 从 ai-chat/index.js 提取
 *
 * 职责：页面状态快照保存/恢复、全局状态管理、历史列表合并、历史拉取与服务端对齐。
 * 纯函数 + 工厂函数，不依赖 Page 注册。
 */

const { request, formatRequestError } = require('./request')

const HISTORY_ENDPOINT = '/api/consult/chat/history'
const SNAPSHOT_MAX_AGE_MS = 5 * 60 * 1000

// ── 纯函数 ────────────────────────────────────────────────────────────

function snapshotHasInflightAssistant(snap) {
  const list = snap.historyList || []
  return list.some(
    (m) => m && m.role === 'assistant' && (m.status === 'thinking' || m.status === 'typing')
  )
}

function mergeServerHistoryWithLocalInflight(serverList, localList) {
  const result = serverList.slice()
  const tail = []
  for (let i = localList.length - 1; i >= 0; i -= 1) {
    const item = localList[i]
    if (!item || !item.isLocal) break
    if (item.role === 'user' || (item.role === 'assistant' && (item.status === 'thinking' || item.status === 'typing'))) {
      tail.unshift(item)
      continue
    }
    break
  }
  tail.forEach((item) => {
    const last = result[result.length - 1]
    if (item.role === 'user') {
      const sameUserAtTail = last && last.role === 'user' && String(last.content || '').trim() === String(item.content || '').trim()
      if (!sameUserAtTail) result.push(item)
      return
    }
    if (!(last && last.role === 'assistant')) result.push(item)
  })
  return result
}

// ── 快照保存/恢复 ─────────────────────────────────────────────────────

function savePageState(ctx, extra) {
  const list = ctx.data.historyList || []
  getApp().globalData.aiChatPageState = {
    savedAt: Date.now(),
    historyList: list.map((row) => ({ ...row })),
    sending: ctx.data.sending,
    isTyping: ctx.data.isTyping,
    message: ctx.data.message,
    errorMessage: ctx.data.errorMessage,
    lastFailedMessage: ctx.data.lastFailedMessage,
    guideClosing: ctx.data.guideClosing,
    activeAssistantId: ctx.activeAssistantId || '',
    stoppedAssistantId: ctx.stoppedAssistantId || '',
    ...extra,
  }
}

function restorePageState(ctx) {
  const snap = getApp().globalData.aiChatPageState
  if (!snap || typeof snap.savedAt !== 'number') return false
  if (Date.now() - snap.savedAt > SNAPSHOT_MAX_AGE_MS) {
    getApp().globalData.aiChatPageState = null
    return false
  }
  if (!snap.sending && !snap.isTyping && !snapshotHasInflightAssistant(snap)) return false
  getApp().globalData.aiChatPageState = null
  const restoredList = snap.historyList || []
  ctx.setData({
    historyList: restoredList,
    historyLoadFailed: false,
    sending: !!snap.sending,
    isTyping: !!snap.isTyping,
    message: snap.message || '',
    errorMessage: snap.errorMessage || '',
    lastFailedMessage: snap.lastFailedMessage || '',
    guideClosing: !!snap.guideClosing,
    ...ctx.staticBottomScrollForList(restoredList),
  })
  ctx.activeAssistantId = snap.activeAssistantId || ''
  ctx.stoppedAssistantId = snap.stoppedAssistantId || ''
  ctx._shouldAlignAfterRestore = true
  return true
}

function restoreThinkingBubbleAfterReturn(ctx) {
  const snap = getApp().globalData.aiChatPageState
  if (!snap) return
  const inFlight = snap.sending || snap.isTyping || snapshotHasInflightAssistant(snap)
  if (!inFlight) return
  if (ctx.data.historyList.some((m) => m.role === 'assistant' && (m.status === 'thinking' || m.status === 'typing'))) {
    getApp().globalData.aiChatPageState = null
    return
  }
  const restoredList = snap.historyList || []
  ctx.setData({
    historyList: restoredList,
    historyLoadFailed: false,
    sending: !!snap.sending,
    isTyping: !!snap.isTyping,
    message: snap.message || '',
    errorMessage: snap.errorMessage || '',
    lastFailedMessage: snap.lastFailedMessage || '',
    guideClosing: !!snap.guideClosing,
    ...ctx.staticBottomScrollForList(restoredList),
  })
  ctx.activeAssistantId = snap.activeAssistantId || ''
  ctx.stoppedAssistantId = snap.stoppedAssistantId || ''
  getApp().globalData.aiChatPageState = null
  ctx.scheduleComposerMeasure(40)
}

// ── 工厂：历史拉取与服务端对齐 ────────────────────────────────────────

function createHistoryManager(ctx) {
  return {
    hasInflightAssistantBubble() {
      return ctx.data.historyList.some(
        (m) => m.role === 'assistant' && (m.status === 'thinking' || m.status === 'typing')
      )
    },

    scheduleServerAlignment() {
      if (ctx.serverAlignTimer) {
        clearTimeout(ctx.serverAlignTimer)
        ctx.serverAlignTimer = null
      }
      ctx.serverAlignTimer = setTimeout(() => {
        ctx.serverAlignTimer = null
        if (!ctx.data.historyList.length && !ctx.data.sending && !ctx.data.isTyping) return
        ctx.syncHistoryWithServer()
      }, 320)
    },

    syncHistoryWithServer() {
      return ctx.fetchChatHistory({ force: true, silent: true, alignWithServer: true })
    },

    tryApplyServerAuthoritativeHistory(nextList) {
      if (!nextList || nextList.length < 2) return false
      const last = nextList[nextList.length - 1]
      const prev = nextList[nextList.length - 2]
      if (last.role !== 'assistant' || !String(last.content || '').trim()) return false
      if (prev.role !== 'user') return false
      const waiting = ctx.data.sending || ctx.data.isTyping || ctx.hasInflightAssistantBubble()
      if (!waiting) return false
      if (ctx.standardRequestTask && ctx.standardRequestTask.abort) {
        ctx.standardRequestTask.abort()
        ctx.standardRequestTask = null
      }
      ctx.clearOutputTasks({ includeNetwork: false })
      if (ctx.alignPollTimer) {
        clearTimeout(ctx.alignPollTimer)
        ctx.alignPollTimer = null
      }
      ctx.alignPollCount = 0
      ctx.setData({
        historyList: nextList,
        historyLoadFailed: false,
        sending: false,
        isTyping: false,
        errorMessage: '',
        lastFailedMessage: '',
        guideClosing: false,
        ...ctx.staticBottomScrollForList(nextList),
      })
      ctx.activeAssistantId = ''
      ctx.stoppedAssistantId = ''
      ctx.scheduleComposerMeasure(40)
      return true
    },

    maybeScheduleAlignPoll() {
      if (ctx.alignPollTimer) {
        clearTimeout(ctx.alignPollTimer)
        ctx.alignPollTimer = null
      }
      const stillWaiting =
        (ctx.data.sending || ctx.data.isTyping || ctx.hasInflightAssistantBubble()) &&
        ctx.data.historyList.some((m) => m.role === 'assistant' && (m.status === 'thinking' || m.status === 'typing'))
      if (!stillWaiting) {
        ctx.alignPollCount = 0
        return
      }
      if ((ctx.alignPollCount || 0) >= 12) {
        ctx.alignPollCount = 0
        return
      }
      ctx.alignPollCount = (ctx.alignPollCount || 0) + 1
      ctx.alignPollTimer = setTimeout(() => {
        ctx.alignPollTimer = null
        ctx.syncHistoryWithServer()
      }, 1800)
    },

    fetchChatHistory(options = {}) {
      const { force = false, silent = false, mergeInFlightAssistant = false, alignWithServer = false } = options
      if ((ctx.data.sending || ctx.data.isTyping) && !force) {
        ctx.pendingHistoryRefresh = true
        return Promise.resolve(null)
      }

      const requestId = (ctx.historyRequestSeq || 0) + 1
      ctx.historyRequestSeq = requestId
      ctx.pendingHistoryRefresh = false
      ctx.setData({
        loading: silent ? ctx.data.loading : true,
        historyLoadFailed: false,
        errorMessage: '',
      })

      return request({
        url: `${HISTORY_ENDPOINT}?page=${ctx.data.page}&size=${ctx.data.size}`,
        method: 'GET',
      })
        .then((data) => {
          if (requestId !== ctx.historyRequestSeq) return
          const records = (data && data.records) || []
          let nextList = records.slice().reverse().map((item) => ({
            ...item,
            anchorId: `message-${item.id}`,
            status: 'done',
            createTime: ctx.formatChatTime(item.createTime),
          }))

          if (alignWithServer) {
            if (ctx.tryApplyServerAuthoritativeHistory(nextList)) return
            if (ctx.data.sending || ctx.data.isTyping || ctx.hasInflightAssistantBubble()) {
              nextList = mergeServerHistoryWithLocalInflight(nextList, ctx.data.historyList)
              ctx.setData({
                historyList: nextList, historyLoadFailed: false, guideClosing: false,
                ...ctx.staticBottomScrollForList(nextList),
              })
              ctx.scheduleComposerMeasure(40)
              ctx.maybeScheduleAlignPoll()
              return
            }
            ctx.setData({
              historyList: nextList, historyLoadFailed: false, guideClosing: false,
              ...ctx.staticBottomScrollForList(nextList),
            })
            ctx.scheduleComposerMeasure(40)
            return
          }

          if (ctx.data.sending || ctx.data.isTyping) {
            ctx.pendingHistoryRefresh = true
            return
          }
          if (mergeInFlightAssistant) {
            nextList = mergeServerHistoryWithLocalInflight(nextList, ctx.data.historyList)
          }
          ctx.setData({
            historyList: nextList, historyLoadFailed: false, guideClosing: false,
            ...ctx.staticBottomScrollForList(nextList),
          })
          ctx.scheduleComposerMeasure(40)
        })
        .catch((error) => {
          if (requestId !== ctx.historyRequestSeq) return
          const unauthorized = ctx.isUnauthorizedError(error)
          ctx.setData({
            errorMessage: unauthorized ? '' : formatRequestError(error, '获取聊天记录失败'),
            historyLoadFailed: unauthorized ? false : !ctx.data.historyList.length && !ctx.data.sending && !ctx.data.isTyping,
          })
        })
        .finally(() => {
          if (requestId !== ctx.historyRequestSeq) return
          ctx.setData({ loading: false })
          ctx.scheduleComposerMeasure(40)
        })
    },

    flushDeferredHistoryRefresh() {
      if (!ctx.pendingHistoryRefresh || ctx.data.sending || ctx.data.isTyping) return
      ctx.fetchChatHistory({ silent: !!ctx.data.historyList.length })
    },
  }
}

module.exports = {
  snapshotHasInflightAssistant,
  mergeServerHistoryWithLocalInflight,
  savePageState,
  restorePageState,
  restoreThinkingBubbleAfterReturn,
  createHistoryManager,
}
