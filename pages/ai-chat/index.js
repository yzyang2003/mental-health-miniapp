const { request, formatRequestError } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')
const { SCROLL_CONSTANTS, createScrollManager } = require('../../utils/chat-scroll')
const { createTypewriter } = require('../../utils/chat-typewriter')
const { STREAM_MODES, createStreamHandler } = require('../../utils/chat-stream')
const {
  savePageState,
  restorePageState,
  restoreThinkingBubbleAfterReturn,
  createHistoryManager,
} = require('../../utils/chat-state')

const RESET_ENDPOINT = '/api/consult/chat/reset'
const STT_ENDPOINT = '/api/consult/chat/stt'
const TTS_ENDPOINT = '/api/consult/chat/tts'
const ACTIVE_STREAM_MODE = STREAM_MODES.TYPING
const SOCKET_STREAM_URL = ''

Page({
  data: {
    navTop: 8, sideInsetPx: 12, contentInsetPx: 12, scrollGutterPx: 2,
    historyTopSafePx: 2, shellRadiusPx: 16, capsuleTopPx: 0,
    scrollLowerThreshold: SCROLL_CONSTANTS.LOWER_THRESHOLD_PX,
    loading: false, historyLoadFailed: false, sending: false, isTyping: false,
    guideClosing: false, topInset: 8, bottomSafeInset: 0, keyboardHeight: 0,
    composerBottom: 8, composerHeight: 132, contentGap: 8, bottomSpacerPx: 0,
    page: 1, size: 20, historyList: [], message: '', errorMessage: '',
    lastFailedMessage: '', historyScrollTop: 0, scrollIntoView: '',
    scrollWithAnimation: false, isRecording: false, recordingDuration: 0, inputMode: 'text', isInputFocus: false, inputLineCount: 1, isInputExpanded: false,
    quickPrompts: [
      { icon: '😰', text: '我最近压力很大，不知道怎么调整' },
      { icon: '😴', text: '这几天总失眠，可以怎么缓解' },
      { icon: '😔', text: '我总是焦虑，会反复胡思乱想' },
      { icon: '💼', text: '我最近情绪有点低落，想聊聊' },
    ],
  },

  onLoad() {
    if (!this.ensureLogin()) return
    Object.assign(this, createScrollManager(this))
    Object.assign(this, createTypewriter(this))
    Object.assign(this, createStreamHandler(this))
    Object.assign(this, createHistoryManager(this))

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
    restorePageState(this)
    this.scheduleComposerMeasure(60)
    this.scheduleHistoryViewportMeasure(80)
    this._historyFetchScheduledFromOnLoad = true
    this.fetchChatHistory({ force: true, mergeInFlightAssistant: true })
  },

  onHide() {
    if (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) {
      savePageState(this)
    }
    if (this.data.keyboardHeight) this.setData({ keyboardHeight: 0 })
  },

  onShow() {
    if (!this.ensureLogin()) return
    const isFirstShowAfterLoad = this._firstShowAfterLoad
    this._firstShowAfterLoad = false
    if (this.data.keyboardHeight) this.setData({ keyboardHeight: 0 })
    this.initLayoutMetrics()
    this.scheduleComposerMeasure(60)
    this.scheduleHistoryViewportMeasure(80)
    restoreThinkingBubbleAfterReturn(this)
    const prefill = getApp().globalData.aiChatPrefill
    if (prefill) {
      getApp().globalData.aiChatPrefill = ''
      this.setData({ message: prefill })
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
    if (!historyFetchFromQuiz && !this.data.loading && !this.data.historyList.length && !this.data.sending && !this.data.isTyping) {
      if (!(isFirstShowAfterLoad && this._historyFetchScheduledFromOnLoad)) {
        this.fetchChatHistory()
      }
    }
    if (!isFirstShowAfterLoad || this._shouldAlignAfterRestore) {
      this.scheduleServerAlignment()
    }
    this._shouldAlignAfterRestore = false
  },

  onUnload() {
    this._firstShowAfterLoad = true
    if (this.data.sending || this.data.isTyping || this.hasInflightAssistantBubble()) {
      savePageState(this, { savedByUnload: true })
    } else {
      getApp().globalData.aiChatPageState = null
    }
    this.clearOutputTasks()
    const timers = ['scrollBottomTimer', '_chatScrollTouchEndTimer', 'historyViewportMeasureTimer', 'measureComposerTimer', 'alignPollTimer', 'serverAlignTimer']
    timers.forEach((t) => { if (this[t]) { clearTimeout(this[t]); this[t] = null } })
  },

  ensureLogin() { return ensurePageLogin() },

  goBack() {
    wx.navigateBack({ delta: 1 })
  },

  // ── 输入事件 ──────────────────────────────────────────────────────
  onMessageInput(e) {
    const message = e.detail.value || ''
    this.setData({ message, errorMessage: '' })
    if (this.data.keyboardHeight > 0) this.autoStickToBottom = true
    // 计算行数
    this.calculateLineCount(message)
  },
  calculateLineCount(text) {
    if (!text) {
      this.setData({ inputLineCount: 1 })
      return
    }
    // 检测换行符
    const newlineCount = (text.match(/\n/g) || []).length
    // 估算每行字符数（根据输入框宽度）
    const charsPerLine = 20
    const textLines = Math.ceil(text.length / charsPerLine)
    // 取较大值
    const totalLines = Math.max(newlineCount + 1, textLines)
    this.setData({ inputLineCount: Math.min(totalLines, 4) })
  },
  onInputLineChange(e) {
    const lineCount = (e.detail && e.detail.lineCount) || 1
    this.setData({ inputLineCount: lineCount })
    this.scheduleHistoryViewportMeasure(16)
    if (this.data.keyboardHeight > 0) { this.autoStickToBottom = true; this.scrollToBottom(0, false) }
  },
  onKeyboardHeightChange(e) {
    const height = (e && e.detail && e.detail.height) || 0
    this.setData({ keyboardHeight: height }, () => {
      this.autoStickToBottom = true; this.scheduleHistoryViewportMeasure(24); this.scrollToBottom(0, false, true)
    })
  },
  onInputFocus() {
    this.setData({ isInputFocus: true })
    this.autoStickToBottom = true; this.scheduleHistoryViewportMeasure(24); this.scrollToBottom(0, false, true)
  },
  onInputBlur() {
    this.setData({ isInputFocus: false, keyboardHeight: 0 }, () => {
      this.autoStickToBottom = true; this.scheduleHistoryViewportMeasure(48); this.scrollToBottom(0, false, true)
    })
  },

  // ── 快捷操作 ──────────────────────────────────────────────────────
  reloadHistory() {
    this.autoStickToBottom = true
    this.fetchChatHistory({ force: true, mergeInFlightAssistant: true })
  },
  useQuickPrompt(e) {
    const promptObj = e.currentTarget.dataset.prompt || {}
    const prompt = promptObj.text || ''
    if (!prompt) return
    if (this.data.sending || this.data.isTyping) { wx.showToast({ title: '请等待当前回复完成', icon: 'none' }); return }
    if (!this.data.historyList.length && !this.data.guideClosing) {
      this.setData({ errorMessage: '', message: '', guideClosing: true })
      setTimeout(() => { this.sendTextMessage(prompt); this.setData({ guideClosing: false }) }, 140)
      return
    }
    this.setData({ errorMessage: '', message: '' })
    this.sendTextMessage(prompt)
  },
  startNewChat() {
    if (this.data.sending || this.data.isTyping) { wx.showToast({ title: '请先等待当前回复结束', icon: 'none' }); return }
    wx.showModal({
      title: '开始新对话', content: '这会清空当前聊天记录，并开启一个新的对话上下文。',
      success: (res) => { if (res.confirm) this.resetChatHistory() },
    })
  },
  resetChatHistory() {
    this.setData({ loading: true, errorMessage: '' })
    request({ url: RESET_ENDPOINT, method: 'POST' })
      .then(() => {
        this.clearOutputTasks()
        this.setData({
          historyList: [], historyLoadFailed: false, message: '', lastFailedMessage: '',
          guideClosing: false, isTyping: false, sending: false, scrollIntoView: '', scrollWithAnimation: false,
        })
        this.scheduleComposerMeasure(40)
        wx.showToast({ title: '已开始新对话', icon: 'success' })
      })
      .catch((error) => { this.setData({ errorMessage: formatRequestError(error, '开始新对话失败') }) })
      .finally(() => { this.setData({ loading: false }) })
  },

  // ── 消息发送 ──────────────────────────────────────────────────────
  sendMessage() {
    const message = (this.data.message || '').trim()
    if (!message) { this.setData({ errorMessage: '请输入你想和小爱说的话' }); return }
    if (this.data.sending || this.data.isTyping) { wx.showToast({ title: '请等待当前回复完成', icon: 'none' }); return }
    this.sendTextMessage(message)
  },
  retryLastMessage() {
    const message = (this.data.lastFailedMessage || '').trim()
    if (!message) return
    if (this.data.sending || this.data.isTyping) { wx.showToast({ title: '请等待当前回复完成', icon: 'none' }); return }
    const prunedList = this.pruneFailedTurnForRetry(message)
    this.setData({ historyList: prunedList, message, errorMessage: '' }, () => { this.sendTextMessage(message) })
  },
  pruneFailedTurnForRetry(message) {
    const list = this.data.historyList || []
    if (list.length < 2) return list
    const last = list[list.length - 1], prev = list[list.length - 2]
    if (prev.role === 'user' && String(prev.content || '').trim() === message && last.role === 'assistant' && last.status === 'error') {
      return list.slice(0, -2)
    }
    return list
  },
  sendTextMessage(message) {
    const userId = this.createLocalId('user')
    const assistantId = this.createLocalId('assistant')
    const nextHistory = this.data.historyList.concat([
      { id: userId, anchorId: `message-${userId}`, role: 'user', content: message, status: 'done', createTime: this.formatNow(), isLocal: true },
      { id: assistantId, anchorId: `message-${assistantId}`, role: 'assistant', content: '', status: 'thinking', createTime: '', isLocal: true },
    ])
    this.setData({ sending: true, isTyping: false, errorMessage: '', lastFailedMessage: '', message: '', historyList: nextHistory })
    this.autoStickToBottom = true
    if (wx.vibrateShort) { try { wx.vibrateShort({ type: 'light' }) } catch (e) { wx.vibrateShort() } }
    this.stoppedAssistantId = ''
    this.activeAssistantId = assistantId
    this.scheduleComposerMeasure(20)
    this.scrollToBottom(0, false, true)
    if (ACTIVE_STREAM_MODE === STREAM_MODES.CHUNK) { this.sendChunkStreamMessage(message, assistantId); return }
    if (ACTIVE_STREAM_MODE === STREAM_MODES.SOCKET) { this.sendSocketStreamMessage(message, assistantId, SOCKET_STREAM_URL); return }
    this.sendStandardMessage(message, assistantId)
  },

  // ── 输入模式切换 ──────────────────────────────────────────────────
  switchToVoiceMode() {
    this.setData({ inputMode: 'voice', keyboardHeight: 0 })
    wx.hideKeyboard()
  },
  switchToTextMode() {
    this.setData({ inputMode: 'text' })
  },
  expandInput() {
    this.setData({ isInputExpanded: true })
  },
  collapseInput() {
    this.setData({ isInputExpanded: false })
  },

  // ── 语音录制 ──────────────────────────────────────────────────────
  startRecording() {
    if (this.data.sending || this.data.isTyping) return
    // 检查录音权限
    wx.authorize({
      scope: 'scope.record',
      success: () => {
        this.doStartRecording()
      },
      fail: () => {
        wx.showModal({
          title: '需要录音权限',
          content: '语音输入需要您的录音权限，请在设置中开启',
          confirmText: '去设置',
          success: (res) => {
            if (res.confirm) {
              wx.openSetting()
            }
          },
        })
      },
    })
  },
  doStartRecording() {
    this.setData({ isRecording: true, recordingDuration: 0 })
    // 开始计时
    this.recordingTimer = setInterval(() => {
      this.setData({ recordingDuration: this.data.recordingDuration + 1 })
    }, 1000)
    this.recorderManager = wx.getRecorderManager()
    this.recorderManager.onStart(() => {
      console.log('录音开始')
    })
    this.recorderManager.onStop((res) => {
      console.log('录音结束', res)
      if (res.tempFilePath) {
        this.uploadAudioForStt(res.tempFilePath)
      }
    })
    this.recorderManager.onError((err) => {
      console.error('录音错误', err)
      this.setData({ isRecording: false })
      wx.showToast({ title: '录音失败', icon: 'none' })
    })
    this.recorderManager.start({
      format: 'mp3',
      duration: 60000,
      sampleRate: 16000,
      numberOfChannels: 1,
    })
  },
  stopRecording() {
    if (!this.data.isRecording) return
    this.setData({ isRecording: false })
    if (this.recordingTimer) {
      clearInterval(this.recordingTimer)
      this.recordingTimer = null
    }
    if (this.recorderManager) {
      this.recorderManager.stop()
    }
  },
  cancelRecording() {
    if (!this.data.isRecording) return
    this.setData({ isRecording: false })
    if (this.recordingTimer) {
      clearInterval(this.recordingTimer)
      this.recordingTimer = null
    }
    if (this.recorderManager) {
      this.recorderManager.stop()
    }
    wx.showToast({ title: '已取消录音', icon: 'none' })
  },
  uploadAudioForStt(filePath) {
    wx.showLoading({ title: '识别中...' })
    wx.uploadFile({
      url: getApp().globalData.baseUrl + STT_ENDPOINT,
      filePath: filePath,
      name: 'audio',
      header: {
        'Authorization': 'Bearer ' + wx.getStorageSync('token'),
      },
      success: (res) => {
        wx.hideLoading()
        try {
          const data = JSON.parse(res.data)
          if (data.code === 0 && data.data) {
            this.setData({ message: data.data })
            wx.showToast({ title: '识别成功', icon: 'success' })
          } else {
            wx.showToast({ title: data.message || '识别失败', icon: 'none' })
          }
        } catch (e) {
          wx.showToast({ title: '识别失败', icon: 'none' })
        }
      },
      fail: () => {
        wx.hideLoading()
        wx.showToast({ title: '上传失败', icon: 'none' })
      },
    })
  },

  // ── 语音播放 ──────────────────────────────────────────────────────
  playTts(e) {
    const content = e.currentTarget.dataset.content || ''
    if (!content) return
    // 检查缓存
    if (this.ttsCache && this.ttsCache[content]) {
      this.playAudio(this.ttsCache[content])
      return
    }
    wx.showLoading({ title: '生成语音...' })
    request({
      url: TTS_ENDPOINT,
      method: 'POST',
      data: { text: content },
    })
      .then((res) => {
        wx.hideLoading()
        if (res && res.audioUrl) {
          // 缓存音频
          if (!this.ttsCache) this.ttsCache = {}
          this.ttsCache[content] = res.audioUrl
          this.playAudio(res.audioUrl)
        } else {
          wx.showToast({ title: '生成失败', icon: 'none' })
        }
      })
      .catch(() => {
        wx.hideLoading()
        wx.showToast({ title: '生成失败', icon: 'none' })
      })
  },
  playAudio(audioUrl) {
    const audioContext = wx.createInnerAudioContext()
    audioContext.src = audioUrl
    audioContext.play()
    audioContext.onEnded(() => {
      audioContext.destroy()
    })
    audioContext.onError((err) => {
      console.error('播放失败', err)
      wx.showToast({ title: '播放失败', icon: 'none' })
      audioContext.destroy()
    })
  },

  // ── 输出控制 ──────────────────────────────────────────────────────
  stopOutput() {
    if (!this.data.isTyping && !this.data.sending) return
    if (this.socketTask) { this.socketTask.close({}); this.socketTask = null }
    if (this.streamRequestTask && this.streamRequestTask.abort) { this.streamRequestTask.abort(); this.streamRequestTask = null }
    if (this.standardRequestTask && this.standardRequestTask.abort) { this.standardRequestTask.abort(); this.standardRequestTask = null }
    if (this.activeAssistantId) this.stoppedAssistantId = this.activeAssistantId
    if (this.typewriterTimer && this.activeAssistantId) { clearTimeout(this.typewriterTimer); this.typewriterTimer = null }
    if (this.activeAssistantId) this.stopAssistantMessage(this.activeAssistantId)
    else this.setData({ sending: false, isTyping: false })
  },
  stopAssistantMessage(assistantId) {
    if (this.activeAssistantId === assistantId) this.flushStreamAppend()
    const index = this.findMessageIndexById(assistantId)
    if (index >= 0) {
      this.setData({
        [`historyList[${index}].status`]: 'done',
        [`historyList[${index}].content`]: this.data.historyList[index].content || '已停止回复',
      })
    }
    this.clearOutputTasks()
    this.setData({ sending: false, isTyping: false })
    this.scheduleComposerMeasure(40)
    if (this.autoStickToBottom) this.scrollToBottom(0, false, true)
    this.flushDeferredHistoryRefresh()
  },
  finishAssistantMessage(assistantId) {
    if (this.activeAssistantId === assistantId) this.flushStreamAppend()
    const index = this.findMessageIndexById(assistantId)
    if (index >= 0) this.setData({ [`historyList[${index}].status`]: 'done' })
    this.clearOutputTasks()
    this.setData({ sending: false, isTyping: false })
    this.scheduleComposerMeasure(40)
    if (this.autoStickToBottom) this.scrollToBottom(0, false, true)
    this.flushDeferredHistoryRefresh()
  },
  markAssistantError(assistantId) {
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
    if (this.typewriterTimer) { clearTimeout(this.typewriterTimer); this.typewriterTimer = null }
    if (this.streamFlushTimer) { clearTimeout(this.streamFlushTimer); this.streamFlushTimer = null }
    if (this.scrollBottomTimer) { clearTimeout(this.scrollBottomTimer); this.scrollBottomTimer = null }
    if (includeNetwork) {
      if (this.streamRequestTask && this.streamRequestTask.abort) { this.streamRequestTask.abort(); this.streamRequestTask = null }
      if (this.standardRequestTask && this.standardRequestTask.abort) { this.standardRequestTask.abort(); this.standardRequestTask = null }
      if (this.socketTask) { this.socketTask.close({}); this.socketTask = null }
    }
    this.activeAssistantId = ''; this.pendingReplyText = ''; this.renderedReplyText = ''
    this.streamAppendBuffer = ''; this.streamResidue = ''; this.socketFailed = false
    this._lastTypingAnchorToggleAt = 0
    this.endTypingBottomFollow()
  },

  // ── 工具方法 ──────────────────────────────────────────────────────
  findMessageIndexById(id) { return this.data.historyList.findIndex((item) => item.id === id) },
  createLocalId(prefix) { return `${prefix}-${Date.now()}-${Math.floor(Math.random() * 100000)}` },
  formatNow() {
    const n = new Date()
    const pad = (v) => String(v).padStart(2, '0')
    return `${n.getFullYear()}-${pad(n.getMonth() + 1)}-${pad(n.getDate())} ${pad(n.getHours())}:${pad(n.getMinutes())}`
  },
  formatChatTime(value) {
    if (value == null || value === '') return ''
    return String(value).trim().replace('T', ' ').replace(/\.\d+/, '').replace(/Z$/i, '').trim()
  },
})
