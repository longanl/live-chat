# Live-Chat 在线聊天室

基于 Spring Boot 3 + Vue 3 的实时在线聊天应用，支持群聊、私聊、好友管理、在线状态实时感知。

## 功能特性

- **用户系统**: 注册、登录、登出、修改密码、修改个人信息
- **群聊**: 所有在线用户可参与的公共聊天室
- **私聊**: 点对点私密聊天
- **好友系统**: 搜索添加好友、审批好友请求、拒绝请求、删除好友
- **实时在线**: WebSocket 实时推送在线用户列表和在线人数
- **头像上传**: 阿里云 OSS 存储用户头像
- **消息历史**: 持久化存储聊天记录，支持历史消息查询
- **JWT 认证**: 基于 Token 的接口鉴权
- **API 文档**: 内置 Swagger 在线接口文档

## 技术栈

| 前端 | 版本 | 后端 | 版本 |
|------|------|------|------|
| Vue 3 | ^3.2.38 | Java | 17 |
| Pinia | ^3.0.3 | Spring Boot | 3.5.3 |
| Vue Router 4 | ^4.1.5 | MyBatis | 3.0.3 |
| Element Plus | ^2.4.4 | MySQL | 8.x |
| Axios | ^1.7.2 | WebSocket | (内置) |
| Vite | ^3.0.9 | JWT (jjwt) | 0.9.1 |
| ESLint + Prettier | - | Aliyun OSS SDK | 3.17.4 |
| | | Redis | (依赖已声明) |

## 快速开始

### 环境要求

- JDK 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+
- (可选) 阿里云 OSS 账号（用于头像上传，不上传头像可跳过）

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd live-chat
```

### 2. 数据库初始化

创建数据库和表结构：

```sql
CREATE DATABASE IF NOT EXISTS live_chat DEFAULT CHARSET utf8mb4;
```

建表 SQL（按需执行，MyBatis 会自动映射）：

```sql
-- 用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    avatar VARCHAR(500) DEFAULT NULL,
    nickname VARCHAR(50) DEFAULT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-离线 1-在线',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 消息表
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sender_id BIGINT NOT NULL,
    receiver_id BIGINT DEFAULT 0 COMMENT '0-群聊, 其他-私聊对方ID',
    message_type TINYINT DEFAULT 1 COMMENT '1-文本 2-文件',
    content TEXT,
    file_id VARCHAR(255) DEFAULT NULL,
    send_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    read_status TINYINT DEFAULT 0 COMMENT '0-未读 1-已读'
);

-- 好友关系表
CREATE TABLE IF NOT EXISTS user_relations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    friend_id BIGINT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0-待审批 1-已通过 2-已拒绝 3-已请求',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_friend (user_id, friend_id)
);
```

### 3. 启动后端

```bash
cd back-end
```

修改 `src/main/resources/application.yml` 中的数据库连接和 OSS 配置：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live_chat?useSSL=true&serverTimezone=Asia/Shanghai&allowMultiQueries=true
    username: root
    password: your-password
```

如果不使用 OSS 头像上传功能，可以简单配置占位符：

```yaml
live-chat:
  alioss:
    endpoint: https://oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key
    access-key-secret: your-secret
    bucket-name: your-bucket
```

然后启动：

```bash
mvn spring-boot:run
```

后端默认运行在 `http://localhost:8080`。

### 4. 启动前端

```bash
cd front-end
npm install
npm run dev
```

前端默认运行在 `http://localhost:5173`。

### 5. 访问

- **前端页面**: http://localhost:5173
- **后端 API**: http://localhost:8080
- **Swagger 文档**: http://localhost:8080/swagger-ui.html

## 项目结构

```
live-chat/
├── back-end/                          # Spring Boot 后端
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/xuziran/livechat/
│       │   ├── LiveChatApplication.java          # 启动类
│       │   ├── config/                           # 配置类
│       │   │   ├── OssConfiguration.java         # OSS Bean 配置
│       │   │   ├── WebMvcConfiguration.java      # 拦截器、Swagger
│       │   │   └── WebSocketConfiguration.java   # WebSocket 导出器
│       │   ├── constant/
│       │   │   └── MessageConstant.java          # 消息类型常量
│       │   ├── controller/                       # REST 控制器
│       │   │   ├── FileController.java           # 文件上传
│       │   │   ├── FriendsController.java        # 好友管理
│       │   │   ├── MessagesController.java       # 消息历史
│       │   │   └── UsersController.java          # 用户相关
│       │   ├── interceptor/
│       │   │   └── JwtTokenInterceptor.java       # JWT 拦截器
│       │   ├── mapper/                           # MyBatis 接口
│       │   ├── pojo/                             # 实体/DTO/VO
│       │   ├── service/                          # 业务逻辑
│       │   ├── utils/                            # 工具类
│       │   └── websocket/
│       │       └── WebSocketServer.java          # WebSocket 处理器
│       └── resources/
│           ├── application.yml                   # 主配置
│           └── mapper/                           # MyBatis XML 映射
│
└── front-end/                         # Vue 3 前端
    ├── package.json
    ├── vite.config.js                 # Vite 配置 + API 代理
    └── src/
        ├── main.js                    # 入口 (初始化 Pinia, WebSocket, Element-Plus)
        ├── App.vue
        ├── api/                       # Axios 接口封装
        │   ├── login.js               # 登录/注册
        │   ├── friends.js             # 好友操作
        │   └── index.js               # 消息/个人信息
        ├── router/
        │   └── index.js               # 路由配置
        ├── stores/
        │   └── chat.js                # Pinia 状态管理
        ├── utils/
        │   ├── request.js             # Axios 实例 + 拦截器
        │   └── websocket.js           # WebSocket 连接管理
        └── views/
            ├── login/login.vue        # 登录页
            ├── register/register.vue  # 注册页
            ├── layout/layout.vue      # 主布局
            ├── index/index.vue        # 群聊页
            ├── contact/contact.vue    # 好友管理页
            └── p2pchat/p2pchat.vue    # 私聊页
```

## 数据库设计

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| `users` | 用户表 | id, username, password, avatar, nickname, status(0离线/1在线) |
| `chat_messages` | 聊天消息表 | id, sender_id, receiver_id(0=群聊), content, message_type, send_time, read_status |
| `user_relations` | 好友关系表 | id, user_id, friend_id, status(0待审批/1已通过/2已拒绝/3已请求) |

## API 接口

### 用户模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/users/login` | 用户登录 |
| POST | `/users/register` | 用户注册 |
| PUT | `/users/modifyPassword` | 修改密码 |
| GET | `/users/logout/{id}` | 用户登出 |
| POST | `/users/updateProfile` | 更新个人信息 |

### 好友模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/friends/list/{id}` | 查询好友列表 |
| GET | `/friends/require/{id}` | 查询未处理的好友请求 |
| POST | `/friends/add` | 添加好友 |
| POST | `/friends/approve` | 审批好友请求 |
| POST | `/friends/reject` | 拒绝好友请求 |
| POST | `/friends/delete` | 删除好友 |

### 消息模块

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/messages/history` | 查询所有历史消息 |
| POST | `/messages/update` | 更新消息已读状态 |

### 文件模块

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/uploadavatar687` | 上传头像到阿里云 OSS |

## WebSocket 通信协议

WebSocket 端点：`ws://{host}:8080/ws/{userId}`

### 客户端 → 服务端

```json
{
  "type": "group",
  "content": {
    "senderId": 1,
    "receiverId": 0,
    "content": "你好"
  }
}
```

| type 取值 | 说明 |
|-----------|------|
| `group` | 群聊消息，receiverId 填 0 |
| `p2pchat` | 私聊消息，receiverId 填对方用户 ID |

### 服务端 → 客户端

**聊天消息** (type = `group` / `p2pchat`):
```json
{
  "type": "group",
  "content": {
    "senderId": 1,
    "receiverId": 0,
    "content": "你好",
    "nickname": "小明",
    "avatar": "http://...",
    "messageType": 1,
    "sendTime": "2024-01-01T12:00:00",
    "readStatus": 0
  }
}
```

**在线用户列表** (type = `onlineUsers`):
```json
{
  "type": "onlineUsers",
  "content": [
    {"id": 1, "username": "user1", "nickname": "小明", "avatar": "...", "status": 1}
  ]
}
```

**在线人数** (type = `onlineCount`):
```json
{
  "type": "onlineCount",
  "content": 10
}
```

## 开发说明

### 前端代理配置

`vite.config.js` 已配置 `/api` 代理到后端 `localhost:8080`，开发时前端请求 `/api/xxx` 会自动转发。

### 统一响应格式

```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

## 优化建议

以下是项目运行前建议优化的点：

### 必须处理

1. **数据库连接改成本地** — `application.yml` 中 `spring.datasource.url` 当前为 `jdbc:mysql://mysql:3306/live_chat`（Docker 主机名），需要改为 `localhost` 或实际 IP

2. **前端 WebSocket 地址改为可配置** — `front-end/src/utils/websocket.js:45` 中 `ws://47.96.247.155:8080/ws/${userId}` 是硬编码的生产 IP，建议改为环境变量或相对路径：
   ```js
   const wsUrl = import.meta.env.VITE_WS_URL || `ws://${location.hostname}:8080`
   websocket = new WebSocket(`${wsUrl}/ws/${userId}`)
   ```
   然后在 `.env.development` 中配置 `VITE_WS_URL=ws://localhost:8080`

### 建议处理

3. **JWT 密钥不要明文硬编码** — `application.yml` 中 `live-chat.jwt.secret-key: xuziran`，建议改为环境变量 `${JWT_SECRET}` 或使用更复杂的密钥

4. **私聊消息广播问题** — 当前 `WebSocketServer` 收到私聊消息后调用 `sendToAllClient()` 广播给所有在线用户，客户端才做过滤。建议改为 `sendToOneClient()` 仅发送给收发双方，避免消息泄露

5. **OSS 配置改为可选** — 如果不配置 OSS，头像上传接口会报错。建议添加开关或提供默认头像逻辑

6. **注册添加用户名唯一校验** — 当前注册接口直接 INSERT，没有检查用户名是否已存在，会导致数据库抛唯一键异常

7. **Redis 配置清理** — 项目中声明了 Redis 依赖和 `@EnableCaching` 注解，但消息缓存逻辑实际上并未实现（只在 `MessagesController` 的注释中提及）。建议要么实现缓存，要么移除相关依赖

8. **前端包名修正** — `front-end/package.json` 中的 `name` 字段当前为 `vue-tlias-management`，建议改为 `live-chat-frontend`
