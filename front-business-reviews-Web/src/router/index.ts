import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/login/index.vue'
import MainLayout from '@/layouts/MainLayout.vue'
import DashboardView from '@/views/dashboard/index.vue'
import ShopListView from '@/views/shop/list.vue'
import ShopCreateView from '@/views/shop/create.vue'
import CouponListView from '@/views/coupon/list.vue'
import CouponCreateView from '@/views/coupon/create.vue'
import NoteListView from '@/views/note/list.vue'
import NoteCreateView from '@/views/note/create.vue'
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
      {
        path: '',
        name: 'Dashboard',
        component: DashboardView
      },
      {
        path: 'shops',
        name: 'ShopList',
        component: ShopListView
      },
      {
        path: 'shops/create',
        name: 'ShopCreate',
        component: ShopCreateView
      },
      {
        path: 'shops/edit/:id',
        name: 'ShopEdit',
        component: ShopCreateView,
        meta: { activeMenu: '/shops' }
      },
      {
        path: 'coupons',
        name: 'CouponList',
        component: CouponListView
      },
      {
        path: 'coupons/create',
        name: 'CouponCreate',
        component: CouponCreateView
      },
      {
        path: 'coupons/edit/:id',
        name: 'CouponEdit',
        component: CouponCreateView,
        meta: { activeMenu: '/coupons' }
      },
      {
        path: 'notes',
        name: 'NoteList',
        component: NoteListView
      },
      {
        path: 'notes/create',
        name: 'NoteCreate',
        component: NoteCreateView
      },
      {
        path: 'notes/edit/:id',
        name: 'NoteEdit',
        component: NoteCreateView,
        meta: { activeMenu: '/notes' }
      },
      {
        path: 'comments',
        name: 'CommentList',
        component: CommentListView
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('merchant_token')
  
  // 如果访问需要认证的页面但没有token，重定向到登录页
  if (to.meta.requiresAuth !== false && !token) {
    next({ path: '/login', replace: true })
  } 
  // 如果已登录但访问登录页，重定向到首页
  else if (to.path === '/login' && token) {
    next({ path: '/', replace: true })
  } 
  else {
    next()
  }
})

export default router