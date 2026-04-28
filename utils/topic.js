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

module.exports = {
  normalizeTopicImageUrl,
  safeEncodeUrl,
  calculateTextareaRows,
  buildTextareaHeight,
}
