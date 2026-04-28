const { request, requestRaw, formatRequestError } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

const STREAM_MODE_TYPING = 'typing'
const STREAM_MODE_CHUNK = 'chunk'
const STREAM_MODE_SOCKET = 'socket'

const REQUEST_ENDPOINT = '/api/consult/chat/send'
const RESET_ENDPOINT = '/api/consult/chat/reset'
const HISTORY_ENDPOINT = '/api/consult/chat/history'

// 目前正式启用的是“完整回复 + 前端打字机”模式，其他流式分支仅作为预留方案保留。
const ACTIVE_STREAM_MODE = STREAM_MODE_TYPING
const CHUNK_STREAM_ENDPOINT = '/api/consult/chat/stream'
const SOCKET_STREAM_URL = ''

// 单字步进；间隔约为上一版的 1/1.5，整体约 1.5 倍速
const TYPEWRITER_INTERVAL = 32
const TYPEWRITER_CHUNK_SIZE = 1

// 未按压时：距底过远也停跟底（兜底）
const SCROLL_PAUSE_AWAY_PX = 6
// 距底多近才触发 scrolltolower（越小需越贴底才恢复跟底）
const SCROLL_LOWER_THRESHOLD_PX = 8
// 打字机跟底：限制 scroll-into-view 频率，避免与手滑/惯性抢滚动导致卡顿、尾段抖动
const SCROLL_FOLLOW_TYPING_MIN_MS = 180
// 打字阶段把短时间内多次滚底请求合并（leading 定时，避免连续双次布局）
const SCROLL_FOLLOW_TYPING_DELAY_MS = 100
// 已锚在 chat-bottom 时，限制「清空再绑」全量切换的频率；间隔内改为轻量单次 setData
const SCROLL_TYPING_ANCHOR_TOGGLE_MIN_MS = 220
// 手指离开聊天区后短时间内不程序滚底，避开惯性滚动末段
const SCROLL_AFTER_TOUCH_COOLDOWN_MS = 350
// 打字期间 follow scrollTop 的最小测量间隔
const SCROLL_TYPING_FOLLOW_MEASURE_MIN_MS = 120
const SCROLL_TYPING_FOLLOW_MIN_DELTA_PX = 2

Page({
  data: {
    navTop: 8,
    sideInsetPx: 12,
    contentInsetPx: 12,
    scrollGutterPx: 2,
    historyTopSafePx: 2,
    shellRadiusPx: 16,
    scrollLowerThreshold: SCROLL_LOWER_THRESHOLD_PX,
    loading: false,
    historyLoadFailed: false,
    sending: false,
    isTyping: false,
    guideClosing: false,
    topInset: 8,
    bottomSafeInset: 0,
    keyboardHeight: 0,
    composerBottom: 8,
    composerHeight: 132,
    contentGap: 8,
    bottomSpacerPx: 0,
    page: 1,
    size: 20,
    historyList: [],
    message: '',
    errorMessage: '',
    lastFailedMessage: '',
    historyScrollTop: 0,
    scrollIntoView: '',
    scrollWithAnimation: false,
    quickPrompts: [
      '我最近压力很大，不知道怎么调整',
      '这几天总失眠，可以怎么缓解',
      '我总是焦虑，会反复胡思乱想',
      '我最近情绪有点低落，想聊聊',
    ],
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.historyRequestSeq = 0
    this.pendingHistoryRefresh = false
    this.socketFailed = false
    this.alignPollCount = 0
    this.alignPollTimer = null
    this.serverAlignTimer = null
    this.scrollBottomTimer = null
    this.historyViewportMeasureTimer = null
    this.historyViewportHeight = 0
    this.composerOverlayHeight = 0
    this.lastDistanceToBottom = Number.POSITIVE_INFINITY
    this.autoStickToBottom = true
    this.lastAutoScrollAt = 0
    this._shouldAlignAfterRestore = false
    this._firstShowAfterLoad = true
    /** onLoad 已发起首屏历史拉取时置 true，避免 onShow 同帧再拉一次造成竞态 */
    this._historyFetchScheduledFromOnLoad = false
    this._chatScrollUserGesture = false
    this._chatFingerDown = false
    this._lastHistoryScrollTop = undefined
    this._chatScrollTouchEndTimer = null
    this._chatScrollCooldownUntil = 0
    this._lastTypingAnchorToggleAt = 0
    this.latestHistoryScrollHeight = 0
    this.typingFollowEnabled = false
    this.typingFollowViewportHeight = 0
    this.typingFollowBottomSpacer = 0
    this.typingFollowLastTop = 0
    this.typingFollowMeasurePending = false
    this.typingFollowLastMeasureAt = 0
    this.initLayoutMetrics()
    this.restoreAiChatPageFromSnapshot()
    this.scheduleComposerMeasure(60)
    this.scheduleHistoryViewportMeasure(80)
    this._historyFetchScheduledFromOnLoad = true
    this.fetchChatHistory({ force: true, mergeInFlightAssistant: true })
  },

  /**
   * 从其它页返回或重新打开页面时，恢复离开时保存的列表与「思考中」状态。
   */
  restoreAiChatPageFromSnapshot() {
    const snap = getApp().globalData.aiChatPageState
    if (!snap || typeof snap.savedAt !== 'number') {
      return
    }
    if (Date.now() - snap.savedAt > 5 * 60 * 1000) {
      getApp().globalData.aiChatPageState = null
      return
    }
    if (!snap.sending && !snap.isTyping && !this.snapshotHasInflightAssistant(snap)) {
      return
    }
    getApp().globalData.aiChatPageState = null
    const restoredList = snap.historyList || []
    this.setData({
      historyList: restoredList,
      historyLoadFailed: false,
      sending: !!snap.sending,
      isTyping: !!snap.isTyping,
      message: snap.message || '',
      errorMessage: snap.errorMessage || '',
      lastFailedMessage: snap.lastFailedMessage || '',
      guideClosing: !!snap.guideClosing,
      ...this.staticBottomScrollForList(restoredList),
    })
    this.activeAssistantId = snap.activeAssistantId || ''
    this.stoppedAssistantId = snap.stoppedAssistantId || ''
    this._shouldAlignAfterRestore = true
  },

  snapshotHasInflightAssistant(snap) {
    const list = snap.historyList || []
    return list.some(
      (m) =>
        m &&
        m.role === 'assistant' &&
        (m.status === 'thinking' || m.status === 'typing')
    )
  },

  onHide() {
    if (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) {
      const list = this.data.historyList || []
      getApp().globalData.aiChatPageState = {
        savedAt: Date.now(),
        historyList: list.map((row) => ({ ...row })),
        sending: this.data.sending,
        isTyping: this.data.isTyping,
        message: this.data.message,
        errorMessage: this.data.errorMessage,
        lastFailedMessage: this.data.lastFailedMessage,
        guideClosing: this.data.guideClosing,
        activeAssistantId: this.activeAssistantId || '',
        stoppedAssistantId: this.stoppedAssistantId || '',
      }
    }
    if (this.data.keyboardHeight) {
      this.setData({
        keyboardHeight: 0,
      })
    }
  },

  hasInflightAssistantBubble() {
    return this.data.historyList.some(
      (m) =>
        m.role === 'assistant' &&
        (m.status === 'thinking' || m.status === 'typing')
    )
  },

  /**
   * 页面仅 onHide 未销毁时，从其它页返回后若列表里丢了「思考中/打字中」气泡，用 onHide 快照补回。
   */
  restoreThinkingBubbleAfterReturn() {
    const snap = getApp().globalData.aiChatPageState
    if (!snap) {
      return
    }
    const inFlight =
      snap.sending ||
      snap.isTyping ||
      this.snapshotHasInflightAssistant(snap)
    if (!inFlight) {
      return
    }
    if (this.hasInflightAssistantBubble()) {
      getApp().globalData.aiChatPageState = null
      return
    }
    const restoredList = snap.historyList || []
    this.setData({
      historyList: restoredList,
      historyLoadFailed: false,
      sending: !!snap.sending,
      isTyping: !!snap.isTyping,
      message: snap.message || '',
      errorMessage: snap.errorMessage || '',
      lastFailedMessage: snap.lastFailedMessage || '',
      guideClosing: !!snap.guideClosing,
      ...this.staticBottomScrollForList(restoredList),
    })
    this.activeAssistantId = snap.activeAssistantId || ''
    this.stoppedAssistantId = snap.stoppedAssistantId || ''
    getApp().globalData.aiChatPageState = null
    this.scheduleComposerMeasure(40)
  },

  mergeServerHistoryWithLocalInflight(serverList) {
    const prev = this.data.historyList || []
    const result = serverList.slice()
    const tail = []
    for (let i = prev.length - 1; i >= 0; i -= 1) {
      const item = prev[i]
      if (!item || !item.isLocal) {
        break
      }
      if (
        item.role === 'user' ||
        (item.role === 'assistant' &&
          (item.status === 'thinking' || item.status === 'typing'))
      ) {
        tail.unshift(item)
        continue
      }
      break
    }
    tail.forEach((item) => {
      const last = result[result.length - 1]
      if (item.role === 'user') {
        const sameUserAtTail =
          last &&
          last.role === 'user' &&
          String(last.content || '').trim() === String(item.content || '').trim()
        if (!sameUserAtTail) {
          result.push(item)
        }
        return
      }
      const hasAssistantTail = last && last.role === 'assistant'
      if (!hasAssistantTail) {
        result.push(item)
      }
    })
    return result
  },

  /** 首屏/历史刷新时与列表同一次 setData，无动画直接落在最新消息 */
  staticBottomScrollForList(list) {
    const len = (list && list.length) || 0
    if (!len) {
      return { scrollIntoView: '', scrollWithAnimation: false }
    }
    if (this.data.isTyping) {
      return { scrollIntoView: '', scrollWithAnimation: false }
    }
    return { scrollIntoView: 'chat-bottom', scrollWithAnimation: false }
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
    const isFirstShowAfterLoad = this._firstShowAfterLoad
    this._firstShowAfterLoad = false
    if (this.data.keyboardHeight) {
      this.setData({
        keyboardHeight: 0,
      })
    }
    this.initLayoutMetrics()
    this.scheduleComposerMeasure(60)
    this.scheduleHistoryViewportMeasure(80)
    this.restoreThinkingBubbleAfterReturn()
    const prefill = getApp().globalData.aiChatPrefill
    if (prefill) {
      getApp().globalData.aiChatPrefill = ''
      this.setData({
        message: prefill,
      })
    }
    let historyFetchFromQuiz = false
    const needHistoryFromQuiz = getApp().globalData.aiChatNeedHistoryRefresh
    if (needHistoryFromQuiz) {
      getApp().globalData.aiChatNeedHistoryRefresh = false
      if (this.data.sending || this.data.isTyping) {
        this.pendingHistoryRefresh = true
      } else {
        historyFetchFromQuiz = true
        this.fetchChatHistory({ silent: !!this.data.historyList.length })
      }
    }
    if (this.pendingHistoryRefresh && !this.data.sending && !this.data.isTyping) {
      this.flushDeferredHistoryRefresh()
    }
    if (
      !historyFetchFromQuiz &&
      !this.data.loading &&
      !this.data.historyList.length &&
      !this.data.sending &&
      !this.data.isTyping
    ) {
      // 首帧 onLoad 已拉历史时，onShow 里 loading 可能尚未 true，会误判再拉一次
      if (isFirstShowAfterLoad && this._historyFetchScheduledFromOnLoad) {
        // 跳过
      } else {
        this.fetchChatHistory()
      }
    }
    if (!isFirstShowAfterLoad || this._shouldAlignAfterRestore) {
      this.scheduleServerAlignment()
    }
    this._shouldAlignAfterRestore = false
  },

  /**
   * 返回页面后短延迟拉取服务端历史，与数据库对齐；若仍在「思考中」则轮询直到出现助手回复或超时。
   */
  scheduleServerAlignment() {
    if (this.serverAlignTimer) {
      clearTimeout(this.serverAlignTimer)
      this.serverAlignTimer = null
    }
    this.serverAlignTimer = setTimeout(() => {
      this.serverAlignTimer = null
      if (!this.data.historyList.length && !this.data.sending && !this.data.isTyping) {
        return
      }
      this.syncHistoryWithServer()
    }, 320)
  },

  syncHistoryWithServer() {
    return this.fetchChatHistory({
      force: true,
      silent: true,
      alignWithServer: true,
    })
  },

  /**
   * 服务端已落库「用户 + 助手」完整一轮且本地仍在等待时，以服务端列表为准并结束本地请求/打字状态。
   */
  tryApplyServerAuthoritativeHistory(nextList) {
    if (!nextList || nextList.length < 2) {
      return false
    }
    const last = nextList[nextList.length - 1]
    const prev = nextList[nextList.length - 2]
    if (last.role !== 'assistant' || !String(last.content || '').trim()) {
      return false
    }
    if (prev.role !== 'user') {
      return false
    }
    const waiting =
      this.data.sending ||
      this.data.isTyping ||
      this.hasInflightAssistantBubble()
    if (!waiting) {
      return false
    }
    if (this.standardRequestTask && this.standardRequestTask.abort) {
      this.standardRequestTask.abort()
      this.standardRequestTask = null
    }
    this.clearOutputTasks({ includeNetwork: false })
    if (this.alignPollTimer) {
      clearTimeout(this.alignPollTimer)
      this.alignPollTimer = null
    }
    this.alignPollCount = 0
    this.setData({
      historyList: nextList,
      historyLoadFailed: false,
      sending: false,
      isTyping: false,
      errorMessage: '',
      lastFailedMessage: '',
      guideClosing: false,
      ...this.staticBottomScrollForList(nextList),
    })
    this.activeAssistantId = ''
    this.stoppedAssistantId = ''
    this.scheduleComposerMeasure(40)
    return true
  },

  maybeScheduleAlignPoll() {
    if (this.alignPollTimer) {
      clearTimeout(this.alignPollTimer)
      this.alignPollTimer = null
    }
    const stillWaiting =
      (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) &&
      this.data.historyList.some(
        (m) =>
          m.role === 'assistant' &&
          (m.status === 'thinking' || m.status === 'typing')
      )
    if (!stillWaiting) {
      this.alignPollCount = 0
      return
    }
    if ((this.alignPollCount || 0) >= 12) {
      this.alignPollCount = 0
      return
    }
    this.alignPollCount = (this.alignPollCount || 0) + 1
    this.alignPollTimer = setTimeout(() => {
      this.alignPollTimer = null
      this.syncHistoryWithServer()
    }, 1800)
  },

  onUnload() {
    this._firstShowAfterLoad = true
    if (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) {
      getApp().globalData.aiChatPageState = {
        savedAt: Date.now(),
        historyList: (this.data.historyList || []).map((row) => ({ ...row })),
        sending: this.data.sending,
        isTyping: this.data.isTyping,
        message: this.data.message,
        errorMessage: this.data.errorMessage,
        lastFailedMessage: this.data.lastFailedMessage,
        guideClosing: this.data.guideClosing,
        activeAssistantId: this.activeAssistantId || '',
        stoppedAssistantId: this.stoppedAssistantId || '',
        savedByUnload: true,
      }
    } else {
      getApp().globalData.aiChatPageState = null
    }
    this.clearOutputTasks()
    if (this.scrollBottomTimer) {
      clearTimeout(this.scrollBottomTimer)
      this.scrollBottomTimer = null
    }
    if (this._chatScrollTouchEndTimer) {
      clearTimeout(this._chatScrollTouchEndTimer)
      this._chatScrollTouchEndTimer = null
    }
    if (this.historyViewportMeasureTimer) {
      clearTimeout(this.historyViewportMeasureTimer)
      this.historyViewportMeasureTimer = null
    }
    if (this.measureComposerTimer) {
      clearTimeout(this.measureComposerTimer)
      this.measureComposerTimer = null
    }
    if (this.alignPollTimer) {
      clearTimeout(this.alignPollTimer)
      this.alignPollTimer = null
    }
    if (this.serverAlignTimer) {
      clearTimeout(this.serverAlignTimer)
      this.serverAlignTimer = null
    }
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  initLayoutMetrics() {
    const systemInfo = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync()
    let navTop = 8
    let topInset = 56
    let sideInsetPx = 12
    let shellRadiusPx = 16
    if (wx.getMenuButtonBoundingClientRect) {
      const rect = wx.getMenuButtonBoundingClientRect()
      if (rect && rect.bottom > 0) {
        navTop = Math.max(8, rect.top - 2)
        topInset = Math.max(56, rect.height + 24)
        const windowWidth = systemInfo && systemInfo.windowWidth ? systemInfo.windowWidth : rect.right + 12
        const rightGap = Math.max(8, Math.round(windowWidth - rect.right))
        // 相对胶囊再外延一小档，让容器更贴边但仍保留安全留白
        sideInsetPx = Math.max(4, rightGap - 4)
        shellRadiusPx = Math.max(14, Math.round(rect.height / 2))
      }
    }
    const safeArea = systemInfo.safeArea || null
    const windowHeight = systemInfo.windowHeight || 0
    const bottomSafeInset = safeArea && windowHeight ? Math.max(windowHeight - safeArea.bottom, 0) : 0

    this.setData({
      navTop,
      sideInsetPx,
      contentInsetPx: sideInsetPx,
      scrollGutterPx: Math.max(1, Math.round(sideInsetPx * 0.3)),
      historyTopSafePx: Math.max(
        Math.max(1, Math.round(sideInsetPx * 0.3)),
        Math.max(0, topInset - 14)
      ),
      shellRadiusPx,
      topInset,
      bottomSafeInset,
    })
  },

  scheduleHistoryViewportMeasure(delay = 0) {
    if (this.historyViewportMeasureTimer) {
      clearTimeout(this.historyViewportMeasureTimer)
      this.historyViewportMeasureTimer = null
    }

    this.historyViewportMeasureTimer = setTimeout(() => {
      this.historyViewportMeasureTimer = null
      const query = this.createSelectorQuery()
      let historyRect = null
      let composerRect = null
      query.select('.chat-history').boundingClientRect((rect) => {
        historyRect = rect || null
      })
      query.select('.composer-wrapper').boundingClientRect((rect) => {
        composerRect = rect || null
      })
      query.exec(() => {
        if (!historyRect || !historyRect.height) {
          return
        }
        this.historyViewportHeight = Math.ceil(historyRect.height)
        if (!composerRect) {
          this.composerOverlayHeight = 0
          if ((this.data.bottomSpacerPx || 0) !== 0) {
            this.setData({
              bottomSpacerPx: 0,
            })
          }
          return
        }
        // 以几何重叠高度作为“被输入框遮挡的可见区”，确保判定对齐输入框上沿
        const overlap = Math.max(0, Math.ceil(historyRect.bottom - composerRect.top))
        const prevOverlap = this.composerOverlayHeight || 0
        this.composerOverlayHeight = overlap
        if ((this.data.bottomSpacerPx || 0) !== overlap) {
          this.setData({
            bottomSpacerPx: overlap,
          }, () => {
            // 首次进入或键盘高度变化后，遮挡高度一旦更新，立即把底部锚点对齐到输入框上沿
            if (this.autoStickToBottom && this.data.historyList.length && !this.data.isTyping) {
              this.scrollToBottom(0, false, true)
            }
          })
          return
        }
        if (
          this.autoStickToBottom &&
          this.data.historyList.length &&
          !this.data.isTyping &&
          Math.abs(overlap - prevOverlap) > 0.5
        ) {
          this.scrollToBottom(0, false, true)
        }
      })
    }, delay)
  },

  scheduleComposerMeasure(delay = 0) {
    if (this.measureComposerTimer) {
      clearTimeout(this.measureComposerTimer)
      this.measureComposerTimer = null
    }
    this.scheduleHistoryViewportMeasure(delay)
  },

  fetchChatHistory(options = {}) {
    const { force = false, silent = false, mergeInFlightAssistant = false, alignWithServer = false } = options
    if ((this.data.sending || this.data.isTyping) && !force) {
      this.pendingHistoryRefresh = true
      return Promise.resolve(null)
    }

    const requestId = (this.historyRequestSeq || 0) + 1
    this.historyRequestSeq = requestId
    this.pendingHistoryRefresh = false

    this.setData({
      loading: silent ? this.data.loading : true,
      historyLoadFailed: false,
      errorMessage: '',
    })

    return request({
      url: `${HISTORY_ENDPOINT}?page=${this.data.page}&size=${this.data.size}`,
      method: 'GET',
    })
      .then((data) => {
        if (requestId !== this.historyRequestSeq) {
          return
        }
        const records = (data && data.records) || []
        let nextList = records.slice().reverse().map((item) => ({
          ...item,
          anchorId: `message-${item.id}`,
          status: 'done',
          createTime: this.formatChatTime(item.createTime),
        }))

        if (alignWithServer) {
          if (this.tryApplyServerAuthoritativeHistory(nextList)) {
            return
          }
          if (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) {
            nextList = this.mergeServerHistoryWithLocalInflight(nextList)
            this.setData({
              historyList: nextList,
              historyLoadFailed: false,
              guideClosing: false,
              ...this.staticBottomScrollForList(nextList),
            })
            this.scheduleComposerMeasure(40)
            this.maybeScheduleAlignPoll()
            return
          }
          this.setData({
            historyList: nextList,
            historyLoadFailed: false,
            guideClosing: false,
            ...this.staticBottomScrollForList(nextList),
          })
          this.scheduleComposerMeasure(40)
          return
        }

        if (this.data.sending || this.data.isTyping) {
          this.pendingHistoryRefresh = true
          return
        }
        if (mergeInFlightAssistant) {
          nextList = this.mergeServerHistoryWithLocalInflight(nextList)
        }
        this.setData({
          historyList: nextList,
          historyLoadFailed: false,
          guideClosing: false,
          ...this.staticBottomScrollForList(nextList),
        })
        this.scheduleComposerMeasure(40)
      })
      .catch((error) => {
        if (requestId !== this.historyRequestSeq) {
          return
        }
        const unauthorized = this.isUnauthorizedError(error)
        this.setData({
          errorMessage: unauthorized ? '' : formatRequestError(error, '获取聊天记录失败'),
          historyLoadFailed: unauthorized
            ? false
            : !this.data.historyList.length && !this.data.sending && !this.data.isTyping,
        })
      })
      .finally(() => {
        if (requestId !== this.historyRequestSeq) {
          return
        }
        this.setData({
          loading: false,
        })
        this.scheduleComposerMeasure(40)
      })
  },

  flushDeferredHistoryRefresh() {
    if (!this.pendingHistoryRefresh || this.data.sending || this.data.isTyping) {
      return
    }
    this.fetchChatHistory({
      silent: !!this.data.historyList.length,
    })
  },

  onMessageInput(e) {
    this.setData({
      message: e.detail.value || '',
      errorMessage: '',
    })
    if (this.data.keyboardHeight > 0) {
      this.autoStickToBottom = true
      // 单行输入时不再每个按键触发滚底；避免输入过程细碎抖动
    }
  },

  onInputLineChange() {
    // textarea 自动增高时，及时重算被输入框遮挡的滚动区高度，避免消息区被压住
    this.scheduleHistoryViewportMeasure(16)
    if (this.data.keyboardHeight > 0) {
      this.autoStickToBottom = true
      this.scrollToBottom(0, false)
    }
  },

  onKeyboardHeightChange(e) {
    const height = (e && e.detail && e.detail.height) || 0

    this.setData({
      keyboardHeight: height,
    }, () => {
      this.autoStickToBottom = true
      this.scheduleHistoryViewportMeasure(24)
      this.scrollToBottom(0, false, true)
    })
  },

  onInputFocus() {
    this.autoStickToBottom = true
    this.scheduleHistoryViewportMeasure(24)
    this.scrollToBottom(0, false, true)
  },

  onInputBlur() {
    this.setData({
      keyboardHeight: 0,
    }, () => {
      this.autoStickToBottom = true
      this.scheduleHistoryViewportMeasure(48)
      this.scrollToBottom(0, false, true)
    })
  },

  onHistoryScroll(e) {
    const detail = (e && e.detail) || {}
    const scrollTop = Number(detail.scrollTop || 0)
    const scrollHeight = Number(detail.scrollHeight || 0)
    this.latestHistoryScrollHeight = scrollHeight
    if (!this.historyViewportHeight) {
      this.scheduleHistoryViewportMeasure(0)
      return
    }

    // 手指按压期间：只要有微弱滚动（scrollTop 变化）即停止跟底
    if (this._chatFingerDown) {
      if (
        this._lastHistoryScrollTop !== undefined &&
        Math.abs(scrollTop - this._lastHistoryScrollTop) > 0.5
      ) {
        this.autoStickToBottom = false
      }
      this._lastHistoryScrollTop = scrollTop
      return
    }

    const effectiveViewportHeight = Math.max(
      0,
      this.historyViewportHeight - (this.composerOverlayHeight || 0)
    )
    const distanceToBottom =
      scrollHeight -
      scrollTop -
      effectiveViewportHeight -
      (this.data.bottomSpacerPx || 0)
    this.lastDistanceToBottom = distanceToBottom
    // 打字机跟底期间，保持底线固定，不在这里反复切换自动跟底状态，避免视觉抽搐
    if (this.data.isTyping && this.autoStickToBottom && !this._chatFingerDown) {
      return
    }
    if (this.autoStickToBottom && distanceToBottom > SCROLL_PAUSE_AWAY_PX) {
      this.autoStickToBottom = false
    }
    if (
      !this.autoStickToBottom &&
      this._chatScrollUserGesture &&
      distanceToBottom <= SCROLL_LOWER_THRESHOLD_PX
    ) {
      this.autoStickToBottom = true
    }
  },

  onChatScrollTouchStart() {
    if (this._chatScrollTouchEndTimer) {
      clearTimeout(this._chatScrollTouchEndTimer)
      this._chatScrollTouchEndTimer = null
    }
    this._chatScrollCooldownUntil = 0
    this._chatFingerDown = true
    this._chatScrollUserGesture = true
    this._lastHistoryScrollTop = undefined
  },

  onChatScrollTouchEnd() {
    this._chatFingerDown = false
    this._lastHistoryScrollTop = undefined
    this._chatScrollCooldownUntil = Date.now() + SCROLL_AFTER_TOUCH_COOLDOWN_MS
    if (this._chatScrollTouchEndTimer) {
      clearTimeout(this._chatScrollTouchEndTimer)
    }
    // 抬手后延迟 0.5s 再清除「可用于 scrolltolower 恢复跟底」的触控会话，便于惯性滑到底仍算一次手势
    this._chatScrollTouchEndTimer = setTimeout(() => {
      this._chatScrollTouchEndTimer = null
      this._chatScrollUserGesture = false
    }, 500)
  },

  /** 仅用户手势滑到列表底部时恢复跟底；程序 scroll-into-view 不会带 touch，不会误开 */
  onHistoryScrollToLower() {
    if (
      this._chatScrollUserGesture &&
      this.lastDistanceToBottom <= SCROLL_LOWER_THRESHOLD_PX
    ) {
      this.autoStickToBottom = true
    }
  },

  reloadHistory() {
    this.autoStickToBottom = true
    this.fetchChatHistory({ force: true, mergeInFlightAssistant: true })
  },

  useQuickPrompt(e) {
    const prompt = e.currentTarget.dataset.prompt || ''
    if (!prompt) {
      return
    }

    if (this.data.sending || this.data.isTyping) {
      wx.showToast({
        title: '请等待当前回复完成',
        icon: 'none',
      })
      return
    }

    if (!this.data.historyList.length && !this.data.guideClosing) {
      this.setData({
        errorMessage: '',
        message: '',
        guideClosing: true,
      })

      setTimeout(() => {
        this.sendTextMessage(prompt)
        this.setData({
          guideClosing: false,
        })
      }, 140)
      return
    }

    this.setData({
      errorMessage: '',
      message: '',
    })

    this.sendTextMessage(prompt)
  },

  startNewChat() {
    if (this.data.sending || this.data.isTyping) {
      wx.showToast({
        title: '请先等待当前回复结束',
        icon: 'none',
      })
      return
    }

    wx.showModal({
      title: '开始新对话',
      content: '这会清空当前聊天记录，并开启一个新的对话上下文。',
      success: (res) => {
        if (!res.confirm) {
          return
        }

        this.resetChatHistory()
      },
    })
  },

  resetChatHistory() {
    this.setData({
      loading: true,
      errorMessage: '',
    })

    request({
      url: RESET_ENDPOINT,
      method: 'POST',
    })
      .then(() => {
        this.clearOutputTasks()
        this.setData({
          historyList: [],
          historyLoadFailed: false,
          message: '',
          lastFailedMessage: '',
          guideClosing: false,
          isTyping: false,
          sending: false,
          scrollIntoView: '',
          scrollWithAnimation: false,
        })
        this.scheduleComposerMeasure(40)
        wx.showToast({
          title: '已开始新对话',
          icon: 'success',
        })
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '开始新对话失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  sendMessage() {
    const message = (this.data.message || '').trim()
    if (!message) {
      this.setData({
        errorMessage: '请输入你想和小爱说的话',
      })
      return
    }

    if (this.data.sending || this.data.isTyping) {
      wx.showToast({
        title: '请等待当前回复完成',
        icon: 'none',
      })
      return
    }

    this.sendTextMessage(message)
  },

  retryLastMessage() {
    const message = (this.data.lastFailedMessage || '').trim()
    if (!message) {
      return
    }

    if (this.data.sending || this.data.isTyping) {
      wx.showToast({
        title: '请等待当前回复完成',
        icon: 'none',
      })
      return
    }

    const prunedList = this.pruneFailedTurnForRetry(message)
    this.setData(
      {
        historyList: prunedList,
        message,
        errorMessage: '',
      },
      () => {
        this.sendTextMessage(message)
      }
    )
  },

  /**
   * 去掉本轮「用户 + 失败助手」占位，避免重试时重复插入用户气泡。
   */
  pruneFailedTurnForRetry(message) {
    const list = this.data.historyList || []
    if (list.length < 2) {
      return list
    }
    const last = list[list.length - 1]
    const prev = list[list.length - 2]
    const sameUser =
      prev.role === 'user' && String(prev.content || '').trim() === message
    const assistantFailed =
      last.role === 'assistant' && last.status === 'error'
    if (sameUser && assistantFailed) {
      return list.slice(0, -2)
    }
    return list
  },

  sendTextMessage(message) {
    const userId = this.createLocalId('user')
    const assistantId = this.createLocalId('assistant')

    const nextHistory = this.data.historyList.concat([
      {
        id: userId,
        anchorId: `message-${userId}`,
        role: 'user',
        content: message,
        status: 'done',
        createTime: this.formatNow(),
        isLocal: true,
      },
      {
        id: assistantId,
        anchorId: `message-${assistantId}`,
        role: 'assistant',
        content: '',
        status: 'thinking',
        createTime: '',
        isLocal: true,
      },
    ])

    this.setData({
      sending: true,
      isTyping: false,
      errorMessage: '',
      lastFailedMessage: '',
      message: '',
      historyList: nextHistory,
    })
    this.autoStickToBottom = true
    if (wx.vibrateShort) {
      try {
        wx.vibrateShort({ type: 'light' })
      } catch (error) {
        wx.vibrateShort()
      }
    }
    this.stoppedAssistantId = ''
    this.activeAssistantId = assistantId
    this.scheduleComposerMeasure(20)
    this.scrollToBottom(0, false, true)

    if (ACTIVE_STREAM_MODE === STREAM_MODE_CHUNK) {
      this.sendChunkStreamMessage(message, assistantId)
      return
    }

    if (ACTIVE_STREAM_MODE === STREAM_MODE_SOCKET) {
      this.sendSocketStreamMessage(message, assistantId)
      return
    }

    this.sendStandardMessage(message, assistantId)
  },

  sendStandardMessage(message, assistantId) {
    const { task, promise } = requestRaw({
      url: REQUEST_ENDPOINT,
      method: 'POST',
      data: {
        message,
      },
      timeout: 20000,
    })

    this.standardRequestTask = task

    promise
      .then((res) => {
        if (this.stoppedAssistantId === assistantId) {
          return
        }

        const parsed = this.parseChatSendResponse(res)
        if (!parsed.ok) {
          const errorText = parsed.error || '发送消息失败'
          this.markAssistantError(assistantId, errorText)
          this.setData({
            errorMessage: errorText,
            lastFailedMessage: message,
            message,
            sending: false,
            isTyping: false,
          })
          this.scheduleComposerMeasure(40)
          this.flushDeferredHistoryRefresh()
          return
        }

        this.startTypewriter(assistantId, parsed.reply)
      })
      .catch((error) => {
        if (error && error.errMsg && error.errMsg.includes('abort')) {
          return
        }

        const errorText = formatRequestError(error, '发送消息失败')
        this.markAssistantError(assistantId, errorText)
        if (this.isUnauthorizedError(error)) {
          this.setData({
            sending: false,
            isTyping: false,
          })
        } else {
          this.setData({
            errorMessage: errorText,
            lastFailedMessage: message,
            message,
            sending: false,
            isTyping: false,
          })
        }
        this.scheduleComposerMeasure(40)
        this.flushDeferredHistoryRefresh()
      })
      .finally(() => {
        if (this.standardRequestTask === task) {
          this.standardRequestTask = null
        }
      })
  },

  sendChunkStreamMessage(message, assistantId) {
    const token = getApp().globalData.token || wx.getStorageSync('token') || ''
    this.streamResidue = ''

    const requestTask = wx.request({
      url: `${getApp().globalData.baseUrl}${CHUNK_STREAM_ENDPOINT}`,
      method: 'POST',
      enableChunked: true,
      responseType: 'arraybuffer',
      header: {
        'content-type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {}),
      },
      data: {
        message,
      },
      success: () => {
        this.finishAssistantMessage(assistantId)
      },
      fail: (error) => {
        const errorText = formatRequestError(error, '流式请求失败')
        this.markAssistantError(assistantId, errorText)
        if (this.isUnauthorizedError(error)) {
          this.setData({
            sending: false,
            isTyping: false,
          })
        } else {
          this.setData({
            errorMessage: errorText,
            lastFailedMessage: message,
            message,
            sending: false,
            isTyping: false,
          })
        }
        this.scheduleComposerMeasure(40)
        this.flushDeferredHistoryRefresh()
      },
    })

    this.streamRequestTask = requestTask

    requestTask.onChunkReceived((res) => {
      const chunkText = this.decodeChunk(res.data)
      this.consumeSseChunk(chunkText, assistantId)
    })
  },

  sendSocketStreamMessage(message, assistantId) {
    if (!SOCKET_STREAM_URL) {
      const errorText = '当前未配置 WebSocket 流式地址'
      this.markAssistantError(assistantId, errorText)
      this.setData({
        errorMessage: errorText,
        lastFailedMessage: message,
        message,
        sending: false,
        isTyping: false,
      })
      this.scheduleComposerMeasure(40)
      this.flushDeferredHistoryRefresh()
      return
    }

    const socketTask = wx.connectSocket({
      url: SOCKET_STREAM_URL,
    })

    this.socketFailed = false
    this.socketTask = socketTask

    socketTask.onOpen(() => {
      socketTask.send({
        data: JSON.stringify({ message }),
      })
    })

    socketTask.onMessage((res) => {
      this.consumeSocketChunk(res.data, assistantId)
    })

    socketTask.onError(() => {
      this.socketFailed = true
      const errorText = '连接流式服务失败'
      this.markAssistantError(assistantId, errorText)
      this.setData({
        errorMessage: errorText,
        lastFailedMessage: message,
        message,
        sending: false,
        isTyping: false,
      })
      this.scheduleComposerMeasure(40)
    })

    socketTask.onClose(() => {
      if (this.socketFailed) {
        this.clearOutputTasks()
        this.scheduleComposerMeasure(40)
        this.flushDeferredHistoryRefresh()
        return
      }
      this.finishAssistantMessage(assistantId)
    })
  },

  consumeSseChunk(chunkText, assistantId) {
    if (!chunkText) {
      return
    }

    const merged = `${this.streamResidue || ''}${chunkText}`
    const events = merged.split('\n\n')
    this.streamResidue = events.pop() || ''

    events.forEach((eventText) => {
      const line = eventText
        .split('\n')
        .find((item) => item.trim().startsWith('data:'))

      if (!line) {
        return
      }

      const payload = line.replace(/^data:\s*/, '').trim()
      if (!payload || payload === '[DONE]') {
        this.finishAssistantMessage(assistantId)
        return
      }

      this.appendStreamContent(payload, assistantId)
    })
  },

  consumeSocketChunk(payload, assistantId) {
    if (!payload) {
      return
    }

    this.appendStreamContent(payload, assistantId)
  },

  appendStreamContent(payload, assistantId) {
    let content = payload

    try {
      const parsed = JSON.parse(payload)
      content = parsed.content || parsed.delta || parsed.reply || ''
      if (parsed.done) {
        this.finishAssistantMessage(assistantId)
      }
    } catch (error) {
      content = payload
    }

    if (!content) {
      return
    }

    this.bufferStreamAppend(assistantId, content)
  },

  bufferStreamAppend(assistantId, content) {
    this.activeAssistantId = assistantId
    this.streamAppendBuffer = `${this.streamAppendBuffer || ''}${content}`
    this.setData({
      sending: false,
      isTyping: true,
    }, () => {
      this.beginTypingBottomFollow()
    })

    if (this.streamFlushTimer) {
      return
    }

    this.streamFlushTimer = setTimeout(() => {
      this.flushStreamAppend()
    }, 48)
  },

  flushStreamAppend() {
    if (this.streamFlushTimer) {
      clearTimeout(this.streamFlushTimer)
      this.streamFlushTimer = null
    }

    if (!this.activeAssistantId || !this.streamAppendBuffer) {
      return
    }

    const index = this.findMessageIndexById(this.activeAssistantId)
    if (index < 0) {
      this.streamAppendBuffer = ''
      this.clearOutputTasks({ includeNetwork: false })
      this.setData({
        sending: false,
        isTyping: false,
      })
      return
    }

    const currentContent = this.data.historyList[index].content || ''
    const nextContent = `${currentContent}${this.streamAppendBuffer}`
    this.streamAppendBuffer = ''

    this.setData({
      [`historyList[${index}].status`]: 'typing',
      [`historyList[${index}].content`]: nextContent,
    })
    this.followTypingBottom(false)
  },

  startTypewriter(assistantId, fullText) {
    const index = this.findMessageIndexById(assistantId)
    if (index < 0) {
      return
    }

    this.clearOutputTasks({ includeNetwork: false })
    this.activeAssistantId = assistantId
    this.pendingReplyText = fullText || ''
    this.renderedReplyText = ''

    this.setData({
      [`historyList[${index}].status`]: 'typing',
      sending: false,
      isTyping: true,
    }, () => {
      this.beginTypingBottomFollow()
      this.followTypingBottom(true)
    })

    const step = () => {
      const rest = this.pendingReplyText.slice(this.renderedReplyText.length)
      if (!rest.length) {
        this.finishAssistantMessage(assistantId)
        return
      }

      const nextChunk = rest.slice(0, TYPEWRITER_CHUNK_SIZE)
      this.renderedReplyText += nextChunk
      const currentIndex = this.findMessageIndexById(assistantId)
      if (currentIndex < 0) {
        this.clearOutputTasks({ includeNetwork: false })
        this.setData({
          sending: false,
          isTyping: false,
        })
        return
      }

      this.setData({
        [`historyList[${currentIndex}].content`]: this.renderedReplyText,
      })
      this.followTypingBottom(false)

      this.typewriterTimer = setTimeout(step, TYPEWRITER_INTERVAL)
    }

    step()
  },

  stopOutput() {
    if (!this.data.isTyping && !this.data.sending) {
      return
    }

    if (this.socketTask) {
      this.socketTask.close({})
      this.socketTask = null
    }

    if (this.streamRequestTask && this.streamRequestTask.abort) {
      this.streamRequestTask.abort()
      this.streamRequestTask = null
    }

    if (this.standardRequestTask && this.standardRequestTask.abort) {
      this.standardRequestTask.abort()
      this.standardRequestTask = null
    }

    if (this.activeAssistantId) {
      this.stoppedAssistantId = this.activeAssistantId
    }

    if (this.typewriterTimer && this.activeAssistantId) {
      clearTimeout(this.typewriterTimer)
      this.typewriterTimer = null
    }

    if (this.activeAssistantId) {
      this.stopAssistantMessage(this.activeAssistantId)
    } else {
      this.setData({
        sending: false,
        isTyping: false,
      })
    }
  },

  stopAssistantMessage(assistantId) {
    if (this.activeAssistantId === assistantId) {
      this.flushStreamAppend()
    }

    const index = this.findMessageIndexById(assistantId)
    if (index >= 0) {
      const currentContent = this.data.historyList[index].content || ''
      this.setData({
        [`historyList[${index}].status`]: 'done',
        [`historyList[${index}].content`]: currentContent || '已停止回复',
      })
    }

    this.clearOutputTasks()
    this.setData({
      sending: false,
      isTyping: false,
    })
    this.scheduleComposerMeasure(40)
    if (this.autoStickToBottom) {
      this.scrollToBottom(0, false, true)
    }
    this.flushDeferredHistoryRefresh()
  },

  finishAssistantMessage(assistantId) {
    if (this.activeAssistantId === assistantId) {
      this.flushStreamAppend()
    }

    const index = this.findMessageIndexById(assistantId)
    if (index >= 0) {
      this.setData({
        [`historyList[${index}].status`]: 'done',
      })
    }

    this.clearOutputTasks()
    this.setData({
      sending: false,
      isTyping: false,
    })
    this.scheduleComposerMeasure(40)
    if (this.autoStickToBottom) {
      this.scrollToBottom(0, false, true)
    }
    this.flushDeferredHistoryRefresh()
  },

  markAssistantError(assistantId, message) {
    const index = this.findMessageIndexById(assistantId)
    if (index >= 0) {
      this.setData({
        [`historyList[${index}].status`]: 'error',
        [`historyList[${index}].content`]: '暂时没能接住这句话，请重试一次。',
      })
    }
  },

  isUnauthorizedError(error) {
    return !!(error && typeof error.statusCode === 'number' && error.statusCode === 401)
  },

  clearOutputTasks(options = {}) {
    const { includeNetwork = true } = options
    if (this.typewriterTimer) {
      clearTimeout(this.typewriterTimer)
      this.typewriterTimer = null
    }
    if (this.streamFlushTimer) {
      clearTimeout(this.streamFlushTimer)
      this.streamFlushTimer = null
    }
    if (this.scrollBottomTimer) {
      clearTimeout(this.scrollBottomTimer)
      this.scrollBottomTimer = null
    }
    if (includeNetwork) {
      if (this.streamRequestTask && this.streamRequestTask.abort) {
        this.streamRequestTask.abort()
        this.streamRequestTask = null
      }
      if (this.standardRequestTask && this.standardRequestTask.abort) {
        this.standardRequestTask.abort()
        this.standardRequestTask = null
      }
      if (this.socketTask) {
        this.socketTask.close({})
        this.socketTask = null
      }
    }
    this.activeAssistantId = ''
    this.pendingReplyText = ''
    this.renderedReplyText = ''
    this.streamAppendBuffer = ''
    this.streamResidue = ''
    this.socketFailed = false
    this._lastTypingAnchorToggleAt = 0
    this.endTypingBottomFollow()
  },

  findMessageIndexById(id) {
    return this.data.historyList.findIndex((item) => item.id === id)
  },

  createLocalId(prefix) {
    return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}`
  },

  formatNow() {
    const now = new Date()
    const year = now.getFullYear()
    const month = `${now.getMonth() + 1}`.padStart(2, '0')
    const day = `${now.getDate()}`.padStart(2, '0')
    const hour = `${now.getHours()}`.padStart(2, '0')
    const minute = `${now.getMinutes()}`.padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  },

  /** 历史接口常为 ISO 字符串，展示时去掉日期与时间之间的 T，并去掉毫秒与 Z */
  formatChatTime(value) {
    if (value == null || value === '') {
      return ''
    }
    let s = String(value).trim().replace('T', ' ')
    s = s.replace(/\.\d+/, '')
    s = s.replace(/Z$/i, '')
    return s.trim()
  },

  decodeChunk(arrayBuffer) {
    try {
      return new TextDecoder('utf-8').decode(arrayBuffer)
    } catch (error) {
      return ''
    }
  },

  beginTypingBottomFollow() {
    const effectiveViewportHeight = Math.max(
      0,
      this.historyViewportHeight - (this.composerOverlayHeight || 0)
    )
    this.typingFollowEnabled = true
    this.typingFollowViewportHeight = effectiveViewportHeight
    this.typingFollowBottomSpacer = this.data.bottomSpacerPx || 0
    this.typingFollowLastTop = this.data.historyScrollTop || 0
    this.typingFollowMeasurePending = false
    this.typingFollowLastMeasureAt = 0
  },

  endTypingBottomFollow() {
    this.typingFollowEnabled = false
    this.typingFollowViewportHeight = 0
    this.typingFollowBottomSpacer = 0
    this.typingFollowLastTop = 0
    this.typingFollowMeasurePending = false
    this.typingFollowLastMeasureAt = 0
  },

  followTypingBottom(force = false) {
    if (!this.typingFollowEnabled || !this.autoStickToBottom) {
      return
    }
    if (!force && this._chatFingerDown) {
      return
    }
    if (!force && this._chatScrollCooldownUntil && Date.now() < this._chatScrollCooldownUntil) {
      return
    }
    const now = Date.now()
    if (!force && now - (this.typingFollowLastMeasureAt || 0) < SCROLL_TYPING_FOLLOW_MEASURE_MIN_MS) {
      return
    }
    if (this.typingFollowMeasurePending) {
      return
    }
    this.typingFollowLastMeasureAt = now
    this.typingFollowMeasurePending = true
    const query = this.createSelectorQuery()
    query.select('.chat-history').fields({ size: true, scrollOffset: true }, (metrics) => {
      this.typingFollowMeasurePending = false
      const scrollHeight = Number((metrics && metrics.scrollHeight) || this.latestHistoryScrollHeight || 0)
      if (!scrollHeight) {
        return
      }
      this.latestHistoryScrollHeight = scrollHeight
      const desiredTop = Math.max(
        0,
        scrollHeight - this.typingFollowViewportHeight - this.typingFollowBottomSpacer
      )
      const nextTop = Math.max(this.typingFollowLastTop || 0, Math.floor(desiredTop))
      if (!force && Math.abs((this.data.historyScrollTop || 0) - nextTop) < SCROLL_TYPING_FOLLOW_MIN_DELTA_PX) {
        return
      }
      this.typingFollowLastTop = nextTop
      this.lastAutoScrollAt = Date.now()
      this.setData({
        historyScrollTop: nextTop,
        scrollIntoView: '',
        scrollWithAnimation: false,
      })
    })
    query.exec()
  },

  scrollToBottom(delay = 40, animate = true, force = false) {
    if (!force && this.typingFollowEnabled && this.data.isTyping && this.autoStickToBottom) {
      this.followTypingBottom(false)
      return
    }
    if (!force && this.autoStickToBottom === false) {
      return
    }
    // 用户手指在列表上或刚离手惯性中：不抢滚动，减轻卡顿与尾抖
    if (!force && this._chatFingerDown) {
      return
    }
    if (!force && this._chatScrollCooldownUntil && Date.now() < this._chatScrollCooldownUntil) {
      return
    }

    const followDuringReply = this.data.isTyping && this.autoStickToBottom
    const minInterval = animate ? 120 : 160
    const now = Date.now()
    if (!force) {
      const elapsed = now - (this.lastAutoScrollAt || 0)
      if (followDuringReply) {
        if (elapsed < SCROLL_FOLLOW_TYPING_MIN_MS) {
          return
        }
      } else if (elapsed < minInterval) {
        return
      }
    }

    const markScrolled = () => {
      this.lastAutoScrollAt = Date.now()
    }

    if (!animate) {
      const target = 'chat-bottom'
      const apply = () => {
        markScrolled()
        this.setData({
          scrollIntoView: target,
          scrollWithAnimation: false,
        })
      }
      const triggerApply = () => {
        if (this.data.scrollIntoView === target) {
          const now = Date.now()
          // 打字跟底且已停在锚点：缩短间隔内避免反复「清空→再绑」造成中段抽动，仅轻推一次
          if (
            followDuringReply &&
            this._lastTypingAnchorToggleAt &&
            now - this._lastTypingAnchorToggleAt < SCROLL_TYPING_ANCHOR_TOGGLE_MIN_MS
          ) {
            markScrolled()
            this.setData({
              scrollIntoView: target,
              scrollWithAnimation: false,
            })
            return
          }
          this._lastTypingAnchorToggleAt = now
          this.setData({ scrollIntoView: '', scrollWithAnimation: false })
          if (typeof wx !== 'undefined' && wx.nextTick) {
            wx.nextTick(apply)
          } else {
            setTimeout(apply, 0)
          }
          return
        }
        apply()
      }
      const scheduleDelay =
        delay > 0 ? delay : (!force && followDuringReply ? SCROLL_FOLLOW_TYPING_DELAY_MS : 0)
      if (scheduleDelay > 0) {
        // 打字中：若已有一次待执行滚底，直接复用，避免中段排队多次全量切换
        if (!force && followDuringReply && this.scrollBottomTimer) {
          return
        }
        if (this.scrollBottomTimer) {
          clearTimeout(this.scrollBottomTimer)
          this.scrollBottomTimer = null
        }
        this.scrollBottomTimer = setTimeout(() => {
          this.scrollBottomTimer = null
          if (!force && this.autoStickToBottom === false) {
            return
          }
          if (!force && this._chatFingerDown) {
            return
          }
          if (!force && this._chatScrollCooldownUntil && Date.now() < this._chatScrollCooldownUntil) {
            return
          }
          triggerApply()
        }, scheduleDelay)
        return
      }
      if (this.scrollBottomTimer) {
        clearTimeout(this.scrollBottomTimer)
        this.scrollBottomTimer = null
      }
      triggerApply()
      return
    }

    if (this.scrollBottomTimer) {
      clearTimeout(this.scrollBottomTimer)
      this.scrollBottomTimer = null
    }
    this.setData({
      scrollIntoView: '',
      scrollWithAnimation: true,
    })

    this.scrollBottomTimer = setTimeout(() => {
      this.scrollBottomTimer = null
      if (!force && this.autoStickToBottom === false) {
        return
      }
      markScrolled()
      this.setData({
        scrollIntoView: 'chat-bottom',
        scrollWithAnimation: true,
      })
    }, delay)
  },

  /**
   * 解析 POST /api/consult/chat/send 的响应，只认 Result{ data: { reply } }，避免把微信的 errMsg 等误当正文。
   */
  parseChatSendResponse(res) {
    let body = res && res.data
    if (typeof body === 'string') {
      try {
        body = JSON.parse(body)
      } catch (e) {
        return { ok: false, error: '服务器返回非 JSON，请检查后端与 baseUrl' }
      }
    }
    if (!body || typeof body !== 'object') {
      return { ok: false, error: '响应格式异常' }
    }

    if (Object.prototype.hasOwnProperty.call(body, 'code') && !(body.code >= 200 && body.code < 300)) {
      return { ok: false, error: (body && body.message) || '发送消息失败' }
    }

    let payload = body
    if (Object.prototype.hasOwnProperty.call(body, 'data')) {
      payload = body.data
    }

    if (payload == null || typeof payload !== 'object') {
      return { ok: false, error: '缺少回复数据' }
    }

    const reply = payload.reply
    const text = typeof reply === 'string' ? reply.trim() : ''
    if (!text) {
      return { ok: false, error: '小爱未返回有效内容，请重试' }
    }
    if (text === 'request:ok') {
      return { ok: false, error: '解析异常，请勿将网络状态当作回复。请重试或检查后端接口。' }
    }

    return { ok: true, reply: text }
  },

})
