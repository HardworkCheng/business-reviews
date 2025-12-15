import axios from 'axios'

// 上传文件到阿里云OSS（通过后端代理）
export async function uploadFile(file: File): Promise<{ url: string }> {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('folder', 'merchant')
  
  const token = localStorage.getItem('merchant_token')
  
  const response = await axios.post('/api/merchant/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': token ? `Bearer ${token}` : ''
    }
  })
  
  if (response.data.code === 200) {
    return response.data.data
  } else {
    throw new Error(response.data.msg || '上传失败')
  }
}

// 上传多个文件
export async function uploadFiles(files: File[]): Promise<{ urls: string[] }> {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('files', file)
  })
  formData.append('folder', 'merchant')
  
  const token = localStorage.getItem('merchant_token')
  
  const response = await axios.post('/api/merchant/upload/batch', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
      'Authorization': token ? `Bearer ${token}` : ''
    }
  })
  
  if (response.data.code === 200) {
    return response.data.data
  } else {
    throw new Error(response.data.msg || '上传失败')
  }
}
