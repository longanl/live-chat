<script setup lang="ts">
/**
 * 统一聊天页：群聊（/chat/group/:conversationId）与私聊（/chat/p2p/:peerId）共用。
 *
 * 私聊在首次发言前没有会话ID，因此路由用「对方用户ID」定位；
 * 发出第一条消息后由后端懒建会话，并把带 conversationId 的消息推回来，
 * 本地会话列表随之补齐，conversationId 计算属性生效，消息自然出现。
 */
import { computed, inject, onBeforeUnmount, ref, watch } from 'vue'
import type { Ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadResult } from '@/types'
import { useChatStore } from '@/stores/chat'
import { useWebSocketStore } from '@/stores/websocket'
import { conversationTitle, isGroupConversation } from '@/utils/conversation'
import ChatHeader from '@/components/chat/ChatHeader.vue'
import MessageList from '@/components/chat/MessageList.vue'
import MessageInput from '@/components/chat/MessageInput.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const route = useRoute()
const chatStore = useChatStore()
const websocket = useWebSocketStore()

/** 信息栏开关由外层 AppLayout 提供 */
const layout = inject<{ infoOpen: Ref<boolean>; toggleInfo: () => void }>('chatLayout')
const infoOpen = computed(() => layout?.infoOpen.value ?? false)
function toggleInfo(): void {
  layout?.toggleInfo()
}

const groupId = computed(() =>
  route.params.conversationId ? Number(route.params.conversationId) : null
)
const peerId = computed(() => (route.params.peerId ? Number(route.params.peerId) : null))

/** 群聊直接用路由参数；私聊由「对方ID → 会话」映射得到（未聊过时为 null） */
const conversationId = computed<number | null>(() => {
  if (groupId.value) return groupId.value
  if (peerId.value) return chatStore.conversationByPeer(peerId.value)?.conversationId ?? null
  return null
})

const conversation = computed(() => chatStore.conversationOf(conversationId.value))
const isGroup = computed(() => !!groupId.value || isGroupConversation(conversation.value))

   const peer = computed(() => chatStore.friends.list.find((f) => f.id === peerId.value) ?? null)

/** 私聊对方是否为好友（须等好友列表加载完成后判定，避免把「尚未加载」误判为非好友） */
const isFriendPeer = computed(() => !!peer.value)

/** 非好友私聊：保留历史只读，禁止发送 */
const peerReadonly = computed(
  () => !!peerId.value && !isGroup.value && chatStore.friendsLoaded && !isFriendPeer.value
)

const title = computed(() => {
  if (isGroup.value) return conversationTitle(conversation.value) || '群聊'
  return peer.value?.nickname || conversation.value?.peerNickname || '私聊'
})

const avatarSrc = computed(() =>
  isGroup.value ? '' : peer.value?.avatar || conversation.value?.peerAvatar || ''
)

const online = computed(() => !isGroup.value && (peer.value?.status ?? 0) === 1)

const subtitle = computed(() => {
  if (isGroup.value) return `${conversation.value?.memberCount ?? 0} 名成员`
  return online.value ? '在线' : '离线'
})

const messages = computed(() => chatStore.messagesOf(conversationId.value))
const hasMore = computed(() => !chatStore.hasLoadedAll(conversationId.value))
const loadingMore = ref(false)

/** 已定位到会话但首屏历史尚未返回：此时显示加载中，而非「暂无消息」 */
const waitingHistory = computed(
  () =>
    !!conversationId.value &&
    !messages.value.length &&
    !chatStore.hasLoadedAll(conversationId.value)
)

/** 会话标识：变化时消息区直接定位到底部 */
const scrollKey = computed(() => conversationId.value ?? `peer-${peerId.value}`)

const emptyTitle = computed(() => (peerId.value && !conversationId.value ? '还没有聊过' : '暂无消息'))
const emptyDesc = computed(() => {
  if (peerReadonly.value) return '对方不是你的好友，无法发起对话'
  return peerId.value && !conversationId.value ? '发送第一条消息，开始你们的对话' : '来打个招呼吧'
})

/** 未选定会话、实时连接未就绪、或非好友私聊时禁止发送 */
const canSend = computed(
  () =>
    websocket.isConnected &&
    (isGroup.value ? !!groupId.value : !!peerId.value) &&
    !peerReadonly.value
)

watch(
  conversationId,
  async (id) => {
    if (!id) {
      chatStore.setActiveConversation(null)
      return
    }
    chatStore.setActiveConversation(id)
    await chatStore.loadConversation(id)
    await chatStore.markConversationRead(id)
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  // 离开聊天页时取消活跃会话，避免其它会话的新消息被误判为已读
  chatStore.setActiveConversation(null)
})

async function loadMore(): Promise<void> {
  const id = conversationId.value
  if (!id || loadingMore.value || chatStore.hasLoadedAll(id)) return
  loadingMore.value = true
  try {
    await chatStore.loadConversation(id, true)
  } finally {
    loadingMore.value = false
  }
}

function sendPayload(payload: Record<string, unknown>): void {
  if (peerReadonly.value) {
    ElMessage.warning('对方不是你的好友，无法发送消息')
    return
  }
  if (!canSend.value) {
    ElMessage.warning('实时连接未就绪，请稍后再试')
    return
  }
  if (isGroup.value) {
    websocket.seedMessage('group', { ...payload, conversationId: groupId.value })
  } else {
    websocket.seedMessage('p2pchat', { ...payload, receiverId: peerId.value })
  }
}

function sendText(content: string): void {
  sendPayload({ content })
}

function sendFile(file: UploadResult): void {
  sendPayload({
    messageType: 2,
    fileUrl: file.url,
    fileName: file.name,
    fileSize: file.size,
    fileType: file.type
  })
}
</script>

<template>
  <div v-if="!groupId && !peerId" class="chat-view chat-view--placeholder">
    <EmptyState
      icon="ChatDotRound"
      title="开始聊天"
      description="从左侧选择群聊或联系人，即可开始对话"
    />
  </div>

  <div v-else class="chat-view">
    <ChatHeader
      :title="title"
      :subtitle="subtitle"
      :avatar-src="avatarSrc"
      :group="isGroup"
      :show-status-dot="!isGroup"
      :online="online"
      :info-open="infoOpen"
      @toggle-info="toggleInfo"
    />

    <div v-if="!websocket.isConnected" class="chat-view__notice">
      <el-icon><WarningFilled /></el-icon>
      实时连接未就绪，消息可能无法即时送达
    </div>

    <div v-if="peerReadonly" class="chat-view__notice">
      <el-icon><Lock /></el-icon>
      对方不是你的好友，仅可查看历史消息
    </div>

    <MessageList
      :messages="messages"
      :self-id="chatStore.currentUser.id"
      :has-more="hasMore"
      :loading="loadingMore"
      :show-nickname="isGroup"
      :scroll-key="scrollKey"
      @load-more="loadMore"
    >
      <template #empty>
        <div v-if="waitingHistory" class="chat-view__loading">
          <el-icon class="chat-view__loading-icon"><Loading /></el-icon>
          正在加载消息…
        </div>
        <EmptyState v-else icon="ChatDotRound" :title="emptyTitle" :description="emptyDesc" />
      </template>
    </MessageList>

    <MessageInput :disabled="!canSend" @send="sendText" @send-file="sendFile" />
  </div>
</template>

<style scoped>
.chat-view {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.chat-view--placeholder {
  align-items: center;
  justify-content: center;
  background-color: var(--lc-surface);
}

.chat-view__notice {
  flex-shrink: 0;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  font-size: 12px;
  color: #a8700a;
  background-color: #fff7e6;
  border-bottom: 1px solid #ffe0a3;
}

.chat-view__loading {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  height: 100%;
  min-height: 160px;
  font-size: 13px;
  color: var(--lc-text-3);
}

.chat-view__loading-icon {
  animation: chat-view-spin 1s linear infinite;
}

@keyframes chat-view-spin {
  to {
    transform: rotate(360deg);
  }
}
</style>