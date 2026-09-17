<script setup lang="ts">
/**
 * 消息滚动容器：群聊与私聊共用。
 * - 上滚到顶部触发 load-more，并在加载后保持视觉位置（不跳动）；
 * - 己方发送/收到新消息且原本贴近底部时自动滚到底；
 * - 按天插入日期分隔；不在底部时提供「回到最新」浮标。
 */
import { computed, nextTick, ref, watch } from 'vue'
import type { ChatMessage } from '@/types'
import MessageItem from '@/components/chat/MessageItem.vue'
import { formatDayDivider, isDifferentDay } from '@/utils/time'

const props = withDefaults(
  defineProps<{
    messages: ChatMessage[]
    selfId?: number
    /** 是否还有更早的消息 */
    hasMore?: boolean
    /** 是否正在加载更早的消息 */
    loading?: boolean
    /** 是否显示昵称（群聊显示） */
    showNickname?: boolean
    /** 会话切换标识：变化时滚到底部 */
    scrollKey?: string | number
  }>(),
  {
    selfId: undefined,
    hasMore: true,
    loading: false,
    showNickname: true,
    scrollKey: ''
  }
)

const emit = defineEmits<{ (e: 'load-more'): void }>()

const scroller = ref<HTMLElement | null>(null)
/** 加载更早消息前的滚动高度，用于加载后复原位置 */
let restoreHeight = 0
const showJump = ref(false)

/** 组装渲染行：跨天时插入日期分隔 */
const rows = computed(() => {
  const out: Array<{ key: string; divider?: string; msg?: ChatMessage }> = []
  let prev: ChatMessage | undefined
  props.messages.forEach((msg, index) => {
    if (!prev || isDifferentDay(prev.sendTime, msg.sendTime)) {
      out.push({ key: `d-${msg.id ?? index}`, divider: formatDayDivider(msg.sendTime) })
    }
    out.push({ key: `m-${msg.id ?? index}`, msg })
    prev = msg
  })
  return out
})

function isNearBottom(el: HTMLElement, threshold = 90): boolean {
  return el.scrollHeight - el.scrollTop - el.clientHeight < threshold
}

function scrollToBottom(): void {
  const el = scroller.value
  if (!el) return
  el.scrollTop = el.scrollHeight
  showJump.value = false
}

function onScroll(): void {
  const el = scroller.value
  if (!el) return
  showJump.value = !isNearBottom(el)
  if (el.scrollTop > 8 || props.loading || !props.hasMore) return
  if (el.scrollHeight <= el.clientHeight) return
  restoreHeight = el.scrollHeight
  emit('load-more')
}

watch(
  () => props.messages.length,
  async () => {
    const el = scroller.value
    if (!el) return
    // pre-flush：此处读到的仍是更新前的滚动位置
    const wasNearBottom = isNearBottom(el)
    if (restoreHeight > 0) {
      await nextTick()
      const target = scroller.value
      if (target) {
        target.scrollTop = target.scrollHeight - restoreHeight
        showJump.value = !isNearBottom(target)
      }
      restoreHeight = 0
      return
    }
    if (wasNearBottom) {
      await nextTick()
      scrollToBottom()
    }
  }
)

// 切换会话：直接定位到最后一条
watch(
  () => props.scrollKey,
  async () => {
    await nextTick()
    scrollToBottom()
  },
  { immediate: true }
)

defineExpose({ scrollToBottom })
</script>

<template>
  <div class="msg-list">
    <div ref="scroller" class="msg-list__scroller" @scroll.passive="onScroll">
      <div v-if="loading" class="msg-list__tip">正在加载更早的消息…</div>
      <div v-else-if="!hasMore && messages.length" class="msg-list__tip">没有更早的消息了</div>

      <template v-for="row in rows" :key="row.key">
        <div v-if="row.divider" class="msg-list__divider">{{ row.divider }}</div>
        <MessageItem
          v-else-if="row.msg"
          :msg="row.msg"
          :self-id="selfId"
          :show-nickname="showNickname"
        />
      </template>

      <slot v-if="!messages.length" name="empty" />
    </div>

    <button v-if="showJump" class="msg-list__jump" type="button" @click="scrollToBottom">
      <el-icon><ArrowDownBold /></el-icon>
      回到最新
    </button>
  </div>
</template>

<style scoped>
.msg-list {
  position: relative;
  flex: 1;
  min-height: 0;
  display: flex;
}

/* 唯一可滚动区域：高度由父级 flex 决定，不再写死像素 */
.msg-list__scroller {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 16px 0 12px;
}

.msg-list__tip {
  text-align: center;
  font-size: 12px;
  color: var(--lc-text-3);
  padding: 6px 0 10px;
}

.msg-list__divider {
  text-align: center;
  font-size: 12px;
  color: var(--lc-text-3);
  margin: 8px 0 12px;
}

.msg-list__jump {
  position: absolute;
  right: 20px;
  bottom: 16px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 12px;
  border: 1px solid var(--lc-border);
  border-radius: 999px;
  background-color: var(--lc-surface);
  color: var(--lc-text-2);
  font-size: 12px;
  cursor: pointer;
  box-shadow: var(--lc-shadow);
}

.msg-list__jump:hover {
  color: var(--lc-primary);
  border-color: var(--lc-primary);
}
</style>