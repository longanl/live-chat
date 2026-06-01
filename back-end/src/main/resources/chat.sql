-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
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
-- Table structure for table `chat_messages`
--

DROP TABLE IF EXISTS `chat_messages`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `chat_messages` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '消息ID，主键',
  `sender_id` bigint DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint DEFAULT NULL COMMENT '接收者ID(0表示群聊)',
  `message_type` tinyint DEFAULT '1' COMMENT '消息类型(1:文本,2:文件)',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消息内容',
  `file_id` bigint DEFAULT NULL COMMENT '文件ID(如果是文件消息)',
  `send_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `read_status` tinyint DEFAULT '0' COMMENT '阅读状态(0:未读,1:已读)',
  PRIMARY KEY (`id`),
  KEY `sender_id` (`sender_id`),
  KEY `receiver_id` (`receiver_id`)
) ENGINE=InnoDB AUTO_INCREMENT=76 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `chat_messages`
--

LOCK TABLES `chat_messages` WRITE;
/*!40000 ALTER TABLE `chat_messages` DISABLE KEYS */;
INSERT INTO `chat_messages` VALUES (1,2,1,1,'你好',NULL,'2025-07-15 09:56:45',1),(2,1,2,1,'你好，最近怎么样？',NULL,'2023-01-01 10:00:00',1),(3,2,1,1,'我很好，谢谢！你呢？',NULL,'2023-01-01 10:01:00',1),(4,1,2,1,'我也不错，准备周末去爬山。',NULL,'2023-01-02 14:30:00',1),(5,2,0,1,'大家好，我是新成员！',NULL,'2023-01-03 09:15:00',1),(6,1,0,1,'欢迎加入群聊！',NULL,'2023-01-03 09:16:00',1),(7,2,1,2,'这是项目文档',1001,'2023-01-04 16:45:00',1),(12,1,0,1,'我是张三',NULL,'2025-07-16 17:27:07',1),(27,2,0,1,'你好',NULL,'2025-07-17 16:41:57',1),(28,2,0,1,'哈哈哈',NULL,'2025-07-17 16:43:30',1),(30,2,0,1,'测试',NULL,'2025-07-18 12:00:11',1),(56,5,2,1,'123456',NULL,'2025-07-20 12:24:40',1),(57,5,2,1,'123456',NULL,'2025-07-20 12:27:31',1),(58,5,1,1,'123456',NULL,'2025-07-20 12:27:53',1),(59,1,5,1,'哈哈哈',NULL,'2025-07-20 12:28:34',1),(60,1,3,1,'我是张三',NULL,'2025-07-20 12:28:58',1),(63,5,3,1,'我也不知道',NULL,'2025-07-20 20:45:40',1),(64,3,5,1,'好吧',NULL,'2025-07-20 20:45:59',1),(68,1,2,1,'你也好',NULL,'2025-07-20 23:03:48',1),(69,5,2,1,'李四',NULL,'2025-07-20 23:04:10',1),(70,1,2,1,'李四',NULL,'2025-07-21 16:14:59',1),(71,2,1,1,'张三',NULL,'2025-07-21 16:15:09',1),(73,2,3,1,'你好',NULL,'2025-07-21 23:20:22',1),(74,3,2,1,'你好',NULL,'2025-07-22 12:21:57',0),(75,3,0,1,'大家好',NULL,'2025-07-22 12:22:19',0);
/*!40000 ALTER TABLE `chat_messages` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_relations`
--

DROP TABLE IF EXISTS `user_relations`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_relations` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `friend_id` bigint NOT NULL,
  `status` tinyint DEFAULT '0' COMMENT '0是未确认，1是已添加，2是已拒绝，3是以发送',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_friend` (`user_id`,`friend_id`),
  KEY `idx_friend_user` (`friend_id`,`user_id`),
  KEY `idx_status` (`status`,`user_id`,`friend_id`)
) ENGINE=InnoDB AUTO_INCREMENT=34 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_relations`
--

LOCK TABLES `user_relations` WRITE;
/*!40000 ALTER TABLE `user_relations` DISABLE KEYS */;
INSERT INTO `user_relations` VALUES (1,1,2,1,'2025-07-18 16:14:35'),(2,2,1,1,'2025-07-18 16:14:47'),(12,5,2,1,'2025-07-19 18:05:57'),(13,2,5,1,'2025-07-19 18:05:57'),(14,5,1,1,'2025-07-19 18:06:28'),(15,1,5,1,'2025-07-19 18:06:28'),(20,3,5,1,'2025-07-19 21:23:44'),(21,5,3,1,'2025-07-19 21:23:44'),(28,1,3,1,'2025-07-19 23:25:06'),(29,3,1,1,'2025-07-19 23:25:06'),(30,2,3,1,'2025-07-20 10:14:54'),(31,3,2,1,'2025-07-20 10:14:54');
/*!40000 ALTER TABLE `user_relations` ENABLE KEYS */;
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
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT 'https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/1.jpg' COMMENT '头像URL',
  `status` tinyint DEFAULT '0' COMMENT '用户状态(0:离线,1:在线)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT '未命名',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'zhangsan','123456','https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/1.jpg',0,'2025-07-15 09:54:31','2025-07-21 16:15:26','张三'),(2,'lisi','123456','https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/1.jpg',0,'2025-07-15 20:32:29','2025-07-21 23:21:27','李鬼'),(3,'xuziran','123456','https://web-framework.oss-cn-hangzhou.aliyuncs.com/2023/2.jpg',1,'2025-07-17 21:36:48','2025-07-22 12:21:45','人民'),(5,'wangwu','000000','https://java-ai-xizran.oss-cn-beijing.aliyuncs.com/微信图片_20250619120320.png',0,'2025-07-17 21:48:12','2025-07-21 16:54:04','王五');
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-07-22 12:30:59
