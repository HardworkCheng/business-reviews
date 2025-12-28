import request from './request'

// 商家登录
export function merchantLogin(data: { phone: string; code?: string; password?: string; loginType?: string }) {
  return request.post('/merchant/auth/login', data)
}

// 商家入驻注册（完整信息）
// 对应数据库表 merchants 的字段
export interface MerchantRegisterData {
  // 账号信息
  phone: string              // contact_phone - 联系电话（作为登录账号，必填）
  code: string               // 验证码（仅用于注册验证，不存数据库）
  password: string           // password - 登录密码（后端加密存储）

  // 商家基本信息
  merchantName: string       // name - 商家名称/企业名称（必填）
  merchantOwnerName: string  // 商家姓名/负责人姓名（必填）
  logo?: string              // logo - 商家Logo（选填）
  avatar?: string            // avatar - 商家头像/形象图（选填）

  // 联系人信息
  contactEmail?: string      // contact_email - 联系邮箱（选填）

  // 营业资质
  licenseNo?: string         // license_no - 营业执照号（选填）
  licenseImage?: string      // license_image - 营业执照图片（选填）

  // 注：以下字段由后端自动处理
  // role_id - 角色ID（后端设置默认角色）
  // status - 状态（默认1正常，或3待审核）
  // created_at, updated_at - 数据库自动生成
  // last_login_at - 登录时更新
}

export function merchantRegister(data: MerchantRegisterData) {
  return request.post('/merchant/auth/register', data)
}

// 获取商家信息
export function getMerchantProfile() {
  return request.get('/merchant/auth/profile')
}

export interface MerchantUpdateData {
  merchantName?: string
  merchantOwnerName?: string
  avatar?: string
  logo?: string
  contactEmail?: string
  phone?: string
  licenseNo?: string
  licenseImage?: string
}

// 更新商家信息
export function updateMerchantProfile(data: MerchantUpdateData) {
  return request.put('/merchant/auth/profile', data)
}

// 发送验证码
// type: 1-登录验证码, 2-注册验证码, 3-找回密码, 4-绑定手机, 5-修改密码
export function sendVerificationCode(phone: string, type: number = 1) {
  return request.post('/merchant/auth/send-code', { phone, type })
}