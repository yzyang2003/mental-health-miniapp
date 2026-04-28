const MOCK_ARTICLES = [
  {
    id: 1,
    title: '做好这几件小事，真的能减少内耗，缓解焦虑',
    source: '福州市疾病预防控制中心',
    description: '正念练习降低交感神经兴奋度，4-7-8呼吸法实操技巧。',
    url: 'https://cdc.fuzhou.gov.cn/zz/jkjy/mxfcrxjb/202510/t20251017_5218363.htm',
  },
  {
    id: 2,
    title: '认知调整、科学减压 学会温柔地与自己相处',
    source: '中国网心理中国',
    description: '正念冥想、渐进式肌肉放松、感恩日记等系统减压方法。',
    url: 'http://psy.china.com.cn/2025-11/28/content_43288328.htm',
  },
  {
    id: 3,
    title: '把情绪炼成“光”',
    source: '中国教育新闻网',
    description: '用“情绪作战地图”可视化理解并驯化自身情绪。',
    url: 'http://chinateacher.jyb.cn/zgjsb/html/2025-08/20/content_646272.htm?div=-1',
  },
  {
    id: 4,
    title: '抑郁症康复指南：日常护理与心理调适的实用策略',
    source: '全民健康网',
    description: '康复核心理念是“共处”而非“战胜”，日常护理策略。',
    url: 'https://www.bqe.net.cn/knowledge/1776.html',
  },
  {
    id: 5,
    title: '练习把自己拥入怀中',
    source: '华东师范大学心理健康教育与咨询中心',
    description: '支持性触摸、重构叙事日记等五种自我关怀日常练习。',
    url: 'http://app.why.com.cn/epublish/qnb/html/2025-06/22/content_123_34738.htm',
  },
  {
    id: 6,
    title: '允许自己崩溃：这不是脆弱！这些科学宣泄法请收好',
    source: '威海市疾病预防控制中心',
    description: '适度宣泄情绪的积极意义及科学宣泄方法，警示伪宣泄陷阱。',
    url: 'https://www.weihaicdc.cn/art/2025/9/3/art_64893_5769646.html',
  },
  {
    id: 7,
    title: '心理疗愈小锦囊｜真正好的关系，是能接住你的情绪',
    source: '中国网心理中国',
    description: '亲密关系中的情绪接纳，通过陪伴和共情缓解压力。',
    url: 'http://psy.china.com.cn/2025-09/19/content_43231907.htm',
  },
  {
    id: 8,
    title: '在“内卷”中突围，探寻生命的意义',
    source: '复旦大学',
    description: '如何在“内卷”洪流中，找到属于自己的航向，实现真正意义上的“突围”。',
    url: 'https://www.stuaff.fudan.edu.cn/8b/69/c30301a691049/page.htm',
  },
  {
    id: 9,
    title: '孤独是本能，不是错：复杂性创伤与关系重建的三条路径',
    source: 'Mind & Body Garden Psychology',
    description: '从C-PTSD角度解释孤独的来源，给出关系重建路径。',
    url: 'https://www.mindbodygarden.com/zh/blog/loneliness',
  },
  {
    id: 10,
    title: '晚上睡不着？试试这8个方法',
    source: '深圳市卫生健康委员会',
    description: '从生活习惯、心理调节等方面提供科学助眠建议。',
    url: 'https://wjw.sz.gov.cn/ztzl/jkkj/content/post_10123456.html',
  },
]

Page({
  data: {
    articleList: [],
  },

  onLoad() {
    this.setData({
      articleList: MOCK_ARTICLES,
    })
  },

  onArticleTap(e) {
    const item = e.currentTarget.dataset.item
    if (!item || !item.url) {
      wx.showToast({
        title: '链接无效，请稍后重试',
        icon: 'none',
      })
      return
    }
    this.confirmLeaveMiniProgram(item.url)
  },

  confirmLeaveMiniProgram(url) {
    wx.showModal({
      title: '提示',
      content: '即将离开小程序，是否继续？',
      confirmText: '继续',
      cancelText: '取消',
      success: (res) => {
        if (res.confirm) {
          this.goWebView(url)
        }
      },
    })
  },

  goWebView(url) {
    if (!/^https?:\/\//.test(url)) {
      wx.showToast({
        title: '链接格式不正确',
        icon: 'none',
      })
      return
    }
    wx.navigateTo({
      url: `/pages/webview/index?url=${encodeURIComponent(url)}`,
    })
  },
})
