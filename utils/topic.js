const config = require('./config')

function getBaseUrl() {
  return (typeof config.getBaseUrl === 'function' ? config.getBaseUrl() : config.baseUrl || '').replace(/\/$/, '')
}

function safeEncodeUrl(url) {
  if (!url) {
    return ''
  }
  try {
    return encodeURI(String(url).trim())
  } catch (error) {
    return String(url).trim()
  }
}

function normalizeTopicImageUrl(rawUrl) {
  if (!rawUrl) {
    return ''
  }
  const url = String(rawUrl).trim()
  if (!url) {
    return ''
  }
  const baseUrl = getBaseUrl()
  if (!baseUrl) {
    return url
  }
  if (/^\/?assets\//i.test(url)) {
    const normalizedPath = url.startsWith('/') ? url : `/${url}`
    return safeEncodeUrl(normalizedPath)
  }
  if (/^\/?uploads\//i.test(url)) {
    const normalizedPath = url.startsWith('/') ? url : `/${url}`
    return safeEncodeUrl(`${baseUrl}${normalizedPath}`)
  }
  if (url.startsWith('/')) {
    return safeEncodeUrl(`${baseUrl}${url}`)
  }
  if (/^https?:\/\//i.test(url)) {
    const uploadPathMatch = url.match(/^https?:\/\/[^/]+(\/uploads\/.*)$/i)
    if (uploadPathMatch && uploadPathMatch[1]) {
      return safeEncodeUrl(`${baseUrl}${uploadPathMatch[1]}`)
    }
    if (/^https?:\/\/(127\.0\.0\.1|localhost)(:\d+)?\//i.test(url)) {
      const tail = url.replace(/^https?:\/\/[^/]+/i, '')
      return safeEncodeUrl(`${baseUrl}${tail}`)
    }
    return safeEncodeUrl(url)
  }
  return safeEncodeUrl(`${baseUrl}/${url.replace(/^\/+/, '')}`)
}

function calculateTextareaRows(content, options = {}) {
  const {
    minRows = 2,
    maxRows = 8,
    estimatedCharsPerRow = 18,
  } = options
  const text = String(content || '')
  if (!text.trim()) {
    return minRows
  }
  const pieces = text.split('\n')
  let rows = 0
  pieces.forEach((line) => {
    const safeLine = String(line || '')
    const estimated = Math.max(1, Math.ceil(safeLine.length / estimatedCharsPerRow))
    rows += estimated
  })
  return Math.max(minRows, Math.min(maxRows, rows))
}

function buildTextareaHeight(content, options = {}) {
  const {
    lineHeightRpx = 44,
    verticalPaddingRpx = 24,
  } = options
  const rows = calculateTextareaRows(content, options)
  return rows * lineHeightRpx + verticalPaddingRpx
}

function formatRelativeTime(dateStr) {
  if (!dateStr) return ''
  const str = String(dateStr).trim().replace('T', ' ')
  const match = str.match(/^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2})(?::(\d{2}))?/)
  if (!match) return str

  const [, year, month, day, hour, minute, second] = match
  const target = new Date(
    Number(year), Number(month) - 1, Number(day),
    Number(hour), Number(minute), Number(second || 0)
  )
  const now = new Date()
  const diffMs = now.getTime() - target.getTime()
  const diffSec = Math.floor(diffMs / 1000)

  if (diffSec < 60) return '刚刚'
  const diffMin = Math.floor(diffSec / 60)
  if (diffMin < 60) return `${diffMin}分钟前`
  const diffHour = Math.floor(diffMin / 60)
  if (diffHour < 24) return `${diffHour}小时前`
  const diffDay = Math.floor(diffHour / 24)
  if (diffDay === 1) return '昨天'
  if (diffDay === 2) return '前天'
  if (diffDay < 7) return `${diffDay}天前`

  // 超过7天显示月-日
  return `${month}-${day} ${hour}:${minute}`
}

const SENSITIVE_WORDS = [
  '自杀', '自残', '轻生', '不想活', '想死', '结束生命', '割腕',
  '跳楼', '上吊', '服毒', '活不下去', '遗书', '遗言',
  '杀人', '报复社会', '恐怖袭击', '炸弹', '枪击',
]

function checkSensitiveContent(text) {
  if (!text || typeof text !== 'string') return null
  const lower = text.trim()
  if (!lower) return null
  for (let i = 0; i < SENSITIVE_WORDS.length; i += 1) {
    const word = SENSITIVE_WORDS[i]
    if (lower.includes(word)) {
      return word
    }
  }
  return null
}

module.exports = {
  normalizeTopicImageUrl,
  safeEncodeUrl,
  calculateTextareaRows,
  buildTextareaHeight,
  formatRelativeTime,
  checkSensitiveContent,
}
