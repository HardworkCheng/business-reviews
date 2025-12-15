<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>商家运营中心</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="menu"
        @select="handleMenuSelect"
        router
      >
        <el-menu-item index="/">
          <el-icon><Odometer /></el-icon>
          <span>数据看板</span>
        </el-menu-item>
        <el-menu-item index="/shops">
          <el-icon><Shop /></el-icon>
          <span>门店管理</span>
        </el-menu-item>
        <el-menu-item index="/notes">
          <el-icon><Document /></el-icon>
          <span>笔记管理</span>
        </el-menu-item>
        <el-menu-item index="/coupons">
          <el-icon><Ticket /></el-icon>
          <span>优惠券</span>
        </el-menu-item>
        <el-menu-item index="/comments">
          <el-icon><ChatLineSquare /></el-icon>
          <span>评论管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <!-- 主区域 -->
    <el-container>
      <!-- 头部 -->
      <el-header class="header">
        <div class="header-left">
          <el-button @click="toggleCollapse" link>
            <el-icon><Expand v-if="isCollapse" /><Fold v-else /></el-icon>
          </el-button>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleUserCommand">
            <div class="user-info">
              <el-avatar :src="userStore.getUserInfo?.avatar || ''" size="small" />
              <span class="username">{{ userStore.getUserInfo?.name || userStore.getUserInfo?.merchantName }}</span>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  Odometer,
  Shop,
  Document,
  Ticket,
  ChatLineSquare,
  Expand,
  Fold
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 状态
const isCollapse = ref(false)

// 计算属性
const activeMenu = computed(() => {
  const { meta, path } = route
  // 如果当前路由设置了activeMenu，则使用它
  if (meta.activeMenu) {
    return meta.activeMenu as string
  }
  return path
})

// 方法
const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleMenuSelect = (index: string) => {
  console.log('菜单选择:', index)
}

const handleUserCommand = (command: string) => {
  if (command === 'logout') {
    userStore.logout()
    router.push('/login')
  } else if (command === 'profile') {
    // 跳转到个人中心
    console.log('跳转到个人中心')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: white;
  transition: width 0.3s;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid #4a5b70;
}

.logo h2 {
  color: white;
  font-size: 18px;
  margin: 0;
}

.menu {
  border-right: none;
  height: calc(100% - 60px);
}

.menu :deep(.el-menu-item) {
  color: #bfcbd9;
}

.menu :deep(.el-menu-item:hover) {
  background-color: #2c3e50;
}

.menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff;
  color: white;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background-color: white;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
}

.username {
  margin-left: 10px;
  font-size: 14px;
  color: #333;
}

.main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>