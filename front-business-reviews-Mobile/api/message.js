/**
 * 消息模块API
 */

import { get, post, put } from './request'

/**
 * 获取聊天列表
 */
export const getChatList = () => {
  return get('/messages/chats')
}

/**
 * 获取聊天消息
 * @param {String} sessionId - 会话ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getChatMessages = (sessionId, pageNum = 1, pageSize = 20) => {
  return get(`/messages/chats/${sessionId}`, { pageNum, pageSize })
}

/**
 * 发送消息
 * @param {String} userId - 接收用户ID
 * @param {Object} data - 消息数据
 * @param {String} data.content - 消息内容
 * @param {Number} data.messageType - 消息类型 1文本，2图片，3语音
 */
export const sendMessage = (userId, data) => {
  return post(`/messages/chats/${userId}`, data)
}

/**
 * 获取系统通知列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getNotices = (pageNum = 1, pageSize = 20) => {
  return get('/messages/notices', { pageNum, pageSize })
}

/**
 * 标记通知为已读
 * @param {Array} noticeIds - 通知ID数组，空数组表示全部已读
 */
export const markNoticesRead = (noticeIds = []) => {
  return put('/messages/notices/read', { noticeIds })
}

/**
 * 获取未读消息数
 */
export const getUnreadCount = () => {
  return get('/messages/unread-count')
}

export default {
  getChatList,
  getChatMessages,
  sendMessage,
  getNotices,
  markNoticesRead,
  getUnreadCount
}
