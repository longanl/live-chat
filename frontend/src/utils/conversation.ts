import type { Conversation } from '@/types'

/** 会话类型：1-私聊 2-群聊（与后端 conversations.type 一致） */
export const CONVERSATION_TYPE_P2P = 1
export const CONVERSATION_TYPE_GROUP = 2

/** 会话列表/头部展示名：群聊用群名，私聊用对方昵称 */
export function conversationTitle(conv?: Conversation | null): string {
  if (!conv) return ''
  if (conv.type === CONVERSATION_TYPE_GROUP) return conv.name || '群聊'
  return conv.peerNickname || '私聊'
}

/** 会话头像地址：群聊不带头像（走首字母兜底），私聊用对方头像 */
export function conversationAvatar(conv?: Conversation | null): string {
  if (!conv || conv.type === CONVERSATION_TYPE_GROUP) return ''
  return conv.peerAvatar || ''
}

/** 会话列表中「最后一条消息」的预览文案 */
export function conversationPreview(conv?: Conversation | null): string {
  if (!conv) return ''
  if (!conv.lastMessageId) return '暂无消息'
  if (conv.lastMessageType === 2) {
    return conv.lastFileName ? `[文件] ${conv.lastFileName}` : '[文件]'
  }
  const content = (conv.lastContent ?? '').trim()
  if (!content) return '暂无消息'
  const prefix = conv.lastSenderNickname ? `${conv.lastSenderNickname}: ` : ''
  return `${prefix}${content}`
}

/** 判断会话是否为群聊 */
export function isGroupConversation(conv?: Conversation | null): boolean {
  return !!conv && conv.type === CONVERSATION_TYPE_GROUP
}