package com.example.mapper;

import com.example.entity.SeckillActivity;
import java.util.List;

public interface SeckillActivityMapper {
    int insert(SeckillActivity activity);
    int deleteById(Integer id);
    int updateById(SeckillActivity activity);
    SeckillActivity selectById(Integer id);
    List<SeckillActivity> selectAll(SeckillActivity activity);
}
