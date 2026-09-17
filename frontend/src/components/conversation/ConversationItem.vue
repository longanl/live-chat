<script setup lang="ts">
import { computed } from 'vue'
import type { Conversation } from '@/types'
import {
  conversationAvatar,
  conversationPreview,
  conversationTitle,
  isGroupConversation
} from '@/utils/conversation'
import { formatListTime } from '@/utils/time'
import UserAvatar from '@/components/common/UserAvatar.vue'

const props = withDefaults(
  defineProps<{
    conversation: Conversation
    active?: boolean
    unread?: number
  }>(),
  { active: false, unread: 0 }
)

const emit = defineEmits<{ (e: 'select', conversation: Conversation): void }>()

const group = computed(() => isGroupConversation(props.conversation))
const title = computed(() => conversationTitle(props.conversation))
const preview = computed(() => conversationPreview(props.conversation))
const avatar = computed(() => conversationAvatar(props.conversation))
const status = computed(() => (group.value ? 0 : props.conversation.peerStatus ?? 0))
</script>

<template>
  <li
    class="conv-item"
    :class="{ 'conv-item--active': active }"
    @click="emit('select', conversation)"
  >
    <UserAvatar
      :src="avatar"
      :name="title"
      :size="44"
      :shape="group ? 'square' : 'circle'"
      :show-status="!group"
      :status="status"
    />
    <div class="conv-item__main">
      <div class="conv-item__row">
        <span class="conv-item__title lc-ellipsis">{{ title }}</span>
        <span class="conv-item__time">{{ formatListTime(conversation.lastSendTime || undefined) }}</span>
      </div>
      <div class="conv-item__row">
        <span class="conv-item__preview lc-ellipsis">{{ preview }}</span>
        <span v-if="unread > 0" class="conv-item__badge">{{ unread > 99 ? '99+' : unread }}</span>
      </div>
    </div>
  </li>
</template>

<style scoped>
.conv-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  cursor: pointer;
  border-radius: var(--lc-radius);
  transition: background-color 0.15s ease;
}

.conv-item:hover {
  background-color: var(--lc-surface-2);
}

.conv-item--active {
  background-color: var(--lc-primary-soft);
}

.conv-item__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.conv-item__row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  min-width: 0;
}

.conv-item__title {
  font-size: 14px;
  font-weight: 500;
  color: var(--lc-text);
  min-width: 0;
}

.conv-item__time {
  font-size: 11px;
  color: var(--lc-text-3);
  flex-shrink: 0;
}

.conv-item__preview {
  font-size: 12px;
  color: var(--lc-text-3);
  min-width: 0;
  flex: 1;
}

.conv-item__badge {
  flex-shrink: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background-color: var(--lc-danger);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}
</style>