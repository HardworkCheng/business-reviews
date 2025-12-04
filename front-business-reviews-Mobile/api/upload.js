/**
 * 上传模块API
 */

import { upload } from './request'

/**
 * 上传单张图片
 * @param {String} filePath - 图片本地路径
 */
export const uploadImage = (filePath) => {
  return upload('/upload/image', filePath, { name: 'file' })
}

/**
 * 批量上传图片
 * @param {Array} filePaths - 图片本地路径数组
 * @returns {Promise} 返回urls数组
 */
export const uploadImages = async (filePaths) => {
  // 并发上传所有图片
  const uploadPromises = filePaths.map(filePath => uploadImage(filePath))
  const results = await Promise.all(uploadPromises)
  
  // 提取所有图片的url
  return {
    urls: results.map(result => result.url)
  }
}

export default {
  uploadImage,
  uploadImages
}
