import request from './request'

// 获取数据看板数据
export function getDashboardData() {
  return request.get('/merchant/dashboard')
}

// 获取笔记分析数据
export function getNoteAnalytics(params: any) {
  return request.get('/merchant/analytics/notes', { params })
}

// 获取优惠券分析数据
export function getCouponAnalytics(params: any) {
  return request.get('/merchant/analytics/coupons', { params })
}

// 获取用户分析数据
export function getUserAnalytics(params: any) {
  return request.get('/merchant/analytics/users', { params })
}