package com.example.mapper;

import com.example.entity.SeckillActivity;
import java.util.List;

public interface SeckillActivityMapper {
    int insert(SeckillActivity activity);
    int deleteById(Integer id);
    int updateById(SeckillActivity activity);
    SeckillActivity selectById(Integer id);
    List<SeckillActivity> selectAll(SeckillActivity activity);

    /**
     * 原子扣减秒杀库存，库存不足时影响行数为 0，避免读改写导致的超卖。
     */
    int deductStock(Integer id);
}
