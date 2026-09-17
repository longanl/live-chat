import type { ChatMessage } from '@/types'

/** 文件消息元数据（新结构：独立列；旧数据兜底解析 content JSON） */
export interface FilePayload {
  url: string
  name?: string
  size?: number
  type?: string
}

/** 解析文件消息的 content（兼容老版本 JSON 内嵌），非文件元数据返回 null */
export function parseFileContent(content?: string): FilePayload | null {
  if (!content) return null
  try {
    const data = JSON.parse(content)
    if (data && typeof data.url === 'string') {
      return data as FilePayload
    }
  } catch {
    // 普通文本消息，忽略
  }
  return null
}

/** 取文件消息的元数据：优先新结构独立列，兜底解析 content（老数据） */
export function fileOf(msg: ChatMessage): FilePayload | null {
  if (msg.messageType !== 2) return null
  if (msg.fileUrl) {
    return {
      url: msg.fileUrl,
      name: msg.fileName,
      size: msg.fileSize,
      type: msg.fileType
    }
  }
  return parseFileContent(msg.content)
}

/** 消息是否为文件消息 */
export function isFileMessage(msg: ChatMessage): boolean {
  return msg.messageType === 2
}

/** 字节数格式化为可读大小 */
export function formatFileSize(size?: number): string {
  if (!size || size <= 0) return ''
  if (size < 1024) return `${size} B`
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`
  return `${(size / 1024 / 1024).toFixed(1)} MB`
}

const IMAGE_EXT = ['jpg', 'jpeg', 'png', 'gif', 'webp']
const VIDEO_EXT = ['mp4', 'webm', 'mov']

/** 依据 MIME 与扩展名推断上传类型 */
export function deriveUploadType(file: File): 'image' | 'video' | 'file' {
  const ext = (file.name.split('.').pop() ?? '').toLowerCase()
  if (file.type.startsWith('image/') || IMAGE_EXT.includes(ext)) return 'image'
  if (file.type.startsWith('video/') || VIDEO_EXT.includes(ext)) return 'video'
  return 'file'
}
