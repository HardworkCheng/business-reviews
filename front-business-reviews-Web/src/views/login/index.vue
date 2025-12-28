<template>
  <div class="login-container">
    <!-- 左侧/背景轮播区域 -->
    <div class="carousel-container">
      <transition-group name="fade" tag="div" class="carousel-wrapper">
        <div 
          v-for="(img, index) in bgImages" 
          :key="img"
          class="carousel-item"
          :class="{ active: currentBgIndex === index }"
          :style="{ backgroundImage: `url(${img})` }"
        ></div>
      </transition-group>
      <div class="carousel-overlay"></div>
    </div>

    <!-- 右侧/浮动登录面板 -->
    <div class="login-panel">
      <div class="panel-header">
        <h1 class="brand-title">商家运营中心</h1>
        <p class="brand-subtitle">高效 · 智能 · 便捷</p>
      </div>

      <el-tabs v-model="activeTab" class="custom-tabs">
        <el-tab-pane label="密码登录" name="password">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" class="login-form">
            <el-form-item prop="phone">
              <div class="input-label">手机号</div>
              <el-input v-model="loginForm.phone" placeholder="请输入手机号" class="minimal-input" />
            </el-form-item>
            <el-form-item prop="password">
              <div class="input-label">密码</div>
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password class="minimal-input" />
            </el-form-item>
            <el-button type="primary" @click="handleLogin" :loading="loading" class="submit-btn">
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="验证码登录" name="code">
          <el-form :model="codeLoginForm" :rules="codeLoginRules" ref="codeLoginFormRef" class="login-form">
            <el-form-item prop="phone">
              <div class="input-label">手机号</div>
              <el-input v-model="codeLoginForm.phone" placeholder="请输入手机号" class="minimal-input" />
            </el-form-item>
            <el-form-item prop="code">
              <div class="input-label">验证码</div>
              <div class="code-wrapper">
                <el-input v-model="codeLoginForm.code" placeholder="6位数字" class="minimal-input code-input" maxlength="6" />
                <el-button link @click="sendCode" :disabled="codeSending || countdown > 0" class="send-code-btn">
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-button type="primary" @click="handleCodeLogin" :loading="loading" class="submit-btn">
              登 录
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>

      <div class="panel-footer">
        <el-button link @click="showRegister = true" class="register-link">还没有账号？立即入驻</el-button>
      </div>
    </div>

    <!-- 商家入驻弹窗 -->
    <el-dialog v-model="showRegister" title="商家入驻申请" width="1100px" class="register-dialog" :close-on-click-modal="false" top="5vh">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="100px" :label-position="'right'" size="default">
        <div class="register-grid">
          <!-- 左列 -->
          <div class="register-column">
            <div class="form-section">
              <div class="section-title">账号信息</div>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="registerForm.phone" placeholder="作为登录账号" maxlength="11" />
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="code-input-wrapper">
                  <el-input v-model="registerForm.code" placeholder="6位验证码" maxlength="6" />
                  <el-button @click="sendRegisterCode" :disabled="codeSending || countdown > 0" class="code-btn-reg">
                    {{ countdown > 0 ? `${countdown}s后重发` : '获取验证码' }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="登录密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="6-20位，字母+数字" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="再次输入密码" show-password />
              </el-form-item>

              
              <el-form-item label="商家姓名" prop="merchantOwnerName">
                <el-input v-model="registerForm.merchantOwnerName" placeholder="商家负责人姓名" />
              </el-form-item>
              
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input v-model="registerForm.contactEmail" placeholder="接收重要通知（选填）" />
              </el-form-item>
            </div>
          </div>
          
          <!-- 右列 -->
          <div class="register-column">
            <div class="form-section">
              <div class="section-title">商家资质</div>
              <el-form-item label="商家名称" prop="merchantName">
                <el-input v-model="registerForm.merchantName" placeholder="对外展示的店铺名称" />
              </el-form-item>
              
              <!-- 图片并排显示以节省空间 -->
              <div class="upload-row">
                <el-form-item label="商家Logo" class="half-item">
                  <el-upload
                    class="mini-uploader"
                    :show-file-list="false"
                    :before-upload="beforeLogoUpload"
                    :http-request="uploadLogoImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.logo" :src="registerForm.logo" class="uploaded-mini" />
                    <div v-else class="upload-placeholder mini-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>Logo</span>
                    </div>
                  </el-upload>
                </el-form-item>
                
                <el-form-item label="商家头像" class="half-item">
                  <el-upload
                    class="mini-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="uploadAvatarImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.avatar" :src="registerForm.avatar" class="uploaded-mini" />
                    <div v-else class="upload-placeholder mini-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>商家头像</span>
                    </div>
                  </el-upload>

                </el-form-item>
              </div>

              <el-form-item label="营业执照号" prop="licenseNo">
                <el-input v-model="registerForm.licenseNo" placeholder="统一社会信用代码（选填）" />
              </el-form-item>
              
              <el-form-item label="营业执照">
                 <el-upload
                    class="license-uploader-compact"
                    :show-file-list="false"
                    :before-upload="beforeLicenseUpload"
                    :http-request="uploadLicenseImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.licenseImage" :src="registerForm.licenseImage" class="uploaded-rect" />
                    <div v-else class="upload-placeholder rect-placeholder">
                      <el-icon><Plus /></el-icon>
                      <span>点击上传营业执照（选填）</span>
                    </div>
                  </el-upload>
              </el-form-item>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="showRegister = false" class="cancel-btn-dialog">取消</el-button>
          <el-button type="primary" @click="handleRegister" :loading="loading" class="submit-btn-dialog">立即入驻</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { merchantLogin, merchantRegister, sendVerificationCode } from '@/api/auth'
import { uploadPublicFile } from '@/services/uploadService'

// 导入图片
import bg1 from '@/assets/images/login/bg1.png'
import bg2 from '@/assets/images/login/bg2.png'
import bg3 from '@/assets/images/login/bg3.png'
import bg4 from '@/assets/images/login/bg4.png'

const router = useRouter()
const userStore = useUserStore()
const loginFormRef = ref()
const codeLoginFormRef = ref()
const registerFormRef = ref()
const activeTab = ref('password')
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const showRegister = ref(false)
const uploading = ref(false)

// 轮播逻辑
const bgImages = [bg1, bg2, bg3, bg4]
const currentBgIndex = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const startCarousel = () => {
  timer = setInterval(() => {
    currentBgIndex.value = (currentBgIndex.value + 1) % bgImages.length
  }, 5000)
}

onMounted(() => {
  startCarousel()
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const loginForm = reactive({ phone: '', password: '' })
const codeLoginForm = reactive({ phone: '', code: '' })
const registerForm = reactive({ 
  phone: '', 
  code: '', 
  password: '', 
  confirmPassword: '', 
  merchantName: '', 
  logo: '',
  avatar: '',
  merchantOwnerName: '',
  contactEmail: '',
  licenseNo: '',
  licenseImage: ''
})

const loginRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '不少于6位', trigger: 'blur' }]
}
const codeLoginRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { pattern: /^\d{6}$/, message: '需6位数字', trigger: 'blur' }]
}
const registerRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { pattern: /^\d{6}$/, message: '需6位数字', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '不少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (_: any, value: string, callback: any) => { value !== registerForm.password ? callback(new Error('密码不一致')) : callback() }, trigger: 'blur' }],
  merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],

  merchantOwnerName: [{ required: true, message: '请输入商家姓名', trigger: 'blur' }],
  contactEmail: [{ type: 'email', message: '格式不正确', trigger: 'blur' }]
}

// 保持原有的上传和登录逻辑不变
const beforeLogoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}
const beforeAvatarUpload = beforeLogoUpload
const beforeLicenseUpload = beforeLogoUpload

const uploadLogoImage = async (options: any) => {
  try { uploading.value = true; const url = await uploadPublicFile(options.file, 'merchant/logo'); registerForm.logo = url; ElMessage.success('Logo上传成功') } catch (e: any) { ElMessage.error('Logo上传失败: ' + (e.message || '未知错误')) } finally { uploading.value = false }
}
const uploadAvatarImage = async (options: any) => {
  try { uploading.value = true; const url = await uploadPublicFile(options.file, 'merchant/avatar'); registerForm.avatar = url; ElMessage.success('头像上传成功') } catch (e: any) { ElMessage.error('头像上传失败: ' + (e.message || '未知错误')) } finally { uploading.value = false }
}
const uploadLicenseImage = async (options: any) => {
  try { uploading.value = true; const url = await uploadPublicFile(options.file, 'merchant/license'); registerForm.licenseImage = url; ElMessage.success('执照上传成功') } catch (e: any) { ElMessage.error('执照上传失败: ' + (e.message || '未知错误')) } finally { uploading.value = false }
}

const startCountdown = () => { countdown.value = 60; const t = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(t) }, 1000) }
const sendCode = async () => { if (!codeLoginForm.phone || !/^1[3-9]\d{9}$/.test(codeLoginForm.phone)) { ElMessage.warning('请输入正确的手机号'); return } try { codeSending.value = true; await sendVerificationCode(codeLoginForm.phone, 1); ElMessage.success('验证码已发送'); startCountdown() } catch { ElMessage.error('发送失败') } finally { codeSending.value = false } }
const sendRegisterCode = async () => { if (!registerForm.phone || !/^1[3-9]\d{9}$/.test(registerForm.phone)) { ElMessage.warning('请输入正确的手机号'); return } try { codeSending.value = true; await sendVerificationCode(registerForm.phone, 2); ElMessage.success('验证码已发送'); startCountdown() } catch { ElMessage.error('发送失败') } finally { codeSending.value = false } }
const handleLogin = async () => { if (!loginFormRef.value) return; await loginFormRef.value.validate(async (valid: boolean) => { if (valid) { try { loading.value = true; const res = await merchantLogin({ phone: loginForm.phone, password: loginForm.password, loginType: 'password' }); userStore.setToken(res.token); userStore.setUserInfo(res.userInfo); ElMessage.success('登录成功'); router.push('/') } catch { ElMessage.error('登录失败') } finally { loading.value = false } } }) }
const handleCodeLogin = async () => { if (!codeLoginFormRef.value) return; await codeLoginFormRef.value.validate(async (valid: boolean) => { if (valid) { try { loading.value = true; const res = await merchantLogin({ phone: codeLoginForm.phone, code: codeLoginForm.code, loginType: 'code' }); userStore.setToken(res.token); userStore.setUserInfo(res.userInfo); ElMessage.success('登录成功'); router.push('/') } catch { ElMessage.error('登录失败') } finally { loading.value = false } } }) }
const handleRegister = async () => { if (!registerFormRef.value) return; await registerFormRef.value.validate(async (valid: boolean) => { if (valid) { try { loading.value = true; const res = await merchantRegister({ ...registerForm }); userStore.setToken(res.token); userStore.setUserInfo(res.userInfo); ElMessage.success('入驻成功'); showRegister.value = false; router.push('/') } catch { ElMessage.error('入驻失败') } finally { loading.value = false } } }) }
</script>

<style scoped>
/* 容器布局 */
.login-container {
  display: flex;
  height: 100vh;
  width: 100%;
  background-color: #f9f9f9;
}

/* 轮播区域 - 占据大量空间 或 分屏 */
.carousel-container {
  flex: 1;
  position: relative;
  overflow: hidden;
  background-color: #000;
}

.carousel-wrapper {
  width: 100%;
  height: 100%;
  position: relative;
}

.carousel-item {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-size: cover;
  background-position: center;
  transition: opacity 1.5s ease-in-out;
  opacity: 0;
}

.carousel-item.active {
  opacity: 1;
}

/* 轮播遮罩 */
.carousel-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, rgba(0,0,0,0) 60%, rgba(255,255,255,1) 100%);
  pointer-events: none;
  /* 平滑过渡到右侧面板 */
}

/* 登录面板 - 极简风格 */
.login-panel {
  width: 480px;
  background: #fff;
  padding: 80px 60px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  box-shadow: -10px 0 30px rgba(0,0,0,0.05);
  position: relative;
  z-index: 10;
}

.panel-header {
  margin-bottom: 48px;
  text-align: left;
}

.brand-title {
  font-size: 32px;
  font-weight: 600;
  color: #171a20;
  letter-spacing: -0.5px;
  margin: 0 0 8px 0;
}

.brand-subtitle {
  font-size: 14px;
  color: #5c5e62;
  font-weight: 400;
}

/* 极简 Input 风格 */
.input-label {
  font-size: 14px;
  font-weight: 500;
  color: #393c41;
  margin-bottom: 8px;
}

.minimal-input :deep(.el-input__wrapper) {
  box-shadow: none;
  background-color: #f4f4f4;
  border-radius: 4px;
  padding: 8px 12px;
  transition: all 0.2s;
}

.minimal-input :deep(.el-input__wrapper:hover) {
  background-color: #e8e8e8;
}

.minimal-input :deep(.el-input__wrapper.is-focus) {
  background-color: #fff;
  box-shadow: 0 0 0 1px #8e8e8e inset;
}

.code-wrapper {
  display: flex;
  gap: 12px;
}

.send-code-btn {
  color: #171a20;
  font-weight: 500;
}
.send-code-btn:hover {
  color: #3e6ae1;
}

/* 按钮风格 */
.submit-btn {
  width: 100%;
  height: 40px;
  background-color: #3e6ae1;
  border-color: #3e6ae1;
  border-radius: 4px;
  font-size: 14px;
  font-weight: 500;
  margin-top: 24px;
  transition: all 0.3s;
}

.submit-btn:hover {
  background-color: #3458b9;
  border-color: #3458b9;
}

/* Tabs */
.custom-tabs :deep(.el-tabs__nav-wrap::after) {
  height: 1px;
  background-color: #e2e2e2;
}

.custom-tabs :deep(.el-tabs__item) {
  font-size: 14px;
  color: #5c5e62;
  font-weight: 500;
}

.custom-tabs :deep(.el-tabs__item.is-active) {
  color: #171a20;
}

.custom-tabs :deep(.el-tabs__active-bar) {
  background-color: #3e6ae1;
  height: 2px;
}

.panel-footer {
  margin-top: 24px;
  text-align: center;
}

.register-link {
  color: #393c41;
  font-size: 13px;
  text-decoration: underline;
}

.register-link:hover {
  color: #171a20;
}

/* 注册弹窗微调 */
.register-dialog :deep(.el-dialog__header) {
  margin-right: 0;
  border-bottom: 1px solid #eee;
  padding: 15px 20px 10px;
}
.register-dialog :deep(.el-dialog__body) {
  padding: 20px 30px;
}

.register-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 40px;
}
.register-column {
  min-width: 0; /* 防止flex/grid溢出 */
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  color: #171a20;
  margin-bottom: 12px;
  border-left: 3px solid #3e6ae1;
  padding-left: 8px;
  line-height: 1.2;
}

.form-section {
  margin-bottom: 0;
}

/* 紧凑表单项 */
.register-dialog :deep(.el-form-item) {
  margin-bottom: 12px; /* 进一步压缩间距 */
}
.register-dialog :deep(.el-form-item__label) {
  padding-right: 12px;
}

/* 图片上传行 */
.upload-row {
  display: flex;
  gap: 15px;
  margin-bottom: 0;
}

.half-item {
  flex: 1;
  min-width: 0; 
  margin-bottom: 12px !important;
}

/* 上传组件样式 */
.upload-placeholder {
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: #fafafa;
  color: #8c939d;
  transition: border-color 0.2s;
  cursor: pointer;
}
.upload-placeholder:hover {
  border-color: #3e6ae1;
  color: #3e6ae1;
}

/* 迷你正方形上传器 (Logo/头像) */
.mini-uploader :deep(.el-upload) {
  width: 140px;
}
.mini-placeholder, .uploaded-mini {
  width: 140px;
  height: 140px;
  object-fit: cover;
  border-radius: 6px;
}

/* 矩形上传器 (执照) */
.license-uploader-compact :deep(.el-upload) {
  width: 100%;
}
.rect-placeholder, .uploaded-rect {
  width: 100%;
  height: 140px;
  object-fit: contain;
}

.mini-placeholder .el-icon, .rect-placeholder .el-icon {
  font-size: 32px;
  margin-bottom: 4px;
}
.mini-placeholder span, .rect-placeholder span {
  font-size: 12px;
}

.upload-hint {
  font-size: 11px;
  color: #8e8e8e;
  margin-top: 6px;
  text-align: center;
  line-height: 1.3;
}

/* 验证码输入框 */
.code-input-wrapper {
  display: flex;
  gap: 10px;
  width: 100%;
}
.code-input-reg {
  flex: 1;
}
.code-btn-reg {
  padding: 0 10px;
  width: 80px;
}

/* 响应式适配 */
@media (max-width: 900px) {
  .carousel-container {
    display: none;
  }
  .login-panel {
    width: 100%;
    max-width: 100%;
    padding: 40px 20px;
  }
  .register-dialog {
    width: 95% !important;
  }
  .register-grid {
    grid-template-columns: 1fr;
    gap: 20px;
  }
}

/* 弹窗按钮风格 */
.submit-btn-dialog {
  background-color: #3e6ae1 !important;
  border-color: #3e6ae1 !important;
  font-weight: 500;
}
.submit-btn-dialog:hover {
  background-color: #3458b9 !important;
  border-color: #3458b9 !important;
}
.cancel-btn-dialog:hover {
  color: #3e6ae1;
  border-color: #c6d1f0;
  background-color: #f0f4ff;
}
</style>
