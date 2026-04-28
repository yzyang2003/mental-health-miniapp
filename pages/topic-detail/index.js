const { request, formatRequestError } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')
const { normalizeTopicImageUrl, buildTextareaHeight, calculateTextareaRows } = require('../../utils/topic')

const TOPIC_COLLAPSE_LENGTH = 220
const REPLY_COLLAPSE_LENGTH = 120
const REPLY_INDENT_STEP_RPX = 20
const REPLY_MAX_DEPTH = 4
const THREAD_PREVIEW_COUNT = 2
const TEXTAREA_MIN_ROWS = 2
const TEXTAREA_MAX_ROWS = 8
const TEXTAREA_LINE_HEIGHT_RPX = 44
const TEXTAREA_VERTICAL_PADDING_RPX = 24
const TEXTAREA_ESTIMATED_CHARS_PER_ROW = 18

Page({
  data: {
    topicId: null,
    loading: false,
    publishing: false,
    errorMessage: '',
    successMessage: '',
    detail: null,
    replyTarget: null,
    replyPlaceholder: '',
    replyKeyboardHeight: 0,
    replyInputFocus: false,
    replyCursor: 0,
    replyTextareaHeightRpx: TEXTAREA_LINE_HEIGHT_RPX + TEXTAREA_VERTICAL_PADDING_RPX,
    activeThreadRootId: null,
    replyExpandMap: {},
    replyForm: {
      content: '',
      anonymous: true,
    },
  },

  onLoad(options) {
    if (!ensurePageLogin()) {
      return
    }

    const topicId = Number(options.topicId || 0)
    if (!topicId) {
      this.setData({
        errorMessage: '无效的帖子 ID',
      })
      return
    }

    this.setData({
      topicId,
    })
    this.fetchTopicDetail()
  },

  onPullDownRefresh() {
    this.fetchTopicDetail()
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  fetchTopicDetail() {
    if (!this.data.topicId) {
      return Promise.resolve()
    }

    this.setData({
      loading: true,
      errorMessage: '',
    })

    return request({
      url: `/api/topic/${this.data.topicId}/detail`,
      method: 'GET',
    })
      .then((data) => {
        const detail = this.buildDetailForDisplay(data || {})
        const allReplies = (detail && detail.replyAllList) || []
        const activeTarget = this.data.replyTarget
        const nextTarget = activeTarget
          ? allReplies.find((item) => item.id === activeTarget.id) || null
          : null
        let nextActiveThreadRootId = this.data.activeThreadRootId || null
        if (nextTarget && nextTarget._rootId) {
          nextActiveThreadRootId = nextTarget._rootId
        } else if (nextActiveThreadRootId) {
          const hasActiveRoot = (detail.replyThreadStats || []).some((t) => t.rootId === nextActiveThreadRootId)
          if (!hasActiveRoot) {
            nextActiveThreadRootId = null
          }
        }
        const rebuiltDetail = this.buildDetailForDisplay(data || {}, nextActiveThreadRootId)
        const rebuiltAllReplies = rebuiltDetail.replyAllList || []
        const rebuiltTarget = nextTarget
          ? rebuiltAllReplies.find((item) => item.id === nextTarget.id) || null
          : null
        this.setData({
          detail: rebuiltDetail,
          replyTarget: rebuiltTarget,
          replyPlaceholder: this.buildReplyPlaceholder(rebuiltTarget),
          activeThreadRootId: nextActiveThreadRootId,
        })
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取帖子详情失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  onReplyContentInput(e) {
    const content = e.detail.value || ''
    const replyTextareaHeightRpx = this.buildReplyTextareaHeight(content)
    this.setData({
      'replyForm.content': content,
      replyCursor: content.length,
      replyTextareaHeightRpx,
      successMessage: '',
    })
  },

  onReplyAnonymousChange(e) {
    this.setData({
      'replyForm.anonymous': !!e.detail.value,
      successMessage: '',
    })
  },

  publishReply() {
    if (!this.data.topicId) {
      return
    }
    if (this.data.publishing) {
      return
    }

    if (!this.data.replyForm.content || !this.data.replyForm.content.trim()) {
      this.setData({
        errorMessage: '请输入回复内容',
      })
      return
    }

    this.setData({
      publishing: true,
      errorMessage: '',
      successMessage: '',
    })

    request({
      url: '/api/reply/publish',
      method: 'POST',
      data: {
        topicId: this.data.topicId,
        content: this.data.replyForm.content.trim(),
        repliedReplyId: this.data.replyTarget ? this.data.replyTarget.id : null,
        anonymous: this.data.replyForm.anonymous,
      },
    })
      .then(() => {
        const app = getApp()
        app.globalData.topicListShouldRefresh = true
        this.setData({
          successMessage: '回复发布成功',
          replyTarget: null,
          replyPlaceholder: '',
          replyKeyboardHeight: 0,
          replyInputFocus: false,
          replyCursor: 0,
          replyTextareaHeightRpx: this.buildReplyTextareaHeight(''),
          replyForm: {
            content: '',
            anonymous: true,
          },
        })
        return this.fetchTopicDetail()
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '回复发布失败'),
        })
      })
      .finally(() => {
        this.setData({
          publishing: false,
        })
      })
  },

  setReplyTarget(e) {
    const reply = e.currentTarget.dataset.reply
    if (!reply || !reply.id) {
      return
    }
    this.setData({
      replyTarget: reply,
      replyPlaceholder: this.buildReplyPlaceholder(reply),
      replyKeyboardHeight: 0,
      replyInputFocus: true,
      replyCursor: (this.data.replyForm.content || '').length,
      activeThreadRootId: reply._rootId || reply.id,
      successMessage: '',
      errorMessage: '',
    })
  },

  replyTopic() {
    const topic = this.data.detail && this.data.detail.topic
    if (!topic) {
      return
    }
    this.setData({
      replyTarget: {
        id: null,
        replierName: topic.publisherName || '楼主',
      },
      replyPlaceholder: this.buildReplyPlaceholder({
        replierName: topic.publisherName || '楼主',
      }),
      replyKeyboardHeight: 0,
      replyInputFocus: true,
      replyCursor: (this.data.replyForm.content || '').length,
      successMessage: '',
      errorMessage: '',
    })
  },

  clearReplyTarget() {
    this.setData({
      replyTarget: null,
      replyPlaceholder: '',
      replyKeyboardHeight: 0,
      replyInputFocus: false,
      'replyForm.content': '',
    })
  },

  onReplyInputFocus() {
    this.setData({
      replyInputFocus: true,
      replyCursor: (this.data.replyForm.content || '').length,
    })
  },

  onReplyInputBlur() {
    this.setData({
      replyInputFocus: false,
    })
  },

  onReplyKeyboardHeightChange(e) {
    const height = (e && e.detail && e.detail.height) || 0
    this.setData({
      replyKeyboardHeight: height,
    })
  },

  toggleAnonymous() {
    if (this.data.publishing) {
      return
    }
    this.setData({
      'replyForm.anonymous': !this.data.replyForm.anonymous,
      successMessage: '',
    })
  },

  toggleReplyContent(e) {
    const replyId = Number(e.currentTarget.dataset.replyId || 0)
    if (!replyId) {
      return
    }
    const nextExpandMap = {
      ...(this.data.replyExpandMap || {}),
      [replyId]: !(this.data.replyExpandMap || {})[replyId],
    }
    this.setData({
      replyExpandMap: nextExpandMap,
      detail: this.buildDetailForDisplay(this.data.detail || {}),
    })
  },

  toggleReplyThread(e) {
    const rootId = Number(e.currentTarget.dataset.rootId || 0)
    if (!rootId) {
      return
    }
    const nextRootId = this.data.activeThreadRootId === rootId ? null : rootId
    this.setData({
      activeThreadRootId: nextRootId,
      detail: this.buildDetailForDisplay(this.data.detail || {}, nextRootId),
    })
  },

  deleteTopic() {
    if (!this.data.topicId) {
      return
    }
    wx.showModal({
      title: '删除帖子',
      content: '删除后帖子及其全部回复将被清空，确认删除吗？',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        request({
          url: `/api/topic/${this.data.topicId}`,
          method: 'DELETE',
        })
          .then(() => {
            getApp().globalData.topicListShouldRefresh = true
            wx.showToast({
              title: '帖子已删除',
              icon: 'success',
            })
            setTimeout(() => {
              wx.navigateBack({
                delta: 1,
              })
            }, 300)
          })
          .catch((error) => {
            this.setData({
              errorMessage: formatRequestError(error, '删除帖子失败'),
            })
          })
      },
    })
  },

  deleteReply(e) {
    const replyId = Number(e.currentTarget.dataset.replyId || 0)
    if (!replyId) {
      return
    }
    wx.showModal({
      title: '删除回复',
      content: '删除后该回复及其所有子回复会一并删除，确认删除吗？',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        request({
          url: `/api/reply/${replyId}`,
          method: 'DELETE',
        })
          .then(() => {
            getApp().globalData.topicListShouldRefresh = true
            if (this.data.replyTarget && this.data.replyTarget.id === replyId) {
              this.setData({
                replyTarget: null,
                replyPlaceholder: '',
                replyKeyboardHeight: 0,
                replyInputFocus: false,
                'replyForm.content': '',
              })
            }
            this.setData({
              successMessage: '回复已删除',
            })
            this.fetchTopicDetail()
          })
          .catch((error) => {
            this.setData({
              errorMessage: formatRequestError(error, '删除回复失败'),
            })
          })
      },
    })
  },

  buildDetailForDisplay(rawDetail, forceActiveRootId) {
    if (!rawDetail) {
      return rawDetail
    }
    const topic = rawDetail.topic || null
    const replies = Array.isArray(rawDetail.replies) ? rawDetail.replies : []
    const topicDisplay = topic
      ? this.decorateContent(
        {
          ...topic,
          images: Array.isArray(topic.images) ? topic.images.map((url) => this.normalizeTopicImageUrl(url)) : [],
        },
        TOPIC_COLLAPSE_LENGTH,
        true
      )
      : null
    const expandMap = this.data.replyExpandMap || {}
    const replyDisplay = replies.map((reply) => this.decorateContent(reply, REPLY_COLLAPSE_LENGTH, !!expandMap[reply.id]))
    const activeRootId = forceActiveRootId !== undefined ? forceActiveRootId : this.data.activeThreadRootId
    const threadPack = this.buildReplyThreadView(replyDisplay, activeRootId)
    return {
      ...rawDetail,
      topic: topicDisplay,
      replies: replyDisplay,
      replyViewList: threadPack.replyViewList,
      replyAllList: threadPack.replyAllList,
      replyThreadStats: threadPack.replyThreadStats,
    }
  },

  decorateContent(item, limit, expanded) {
    const rawContent = String((item && item.content) || '')
    const mentionPrefix = item && item.repliedUserName ? `@${item.repliedUserName} ` : ''
    const content = `${mentionPrefix}${rawContent}`
    const isLong = content.length > limit
    const displayContent = isLong && !expanded ? `${content.slice(0, limit)}...` : content
    return {
      ...item,
      _isLongContent: isLong,
      _expandedContent: expanded || !isLong,
      _displayContent: displayContent,
      _compactTime: this.formatCompactTime(item && item.createTime),
    }
  },

  formatCompactTime(value) {
    if (value == null || value === '') {
      return ''
    }
    const str = String(value).trim()
    const match = str.match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})/)
    if (match) {
      const [, , month, day, hour, minute] = match
      return `${month}-${day} ${hour}:${minute}`
    }
    return str.replace('T', ' ').slice(5, 16)
  },

  buildReplyPlaceholder(target) {
    if (!target || !target.replierName) {
      return '发布回复'
    }
    return '发布回复'
  },

  buildReplyTextareaHeight(content) {
    return buildTextareaHeight(content, {
      minRows: TEXTAREA_MIN_ROWS,
      maxRows: TEXTAREA_MAX_ROWS,
      estimatedCharsPerRow: TEXTAREA_ESTIMATED_CHARS_PER_ROW,
      lineHeightRpx: TEXTAREA_LINE_HEIGHT_RPX,
      verticalPaddingRpx: TEXTAREA_VERTICAL_PADDING_RPX,
    })
  },

  calculateTextareaRows(content) {
    return calculateTextareaRows(content, {
      minRows: TEXTAREA_MIN_ROWS,
      maxRows: TEXTAREA_MAX_ROWS,
      estimatedCharsPerRow: TEXTAREA_ESTIMATED_CHARS_PER_ROW,
    })
  },

  previewTopicImages(e) {
    const current = e.currentTarget.dataset.current
    const urls = e.currentTarget.dataset.urls || []
    const list = Array.isArray(urls) ? urls.filter((url) => !!url) : []
    if (!list.length) {
      return
    }
    wx.previewImage({
      current: current || list[0],
      urls: list,
    })
  },

  normalizeTopicImageUrl(rawUrl) {
    return normalizeTopicImageUrl(rawUrl)
  },

  buildReplyThreadView(replies, activeRootId) {
    const list = Array.isArray(replies) ? replies : []
    if (!list.length) {
      return {
        replyViewList: [],
        replyAllList: [],
        replyThreadStats: [],
      }
    }
    const byId = new Map()
    list.forEach((item) => {
      byId.set(item.id, item)
    })

    const childrenMap = new Map()
    const roots = []
    list.forEach((item) => {
      const parentId = item.repliedReplyId
      if (parentId && byId.has(parentId)) {
        const children = childrenMap.get(parentId) || []
        children.push(item)
        childrenMap.set(parentId, children)
        return
      }
      roots.push(item)
    })

    const ordered = []
    const visited = new Set()
    const rootReplyCountMap = new Map()
    const walk = (node, depth) => {
      if (!node || visited.has(node.id)) {
        return
      }
      visited.add(node.id)
      const safeDepth = Math.min(depth, REPLY_MAX_DEPTH)
      const rootId = depth === 0 ? node.id : (node._rootId || node.id)
      ordered.push({
        ...node,
        _rootId: rootId,
        _isRoot: depth === 0,
        _depth: safeDepth,
        _isNested: safeDepth > 0,
        _indentRpx: safeDepth * REPLY_INDENT_STEP_RPX,
      })
      if (safeDepth > 0) {
        rootReplyCountMap.set(rootId, (rootReplyCountMap.get(rootId) || 0) + 1)
      }
      const children = childrenMap.get(node.id) || []
      children.forEach((child) => {
        child._rootId = rootId
        walk(child, safeDepth + 1)
      })
    }

    roots.forEach((root) => walk(root, 0))
    list.forEach((item) => {
      if (!visited.has(item.id)) {
        walk(item, 0)
      }
    })
    const childrenByRoot = new Map()
    ordered.forEach((item) => {
      if (item._isRoot) {
        return
      }
      const rootId = item._rootId
      const arr = childrenByRoot.get(rootId) || []
      arr.push(item)
      childrenByRoot.set(rootId, arr)
    })

    const replyThreadStats = roots.map((root) => {
      const children = childrenByRoot.get(root.id) || []
      return {
        rootId: root.id,
        replyCount: children.length,
        expanded: activeRootId === root.id,
      }
    })
    const statMap = new Map(replyThreadStats.map((s) => [s.rootId, s]))

    const rootById = new Map(ordered.filter((item) => item._isRoot).map((item) => [item.id, item]))
    const replyViewList = []
    roots.forEach((root) => {
      const rootItem = rootById.get(root.id)
      if (!rootItem) {
        return
      }
      const children = childrenByRoot.get(root.id) || []
      const stat = statMap.get(root.id) || { replyCount: 0, expanded: false }
      const visibleChildren = stat.expanded ? children : children.slice(0, THREAD_PREVIEW_COUNT)
      replyViewList.push({
        ...rootItem,
        _childReplyCount: children.length,
        _visibleChildCount: visibleChildren.length,
        _threadExpanded: stat.expanded,
      })
      visibleChildren.forEach((child) => replyViewList.push(child))
    })

    return {
      replyViewList,
      replyAllList: ordered,
      replyThreadStats,
    }
  },

})
