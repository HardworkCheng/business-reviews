<template>
  <div class="settings-page">
    <div class="page-header">
      <h1 class="page-title">系统设置</h1>
      <p class="page-desc">管理商家账号和UniApp对接配置</p>
    </div>

    <div class="settings-content">
      <!-- 左侧菜单 -->
      <div class="settings-menu">
        <div class="menu-item" :class="{ active: activeTab === 'profile' }" @click="activeTab = 'profile'">
          <el-icon><User /></el-icon><span>商家信息</span>
        </div>
        <div class="menu-item" :class="{ active: activeTab === 'uniapp' }" @click="activeTab = 'uniapp'">
          <el-icon><Connection /></el-icon><span>UniApp配置</span>
        </div>
        <div class="menu-item" :class="{ active: activeTab === 'notification' }" @click="activeTab = 'notification'">
          <el-icon><Bell /></el-icon><span>通知设置</span>
        </div>
        <div class="menu-item" :class="{ active: activeTab === 'security' }" @click="activeTab = 'security'">
          <el-icon><Lock /></el-icon><span>安全设置</span>
        </div>
      </div>

      <!-- 右侧内容 -->
      <div class="settings-panel">
        <!-- 商家信息 -->
        <div v-show="activeTab === 'profile'" class="panel-section">
          <div class="section-header-row">
            <h3>商家基本信息</h3>
            <div>
               <el-button v-if="!isEditing" type="primary" link @click="isEditing = true">修改</el-button>
               <el-button v-else type="info" link @click="isEditing = false; loadProfile()">取消</el-button>
            </div>
          </div>
          <el-form :model="profileForm" label-width="120px" class="settings-form">
            <el-form-item label="商家头像">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false" :http-request="uploadAvatar" accept="image/*" :disabled="!isEditing">
                <el-avatar :size="80" :src="profileForm.avatar"><el-icon v-if="isEditing"><Plus /></el-icon></el-avatar>
              </el-upload>
            </el-form-item>
            
            <el-form-item label="商家名称"><el-input v-model="profileForm.name" :readonly="!isEditing" /></el-form-item>
            <el-form-item label="商家姓名"><el-input v-model="profileForm.merchantOwnerName" :readonly="!isEditing" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="profileForm.phone" :readonly="!isEditing" /></el-form-item>
            <el-form-item label="联系邮箱"><el-input v-model="profileForm.email" :readonly="!isEditing" /></el-form-item>
            
            <el-form-item label="营业执照号"><el-input v-model="profileForm.licenseNo" :readonly="!isEditing" /></el-form-item>
            <el-form-item label="营业执照">
              <el-upload class="license-uploader" action="#" :show-file-list="false" :http-request="uploadLicense" accept="image/*" :disabled="!isEditing">
                <img v-if="profileForm.licenseImage" :src="profileForm.licenseImage" class="license-preview" />
                <div v-else-if="isEditing" class="license-placeholder">
                  <el-icon><Plus /></el-icon>
                  <span>上传执照</span>
                </div>
                <div v-else class="license-placeholder" style="cursor: default; border-style: solid; background: #fff;">
                  <span>未上传</span>
                </div>
              </el-upload>
            </el-form-item>
            
            <el-form-item v-if="isEditing"><el-button type="primary" class="save-btn" @click="saveProfile">保存修改</el-button></el-form-item>
          </el-form>
        </div>

        <!-- UniApp配置 -->
        <div v-show="activeTab === 'uniapp'" class="panel-section">
          <h3>UniApp对接配置</h3>
          <div class="config-card">
            <div class="config-header">
              <span class="config-title">数据同步状态</span>
              <el-tag type="success">已连接</el-tag>
            </div>
            <p class="config-desc">您的商家数据已与UniApp实时同步，用户可在App中查看您发布的内容</p>
          </div>
          <el-form :model="uniappForm" label-width="120px" class="settings-form">
            <el-form-item label="自动同步笔记">
              <el-switch v-model="uniappForm.autoSyncNote" />
              <span class="form-tip">开启后，发布的笔记将自动同步到UniApp</span>
            </el-form-item>
            <el-form-item label="自动同步优惠券">
              <el-switch v-model="uniappForm.autoSyncCoupon" />
              <span class="form-tip">开启后，创建的优惠券将自动同步到UniApp</span>
            </el-form-item>
            <el-form-item label="评论通知">
              <el-switch v-model="uniappForm.commentNotify" />
              <span class="form-tip">用户评论时接收通知</span>
            </el-form-item>
            <el-form-item label="点赞通知">
              <el-switch v-model="uniappForm.likeNotify" />
              <span class="form-tip">用户点赞时接收通知</span>
            </el-form-item>
            <el-form-item><el-button type="primary" class="save-btn" @click="saveUniappConfig">保存配置</el-button></el-form-item>
          </el-form>
        </div>

        <!-- 通知设置 -->
        <div v-show="activeTab === 'notification'" class="panel-section">
          <h3>通知设置</h3>
          <el-form :model="notifyForm" label-width="120px" class="settings-form">
            <el-form-item label="邮件通知"><el-switch v-model="notifyForm.email" /></el-form-item>
            <el-form-item label="短信通知"><el-switch v-model="notifyForm.sms" /></el-form-item>
            <el-form-item label="站内消息"><el-switch v-model="notifyForm.inApp" /></el-form-item>
            <el-form-item><el-button type="primary" class="save-btn" @click="saveNotifyConfig">保存设置</el-button></el-form-item>
          </el-form>
        </div>

        <!-- 安全设置 -->
        <div v-show="activeTab === 'security'" class="panel-section">
          <h3>安全设置</h3>
          <el-form :model="securityForm" label-width="100px" class="settings-form">
            <el-form-item label="当前密码"><el-input v-model="securityForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="securityForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="securityForm.confirmPassword" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="primary" class="save-btn" @click="changePassword">修改密码</el-button></el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Connection, Bell, Lock, Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { updateMerchantProfile } from '@/api/auth'
import { uploadPublicFile } from '@/services/uploadService'

const activeTab = ref('profile')
const userStore = useUserStore()

const profileForm = reactive({ 
  avatar: '', 
  name: '', 
  merchantOwnerName: '',
  phone: '', 
  email: '',
  licenseNo: '',
  licenseImage: ''
})

const isEditing = ref(false)

const uniappForm = reactive({ 
  autoSyncNote: true, 
  autoSyncCoupon: true, 
  commentNotify: true, 
  likeNotify: false 
})

const notifyForm = reactive({ 
  email: true, 
  sms: false, 
  inApp: true 
})

const securityForm = reactive({ 
  oldPassword: '', 
  newPassword: '', 
  confirmPassword: '' 
})

const loading = ref(false)

const loadProfile = () => {
  const userInfo = userStore.userInfo
  if (userInfo) {
    profileForm.avatar = userInfo.avatar || ''
    profileForm.name = userInfo.merchantName || ''
    profileForm.merchantOwnerName = userInfo.name || ''
    profileForm.phone = userInfo.phone || ''
    profileForm.email = userInfo.contactEmail || ''
    profileForm.licenseNo = userInfo.licenseNo || ''
    profileForm.licenseImage = userInfo.licenseImage || ''
  }
}

onMounted(() => {
  loadProfile()
})

const uploadAvatar = async (options: any) => {
  try {
    const url = await uploadPublicFile(options.file, 'merchant/avatar')
    profileForm.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e: any) {
    ElMessage.error('头像上传失败: ' + (e.message || '未知错误'))
  }
}



const uploadLicense = async (options: any) => {
  try {
    const url = await uploadPublicFile(options.file, 'merchant/license')
    profileForm.licenseImage = url
    ElMessage.success('营业执照上传成功')
  } catch (e: any) {
    ElMessage.error('营业执照上传失败: ' + (e.message || '未知错误'))
  }
}

const saveProfile = async () => { 
  loading.value = true
  try {
    const payload = {
      merchantName: profileForm.name,
      merchantOwnerName: profileForm.merchantOwnerName,
      avatar: profileForm.avatar,
      contactEmail: profileForm.email,
      phone: profileForm.phone,
      licenseNo: profileForm.licenseNo,
      licenseImage: profileForm.licenseImage
    }
    await updateMerchantProfile(payload)
    
    // 更新本地Store
    const userInfo = userStore.userInfo || {}
    userStore.setUserInfo({
      ...userInfo,
      merchantName: payload.merchantName,
      name: payload.merchantOwnerName,
      avatar: payload.avatar,
      contactEmail: payload.contactEmail,
      phone: payload.phone,
      licenseNo: payload.licenseNo,
      licenseImage: payload.licenseImage
    })
    ElMessage.success('商家信息保存成功')
    isEditing.value = false 
  } catch (e) {
    ElMessage.error('保存失败')
  } finally {
    loading.value = false
  }
}
const saveUniappConfig = () => { ElMessage.success('UniApp配置保存成功') }
const saveNotifyConfig = () => { ElMessage.success('通知设置保存成功') }
const changePassword = () => { 
  if (securityForm.newPassword !== securityForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  ElMessage.success('密码修改成功') 
}
</script>


<style scoped>
.settings-page { 
  max-width: 1400px; 
  margin: 0 auto; 
  padding: 40px; 
  background-color: #f9f9f9;
  min-height: 100vh;
}

.page-header { 
  margin-bottom: 32px; 
}
.page-title { 
  font-size: 28px; 
  font-weight: 600; 
  color: #171a20; 
  margin: 0 0 4px 0; 
  letter-spacing: -0.5px;
}
.page-desc { 
  font-size: 14px; 
  color: #5c5e62; 
  margin: 0; 
}

.settings-content { 
  display: flex; 
  gap: 32px; 
  align-items: flex-start;
}

.settings-menu { 
  width: 240px; 
  background: white; 
  border-radius: 8px; 
  padding: 12px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  height: fit-content; 
  border: 1px solid #e5e5e5;
}

.menu-item { 
  display: flex; 
  align-items: center; 
  gap: 12px; 
  padding: 12px 16px; 
  font-size: 14px; 
  color: #5c5e62; 
  border-radius: 4px; 
  cursor: pointer; 
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1); 
  font-weight: 500;
  margin-bottom: 4px;
}

.menu-item:hover { 
  background: #f4f4f4; 
  color: #171a20; 
}

.menu-item.active { 
  background: #f4f4f4; 
  color: #171a20; 
  font-weight: 600;
}

.menu-item.active .el-icon {
  color: #3e6ae1;
}

.menu-item .el-icon { 
  font-size: 18px; 
  transition: color 0.25s;
}

.settings-panel { 
  flex: 1; 
  background: white; 
  border-radius: 8px; 
  padding: 40px; 
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.02); 
  border: 1px solid #e5e5e5;
}

.panel-section h3 { 
  font-size: 18px; 
  font-weight: 600; 
  color: #171a20; 
  margin: 0 0 32px 0; 
  padding-left: 12px;
  border-left: 3px solid #3e6ae1;
  line-height: 1.2;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 32px;
}

.section-header-row h3 {
  margin: 0 !important;
}

.settings-form { 
  max-width: 600px; 
}

.settings-form :deep(.el-form-item__label) {
  font-weight: 500;
  color: #393c41;
}

.settings-form :deep(.el-input__wrapper) { 
  border-radius: 4px; 
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  background-color: #f4f4f4;
  transition: all 0.2s;
  padding: 2px 12px;
}

.settings-form :deep(.el-input__wrapper:hover) {
  background-color: #e8e8e8;
}

.settings-form :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  border-color: #8e8e8e;
}

.settings-form :deep(.el-textarea__inner) { 
  border-radius: 4px; 
  box-shadow: none !important;
  border: 1px solid #dcdfe6;
  background-color: #f4f4f4;
  transition: all 0.2s;
  padding: 8px 12px;
}

.settings-form :deep(.el-textarea__inner:hover) {
  background-color: #e8e8e8;
}

.settings-form :deep(.el-textarea__inner:focus) {
  background-color: #fff;
  border-color: #8e8e8e;
}

.save-btn {
  background-color: #3e6ae1 !important;
  border-color: #3e6ae1 !important;
  padding: 12px 32px;
  font-weight: 500;
  border-radius: 4px;
}

.avatar-uploader { 
  cursor: pointer; 
  display: inline-block;
}

.avatar-uploader .el-avatar { 
  border: 2px solid #e5e5e5; 
  transition: all 0.3s; 
  background-color: #f4f4f4;
  color: #8e8e8e;
}

.avatar-uploader:hover .el-avatar { 
  border-color: #3e6ae1; 
  transform: scale(1.05);
}

.config-card { 
  background: #f4f7ff; 
  border-radius: 8px; 
  padding: 24px; 
  margin-bottom: 32px; 
  border: 1px solid #e0e7ff;
}

.config-header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  margin-bottom: 12px; 
}

.config-title { 
  font-size: 16px; 
  font-weight: 600; 
  color: #171a20; 
}

.config-desc { 
  font-size: 14px; 
  color: #5c5e62; 
  margin: 0; 
  line-height: 1.5;
}

.form-tip { 
  font-size: 12px; 
  color: #8e8e8e; 
  margin-left: 16px; 
  font-weight: 400;
}

:deep(.el-switch.is-checked .el-switch__core) {
  background-color: #3e6ae1 !important;
  border-color: #3e6ae1 !important;
}

.license-preview {
  width: 200px;
  height: 120px;
  object-fit: contain;
  border: 1px solid #e5e5e5;
  border-radius: 4px;
}
.license-placeholder {
  width: 200px;
  height: 120px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: #8c939d;
  cursor: pointer;
  background-color: #f4f4f4;
  transition: all 0.3s;
}
.license-placeholder:hover {
  border-color: #3e6ae1;
  color: #3e6ae1;
  background-color: #eef2ff;
}
.license-placeholder .el-icon {
  font-size: 24px;
  margin-bottom: 8px;
}
</style>
