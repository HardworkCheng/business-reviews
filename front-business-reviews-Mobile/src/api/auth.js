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
 * 密码登录
 * @param {String} phone - 手机号
 * @param {String} password - 密码
 */
export const loginByPassword = (phone, password) => {
  return post('/auth/login-by-password', { phone, password }, { noAuth: true })
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
  loginByPassword,
  logout
}
