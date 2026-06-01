import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useWebSocketStore } from '@/utils/websocket.js'

import App from './App.vue'
import router from './router'

import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入全局样式
import './assets/main.css'

// 导入全局组件
const app = createApp(App)

// 初始化 Pinia
app.use(createPinia())
// 获取用户 ID
const getUser = () => {
  const userStr = localStorage.getItem('user');
  if (userStr) {
    return JSON.parse(userStr);
  }
  return null;
};
const user = getUser();
const userId = user ? user.id : null;

// 初始化 WebSocket
if (userId) {
  console.log('用户已登录，正在初始化 WebSocket');
  const websocket = useWebSocketStore();
  websocket.initWebSocket(userId);
} else {
  console.warn('用户未登录，无法初始化 WebSocket');
}
// 注册路由
app.use(router)
// 注册element-plus
app.use(ElementPlus, {locale: zhCn})
// 注册element-plus的icons
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}
// 挂载app
app.mount('#app')


