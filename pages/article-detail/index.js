const { request, formatRequestError } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

Page({
  data: {
    loading: false,
    detail: null,
    contentParagraphs: [],
    contentOutline: [],
    relatedArticles: [],
    errorMessage: '',
  },

  onLoad(options) {
    if (!this.ensureLogin()) {
      return
    }

    const id = Number(options.id || 0)
    if (!id) {
      this.setData({
        errorMessage: '无效的文章 ID',
      })
      return
    }

    this.fetchDetail(id)
  },

  ensureLogin() {
    return ensurePageLogin()
  },

  fetchDetail(id) {
    this.setData({
      loading: true,
      errorMessage: '',
    })

    request({
      url: `/api/article/${id}`,
      method: 'GET',
    })
      .then((data) => {
        const raw = (data && data.content) || ''
        const contentParagraphs = raw
          .split(/\n+/)
          .map((line) => line.trim())
          .filter(Boolean)
        const contentOutline = contentParagraphs.slice(0, 6).map((text, index) => ({
          index,
          id: `paragraph-${index}`,
          text: text.length > 18 ? `${text.slice(0, 18)}...` : text,
        }))
        this.setData({
          detail: data,
          contentParagraphs,
          contentOutline,
        })
        this.fetchRelatedArticles(data)
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取文章详情失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  fetchRelatedArticles(detail) {
    const id = detail && detail.id
    const tag = Array.isArray(detail && detail.tags) && detail.tags.length ? detail.tags[0] : ''
    if (!id || !tag) {
      this.setData({ relatedArticles: [] })
      return
    }
    request({
      url: `/api/article/list?page=1&size=4&tag=${encodeURIComponent(tag)}`,
      method: 'GET',
    })
      .then((data) => {
        const records = ((data && data.records) || []).filter((item) => item.id !== id).slice(0, 3)
        this.setData({
          relatedArticles: records,
        })
      })
      .catch(() => {
        this.setData({ relatedArticles: [] })
      })
  },

  goParagraph(e) {
    const index = Number(e.currentTarget.dataset.index || 0)
    wx.pageScrollTo({
      selector: `#paragraph-${index}`,
      duration: 220,
    })
  },

  copyContentUrl() {
    if (!this.data.detail || !this.data.detail.contentUrl) {
      return
    }

    wx.setClipboardData({
      data: this.data.detail.contentUrl,
    })
  },

  openRelated(e) {
    const id = e.currentTarget.dataset.id
    if (!id) {
      return
    }
    wx.redirectTo({
      url: `/pages/article-detail/index?id=${id}`,
    })
  },

  goToSelfHealing() {
    wx.navigateTo({
      url: '/pages/self-healing-list/index',
    })
  },

})
