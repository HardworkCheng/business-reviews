/**
 * 商家模块API
 */

import { get, post, del } from './request'

/**
 * 获取商家列表（支持多条件筛选和排序）
 * @param {Object} params - 查询参数
 * @param {String} params.category - 分类
 * @param {String} params.sortField - 排序字段 distance/popularity/rating/price
 * @param {String} params.sortOrder - 排序方向 asc/desc
 * @param {String} params.keyword - 搜索关键词
 * @param {Number} params.latitude - 纬度
 * @param {Number} params.longitude - 经度
 * @param {Number} params.pageNum - 页码
 * @param {Number} params.pageSize - 每页数量
 */
export const getShopList = (params) => {
  return get('/shops', params, { noAuth: true })
}

/**
 * 获取商家详情
 * @param {String} id - 商家ID
 */
export const getShopDetail = (id) => {
  return get(`/shops/${id}`, {}, { noAuth: true })
}

/**
 * 获取商家评价列表
 * @param {String} shopId - 商家ID
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 * @param {String} sortBy - 排序方式 latest/rating
 */
export const getShopReviews = (shopId, pageNum = 1, pageSize = 10, sortBy = 'latest') => {
  return get(`/shops/${shopId}/reviews`, { pageNum, pageSize, sortBy }, { noAuth: true })
}

/**
 * 发表商家评价
 * @param {String} shopId - 商家ID
 * @param {Object} data - 评价数据
 * @param {Number} data.rating - 总体评分
 * @param {Number} data.tasteScore - 口味评分
 * @param {Number} data.environmentScore - 环境评分
 * @param {Number} data.serviceScore - 服务评分
 * @param {String} data.content - 评价内容
 * @param {Array} data.images - 图片数组
 */
export const postShopReview = (shopId, data) => {
  return post(`/shops/${shopId}/reviews`, data)
}

/**
 * 收藏商家
 * @param {String} shopId - 商家ID
 */
export const favoriteShop = (shopId) => {
  return post(`/shops/${shopId}/favorite`)
}

/**
 * 取消收藏商家
 * @param {String} shopId - 商家ID
 */
export const unfavoriteShop = (shopId) => {
  return del(`/shops/${shopId}/favorite`)
}

export default {
  getShopList,
  getShopDetail,
  getShopReviews,
  postShopReview,
  favoriteShop,
  unfavoriteShop
}
