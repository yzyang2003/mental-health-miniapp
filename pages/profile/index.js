const { request, formatRequestError } = require('../../utils/request')
const { ensurePageLogin } = require('../../utils/auth')

const defaultAvatarUrl = 'https://mmbiz.qpic.cn/mmbiz/icTdbqWNOwNRna42FI242Lcia07jQodd2FJGIYQfG0LAJGFxM4FbnQP6yfMxBgJ0F3YRqJCJ1aPAK2dQagdusBZg/0'

Page({
  data: {
    loading: false,
    userInfo: null,
    errorMessage: '',
    successMessage: '',
    showProfileEdit: false,
    showAccountPanel: false,
    featureCards: [
      {
        key: 'notice',
        title: '公告',
        shortDesc: '通知与更新',
        path: '/pages/notice-center/index',
        tone: 'mint',
        iconText: '铃',
      },
      {
        key: 'quizHistory',
        title: '测评历史',
        shortDesc: '做过的测评',
        path: '/pages/my-quiz-history/index',
        tone: 'sage',
        iconText: '测',
      },
      {
        key: 'chat',
        title: '小爱',
        shortDesc: 'AI 对话',
        path: '/pages/ai-chat/index',
        tone: 'sky',
        iconText: 'AI',
      },
      {
        key: 'treeHoleMine',
        title: '树洞发帖',
        shortDesc: '我的历史帖子',
        path: '/pages/topic-list/index?mine=1',
        tone: 'teal',
        iconText: '洞',
      },
    ],
    profileForm: {
      nickName: '',
      avatarUrl: defaultAvatarUrl,
      region: ['', '', ''],
      gender: '0',
      language: 'zh_CN',
    },
  },

  onShow() {
    if (!ensurePageLogin()) {
      return
    }
    this.fetchUserInfo()
  },

  fetchUserInfo() {
    this.setData({
      loading: true,
      errorMessage: '',
    })

    return request({
      url: '/api/user/info',
      method: 'GET',
    })
      .then((data) => {
        this.setData({
          userInfo: data,
        })
        getApp().globalData.userInfo = data
        this.syncProfileForm(data)
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '获取用户信息失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  onChooseAvatar(e) {
    const { avatarUrl } = e.detail
    this.setData({
      'profileForm.avatarUrl': avatarUrl || defaultAvatarUrl,
      successMessage: '',
    })
  },

  onNicknameInput(e) {
    this.setData({
      'profileForm.nickName': e.detail.value || '',
      successMessage: '',
    })
  },

  onRegionChange(e) {
    this.setData({
      'profileForm.region': e.detail.value || ['', '', ''],
      successMessage: '',
    })
  },

  onGenderChange(e) {
    this.setData({
      'profileForm.gender': e.detail.value || '0',
      successMessage: '',
    })
  },

  saveProfile() {
    const profileForm = this.data.profileForm
    const [province, city, country] = profileForm.region || ['', '', '']

    this.setData({
      loading: true,
      errorMessage: '',
      successMessage: '',
    })

    request({
      url: '/api/user/profile',
      method: 'POST',
      data: {
        nickName: profileForm.nickName,
        avatarUrl: profileForm.avatarUrl === defaultAvatarUrl ? '' : profileForm.avatarUrl,
        province,
        city,
        country,
        gender: Number(profileForm.gender || 0),
        language: profileForm.language || 'zh_CN',
      },
    })
      .then((data) => {
        this.setData({
          userInfo: data,
          successMessage: '用户资料保存成功',
        })
        getApp().globalData.userInfo = data
        this.syncProfileForm(data)
      })
      .catch((error) => {
        this.setData({
          errorMessage: formatRequestError(error, '保存用户资料失败'),
        })
      })
      .finally(() => {
        this.setData({
          loading: false,
        })
      })
  },

  handleLogout() {
    const app = getApp()
    app.globalData.token = ''
    app.globalData.openid = ''
    app.globalData.userInfo = null
    wx.removeStorageSync('token')
    wx.removeStorageSync('openid')
    wx.reLaunch({
      url: '/pages/login/index',
    })
  },

  openFeature(e) {
    const { path } = e.currentTarget.dataset
    if (!path) {
      return
    }

    wx.navigateTo({
      url: path,
    })
  },

  toggleProfileEdit() {
    this.setData({
      showProfileEdit: !this.data.showProfileEdit,
    })
  },

  toggleAccountPanel() {
    this.setData({
      showAccountPanel: !this.data.showAccountPanel,
    })
  },

  syncProfileForm(userInfo) {
    if (!userInfo) {
      return
    }

    this.setData({
      profileForm: {
        nickName: userInfo.nickName || '',
        avatarUrl: userInfo.avatarUrl || defaultAvatarUrl,
        region: [
          userInfo.province || '',
          userInfo.city || '',
          userInfo.country || '',
        ],
        gender: String(userInfo.gender == null ? 0 : userInfo.gender),
        language: userInfo.language || 'zh_CN',
      },
    })
  },

})
