-- ============================================================
-- 校园小卖部 索引补充脚本（在 schema_full.sql 之后执行）
-- 目的：为高频过滤/关联列补索引，避免全表扫描
-- 说明：MySQL 的 ADD INDEX 不支持 IF NOT EXISTS，这里用存储过程封装，
--       已存在的索引会跳过，因此可重复执行。
-- 用法: mysql -uroot -p < indexes.sql
-- ============================================================
USE `shop`;

DROP PROCEDURE IF EXISTS `add_index_if_absent`;
DELIMITER $$
CREATE PROCEDURE `add_index_if_absent`(IN p_table VARCHAR(64), IN p_index VARCHAR(64), IN p_cols VARCHAR(255))
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.statistics
        WHERE table_schema = DATABASE() AND table_name = p_table AND index_name = p_index
    ) THEN
        SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD INDEX `', p_index, '` (', p_cols, ')');
        PREPARE stmt FROM @ddl;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END$$
DELIMITER ;

-- orders：定时任务按 (status, time) 过滤；用户端按 user_id 查询；order_no 用于回调定位
CALL add_index_if_absent('orders', 'idx_orders_status_time', '`status`, `time`');
CALL add_index_if_absent('orders', 'idx_orders_user', '`user_id`');
CALL add_index_if_absent('orders', 'idx_orders_order_no', '`order_no`');

-- order_detail：按 order_id 关联
CALL add_index_if_absent('order_detail', 'idx_order_detail_order', '`order_id`');

-- reply：帖子详情按 post_id 拉取回复
CALL add_index_if_absent('reply', 'idx_reply_post', '`post_id`');

-- post：我的帖子按 (user_id, status) 过滤
CALL add_index_if_absent('post', 'idx_post_user_status', '`user_id`, `status`');

-- refund_orders：售后列表按 order_id / user_id 查询
CALL add_index_if_absent('refund_orders', 'idx_refund_order', '`order_id`');
CALL add_index_if_absent('refund_orders', 'idx_refund_user', '`user_id`');

-- chat_message：客服会话按 user_id 查询
CALL add_index_if_absent('chat_message', 'idx_chat_user', '`user_id`');

-- collect：按 (user_id, goods_id) 查询收藏
CALL add_index_if_absent('collect', 'idx_collect_user_goods', '`user_id`, `goods_id`');

-- comment：商品详情/我的评价按 goods_id、user_id 查询
CALL add_index_if_absent('comment', 'idx_comment_goods', '`goods_id`');
CALL add_index_if_absent('comment', 'idx_comment_user', '`user_id`');

DROP PROCEDURE IF EXISTS `add_index_if_absent`;
