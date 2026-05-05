/**
 * 自我疗愈工具模块。
 * 练习数据从后端 API 拉取，本模块只保留分类映射和完成状态工具。
 */

/** 后端 issueType → 前端分类名 */
const ISSUE_TYPE_MAP = {
  '焦虑': '缓解焦虑',
  '失眠': '睡眠改善',
  '情绪低落': '改善情绪',
  '压力': '应对压力',
  '注意力分散': '专注当下',
}

const COMPLETED_MAP_KEY = 'selfHealingCompletedMap'

/** 将后端数据适配为前端格式 */
function adaptExercise(raw) {
  const durationSec = raw.duration || 0
  const minutes = Math.round(durationSec / 60)
  return {
    id: raw.id,
    title: raw.title,
    cover: raw.cover,
    shortDesc: raw.description || '',
    issueType: raw.issueType,
    categoryName: ISSUE_TYPE_MAP[raw.issueType] || raw.issueType,
    duration: durationSec,
    durationText: minutes > 0 ? `${minutes}分钟` : '',
    stepCount: Array.isArray(raw.steps) ? raw.steps.length : 0,
    steps: (raw.steps || []).map((s, i) => ({
      order: i + 1,
      title: s.title || '',
      description: s.description || '',
      duration: s.duration || 0,
    })),
  }
}

/** 从后端返回的 records 列表生成分类列表 */
function buildCategories(records) {
  const seen = new Set()
  const cats = [{ id: 0, name: '全部练习' }]
  for (const r of records) {
    const name = ISSUE_TYPE_MAP[r.issueType] || r.issueType
    if (!seen.has(name)) {
      seen.add(name)
      cats.push({ id: cats.length, name, issueType: r.issueType })
    }
  }
  return cats
}

function readCompletedMap() {
  const raw = wx.getStorageSync(COMPLETED_MAP_KEY) || {}
  return raw && typeof raw === 'object' ? raw : {}
}

function writeCompleted(id, completed) {
  const map = readCompletedMap()
  map[String(id)] = Boolean(completed)
  wx.setStorageSync(COMPLETED_MAP_KEY, map)
}

function mergeCompleted(exercises) {
  const map = readCompletedMap()
  return exercises.map((item) => ({
    ...item,
    completed: Boolean(map[String(item.id)]),
  }))
}

function filterExercises(exercises, selectedIssueType) {
  if (!selectedIssueType) return exercises
  return exercises.filter((item) => item.issueType === selectedIssueType)
}

module.exports = {
  ISSUE_TYPE_MAP,
  adaptExercise,
  buildCategories,
  readCompletedMap,
  writeCompleted,
  mergeCompleted,
  filterExercises,
}
