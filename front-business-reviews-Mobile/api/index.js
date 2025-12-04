/**
 * API统一导出入口
 * 将所有模块的API统一导出，方便在页面中使用
 * 
 * 使用示例：
 * import api from '@/api'
 * 
 * // 调用接口
 * api.auth.loginByCode(phone, code)
 * api.note.getRecommendedNotes(1, 10)
 * api.user.getUserInfo()
 */

import * as auth from './auth'
import * as user from './user'
import * as note from './note'
import * as comment from './comment'
import * as shop from './shop'
import * as message from './message'
import * as upload from './upload'
import * as common from './common'

export default {
  auth,      // 认证模块
  user,      // 用户模块
  note,      // 笔记模块
  comment,   // 评论模块
  shop,      // 商家模块
  message,   // 消息模块
  upload,    // 上传模块
  common     // 公共模块（分类、话题、地图、搜索）
}
