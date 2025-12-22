import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/login/index.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import ShopListView from '@/views/shop/list.vue'
import ShopCreateView from '@/views/shop/create.vue'
import CouponListView from '@/views/coupon/list.vue'
import CouponCreateView from '@/views/coupon/create.vue'
import NoteListView from '@/views/note/list.vue'
import NoteCreateView from '@/views/note/create.vue'
import NoteDetailView from '@/views/note/detail.vue'
import CommentListView from '@/views/comment/list.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: { requiresAuth: false }
  },
  {
    path: '/',
    component: MainLayout,
    meta: { requiresAuth: true },
    children: [
      { path: 'shops', name: 'ShopList', component: ShopListView },
      { path: 'shops/create', name: 'ShopCreate', component: ShopCreateView },
      { path: 'shops/edit/:id', name: 'ShopEdit', component: ShopCreateView, meta: { activeMenu: '/shops' } },
      { path: 'notes', name: 'NoteList', component: NoteListView },
      { path: 'notes/create', name: 'NoteCreate', component: NoteCreateView },
      { path: 'notes/detail/:id', name: 'NoteDetail', component: NoteDetailView, meta: { activeMenu: '/notes' } },
      { path: 'notes/edit/:id', name: 'NoteEdit', component: NoteCreateView, meta: { activeMenu: '/notes' } },
      { path: 'coupons', name: 'CouponList', component: CouponListView },
      { path: 'coupons/create', name: 'CouponCreate', component: CouponCreateView },
      { path: 'coupons/edit/:id', name: 'CouponEdit', component: CouponCreateView, meta: { activeMenu: '/coupons' } },
      { path: 'comments', name: 'CommentList', component: CommentListView },
      { path: 'settings', name: 'Settings', component: () => import('@/views/settings/index.vue') },
      { path: 'test-upload', name: 'TestUpload', component: () => import('@/views/test-upload.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/**
 * 验证token是否有效（简单检查格式）
 * 不再在前端验证过期时间，让后端来验证
 */
const isValidToken = (token: string | null): boolean => {
  console.log('[isValidToken] 检查token:', token ? `${token.substring(0, 20)}...` : 'null')
  
  if (!token || token.trim() === '') {
    console.log('[isValidToken] token为空')
    return false
  }
  
  // JWT token 格式检查: header.payload.signature
  const parts = token.split('.')
  if (parts.length !== 3) {
    console.log('[isValidToken] token格式错误，不是3部分')
    return false
  }
  
  // 只检查格式，不检查过期时间，让后端验证
  console.log('[isValidToken] token格式正确')
  return true
}

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('merchant_token')
  console.log('[Router Guard] localStorage中的token:', token ? `存在(${token.length}字符)` : '不存在')
  
  const hasValidToken = isValidToken(token)
  
  console.log('[Router Guard] 路径:', to.path, '| Token有效:', hasValidToken)
  
  // 已删除的路由列表
  const removedRoutes = ['/users', '/messages', '/analytics']
  
  // 如果访问已删除的路由,重定向到门店管理或登录页
  if (removedRoutes.includes(to.path)) {
    next({ path: hasValidToken ? '/shops' : '/login', replace: true })
    return
  }
  
  // 根路径处理
  if (to.path === '/') {
    if (hasValidToken) {
      next({ path: '/shops', replace: true })
    } else {
      next({ path: '/login', replace: true })
    }
    return
  }
  
  // 登录页面不需要认证
  if (to.path === '/login') {
    if (hasValidToken) {
      // 已登录用户访问登录页，重定向到店铺管理
      next({ path: '/shops', replace: true })
    } else {
      next()
    }
    return
  }
  
  // 其他所有页面都需要认证
  if (!hasValidToken) {
    next({ path: '/login', replace: true })
    return
  }
  
  // 已登录，允许访问
  next()
})

export default router
