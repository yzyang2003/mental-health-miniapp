const { request } = require('../../utils/request')
const { normalizeQuestionnaireList } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')
const { sortQuestionnaireList } = require('../../utils/quizCatalog')
const { CORE_NOTE_BY_TYPE } = require('../../utils/consultConfig')
Page({
  data: {
    capsuleTopPx: 0,
    capsuleRightPx: 0,
    modules: [
      {
        key: 'quiz',
        title: '心理测评',
        desc: '选择量表完成测评，系统会给出分值、结论和建议。',
        path: '/pages/quiz-list/index',
        icon: '📊',
        colorTheme: 'blue',
      },
      {
        key: 'chat',
        title: 'AI 咨询师"小爱"',
        desc: '和小爱进行文字对话，支持查看最近聊天记录。',
        path: '/pages/ai-chat/index',
        icon: '💬',
        colorTheme: 'purple',
      },
    ],
    questionnaireQuickList: [],
    chatSummary: '',
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.fetchQuestionnaireQuickList()
    this.fetchChatSummary()
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
    this.calcCapsule()
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

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchQuestionnaireQuickList() {
    this.setData({ quickListLoading: true })
    return request({
      url: '/api/consult/quiz/list',
      method: 'GET',
    })
      .then((data) => {
        const quickList = sortQuestionnaireList(normalizeQuestionnaireList(data || []))
          .filter((item) => item && item.id && item.title)
          .map((item) => ({
            id: item.id,
            title: item.title,
            coreNote: this.buildQuestionnaireCoreNote(item),
          }))
          .slice(0, 5)
        this.setData({
          questionnaireQuickList: quickList,
          quickListLoading: false,
        })
      })
      .catch(() => {
        this.setData({
          questionnaireQuickList: [],
          quickListLoading: false,
        })
      })
  },

  fetchChatSummary() {
    return request({
      url: '/api/consult/chat/history',
      method: 'GET',
      data: { page: 1, size: 2 },
    })
      .then((res) => {
        const records = (res && res.records) || []
        if (records.length === 0) {
          this.setData({ chatSummary: '' })
          return
        }
        // 取最后一条用户消息作为摘要
        const lastUserMsg = records
          .reverse()
          .find((item) => item.role === 'user')
        if (lastUserMsg && lastUserMsg.content) {
          const summary = lastUserMsg.content.length > 50
            ? lastUserMsg.content.slice(0, 50) + '...'
            : lastUserMsg.content
          this.setData({ chatSummary: summary })
        } else {
          this.setData({ chatSummary: '' })
        }
      })
      .catch(() => {
        this.setData({ chatSummary: '' })
      })
  },

  buildQuestionnaireCoreNote(item) {
    if (!item) {
      return ''
    }
    const type = String(item.type || '').trim()
    if (CORE_NOTE_BY_TYPE[type]) {
      return CORE_NOTE_BY_TYPE[type]
    }
    const rawDesc = String(item.description || '').replace(/\s+/g, ' ').trim()
    if (!rawDesc) {
      return ''
    }
    return rawDesc.replace(/[。！？；].*$/, '')
  },

  openModule(e) {
    const { path } = e.currentTarget.dataset
    if (!path) {
      return
    }

    wx.navigateTo({
      url: path,
    })
  },

  openQuestionnaireQuickEntry(e) {
    const id = e.currentTarget.dataset.id
    if (!id) {
      return
    }

    wx.navigateTo({
      url: `/pages/quiz-detail/index?id=${id}`,
    })
  },

  noop() {},
})
