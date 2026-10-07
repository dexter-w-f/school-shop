-- 为 goods 表补充乐观锁版本号，用于库存并发调整。
ALTER TABLE goods ADD COLUMN version int(11) NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER recommend;

-- 为 order_detail 表补充秒杀活动ID，便于售后/取消时回滚秒杀库存。
ALTER TABLE order_detail ADD COLUMN activity_id int(11) DEFAULT NULL COMMENT '秒杀活动ID' AFTER num;
