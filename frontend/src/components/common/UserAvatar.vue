<script setup lang="ts">
/**
 * 统一头像：图片优先，缺失时用「首字符 + 稳定色」兜底；
 * 可选右下角在线状态小点。所有列表/气泡/资料卡都用它，避免各页各写一套。
 */
import { computed } from 'vue'
import { avatarColor, avatarText } from '@/utils/avatar'

const props = withDefaults(
  defineProps<{
    /** 头像地址 */
    src?: string | null
    /** 用于生成兜底字符与颜色（昵称/群名） */
    name?: string | null
    /** 尺寸（px） */
    size?: number
    /** 是否显示在线状态点 */
    showStatus?: boolean
    /** 在线状态：1-在线 0-离线 */
    status?: number | null
    /** 圆角形状 */
    shape?: 'circle' | 'square'
  }>(),
  {
    src: '',
    name: '',
    size: 40,
    showStatus: false,
    status: 0,
    shape: 'circle'
  }
)

const text = computed(() => avatarText(props.name ?? ''))
const color = computed(() => avatarColor(props.name ?? ''))
const boxStyle = computed(() => ({
  width: `${props.size}px`,
  height: `${props.size}px`,
  fontSize: `${Math.max(12, Math.round(props.size * 0.42))}px`,
  backgroundColor: color.value
}))
const online = computed(() => props.status === 1)
</script>

<template>
  <span
    class="lc-avatar"
    :class="[`lc-avatar--${shape}`, { 'lc-avatar--has-status': showStatus }]"
    :style="boxStyle"
  >
    <img v-if="src" :src="src" :alt="name || 'avatar'" class="lc-avatar__img" />
    <span v-else class="lc-avatar__text">{{ text }}</span>
    <span v-if="showStatus" class="lc-avatar__status" :class="{ 'is-online': online }" />
  </span>
</template>

<style scoped>
.lc-avatar {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #fff;
  font-weight: 600;
  overflow: visible;
  user-select: none;
}

.lc-avatar--circle {
  border-radius: 50%;
}

.lc-avatar--square {
  border-radius: var(--lc-radius);
}

.lc-avatar__img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: inherit;
  display: block;
}

.lc-avatar__text {
  line-height: 1;
}

.lc-avatar__status {
  position: absolute;
  right: -1px;
  bottom: -1px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background-color: var(--lc-text-3);
  border: 2px solid var(--lc-surface);
  box-sizing: content-box;
}

.lc-avatar__status.is-online {
  background-color: var(--lc-success);
}
</style>