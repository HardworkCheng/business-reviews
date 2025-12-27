import request from './request'

// CategoryVO接口定义
export interface CategoryVO {
  id: number
  name: string
  icon?: string
  color?: string
  sortOrder?: number
}

// 获取类目列表
export function getCategories() {
  return request.get<CategoryVO[]>('/api/common/categories')
}

// 获取门店列表
export function getShopList(params: any) {
  return request.get('/merchant/shops', { params })
}

// 创建门店
export function createShop(data: any) {
  return request.post('/merchant/shops', data)
}

// 更新门店
export function updateShop(id: number, data: any) {
  return request.put(`/merchant/shops/${id}`, data)
}

// 删除门店
export function deleteShop(id: number) {
  return request.delete(`/merchant/shops/${id}`)
}

// 获取门店详情
export function getShopDetail(id: number) {
  return request.get(`/merchant/shops/${id}`)
}

// 更新门店状态
export function updateShopStatus(id: number, status: number) {
  return request.put(`/merchant/shops/${id}/status`, null, { params: { status } })
}

// 获取门店统计
export function getShopStats(id: number) {
  return request.get(`/merchant/shops/${id}/stats`)
}