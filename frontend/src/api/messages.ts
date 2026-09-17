import request from '@/utils/request'
import type { ApiResponse, ChatMessage, UnreadStat } from '@/types'
import { HISTORY_PAGE_SIZE } from '@/utils/constants'

/** 会话历史（游标分页） */
export const getHistory = (
  conversationId: number,
  beforeId?: number,
  limit = HISTORY_PAGE_SIZE
): Promise<ApiResponse<ChatMessage[]>> =>
  request.get<ChatMessage[]>('/messages/history', {
    params: { conversationId, beforeId, limit }
  })

/** 标记会话已读（推进已读游标） */
export const markRead = (conversationId: number): Promise<ApiResponse<any>> =>
  request.post('/messages/read', { conversationId })

/** 按会话统计未读数 */
export const getUnread = (): Promise<ApiResponse<UnreadStat[]>> =>
  request.get<UnreadStat[]>('/messages/unread')