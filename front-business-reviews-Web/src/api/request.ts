import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api', // 使用vite代理
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('merchant_token')
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const { code, data, msg } = response.data
    
    if (code === 200) {
      return data
    } else if (code === 401) {
      // token过期，清除本地存储并跳转到登录页
      localStorage.removeItem('merchant_token')
      window.location.href = '/login'
      return Promise.reject(new Error(msg || '登录已过期'))
    } else {
      return Promise.reject(new Error(msg || '请求失败'))
    }
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

export default service