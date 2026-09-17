# 09 · 登录后立即建立 WebSocket

## 问题

登录成功后不建连，靠 `onScopeDispose` 整页刷新兜底（`login.vue` 卸载即 reload）；
刷新前发送消息静默失败，在线人数/成员为空。

## 解决

- `main.ts` 抽 `bootstrapWebSocket()`：冷启动时已登录则建连；
- `login.vue` 登录成功后 `websocket.initWebSocket(result.data.id)` 显式建连，删除 `reload` hack；
- `router.beforeEach` 兜底（见 10）：已登录且未连接时补连；
  `initWebSocket` 内置"先关旧连接"，不会产生重复连接。

## 落地 ✅

- 文件：`frontend/src/main.ts`、`views/login.vue`、`router/index.ts`
- 验证：登录即 connected；登录/注册页互跳不再整页刷新