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
          <h3>商家基本信息</h3>
          <el-form :model="profileForm" label-width="100px" class="settings-form">
            <el-form-item label="商家头像">
              <el-upload class="avatar-uploader" action="#" :show-file-list="false">
                <el-avatar :size="80" :src="profileForm.avatar"><el-icon><Plus /></el-icon></el-avatar>
              </el-upload>
            </el-form-item>
            <el-form-item label="商家名称"><el-input v-model="profileForm.name" /></el-form-item>
            <el-form-item label="联系电话"><el-input v-model="profileForm.phone" /></el-form-item>
            <el-form-item label="联系邮箱"><el-input v-model="profileForm.email" /></el-form-item>
            <el-form-item label="商家简介"><el-input v-model="profileForm.description" type="textarea" :rows="3" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveProfile">保存修改</el-button></el-form-item>
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
            <el-form-item><el-button type="primary" @click="saveUniappConfig">保存配置</el-button></el-form-item>
          </el-form>
        </div>

        <!-- 通知设置 -->
        <div v-show="activeTab === 'notification'" class="panel-section">
          <h3>通知设置</h3>
          <el-form :model="notifyForm" label-width="120px" class="settings-form">
            <el-form-item label="邮件通知"><el-switch v-model="notifyForm.email" /></el-form-item>
            <el-form-item label="短信通知"><el-switch v-model="notifyForm.sms" /></el-form-item>
            <el-form-item label="站内消息"><el-switch v-model="notifyForm.inApp" /></el-form-item>
            <el-form-item><el-button type="primary" @click="saveNotifyConfig">保存设置</el-button></el-form-item>
          </el-form>
        </div>

        <!-- 安全设置 -->
        <div v-show="activeTab === 'security'" class="panel-section">
          <h3>安全设置</h3>
          <el-form :model="securityForm" label-width="100px" class="settings-form">
            <el-form-item label="当前密码"><el-input v-model="securityForm.oldPassword" type="password" show-password /></el-form-item>
            <el-form-item label="新密码"><el-input v-model="securityForm.newPassword" type="password" show-password /></el-form-item>
            <el-form-item label="确认密码"><el-input v-model="securityForm.confirmPassword" type="password" show-password /></el-form-item>
            <el-form-item><el-button type="primary" @click="changePassword">修改密码</el-button></el-form-item>
          </el-form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Connection, Bell, Lock, Plus } from '@element-plus/icons-vue'

const activeTab = ref('profile')

const profileForm = reactive({ avatar: '', name: '示例商家', phone: '138****1234', email: 'merchant@example.com', description: '这是一家优质商家' })
const uniappForm = reactive({ autoSyncNote: true, autoSyncCoupon: true, commentNotify: true, likeNotify: false })
const notifyForm = reactive({ email: true, sms: false, inApp: true })
const securityForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const saveProfile = () => { ElMessage.success('商家信息保存成功') }
const saveUniappConfig = () => { ElMessage.success('UniApp配置保存成功') }
const saveNotifyConfig = () => { ElMessage.success('通知设置保存成功') }
const changePassword = () => { ElMessage.success('密码修改成功') }
</script>


<style scoped>
.settings-page { max-width: 1200px; margin: 0 auto; }

.page-header { margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; color: #171717; margin: 0 0 6px 0; }
.page-desc { font-size: 14px; color: #737373; margin: 0; }

.settings-content { display: flex; gap: 24px; }

.settings-menu { width: 220px; background: white; border-radius: 16px; padding: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); height: fit-content; }
.menu-item { display: flex; align-items: center; gap: 12px; padding: 14px 16px; font-size: 14px; color: #525252; border-radius: 10px; cursor: pointer; transition: all 0.2s ease; }
.menu-item:hover { background: #FFF7ED; color: #FF7D00; }
.menu-item.active { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; }
.menu-item .el-icon { font-size: 18px; }

.settings-panel { flex: 1; background: white; border-radius: 16px; padding: 32px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.panel-section h3 { font-size: 18px; font-weight: 600; color: #171717; margin: 0 0 24px 0; padding-bottom: 16px; border-bottom: 1px solid #F5F5F5; }

.settings-form { max-width: 500px; }
.settings-form :deep(.el-input__wrapper) { border-radius: 10px; }
.settings-form :deep(.el-textarea__inner) { border-radius: 10px; }

.avatar-uploader { cursor: pointer; }
.avatar-uploader .el-avatar { border: 2px dashed #E5E5E5; transition: border-color 0.2s ease; }
.avatar-uploader:hover .el-avatar { border-color: #FF7D00; }

.config-card { background: #FFF7ED; border-radius: 12px; padding: 20px; margin-bottom: 24px; }
.config-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.config-title { font-size: 15px; font-weight: 600; color: #171717; }
.config-desc { font-size: 13px; color: #737373; margin: 0; }

.form-tip { font-size: 12px; color: #A3A3A3; margin-left: 12px; }
</style>
