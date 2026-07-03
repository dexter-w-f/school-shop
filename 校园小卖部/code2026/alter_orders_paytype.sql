-- 在线支付功能: 给 orders 表添加 pay_type 字段
ALTER TABLE `orders` ADD COLUMN `pay_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式: 余额支付/支付宝模拟/微信模拟' AFTER `deliver`;
