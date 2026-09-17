<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Conversation } from '@/types'
import { useChatStore } from '@/stores/chat'
import { isGroupConversation } from '@/utils/conversation'
import ConversationItem from '@/components/conversation/ConversationItem.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const props = withDefaults(
  defineProps<{
    /** 当前会话标识，用于高亮（群聊为 g:{会话ID}，私聊为 p:{对方ID}） */
    activeKey?: string
  }>(),
  { activeKey: '' }
)

const emit = defineEmits<{ (e: 'select', conversation: Conversation): void }>()

const chatStore = useChatStore()
const keyword = ref('')
const currentPage = ref(1)
const pageSize = ref(20)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return chatStore.conversations.list
  return chatStore.conversations.list.filter((c) => {
    const peer = `${c.peerNickname ?? ''} ${c.name ?? ''}`.toLowerCase()
    return peer.includes(kw) || (c.lastContent ?? '').toLowerCase().includes(kw)
  })
})

const groups = computed(() => filtered.value.filter((c) => isGroupConversation(c)))
const peers = computed(() => filtered.value.filter((c) => !isGroupConversation(c)))

/** 高亮判定：群聊按 g:{会话ID}，私聊按 p:{对方ID}，前缀区分两个 ID 空间 */
function isActive(conv: Conversation): boolean {
  return isGroupConversation(conv)
    ? props.activeKey === `g:${conv.conversationId}`
    : props.activeKey === `p:${conv.peerId}`
}

function onPageChange(page: number, size: number): void {
  currentPage.value = page
  pageSize.value = size
  chatStore.loadConversations(page, size)
}

// 同步 store 分页状态到本地控件
watch(
  () => [chatStore.conversations.page, chatStore.conversations.size],
  ([p, s]) => {
    currentPage.value = p
    pageSize.value = s
  }
)
</script>

<template>
  <div class="conv-list">
    <div class="conv-list__search">
      <el-input v-model="keyword" placeholder="搜索会话" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
    </div>

    <div class="conv-list__body">
      <template v-if="filtered.length">
        <div v-if="groups.length" class="conv-list__group">
          <div class="conv-list__group-title">群聊（{{ groups.length }}）</div>
          <ul class="conv-list__items">
            <ConversationItem
              v-for="conv in groups"
              :key="`g-${conv.conversationId}`"
              :conversation="conv"
              :active="isActive(conv)"
              :unread="chatStore.unreadOf(conv.conversationId)"
              @select="emit('select', $event)"
            />
          </ul>
        </div>

        <div v-if="peers.length" class="conv-list__group">
          <div class="conv-list__group-title">私聊（{{ peers.length }}）</div>
          <ul class="conv-list__items">
            <ConversationItem
              v-for="conv in peers"
              :key="`p-${conv.conversationId}`"
              :conversation="conv"
              :active="isActive(conv)"
              :unread="chatStore.unreadOf(conv.conversationId)"
              @select="emit('select', $event)"
            />
          </ul>
        </div>
      </template>

      <EmptyState
        v-else
        icon="ChatDotRound"
        :title="keyword ? '没有匹配的会话' : '还没有会话'"
        :description="keyword ? '换个关键词试试' : '去联系人页开始一段对话吧'"
      />
    </div>

    <div class="conv-list__foot">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="chatStore.conversations.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        small
        @current-change="(p) => { currentPage = p; chatStore.loadConversations(p, pageSize) }"
        @size-change="(s) => { pageSize = s; chatStore.loadConversations(currentPage, s) }"
      />
    </div>
  </div>
</template>

<style scoped>
.conv-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.conv-list__search {
  padding: 12px 12px 8px;
}

.conv-list__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.conv-list__group {
  padding-top: 6px;
}

.conv-list__group-title {
  font-size: 12px;
  color: var(--lc-text-3);
  padding: 6px 8px;
}

.conv-list__items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.conv-list__foot {
  padding: 8px;
  border-top: 1px solid var(--lc-border);
  background-color: var(--lc-surface);
}
</style>