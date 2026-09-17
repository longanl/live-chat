# 阶段一 API 未实现接口落地

## 问题现象

`api文档.md` v1.1 中标记为 🆕（尚无实现）的部分接口影响了体验与对接：

- `GET /user/search`：没有用户搜索入口，`/friends/add` 只能手动输用户名。
- `GET /user/{id}`：无法查看用户详情。
- `POST /conversations/private`：私聊会话只在首次 WebSocket 发消息时惰性创建，无 HTTP 创建途径。
- `POST /messages/send`：发送消息只有 WebSocket 通道，无 HTTP 兜底。
- `DELETE /conversations/{id}/members/{userId}`：群聊没有踢人能力。

## 问题原因

功能未实现，SQL / 表结构 / VO / DTO / Controller 均无对应内容。同时消息保存与广播逻辑分散在 `WsChatController.saveAndBuild` 内，无法被 HTTP 通道复用。

## 解决方案

按「阶段一」实施（不涉及表结构变更；删除消息、拉黑、群信息、撤回等留到后续阶段）：

1. **通用分页**：新增 `model/vo/PageResult.java`（total/page/size/list，含 `of()` 静态工厂）。
2. **搜索 / 详情**：
   - 新增 `UserSearchVO`、`UserDetailVO`（含当前用户视角的 `relation` 字段）。
   - `UserMapper` 新增 `countSearch` / `selectSearch` / `selectUserDetail`。
   - `relation` 用一个 SQL 片段通过相关子查询一次性算出：
     1. 命中 `friend_relations`（pair 按小大排序，用 `LEAST/GREATEST`）→ `FRIEND`；
     2. `friend_requests` 里 `from=我,to=对方,status=0` → `REQUEST_SENT`；
     3. `from=对方,to=我,status=0` → `REQUEST_RECEIVED`；
     4. 否则 `NONE`。
   - 避免了对每条结果 N+1 查 `FriendMapper`，与项目里列表查询用批量子查询的风格一致。
   - 约定：`keyword` 必填且排除本人；`page` 默认 1，`size` 默认 20、上限 100。
3. **私聊会话**：`MessagesService.createPrivateConversation` 直接复用 `resolveConversation(P2P,...)`（仅限好友、不存在则建会话并补齐双方成员），`POST /conversations/private` 返回会话 ID。
4. **HTTP 发送消息**：
   - 把 `WsChatController.saveAndBuild` 下沉为 `MessagesService.saveMessage`（持久化 + 构造 `MessageVO`，文件判定 `messageType=2 && fileUrl 非空`），WS 与 HTTP 共用，删除重复代码。
   - 新增 `MessagesService.broadcastMessage`：群聊广播 `/topic/conv/{id}`；私聊发给全部成员（含发送者回显），与 WS 通道行为一致。
   - `POST /messages/send` 仅校验 `clientMsgId` 必填（幂等去重依赖表结构，放后续阶段）。
   - `MessageDTO` 增加 `clientMsgId` 字段；主题前缀抽到 `Constant.TOPIC_CONVERSATION_PREFIX`。
5. **踢人**：`ConversationService.kickMember` —— 必须是群成员且是群主、不能踢自己、目标必须是群成员，然后删成员记录。

## 为什么采用这个方案

- **复用而非复制**：消息落库 + 推送给出逻辑原本只存在于 WS 控制器，下沉到 service 后 HTTP 与 WS 行为天然一致，后续加撤回等只需改一处。
- **SQL 一次算关系**：relation 场景少（4 种）、分页量小（≤100），子查询开销可接受，比在应用层对每行分别查 2~3 次更简洁。
- **阶段边界**：凡涉及表结构变更的能力（拉黑表、群 avatar/notice、撤回标记、clientMsgId 唯一列、删除视图）都推到阶段二以后，避免本阶段引入 DDL 与迁移风险。

## 解决了什么问题

阶段一 5 个未实现接口全部可用：搜索、详情、私聊会话、HTTP 发消息（含实时推送）、踢人。文档已同步把对应条目改为 ✅。

限制与遗留：

- `relation` 尚无 `BLOCKED`（拉黑未实现）；详情暂无 `signature`（表无列）。
- `/messages/send` 的 `clientMsgId` 只做必填校验，不做幂等去重。
- 踢人当前仅群主，管理员角色不存在。
- 未验证的猜测不得作为结论：本方案未运行测试/构建，仅做静态 lint 通过。

## 相关文件

- `backend/src/main/java/com/xuziran/livechat/model/vo/{PageResult,UserSearchVO,UserDetailVO}.java`（新增）
- `backend/src/main/java/com/xuziran/livechat/model/dto/{PrivateChatDTO（新增）,MessageDTO}.java`
- `backend/src/main/java/com/xuziran/livechat/mapper/UserMapper.java` + `resources/mapper/UserMapper.xml`
- `backend/src/main/java/com/xuziran/livechat/service/{UserService,MessagesService,ConversationService}.java` + `impl/*`
- `backend/src/main/java/com/xuziran/livechat/controller/{UsersController,ConversationController,MessagesController}.java`
- `backend/src/main/java/com/xuziran/livechat/websocket/WsChatController.java`
- `backend/src/main/java/com/xuziran/livechat/common/constant/Constant.java`
- `backend/src/main/resources/api文档.md`