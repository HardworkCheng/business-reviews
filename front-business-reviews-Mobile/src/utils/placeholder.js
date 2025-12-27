/**
 * 图片URL处理工具
 * 用于处理用户头像、笔记封面等图片URL
 */

// 获取API基础URL
const getApiBaseUrl = () => {
  // H5环境下使用代理
  // #ifdef H5
  return 'http://localhost:8080'
  // #endif
  
  // 非H5环境使用完整URL
  // #ifndef H5
  return 'http://localhost:8080'
  // #endif
}

/**
 * 获取图片完整URL
 * @param {String} imagePath - 图片路径(可能是相对路径或完整URL)
 * @param {String} type - 图片类型: 'avatar'(头像) 或 'cover'(封面)
 * @returns {String} 完整的图片URL
 */
export const getImageUrl = (imagePath, type = 'avatar') => {
  // 如果没有图片路径,返回默认占位图
  if (!imagePath) {
    return getDefaultImage(type)
  }
  
  // 如果已经是完整的HTTP/HTTPS URL,直接返回
  if (imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
    return imagePath
  }
  
  // 如果是本地静态资源路径,直接返回
  if (imagePath.startsWith('/static/')) {
    return imagePath
  }
  
  // 如果是相对路径,拼接API基础URL
  const baseUrl = getApiBaseUrl()
  const cleanPath = imagePath.startsWith('/') ? imagePath : `/${imagePath}`
  
  return `${baseUrl}${cleanPath}`
}

/**
 * 获取默认占位图
 * @param {String} type - 图片类型
 * @returns {String} 默认图片路径
 */
const getDefaultImage = (type) => {
  switch (type) {
    case 'avatar':
      return '/static/icons/default-avatar.png'
    case 'cover':
      return '/static/icons/placeholder.png'
    default:
      return '/static/icons/placeholder.png'
  }
}

export default {
  getImageUrl
}
