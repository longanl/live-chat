import request from '@/utils/request'
import type { ApiResponse, Conversation, Friend, PageResult } from '@/types'

/** 我的会话列表（群聊 + 已建立的私聊，含最后一条消息与未读数） */
export const getConversations = (
  page = 1,
  size = 20
): Promise<ApiResponse<PageResult<Conversation>>> =>
  request.get<PageResult<Conversation>>('/conversations', { params: { page, size } })

/** 会话详情（需为会话成员） */
export const getConversationDetail = (conversationId: number): Promise<ApiResponse<Conversation>> =>
  request.get<Conversation>(`/conversations/${conversationId}`)

/** 会话成员列表（分页） */
export const getConversationMembers = (
  conversationId: number,
  page = 1,
  size = 20
): Promise<ApiResponse<PageResult<Friend>>> =>
  request.get<PageResult<Friend>>(`/conversations/${conversationId}/members`, { params: { page, size } })

/** 创建群聊，返回新会话ID */
export const createGroup = (name: string, memberIds: number[] = []): Promise<ApiResponse<number>> =>
  request.post<number>('/conversations/group', { name, memberIds })

/** 邀请成员加入群聊 */
export const addGroupMembers = (
  conversationId: number,
  userIds: number[]
): Promise<ApiResponse<any>> =>
  request.post(`/conversations/${conversationId}/members`, { userIds })

/** 退出群聊 */
export const quitGroup = (conversationId: number): Promise<ApiResponse<any>> =>
  request.delete(`/conversations/${conversationId}/members/me`)