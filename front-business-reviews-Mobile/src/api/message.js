/**
 * 私信消息模块API
 */

import { get, post, del } from './request'

/**
 * 获取会话列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getConversationList = (pageNum = 1, pageSize = 20) => {
  return get('/messages/conversations', { pageNum, pageSize })
}

/**
 * 获取聊天记录
 * @param {Number} otherUserId - 对方用户ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getConversationMessages = (otherUserId, pageNum = 1, pageSize = 50) => {
  return get(`/messages/chat/${otherUserId}`, { pageNum, pageSize })
}

/**
 * 发送消息
 * @param {Object} data - 消息数据
 * @param {Number} data.receiverId - 接收者ID
 * @param {String} data.content - 消息内容
 * @param {Number} data.messageType - 消息类型：1=文本，2=图片，3=语音
 */
export const sendMessage = (data) => {
  return post('/messages/send', data)
}

/**
 * 标记消息为已读
 * @param {Number} targetUserId - 对方用户ID
 */
export const markAsRead = (targetUserId) => {
  return post(`/messages/read/${targetUserId}`)
}

/**
 * 获取未读消息总数
 */
export const getUnreadCount = () => {
  return get('/messages/unread-count')
}

/**
 * 获取通知列表
 */
export const getNotifications = (pageNum = 1, pageSize = 20, type = null) => {
  const params = { pageNum, pageSize }
  if (type !== null) {
    params.type = type
  }
  return get('/messages/notifications', params)
}

/**
 * 标记通知为已读
 */
export const markNotificationAsRead = (notificationId) => {
  return post(`/messages/notifications/${notificationId}/read`)
}

/**
 * 标记所有通知为已读
 */
export const markAllNotificationsAsRead = (type = null) => {
  const params = type !== null ? { type } : {}
  return post('/messages/notifications/read-all', params)
}

export default {
  getConversationList,
  getConversationMessages,
  sendMessage,
  markAsRead,
  getUnreadCount,
  getNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead
}
