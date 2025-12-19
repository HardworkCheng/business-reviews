import request from '@/api/request'

/**
 * 上传服务接口
 */
export interface UploadService {
  uploadSingleFile(file: File, folder?: string): Promise<string>
  uploadMultipleFiles(files: File[], folder?: string): Promise<string[]>
}

/**
 * 上传响应接口
 */
interface UploadResponse {
  code: number
  data: {
    url: string
  }
  msg?: string
}

/**
 * 上传结果接口
 */
export interface UploadResult {
  success: boolean
  url?: string
  error?: string
}

/**
 * 文件验证配置
 */
const VALIDATION_CONFIG = {
  maxFileSize: 10 * 1024 * 1024, // 10MB
  allowedTypes: ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp', 'image/bmp'],
  allowedExtensions: ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp']
}

/**
 * 验证文件
 */
function validateFile(file: File): { valid: boolean; error?: string } {
  console.log('验证文件:', file.name, file.size, file.type)
  
  // 检查文件是否存在
  if (!file) {
    return { valid: false, error: '请选择文件' }
  }
  
  // 检查文件大小
  if (file.size > VALIDATION_CONFIG.maxFileSize) {
    const sizeMB = (VALIDATION_CONFIG.maxFileSize / (1024 * 1024)).toFixed(0)
    return { valid: false, error: `文件大小不能超过${sizeMB}MB` }
  }
  
  // 检查文件类型
  if (!VALIDATION_CONFIG.allowedTypes.includes(file.type)) {
    return { valid: false, error: '仅支持JPG、PNG、GIF、WEBP、BMP格式的图片' }
  }
  
  // 检查文件扩展名
  const fileName = file.name.toLowerCase()
  const hasValidExtension = VALIDATION_CONFIG.allowedExtensions.some(ext => fileName.endsWith(ext))
  if (!hasValidExtension) {
    return { valid: false, error: '文件扩展名不支持，仅支持JPG、PNG、GIF、WEBP、BMP格式' }
  }
  
  console.log('文件验证通过:', file.name)
  return { valid: true }
}

/**
 * 上传单个文件
 */
export async function uploadSingleFile(file: File, folder: string = 'merchant'): Promise<string> {
  console.log('开始上传单个文件:', file.name, '文件夹:', folder)
  
  // 验证文件
  const validation = validateFile(file)
  if (!validation.valid) {
    console.error('文件验证失败:', validation.error)
    throw new Error(validation.error)
  }
  
  // 创建FormData
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  console.log('FormData内容:', Array.from(formData.entries()))
  
  try {
    console.log('发送上传请求...')
    console.log('当前token:', localStorage.getItem('merchant_token'))
    
    // 使用axios发送请求，让浏览器自动设置Content-Type以包含boundary
    const response = await request.post('/merchant/upload', formData, {
      timeout: 30000 // 30秒超时
    })
    
    console.log('上传响应:', response)
    
    // 检查响应格式 - response已经被axios拦截器处理过，直接是data部分
    if (!response || !response.url) {
      console.error('响应格式错误:', response)
      throw new Error('服务器响应格式错误，未返回文件URL')
    }
    
    const fileUrl = response.url
    console.log('文件上传成功，URL:', fileUrl)
    
    // 验证URL格式
    if (!fileUrl.startsWith('http')) {
      console.error('返回的URL格式无效:', fileUrl)
      throw new Error('服务器返回的文件URL格式无效')
    }
    
    return fileUrl
    
  } catch (error: any) {
    console.error('文件上传失败:', error)
    
    // 处理不同类型的错误
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      throw new Error('上传超时，请检查网络连接后重试')
    } else if (error.response?.status === 401) {
      throw new Error('登录已过期，请重新登录')
    } else if (error.response?.status === 413) {
      throw new Error('文件过大，服务器拒绝处理')
    } else if (error.response?.status >= 500) {
      throw new Error('服务器内部错误，请稍后重试')
    } else if (error.message) {
      throw new Error(error.message)
    } else {
      throw new Error('上传失败，请稍后重试')
    }
  }
}

/**
 * 上传多个文件
 */
export async function uploadMultipleFiles(files: File[], folder: string = 'merchant'): Promise<string[]> {
  console.log('开始批量上传文件:', files.length, '个文件，文件夹:', folder)
  
  if (!files || files.length === 0) {
    throw new Error('请选择要上传的文件')
  }
  
  // 验证所有文件
  for (let i = 0; i < files.length; i++) {
    const validation = validateFile(files[i])
    if (!validation.valid) {
      throw new Error(`文件 "${files[i].name}" ${validation.error}`)
    }
  }
  
  const uploadPromises: Promise<string>[] = []
  const results: string[] = []
  
  // 并发上传所有文件
  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    console.log(`准备上传第${i + 1}个文件:`, file.name)
    
    const uploadPromise = uploadSingleFile(file, folder)
      .then(url => {
        console.log(`第${i + 1}个文件上传成功:`, url)
        return url
      })
      .catch(error => {
        console.error(`第${i + 1}个文件上传失败:`, error)
        throw new Error(`文件 "${file.name}" 上传失败: ${error.message}`)
      })
    
    uploadPromises.push(uploadPromise)
  }
  
  try {
    // 等待所有上传完成
    const urls = await Promise.all(uploadPromises)
    console.log('所有文件上传完成:', urls)
    return urls
  } catch (error: any) {
    console.error('批量上传失败:', error)
    throw error
  }
}

/**
 * 公开上传单个文件（无需登录，用于商家入驻注册时上传图片）
 * 仅允许上传到 merchant/logo, merchant/avatar, merchant/license 目录
 */
export async function uploadPublicFile(file: File, folder: string = 'merchant/register'): Promise<string> {
  console.log('开始公开上传文件:', file.name, '文件夹:', folder)
  
  // 验证文件
  const validation = validateFile(file)
  if (!validation.valid) {
    console.error('文件验证失败:', validation.error)
    throw new Error(validation.error)
  }
  
  // 创建FormData
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', folder)
  
  try {
    console.log('发送公开上传请求...')
    
    // 使用公开上传接口，无需token
    const response = await request.post('/merchant/upload/public', formData, {
      timeout: 30000 // 30秒超时
    })
    
    console.log('公开上传响应:', response)
    
    // 检查响应格式
    if (!response || !response.url) {
      console.error('响应格式错误:', response)
      throw new Error('服务器响应格式错误，未返回文件URL')
    }
    
    const fileUrl = response.url
    console.log('公开上传成功，URL:', fileUrl)
    
    // 验证URL格式
    if (!fileUrl.startsWith('http')) {
      console.error('返回的URL格式无效:', fileUrl)
      throw new Error('服务器返回的文件URL格式无效')
    }
    
    return fileUrl
    
  } catch (error: any) {
    console.error('公开文件上传失败:', error)
    
    if (error.code === 'ECONNABORTED' || error.message?.includes('timeout')) {
      throw new Error('上传超时，请检查网络连接后重试')
    } else if (error.response?.status === 413) {
      throw new Error('文件过大，服务器拒绝处理')
    } else if (error.response?.status >= 500) {
      throw new Error('服务器内部错误，请稍后重试')
    } else if (error.message) {
      throw new Error(error.message)
    } else {
      throw new Error('上传失败，请稍后重试')
    }
  }
}

/**
 * 上传服务实现
 */
export const uploadService: UploadService = {
  uploadSingleFile,
  uploadMultipleFiles
}

export default uploadService