import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { useWebSocketStore } from '@/stores/websocket'

import App from './App.vue'
import router from './router'

// Element Plus 基础样式（CSS 变量 + reset）：按需组件样式由 unplugin 自动引入
import 'element-plus/theme-chalk/base.css'
// ElMessage / ElMessageBox 是主动调用的 API，样式需手动引入
import 'element-plus/es/components/message/style/css'
import 'element-plus/es/components/message-box/style/css'
// 按需注册用到的图标（全量注册会打包全部 400+ 图标）
import {
  ArrowDown,
  ArrowDownBold,
  Bell,
  ChatDotRound,
  ChatLineRound,
  Delete,
  Document,
  InfoFilled,
  Loading,
  Lock,
  Paperclip,
  Picture,
  Plus,
  Search,
  Setting,
  Sunny,
  SwitchButton,
  User,
  UserFilled,
  WarningFilled
} from '@element-plus/icons-vue'

// 导入全局样式（设计 token + 基础样式）
import './styles/main.css'

// 导入全局组件
const app = createApp(App)

// 初始化 Pinia
app.use(createPinia())

/**
 * 已登录则建立 WebSocket 连接（应用冷启动与登录成功后共用）
 * 说明：登录页登录成功后也会显式调用该 Store 方法建连，
 * 因此不再依赖「整页刷新」来触发 main.ts 的冷启动逻辑。
 */
const bootstrapWebSocket = () => {
  const user = JSON.parse(localStorage.getItem('user') ?? 'null');
  if (!user || !user.id) {
    console.warn('用户未登录，无法初始化 WebSocket');
    return;
  }
  console.log('用户已登录，正在初始化 WebSocket');
  useWebSocketStore().initWebSocket(user.id);
};

bootstrapWebSocket();

// 注册路由
app.use(router)

// 注册用到的 element-plus 图标（供 <component :is="icon" /> 与模板直接引用）
const appIcons = {
  ArrowDown,
  ArrowDownBold,
  Bell,
  ChatDotRound,
  ChatLineRound,
  Delete,
  Document,
  InfoFilled,
  Loading,
  Lock,
  Paperclip,
  Picture,
  Plus,
  Search,
  Setting,
  Sunny,
  SwitchButton,
  User,
  UserFilled,
  WarningFilled
}
for (const [key, component] of Object.entries(appIcons)) {
  app.component(key, component)
}

// 挂载app
app.mount('#app')


