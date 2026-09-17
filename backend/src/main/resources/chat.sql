-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
-- 全新会话(conversation)模型，详见 problems/05-数据库重设计方案.md
-- 好友关系拆分为 friend_relations + friend_requests，详见 problems/08-用户关系表重设计.md
-- ------------------------------------------------------
-- Host: localhost    Database: live_chat
-- ------------------------------------------------------
-- Server version	8.0.34

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
--
-- Current Database: `live_chat`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `live_chat` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `live_chat`;

--
-- Table structure for table `conversations`
--

DROP TABLE IF EXISTS `conversations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `type` tinyint NOT NULL COMMENT '1-私聊 2-群聊',
  `name` varchar(100) DEFAULT NULL COMMENT '群聊名称；私聊为空',
  `owner_id` bigint DEFAULT NULL COMMENT '群主用户ID；私聊为空',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '群头像',
  `notice` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '群公告',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话（1私聊/2群聊）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversations`
--

LOCK TABLES `conversations` WRITE;
/*!40000 ALTER TABLE `conversations` DISABLE KEYS */;
INSERT INTO `conversations` VALUES (1,2,'技术讨论群',NULL,'2025-07-15 09:54:00'),(2,1,NULL,NULL,'2025-07-18 16:00:00');
/*!40000 ALTER TABLE `conversations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `conversation_members`
--

DROP TABLE IF EXISTS `conversation_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `conversation_members` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  `last_read_msg_id` bigint NOT NULL DEFAULT '0' COMMENT '已读游标：已读到的最大消息id',
  `join_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_conv_user` (`conversation_id`,`user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话成员 + 已读游标';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `conversation_members`
--

LOCK TABLES `conversation_members` WRITE;
/*!40000 ALTER TABLE `conversation_members` DISABLE KEYS */;
INSERT INTO `conversation_members` VALUES
(1,1,1,0,'2025-07-15 09:54:31'),
(2,1,2,0,'2025-07-15 20:32:29'),
(3,1,3,0,'2025-07-17 21:36:48'),
(4,1,5,0,'2025-07-17 21:48:12'),
(5,2,1,0,'2025-07-18 16:00:00'),
(6,2,2,0,'2025-07-18 16:00:01');
/*!40000 ALTER TABLE `conversation_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `conversation_id` bigint NOT NULL COMMENT '所属会话',
  `sender_id` bigint NOT NULL COMMENT '发送者ID',
  `message_type` tinyint NOT NULL DEFAULT '1' COMMENT '消息类型(1:文本,2:文件)',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '文本正文（文件消息可为说明文字）',
  `file_url` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '文件URL',
  `file_name` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '原始文件名',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'image/video/file',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `client_msg_id` varchar(64) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '客户端幂等UUID',
  `recalled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤回(0=否,1=是)',
  PRIMARY KEY (`id`),
  KEY `idx_conv_id` (`conversation_id`,`id`),
  KEY `idx_sender` (`sender_id`),
  UNIQUE KEY `uk_conv_clientmsg` (`conversation_id`,`client_msg_id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天消息（归属会话）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` (`id`,`conversation_id`,`sender_id`,`message_type`,`content`,`file_url`,`file_name`,`file_size`,`file_type`,`send_time`) VALUES
(1,1,1,1,'大家好，我是张三',NULL,NULL,NULL,NULL,'2025-07-15 09:56:45'),
(2,1,2,1,'你好，最近怎么样？',NULL,NULL,NULL,NULL,'2023-01-01 10:00:00'),
(3,1,2,1,'我很好，谢谢！你呢？',NULL,NULL,NULL,NULL,'2023-01-01 10:01:00'),
(4,1,1,1,'我也不错，准备周末去爬山。',NULL,NULL,NULL,NULL,'2023-01-02 14:30:00'),
(5,1,2,1,'大家好，我是新成员！',NULL,NULL,NULL,NULL,'2023-01-03 09:15:00'),
(6,1,1,1,'欢迎加入群聊！',NULL,NULL,NULL,NULL,'2023-01-03 09:16:00'),
(7,1,2,2,'项目文档',NULL,NULL,NULL,NULL,'2023-01-04 16:45:00'),
(12,1,1,1,'我是张三',NULL,NULL,NULL,NULL,'2025-07-16 17:27:07'),
(27,1,2,1,'你好',NULL,NULL,NULL,NULL,'2025-07-17 16:41:57'),
(28,1,2,1,'哈哈哈',NULL,NULL,NULL,NULL,'2025-07-17 16:43:30'),
(30,1,2,1,'测试',NULL,NULL,NULL,NULL,'2025-07-18 12:00:11'),
(56,2,5,1,'123456',NULL,NULL,NULL,NULL,'2025-07-20 12:24:40'),
(57,2,5,1,'123456',NULL,NULL,NULL,NULL,'2025-07-20 12:27:31'),
(70,1,1,1,'李四',NULL,NULL,NULL,NULL,'2025-07-21 16:14:59'),
(71,1,2,1,'张三',NULL,NULL,NULL,NULL,'2025-07-21 16:15:09'),
(73,1,2,1,'你好',NULL,NULL,NULL,NULL,'2025-07-21 23:20:22'),
(74,1,3,1,'你好',NULL,NULL,NULL,NULL,'2025-07-22 12:21:57'),
(75,1,3,1,'大家好',NULL,NULL,NULL,NULL,'2025-07-22 12:22:19');
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID，主键',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '0' COMMENT '用户状态(0:离线,1:在线)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '未命名',
  `signature` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '个性签名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES
(1,'zhangsan','123456',NULL,0,'2025-07-15 09:54:31','2025-07-21 16:15:26','张三'),
(2,'lisi','123456',NULL,0,'2025-07-15 20:32:29','2025-07-21 23:21:27','李鬼'),
(3,'xuziran','123456',NULL,1,'2025-07-17 21:36:48','2025-07-22 12:21:45','人民'),
(5,'wangwu','000000',NULL,0,'2025-07-17 21:48:12','2025-07-21 16:54:04','王五');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_relations`
--

DROP TABLE IF EXISTS `friend_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL COMMENT '好友对中较小的一方',
  `friend_id` bigint NOT NULL COMMENT '好友对中较大的一方',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_pair` (`user_id`,`friend_id`),
  KEY `idx_friend` (`friend_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系（一对好友一行，user_id < friend_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_relations`
--

LOCK TABLES `friend_relations` WRITE;
/*!40000 ALTER TABLE `friend_relations` DISABLE KEYS */;
INSERT INTO `friend_relations` VALUES
(1,1,2,'2025-07-18 16:14:35'),
(2,2,5,'2025-07-19 18:05:57'),
(3,1,5,'2025-07-19 18:06:28'),
(4,3,5,'2025-07-19 21:23:44'),
(5,1,3,'2025-07-19 23:25:06'),
(6,2,3,'2025-07-20 10:14:54');
/*!40000 ALTER TABLE `friend_relations` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friend_requests`
--

DROP TABLE IF EXISTS `friend_requests`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `friend_requests` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint NOT NULL COMMENT '申请人ID',
  `to_user_id` bigint NOT NULL COMMENT '接收人ID',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '0-待处理 1-已同意 2-已拒绝(可再次申请)',
  `handle_time` datetime DEFAULT NULL COMMENT '处理时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_from_to` (`from_user_id`,`to_user_id`),
  KEY `idx_to_status` (`to_user_id`,`status`),
  KEY `idx_from` (`from_user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请（单向，表达方向）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friend_requests`
--

LOCK TABLES `friend_requests` WRITE;
/*!40000 ALTER TABLE `friend_requests` DISABLE KEYS */;
INSERT INTO `friend_requests` VALUES
(1,5,1,0,NULL,'2025-07-22 10:00:00'),
(2,5,2,2,'2025-07-22 10:05:00','2025-07-22 10:02:00');
/*!40000 ALTER TABLE `friend_requests` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_blocks`
--

DROP TABLE IF EXISTS `user_blocks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!5503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_blocks` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `blocker_id` bigint NOT NULL COMMENT '拉黑者ID',
  `blocked_id` bigint NOT NULL COMMENT '被拉黑者ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_blocker_blocked` (`blocker_id`,`blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拉黑关系（blocker_id 拉黑 blocked_id）';
/*!40101 SET character_set_client = @saved_cs_client */;

/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-22 12:30:59
--
-- 阶段二增量 DDL（对已存在的库生效，DROP+重建时这些语句为 NO-OP）
--
ALTER TABLE IF NOT EXISTS `users` ADD COLUMN `signature` varchar(255) DEFAULT NULL COMMENT '个性签名' AFTER `nickname`;
ALTER TABLE IF NOT EXISTS `conversations` ADD COLUMN `avatar` varchar(255) DEFAULT NULL COMMENT '群头像' AFTER `owner_id`, ADD COLUMN `notice` varchar(500) DEFAULT NULL COMMENT '群公告' AFTER `avatar`;
ALTER TABLE IF NOT EXISTS `chat_messages` ADD COLUMN `client_msg_id` varchar(64) DEFAULT NULL COMMENT '客户端幂等UUID' AFTER `send_time`, ADD COLUMN `recalled` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已撤回(0=否,1=是)' AFTER `client_msg_id`;
ALTER TABLE IF NOT EXISTS `chat_messages` ADD UNIQUE INDEX `uk_conv_clientmsg` (`conversation_id`,`client_msg_id`);
CREATE TABLE IF NOT EXISTS `user_blocks` (`id` bigint NOT NULL AUTO_INCREMENT, `blocker_id` bigint NOT NULL COMMENT '拉黑者ID', `blocked_id` bigint NOT NULL COMMENT '被拉黑者ID', `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '拉黑时间', PRIMARY KEY (`id`), UNIQUE KEY `uk_blocker_blocked` (`blocker_id`,`blocked_id`)) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='拉黑关系（blocker_id 拉黑 blocked_id）';
