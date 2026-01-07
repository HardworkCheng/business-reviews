/**
 * 公共模块API
 * 包含分类、话题、地图、搜索等功能
 */

import { get } from './request'

// ==================== 分类模块 ====================

/**
 * 获取分类列表
 */
export const getCategories = () => {
  return get('/categories', {}, { noAuth: true })
}

// ==================== 话题模块 ====================

/**
 * 获取热门话题
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const getHotTopics = (pageNum = 1, pageSize = 10) => {
  return get('/topics/hot', { pageNum, pageSize }, { noAuth: true })
}

/**
 * 搜索话题
 * @param {String} keyword - 搜索关键词
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const searchTopics = (keyword, pageNum = 1, pageSize = 10) => {
  return get('/topics/search', { keyword, pageNum, pageSize }, { noAuth: true })
}

// ==================== 地图模块 ====================

/**
 * 获取附近商家
 * @param {Object} params - 查询参数
 * @param {Number} params.latitude - 纬度
 * @param {Number} params.longitude - 经度
 * @param {String} params.category - 分类（可选）
 * @param {Number} params.radius - 搜索半径，单位米（可选，默认5000）
 */
export const getNearbyShops = (params) => {
  return get('/map/nearby-shops', params, { noAuth: true })
}

/**
 * IP定位 (后端代理)
 */
export const getCityByIp = () => {
  return get('/api/common/location/ip', {}, { noAuth: true })
}

/**
 * 逆地理编码 (后端代理)
 * @param {String|Number} longitude 经度
 * @param {String|Number} latitude 纬度
 */
export const getCityByLocation = (longitude, latitude) => {
  return get('/api/common/location/regeo', { longitude, latitude }, { noAuth: true })
}

// ==================== 搜索模块 ====================

/**
 * 综合搜索
 * @param {String} keyword - 搜索关键词
 * @param {String} type - 搜索类型 all/note/shop
 * @param {Number} pageNum - 页码
 * @param {Number} pageSize - 每页数量
 */
export const search = (keyword, type = 'all', pageNum = 1, pageSize = 10) => {
  return get('/search', { keyword, type, pageNum, pageSize }, { noAuth: true })
}

/**
 * 搜索建议
 * @param {String} keyword - 搜索关键词
 */
export const getSearchSuggest = (keyword) => {
  return get('/search/suggest', { keyword }, { noAuth: true })
}

/**
 * 获取热门搜索
 */
export const getHotSearch = () => {
  return get('/search/hot', {}, { noAuth: true })
}

export default {
  // 分类
  getCategories,
  // 话题
  getHotTopics,
  searchTopics,
  // 地图
  getNearbyShops,
  getCityByIp,
  getCityByLocation,
  // 搜索
  search,
  getSearchSuggest,
  getHotSearch
}
