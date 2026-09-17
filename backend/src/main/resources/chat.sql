-- ============================================================
-- live_chat 数据库完整初始化脚本
-- MySQL 8.0+
--
-- 数据库：live_chat
--
-- 设计说明：
-- 1. conversations：会话
--    type = 1 私聊
--    type = 2 群聊
--
-- 2. conversation_members：会话成员 + 已读游标
--
-- 3. chat_messages：聊天消息
--    client_msg_id 用于客户端消息幂等
--    recalled 用于消息撤回
--
-- 4. friend_relations：好友关系
--    user_id < friend_id
--    一对好友只保存一条记录
--
-- 5. friend_requests：好友申请
--    单向记录申请方向
--    拒绝后允许再次申请，因此不设置
--    (from_user_id, to_user_id) 唯一约束
--
-- 6. user_blocks：拉黑关系
-- ============================================================


-- ============================================================
-- 1. 创建数据库
-- ============================================================

CREATE DATABASE IF NOT EXISTS `live_chat`
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE `live_chat`;


-- ============================================================
-- 2. 基础设置
-- ============================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET UNIQUE_CHECKS = 0;
SET SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO';


-- ============================================================
-- 3. 删除旧表
--
-- 由于是完整初始化脚本，因此直接 DROP + CREATE。
-- 执行本脚本会清空当前 live_chat 数据库中的业务数据。
-- ============================================================

DROP TABLE IF EXISTS `user_blocks`;
DROP TABLE IF EXISTS `friend_requests`;
DROP TABLE IF EXISTS `friend_relations`;
DROP TABLE IF EXISTS `chat_messages`;
DROP TABLE IF EXISTS `conversation_members`;
DROP TABLE IF EXISTS `conversations`;
DROP TABLE IF EXISTS `users`;


-- ============================================================
-- 4. users
-- ============================================================

CREATE TABLE `users` (
                         `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',

                         `username` varchar(50)
                                  COLLATE utf8mb4_unicode_ci
                                     NOT NULL
                             COMMENT '用户名',

                         `password` varchar(100)
                                  COLLATE utf8mb4_unicode_ci
                                     NOT NULL
                             COMMENT '加密后的密码',

                         `avatar` varchar(255)
                                  COLLATE utf8mb4_unicode_ci
                             DEFAULT NULL
                             COMMENT '头像URL',

                         `status` tinyint
                             DEFAULT '0'
                             COMMENT '用户状态(0:离线,1:在线)',

                         `create_time` datetime
                             DEFAULT CURRENT_TIMESTAMP
                             COMMENT '创建时间',

                         `update_time` datetime
                             DEFAULT NULL
                             ON UPDATE CURRENT_TIMESTAMP
                             COMMENT '更新时间',

                         `nickname` varchar(50)
                                  COLLATE utf8mb4_unicode_ci
                             DEFAULT '未命名'
                             COMMENT '昵称',

                         `signature` varchar(255)
                                  COLLATE utf8mb4_unicode_ci
                             DEFAULT NULL
                             COMMENT '个性签名',

                         PRIMARY KEY (`id`),

                         UNIQUE KEY `uk_username` (`username`),

                         KEY `idx_status` (`status`)

) ENGINE=InnoDB
  AUTO_INCREMENT=13
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='用户';


-- ============================================================
-- 5. users 测试数据
-- ============================================================

INSERT INTO `users`
(
    `id`,
    `username`,
    `password`,
    `avatar`,
    `status`,
    `create_time`,
    `update_time`,
    `nickname`,
    `signature`
)
VALUES
    (
        1,
        'zhangsan',
        '123456',
        NULL,
        0,
        '2025-07-15 09:54:31',
        '2025-07-21 16:15:26',
        '张三',
        NULL
    ),
    (
        2,
        'lisi',
        '123456',
        NULL,
        0,
        '2025-07-15 20:32:29',
        '2025-07-21 23:21:27',
        '李鬼',
        NULL
    ),
    (
        3,
        'xuziran',
        '123456',
        NULL,
        1,
        '2025-07-17 21:36:48',
        '2025-07-22 12:21:45',
        '人民',
        NULL
    ),
    (
        5,
        'wangwu',
        '000000',
        NULL,
        0,
        '2025-07-17 21:48:12',
        '2025-07-21 16:54:04',
        '王五',
        NULL
    );


-- ============================================================
-- 6. conversations
-- ============================================================

CREATE TABLE `conversations` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,

                                 `type` tinyint NOT NULL
                                     COMMENT '1-私聊 2-群聊',

                                 `name` varchar(100)
                                     DEFAULT NULL
                                     COMMENT '群聊名称；私聊为空',

                                 `owner_id` bigint
                                     DEFAULT NULL
                                     COMMENT '群主用户ID；私聊为空',

                                 `avatar` varchar(255)
                                          COLLATE utf8mb4_unicode_ci
                                     DEFAULT NULL
                                     COMMENT '群头像',

                                 `notice` varchar(500)
                                          COLLATE utf8mb4_unicode_ci
                                     DEFAULT NULL
                                     COMMENT '群公告',

                                 `create_time` datetime
                                     DEFAULT CURRENT_TIMESTAMP,

                                 PRIMARY KEY (`id`),

                                 KEY `idx_type` (`type`)

) ENGINE=InnoDB
  AUTO_INCREMENT=3
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='会话（1私聊/2群聊）';


-- ============================================================
-- 7. conversations 测试数据
-- ============================================================

INSERT INTO `conversations`
(
    `id`,
    `type`,
    `name`,
    `owner_id`,
    `avatar`,
    `notice`,
    `create_time`
)
VALUES
    (
        1,
        2,
        '技术讨论群',
        NULL,
        NULL,
        NULL,
        '2025-07-15 09:54:00'
    ),
    (
        2,
        1,
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-18 16:00:00'
    );


-- ============================================================
-- 8. conversation_members
-- ============================================================

CREATE TABLE `conversation_members` (
                                        `id` bigint NOT NULL AUTO_INCREMENT,

                                        `conversation_id` bigint NOT NULL
                                            COMMENT '会话ID',

                                        `user_id` bigint NOT NULL
                                            COMMENT '用户ID',

                                        `last_read_msg_id` bigint NOT NULL DEFAULT '0'
                                            COMMENT '已读游标：已读到的最大消息ID',

                                        `join_time` datetime
                                                                           DEFAULT CURRENT_TIMESTAMP
                                            COMMENT '加入时间',

                                        PRIMARY KEY (`id`),

                                        UNIQUE KEY `uk_conv_user`
                                            (`conversation_id`, `user_id`),

                                        KEY `idx_user`
                                            (`user_id`)

) ENGINE=InnoDB
  AUTO_INCREMENT=8
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='会话成员 + 已读游标';


-- ============================================================
-- 9. conversation_members 测试数据
-- ============================================================

INSERT INTO `conversation_members`
(
    `id`,
    `conversation_id`,
    `user_id`,
    `last_read_msg_id`,
    `join_time`
)
VALUES
    (
        1,
        1,
        1,
        0,
        '2025-07-15 09:54:31'
    ),
    (
        2,
        1,
        2,
        0,
        '2025-07-15 20:32:29'
    ),
    (
        3,
        1,
        3,
        0,
        '2025-07-17 21:36:48'
    ),
    (
        4,
        1,
        5,
        0,
        '2025-07-17 21:48:12'
    ),
    (
        5,
        2,
        1,
        0,
        '2025-07-18 16:00:00'
    ),
    (
        6,
        2,
        2,
        0,
        '2025-07-18 16:00:01'
    );


-- ============================================================
-- 10. chat_messages
-- ============================================================

CREATE TABLE `chat_messages` (
                                 `id` bigint NOT NULL AUTO_INCREMENT,

                                 `conversation_id` bigint NOT NULL
                                     COMMENT '所属会话',

                                 `sender_id` bigint NOT NULL
                                     COMMENT '发送者ID',

                                 `message_type` tinyint NOT NULL DEFAULT '1'
                                     COMMENT '消息类型(1:文本,2:文件)',

                                 `content` text
                                          COLLATE utf8mb4_unicode_ci
                                     COMMENT '文本正文（文件消息可为说明文字）',

                                 `file_url` varchar(255)
                                          COLLATE utf8mb4_unicode_ci
                                                                 DEFAULT NULL
                                     COMMENT '文件URL',

                                 `file_name` varchar(255)
                                          COLLATE utf8mb4_unicode_ci
                                                                 DEFAULT NULL
                                     COMMENT '原始文件名',

                                 `file_size` bigint
                                                                 DEFAULT NULL
                                     COMMENT '文件大小(字节)',

                                 `file_type` varchar(10)
                                          COLLATE utf8mb4_unicode_ci
                                                                 DEFAULT NULL
                                     COMMENT 'image/video/file',

                                 `send_time` datetime
                                                                 DEFAULT CURRENT_TIMESTAMP
                                     COMMENT '发送时间',

                                 `client_msg_id` varchar(64)
                                          COLLATE utf8mb4_unicode_ci
                                                                 DEFAULT NULL
                                     COMMENT '客户端幂等UUID',

                                 `recalled` tinyint(1) NOT NULL DEFAULT '0'
                                     COMMENT '是否已撤回(0=否,1=是)',

                                 PRIMARY KEY (`id`),

                                 KEY `idx_conv_id`
                                     (`conversation_id`, `id`),

                                 KEY `idx_sender`
                                     (`sender_id`),

                                 UNIQUE KEY `uk_conv_clientmsg`
                                     (`conversation_id`, `client_msg_id`)

) ENGINE=InnoDB
  AUTO_INCREMENT=76
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='聊天消息（归属会话）';


-- ============================================================
-- 11. chat_messages 测试数据
-- ============================================================

INSERT INTO `chat_messages`
(
    `id`,
    `conversation_id`,
    `sender_id`,
    `message_type`,
    `content`,
    `file_url`,
    `file_name`,
    `file_size`,
    `file_type`,
    `send_time`,
    `client_msg_id`,
    `recalled`
)
VALUES
    (
        1,
        1,
        1,
        1,
        '大家好，我是张三',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-15 09:56:45',
        NULL,
        0
    ),
    (
        2,
        1,
        2,
        1,
        '你好，最近怎么样？',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-01 10:00:00',
        NULL,
        0
    ),
    (
        3,
        1,
        2,
        1,
        '我很好，谢谢！你呢？',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-01 10:01:00',
        NULL,
        0
    ),
    (
        4,
        1,
        1,
        1,
        '我也不错，准备周末去爬山。',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-02 14:30:00',
        NULL,
        0
    ),
    (
        5,
        1,
        2,
        1,
        '大家好，我是新成员！',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-03 09:15:00',
        NULL,
        0
    ),
    (
        6,
        1,
        1,
        1,
        '欢迎加入群聊！',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-03 09:16:00',
        NULL,
        0
    ),
    (
        7,
        1,
        2,
        2,
        '项目文档',
        NULL,
        NULL,
        NULL,
        NULL,
        '2023-01-04 16:45:00',
        NULL,
        0
    ),
    (
        12,
        1,
        1,
        1,
        '我是张三',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-16 17:27:07',
        NULL,
        0
    ),
    (
        27,
        1,
        2,
        1,
        '你好',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-17 16:41:57',
        NULL,
        0
    ),
    (
        28,
        1,
        2,
        1,
        '哈哈哈',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-17 16:43:30',
        NULL,
        0
    ),
    (
        30,
        1,
        2,
        1,
        '测试',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-18 12:00:11',
        NULL,
        0
    ),
    (
        56,
        2,
        5,
        1,
        '123456',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-20 12:24:40',
        NULL,
        0
    ),
    (
        57,
        2,
        5,
        1,
        '123456',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-20 12:27:31',
        NULL,
        0
    ),
    (
        70,
        1,
        1,
        1,
        '李四',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-21 16:14:59',
        NULL,
        0
    ),
    (
        71,
        1,
        2,
        1,
        '张三',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-21 16:15:09',
        NULL,
        0
    ),
    (
        73,
        1,
        2,
        1,
        '你好',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-21 23:20:22',
        NULL,
        0
    ),
    (
        74,
        1,
        3,
        1,
        '你好',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-22 12:21:57',
        NULL,
        0
    ),
    (
        75,
        1,
        3,
        1,
        '大家好',
        NULL,
        NULL,
        NULL,
        NULL,
        '2025-07-22 12:22:19',
        NULL,
        0
    );


-- ============================================================
-- 12. friend_relations
-- ============================================================

CREATE TABLE `friend_relations` (
                                    `id` bigint NOT NULL AUTO_INCREMENT,

                                    `user_id` bigint NOT NULL
                                        COMMENT '好友对中较小的一方',

                                    `friend_id` bigint NOT NULL
                                        COMMENT '好友对中较大的一方',

                                    `create_time` datetime
                                        DEFAULT CURRENT_TIMESTAMP,

                                    PRIMARY KEY (`id`),

                                    UNIQUE KEY `uk_pair`
                                        (`user_id`, `friend_id`),

                                    KEY `idx_friend`
                                        (`friend_id`)

) ENGINE=InnoDB
  AUTO_INCREMENT=7
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='好友关系（一对好友一行，user_id < friend_id）';


-- ============================================================
-- 13. friend_relations 测试数据
-- ============================================================

INSERT INTO `friend_relations`
(
    `id`,
    `user_id`,
    `friend_id`,
    `create_time`
)
VALUES
    (
        1,
        1,
        2,
        '2025-07-18 16:14:35'
    ),
    (
        2,
        2,
        5,
        '2025-07-19 18:05:57'
    ),
    (
        3,
        1,
        5,
        '2025-07-19 18:06:28'
    ),
    (
        4,
        3,
        5,
        '2025-07-19 21:23:44'
    ),
    (
        5,
        1,
        3,
        '2025-07-19 23:25:06'
    ),
    (
        6,
        2,
        3,
        '2025-07-20 10:14:54'
    );


-- ============================================================
-- 14. friend_requests
--
-- 注意：
-- 不使用 UNIQUE(from_user_id, to_user_id)
--
-- 因为：
--
-- A -> B 申请
--      ↓
--     拒绝
--      ↓
-- A -> B 可以再次申请
--
-- 历史申请记录应该保留下来。
-- ============================================================

CREATE TABLE `friend_requests` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,

                                   `from_user_id` bigint NOT NULL
                                       COMMENT '申请人ID',

                                   `to_user_id` bigint NOT NULL
                                       COMMENT '接收人ID',

                                   `status` tinyint NOT NULL DEFAULT '0'
                                       COMMENT '0-待处理 1-已同意 2-已拒绝',

                                   `handle_time` datetime
                                                             DEFAULT NULL
                                       COMMENT '处理时间',

                                   `create_time` datetime
                                                             DEFAULT CURRENT_TIMESTAMP
                                       COMMENT '申请时间',

                                   PRIMARY KEY (`id`),

                                   KEY `idx_from_to`
                                       (`from_user_id`, `to_user_id`),

                                   KEY `idx_to_status`
                                       (`to_user_id`, `status`),

                                   KEY `idx_from`
                                       (`from_user_id`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='好友申请（单向，表达方向）';


-- ============================================================
-- 15. friend_requests 测试数据
-- ============================================================

INSERT INTO `friend_requests`
(
    `id`,
    `from_user_id`,
    `to_user_id`,
    `status`,
    `handle_time`,
    `create_time`
)
VALUES
    (
        1,
        5,
        1,
        0,
        NULL,
        '2025-07-22 10:00:00'
    ),
    (
        2,
        5,
        2,
        2,
        '2025-07-22 10:05:00',
        '2025-07-22 10:02:00'
    );


-- ============================================================
-- 16. user_blocks
-- ============================================================

CREATE TABLE `user_blocks` (
                               `id` bigint NOT NULL AUTO_INCREMENT,

                               `blocker_id` bigint NOT NULL
                                   COMMENT '拉黑者ID',

                               `blocked_id` bigint NOT NULL
                                   COMMENT '被拉黑者ID',

                               `create_time` datetime
                                   DEFAULT CURRENT_TIMESTAMP
                                   COMMENT '拉黑时间',

                               PRIMARY KEY (`id`),

                               UNIQUE KEY `uk_blocker_blocked`
                                   (`blocker_id`, `blocked_id`)

) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
    COMMENT='拉黑关系（blocker_id 拉黑 blocked_id）';


-- ============================================================
-- 17. 恢复设置
-- ============================================================

SET FOREIGN_KEY_CHECKS = 1;
SET UNIQUE_CHECKS = 1;


-- ============================================================
-- 18. 初始化完成
-- ============================================================

SELECT
    'live_chat database initialization completed' AS message;

SELECT
    TABLE_NAME,
    TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = 'live_chat'
ORDER BY TABLE_NAME;