import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '@/utils/auth'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login/index.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout/index.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard/index.vue'),
        meta: { title: '数据概览' }
      },
      {
        path: 'article',
        name: 'ArticleList',
        component: () => import('@/views/Article/List.vue'),
        meta: { title: '文章管理' }
      },
      {
        path: 'article/edit/:id?',
        name: 'ArticleEdit',
        component: () => import('@/views/Article/Edit.vue'),
        meta: { title: '文章编辑' }
      },
      {
        path: 'music',
        name: 'MusicList',
        component: () => import('@/views/Music/List.vue'),
        meta: { title: '音乐管理' }
      },
      {
        path: 'healing',
        name: 'HealingList',
        component: () => import('@/views/Healing/List.vue'),
        meta: { title: '自愈练习管理' }
      },
      {
        path: 'topic',
        name: 'TopicList',
        component: () => import('@/views/Topic/List.vue'),
        meta: { title: '树洞审核' }
      },
      {
        path: 'quiz',
        name: 'QuizList',
        component: () => import('@/views/Quiz/List.vue'),
        meta: { title: '问卷管理' }
      },
      {
        path: 'quiz/:id/questions',
        name: 'QuizQuestions',
        component: () => import('@/views/Quiz/Questions.vue'),
        meta: { title: '题目编辑' }
      },
      {
        path: 'user',
        name: 'UserList',
        component: () => import('@/views/User/List.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'chat',
        name: 'ChatList',
        component: () => import('@/views/Chat/List.vue'),
        meta: { title: '对话记录' }
      },
      {
        path: 'notice',
        name: 'NoticeList',
        component: () => import('@/views/Notice/List.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'ai-config',
        name: 'AiConfig',
        component: () => import('@/views/AiConfig/index.vue'),
        meta: { title: 'AI配置' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 登录守卫
router.beforeEach((to, from, next) => {
  const token = getToken()
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path === '/login' && token) {
    next('/dashboard')
  } else {
    next()
  }
})

// 设置页面标题
router.afterEach((to) => {
  document.title = to.meta.title ? `${to.meta.title} - 校园心理健康管理系统` : '校园心理健康管理系统'
})

export default router
