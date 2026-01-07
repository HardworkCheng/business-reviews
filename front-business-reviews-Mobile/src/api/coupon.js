/**
 * 优惠券模块API
 */

import { get, post } from './request'

/**
 * 获取所有可用优惠券列表
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 * @param {String} keyword - 搜索关键词
 */
export const getCouponList = (pageNum = 1, pageSize = 20, keyword = '') => {
  return get('/coupons', { pageNum, pageSize, keyword }, { noAuth: true })
}

/**
 * 获取可领取的优惠券列表（领券中心）
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 * @param {String} keyword - 搜索关键词
 * @param {Number} type - 优惠券类型
 */
export const getAvailableCoupons = (pageNum = 1, pageSize = 20, keyword = '', type = null) => {
  const params = { pageNum, pageSize }
  if (keyword) params.keyword = keyword
  if (type) params.type = type
  // 添加 noAuth: true，允许未登录用户也能看到优惠券列表
  // 后端仍可通过 Token 判断已领取状态（如果有 Token）
  return get('/app/coupons/available', params, { noAuth: true })
}

/**
 * 获取我的优惠券列表
 * @param {String} status - 状态: all/unused/used/expired
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getMyCoupons = (status = 'all', pageNum = 1, pageSize = 20) => {
  return get('/app/coupons/my', { status, pageNum, pageSize })
}

/**
 * 领取优惠券
 * @param {Number} couponId - 优惠券ID
 */
export const receiveCoupon = (couponId) => {
  return post(`/app/coupons/${couponId}/claim`)
}

/**
 * 获取优惠券详情
 * @param {Number} couponId - 优惠券ID
 */
export const getCouponDetail = (couponId) => {
  return get(`/app/coupons/${couponId}`, {}, { noAuth: true })
}

/**
 * 获取秒杀活动列表
 * @param {Number} status - 活动状态: 1未开始, 2进行中, 3已结束
 */
export const getSeckillActivities = (status = 2) => {
  return get('/app/seckill/activities', { status }, { noAuth: true })
}

/**
 * 领取秒杀券
 * @param {Number} seckillId - 秒杀活动ID
 * @param {Number} couponId - 优惠券ID
 */
export const claimSeckillCoupon = (seckillId, couponId) => {
  return post(`/app/seckill/${seckillId}/coupons/${couponId}/claim`)
}

/**
 * 获取用户优惠券详情（含券码）
 * @param {Number} userCouponId - 用户优惠券ID
 */
export const getUserCouponDetail = (userCouponId) => {
  return get(`/app/coupons/my/${userCouponId}`)
}

export default {
  getCouponList,
  getAvailableCoupons,
  getMyCoupons,
  receiveCoupon,
  getCouponDetail,
  getSeckillActivities,
  claimSeckillCoupon,
  getUserCouponDetail
}
