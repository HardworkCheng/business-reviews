/**
 * 统一请求封装
 * 基于uni.request进行二次封装
 */

// API基础地址配置
const BASE_URL = {
  development: 'http://localhost:8080/api',
  production: 'https://api.business-reviews.com/api'
}

// 获取当前环境的基础URL
const getBaseUrl = () => {
  // 可以根据环境变量或其他方式切换
  // 目前默认使用开发环境
  return BASE_URL.development
}

/**
 * 统一请求方法
 * @param {Object} options - 请求配置
 * @param {String} options.url - 请求地址
 * @param {String} options.method - 请求方法
 * @param {Object} options.data - 请求数据
 * @param {Object} options.header - 请求头
 * @param {Boolean} options.noAuth - 是否不需要token
 */
export const request = (options) => {
  return new Promise((resolve, reject) => {
    // 获取token
    const token = uni.getStorageSync('token') || ''
    console.log('Request - URL:', options.url, ', Token:', token ? token.substring(0, 20) + '...' : '无')
    
    // 设置请求头
    const header = {
      'Content-Type': 'application/json',
      ...options.header
    }
    
    // 需要token的接口自动添加Authorization
    if (token && !options.noAuth) {
      header['Authorization'] = `Bearer ${token}`
      console.log('Request - Authorization header added')
    }
    
    // 发起请求
    uni.request({
      url: getBaseUrl() + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: header,
      success: (res) => {
        const data = res.data
        console.log('Response - URL:', options.url, ', Code:', data.code, ', Message:', data.message)
        
        // 请求成功
        if (data.code === 200) {
          resolve(data.data)
        } 
        // Token过期或未登录
        else if (data.code === 401) {
          uni.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none',
            duration: 1500
          })
          
          // 清除token
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          
          // 跳转到登录页
          setTimeout(() => {
            uni.reLaunch({
              url: '/pages/login/login'
            })
          }, 1500)
          
          reject(data)
        }
        // 其他错误
        else {
          uni.showToast({
            title: data.message || '请求失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '网络请求失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

/**
 * GET请求
 */
export const get = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'GET',
    data,
    ...options
  })
}

/**
 * POST请求
 */
export const post = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'POST',
    data,
    ...options
  })
}

/**
 * PUT请求
 */
export const put = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'PUT',
    data,
    ...options
  })
}

/**
 * DELETE请求
 */
export const del = (url, data = {}, options = {}) => {
  return request({
    url,
    method: 'DELETE',
    data,
    ...options
  })
}

/**
 * 文件上传
 */
export const upload = (url, filePath, options = {}) => {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token') || ''
    
    const header = {
      ...options.header
    }
    
    if (token) {
      header['Authorization'] = `Bearer ${token}`
    }
    
    uni.uploadFile({
      url: getBaseUrl() + url,
      filePath: filePath,
      name: options.name || 'file',
      header: header,
      success: (res) => {
        const data = JSON.parse(res.data)
        
        if (data.code === 200) {
          resolve(data.data)
        } else {
          uni.showToast({
            title: data.message || '上传失败',
            icon: 'none'
          })
          reject(data)
        }
      },
      fail: (err) => {
        uni.showToast({
          title: '上传失败',
          icon: 'none'
        })
        reject(err)
      }
    })
  })
}

export default {
  request,
  get,
  post,
  put,
  del,
  upload,
  getBaseUrl
}
