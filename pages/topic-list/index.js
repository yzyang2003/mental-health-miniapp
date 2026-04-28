const { request, formatRequestError, buildApiUrl } = require('../../utils/request')
const { ensurePageLogin, buildAuthorizationHeader } = require('../../utils/auth')
const { normalizeTopicImageUrl, buildTextareaHeight, calculateTextareaRows } = require('../../utils/topic')

const TOPIC_COLLAPSE_LENGTH = 140
const MAX_TOPIC_IMAGES = 9
const TOPIC_CONTENT_LIMIT = 5000
const COMPRESS_TRIGGER_SIZE = 600 * 1024
const COMPRESS_QUALITY = 70
const TEXTAREA_MIN_ROWS = 2
const TEXTAREA_MAX_ROWS = 8
const TEXTAREA_LINE_HEIGHT_RPX = 44
const TEXTAREA_VERTICAL_PADDING_RPX = 24
const TEXTAREA_ESTIMATED_CHARS_PER_ROW = 18
const EMOJI_STICKERS = [
  { id: 'user_emoji_1', src: '/assets/emoji/user-emoji-1.png' },
  { id: 'user_emoji_2', src: '/assets/emoji/user-emoji-2.png' },
  { id: 'user_emoji_3', src: '/assets/emoji/user-emoji-3.png' },
  { id: 'user_emoji_4', src: '/assets/emoji/user-emoji-4.png' },
  { id: 'user_emoji_5', src: '/assets/emoji/user-emoji-5.png' },
]

Page({
  data: {
    mineOnly: false,
    heroTitle: '树洞互助社区',
    heroDesc: '这里可以匿名分享心情、记录近况，也可以在回复中获得温和的陪伴与支持。',
    listTitle: '树洞列表',
    emptyHint: '还没有树洞帖子，快来发布第一条吧',
    loading: false,
    publishing: false,
    uploadingImages: false,
    topicList: [],
    page: 1,
    size: 10,
    pages: 0,
    total: 0,
    errorMessage: '',
    successMessage: '',
    floatingNotice: '',
    floatingNoticeLocked: false,
    topicInputFocus: false,
    topicCursor: 0,
    keyboardHeight: 0,
    emojiPickerVisible: false,
    emojiStickers: EMOJI_STICKERS,
    topicContentLength: 0,
    topicProgressPercent: 0,
    topicContentLimit: TOPIC_CONTENT_LIMIT,
    topicProgressStyle: 'background: conic-gradient(#22c55e 0deg, rgba(152, 171, 160, 0.28) 0deg 360deg);',
    topicTextareaHeightRpx: TEXTAREA_LINE_HEIGHT_RPX + TEXTAREA_VERTICAL_PADDING_RPX,
    topicForm: {
      content: '',
      images: [],
      anonymous: true,
    },
  },

  _noticeTimer: null,
  _noticeLockTimer: null,
  _topicListRequestSeq: 0,

  onLoad(options) {
    if (!ensurePageLogin()) {
      return
    }

    const mineFlag = String((options && options.mine) || '')
    const mineOnly = mineFlag === '1' || mineFlag.toLowerCase() === 'true'
    if (mineOnly) {
      wx.setNavigationBarTitle({
        title: '我的树洞发帖',
      })
      this.setData({
        mineOnly: true,
        heroTitle: '我的树洞发帖',
        heroDesc: '这里只展示你发布过的帖子，支持查看详情与删除；下拉可刷新列表。',
        listTitle: '发帖记录',
        emptyHint: '你还没有发布过树洞帖子',
      })
    }

    this.fetchTopicList(1, false)
  },

  onShow() {
    if (!ensurePageLogin()) {
      return
    }

    if (getApp().globalData.topicListShouldRefresh) {
      getApp().globalData.topicListShouldRefresh = false
      this.fetchTopicList(1, false)
    }
  },

  onUnload() {
    if (this._noticeTimer) {
      clearTimeout(this._noticeTimer)
      this._noticeTimer = null
    }
    if (this._noticeLockTimer) {
      clearTimeout(this._noticeLockTimer)
      this._noticeLockTimer = null
    }
  },

  onPullDownRefresh() {
    this.dismissFloatingNotice()
    this.fetchTopicList(1, false)
      .finally(() => {
        wx.stopPullDownRefresh()
      })
  },

  onReachBottom() {
    this.loadMoreTopics()
  },

  onPageScroll() {
    this.dismissFloatingNotice()
  },

  onTopicContentInput(e) {
    this.dismissFloatingNotice()
    const content = e.detail.value || ''
    const topicTextareaHeightRpx = this.buildTopicTextareaHeight(content)
    this.setData({
      'topicForm.content': content,
      topicCursor: content.length,
      topicContentLength: content.length,
      topicProgressPercent: this.buildTopicProgressPercent(content.length),
      topicProgressStyle: this.buildTopicProgressStyle(content.length),
      topicTextareaHeightRpx,
      successMessage: '',
      emojiPickerVisible: false,
    })
  },

  focusTopicInput() {
    if (this.data.publishing) {
      return
    }
    this.setData({
      topicInputFocus: true,
    })
  },

  onTopicInputFocus() {
    this.dismissFloatingNotice()
    this.setData({
      topicInputFocus: true,
      topicCursor: (this.data.topicForm.content || '').length,
      emojiPickerVisible: false,
    })
  },

  onTopicInputBlur() {
    this.setData({
      topicInputFocus: false,
      keyboardHeight: 0,
    })
  },

  onTopicKeyboardHeightChange(e) {
    const keyboardHeight = Number((e && e.detail && e.detail.height) || 0)
    this.setData({
      keyboardHeight: keyboardHeight > 0 ? keyboardHeight : 0,
    })
  },

  onTopicAnonymousChange(e) {
    this.dismissFloatingNotice()
    this.setData({
      'topicForm.anonymous': !!e.detail.value,
      successMessage: '',
    })
  },

  toggleAnonymous() {
    if (this.data.publishing) {
      return
    }
    this.dismissFloatingNotice()
    this.setData({
      'topicForm.anonymous': !this.data.topicForm.anonymous,
      successMessage: '',
    })
  },

  chooseTopicImages() {
    if (this.data.publishing || this.data.uploadingImages) {
      return
    }
    this.setData({
      emojiPickerVisible: false,
    })
    this.dismissFloatingNotice()
    const currentImages = (this.data.topicForm && this.data.topicForm.images) || []
    const remain = Math.max(0, MAX_TOPIC_IMAGES - currentImages.length)
    if (remain <= 0) {
      wx.showToast({
        title: `最多上传 ${MAX_TOPIC_IMAGES} 张`,
        icon: 'none',
      })
      return
    }

    wx.chooseImage({
      count: remain,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: async (res) => {
        const filePaths = (res && res.tempFilePaths) || []
        if (!filePaths.length) {
          return
        }
        this.setData({ uploadingImages: true })
        const uploadedUrls = []
        try {
          for (let i = 0; i < filePaths.length; i += 1) {
            const tempPath = filePaths[i]
            if (!tempPath) {
              continue
            }
            let fileSize = 0
            try {
              const fileInfo = wx.getFileSystemManager().statSync(tempPath)
              fileSize = Number((fileInfo && fileInfo.stats && fileInfo.stats.size) || fileInfo.size || 0)
            } catch (error) {
              fileSize = 0
            }
            // 真机拍照原图过大时，列表多图渲染容易灰块；上传前做压缩兜底。
            // eslint-disable-next-line no-await-in-loop
            const uploadPath = await this.prepareUploadPath(tempPath, fileSize)
            // eslint-disable-next-line no-await-in-loop
            const url = await this.uploadTopicImage(uploadPath)
            if (url) {
              uploadedUrls.push(url)
            }
          }
          if (uploadedUrls.length) {
            this.setData({
              'topicForm.images': currentImages.concat(uploadedUrls),
              successMessage: '',
            })
          }
        } catch (error) {
          this.setData({
            errorMessage: formatRequestError(error, '图片上传失败'),
          })
        } finally {
          this.setData({ uploadingImages: false })
        }
      },
    })
  },

  toggleEmojiPicker() {
    if (this.data.publishing || this.data.uploadingImages) {
      return
    }
    this.dismissFloatingNotice()
    this.setData({
      topicInputFocus: false,
      emojiPickerVisible: !this.data.emojiPickerVisible,
    })
  },

  appendEmojiSticker(e) {
    const stickerUrl = e.currentTarget.dataset.src
    if (!stickerUrl || this.data.publishing || this.data.uploadingImages) {
      return
    }
    const currentImages = (this.data.topicForm && this.data.topicForm.images) || []
    if (currentImages.length >= MAX_TOPIC_IMAGES) {
      wx.showToast({
        title: `最多上传 ${MAX_TOPIC_IMAGES} 张`,
        icon: 'none',
      })
      return
    }
    this.dismissFloatingNotice()
    this.setData({
      'topicForm.images': currentImages.concat(stickerUrl),
      emojiPickerVisible: false,
      successMessage: '',
    })
  },

  removeTopicImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    if (Number.isNaN(index) || index < 0) {
      return
    }
    this.dismissFloatingNotice()
    const images = ((this.data.topicForm && this.data.topicForm.images) || []).slice()
    images.splice(index, 1)
    this.setData({
      'topicForm.images': images,
      successMessage: '',
    })
  },

  previewPublishImages(e) {
    const current = e.currentTarget.dataset.current
    const urls = e.currentTarget.dataset.urls || []
    this.openImagePreview(urls, current)
  },

  previewTopicImages(e) {
    const current = e.currentTarget.dataset.current
    const urls = e.currentTarget.dataset.urls || []
    this.openImagePreview(urls, current)
  },

  openImagePreview(urls, current) {
    const list = Array.isArray(urls) ? urls.filter((url) => !!url) : []
    if (!list.length) {
      return
    }
    wx.previewImage({
      current: current || list[0],
      urls: list,
    })
  },

  uploadTopicImage(filePath) {
    return new Promise((resolve, reject) => {
      wx.uploadFile({
        url: buildApiUrl('/api/topic/upload-image'),
        filePath,
        name: 'file',
        header: buildAuthorizationHeader(),
        success: (res) => {
          if (res.statusCode < 200 || res.statusCode >= 300) {
            reject({ statusCode: res.statusCode, errMsg: 'upload failed', data: res.data })
            return
          }
          let body = res.data
          try {
            body = typeof body === 'string' ? JSON.parse(body) : body
          } catch (error) {
            // keep original body
          }
          if (body && typeof body === 'object' && Object.prototype.hasOwnProperty.call(body, 'code')) {
            if (body.code >= 200 && body.code < 300 && body.data && body.data.url) {
              resolve(this.normalizeTopicImageUrl(body.data.url))
              return
            }
            reject({ data: body, errMsg: body.message || 'upload failed' })
            return
          }
          if (body && typeof body === 'object' && body.url) {
            resolve(this.normalizeTopicImageUrl(body.url))
            return
          }
          reject({ errMsg: '上传接口返回异常' })
        },
        fail: (error) => reject(error),
      })
    })
  },

  prepareUploadPath(filePath, fileSize) {
    if (!filePath || !fileSize || fileSize < COMPRESS_TRIGGER_SIZE) {
      return Promise.resolve(filePath)
    }
    return new Promise((resolve) => {
      wx.compressImage({
        src: filePath,
        quality: COMPRESS_QUALITY,
        success: (res) => resolve((res && res.tempFilePath) || filePath),
        fail: () => resolve(filePath),
      })
    })
  },

  publishTopic() {
    if (this.data.publishing) {
      return
    }
    const topicForm = this.data.topicForm
    if (!topicForm.content || !topicForm.content.trim()) {
      this.setData({
        errorMessage: '请输入树洞内容',
      })
      return
    }

    this.setData({
      publishing: true,
      errorMessage: '',
      successMessage: '',
    })

    request({
      url: '/api/topic/publish',
      method: 'POST',
      data: {
        content: topicForm.content.trim(),
        images: topicForm.images || [],
        anonymous: topicForm.anonymous,
      },
    })
      .then(() => {
        this.setData({
          successMessage: '',
          topicForm: {
            content: '',
            images: [],
            anonymous: true,
          },
          topicInputFocus: false,
          topicCursor: 0,
          emojiPickerVisible: false,
          topicContentLength: 0,
          topicProgressPercent: 0,
          topicProgressStyle: this.buildTopicProgressStyle(0),
          topicTextareaHeightRpx: this.buildTopicTextareaHeight(''),
        })
        this.showFloatingNotice('树洞发布成功了', { lockForDuration: true, durationMs: 2000 })
        return this.fetchTopicList(1, false)
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '树洞发布失败'),
        })
      })
      .finally(() => {
        this.setData({
          publishing: false,
        })
      })
  },

  fetchTopicList(page = 1, append = false) {
    const requestId = (this._topicListRequestSeq || 0) + 1
    this._topicListRequestSeq = requestId
    this.setData({
      loading: true,
      errorMessage: '',
    })

    const mineQuery = this.data.mineOnly ? '&mine=true' : ''
    return request({
      url: `/api/topic/list?page=${page}&size=${this.data.size}${mineQuery}`,
      method: 'GET',
    })
      .then((data) => {
        if (requestId !== this._topicListRequestSeq) {
          return
        }
        const records = (data && data.records) || []
        const mergedRecords = append ? this.data.topicList.concat(records) : records
        const topicList = this.buildTopicListForDisplay(mergedRecords)
        this.setData({
          topicList,
          page: (data && data.current) || page,
          pages: (data && data.pages) || 0,
          total: (data && data.total) || 0,
        })
      })
      .catch((error) => {
        if (requestId !== this._topicListRequestSeq) {
          return
        }
        this.setData({
          errorMessage: formatRequestError(error, '获取树洞列表失败'),
        })
      })
      .finally(() => {
        if (requestId !== this._topicListRequestSeq) {
          return
        }
        this.setData({
          loading: false,
        })
      })
  },

  loadMoreTopics() {
    if (this.data.loading) {
      return
    }
    const nextPage = this.data.page + 1
    if (this.data.pages && nextPage > this.data.pages) {
      this.showFloatingNotice('已经到底啦', { lockMs: 500 })
      return
    }

    this.fetchTopicList(nextPage, true)
  },

  openCommunityTopicList() {
    wx.redirectTo({
      url: '/pages/topic-list/index',
    })
  },

  openTopicDetail(e) {
    const topicId = e.currentTarget.dataset.topicId
    if (!topicId) {
      return
    }

    wx.navigateTo({
      url: `/pages/topic-detail/index?topicId=${topicId}`,
    })
  },

  noop() {},

  deleteTopic(e) {
    const topicId = Number(e.currentTarget.dataset.topicId || 0)
    if (!topicId) {
      return
    }
    wx.showModal({
      title: '删除帖子',
      content: '删除后帖子及其全部回复将被清空，确认删除吗？',
      success: (res) => {
        if (!res.confirm) {
          return
        }
        this.doDeleteTopic(topicId)
      },
    })
  },

  doDeleteTopic(topicId) {
    request({
      url: `/api/topic/${topicId}`,
      method: 'DELETE',
    })
      .then(() => {
        this.setData({
          successMessage: '帖子已删除',
        })
        this.fetchTopicList(1, false)
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '删除帖子失败'),
        })
      })
  },

  buildTopicListForDisplay(records) {
    return (records || []).map((item) => {
      const content = String((item && item.content) || '')
      const isLong = content.length > TOPIC_COLLAPSE_LENGTH
      const images = this.parseImageList(item && item.images).map((url) => this.normalizeTopicImageUrl(url))
      return {
        ...item,
        images,
        createTime: this.formatDisplayDateTime(item && item.createTime),
        _isLongContent: isLong,
        _displayContent: isLong ? `${content.slice(0, TOPIC_COLLAPSE_LENGTH)}...` : content,
      }
    })
  },

  parseImageList(rawImages) {
    if (Array.isArray(rawImages)) {
      return rawImages
        .map((url) => this.cleanImageToken(url))
        .filter((url) => !!url)
    }
    if (typeof rawImages === 'string') {
      const text = rawImages.trim()
      if (!text) {
        return []
      }
      // 兼容后端偶发返回 JSON 字符串。
      if ((text.startsWith('[') && text.endsWith(']')) || (text.startsWith('"') && text.endsWith('"'))) {
        try {
          const parsed = JSON.parse(text)
          if (Array.isArray(parsed)) {
            return parsed
              .map((url) => this.cleanImageToken(url))
              .filter((url) => !!url)
          }
          if (typeof parsed === 'string' && parsed.trim()) {
            const one = this.cleanImageToken(parsed)
            return one ? [one] : []
          }
        } catch (error) {
          // ignore and fallback split
        }
      }
      // 兼容后端偶发返回 Java List.toString(): [url1, url2]
      const unwrapped = text.startsWith('[') && text.endsWith(']')
        ? text.slice(1, -1)
        : text
      if (unwrapped.includes(',')) {
        return unwrapped
          .split(',')
          .map((item) => this.cleanImageToken(item))
          .filter((item) => !!item)
      }
      const one = this.cleanImageToken(unwrapped)
      return one ? [one] : []
    }
    return []
  },

  cleanImageToken(value) {
    if (value == null) {
      return ''
    }
    let token = String(value).trim()
    if (!token) {
      return ''
    }
    // 去掉字符串两端的引号和残留中括号
    token = token.replace(/^['"\[]+/, '').replace(/['"\]]+$/, '').trim()
    return token
  },

  normalizeTopicImageUrl(rawUrl) {
    return normalizeTopicImageUrl(rawUrl)
  },

  buildTopicProgressStyle(length) {
    const safeLength = Math.max(0, Number(length || 0))
    const ratio = Math.min(1, safeLength / TOPIC_CONTENT_LIMIT)
    const degree = Math.round(ratio * 360)
    let color = '#22c55e'
    if (ratio >= 0.9) {
      color = '#ef4444'
    } else if (ratio >= 0.75) {
      color = '#f59e0b'
    }
    return `background: conic-gradient(${color} 0deg ${degree}deg, rgba(152, 171, 160, 0.28) ${degree}deg 360deg);`
  },

  buildTopicProgressPercent(length) {
    const safeLength = Math.max(0, Number(length || 0))
    const ratio = Math.min(1, safeLength / TOPIC_CONTENT_LIMIT)
    return Math.round(ratio * 100)
  },

  showFloatingNotice(message, options = {}) {
    if (!message) {
      return
    }
    const lockForDuration = !!options.lockForDuration
    const durationMs = Math.max(300, Number(options.durationMs || 3000))
    const lockMs = Math.max(0, Number(options.lockMs || 0))
    if (this._noticeTimer) {
      clearTimeout(this._noticeTimer)
      this._noticeTimer = null
    }
    if (this._noticeLockTimer) {
      clearTimeout(this._noticeLockTimer)
      this._noticeLockTimer = null
    }
    const shouldLock = lockForDuration || lockMs > 0
    this.setData({
      floatingNotice: message,
      floatingNoticeLocked: shouldLock,
    })
    if (!lockForDuration && lockMs > 0) {
      this._noticeLockTimer = setTimeout(() => {
        this._noticeLockTimer = null
        if (this.data.floatingNotice) {
          this.setData({
            floatingNoticeLocked: false,
          })
        }
      }, lockMs)
    }
    this._noticeTimer = setTimeout(() => {
      this._noticeTimer = null
      if (this._noticeLockTimer) {
        clearTimeout(this._noticeLockTimer)
        this._noticeLockTimer = null
      }
      this.setData({
        floatingNotice: '',
        floatingNoticeLocked: false,
      })
    }, durationMs)
  },

  dismissFloatingNotice() {
    if (this.data.floatingNoticeLocked) {
      return
    }
    if (this._noticeTimer) {
      clearTimeout(this._noticeTimer)
      this._noticeTimer = null
    }
    if (this.data.floatingNotice) {
      this.setData({
        floatingNotice: '',
      })
    }
  },

  buildTopicTextareaHeight(content) {
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

  formatDisplayDateTime(value) {
    if (value == null || value === '') {
      return ''
    }
    const text = String(value).trim().replace('T', ' ')
    const match = text.match(/^(\d{4})-(\d{2})-(\d{2})\s+(\d{2}):(\d{2})(?::(\d{2}))?/)
    if (match) {
      const [, year, month, day, hour, minute, second] = match
      return `${year}年${month}月${day}日 ${hour}:${minute}:${second || '00'}`
    }
    return text
  },

})
