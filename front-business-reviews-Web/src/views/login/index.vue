<template>
  <div class="login-container">
    <div class="bg-decoration bg-decoration-1"></div>
    <div class="bg-decoration bg-decoration-2"></div>
    
    <div class="login-card">
      <div class="card-decoration"></div>
      
      <div class="login-content">
        <div class="login-header">
          <h1 class="login-title">美食点评</h1>
          <p class="login-subtitle">欢迎商家登录您的账号</p>
        </div>
        
        <el-tabs v-model="activeTab" class="login-tabs">
          <el-tab-pane label="密码登录" name="password">
            <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
              <el-form-item prop="phone">
                <el-input v-model="loginForm.phone" placeholder="请输入手机号" size="large" maxlength="11" class="custom-input">
                  <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item prop="password">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" size="large" show-password class="custom-input">
                  <template #prefix><el-icon class="input-icon"><Lock /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleLogin" :loading="loading" size="large" class="login-btn">
                  <span>登录</span><el-icon class="btn-arrow"><ArrowRight /></el-icon>
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
          
          <el-tab-pane label="验证码登录" name="code">
            <el-form :model="codeLoginForm" :rules="codeLoginRules" ref="codeLoginFormRef">
              <el-form-item prop="phone">
                <el-input v-model="codeLoginForm.phone" placeholder="请输入手机号" size="large" maxlength="11" class="custom-input">
                  <template #prefix><el-icon class="input-icon"><User /></el-icon></template>
                </el-input>
              </el-form-item>
              <el-form-item prop="code">
                <div class="code-input-wrapper">
                  <el-input v-model="codeLoginForm.code" placeholder="请输入验证码" size="large" maxlength="6" class="custom-input code-input">
                    <template #prefix><el-icon class="input-icon"><Key /></el-icon></template>
                  </el-input>
                  <el-button @click="sendCode" :disabled="codeSending || countdown > 0" size="large" class="code-btn">
                    {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="handleCodeLogin" :loading="loading" size="large" class="login-btn">
                  <span>登录</span><el-icon class="btn-arrow"><ArrowRight /></el-icon>
                </el-button>
              </el-form-item>
            </el-form>
          </el-tab-pane>
        </el-tabs>
        
        <div class="divider"><span>其他登录方式</span></div>
        
        <div class="third-party-login">
          <a href="javascript:void(0);" class="third-party-btn wechat"><el-icon><ChatDotRound /></el-icon></a>
          <a href="javascript:void(0);" class="third-party-btn qq"><el-icon><Comment /></el-icon></a>
        </div>
      </div>
      
      <div class="login-footer">
        <span class="footer-text">还没有账号?</span>
        <el-button type="text" @click="showRegister = true" class="register-link">立即入驻</el-button>
      </div>
    </div>
    
    <!-- 商家入驻弹窗 - 两列布局 -->
    <el-dialog v-model="showRegister" title="商家入驻" width="900px" class="register-dialog" :close-on-click-modal="false">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef" label-width="90px">
        <div class="register-grid">
          <!-- 左列 -->
          <div class="register-column">
            <div class="form-section">
              <div class="section-title">登录信息</div>
              <el-form-item label="手机号" prop="phone">
                <el-input v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11" />
              </el-form-item>
              <el-form-item label="验证码" prop="code">
                <div class="code-input-wrapper">
                  <el-input v-model="registerForm.code" placeholder="请输入验证码" maxlength="6" class="code-input" />
                  <el-button @click="sendRegisterCode" :disabled="codeSending || countdown > 0" class="code-btn">
                    {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                  </el-button>
                </div>
              </el-form-item>
              <el-form-item label="密码" prop="password">
                <el-input v-model="registerForm.password" type="password" placeholder="请输入密码" show-password />
              </el-form-item>
              <el-form-item label="确认密码" prop="confirmPassword">
                <el-input v-model="registerForm.confirmPassword" type="password" placeholder="请确认密码" show-password />
              </el-form-item>
            </div>
            
            <div class="form-section">
              <div class="section-title">营业资质（可选）</div>
              <el-form-item label="营业执照号" prop="licenseNo">
                <el-input v-model="registerForm.licenseNo" placeholder="请输入营业执照号" />
              </el-form-item>
              <el-form-item label="执照图片" prop="licenseImage">
                <div class="upload-area">
                  <el-upload
                    class="license-uploader"
                    :show-file-list="false"
                    :before-upload="beforeLicenseUpload"
                    :http-request="uploadLicenseImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.licenseImage" :src="registerForm.licenseImage" class="uploaded-image" />
                    <div v-else class="upload-placeholder">
                      <el-icon :size="24"><Plus /></el-icon>
                      <span>上传执照</span>
                    </div>
                  </el-upload>
                </div>
              </el-form-item>
            </div>
          </div>
          
          <!-- 右列 -->
          <div class="register-column">
            <div class="form-section">
              <div class="section-title">商家信息</div>
              <el-form-item label="商家名称" prop="merchantName">
                <el-input v-model="registerForm.merchantName" placeholder="请输入商家名称/企业名称" />
              </el-form-item>
              <el-form-item label="商家Logo" prop="logo">
                <div class="upload-area">
                  <el-upload
                    class="logo-uploader"
                    :show-file-list="false"
                    :before-upload="beforeLogoUpload"
                    :http-request="uploadLogoImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.logo" :src="registerForm.logo" class="uploaded-logo" />
                    <div v-else class="upload-placeholder logo-placeholder">
                      <el-icon :size="32"><Plus /></el-icon>
                      <span>上传Logo</span>
                    </div>
                  </el-upload>
                </div>
              </el-form-item>
              <el-form-item label="商家头像" prop="avatar">
                <div class="upload-area">
                  <el-upload
                    class="avatar-uploader"
                    :show-file-list="false"
                    :before-upload="beforeAvatarUpload"
                    :http-request="uploadAvatarImage"
                    accept="image/*"
                  >
                    <img v-if="registerForm.avatar" :src="registerForm.avatar" class="uploaded-avatar" />
                    <div v-else class="upload-placeholder avatar-placeholder">
                      <el-icon :size="32"><Avatar /></el-icon>
                      <span>上传头像</span>
                    </div>
                  </el-upload>
                </div>
              </el-form-item>
            </div>
            
            <div class="form-section">
              <div class="section-title">联系人信息</div>
              <el-form-item label="联系人姓名" prop="contactName">
                <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
              </el-form-item>
              <el-form-item label="联系邮箱" prop="contactEmail">
                <el-input v-model="registerForm.contactEmail" placeholder="请输入联系邮箱（可选）" />
              </el-form-item>
            </div>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="showRegister = false">取消</el-button>
        <el-button type="primary" @click="handleRegister" :loading="loading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Key, ArrowRight, ChatDotRound, Comment, Plus, Avatar } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { merchantLogin, merchantRegister, sendVerificationCode } from '@/api/auth'
import { uploadPublicFile } from '@/services/uploadService'

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
  contactName: '', 
  contactEmail: '',
  licenseNo: '',
  licenseImage: ''
})

const loginRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }]
}
const codeLoginRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }]
}
const registerRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  code: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: (_: any, value: string, callback: any) => { value !== registerForm.password ? callback(new Error('两次输入的密码不一致')) : callback() }, trigger: 'blur' }],
  merchantName: [{ required: true, message: '请输入商家名称', trigger: 'blur' }],
  contactName: [{ required: true, message: '请输入联系人姓名', trigger: 'blur' }],
  contactEmail: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// 图片上传前验证
const beforeLogoUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) { ElMessage.error('只能上传图片文件'); return false }
  if (!isLt5M) { ElMessage.error('图片大小不能超过5MB'); return false }
  return true
}
const beforeAvatarUpload = beforeLogoUpload
const beforeLicenseUpload = beforeLogoUpload

// 上传Logo图片（使用公开上传接口，无需登录）
const uploadLogoImage = async (options: any) => {
  try {
    uploading.value = true
    const url = await uploadPublicFile(options.file, 'merchant/logo')
    registerForm.logo = url
    ElMessage.success('Logo上传成功')
  } catch (e: any) {
    ElMessage.error('Logo上传失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 上传头像图片（使用公开上传接口，无需登录）
const uploadAvatarImage = async (options: any) => {
  try {
    uploading.value = true
    const url = await uploadPublicFile(options.file, 'merchant/avatar')
    registerForm.avatar = url
    ElMessage.success('头像上传成功')
  } catch (e: any) {
    ElMessage.error('头像上传失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

// 上传执照图片（使用公开上传接口，无需登录）
const uploadLicenseImage = async (options: any) => {
  try {
    uploading.value = true
    const url = await uploadPublicFile(options.file, 'merchant/license')
    registerForm.licenseImage = url
    ElMessage.success('执照图片上传成功')
  } catch (e: any) {
    ElMessage.error('执照图片上传失败: ' + (e.message || '未知错误'))
  } finally {
    uploading.value = false
  }
}

const startCountdown = () => { countdown.value = 60; const timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000) }
const sendCode = async () => { if (!codeLoginForm.phone || !/^1[3-9]\d{9}$/.test(codeLoginForm.phone)) { ElMessage.warning('请输入正确的手机号'); return } try { codeSending.value = true; await sendVerificationCode(codeLoginForm.phone, 1); ElMessage.success('验证码已发送'); startCountdown() } catch { ElMessage.error('发送验证码失败') } finally { codeSending.value = false } }
const sendRegisterCode = async () => { if (!registerForm.phone || !/^1[3-9]\d{9}$/.test(registerForm.phone)) { ElMessage.warning('请输入正确的手机号'); return } try { codeSending.value = true; await sendVerificationCode(registerForm.phone, 2); ElMessage.success('验证码已发送'); startCountdown() } catch { ElMessage.error('发送验证码失败') } finally { codeSending.value = false } }
const handleLogin = async () => { if (!loginFormRef.value) return; await loginFormRef.value.validate(async (valid: boolean) => { if (valid) { try { loading.value = true; const res = await merchantLogin({ phone: loginForm.phone, password: loginForm.password, loginType: 'password' }); userStore.setToken(res.token); userStore.setUserInfo(res.userInfo); ElMessage.success('登录成功'); router.push('/') } catch { ElMessage.error('登录失败') } finally { loading.value = false } } }) }
const handleCodeLogin = async () => { if (!codeLoginFormRef.value) return; await codeLoginFormRef.value.validate(async (valid: boolean) => { if (valid) { try { loading.value = true; const res = await merchantLogin({ phone: codeLoginForm.phone, code: codeLoginForm.code, loginType: 'code' }); userStore.setToken(res.token); userStore.setUserInfo(res.userInfo); ElMessage.success('登录成功'); router.push('/') } catch { ElMessage.error('登录失败') } finally { loading.value = false } } }) }
const handleRegister = async () => { 
  if (!registerFormRef.value) return
  await registerFormRef.value.validate(async (valid: boolean) => { 
    if (valid) { 
      try { 
        loading.value = true
        const res = await merchantRegister({ 
          phone: registerForm.phone, 
          code: registerForm.code, 
          password: registerForm.password, 
          merchantName: registerForm.merchantName,
          logo: registerForm.logo || undefined,
          avatar: registerForm.avatar || undefined,
          contactName: registerForm.contactName,
          contactEmail: registerForm.contactEmail || undefined,
          licenseNo: registerForm.licenseNo || undefined,
          licenseImage: registerForm.licenseImage || undefined
        })
        userStore.setToken(res.token)
        userStore.setUserInfo(res.userInfo)
        ElMessage.success('入驻成功')
        showRegister.value = false
        router.push('/') 
      } catch { 
        ElMessage.error('入驻失败') 
      } finally { 
        loading.value = false 
      } 
    } 
  }) 
}
</script>


<style scoped>
.login-container { display: flex; justify-content: center; align-items: center; min-height: 100vh; background: linear-gradient(135deg, #FFFBEB 0%, #ECFDF5 100%); position: relative; overflow: hidden; padding: 20px; }
.bg-decoration { position: absolute; border-radius: 50%; background-color: rgba(255, 125, 0, 0.1); filter: blur(60px); }
.bg-decoration-1 { width: 300px; height: 300px; top: -100px; left: -100px; }
.bg-decoration-2 { width: 400px; height: 400px; bottom: -150px; right: -150px; }
.login-card { width: 100%; max-width: 420px; background: white; border-radius: 16px; box-shadow: 0 20px 60px rgba(0, 0, 0, 0.1); overflow: hidden; position: relative; z-index: 1; transition: all 0.3s ease; }
.login-card:hover { box-shadow: 0 25px 80px rgba(0, 0, 0, 0.15); }
.card-decoration { height: 6px; background: linear-gradient(90deg, #FF7D00 0%, #FFB366 100%); }
.login-content { padding: 40px 32px 24px; }
.login-header { text-align: center; margin-bottom: 32px; }
.login-title { font-size: clamp(1.8rem, 5vw, 2.2rem); font-weight: 700; color: #171717; margin: 0 0 8px 0; }
.login-subtitle { font-size: 14px; color: #737373; margin: 0; }
.login-tabs :deep(.el-tabs__item) { font-size: 15px; color: #737373; }
.login-tabs :deep(.el-tabs__item.is-active) { color: #FF7D00; font-weight: 500; }
.login-tabs :deep(.el-tabs__active-bar) { background-color: #FF7D00; }
.custom-input :deep(.el-input__wrapper) { border-radius: 10px; padding: 4px 12px; }
.custom-input :deep(.el-input__wrapper:focus-within) { box-shadow: 0 0 0 1px #FF7D00 inset, 0 0 0 3px rgba(255, 125, 0, 0.1); }
.input-icon { color: #FF7D00; font-size: 18px; }
.code-input-wrapper { display: flex; gap: 12px; width: 100%; }
.code-input { flex: 1; }
.code-btn { flex-shrink: 0; border-color: #FF7D00; color: #FF7D00; }
.code-btn:hover { background-color: rgba(255, 125, 0, 0.1); }
.login-btn { width: 100%; height: 48px; font-size: 16px; font-weight: 500; border-radius: 10px; background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); border: none; display: flex; align-items: center; justify-content: center; gap: 8px; transition: all 0.3s ease; }
.login-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(255, 125, 0, 0.3); }
.btn-arrow { transition: transform 0.3s ease; }
.login-btn:hover .btn-arrow { transform: translateX(4px); }
.divider { display: flex; align-items: center; margin: 28px 0; color: #A3A3A3; font-size: 13px; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background-color: #E5E5E5; }
.divider span { padding: 0 16px; }
.third-party-login { display: flex; justify-content: center; gap: 20px; }
.third-party-btn { width: 48px; height: 48px; border-radius: 50%; display: flex; align-items: center; justify-content: center; color: white; font-size: 20px; transition: all 0.3s ease; }
.third-party-btn:hover { transform: scale(1.1); }
.third-party-btn.wechat { background-color: #07C160; }
.third-party-btn.qq { background-color: #12B7F5; }
.login-footer { padding: 16px 32px; background-color: #FAFAFA; border-top: 1px solid #F5F5F5; text-align: center; }
.footer-text { color: #737373; font-size: 14px; }
.register-link { color: #FF7D00; font-weight: 500; padding: 0 4px; }
.register-link:hover { color: #E67100; }

/* 注册弹窗样式 - 两列布局 */
.register-dialog :deep(.el-dialog) { border-radius: 12px; }
.register-dialog :deep(.el-dialog__body) { max-height: 70vh; overflow-y: auto; padding: 20px 24px; }
.register-dialog :deep(.el-dialog__header) { border-bottom: 1px solid #f0f0f0; padding-bottom: 16px; }

.register-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 24px; }
.register-column { display: flex; flex-direction: column; gap: 0; }

.form-section { margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #f0f0f0; }
.form-section:last-child { border-bottom: none; margin-bottom: 0; }
.section-title { font-size: 14px; font-weight: 600; color: #FF7D00; margin-bottom: 16px; padding-left: 8px; border-left: 3px solid #FF7D00; }

/* 图片上传区域 */
.upload-area { width: 100%; }
.logo-uploader, .avatar-uploader, .license-uploader { width: 100%; }
.logo-uploader :deep(.el-upload), .avatar-uploader :deep(.el-upload), .license-uploader :deep(.el-upload) { width: 100%; }

.upload-placeholder { width: 100%; height: 120px; border: 2px dashed #dcdfe6; border-radius: 8px; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 8px; color: #909399; cursor: pointer; transition: all 0.3s; background: #fafafa; }
.upload-placeholder:hover { border-color: #FF7D00; color: #FF7D00; background: #fff7ed; }
.upload-placeholder span { font-size: 12px; }

.logo-placeholder, .avatar-placeholder { height: 100px; }

.uploaded-image { width: 100%; height: 120px; object-fit: contain; border-radius: 8px; border: 1px solid #e4e7ed; }
.uploaded-logo { width: 100%; height: 100px; object-fit: contain; border-radius: 8px; border: 1px solid #e4e7ed; }
.uploaded-avatar { width: 100%; height: 100px; object-fit: cover; border-radius: 8px; border: 1px solid #e4e7ed; }

/* 响应式 */
@media (max-width: 768px) {
  .register-grid { grid-template-columns: 1fr; }
  .register-dialog :deep(.el-dialog) { width: 95% !important; max-width: 500px; }
}
</style>
