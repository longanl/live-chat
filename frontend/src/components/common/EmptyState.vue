<script setup lang="ts">
/** 统一空态：列表/聊天区无数据时使用，避免各页各写一段"暂无数据" */
withDefaults(
  defineProps<{
    /** 图标（Element Plus 图标组件名，如 ChatDotRound） */
    icon?: string
    title?: string
    description?: string
  }>(),
  {
    icon: 'ChatDotRound',
    title: '暂无数据',
    description: ''
  }
)
</script>

<template>
  <div class="lc-empty">
    <el-icon class="lc-empty__icon"><component :is="icon" /></el-icon>
    <p class="lc-empty__title">{{ title }}</p>
    <p v-if="description" class="lc-empty__desc">{{ description }}</p>
    <slot />
  </div>
</template>

<style scoped>
.lc-empty {
  height: 100%;
  min-height: 160px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 24px;
  color: var(--lc-text-3);
  text-align: center;
}

.lc-empty__icon {
  font-size: 40px;
  color: var(--lc-border-strong);
}

.lc-empty__title {
  margin: 0;
  font-size: 14px;
  color: var(--lc-text-2);
}

.lc-empty__desc {
  margin: 0;
  font-size: 12px;
  color: var(--lc-text-3);
}
</style>