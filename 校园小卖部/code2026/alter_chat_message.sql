CREATE TABLE `chat_message` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` int(11) DEFAULT NULL COMMENT '用户ID',
  `question` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT '用户问题',
  `answer` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'AI回复',
  `time` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '时间',
  PRIMARY KEY (`id`) USING BTREE,
  KEY `user_id` (`user_id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='聊天记录';
