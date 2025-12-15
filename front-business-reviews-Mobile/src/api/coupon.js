/**
 * 优惠券模块API
 */

import { get, post } from './request'

/**
 * 获取所有可用优惠券列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getCouponList = (pageNum = 1, pageSize = 20) => {
  return get('/coupons', { pageNum, pageSize }, { noAuth: true })
}

/**
 * 获取我的优惠券列表
 * @param {String} status - 状态: all/unused/used/expired
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyCoupons = (status = 'all', pageNum = 1, pageSize = 20) => {
  return get('/coupons/my', { status, pageNum, pageSize })
}

/**
 * 领取优惠券
 * @param {Number} couponId - 优惠券ID
 */
export const receiveCoupon = (couponId) => {
  return post(`/coupons/${couponId}/receive`)
}

/**
 * 获取优惠券详情
 * @param {Number} couponId - 优惠券ID
 */
export const getCouponDetail = (couponId) => {
  return get(`/coupons/${couponId}`, {}, { noAuth: true })
}

export default {
  getCouponList,
  getMyCoupons,
  receiveCoupon,
  getCouponDetail
}
