<script setup lang="ts">
/**
 * 顶栏：品牌 + 主导航 + 在线人数 + 用户菜单。
 * 取代旧 layout.vue 中把裸 div 塞进 el-menu 的写法，用户区天然右对齐。
 */
import { computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { useWebSocketStore } from '@/stores/websocket'
import { logoutApi } from '@/api/users'
import UserAvatar from '@/components/common/UserAvatar.vue'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()
const websocket = useWebSocketStore()

const user = computed(() => chatStore.currentUser)
const onlineCount = computed(() => websocket.onlineCount)
const pendingRequests = computed(() => chatStore.pendingRequests)

const navItems = [
  { path: '/chat', label: '首页', icon: 'ChatDotRound' },
  { path: '/contacts', label: '联系人', icon: 'User' },
  { path: '/groups', label: '群组', icon: 'UserFilled' },
  { path: '/settings', label: '设置', icon: 'Setting' }
]

// 顶栏「联系人」红点：待处理好友申请数
onMounted(() => {
  void chatStore.loadPendingRequests()
})
watch(
  () => route.path,
  () => {
    void chatStore.loadPendingRequests()
  }
)

/** 首页需匹配 /chat 及其子路径，其余前缀匹配即可 */
function isActive(path: string): boolean {
  return path === '/chat' ? route.path.startsWith('/chat') : route.path.startsWith(path)
}

async function handleCommand(command: string): Promise<void> {
  if (command === 'profile') {
    router.push('/settings')
    return
  }
  if (command !== 'logout') return
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch {
    return
  }
  try {
    await logoutApi()
  } catch (error) {
    console.error('退出登录接口异常', error)
  }
  localStorage.removeItem('user')
  websocket.closeWebSocket()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<template>
  <header class="top-nav">
    <div class="top-nav__left">
      <router-link to="/chat" class="top-nav__brand">
        <span class="top-nav__logo"><el-icon><ChatDotRound /></el-icon></span>
        <span class="top-nav__name">Live-Chat</span>
      </router-link>

      <nav class="top-nav__menu">
        <router-link
          v-for="item in navItems"
          :key="item.path"
          :to="item.path"
          class="top-nav__link"
          :class="{ 'is-active': isActive(item.path) }"
        >
          <el-icon>
            <el-badge
              v-if="item.path === '/contacts'"
              :value="pendingRequests.length"
              :hidden="!pendingRequests.length"
              :offset="[6, -4]"
            >
              <component :is="item.icon" />
            </el-badge>
            <component v-else :is="item.icon" />
          </el-icon>
          <span>{{ item.label }}</span>
        </router-link>
      </nav>
    </div>

    <div class="top-nav__right">
      <el-tooltip content="当前在线人数" placement="bottom">
        <span class="top-nav__online">
          <span class="top-nav__online-dot" />
          {{ onlineCount }} 人在线
        </span>
      </el-tooltip>

      <el-dropdown trigger="click" @command="handleCommand">
        <span class="top-nav__user">
          <UserAvatar :src="user.avatar" :name="user.nickname || user.username" :size="30" />
          <span class="top-nav__user-name lc-ellipsis">{{ user.nickname || user.username }}</span>
          <el-icon><ArrowDown /></el-icon>
        </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>个人资料
            </el-dropdown-item>
            <el-dropdown-item command="logout" divided>
              <el-icon><SwitchButton /></el-icon>退出登录
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </header>
</template>

<style scoped>
.top-nav {
  flex-shrink: 0;
  height: var(--lc-header-h);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 18px;
  background-color: var(--lc-surface);
  border-bottom: 1px solid var(--lc-border);
}

.top-nav__left {
  display: flex;
  align-items: center;
  gap: 26px;
  min-width: 0;
}

.top-nav__brand {
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: var(--lc-text);
}

.top-nav__logo {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 30px;
  height: 30px;
  border-radius: var(--lc-radius-sm);
  background-color: var(--lc-primary);
  color: #fff;
  font-size: 18px;
}

.top-nav__name {
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.2px;
}

.top-nav__menu {
  display: flex;
  align-items: center;
  gap: 4px;
}

.top-nav__link {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 7px 12px;
  border-radius: var(--lc-radius-sm);
  font-size: 14px;
  color: var(--lc-text-2);
  text-decoration: none;
}

.top-nav__link:hover {
  background-color: var(--lc-surface-2);
  color: var(--lc-text);
}

.top-nav__link.is-active {
  background-color: var(--lc-primary-soft);
  color: var(--lc-primary);
  font-weight: 600;
}

.top-nav__right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.top-nav__online {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--lc-text-2);
}

.top-nav__online-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background-color: var(--lc-success);
}

.top-nav__user {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px 4px 4px;
  border-radius: 999px;
  cursor: pointer;
  outline: none;
}

.top-nav__user:hover {
  background-color: var(--lc-surface-2);
}

.top-nav__user-name {
  max-width: 120px;
  font-size: 13px;
  color: var(--lc-text);
}

@media (max-width: 900px) {
  .top-nav__name,
  .top-nav__online,
  .top-nav__user-name {
    display: none;
  }
}
</style>
