# 在线聊天室 API 文档

> **Base URL**：`http://localhost:8080`
> **版本**：v1.1
> **认证**：请求头 `token: <JWT>`（`/user/login`、`/user/register`、`/user/avatar`、`/files/upload` 免登录）
> **统一响应**：`Result<T>` = `{ code: int, msg: string, data: T }`
> **Swagger UI**：`http://localhost:8080/swagger-ui.html`（OpenAPI spec：`/v3/api-docs`）
> **时间格式**：`yyyy-MM-dd HH:mm`（`LocalDateTime` 序列化结果，非 ISO 8601）

> 📌 **标记说明**
> ✅ = 当前已实现
> 🆕 = 建议补充（尚未实现，标注为规划接口）
> ⚠️ = 存在问题需修复

---

## 统一响应约定

所有接口返回 `Result<T>`：

```json
// 成功（msg 为 null）
{ "code": 200, "msg": null, "data": { } }

// 业务失败（HTTP 200）
{ "code": 0, "msg": "错误原因", "data": null }
```

- `code`：`200` 成功，`0` 业务失败。
- 认证失败：HTTP `401`（无响应体）。
- 未捕获异常：HTTP `500`；静态资源/未知路径：HTTP `404`。

---

## 目录

- [一、用户模块](#一用户模块)
- [二、好友模块](#二好友模块)
- [三、会话 / 群组模块](#三会话--群组模块)
- [四、消息模块](#四消息模块)
- [五、文件模块](#五文件模块)
- [六、WebSocket / STOMP 接口](#六websocket--stomp-接口)
- [七、数据模型与枚举](#七数据模型与枚举)
- [附录 A：遗留问题](#附录-a遗留问题)
- [附录 B：接口总览](#附录-b接口总览)

---

## 一、用户模块

### 1.1 用户注册 ✅

```
POST /user/register
Content-Type: application/json
```

**请求体 `RegisterDTO`**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| username | string | ✓ | 用户名 |
| nickname | string | | 昵称 |
| password | string | ✓ | 密码 |
| confirmPassword | string | | 确认密码 |
| avatar | string | | 头像 URL |
| id | int64 | | ⚠️ 内部字段，由数据库回填用于自动入群，客户端不应传 |

**响应**：`Result<Void>`

> 注册成功后自动加入内置群会话（ID=1）。

---

### 1.2 用户登录 ✅

```
POST /user/login
Content-Type: application/json
```

**请求体 `UserDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| username | string | ✓ |
| password | string | ✓ |
| rememberMe | boolean | |

**响应**：`Result<UserVO>`

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 1,
    "username": "alice",
    "nickname": "爱丽丝",
    "avatar": "http://...",
    "status": 1,
    "updateTime": "2026-09-16 19:00",
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "rememberMe": false
  }
}
```

> 登录成功会把 `users.status` 置为 1，并返回 JWT，后续 HTTP 请求放入请求头 `token`。

---

### 1.3 用户登出 ✅

```
POST /user/logout
```

**响应**：`Result<Void>`

> 已由 `GET` 改为 `POST`。登出会把 `users.status` 置为 0。

---

### 1.4 修改密码 ✅

```
PUT /user/modifyPassword
Content-Type: application/json
```

**请求体 `PasswordDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| oldPassword | string | ✓ |
| newPassword | string | ✓ |
| confirmPassword | string | |

**响应**：`Result<Void>`

---

### 1.5 更新用户信息 ✅

```
POST /user/updateProfile
Content-Type: application/json
```

**请求体 `ProfileDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| nickname | string | |
| avatar | string | |

**响应**：`Result<Void>`

---

### 1.6 上传头像 ✅

```
POST /user/avatar
Content-Type: multipart/form-data
```

**表单字段**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| file | File | ✓ | jpg / jpeg / png / gif / webp，≤ 10 MB |

**响应**：`Result<String>`（新头像 URL）

> 路径已由旧接口 `/uploadavatar687` 改为 `/user/avatar`，且允许免登录调用（注册页可用）。
> 该接口已加入 JWT 拦截器白名单。

---

### 1.7 搜索用户 🆕

```
GET /user/search?keyword={keyword}&page={page}&size={size}
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| keyword | string | ✓ | 用户名或昵称模糊匹配 |
| page | int32 | | 页码，默认 1 |
| size | int32 | | 每页数量，默认 20 |

**响应**：`Result<PageResult<UserSearchVO>>`

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "total": 30,
    "page": 1,
    "size": 20,
    "list": [
      {
        "id": 2,
        "username": "bob",
        "nickname": "鲍勃",
        "avatar": "http://...",
        "status": 1,
        "relation": "FRIEND"
      }
    ]
  }
}
```

**`relation` 取值**：`NONE`（陌生人）/ `FRIEND`（好友）/ `REQUEST_SENT`（已发请求）/ `REQUEST_RECEIVED`（对方已发请求）/ `BLOCKED`

> 说明：没有这个接口，`/friends/add` 只能靠用户手动输用户名，体验较差。

---

### 1.8 查看用户详情 🆕

```
GET /user/{id}
```

**路径参数**：`id` (int64)

**响应**：`Result<UserDetailVO>`

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "id": 2,
    "username": "bob",
    "nickname": "鲍勃",
    "avatar": "http://...",
    "status": 1,
    "signature": "个性签名",
    "relation": "FRIEND"
  }
}
```

---

### 1.9 用户在线状态 ✅（走 WebSocket）

在线状态不再提供 HTTP 接口，改由 WebSocket 推送。详见 [六、WebSocket / STOMP 接口](#六websocket--stomp-接口)。

---

## 二、好友模块

### 2.1 添加好友 ✅

```
POST /friends/add
Content-Type: application/json
```

**请求体 `AddFriendDTO`**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| friendUsername | string | ✓ | 对方用户名 |

> 🆕 建议同时支持 `friendId`，二者传其一即可。

**响应**：`Result<Void>`

**行为规则**：
- 用户不存在 / 加自己 / 已是好友 → 业务失败。
- 对方已先发起申请 → 直接互相成为好友。
- 自己已有待处理申请 → 提示“申请已发送，请等待对方确认”。
- 否则新增一条待处理申请。

---

### 2.2 审批好友请求 ✅

```
POST /friends/approve
Content-Type: application/json
```

**请求体 `RelationshipDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| friendId | int64 | ✓ |

**响应**：`Result<Void>`

> 🆕 建议改为 `requestId`，语义更准确（避免同一用户重复请求时歧义）。
> `friendId` 指申请人 ID，服务端按 `from_user_id=friendId, to_user_id=当前用户, status=0` 匹配待处理申请。

---

### 2.3 拒绝好友请求 ✅

```
POST /friends/reject
Content-Type: application/json
```

**请求体 `RelationshipDTO`**（同上）

**响应**：`Result<Void>`

---

### 2.4 删除好友 ✅

```
POST /friends/delete
Content-Type: application/json
```

**请求体 `RelationshipDTO`**（同上）

**响应**：`Result<Void>`

> 同时会把双方所有待处理申请置为已拒绝。

---

### 2.5 查询好友列表 ✅

```
GET /friends/list
```

**响应**：`Result<User[]>`

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 2,
      "username": "bob",
      "nickname": "鲍勃",
      "avatar": "http://...",
      "status": 1,
      "password": null,
      "createTime": null,
      "updateTime": null,
      "conversationId": 10
    }
  ]
}
```

**字段说明**

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 好友用户 ID |
| username | string | 用户名 |
| nickname | string | 昵称 |
| avatar | string | 头像 URL |
| status | int32 | 0=离线，1=在线 |
| conversationId | int64 | 与该好友的私聊会话 ID，未聊过为 null |

> 无分页参数，返回全量好友。
> `password` 字段虽在 JSON 中出现，但 SQL 未查询该列，实际恒为 `null`，不存在密码泄露；建议后续用 `@JsonIgnore` 彻底移除该字段。

---

### 2.6 查询收到的好友请求 ✅

```
GET /friends/require
```

**响应**：`Result<User[]>`

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 3,
      "username": "carol",
      "nickname": "卡罗尔",
      "avatar": "http://...",
      "status": 0,
      "createTime": "2026-09-16 19:00"
    }
  ]
}
```

> 仅返回**待处理（status=0）**且**发给当前用户**的申请，元素为申请人 `User`。
> 无 `type` 参数，不支持查询“我发出的请求”。`createTime` 此处为申请时间。

---

### 2.7 拉黑用户 🆕

```
POST /friends/block
Content-Type: application/json
```

**请求体 `RelationshipDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| friendId | int64 | ✓ |

**响应**：`Result<Void>`

---

### 2.8 取消拉黑 🆕

```
DELETE /friends/block/{userId}
```

**路径参数**：`userId` (int64)

**响应**：`Result<Void>`

---

### 2.9 拉黑列表 🆕

```
GET /friends/blocked?page={page}&size={size}
```

**响应**：`Result<PageResult<UserVO>>`

---

## 三、会话 / 群组模块

> 会话模型：`conversations` + `conversation_members`。
> `type`：**1=私聊，2=群聊**。
> 所有会话相关接口都会校验“当前登录用户是该会话成员”，非成员返回业务失败。

### 3.1 我的会话列表 ✅

```
GET /conversations
```

**响应**：`Result<ConversationVO[]>`

> 无 `cursor`/`limit` 参数，一次性返回当前用户的全部会话，按最后消息时间倒序。

**`ConversationVO` 字段**

| 字段 | 类型 | 说明 |
|---|---|---|
| conversationId | int64 | 会话 ID |
| type | int32 | **1=私聊，2=群聊** |
| name | string | 群名；私聊为空 |
| ownerId | int64 | 群主 ID；私聊为空 |
| memberCount | int32 | 成员数 |
| createTime | datetime | 创建时间 |
| peerId | int64 | 私聊对方 ID（仅私聊） |
| peerNickname | string | 私聊对方昵称 |
| peerAvatar | string | 私聊对方头像 |
| peerStatus | int32 | 对方在线状态（0/1） |
| lastMessageId | int64 | 最后一条消息 ID |
| lastContent | string | 最后一条内容 |
| lastMessageType | int32 | 最后一条消息类型（1/2） |
| lastFileName | string | 最后一条文件名 |
| lastSenderNickname | string | 最后一条发送者昵称 |
| lastSendTime | datetime | 最后一条发送时间 |
| unreadCount | int32 | 未读数（对方发送且晚于本人已读游标的消息数） |

---

### 3.2 会话详情 ✅

```
GET /conversations/{id}
```

**路径参数**：`id` (int64)

**响应**：`Result<ConversationVO>`（需为会话成员）

---

### 3.3 会话成员列表 ✅

```
GET /conversations/{id}/members
```

**路径参数**：`id` (int64)

**响应**：`Result<User[]>`（需为会话成员，按加入顺序）

```json
{
  "code": 200,
  "msg": null,
  "data": [
    {
      "id": 1,
      "username": "alice",
      "nickname": "爱丽丝",
      "avatar": "http://...",
      "status": 1,
      "password": null
    }
  ]
}
```

> 无分页参数，返回全量成员。SQL 只查询安全列，`password` 恒为 `null`，建议后续加 `@JsonIgnore` 彻底移除。

---

### 3.4 邀请成员加入群聊 ✅

```
POST /conversations/{id}/members
Content-Type: application/json
```

**路径参数**：`id` (int64)

**请求体 `MemberIdsDTO`**

| 字段 | 类型 | 必填 |
|---|---|---|
| userIds | int64[] | ✓ |

**响应**：`Result<Void>`

> 需为群成员；仅群聊支持；自动去重并剔除 null 与自身。

---

### 3.5 退出群聊 ✅

```
DELETE /conversations/{id}/members/me
```

**路径参数**：`id` (int64)

**响应**：`Result<Void>`

> **群主不能退出群聊**（返回业务失败），避免产生无主群。

---

### 3.6 创建群聊 ✅

```
POST /conversations/group
Content-Type: application/json
```

**请求体 `CreateGroupDTO`**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | ✓ | 群名称，≤ 100 字符 |
| memberIds | int64[] | ✓ | 初始成员（不含创建者，创建者自动成为群主与成员） |

**响应**：`Result<Long>`（新会话 ID）

---

### 3.7 创建 / 获取私聊会话 🆕

```
POST /conversations/private
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| targetUserId | int64 | ✓ | 对方用户 ID |

**响应**：`Result<Long>`（会话 ID，已存在则直接返回已有会话）

> 当前私聊会话在首次发送 WebSocket 消息时惰性创建（见 `/app/chat.p2p`），无独立 HTTP 创建接口。

---

### 3.8 修改群信息 🆕

```
PUT /conversations/{id}
Content-Type: application/json
```

**路径参数**：`id` (int64)

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| name | string | | 群名 |
| avatar | string | | 群头像 |
| notice | string | | 群公告 |

**响应**：`Result<Void>`

> 权限：仅群主 / 管理员。

---

### 3.9 踢出成员 🆕

```
DELETE /conversations/{id}/members/{userId}
```

**路径参数**

| 参数 | 类型 | 说明 |
|---|---|---|
| id | int64 | 会话 ID |
| userId | int64 | 被踢用户 ID |

**响应**：`Result<Void>`

> 权限：仅群主 / 管理员，不能踢自己。

---

## 四、消息模块

### 4.1 会话历史（游标分页）✅

```
GET /messages/history?conversationId={id}&beforeId={cursor}&limit={n}
```

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
|---|---|---|---|
| conversationId | int64 | ✓ | 会话 ID |
| beforeId | int64 | | 游标：取此消息 ID 之前的消息 |
| limit | int32 | | 每页数量，默认 **30**，有效范围 1~100，越界回退为 30 |

**响应**：`Result<MessageVO[]>`

> 返回按时间**正序**排列的列表（服务端取最新 N 条后翻转），需为会话成员。

**`MessageVO` 字段**

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 消息 ID |
| conversationId | int64 | 会话 ID |
| senderId | int64 | 发送者 ID |
| nickname | string | 发送者昵称 |
| avatar | string | 发送者头像 |
| messageType | int32 | **1=文本，2=文件**（图片/视频/文件均归为 2） |
| content | string | 文本内容（文件消息可为说明文字） |
| fileUrl | string | 文件 URL |
| fileName | string | 原始文件名 |
| fileSize | int64 | 文件大小（字节） |
| fileType | string | `image` / `video` / `file` |
| sendTime | datetime | 发送时间 |

---

### 4.2 标记会话已读 ✅

```
POST /messages/read
Content-Type: application/json
```

**请求体 `ReadMessageDTO`**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| conversationId | int64 | ✓ | 会话 ID |

**响应**：`Result<Void>`

> 语义：把已读游标直接推进到该会话当前**最大消息 ID**（不支持只读到某一条）。
> 需为会话成员。

---

### 4.3 按会话统计未读数 ✅

```
GET /messages/unread
```

**响应**：`Result<UnreadStat[]>`

```json
{
  "code": 200,
  "msg": null,
  "data": [
    { "conversationId": 10, "count": 10 },
    { "conversationId": 11, "count": 5 }
  ]
}
```

> 只统计“对方发送且晚于本人已读游标”的消息，不含自己发的。
> 无 `total` 包装字段，前端如需总数自行求和。
> 会话列表的 `unreadCount` 已包含同样的统计，通常无需单独调用。

---

### 4.4 HTTP 发送消息（兜底）🆕

```
POST /messages/send
Content-Type: application/json
```

**请求体**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| conversationId | int64 | ✓ | 会话 ID |
| messageType | int32 | ✓ | 1=文本，2=文件 |
| content | string | | 文本内容（文本消息必填） |
| fileUrl | string | | 文件 URL |
| fileName | string | | 文件名 |
| fileSize | int64 | | 文件大小 |
| fileType | string | | `image` / `video` / `file` |
| clientMsgId | string | ✓ | 客户端生成 UUID，用于幂等去重 |

**响应**：`Result<MessageVO>`

> 当前发送消息**只有 WebSocket 通道**，无 HTTP 兜底接口。

---

### 4.5 撤回消息 🆕

```
POST /messages/{id}/recall
```

**路径参数**：`id` (int64)

**响应**：`Result<Void>`

> 规则建议：仅发送者本人、发送后 2 分钟内可撤回。

---

### 4.6 删除消息 🆕

```
DELETE /messages/{id}
```

**路径参数**：`id` (int64)

**响应**：`Result<Void>`

> 规则建议：只删自己的视图，不影响其他成员。

---

## 五、文件模块

### 5.1 上传聊天文件 ✅

```
POST /files/upload?type={type}
Content-Type: multipart/form-data
```

**参数**

| 位置 | 名称 | 类型 | 必填 | 说明 |
|---|---|---|---|---|
| query | type | string | | `file`（默认）/ `image` / `video` / `avatar` |
| header | token | string | ✓ | JWT（`type=avatar` 时免登录） |
| body | file | File | ✓ | 上传的文件 |

**文件限制**

| type | 扩展名 | 大小上限 | 存储目录 |
|---|---|---|---|
| avatar | jpg, jpeg, png, gif, webp | 10 MB | `user/yyyy/MM/dd/` |
| image | jpg, jpeg, png, gif, webp | 10 MB | `chat/image/yyyy/MM/dd/` |
| video | mp4, webm, mov | 200 MB | `chat/video/yyyy/MM/dd/` |
| file | pdf, zip, doc, docx, xls, xlsx, txt, ppt, pptx | 50 MB | `chat/file/yyyy/MM/dd/` |

**响应**：`Result<UploadResult>`

```json
{
  "code": 200,
  "msg": null,
  "data": {
    "url": "http://localhost:8888/chat/image/2026/09/16/xxxx.png",
    "name": "screenshot.png",
    "size": 102400,
    "ext": "png",
    "type": "image"
  }
}
```

> `type` 为文件类别（avatar/image/video/file），非 MIME 类型。
> 文件已改为本地存储（Nginx 图床），由 `upload.path` / `upload.domain` 配置。

---

## 六、WebSocket / STOMP 接口

消息收发与在线状态的主链路是 **WebSocket + STOMP**，HTTP 仅提供历史、已读、未读等查询能力。

### 6.1 连接与鉴权 ✅

| 项 | 值 |
|---|---|
| WebSocket 端点 | `/ws`（原生 WebSocket，无 SockJS） |
| STOMP CONNECT 头 | `token: <JWT>` |
| 消息发送前缀 | `/app` |
| 用户定向前缀 | `/user` |
| 心跳 | 10s / 10s |

CONNECT 帧校验失败（缺 token / token 无效）会被拒绝。鉴权通过后，用户 ID 作为 STOMP Principal，服务端**不信任客户端传入的 senderId**。

### 6.2 发送消息 ✅

| 目标 | 说明 | 请求体 |
|---|---|---|
| `/app/chat.group` | 群聊 | `MessageDTO` |
| `/app/chat.p2p` | 私聊（必须携带 `receiverId`） | `MessageDTO` |

**`MessageDTO` 字段**

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| senderId | int64 | | 忽略（服务端以 JWT 身份为准） |
| receiverId | int64 | 私聊必填 | 接收者 ID |
| conversationId | int64 | 群聊建议填 | 目标会话；为空时回落到内置群（ID=1，兼容旧客户端） |
| messageType | int32 | ✓ | 1=文本，2=文件 |
| content | string | | 文本内容 |
| fileUrl | string | | 文件 URL |
| fileName | string | | 原始文件名 |
| fileSize | int64 | | 文件大小 |
| fileType | string | | `image` / `video` / `file` |

**业务规则**：
- 群聊指定 `conversationId` 时校验群成员身份，非成员拒绝。
- 私聊仅限好友，非好友发送会返回业务错误。
- 私聊会话不存在时自动创建并补齐双方成员。
- 文件消息判定：`messageType=2` 且 `fileUrl` 非空，否则按文本处理。

### 6.3 订阅（接收）✅

| 订阅地址 | 说明 |
|---|---|
| `/topic/conv/{conversationId}` | 群聊消息广播 |
| `/user/{id}/queue/messages` | 私聊消息（收发双方各收到一份） |
| `/topic/online/users` | 在线用户列表（`User[]`，连接/断开时广播） |
| `/topic/online/count` | 在线人数（number） |
| `/user/queue/presence` | 初始在线快照（需主动请求，见下） |
| `/user/queue/errors` | 业务错误提示（如“对方不是你的好友，无法私聊”） |

### 6.4 在线状态 ✅

- 连接建立：`users.status=1`，广播 `/topic/online/users` 与 `/topic/online/count`。
- 断开：同一用户最后一个会话断开时才置 `status=0`（支持多标签页），随后广播。
- 初始快照：连接并订阅 `/user/queue/presence` 后，向 `/app/presence.snapshot` 发送空消息，服务端定向回复 `PresenceSnapshot`：

```json
{
  "users": [ { "id": 1, "username": "alice", "nickname": "爱丽丝", "avatar": "http://...", "status": 1 } ],
  "count": 1
}
```

> 初始快照不随连接主动推送，避免订阅竞态导致丢失。

---

## 七、数据模型与枚举

### 7.1 User（`users`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| username | string | 用户名（唯一） |
| password | string | 密码（⚠️ 不应出现在响应中） |
| avatar | string | 头像 URL |
| nickname | string | 昵称 |
| status | int32 | **0=离线，1=在线** |
| createTime | datetime | 创建时间 |
| updateTime | datetime | 更新时间 |
| conversationId | int64 | 瞬态字段，好友列表回带私聊会话 ID（非表列） |

### 7.2 Conversation（`conversations`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| type | int32 | **1=私聊，2=群聊** |
| name | string | 群名；私聊为空 |
| ownerId | int64 | 群主；私聊为空 |
| createTime | datetime | 创建时间 |

### 7.3 ConversationMember（`conversation_members`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| conversationId | int64 | 会话 ID |
| userId | int64 | 成员用户 ID |
| lastReadMsgId | int64 | 已读游标（已读到的最大消息 ID） |
| joinTime | datetime | 加入时间 |

### 7.4 ChatMessage（`chat_messages`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| conversationId | int64 | 会话 ID |
| senderId | int64 | 发送者 ID |
| messageType | int32 | **1=文本，2=文件** |
| content | string | 文本正文 |
| fileUrl / fileName / fileSize / fileType | | 文件元数据 |
| sendTime | datetime | 发送时间 |

### 7.5 FriendRelation（`friend_relations`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| userId | int64 | 好友对中较小的一方 |
| friendId | int64 | 好友对中较大的一方 |
| createTime | datetime | 建立时间 |

### 7.6 FriendRequest（`friend_requests`）

| 字段 | 类型 | 说明 |
|---|---|---|
| id | int64 | 主键 |
| fromUserId | int64 | 申请人 ID |
| toUserId | int64 | 接收人 ID |
| status | int32 | **0=待处理，1=已同意，2=已拒绝（可再次申请）** |
| handleTime | datetime | 处理时间 |
| createTime | datetime | 申请时间 |

---

## 附录 A：遗留问题

| # | 问题 | 位置 | 建议 |
|---|---|---|---|
| 1 | `RegisterDTO` 携带内部字段 `id` | `/user/register` | 用 `@JsonIgnore` 或拆分入参，避免客户端传入 |
| 2 | 响应中仍出现 `password: null` | `/friends/list`、`/friends/require`、`/conversations/{id}/members` | 返回实体改用 VO，或在 `User.password` 上加 `@JsonIgnore` |
| 3 | 枚举值无 OpenAPI 说明 | `status`、`type`、`messageType`、`FriendRequest.status` | 加 `@Schema(allowableValues=...)` 或改用 Java enum |
| 4 | `/messages/read` 无法只读到某条 | `/messages/read` | 增加可选 `lastReadMessageId` |
| 5 | 会话/好友/成员列表无分页 | `/conversations`、`/friends/list`、`/conversations/{id}/members` | 数据量大时补分页 |
| 6 | 无 HTTP 发送消息兜底 | 消息发送 | 补 `POST /messages/send` |

> 已修复：`GET /user/logout` → `POST /user/logout`；`/uploadavatar687` → `/user/avatar` 并加入拦截器白名单；文件上传 `Content-Type` 已为 `multipart/form-data`。

---

## 附录 B：接口总览

| 模块 | 方法 | 路径 | 说明 | 状态 |
|---|---|---|---|---|
| 用户 | POST | /user/register | 注册 | ✅ |
| 用户 | POST | /user/login | 登录 | ✅ |
| 用户 | POST | /user/logout | 登出 | ✅ |
| 用户 | PUT | /user/modifyPassword | 修改密码 | ✅ |
| 用户 | POST | /user/updateProfile | 更新资料 | ✅ |
| 用户 | POST | /user/avatar | 上传头像（免登录） | ✅ |
| 用户 | GET | /user/search | 搜索用户 | 🆕 |
| 用户 | GET | /user/{id} | 用户详情 | 🆕 |
| 好友 | POST | /friends/add | 添加好友 | ✅ |
| 好友 | POST | /friends/approve | 审批请求 | ✅ |
| 好友 | POST | /friends/reject | 拒绝请求 | ✅ |
| 好友 | POST | /friends/delete | 删除好友 | ✅ |
| 好友 | GET | /friends/list | 好友列表 | ✅ |
| 好友 | GET | /friends/require | 待处理好友请求 | ✅ |
| 好友 | POST | /friends/block | 拉黑 | 🆕 |
| 好友 | DELETE | /friends/block/{userId} | 取消拉黑 | 🆕 |
| 好友 | GET | /friends/blocked | 拉黑列表 | 🆕 |
| 会话 | GET | /conversations | 我的会话列表 | ✅ |
| 会话 | GET | /conversations/{id} | 会话详情 | ✅ |
| 会话 | GET | /conversations/{id}/members | 成员列表 | ✅ |
| 会话 | POST | /conversations/{id}/members | 邀请成员 | ✅ |
| 会话 | DELETE | /conversations/{id}/members/me | 退出群聊 | ✅ |
| 会话 | POST | /conversations/group | 创建群聊 | ✅ |
| 会话 | POST | /conversations/private | 创建/获取私聊 | 🆕 |
| 会话 | PUT | /conversations/{id} | 修改群信息 | 🆕 |
| 会话 | DELETE | /conversations/{id}/members/{userId} | 踢人 | 🆕 |
| 消息 | GET | /messages/history | 历史消息 | ✅ |
| 消息 | POST | /messages/read | 标记已读 | ✅ |
| 消息 | GET | /messages/unread | 未读数统计 | ✅ |
| 消息 | POST | /messages/send | HTTP 发送消息 | 🆕 |
| 消息 | POST | /messages/{id}/recall | 撤回消息 | 🆕 |
| 消息 | DELETE | /messages/{id} | 删除消息 | 🆕 |
| 文件 | POST | /files/upload | 上传聊天文件 | ✅ |
| WS | CONNECT | /ws | 建立连接（header `token`） | ✅ |
| WS | SEND | /app/chat.group | 发送群聊消息 | ✅ |
| WS | SEND | /app/chat.p2p | 发送私聊消息 | ✅ |
| WS | SEND | /app/presence.snapshot | 请求在线快照 | ✅ |
| WS | SUB | /topic/conv/{id} | 订阅群聊消息 | ✅ |
| WS | SUB | /user/{id}/queue/messages | 订阅私聊消息 | ✅ |
| WS | SUB | /topic/online/users | 在线用户列表 | ✅ |
| WS | SUB | /topic/online/count | 在线人数 | ✅ |
| WS | SUB | /user/queue/presence | 在线快照回复 | ✅ |
| WS | SUB | /user/queue/errors | 业务错误提示 | ✅ |

---
