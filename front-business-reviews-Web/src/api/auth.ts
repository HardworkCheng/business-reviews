import request from './request'

// 商家登录
export function merchantLogin(data: { phone: string; code?: string; password?: string; loginType?: string }) {
  return request.post('/merchant/auth/login', data)
}

// 商家入驻注册（完整信息）
export interface MerchantRegisterData {
  // 登录信息
  phone: string
  code: string
  password: string
  // 商家信息
  merchantName: string
  logo?: string
  avatar?: string  // 商家头像
  // 联系人信息
  contactName: string
  contactEmail?: string
  // 营业资质
  licenseNo?: string
  licenseImage?: string
}

export function merchantRegister(data: MerchantRegisterData) {
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