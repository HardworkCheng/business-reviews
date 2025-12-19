<template>
  <div style="padding: 20px;">
    <h2>图片上传测试</h2>
    
    <div style="margin: 20px 0;">
      <p>上传URL: {{ uploadUrl }}</p>
      <p>Token: {{ token ? '已设置' : '未设置' }}</p>
    </div>
    
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :on-success="handleSuccess"
      :on-error="handleError"
      :before-upload="beforeUpload"
      list-type="picture-card"
    >
      <el-icon><Plus /></el-icon>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'

const uploadUrl = ref((import.meta.env.VITE_API_BASE_URL || '') + '/merchant/upload')
const token = ref(localStorage.getItem('merchant_token'))

const uploadHeaders = computed(() => {
  const t = localStorage.getItem('merchant_token')
  return t ? { Authorization: `Bearer ${t}` } : {}
})

const beforeUpload = (file: File) => {
  console.log('=== 上传前检查 ===')
  console.log('文件名:', file.name)
  console.log('文件类型:', file.type)
  console.log('文件大小:', file.size)
  console.log('上传URL:', uploadUrl.value)
  console.log('请求头:', uploadHeaders.value)
  
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleSuccess = (response: any, file: any) => {
  console.log('=== 上传成功 ===')
  console.log('响应:', response)
  console.log('文件:', file)
  
  if (response.code === 200) {
    ElMessage.success('上传成功: ' + response.data.url)
  } else {
    ElMessage.error('上传失败: ' + (response.msg || '未知错误'))
  }
}

const handleError = (error: any) => {
  console.log('=== 上传失败 ===')
  console.error('错误:', error)
  ElMessage.error('上传失败，请检查网络连接和后端服务')
}
</script>
