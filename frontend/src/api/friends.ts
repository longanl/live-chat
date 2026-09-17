import request from '@/utils/request'
import type { ApiResponse, Friend, PageResult } from '@/types'

/** 添加好友（按用户名搜索并发送好友请求） */
export function addFriends(friendUsername: string): Promise<ApiResponse<any>> {
  return request.post('/friends/add', { friendUsername });
}
/** 好友列表（分页） */
export function Friendslist(page = 1, size = 20): Promise<ApiResponse<PageResult<Friend>>> {
  return request.get<PageResult<Friend>>('/friends/list', { params: { page, size } });
}
/** 查看待处理的好友申请（顶栏红点 / 联系人页共用） */
export function lookNewFriend(): Promise<ApiResponse<Friend[]>> {
  return request.get<Friend[]>('/friends/require');
}
/** 同意好友申请 */
export function newFriends(friendId: number): Promise<ApiResponse<any>> {
  return request.post('/friends/approve', { friendId });
}
/** 拒绝好友申请 */
export function removeFriends(friendId: number): Promise<ApiResponse<any>> {
  return request.post('/friends/reject', { friendId });
}
/** 删除好友 */
export function deleteFriend(friendId: number): Promise<ApiResponse<any>> {
  return request.post('/friends/delete', { friendId });
}