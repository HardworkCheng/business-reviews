/**
 * 用户认证模块API
 */

import { get, post } from './request'

/**
 * 发送验证码
 * @param {String} phone - 手机号
 * @param {Number} type - 类型 1登录，2注册，3重置密码
 */
export const sendCode = (phone, type = 1) => {
  return post('/auth/send-code', { phone, type }, { noAuth: true })
}

/**
 * 验证码登录
 * @param {String} phone - 手机号
 * @param {String} code - 验证码
 */
export const loginByCode = (phone, code) => {
  return post('/auth/login-by-code', { phone, code }, { noAuth: true })
}

/**
 * 第三方登录
 * @param {String} type - 登录类型 wechat/qq/weibo
 * @param {String} code - 授权码
 */
export const oauthLogin = (type, code) => {
  return post('/auth/oauth-login', { type, code }, { noAuth: true })
}

/**
 * 退出登录
 */
export const logout = () => {
  return post('/auth/logout')
}

export default {
  sendCode,
  loginByCode,
  oauthLogin,
  logout
}
