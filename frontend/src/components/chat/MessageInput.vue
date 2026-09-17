<script setup lang="ts">
/**
 * 消息输入区：Enter 发送 / Shift+Enter 换行，内置 Emoji 面板与附件上传。
 * 附件上传在组件内完成，成功后向上抛「文件消息元数据」，由父级负责发送。
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/upload'
import { deriveUploadType } from '@/utils/message'
import type { UploadResult } from '@/types'

const props = withDefaults(
  defineProps<{
    /** 是否禁用（未选定会话或连接未就绪） */
    disabled?: boolean
    placeholder?: string
  }>(),
  {
    disabled: false,
    placeholder: '输入消息，Enter 发送，Shift + Enter 换行'
  }
)

const emit = defineEmits<{
  (e: 'send', text: string): void
  (e: 'send-file', payload: UploadResult): void
}>()

const text = ref('')
const uploading = ref(false)
const showEmoji = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

/** 常用 Emoji（原生 Unicode，不引入额外依赖） */
const EMOJIS = [
  '😀', '😄', '😁', '😂', '🤣', '😊', '', '😘',
  '😜', '🤔', '', '😴', '😭', '😡', '👍', '👏',
  '🙏', '', '❤️', '🔥', '✅', '🌟', '💪', '🤝'
]

function submit(): void {
  const content = text.value.trim()
  if (!content || props.disabled) return
  emit('send', content)
  text.value = ''
  showEmoji.value = false
}

function pickEmoji(emoji: string): void {
  text.value += emoji
}

function pickFile(): void {
  fileInput.value?.click()
}

async function onFileChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  uploading.value = true
  try {
    const res = await uploadFile(file, deriveUploadType(file))
    if (res.code === 200 && res.data) {
      emit('send-file', res.data)
    } else {
      ElMessage.error(res.msg || '文件上传失败')
    }
  } catch (error) {
    console.error('文件上传失败', error)
    ElMessage.error('文件上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <div class="msg-input">
    <div class="msg-input__toolbar">
      <button
        class="msg-input__icon-btn"
        type="button"
        title="表情"
        @click="showEmoji = !showEmoji"
      >
        <el-icon><Sunny /></el-icon>
      </button>
      <button
        class="msg-input__icon-btn"
        type="button"
        title="发送图片 / 视频 / 文件"
        :disabled="uploading || disabled"
        @click="pickFile"
      >
        <el-icon v-if="uploading" class="is-loading"><Loading /></el-icon>
        <el-icon v-else><Paperclip /></el-icon>
      </button>
      <input ref="fileInput" type="file" class="msg-input__file" @change="onFileChange" />
    </div>

    <transition name="el-zoom-in-bottom">
      <div v-if="showEmoji" class="msg-input__emoji">
        <button
          v-for="emoji in EMOJIS"
          :key="emoji"
          class="msg-input__emoji-btn"
          type="button"
          @click="pickEmoji(emoji)"
        >
          {{ emoji }}
        </button>
      </div>
    </transition>

    <div class="msg-input__row">
      <el-input
        v-model="text"
        type="textarea"
        :autosize="{ minRows: 1, maxRows: 5 }"
        resize="none"
        :placeholder="placeholder"
        :disabled="disabled"
        class="msg-input__field"
        @keydown.enter.exact.prevent="submit"
      />
      <el-button type="primary" :disabled="disabled || !text.trim()" @click="submit">
        发送
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.msg-input {
  flex-shrink: 0;
  padding: 8px 16px 12px;
  background-color: var(--lc-surface);
  border-top: 1px solid var(--lc-border);
}

.msg-input__toolbar {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 6px;
}

.msg-input__icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border: none;
  border-radius: var(--lc-radius-sm);
  background: transparent;
  color: var(--lc-text-2);
  font-size: 17px;
  cursor: pointer;
}

.msg-input__icon-btn:hover:not(:disabled) {
  background-color: var(--lc-surface-2);
  color: var(--lc-primary);
}

.msg-input__icon-btn:disabled {
  cursor: not-allowed;
  color: var(--lc-text-3);
}

.msg-input__file {
  display: none;
}

.msg-input__emoji {
  display: grid;
  grid-template-columns: repeat(12, 1fr);
  gap: 2px;
  padding: 8px;
  margin-bottom: 8px;
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  background-color: var(--lc-surface-2);
}

.msg-input__emoji-btn {
  border: none;
  background: transparent;
  font-size: 18px;
  line-height: 1;
  padding: 4px 0;
  border-radius: var(--lc-radius-sm);
  cursor: pointer;
}

.msg-input__emoji-btn:hover {
  background-color: var(--lc-surface-3);
}

.msg-input__row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.msg-input__field {
  flex: 1;
}

.msg-input__field :deep(.el-textarea__inner) {
  border-radius: var(--lc-radius);
  padding: 9px 12px;
  line-height: 1.5;
  box-shadow: none;
}
</style>