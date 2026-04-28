const { request } = require('../../utils/request')
const { normalizeQuestionnaireList } = require('../../utils/quizNormalize')
const { ensurePageLogin } = require('../../utils/auth')
const { sortQuestionnaireList } = require('../../utils/quizCatalog')

const CORE_NOTE_BY_TYPE = {
  SCL90_DEMO: '9维度综合心理症状自评量表，适合个体近期整体心理状态评估自查',
  PHQ9_DEMO: '9条目抑郁风险快筛，评估近两周个体情绪状态，适合抑郁情绪自查',
  GAD7_DEMO: '7条目焦虑风险快筛，评估近两周个体情绪状态，适合焦虑情绪自查',
  SDS_DEMO: '20条目抑郁自评量表，适合个体近期抑郁症状筛查',
  SAS_DEMO: '20条目焦虑自评量表，适合个体近期焦虑症状筛查',
}
Page({
  data: {
    modules: [
      {
        key: 'quiz',
        title: '心理测评',
        desc: '选择量表完成测评，系统会给出分值、结论和建议。',
        path: '/pages/quiz-list/index',
      },
      {
        key: 'chat',
        title: 'AI 咨询师“小爱”',
        desc: '和小爱进行文字对话，支持查看最近聊天记录。',
        path: '/pages/ai-chat/index',
      },
    ],
    questionnaireQuickList: [],
  },

  onLoad() {
    if (!this.ensureLogin()) {
      return
    }
    this.fetchQuestionnaireQuickList()
  },

  onShow() {
    if (!this.ensureLogin()) {
      return
    }
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchQuestionnaireQuickList() {
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
        })
      })
      .catch(() => {
        this.setData({
          questionnaireQuickList: [],
        })
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
