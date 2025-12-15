import request from './request'

// 商家登录
export function merchantLogin(data: { phone: string; code?: string; password?: string }) {
  return request.post('/merchant/auth/login', data)
}

// 商家注册
export function merchantRegister(data: { 
  phone: string; 
  code: string; 
  password: string;
  merchantName: string;
  contactName: string;
}) {
  return request.post('/merchant/auth/register', data)
}

// 获取商家信息
export function getMerchantProfile() {
  return request.get('/merchant/auth/profile')
}

// 发送验证码
// type: 1-登录验证码, 2-注册验证码, 3-找回密码, 4-绑定手机, 5-修改密码
export function sendVerificationCode(phone: string, type: number = 1) {
  return request.post('/merchant/auth/send-code', { phone, type })
}