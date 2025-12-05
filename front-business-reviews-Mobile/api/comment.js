/**
 * 评论模块API
 */

import { get, post, del } from './request'

/**
 * 获取笔记评论列表
 * @param {String} noteId - 笔记ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getNoteComments = (noteId, pageNum = 1, pageSize = 20) => {
  // 如果有token就带上，没有也可以访问
  const token = uni.getStorageSync('token')
  if (token) {
    return get(`/notes/${noteId}/comments`, { pageNum, pageSize })
  } else {
    return get(`/notes/${noteId}/comments`, { pageNum, pageSize }, { noAuth: true })
  }
}

/**
 * 发表评论
 * @param {String} noteId - 笔记ID
 * @param {Object} data - 评论数据
 * @param {String} data.content - 评论内容
 * @param {String} data.parentId - 父评论ID（回复评论时填写）
 */
export const postComment = (noteId, data) => {
  // 将noteId添加到请求数据中
  return post('/comments', {
    noteId: noteId,
    ...data
  })
}

/**
 * 点赞评论
 * @param {String} commentId - 评论ID
 */
export const likeComment = (commentId) => {
  return post(`/comments/${commentId}/like`)
}

/**
 * 取消点赞评论
 * @param {String} commentId - 评论ID
 */
export const unlikeComment = (commentId) => {
  return del(`/comments/${commentId}/like`)
}

/**
 * 删除评论
 * @param {String} commentId - 评论ID
 */
export const deleteComment = (commentId) => {
  return del(`/comments/${commentId}`)
}

export default {
  getNoteComments,
  postComment,
  likeComment,
  unlikeComment,
  deleteComment
}
