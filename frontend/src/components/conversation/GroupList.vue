<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Conversation } from '@/types'
import { useChatStore } from '@/stores/chat'
import UserAvatar from '@/components/common/UserAvatar.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import CreateGroupDialog from '@/components/conversation/CreateGroupDialog.vue'

const emit = defineEmits<{
  (e: 'select', conversation: Conversation): void
  (e: 'created', conversationId: number): void
}>()

const chatStore = useChatStore()
const keyword = ref('')
const showCreateDialog = ref(false)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return chatStore.groupConversations
  return chatStore.groupConversations.filter((c) => (c.name ?? '').toLowerCase().includes(kw))
})

function handleCreated(conversationId: number): void {
  emit('created', conversationId)
}
</script>

<template>
  <div class="group-list">
    <div class="group-list__top">
      <el-input v-model="keyword" placeholder="搜索群聊" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-button type="primary" class="group-list__create" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建群聊
      </el-button>
    </div>

    <div class="group-list__body">
      <ul v-if="filtered.length" class="group-list__items">
        <li
          v-for="group in filtered"
          :key="group.conversationId"
          class="group-item"
          @click="emit('select', group)"
        >
          <UserAvatar :name="group.name" :size="44" shape="square" />
          <div class="group-item__main">
            <span class="group-item__name lc-ellipsis">{{ group.name || '未命名群聊' }}</span>
            <span class="group-item__sub lc-ellipsis">{{ group.memberCount ?? 0 }} 名成员</span>
          </div>
        </li>
      </ul>

      <EmptyState
        v-else
        icon="UserFilled"
        :title="keyword ? '没有匹配的群聊' : '还没有群聊'"
        description="创建一个群，把好友拉进来一起聊"
      />
    </div>

    <CreateGroupDialog v-model="showCreateDialog" @created="handleCreated" />
  </div>
</template>

<style scoped>
.group-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.group-list__top {
  padding: 12px 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.group-list__create {
  width: 100%;
}

.group-list__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.group-list__items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.group-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: var(--lc-radius);
  cursor: pointer;
}

.group-item:hover {
  background-color: var(--lc-surface-2);
}

.group-item__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.group-item__name {
  font-size: 14px;
  color: var(--lc-text);
}

.group-item__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}
</style>
