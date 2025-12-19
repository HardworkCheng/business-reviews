import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 引入自定义主题样式
import './styles/variables.scss'
import './styles/common.scss'
import './styles/element-plus.scss'

// 应用启动时清除登录状态，强制用户重新登录
localStorage.removeItem('merchant_token')
console.log('[App] 已清除登录状态，需要重新登录')

const app = createApp(App)

// 注册所有Element Plus图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(createPinia())
app.use(router)
app.use(ElementPlus)

app.mount('#app')
