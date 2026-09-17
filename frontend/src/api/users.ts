import request from '@/utils/request'
import type { ApiResponse, LoginForm, PasswordForm, RegisterForm, UserInfo } from '@/types'

/** 登录 */
export const loginApi = (data: LoginForm): Promise<ApiResponse<UserInfo>> => request.post<UserInfo>('/user/login', data);
/** 注册 */
export const registerApi = (data: RegisterForm): Promise<ApiResponse<any>> => request.post('/user/register', data);
/** 修改密码 */
export const modifyPassword = (data: PasswordForm): Promise<ApiResponse<any>> => request.put('/user/modifyPassword', data);
/** 退出登录 */
export const logoutApi = (): Promise<ApiResponse<any>> => request.post('/user/logout');
/** 更新用户资料 */
export const updateProfile = (profile: Partial<UserInfo>): Promise<ApiResponse<any>> =>
  request.post('/user/updateProfile', profile)