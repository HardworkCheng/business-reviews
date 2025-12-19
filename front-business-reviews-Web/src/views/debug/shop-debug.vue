<template>
  <div class="debug-page">
    <h1>店铺数据调试页面</h1>
    
    <div class="debug-section">
      <h2>系统状态</h2>
      <div class="status-grid">
        <div class="status-item">
          <label>页面加载时间:</label>
          <span>{{ loadTime }}</span>
        </div>
        <div class="status-item">
          <label>API基础URL:</label>
          <span>{{ apiBaseUrl }}</span>
        </div>
        <div class="status-item">
          <label>认证Token:</label>
          <span>{{ hasToken ? '已设置' : '未设置' }}</span>
        </div>
      </div>
    </div>

    <div class="debug-section">
      <h2>数据加载测试</h2>
      <el-button @click="testApiConnection" :loading="testing">测试API连接</el-button>
      <el-button @click="testShopData" :loading="loadingShop">测试店铺数据</el-button>
      <el-button @click="clearAllCache">清除所有缓存</el-button>
    </div>

    <div class="debug-section" v-if="apiResult">
      <h2>API测试结果</h2>
      <pre class="api-result">{{ JSON.stringify(apiResult, null, 2) }}</pre>
    </div>

    <div class="debug-section" v-if="shopData">
      <h2>店铺数据</h2>
      <pre class="shop-data">{{ JSON.stringify(shopData, null, 2) }}</pre>
    </div>

    <div class="debug-section">
      <h2>控制台日志</h2>
      <div class="console-logs">
        <div v-for="(log, index) in consoleLogs" :key="index" class="log-item" :class="log.type">
          <span class="log-time">{{ log.time }}</span>
          <span class="log-message">{{ log.message }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getShopList } from '@/api/shop'

const loadTime = ref('')
const apiBaseUrl = ref('/api')
const hasToken = ref(false)
const testing = ref(false)
const loadingShop = ref(false)
const apiResult = ref(null)
const shopData = ref(null)
const consoleLogs = ref<Array<{time: string, message: string, type: string}>>([])

// 拦截console.log
const originalLog = console.log
const originalError = console.error
const originalWarn = console.warn

const addLog = (message: string, type: string) => {
  consoleLogs.value.push({
    time: new Date().toLocaleTimeString(),
    message: typeof message === 'object' ? JSON.stringify(message) : String(message),
    type
  })
  // 保持最新50条日志
  if (consoleLogs.value.length > 50) {
    consoleLogs.value.shift()
  }
}

console.log = (...args) => {
  originalLog(...args)
  addLog(args.join(' '), 'log')
}

console.error = (...args) => {
  originalError(...args)
  addLog(args.join(' '), 'error')
}

console.warn = (...args) => {
  originalWarn(...args)
  addLog(args.join(' '), 'warn')
}

const testApiConnection = async () => {
  testing.value = true
  try {
    const response = await fetch('/api/merchant/shops?pageNum=1&pageSize=1', {
      headers: {
        'Authorization': `Bearer ${localStorage.getItem('merchant_token')}`,
        'Content-Type': 'application/json'
      }
    })
    
    const result = await response.json()
    apiResult.value = {
      status: response.status,
      statusText: response.statusText,
      headers: Object.fromEntries(response.headers.entries()),
      data: result
    }
    
    if (response.ok) {
      ElMessage.success('API连接测试成功')
    } else {
      ElMessage.error(`API连接失败: ${response.status}`)
    }
  } catch (error: any) {
    apiResult.value = {
      error: error.message,
      stack: error.stack
    }
    ElMessage.error('API连接测试失败')
  } finally {
    testing.value = false
  }
}

const testShopData = async () => {
  loadingShop.value = true
  try {
    const result = await getShopList({ pageNum: 1, pageSize: 1 })
    shopData.value = result
    ElMessage.success('店铺数据获取成功')
  } catch (error: any) {
    shopData.value = {
      error: error.message,
      stack: error.stack
    }
    ElMessage.error('店铺数据获取失败')
  } finally {
    loadingShop.value = false
  }
}

const clearAllCache = () => {
  // 清除localStorage
  const keys = Object.keys(localStorage)
  keys.forEach(key => {
    if (key.includes('shop') || key.includes('merchant')) {
      if (!key.includes('token')) {
        localStorage.removeItem(key)
      }
    }
  })
  
  // 清除sessionStorage
  sessionStorage.clear()
  
  // 清除浏览器缓存
  if ('caches' in window) {
    caches.keys().then(names => {
      names.forEach(name => caches.delete(name))
    })
  }
  
  ElMessage.success('缓存已清除')
}

onMounted(() => {
  loadTime.value = new Date().toLocaleString()
  hasToken.value = !!localStorage.getItem('merchant_token')
  
  console.log('🔧 调试页面已加载')
  console.log('📊 系统信息:', {
    userAgent: navigator.userAgent,
    url: window.location.href,
    localStorage: Object.keys(localStorage),
    sessionStorage: Object.keys(sessionStorage)
  })
})
</script>

<style scoped>
.debug-page {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.debug-section {
  margin-bottom: 30px;
  padding: 20px;
  border: 1px solid #ddd;
  border-radius: 8px;
  background: #f9f9f9;
}

.status-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 10px;
}

.status-item {
  display: flex;
  justify-content: space-between;
  padding: 8px;
  background: white;
  border-radius: 4px;
}

.api-result, .shop-data {
  background: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  overflow-x: auto;
  max-height: 400px;
}

.console-logs {
  max-height: 300px;
  overflow-y: auto;
  background: #1e1e1e;
  color: #fff;
  padding: 10px;
  border-radius: 4px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
}

.log-item {
  margin-bottom: 5px;
  display: flex;
  gap: 10px;
}

.log-item.error {
  color: #ff6b6b;
}

.log-item.warn {
  color: #ffd93d;
}

.log-item.log {
  color: #6bcf7f;
}

.log-time {
  color: #888;
  min-width: 80px;
}

.log-message {
  flex: 1;
  word-break: break-all;
}
</style>