import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getLoginUser } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const isLoggedIn = ref(false)

  // 从本地存储加载用户信息
  const loadUserFromStorage = () => {
    const stored = localStorage.getItem('userInfo')
    if (stored) {
      try {
        userInfo.value = JSON.parse(stored)
        isLoggedIn.value = true
      } catch (e) {
        console.error('解析用户信息失败:', e)
        clearUser()
      }
    }
  }

  // 设置用户信息
  const setUser = (user) => {
    userInfo.value = user
    isLoggedIn.value = true
    localStorage.setItem('userInfo', JSON.stringify(user))
  }

  // 清除用户信息
  const clearUser = () => {
    userInfo.value = null
    isLoggedIn.value = false
    localStorage.removeItem('userInfo')
  }

  // 检查登录状态
  const checkLoginStatus = async () => {
    try {
      const res = await getLoginUser()
      if (res.code === 200 || res.code === 0) {
        setUser(res.data)
        return true
      }
    } catch (e) {
      clearUser()
    }
    return false
  }

  return {
    userInfo,
    isLoggedIn,
    loadUserFromStorage,
    setUser,
    clearUser,
    checkLoginStatus
  }
})

