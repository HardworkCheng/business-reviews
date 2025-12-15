<template>
  <div class="login-container">
    <div class="login-form">
      <h2>商家运营中心</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="密码登录" name="password">
          <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef">
            <el-form-item prop="phone">
              <el-input 
                v-model="loginForm.phone" 
                placeholder="请输入手机号" 
                prefix-icon="Phone"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input 
                v-model="loginForm.password" 
                type="password" 
                placeholder="请输入密码" 
                prefix-icon="Lock"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleLogin" 
                :loading="loading"
                style="width: 100%"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <el-tab-pane label="验证码登录" name="code">
          <el-form :model="codeLoginForm" :rules="codeLoginRules" ref="codeLoginFormRef">
            <el-form-item prop="phone">
              <el-input 
                v-model="codeLoginForm.phone" 
                placeholder="请输入手机号" 
                prefix-icon="Phone"
                maxlength="11"
              />
            </el-form-item>
            <el-form-item prop="code">
              <div style="display: flex; gap: 10px;">
                <el-input 
                  v-model="codeLoginForm.code" 
                  placeholder="请输入验证码" 
                  prefix-icon="Key"
                  maxlength="6"
                />
                <el-button 
                  @click="sendCode" 
                  :disabled="codeSending || countdown > 0"
                >
                  {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button 
                type="primary" 
                @click="handleCodeLogin" 
                :loading="loading"
                style="width: 100%"
              >
                登录
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
      <div class="footer">
        <span>还没有账号？</span>
        <el-button type="text" @click="showRegister = true">立即入驻</el-button>
      </div>
    </div>
    
    <!-- 商家入驻弹窗 -->
    <el-dialog v-model="showRegister" title="商家入驻" width="500px">
      <el-form :model="registerForm" :rules="registerRules" ref="registerFormRef">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" maxlength="11" />
        </el-form-item>
        <el-form-item label="验证码" prop="code">
          <div style="display: flex; gap: 10px;">
            <el-input v-model="registerForm.code" placeholder="请输入验证码" maxlength="6" />
            <el-button @click="sendRegisterCode" :disabled="codeSending || countdown > 0">
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
        <el-form-item label="商家名称" prop="merchantName">
          <el-input v-model="registerForm.merchantName" placeholder="请输入商家名称" />
        </el-form-item>
        <el-form-item label="联系人姓名" prop="contactName">
          <el-input v-model="registerForm.contactName" placeholder="请输入联系人姓名" />
        </el-form-item>
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
import { useUserStore } from '@/stores/user'
import { merchantLogin, merchantRegister, sendVerificationCode } from '@/api/auth'

const router = useRouter()
const userStore = useUserStore()

// 表单引用
const loginFormRef = ref()
const codeLoginFormRef = ref()
const registerFormRef = ref()

// 状态
const activeTab = ref('password')
const loading = ref(false)
const codeSending = ref(false)
const countdown = ref(0)
const showRegister = ref(false)

// 登录表单
const loginForm = reactive({
  phone: '',
  password: ''
})

const codeLoginForm = reactive({
  phone: '',
  code: ''
})

// 注册表单
const registerForm = reactive({
  phone: '',
  code: '',
  password: '',
  confirmPassword: '',
  merchantName: '',
  contactName: ''
})

// 表单验证规则
const loginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const codeLoginRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ]
}

const registerRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { pattern: /^\d{6}$/, message: '验证码格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== registerForm.password) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  merchantName: [
    { required: true, message: '请输入商家名称', trigger: 'blur' }
  ],
  contactName: [
    { required: true, message: '请输入联系人姓名', trigger: 'blur' }
  ]
}

// 发送验证码
const sendCode = async () => {
  if (!codeLoginForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(codeLoginForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    codeSending.value = true
    await sendVerificationCode(codeLoginForm.phone, 1) // type=1 登录验证码
    ElMessage.success('验证码已发送')
    
    // 倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('发送验证码失败')
  } finally {
    codeSending.value = false
  }
}

// 发送注册验证码
const sendRegisterCode = async () => {
  if (!registerForm.phone) {
    ElMessage.warning('请输入手机号')
    return
  }
  
  if (!/^1[3-9]\d{9}$/.test(registerForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  
  try {
    codeSending.value = true
    await sendVerificationCode(registerForm.phone, 2) // type=2 注册验证码
    ElMessage.success('验证码已发送')
    
    // 倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    ElMessage.error('发送验证码失败')
  } finally {
    codeSending.value = false
  }
}

// 密码登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        const res = await merchantLogin({
          phone: loginForm.phone,
          password: loginForm.password,
          loginType: 'password'
        })
        
        // 保存token和用户信息
        userStore.setToken(res.token)
        userStore.setUserInfo(res.userInfo)
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 验证码登录
const handleCodeLogin = async () => {
  if (!codeLoginFormRef.value) return
  
  await codeLoginFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        const res = await merchantLogin({
          phone: codeLoginForm.phone,
          code: codeLoginForm.code,
          loginType: 'code'
        })
        
        // 保存token和用户信息
        userStore.setToken(res.token)
        userStore.setUserInfo(res.userInfo)
        
        ElMessage.success('登录成功')
        router.push('/')
      } catch (error) {
        ElMessage.error('登录失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 商家入驻
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
          contactName: registerForm.contactName
        })
        
        // 保存token和用户信息
        userStore.setToken(res.token)
        userStore.setUserInfo(res.userInfo)
        
        ElMessage.success('入驻成功')
        showRegister.value = false
        router.push('/')
      } catch (error) {
        ElMessage.error('入驻失败')
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100vh;
  min-height: 100vh;
  background-color: #f5f7fa;
}

.login-form {
  width: 400px;
  padding: 30px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-form h2 {
  text-align: center;
  margin-bottom: 20px;
  color: #333;
}

.footer {
  text-align: center;
  margin-top: 20px;
  color: #666;
}
</style>