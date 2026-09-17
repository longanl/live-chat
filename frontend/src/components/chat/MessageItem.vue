<script setup lang="ts">
/**
 * 单条消息：统一渲染文本 / 图片 / 视频 / 文件，己方靠右、他人靠左。
 * 群聊由父级传入 showNickname 控制是否显示昵称。
 */
import { computed } from 'vue'
import type { ChatMessage } from '@/types'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { fileOf, formatFileSize, type FilePayload } from '@/utils/message'
import { formatBubbleTime } from '@/utils/time'

const props = withDefaults(
  defineProps<{
    msg: ChatMessage
    selfId?: number
    showNickname?: boolean
  }>(),
  {
    selfId: undefined,
    showNickname: true
  }
)

const IMAGE_EXT = ['jpg', 'jpeg', 'png', 'gif', 'webp']
const VIDEO_EXT = ['mp4', 'webm', 'mov']

type FileKind = 'image' | 'video' | 'file'

/** 依据后端 type 字段与文件名扩展名判断文件展示形态 */
function inferKind(payload: FilePayload): FileKind {
  if (payload.type === 'image' || payload.type === 'video' || payload.type === 'file') {
    return payload.type
  }
  const ext = (payload.name?.split('.').pop() ?? '').toLowerCase()
  if (IMAGE_EXT.includes(ext)) return 'image'
  if (VIDEO_EXT.includes(ext)) return 'video'
  return 'file'
}

const mine = computed(() => props.selfId !== undefined && props.msg.senderId === props.selfId)
const file = computed<FilePayload | null>(() => fileOf(props.msg))
const kind = computed<FileKind>(() => (file.value ? inferKind(file.value) : 'file'))
</script>

<template>
  <div class="msg-item" :class="{ 'msg-item--mine': mine }">
    <UserAvatar
      class="msg-item__avatar"
      :src="msg.avatar"
      :name="msg.nickname"
      :size="36"
    />
    <div class="msg-item__body">
      <div v-if="showNickname && !mine" class="msg-item__name">
        {{ msg.nickname || '用户' }}
      </div>

      <div class="msg-item__row" :class="{ 'msg-item__row--mine': mine }">
        <template v-if="file">
          <div class="msg-bubble msg-bubble--file" :class="{ 'msg-bubble--mine': mine }">
            <a v-if="kind === 'image'" :href="file.url" target="_blank" rel="noopener">
              <img class="msg-file__image" :src="file.url" :alt="file.name || '图片'" />
            </a>
            <video
              v-else-if="kind === 'video'"
              class="msg-file__video"
              :src="file.url"
              controls
              preload="metadata"
            />
            <a
              v-else
              class="msg-file"
              :href="file.url"
              target="_blank"
              rel="noopener"
              download
            >
              <el-icon class="msg-file__icon"><Document /></el-icon>
              <span class="msg-file__text">
                <span class="msg-file__name lc-ellipsis">{{ file.name || '文件' }}</span>
                <span class="msg-file__size">{{ formatFileSize(file.size) || '点击下载' }}</span>
              </span>
            </a>
          </div>
        </template>
        <div v-else class="msg-bubble" :class="{ 'msg-bubble--mine': mine }">
          {{ msg.content }}
        </div>

        <span class="msg-item__time">{{ formatBubbleTime(msg.sendTime) }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.msg-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 6px 16px;
}

.msg-item--mine {
  flex-direction: row-reverse;
}

.msg-item__avatar {
  margin-top: 2px;
}

.msg-item__body {
  max-width: 62%;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.msg-item--mine .msg-item__body {
  align-items: flex-end;
}

.msg-item__name {
  font-size: 12px;
  color: var(--lc-text-3);
  margin-bottom: 4px;
}

.msg-item__row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  min-width: 0;
}

.msg-item__row--mine {
  flex-direction: row-reverse;
}

.msg-item__time {
  flex-shrink: 0;
  font-size: 11px;
  color: var(--lc-text-3);
}

.msg-bubble {
  padding: 8px 12px;
  border-radius: var(--lc-radius);
  background-color: var(--lc-surface);
  color: var(--lc-text);
  font-size: 14px;
  line-height: 1.5;
  word-break: break-word;
  white-space: pre-wrap;
  box-shadow: var(--lc-shadow-sm);
}

.msg-bubble--mine {
  background-color: var(--lc-primary);
  color: #fff;
}

.msg-bubble--file {
  padding: 6px;
  overflow: hidden;
}

.msg-file__image {
  display: block;
  max-width: 260px;
  max-height: 260px;
  border-radius: var(--lc-radius-sm);
  object-fit: cover;
}

.msg-file__video {
  display: block;
  max-width: 300px;
  border-radius: var(--lc-radius-sm);
  background-color: #000;
}

.msg-file {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  min-width: 180px;
  text-decoration: none;
  color: inherit;
}

.msg-file__icon {
  font-size: 26px;
  color: var(--lc-primary);
  flex-shrink: 0;
}

.msg-bubble--mine .msg-file__icon {
  color: #fff;
}

.msg-file__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 2px;
}

.msg-file__name {
  font-size: 13px;
  max-width: 180px;
}

.msg-file__size {
  font-size: 11px;
  color: var(--lc-text-3);
}

.msg-bubble--mine .msg-file__size {
  color: rgba(255, 255, 255, 0.8);
}
</style>
