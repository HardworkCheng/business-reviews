<template>
  <el-container class="layout-container">
    <el-container>
      <!-- 顶部导航栏 -->
      <el-header class="header">
        <div class="header-left">
          <div class="logo">
            <el-icon class="logo-icon"><Shop /></el-icon>
            <h2>商家运营中心</h2>
          </div>
        </div>
        
        <!-- 导航菜单 -->
        <div class="header-center">
          <div class="nav-tabs">
            <div class="nav-tab" :class="{ active: activeMenu === '/shops' }" @click="$router.push('/shops')">
              <el-icon><Shop /></el-icon><span>门店管理</span>
            </div>
            <div class="nav-tab" :class="{ active: activeMenu === '/notes' }" @click="$router.push('/notes')">
              <el-icon><Document /></el-icon><span>笔记管理</span>
            </div>
            <div class="nav-tab" :class="{ active: activeMenu === '/coupons' }" @click="$router.push('/coupons')">
              <el-icon><Ticket /></el-icon><span>优惠券</span>
            </div>
            <div class="nav-tab" :class="{ active: activeMenu === '/comments' }" @click="$router.push('/comments')">
              <el-icon><ChatLineSquare /></el-icon><span>评论管理</span>
            </div>
            <div class="nav-tab" :class="{ active: activeMenu === '/settings' }" @click="$router.push('/settings')">
              <el-icon><Setting /></el-icon><span>系统设置</span>
            </div>
          </div>
        </div>
        
        <div class="header-right">
          <el-dropdown @command="handleUserCommand" trigger="click">
            <div class="user-info">
              <el-avatar :src="userStore.getUserInfo?.avatar || ''" :size="32" class="user-avatar">
                <el-icon><UserFilled /></el-icon>
              </el-avatar>
              <span class="username">{{ userStore.getUserInfo?.merchantName || '商家' }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="main"><router-view /></el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { Shop, Document, Ticket, ChatLineSquare, ArrowDown, User, UserFilled, Setting, SwitchButton } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const activeMenu = computed(() => {
  const { meta, path } = route
  if (meta.activeMenu) return meta.activeMenu as string
  return path
})

const currentPageName = computed(() => {
  const nameMap: Record<string, string> = {
    '/shops': '门店管理', '/notes': '笔记管理', '/coupons': '优惠券',
    '/comments': '评论管理', '/settings': '系统设置'
  }
  return nameMap[route.path] || ''
})


const handleUserCommand = (command: string) => {
  if (command === 'logout') { userStore.logout(); router.push('/login') }
  else if (command === 'profile') { router.push('/settings') }
  else if (command === 'settings') { router.push('/settings') }
}
</script>


<style scoped>
.layout-container { 
  height: 100vh; 
  background: #F2F3F6;
}

.header { 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  background: #FFFFFF; 
  border-bottom: 1px solid #E5E7EB; 
  padding: 0 20px; 
  height: 48px; 
  box-shadow: none;
}

.header-left { 
  display: flex; 
  align-items: center; 
  flex-shrink: 0;
}

.logo { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
}
.logo-icon { 
  font-size: 20px; 
  color: #FF7D00; 
}
.logo h2 { 
  color: #333333; 
  font-size: 16px; 
  font-weight: 600; 
  margin: 0; 
  white-space: nowrap; 
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 40px;
}

.nav-tabs {
  display: flex;
  align-items: center;
  gap: 4px;
}

.nav-tab {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  font-size: 14px;
  color: #666666;
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s ease;
  user-select: none;
}

.nav-tab .el-icon {
  font-size: 16px;
}

.nav-tab:hover {
  background: #FFF7ED;
  color: #FF7D00;
}

.nav-tab.active {
  background: #FF7D00;
  color: #FFFFFF;
  font-weight: 500;
}

.header-right { 
  display: flex; 
  align-items: center; 
  flex-shrink: 0;
}

.user-info { 
  display: flex; 
  align-items: center; 
  gap: 8px; 
  cursor: pointer; 
  padding: 6px 12px; 
  border-radius: 6px; 
  transition: all 0.2s ease; 
}

.user-info:hover { 
  background: #F9FAFB; 
}

.user-avatar { 
  border: none;
  box-shadow: none;
}

.username { 
  font-size: 14px; 
  font-weight: 500; 
  color: #333333; 
}

.dropdown-icon { 
  font-size: 12px; 
  color: #999999; 
}

.main { 
  background: #F2F3F6; 
  padding: 0; 
  overflow-y: auto; 
  height: calc(100vh - 48px);
}

/* 响应式适配 */
@media (max-width: 1200px) {
  .nav-tab span {
    display: none;
  }
  .nav-tab {
    padding: 8px 12px;
  }
}

@media (max-width: 768px) {
  .logo h2 {
    display: none;
  }
  .header-center {
    margin: 0 20px;
  }
}
</style>
