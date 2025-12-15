import { defineStore } from 'pinia'

interface UserInfo {
  userId: string
  merchantId: string
  merchantName: string
  merchantLogo: string
  name: string
  phone: string
  avatar: string
  roleName: string
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null as UserInfo | null,
    token: localStorage.getItem('merchant_token') || ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    getUserInfo: (state) => state.userInfo
  },

  actions: {
    setToken(token: string) {
      this.token = token
      localStorage.setItem('merchant_token', token)
    },

    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo
    },

    logout() {
      this.token = ''
      this.userInfo = null
      localStorage.removeItem('merchant_token')
    }
  }
})