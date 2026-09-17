<script setup lang="ts">
/**
 * 三栏外壳：顶栏 + 列表列 + 聊天列 + 可折叠信息栏。
 * 左列内容由路由 meta.list 决定；聊天页通过 provide('chatLayout') 控制信息栏开关。
 */
import { computed, onMounted, provide, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Conversation, Friend, PageResult } from '@/types'
import { useChatStore } from '@/stores/chat'
import { isGroupConversation } from '@/utils/conversation'
import TopNav from '@/components/layout/TopNav.vue'
import ConversationList from '@/components/conversation/ConversationList.vue'
import ContactList from '@/components/conversation/ContactList.vue'
import GroupList from '@/components/conversation/GroupList.vue'
import GroupInfoPanel from '@/components/chat/GroupInfoPanel.vue'
import PeerInfoPanel from '@/components/chat/PeerInfoPanel.vue'

const route = useRoute()
const router = useRouter()
const chatStore = useChatStore()

// ---- 信息栏开关：提供给聊天页（ChatView）复用 ----
const infoOpen = ref(true)
function toggleInfo(): void {
  infoOpen.value = !infoOpen.value
}
provide('chatLayout', { infoOpen, toggleInfo })

// ---- 从路由派生当前上下文 ----
const listType = computed(() => (route.meta.list as string) ?? 'none')
const activeGroupId = computed(() =>
  route.params.conversationId ? Number(route.params.conversationId) : null
)
const activePeerId = computed(() =>
  route.params.peerId ? Number(route.params.peerId) : null
)
const hasChatContext = computed(() => !!activeGroupId.value || !!activePeerId.value)
const activeConversation = computed(() => chatStore.activeConversation)
const isGroupChat = computed(
  () => !!activeGroupId.value || isGroupConversation(chatStore.activeConversation)
)
const peer = computed(
  () => chatStore.friends.list.find((f) => f.id === activePeerId.value) ?? null
)
/** 列表高亮键：加类型前缀，避免群会话ID与用户ID串号导致误高亮 */
const activeKey = computed(() =>
  activeGroupId.value
    ? `g:${activeGroupId.value}`
    : activePeerId.value
      ? `p:${activePeerId.value}`
      : ''
)

const showInfoPanel = computed(
  () => route.path.startsWith('/chat') && hasChatContext.value && infoOpen.value
)

// ---- 群成员（信息栏）：按当前群懒加载，服务端分页 ----
const members = ref<PageResult<Friend>>({ total: 0, page: 1, size: 50, list: [] })
const membersLoading = ref(false)

watch(
  [activeGroupId, showInfoPanel],
  async ([id, visible]) => {
    if (!id || !visible) {
      members.value = { total: 0, page: 1, size: 50, list: [] }
      return
    }
    membersLoading.value = true
    members.value = await chatStore.loadMembers(id)
    membersLoading.value = false
  },
  { immediate: true }
)

// ---- 邀请成员 ----
const showInviteDialog = ref(false)
const inviteSelection = ref<number[]>([])
const inviting = ref(false)

/** 邀请选项：排除已在群中的成员（基于当前分页成员的 list） */
const inviteOptions = computed(() => {
  const exists = new Set(members.value.list.map((m) => m.id))
  return chatStore.friends.list
    .filter((f) => !exists.has(f.id))
    .map((f) => ({ value: f.id, label: f.nickname || f.username || `用户${f.id}` }))
})

function openInvite(): void {
  inviteSelection.value = []
  showInviteDialog.value = true
}

async function submitInvite(): Promise<void> {
  const groupId = activeGroupId.value
  if (!groupId || !inviteSelection.value.length) {
    ElMessage.warning('请选择要邀请的好友')
    return
  }
  inviting.value = true
  try {
    const ok = await chatStore.inviteMembers(groupId, inviteSelection.value)
    if (ok) {
      showInviteDialog.value = false
      members.value = await chatStore.loadMembers(groupId)
    }
  } finally {
    inviting.value = false
  }
}

// ---- 列表交互 ----
function handleConversationSelect(conv: Conversation): void {
  if (isGroupConversation(conv)) {
    router.push(`/chat/group/${conv.conversationId}`)
  } else if (conv.peerId) {
    router.push(`/chat/p2p/${conv.peerId}`)
  }
}

function handleContactSelect(friend: Friend): void {
  router.push(`/chat/p2p/${friend.id}`)
}

function handleGroupSelect(conv: Conversation): void {
  router.push(`/chat/group/${conv.conversationId}`)
}

function handleGroupCreated(conversationId: number): void {
  router.push(`/chat/group/${conversationId}`)
}

async function handleQuitGroup(): Promise<void> {
  const groupId = activeGroupId.value
  if (!groupId) return
  const ok = await chatStore.quitGroupConversation(groupId)
  if (ok) router.push('/chat')
}

async function handleDeletePeer(): Promise<void> {
  const id = activePeerId.value
  if (!id) return
  await chatStore.deletePerson(id)
  router.push('/chat')
}

onMounted(() => {
  if (!chatStore.currentUser.id) {
    router.push('/login')
    return
  }
  void chatStore.loadConversations()
  void chatStore.getFriends()
})
</script>

<template>
  <div class="app-layout">
    <TopNav />

    <div class="app-layout__body" :class="{ 'has-info': showInfoPanel }">
      <aside class="app-layout__list">
        <ConversationList
          v-if="listType === 'conversations'"
          :active-key="activeKey"
          @select="handleConversationSelect"
        />
        <ContactList v-else-if="listType === 'contacts'" @select="handleContactSelect" />
        <GroupList
          v-else-if="listType === 'groups'"
          @select="handleGroupSelect"
          @created="handleGroupCreated"
        />
        <div v-else class="app-layout__side-title">设置</div>
      </aside>

      <main class="app-layout__main">
        <router-view />
      </main>

      <aside v-if="showInfoPanel" class="app-layout__info">
        <GroupInfoPanel
          v-if="isGroupChat"
          :conversation="activeConversation"
          :members="members"
          :loading="membersLoading"
          :self-id="chatStore.currentUser.id"
          @quit="handleQuitGroup"
          @invite="openInvite"
        />
        <PeerInfoPanel v-else :peer="peer" @delete="handleDeletePeer" />
      </aside>
    </div>

    <el-dialog v-model="showInviteDialog" title="邀请成员入群" width="420px">
      <el-select
        v-model="inviteSelection"
        multiple
        filterable
        placeholder="选择好友"
        class="app-layout__invite"
      >
        <el-option
          v-for="option in inviteOptions"
          :key="option.value"
          :label="option.label"
          :value="option.value"
        />
      </el-select>
      <p v-if="!inviteOptions.length" class="app-layout__invite-tip">没有可邀请的好友了</p>
      <template #footer>
        <el-button @click="showInviteDialog = false">取消</el-button>
        <el-button type="primary" :loading="inviting" @click="submitInvite">邀请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.app-layout {
  height: 100dvh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: var(--lc-bg);
}

/* 三栏：列表列固定宽，主内容自适应，信息栏可折叠 */
.app-layout__body {
  flex: 1;
  min-height: 0;
  display: grid;
  grid-template-columns: var(--lc-list-w) minmax(0, 1fr);
}

.app-layout__body.has-info {
  grid-template-columns: var(--lc-list-w) minmax(0, 1fr) var(--lc-info-w);
}

.app-layout__list {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background-color: var(--lc-surface);
  border-right: 1px solid var(--lc-border);
}

.app-layout__side-title {
  padding: 16px;
  font-size: 13px;
  color: var(--lc-text-3);
}

.app-layout__main {
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  background-color: var(--lc-surface-2);
}

.app-layout__info {
  min-height: 0;
  background-color: var(--lc-surface);
  border-left: 1px solid var(--lc-border);
}

.app-layout__invite {
  width: 100%;
}

.app-layout__invite-tip {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--lc-text-3);
}

/* 窄屏：优先保证聊天区宽度，信息栏折叠 */
@media (max-width: 1280px) {
  .app-layout__body.has-info {
    grid-template-columns: var(--lc-list-w) minmax(0, 1fr);
  }

  .app-layout__info {
    display: none;
  }
}

@media (max-width: 900px) {
  .app-layout__body,
  .app-layout__body.has-info {
    grid-template-columns: 200px minmax(0, 1fr);
  }
}
</style>