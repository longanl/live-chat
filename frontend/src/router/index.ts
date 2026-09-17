import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'
import AppLayout from '@/layouts/AppLayout.vue'
import ChatView from '@/views/ChatView.vue'
import ContactView from '@/views/ContactView.vue'
import GroupView from '@/views/GroupView.vue'
import SettingsView from '@/views/SettingsView.vue'
import LoginView from '@/views/auth/LoginView.vue'
import RegisterView from '@/views/auth/RegisterView.vue'
import { useWebSocketStore } from '@/stores/websocket'

/**
 * 路由结构：
 * - /login /register：免鉴权；
 * - 其余页面挂在 AppLayout 下，左列内容由 meta.list 声明（conversations/contacts/groups/none）；
 * - 聊天页统一为 /chat/*，群聊用会话ID、私聊用对方用户ID（首次发言前尚无会话ID，用它能刷新恢复）。
 */
const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: LoginView, meta: { public: true } },
  { path: '/register', name: 'register', component: RegisterView, meta: { public: true } },
  {
    path: '/',
    component: AppLayout,
    redirect: '/chat',
    children: [
      { path: 'chat', name: 'chat', component: ChatView, meta: { list: 'conversations' } },
      {
        path: 'chat/group/:conversationId',
        name: 'chat-group',
        component: ChatView,
        meta: { list: 'conversations' }
      },
      {
        path: 'chat/p2p/:peerId',
        name: 'chat-p2p',
        component: ChatView,
        meta: { list: 'conversations' }
      },
      { path: 'contacts', name: 'contacts', component: ContactView, meta: { list: 'contacts' } },
      { path: 'groups', name: 'groups', component: GroupView, meta: { list: 'groups' } },
      { path: 'settings', name: 'settings', component: SettingsView, meta: { list: 'none' } }
    ]
  },
  // 旧路由兼容：老版本页面路径重定向到新结构，避免收藏链接 404
  { path: '/index', redirect: '/chat/group/1' },
  { path: '/contact', redirect: '/contacts' },
  { path: '/p2pchat', redirect: '/chat' },
  { path: '/:catchAll(.*)', redirect: '/login' }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

router.beforeEach((to) => {
  const user = JSON.parse(localStorage.getItem('user') ?? 'null')
  // 登录 / 注册页无需鉴权
  if (to.meta.public === true) return true
  // 未登录（含 user 为空对象、id 缺失）一律回到登录页
  if (!user || !user.id) return { path: '/login' }
  // 已登录：兜底建立 WebSocket（刷新、直接输入 URL 等入口）
  const websocket = useWebSocketStore()
  if (!websocket.isConnected) {
    websocket.initWebSocket(user.id)
  }
  return true
})

export default router
