/**
 * 用户信息模块API
 */

import { get, post, put, del } from './request'

/**
 * 获取当前用户信息
 */
export const getUserInfo = () => {
  return get('/user/info')
}

/**
 * 更新用户信息
 * @param {Object} data - 用户信息
 * @param {String} data.username - 用户名
 * @param {String} data.avatar - 头像URL
 * @param {String} data.bio - 个人简介
 * @param {Number} data.gender - 性别 (0保密, 1男, 2女)
 * @param {String} data.birthday - 生日 (YYYY-MM-DD)
 * @param {String} data.wechatOpenid - 微信OpenID
 * @param {String} data.qqOpenid - QQ OpenID
 * @param {String} data.weiboUid - 微博UID
 */
export const updateUserInfo = (data) => {
  return put('/user/info', data)
}

/**
 * 获取我的笔记列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyNotes = (pageNum = 1, pageSize = 10) => {
  return get('/user/notes', { pageNum, pageSize })
}

/**
 * 获取我的收藏列表
 * @param {Number} type - 类型 1笔记，2商家
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyFavorites = (type = 1, pageNum = 1, pageSize = 10) => {
  return get('/user/favorites', { type, pageNum, pageSize })
}

/**
 * 获取浏览历史
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getBrowseHistory = (pageNum = 1, pageSize = 20) => {
  return get('/user/history', { pageNum, pageSize })
}

/**
 * 关注用户
 * @param {String} userId - 用户ID
 */
export const followUser = (userId) => {
  return post('/user/follow', { userId })
}

/**
 * 取消关注用户
 * @param {String} userId - 用户ID
 */
export const unfollowUser = (userId) => {
  return del(`/user/follow/${userId}`)
}

/**
 * 修改密码
 * @param {Object} data - 修改密码数据
 * @param {String} data.code - 验证码
 * @param {String} data.oldPassword - 旧密码
 * @param {String} data.newPassword - 新密码
 * @param {String} data.confirmPassword - 确认新密码
 */
export const changePassword = (data) => {
  return put('/user/password', data)
}

export default {
  getUserInfo,
  updateUserInfo,
  getMyNotes,
  getMyFavorites,
  getBrowseHistory,
  followUser,
  unfollowUser,
  changePassword
}
