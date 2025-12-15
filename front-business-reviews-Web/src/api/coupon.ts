import request from './request'

// 获取优惠券列表
export function getCouponList(params: any) {
  return request.get('/merchant/coupons', { params })
}

// 创建优惠券
export function createCoupon(data: any) {
  return request.post('/merchant/coupons', data)
}

// 更新优惠券
export function updateCoupon(id: number, data: any) {
  return request.put(`/merchant/coupons/${id}`, data)
}

// 删除优惠券
export function deleteCoupon(id: number) {
  return request.delete(`/merchant/coupons/${id}`)
}

// 获取优惠券详情
export function getCouponDetail(id: number) {
  return request.get(`/merchant/coupons/${id}`)
}

// 发放优惠券
export function issueCoupon(id: number, data: any) {
  return request.post(`/merchant/coupons/${id}/issue`, data)
}

// 核销优惠券
export function redeemCoupon(id: number, data: { code: string; shopId: number }) {
  return request.post(`/merchant/coupons/${id}/redeem`, null, { params: data })
}

// 更新优惠券状态
export function updateCouponStatus(id: number, status: number) {
  return request.put(`/merchant/coupons/${id}/status`, null, { params: { status } })
}

// 获取优惠券统计
export function getCouponStats(id: number) {
  return request.get(`/merchant/coupons/${id}/stats`)
}

// 验证券码
export function verifyCoupon(code: string) {
  return request.get('/merchant/coupons/verify', { params: { code } })
}