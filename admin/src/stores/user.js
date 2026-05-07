import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getToken, setToken, removeToken } from '@/utils/auth'
import { adminLogin, getAdminInfo } from '@/api/admin'

export const useUserStore = defineStore('user', () => {
  const token = ref(getToken() || '')
  const adminInfo = ref(null)

  async function login(loginForm) {
    const res = await adminLogin(loginForm)
    const { token: newToken, admin } = res.data
    token.value = newToken
    adminInfo.value = admin
    setToken(newToken)
    return admin
  }

  async function getInfo() {
    const res = await getAdminInfo()
    adminInfo.value = res.data
    return res.data
  }

  function logout() {
    token.value = ''
    adminInfo.value = null
    removeToken()
  }

  return {
    token,
    adminInfo,
    login,
    getInfo,
    logout
  }
})
