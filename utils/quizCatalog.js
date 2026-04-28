const QUESTIONNAIRE_ORDER = {
  PHQ9_DEMO: 1,
  SDS_DEMO: 2,
  GAD7_DEMO: 3,
  SAS_DEMO: 4,
  SCL90_DEMO: 5,
}

function getQuestionnaireRank(type) {
  return QUESTIONNAIRE_ORDER[String(type || '').trim()] || 999
}

function sortQuestionnaireList(list) {
  return (Array.isArray(list) ? list : []).slice().sort((a, b) => {
    const aRank = getQuestionnaireRank(a && a.type)
    const bRank = getQuestionnaireRank(b && b.type)
    if (aRank !== bRank) {
      return aRank - bRank
    }
    return Number((a && a.id) || 0) - Number((b && b.id) || 0)
  })
}

module.exports = {
  QUESTIONNAIRE_ORDER,
  getQuestionnaireRank,
  sortQuestionnaireList,
}
