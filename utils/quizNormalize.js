/**
 * 心理测评 API 数据归一化：兼容 camelCase / snake_case，并补充演示量表展示用字段。
 */

const DEMO_TYPES = ['PHQ9_DEMO', 'GAD7_DEMO', 'SCL90_DEMO', 'SDS_DEMO', 'SAS_DEMO']
const TITLE_BY_TYPE = {
  SCL90_DEMO: '症状自评量表（SCL-90）',
  PHQ9_DEMO: '抑郁症筛查量表（PHQ-9）',
  GAD7_DEMO: '焦虑症筛查量表（GAD-7）',
  SDS_DEMO: '抑郁自评量表（SDS）',
  SAS_DEMO: '焦虑自评量表（SAS）',
}
const DESC_BY_TYPE = {
  SCL90_DEMO:
    '90项症状自评，1-5级评分（从无到严重）。分档阈值为答辩演示呈现，非临床诊断标准。',
  PHQ9_DEMO:
    '9项抑郁症快速筛查，题目计分适配答辩演示呈现。划界阈值为答辩演示呈现，非认证 PHQ-9 标准。',
  GAD7_DEMO:
    '7项焦虑症状快速筛查，题目计分适配答辩演示呈现。划界阈值为答辩演示呈现，非临床认证 GAD-7。',
  SDS_DEMO:
    '20项抑郁自评量表，采用4级频度作答。结果用于答辩演示与自助观察，不作为临床诊断依据。',
  SAS_DEMO:
    '20项焦虑自评量表，采用4级频度作答。结果用于答辩演示与自助观察，不作为临床诊断依据。',
}

function pad2(n) {
  return n < 10 ? `0${n}` : `${n}`
}

function formatDisplayDateTime(value) {
  if (value == null) {
    return value
  }
  const raw = String(value).trim()
  if (!raw) {
    return raw
  }
  const match = raw.match(
    /^(\d{4})-(\d{2})-(\d{2})(?:[T\s](\d{2}):(\d{2})(?::(\d{2}))?)?(?:\.\d+)?(?:Z|[+-]\d{2}:?\d{2})?$/
  )
  if (match) {
    const y = match[1]
    const m = match[2]
    const d = match[3]
    const hh = pad2(Number(match[4] || 0))
    const mm = pad2(Number(match[5] || 0))
    const ss = pad2(Number(match[6] || 0))
    return `${y}年${m}月${d}日 ${hh}:${mm}:${ss}`
  }
  return raw.replace(
    /^(\d{4})-(\d{2})-(\d{2})(?:[T\s](\d{2}:\d{2}(?::\d{2})?))?$/,
    (_m, y, mon, day, time) => `${y}年${mon}月${day}日 ${time || '00:00:00'}`
  )
}

function pick(obj, camel, snake) {
  if (!obj || typeof obj !== 'object') {
    return undefined
  }
  if (Object.prototype.hasOwnProperty.call(obj, camel) && obj[camel] != null) {
    return obj[camel]
  }
  if (snake && Object.prototype.hasOwnProperty.call(obj, snake) && obj[snake] != null) {
    return obj[snake]
  }
  return obj[camel] !== undefined ? obj[camel] : snake ? obj[snake] : undefined
}

function isDemoType(type) {
  const t = (type || '').trim()
  return DEMO_TYPES.indexOf(t) >= 0
}

function normalizeQuestionnaireItem(raw) {
  if (!raw || typeof raw !== 'object') {
    return raw
  }
  const type = pick(raw, 'type', 'type') || ''
  const demo = isDemoType(type)
  const title = TITLE_BY_TYPE[type] || pick(raw, 'title', 'title')
  const rawDesc = pick(raw, 'description', 'description')
  const description =
    DESC_BY_TYPE[type] ||
    (typeof rawDesc === 'string' ? rawDesc.replace(/教学演示/g, '答辩演示') : rawDesc)
  return Object.assign({}, raw, {
    id: pick(raw, 'id', 'id'),
    title,
    description,
    cover: pick(raw, 'cover', 'cover'),
    type,
    createTime: formatDisplayDateTime(pick(raw, 'createTime', 'create_time')),
    updateTime: formatDisplayDateTime(pick(raw, 'updateTime', 'update_time')),
    quizDemoBadge: demo ? '答辩演示' : '',
    quizIsDemo: demo,
  })
}

function normalizeQuestionnaireList(list) {
  if (!Array.isArray(list)) {
    return []
  }
  return list.map(normalizeQuestionnaireItem)
}

function normalizeQuestionItem(q) {
  if (!q || typeof q !== 'object') {
    return q
  }
  const sortOrder = pick(q, 'sortOrder', 'sort_order')
  const options = Array.isArray(q.options) ? q.options : []
  const rawContent = pick(q, 'content', 'content')
  const content =
    typeof rawContent === 'string'
      ? rawContent.replace(/^\s*\d+\s*[\.、]\s*/, '')
      : rawContent
  return Object.assign({}, q, {
    id: pick(q, 'id', 'id'),
    content,
    sortOrder,
    options,
  })
}

function normalizeQuizDetail(raw) {
  if (!raw || typeof raw !== 'object') {
    return null
  }
  const type = pick(raw, 'type', 'type') || ''
  const questions = Array.isArray(raw.questions) ? raw.questions.map(normalizeQuestionItem) : []
  const demo = isDemoType(type)
  const title = TITLE_BY_TYPE[type] || pick(raw, 'title', 'title')
  const rawDesc = pick(raw, 'description', 'description')
  const description =
    DESC_BY_TYPE[type] ||
    (typeof rawDesc === 'string' ? rawDesc.replace(/教学演示/g, '答辩演示') : rawDesc)
  return Object.assign({}, raw, {
    id: pick(raw, 'id', 'id'),
    title,
    description,
    cover: pick(raw, 'cover', 'cover'),
    type,
    questions,
    quizDemoBadge: demo ? '答辩演示' : '',
    quizIsDemo: demo,
  })
}

function coerceSuggestionLines(raw) {
  const lines = pick(raw, 'suggestionLines', 'suggestion_lines')
  if (Array.isArray(lines)) {
    return lines
  }
  if (typeof lines === 'string' && lines.trim()) {
    return lines
      .split('\n')
      .map((s) => s.trim())
      .filter(Boolean)
  }
  return []
}

function buildAiChatHint(title, conclusion, scorePercent) {
  const safeTitle = title || '心理测评'
  const safeConclusion = conclusion || '待评估'
  const safePercent = Number.isFinite(Number(scorePercent)) ? Number(scorePercent) : 0
  return `我刚完成了「${safeTitle}」，结果是「${safeConclusion}」（约 ${safePercent}%）。想请你帮我一起看看接下来可以怎么调整。`
}

function normalizeQuizResult(raw) {
  if (!raw || typeof raw !== 'object') {
    return null
  }
  let ai = pick(raw, 'aiGuidance', 'ai_guidance')
  if (ai == null || String(ai).trim() === '') {
    ai = ''
  }
  const lines = coerceSuggestionLines(raw)
  const scl90Raw = pick(raw, 'scl90Report', 'scl90_report')
  const factorsRaw =
    scl90Raw && Array.isArray(pick(scl90Raw, 'factors', 'factors'))
      ? pick(scl90Raw, 'factors', 'factors')
      : []
  const scl90Report = scl90Raw
    ? {
        totalScore: pick(scl90Raw, 'totalScore', 'total_score'),
        totalAvg: pick(scl90Raw, 'totalAvg', 'total_avg'),
        positiveCount: pick(scl90Raw, 'positiveCount', 'positive_count'),
        negativeCount: pick(scl90Raw, 'negativeCount', 'negative_count'),
        positiveAvg: pick(scl90Raw, 'positiveAvg', 'positive_avg'),
        totalLevel: pick(scl90Raw, 'totalLevel', 'total_level'),
        overview: pick(scl90Raw, 'overview', 'overview'),
        factors: factorsRaw.map((f) => ({
          name: pick(f, 'name', 'name'),
          totalScore: pick(f, 'totalScore', 'total_score'),
          avgScore: pick(f, 'avgScore', 'avg_score'),
          level: pick(f, 'level', 'level'),
          reference: pick(f, 'reference', 'reference'),
          summary: pick(f, 'summary', 'summary'),
        })),
      }
    : null
  const type = pick(raw, 'questionnaireType', 'questionnaire_type') || ''
  const demo = isDemoType(type)
  const questionnaireTitle = TITLE_BY_TYPE[type] || pick(raw, 'questionnaireTitle', 'questionnaire_title')
  const scorePercent = pick(raw, 'scorePercent', 'score_percent')
  const rawAiChatHint = pick(raw, 'aiChatHint', 'ai_chat_hint')
  const aiChatHint = demo
    ? buildAiChatHint(questionnaireTitle, raw.conclusion, scorePercent)
    : (rawAiChatHint || buildAiChatHint(questionnaireTitle, raw.conclusion, scorePercent))
  return Object.assign({}, raw, {
    id: pick(raw, 'id', 'id'),
    questionnaireId: pick(raw, 'questionnaireId', 'questionnaire_id'),
    questionnaireTitle,
    questionnaireType: type,
    score: pick(raw, 'score', 'score'),
    maxScore: pick(raw, 'maxScore', 'max_score'),
    scorePercent,
    conclusion: raw.conclusion,
    interpretation: raw.interpretation,
    suggestions: raw.suggestions,
    suggestionLines: lines,
    disclaimer: raw.disclaimer,
    aiChatHint,
    aiGuidance: ai,
    scl90Report,
    createTime: formatDisplayDateTime(pick(raw, 'createTime', 'create_time')),
    quizDemoBadge: demo ? '答辩演示' : '',
    quizIsDemo: demo,
  })
}

module.exports = {
  DEMO_TYPES,
  isDemoType,
  normalizeQuestionnaireItem,
  normalizeQuestionnaireList,
  normalizeQuizDetail,
  normalizeQuizResult,
  formatDisplayDateTime,
}
