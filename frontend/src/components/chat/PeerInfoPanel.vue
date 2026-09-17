<script setup lang="ts">
/** 私聊信息栏：对方资料 + 操作 */
import { computed } from 'vue'
import type { Friend } from '@/types'
import UserAvatar from '@/components/common/UserAvatar.vue'

const props = withDefaults(
  defineProps<{
    peer?: Friend | null
  }>(),
  { peer: null }
)

const emit = defineEmits<{ (e: 'delete'): void }>()

const online = computed(() => props.peer?.status === 1)
</script>

<template>
  <div class="peer-panel">
    <div class="peer-panel__profile">
      <UserAvatar
        :src="peer?.avatar"
        :name="peer?.nickname || peer?.username"
        :size="64"
        show-status
        :status="peer?.status ?? 0"
      />
      <div class="peer-panel__name">{{ peer?.nickname || peer?.username || '未知用户' }}</div>
      <div class="peer-panel__sub" :class="{ 'is-online': online }">
        {{ online ? '在线' : '离线' }}
      </div>
    </div>

    <div class="peer-panel__grid">
      <div class="peer-panel__cell">
        <span class="peer-panel__label">用户名</span>
        <span class="peer-panel__value lc-ellipsis">{{ peer?.username || '—' }}</span>
      </div>
      <div class="peer-panel__cell">
        <span class="peer-panel__label">昵称</span>
        <span class="peer-panel__value lc-ellipsis">{{ peer?.nickname || '—' }}</span>
      </div>
    </div>

    <el-button class="peer-panel__danger" @click="emit('delete')">
      <el-icon><Delete /></el-icon>
      删除好友
    </el-button>

    <p class="peer-panel__tip">删除后将从联系人列表中移除，历史消息不会删除。</p>
  </div>
</template>

<style scoped>
.peer-panel {
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 16px;
}

.peer-panel__profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--lc-border);
}

.peer-panel__name {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  word-break: break-all;
}

.peer-panel__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.peer-panel__sub.is-online {
  color: var(--lc-success);
}

.peer-panel__grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 14px 0;
}

.peer-panel__cell {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 10px;
  background-color: var(--lc-surface-2);
  border-radius: var(--lc-radius-sm);
}

.peer-panel__label {
  font-size: 12px;
  color: var(--lc-text-3);
  flex-shrink: 0;
}

.peer-panel__value {
  font-size: 13px;
  color: var(--lc-text);
  min-width: 0;
}

.peer-panel__danger {
  width: 100%;
  color: var(--lc-danger);
  border-color: var(--lc-border-strong);
}

.peer-panel__danger:hover {
  color: #fff;
  background-color: var(--lc-danger);
  border-color: var(--lc-danger);
}

.peer-panel__tip {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--lc-text-3);
  line-height: 1.6;
}
</style>