/**
 * 笔记模块API
 */

import { get, post, del } from './request'

/**
 * 获取推荐笔记列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getRecommendedNotes = (pageNum = 1, pageSize = 10) => {
  return get('/notes/recommended', { pageNum, pageSize }, { noAuth: true })
}

/**
 * 获取用户笔记列表
 * @param {Number} userId - 用户ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getUserNotes = (userId, pageNum = 1, pageSize = 10) => {
  return get(`/notes/user/${userId}`, { pageNum, pageSize }, { noAuth: true })
}

/**
 * 获取我的笔记列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyNotes = (pageNum = 1, pageSize = 10) => {
  return get('/notes/my', { pageNum, pageSize })
}

/**
 * 获取我点赞的笔记列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyLikedNotes = (pageNum = 1, pageSize = 10) => {
  return get('/notes/liked', { pageNum, pageSize })
}

/**
 * 获取笔记详情
 * @param {String} id - 笔记ID
 */
export const getNoteDetail = (id) => {
  // 如果有token就带上，没有也可以访问
  const token = uni.getStorageSync('token')
  if (token) {
    return get(`/notes/${id}`)
  } else {
    return get(`/notes/${id}`, {}, { noAuth: true })
  }
}

/**
 * 发布笔记
 * @param {Object} data - 笔记数据
 * @param {String} data.title - 标题
 * @param {String} data.content - 内容
 * @param {Array} data.images - 图片数组
 * @param {String} data.shopId - 商家ID
 * @param {String} data.location - 位置
 * @param {Number} data.latitude - 纬度
 * @param {Number} data.longitude - 经度
 * @param {Array} data.tags - 标签数组
 * @param {Array} data.topics - 话题ID数组
 */
export const publishNote = (data) => {
  return post('/notes', data)
}

/**
 * 点赞笔记
 * @param {String} id - 笔记ID
 */
export const likeNote = (id) => {
  return post(`/notes/${id}/like`)
}

/**
 * 取消点赞笔记
 * @param {String} id - 笔记ID
 */
export const unlikeNote = (id) => {
  return del(`/notes/${id}/like`)
}

/**
 * 收藏笔记
 * @param {String} id - 笔记ID
 */
export const bookmarkNote = (id) => {
  return post(`/notes/${id}/bookmark`)
}

/**
 * 取消收藏笔记
 * @param {String} id - 笔记ID
 */
export const unbookmarkNote = (id) => {
  return del(`/notes/${id}/bookmark`)
}

/**
 * 删除笔记
 * @param {String} id - 笔记ID
 */
export const deleteNote = (id) => {
  return del(`/notes/${id}`)
}

export default {
  getRecommendedNotes,
  getUserNotes,
  getMyNotes,
  getMyLikedNotes,
  getNoteDetail,
  publishNote,
  likeNote,
  unlikeNote,
  bookmarkNote,
  unbookmarkNote,
  deleteNote
}
