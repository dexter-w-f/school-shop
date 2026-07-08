package com.example.config;

import com.example.mapper.SeckillActivityMapper;
import com.example.utils.RedisKeyUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.stereotype.Component;

@Component
public class SeckillRedisStartupInitializer implements CommandLineRunner {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SeckillActivityMapper seckillActivityMapper;

    public SeckillRedisStartupInitializer(RedisTemplate<String, Object> redisTemplate,
                                          SeckillActivityMapper seckillActivityMapper) {
        this.redisTemplate = redisTemplate;
        this.seckillActivityMapper = seckillActivityMapper;
    }

    @Override
    public void run(String... args) {
        redisTemplate.execute((RedisConnection connection) -> {
            String pattern = "seckill:stock:*";

            try (var cursor = connection.scan(org.springframework.data.redis.core.ScanOptions.scanOptions().match(pattern).count(1000).build())) {
                while (cursor.hasNext()) {
                    byte[] keyBytes = cursor.next();
                    String keyStr = new String(keyBytes, java.nio.charset.StandardCharsets.UTF_8);

                    Long ttl = connection.ttl(keyBytes);
                    if (ttl == null || ttl < 0) {
                        String idStr = keyStr.replace("seckill:stock:", "");
                        java.time.Duration ttlDuration = RedisKeyUtils.seckillStockTtl();
                        try {
                            int activityId = Integer.parseInt(idStr);
                            var activity = seckillActivityMapper.selectById(activityId);
                            if (activity != null && activity.getEndTime() != null) {
                                ttlDuration = RedisKeyUtils.seckillStockTtl(activity.getEndTime());
                            }
                        } catch (NumberFormatException e) {
                            // 无法解析活动ID，保留兜底 TTL
                        }
                        connection.expire(keyBytes, ttlDuration.getSeconds());
                    }
                }
            }

            return null;
        });
    }
}
