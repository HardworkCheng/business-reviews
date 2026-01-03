import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', {
    state: () => ({
        // 优先从内存读取，初始化时从 storage 读取
        token: uni.getStorageSync('token') || '',
        userInfo: uni.getStorageSync('userInfo') || null,
    }),

    getters: {
        isLoggedIn: (state) => !!state.token,
        // 使用本地 SVG 默认头像，避免外部链接
        avatar: (state) => state.userInfo?.avatar || '/static/images/default-avatar.svg',
        nickname: (state) => state.userInfo?.nickname || state.userInfo?.username || '点击登录',
    },

    actions: {
        // 登录成功后调用
        login(token, userInfo = null) {
            this.token = token
            uni.setStorageSync('token', token)
            if (userInfo) {
                this.setUserInfo(userInfo)
            }
        },

        // 更新用户信息
        setUserInfo(info) {
            this.userInfo = info
            uni.setStorageSync('userInfo', info)
        },

        // 重新拉取用户信息
        async fetchUserInfo() {
            if (!this.token) return
            try {
                const res = await getUserInfo()
                this.setUserInfo(res)
                return res
            } catch (error) {
                console.error('Failed to fetch user info:', error)
            }
        },

        // 退出登录
        logout() {
            this.token = ''
            this.userInfo = null
            uni.removeStorageSync('token')
            uni.removeStorageSync('userInfo')
        }
    }
})
