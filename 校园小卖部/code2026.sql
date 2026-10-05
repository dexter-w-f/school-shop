-- ============================================================================
-- ⚠️ 本文件已过时，请勿用于建库！
--
-- 这是早期从 Navicat 导出的残留，**只包含 `admin` 一张表**，
-- 缺少 user / goods / category / cart / collect / comment / carousel /
-- orders / order_detail 等核心业务表。
-- 用它初始化数据库会导致 7 个 alter_*.sql 从第一句就失败，系统无法运行。
--
-- ✅ 正确建库方式（完整 17 张表，由本地可运行的 shop 库真实导出）：
--    mysql -uroot -p < 校园小卖部/code2026/schema_full.sql
--    mysql -uroot -p < 校园小卖部/code2026/indexes.sql
--    或直接用仓库根目录 docker-compose.yml（已挂载上述两个脚本）。
-- ============================================================================

/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 50737
 Source Host           : localhost:3306
 Source Schema         : system

 Target Server Type    : MySQL
 Target Server Version : 50737
 File Encoding         : 65001

 Date: 01/07/2024 16:38:45
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `username` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '账号',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '密码',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '名称',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '管理员信息' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', '$2a$10$jcUgvGD5J2HaR/gRdFP4muew4sPUqB9KQSVcXfK57s5Db/up..0vS', '管理员', 'http://localhost:9090/files/download/avatar.png', '管理员');
-- 密码为 BCrypt 值，明文 'admin'，已用项目自身的 BCryptPasswordEncoder 实测校验通过
-- （此前版本里的哈希经实测无法通过校验，会导致 admin 登录失败）
-- 普通用户默认口令为 123456，见 update_passwords.sql

SET FOREIGN_KEY_CHECKS = 1;
