package com.example.utils;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.DateUnit;

import java.time.Duration;
import java.util.Date;

public class RedisKeyUtils {

    private RedisKeyUtils() {}

    public static String seckillStockKey(Integer activityId) {
        return "seckill:stock:" + activityId;
    }

    /**
     * 秒杀库存 key 的兜底过期时间。
     * 这里给一个固定窗口，避免活动异常结束时 key 长期残留。
     */
    public static Duration seckillStockTtl() {
        return Duration.ofHours(24);
    }

    /**
     * 根据活动结束时间计算秒杀库存 key 的过期时间。
     * 如果活动还未结束，使用距离结束时间的剩余秒数；
     * 如果活动已结束，给一个较短兜底时间，便于自动清理；
     * 如果解析失败，回退到固定 24 小时兜底。
     */
    public static Duration seckillStockTtl(String endTime) {
        if (endTime == null || endTime.isBlank()) {
            return seckillStockTtl();
        }
        try {
            Date endDate = DateUtil.parse(endTime);
            long seconds = DateUtil.between(new Date(), endDate, DateUnit.SECOND);
            if (seconds <= 0) {
                return Duration.ofHours(1);
            }
            long capped = Math.min(seconds, seckillStockTtl().getSeconds());
            return Duration.ofSeconds(capped);
        } catch (Exception e) {
            return seckillStockTtl();
        }
    }
}
