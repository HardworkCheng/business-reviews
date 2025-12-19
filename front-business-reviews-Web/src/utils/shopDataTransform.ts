/**
 * 商家店铺数据转换工具
 * 用于处理API数据和表单数据之间的转换
 */

// API返回的店铺数据类型
export interface ApiShopData {
  id: number | string
  merchantId: number
  categoryId: number
  name: string
  headerImage: string
  image?: string  // 兼容旧字段名
  images: string
  description: string
  phone: string
  address: string
  latitude: number | string | null
  longitude: number | string | null
  businessHours: string
  averagePrice: number | string | null
  avgPrice?: number | string | null  // 兼容旧字段名
  rating: number
  tasteScore: number
  environmentScore: number
  serviceScore: number
  reviewCount: number
  popularity: number
  status: number
}

// 前端表单数据类型
export interface ShopFormData {
  id: number | null
  merchantId: number | null
  categoryId: number
  name: string
  headerImage: string
  images: string
  description: string
  phone: string
  address: string
  latitude: string
  longitude: string
  businessHours: string
  averagePrice: string
  rating: number
  tasteScore: number
  environmentScore: number
  serviceScore: number
  reviewCount: number
  popularity: number
  status: number
}

/**
 * 安全转换数字为字符串
 * @param value 要转换的值
 * @param defaultValue 默认值
 * @param precision 小数精度（可选）
 * @returns 转换后的字符串
 */
function safeNumberToString(
  value: number | string | null | undefined, 
  defaultValue: string = '', 
  precision?: number
): string {
  if (value === null || value === undefined || value === '') {
    return defaultValue
  }
  
  const numValue = typeof value === 'string' ? parseFloat(value) : value
  
  if (isNaN(numValue)) {
    return defaultValue
  }
  
  if (precision !== undefined) {
    return numValue.toFixed(precision)
  }
  
  return numValue.toString()
}

/**
 * 安全转换字符串，处理null和undefined
 * @param value 要转换的值
 * @param defaultValue 默认值
 * @returns 转换后的字符串
 */
function safeStringValue(value: string | null | undefined, defaultValue: string = ''): string {
  if (value === null || value === undefined) {
    return defaultValue
  }
  return value
}

/**
 * 将API数据转换为表单数据
 * @param apiData API返回的店铺数据
 * @returns 转换后的表单数据
 */
export function transformApiDataToForm(apiData: ApiShopData): ShopFormData {
  console.log('🔄 开始转换API数据到表单数据:', apiData)
  
  // 处理id字段，可能是字符串或数字
  const id = apiData.id ? (typeof apiData.id === 'string' ? parseInt(apiData.id, 10) : apiData.id) : null
  
  // 处理headerImage，兼容image字段
  const headerImage = safeStringValue(apiData.headerImage) || safeStringValue(apiData.image)
  
  // 处理averagePrice，兼容avgPrice字段
  const avgPrice = apiData.averagePrice ?? apiData.avgPrice
  
  const formData: ShopFormData = {
    id: id,
    merchantId: apiData.merchantId || null,
    categoryId: apiData.categoryId || 1,
    name: safeStringValue(apiData.name),
    headerImage: headerImage,
    images: safeStringValue(apiData.images),
    description: safeStringValue(apiData.description),
    phone: safeStringValue(apiData.phone),
    address: safeStringValue(apiData.address),
    latitude: safeNumberToString(apiData.latitude, '', 6),
    longitude: safeNumberToString(apiData.longitude, '', 6),
    businessHours: safeStringValue(apiData.businessHours, '09:00-22:00'),
    averagePrice: safeNumberToString(avgPrice),
    rating: apiData.rating || 0,
    tasteScore: apiData.tasteScore || 0,
    environmentScore: apiData.environmentScore || 0,
    serviceScore: apiData.serviceScore || 0,
    reviewCount: apiData.reviewCount || 0,
    popularity: apiData.popularity || 0,
    status: apiData.status ?? 1
  }
  
  console.log('✅ 数据转换完成:', formData)
  console.log('📊 转换对比:')
  console.log('  - latitude:', apiData.latitude, '→', formData.latitude)
  console.log('  - longitude:', apiData.longitude, '→', formData.longitude)
  console.log('  - averagePrice:', apiData.averagePrice, '→', formData.averagePrice)
  
  return formData
}

/**
 * 验证表单数据的完整性
 * @param formData 表单数据
 * @returns 验证结果和错误信息
 */
export function validateShopData(formData: ShopFormData): { isValid: boolean; errors: string[] } {
  const errors: string[] = []
  
  // 必填字段验证
  if (!formData.name?.trim()) {
    errors.push('店铺名称不能为空')
  }
  
  if (!formData.phone?.trim()) {
    errors.push('联系电话不能为空')
  }
  
  if (!formData.address?.trim()) {
    errors.push('店铺地址不能为空')
  }
  
  // 数据格式验证
  if (formData.latitude && formData.latitude.trim()) {
    const lat = parseFloat(formData.latitude)
    if (isNaN(lat) || lat < -90 || lat > 90) {
      errors.push('纬度格式不正确（应在-90到90之间）')
    }
  }
  
  if (formData.longitude && formData.longitude.trim()) {
    const lng = parseFloat(formData.longitude)
    if (isNaN(lng) || lng < -180 || lng > 180) {
      errors.push('经度格式不正确（应在-180到180之间）')
    }
  }
  
  if (formData.averagePrice && formData.averagePrice.trim()) {
    const price = parseFloat(formData.averagePrice)
    if (isNaN(price) || price < 0) {
      errors.push('人均消费格式不正确（应为正数）')
    }
  }
  
  return {
    isValid: errors.length === 0,
    errors
  }
}

/**
 * 比较两个表单数据对象是否相等
 * @param data1 数据对象1
 * @param data2 数据对象2
 * @returns 是否相等
 */
export function compareShopData(data1: ShopFormData, data2: ShopFormData): boolean {
  const keys = Object.keys(data1) as (keyof ShopFormData)[]
  
  for (const key of keys) {
    if (data1[key] !== data2[key]) {
      console.log(`📋 数据差异发现 - ${key}:`, data1[key], '≠', data2[key])
      return false
    }
  }
  
  return true
}

/**
 * 创建默认的表单数据
 * @returns 默认表单数据
 */
export function createDefaultShopFormData(): ShopFormData {
  return {
    id: null,
    merchantId: null,
    categoryId: 1,
    name: '',
    headerImage: '',
    images: '',
    description: '',
    phone: '',
    address: '',
    latitude: '',
    longitude: '',
    businessHours: '09:00-22:00',
    averagePrice: '',
    rating: 0,
    tasteScore: 0,
    environmentScore: 0,
    serviceScore: 0,
    reviewCount: 0,
    popularity: 0,
    status: 1
  }
}