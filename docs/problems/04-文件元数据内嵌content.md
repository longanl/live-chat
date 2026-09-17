# 04 · 文件元数据内嵌 content JSON，无法查询与校验

## 问题

上一阶段把图片/视频/普通文件的元数据序列化为 JSON 字符串塞进 `chat_messages.content`：

```json
{"url":"http://...","name":"xxx.pdf","size":12345,"type":"file"}
```

看似省了列，实则：

- `content` 是消息正文，语义被文件 JSON 污染，`where content like ...` 会把文件元数据一并命中；
- 无法对文件做聚合（统计某用户发了多少文件）、无法按 `file_type` 过滤/索引；
- 各端重复"序列化 → 解析"逻辑，解析失败即静默丢消息；
- 前世遗留的 `file_id` 列既无对应表也无人用，纯僵尸列。

## 方案：文件元数据拆为独立列

`chat_messages` 在消息维度之外增加标准化文件列：

```sql
file_url  varchar(255) NULL   -- 文件可访问 URL
file_name varchar(255) NULL   -- 原始文件名
file_size bigint      NULL   -- 字节
file_type varchar(10)  NULL   -- image / video / file
```

- `message_type=1` 时文件列为空，`message_type=2` 时填充；
- 前端 `isFileMessage()` 依据 `messageType === 2` 判断，字段直接读取，
  不再解析 JSON（`parseFileContent` 保留为兼容老数据兜底）；
- 删除 `file_id` 僵尸列与 `read_status`（见 03）。

## 落地

- ✅ `chat.sql` 新列（见 05）
- ✅ 后端 `ChatMessage` / `MessageVO` / `MessageDTO` 对齐字段
- ✅ 前端 `ChatMessage` 类型增加 `fileUrl/fileName/fileSize/fileType`