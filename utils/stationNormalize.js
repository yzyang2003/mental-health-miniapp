const DEFAULT_ARTICLE_TAGS = ['全部', '焦虑', '抑郁', '压力', '睡眠', '情绪管理']
const DEFAULT_MUSIC_TYPES = ['全部', '放松', '振奋', '助眠', '专注']
const DEFAULT_ISSUE_TYPES = ['全部', '失眠', '焦虑', '情绪低落', '压力', '注意力分散']

function asText(value, fallback = '') {
  if (value == null) {
    return fallback
  }
  const text = String(value).trim()
  return text || fallback
}

function toNumber(value, fallback = 0) {
  const num = Number(value)
  return Number.isFinite(num) ? num : fallback
}

function formatDuration(duration) {
  const totalSeconds = toNumber(duration, 0)
  if (totalSeconds <= 0) {
    return '未知时长'
  }
  const minute = Math.floor(totalSeconds / 60)
  const second = totalSeconds % 60
  return `${minute}分${second}秒`
}

function normalizeArticleItem(item) {
  const tags = Array.isArray(item && item.tags) ? item.tags.filter(Boolean) : []
  return {
    ...(item || {}),
    title: asText(item && item.title, '未命名文章'),
    summary: asText(item && item.summary, '暂无摘要'),
    tags,
    tag: tags[0] || asText(item && item.tag, '未分类'),
    viewCount: toNumber(item && item.viewCount, 0),
  }
}

function normalizeMusicItem(item) {
  const title = asText((item && item.title) || (item && item.songName), '未命名音乐')
  const singer = asText(item && item.singer, '未知歌手')
  return {
    ...(item || {}),
    title,
    singer,
    emotionType: asText(item && item.emotionType, '未分类'),
    duration: toNumber(item && item.duration, 0),
    durationText: formatDuration(item && item.duration),
    playCount: toNumber(item && item.playCount, 0),
  }
}

function normalizeSelfHealingItem(item) {
  const steps = Array.isArray(item && item.steps) ? item.steps : []
  return {
    ...(item || {}),
    title: asText(item && item.title, '未命名练习'),
    description: asText(item && item.description, '暂无描述'),
    issueType: asText(item && item.issueType, '未分类'),
    duration: toNumber(item && item.duration, 0),
    steps,
    stepCount: steps.length,
  }
}

function buildFilterOptions(defaultOptions, values) {
  const unique = []
  const seen = new Set()
  defaultOptions.forEach((item) => {
    const text = asText(item)
    if (!text || seen.has(text)) {
      return
    }
    seen.add(text)
    unique.push(text)
  })
  values.forEach((item) => {
    const text = asText(item)
    if (!text || seen.has(text)) {
      return
    }
    seen.add(text)
    unique.push(text)
  })
  if (!unique.length || unique[0] !== '全部') {
    return ['全部'].concat(unique.filter((item) => item !== '全部'))
  }
  return unique
}

module.exports = {
  DEFAULT_ARTICLE_TAGS,
  DEFAULT_MUSIC_TYPES,
  DEFAULT_ISSUE_TYPES,
  formatDuration,
  normalizeArticleItem,
  normalizeMusicItem,
  normalizeSelfHealingItem,
  buildFilterOptions,
}
