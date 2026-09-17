<script setup lang="ts">
/** 聊天头部：标题 + 副标题（在线/成员信息） + 信息栏开关 */
import UserAvatar from '@/components/common/UserAvatar.vue'

withDefaults(
  defineProps<{
    title?: string
    subtitle?: string
    avatarSrc?: string | null
    /** 是否群聊（群聊头像走首字母兜底，形状为方形） */
    group?: boolean
    showStatusDot?: boolean
    online?: boolean
    infoOpen?: boolean
  }>(),
  {
    title: '',
    subtitle: '',
    avatarSrc: '',
    group: false,
    showStatusDot: false,
    online: false,
    infoOpen: false
  }
)

const emit = defineEmits<{ (e: 'toggle-info'): void }>()
</script>

<template>
  <header class="chat-header">
    <div class="chat-header__main">
      <UserAvatar
        :src="avatarSrc"
        :name="title"
        :size="38"
        :shape="group ? 'square' : 'circle'"
        :show-status="showStatusDot"
        :status="online ? 1 : 0"
      />
      <div class="chat-header__text">
        <div class="chat-header__title lc-ellipsis">{{ title }}</div>
        <div class="chat-header__subtitle lc-ellipsis">{{ subtitle }}</div>
      </div>
    </div>

    <div class="chat-header__actions">
      <slot name="actions" />
      <el-tooltip :content="infoOpen ? '收起信息栏' : '展开信息栏'" placement="bottom">
        <button class="chat-header__icon-btn" type="button" @click="emit('toggle-info')">
          <el-icon><InfoFilled /></el-icon>
        </button>
      </el-tooltip>
    </div>
  </header>
</template>

<style scoped>
.chat-header {
  flex-shrink: 0;
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  background-color: var(--lc-surface);
  border-bottom: 1px solid var(--lc-border);
}

.chat-header__main {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.chat-header__text {
  min-width: 0;
}

.chat-header__title {
  font-size: 15px;
  font-weight: 600;
  color: var(--lc-text);
}

.chat-header__subtitle {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-top: 2px;
}

.chat-header__actions {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.chat-header__icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  border: none;
  border-radius: var(--lc-radius-sm);
  background: transparent;
  color: var(--lc-text-2);
  font-size: 17px;
  cursor: pointer;
}

.chat-header__icon-btn:hover {
  background-color: var(--lc-surface-2);
  color: var(--lc-primary);
}
</style>