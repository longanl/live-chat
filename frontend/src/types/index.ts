/**
 * 项目统一的业务类型定义
 *
 * 说明：接口中保留 `[key: string]: any` 索引签名，用于兼容后端返回的额外字段；
 * 同时 .vue 中的 <script setup> 仍为 JS，索引签名可避免模板/脚本访问未声明字段时报错。
 */

/** 通用分页结果 */
export interface PageResult<T> {
  total: number
  page: number
  size: number
  list: T[]
}

/** 后端统一响应结构：{ code, msg, data } */
export interface ApiResponse<T = any> {
  code: number
  msg?: string
  message?: string
  data: T
}

/** localStorage 中存储的登录用户信息（登录接口返回的 data） */
export interface UserInfo {
  id: number
  username?: string
  nickname?: string
  avatar?: string
  /** 登录/鉴权 token，请求拦截器会写入请求头 token */
  token?: string
  [key: string]: any
}

/** 好友 / 联系人 */
export interface Friend {
  id: number
  username?: string
  nickname?: string
  avatar?: string
  /** 在线状态：1-在线，0-离线 */
  status?: number
  /** 与当前用户的私聊会话ID（尚未聊过为 undefined） */
  conversationId?: number
  /** 好友申请时间（新朋友列表使用） */
  createTime?: string
  [key: string]: any
}

/** 聊天消息（历史消息与 WebSocket 推送的消息体） */
export interface ChatMessage {
  id?: number
  /** 所属会话ID */
  conversationId?: number
  senderId?: number
  /** 消息类型：1-文本 2-文件 */
  messageType?: number
  /** 文本正文（文件消息可为说明文字） */
  content?: string
  nickname?: string
  avatar?: string
  /** 文件元数据（messageType=2 时有值） */
  fileUrl?: string
  fileName?: string
  fileSize?: number
  /** image / video / file */
  fileType?: string
  sendTime?: string
  [key: string]: any
}

/** 会话（我的会话列表 / 会话详情，对应后端 ConversationVO） */
export interface Conversation {
  /** 会话ID */
  conversationId: number
  /** 1-私聊 2-群聊 */
  type: number
  /** 群聊名称；私聊为空 */
  name?: string | null
  /** 群主用户ID；私聊为空 */
  ownerId?: number | null
  memberCount?: number
  createTime?: string
  /** 私聊对方信息（type=1 时有值） */
  peerId?: number | null
  peerNickname?: string | null
  peerAvatar?: string | null
  peerStatus?: number | null
  /** 最后一条消息（会话尚无消息时为空） */
  lastMessageId?: number | null
  lastContent?: string | null
  lastMessageType?: number | null
  lastFileName?: string | null
  lastSenderNickname?: string | null
  lastSendTime?: string | null
  /** 当前用户未读数 */
  unreadCount?: number
  [key: string]: any
}

/** 按会话统计的未读数 */
export interface UnreadStat {
  conversationId?: number
  count?: number
}

/** 文件上传结果 */
export interface UploadResult {
  url: string
  name: string
  size: number
  ext: string
  type: string
}

/** 在线用户列表元素 */
export interface OnlineUser {
  id?: number
  username?: string
  nickname?: string
  avatar?: string
  [key: string]: any
}

/** WebSocket 消息帧：{ type, content } */
export interface WsMessage {
  /** 消息类型：group-群聊，p2pchat-私聊，onlineUsers-在线用户列表，onlineCount-在线人数 */
  type: string
  content: any
}

/** 登录表单 */
export interface LoginForm {
  username: string
  password: string
  rememberMe?: boolean
  [key: string]: any
}

/** 注册表单 */
export interface RegisterForm {
  username: string
  nickname: string
  password: string
  confirmPassword: string
  /** 头像地址，上传成功前为 null */
  avatar: string | null
  [key: string]: any
}

/** 修改密码表单 */
export interface PasswordForm {
  id?: number
  oldPassword: string
  newPassword: string
  confirmPassword: string
  [key: string]: any
}