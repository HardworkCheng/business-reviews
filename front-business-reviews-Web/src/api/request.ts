import axios from 'axios'
import type { AxiosInstance, AxiosResponse, InternalAxiosRequestConfig } from 'axios'

// 重试配置
const RETRY_CONFIG = {
  maxRetries: 3,
  retryDelay: 1000,
  retryCondition: (error: any) => {
    // 网络错误或5xx服务器错误时重试
    return !error.response || (error.response.status >= 500 && error.response.status < 600)
  }
}

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: '/api', // 使用vite代理
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8'
  }
})

// 重试函数
const retryRequest = async (config: InternalAxiosRequestConfig, retryCount = 0): Promise<any> => {
  try {
    return await service(config)
  } catch (error: any) {
    if (retryCount < RETRY_CONFIG.maxRetries && RETRY_CONFIG.retryCondition(error)) {
      console.log(`🔄 请求重试 ${retryCount + 1}/${RETRY_CONFIG.maxRetries}:`, config.url)
      await new Promise(resolve => setTimeout(resolve, RETRY_CONFIG.retryDelay * (retryCount + 1)))
      return retryRequest(config, retryCount + 1)
    }
    throw error
  }
}

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('merchant_token')
    if (token) {
      config.headers.set('Authorization', `Bearer ${token}`)
    }
    
    // 如果是FormData，删除Content-Type让浏览器自动设置
    if (config.data instanceof FormData) {
      config.headers.delete('Content-Type')
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
    
    console.log('📡 API响应:', {
      url: response.config.url,
      method: response.config.method,
      status: response.status,
      code,
      dataSize: JSON.stringify(data || {}).length
    })
    
    if (code === 200) {
      return data
    } else if (code === 401) {
      // token过期，清除本地存储并跳转到登录页
      // 但不要在登录接口返回401时清除（可能是密码错误等）
      const isLoginApi = response.config.url?.includes('/auth/login')
      if (!isLoginApi) {
        console.warn('🔐 Token过期，跳转登录页')
        localStorage.removeItem('merchant_token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(msg || '登录已过期'))
    } else {
      console.error('❌ 业务错误:', { code, msg })
      return Promise.reject(new Error(msg || '请求失败'))
    }
  },
  async (error) => {
    console.error('❌ 网络错误:', {
      message: error.message,
      status: error.response?.status,
      url: error.config?.url,
      method: error.config?.method
    })
    
    // 如果是401错误，检查是否需要清除token
    if (error.response?.status === 401) {
      const isLoginApi = error.config?.url?.includes('/auth/login')
      if (!isLoginApi) {
        console.warn('🔐 HTTP 401错误，跳转登录页')
        localStorage.removeItem('merchant_token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error('登录已过期'))
    }
    
    // 网络错误处理
    let errorMessage = '请求失败'
    
    if (error.code === 'NETWORK_ERROR' || !error.response) {
      errorMessage = '网络连接失败，请检查网络设置'
    } else if (error.code === 'ECONNABORTED') {
      errorMessage = '请求超时，请稍后重试'
    } else if (error.response) {
      const status = error.response.status
      switch (status) {
        case 400:
          errorMessage = '请求参数错误'
          break
        case 403:
          errorMessage = '没有权限访问'
          break
        case 404:
          errorMessage = '请求的资源不存在'
          break
        case 500:
          errorMessage = '服务器内部错误'
          break
        case 502:
          errorMessage = '网关错误'
          break
        case 503:
          errorMessage = '服务暂时不可用'
          break
        default:
          errorMessage = `服务器错误 (${status})`
      }
    }
    
    return Promise.reject(new Error(errorMessage))
  }
)

export default service