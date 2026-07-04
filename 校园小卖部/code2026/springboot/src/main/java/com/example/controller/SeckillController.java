package com.example.controller;

import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.Goods;
import com.example.entity.Orders;
import com.example.entity.OrderDetail;
import com.example.entity.SeckillActivity;
import com.example.entity.User;
import com.example.mapper.*;
import com.example.service.OrdersService;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Resource private SeckillActivityMapper seckillActivityMapper;
    @Resource private GoodsMapper goodsMapper;
    @Resource private UserMapper userMapper;
    @Resource private OrdersMapper ordersMapper;
    @Resource private OrderDetailMapper orderDetailMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    // ---- Admin CRUD ----
    @GetMapping("/list")
    public Result list(SeckillActivity activity) {
        List<SeckillActivity> all = seckillActivityMapper.selectAll(null);
        String now = DateUtil.now();
        for (SeckillActivity a : all) {
            boolean ch = false;
            if ("未开始".equals(a.getStatus()) && a.getStartTime() != null && a.getStartTime().compareTo(now) <= 0) {
                a.setStatus("进行中"); ch = true;
            }
            if ("进行中".equals(a.getStatus()) && a.getEndTime() != null && a.getEndTime().compareTo(now) < 0) {
                a.setStatus("已结束"); ch = true;
            }
            if (ch) seckillActivityMapper.updateById(a);
        }
        return Result.success(seckillActivityMapper.selectAll(activity));
    }

    @PostMapping("/add")
    public Result add(@RequestBody SeckillActivity activity) {
        if (activity.getGoodsId() == null) return Result.error("请选择商品");
        List<SeckillActivity> existing = seckillActivityMapper.selectAll(null);
        for (SeckillActivity e : existing) {
            if (e.getGoodsId() == null || !e.getGoodsId().equals(activity.getGoodsId())) continue;
            if ("未开始".equals(e.getStatus()) || "进行中".equals(e.getStatus())) {
                return Result.error("该商品已有进行中或待开始的秒杀活动");
            }
        }
        activity.setCreateTime(DateUtil.now());
        activity.setStatus("未开始");
        seckillActivityMapper.insert(activity);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id) {
        seckillActivityMapper.deleteById(id);
        redisTemplate.delete("seckill:stock:" + id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody SeckillActivity activity) {
        seckillActivityMapper.updateById(activity);
        return Result.success();
    }

    // ---- User ----
    @GetMapping("/active")
    public Result active() {
        SeckillActivity q = new SeckillActivity();
        List<SeckillActivity> list = seckillActivityMapper.selectAll(null);
        String now = DateUtil.now();
        for (SeckillActivity a : list) {
            if ("未开始".equals(a.getStatus()) && a.getStartTime().compareTo(now) <= 0) {
                a.setStatus("进行中");
                seckillActivityMapper.updateById(a);
            }
            if ("进行中".equals(a.getStatus()) && a.getEndTime().compareTo(now) < 0) {
                a.setStatus("已结束");
                seckillActivityMapper.updateById(a);
            }
        }
        // 返回进行中的活动
        SeckillActivity activeQ = new SeckillActivity();
        activeQ.setStatus("进行中");
        return Result.success(seckillActivityMapper.selectAll(activeQ));
    }

    @PostMapping("/buy")
    @Transactional
    public Result buy(@RequestParam Integer userId, @RequestParam Integer activityId) {
        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) return Result.error("活动不存在");
        if (!"进行中".equals(activity.getStatus())) return Result.error("活动未开始或已结束");
        if (activity.getTotalStock() <= 0) return Result.error("已售罄");

        User user = userMapper.selectById(userId);
        if (user == null) return Result.error("用户不存在");

        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        if (goods == null) return Result.error("商品不存在");
        if (goods.getStore() < 1) return Result.error("商品库存不足");

        // Redis 原子扣库存
        String key = "seckill:stock:" + activityId;
        Boolean hasKey = redisTemplate.hasKey(key);
        if (Boolean.FALSE.equals(hasKey)) {
            redisTemplate.opsForValue().set(key, activity.getTotalStock());
        }
        Long stock = redisTemplate.opsForValue().decrement(key);
        if (stock == null || stock < 0) {
            redisTemplate.opsForValue().increment(key);
            seckillActivityMapper.updateById(activity);
            return Result.error("秒杀已结束");
        }

        // 创建订单
        Orders order = new Orders();
        order.setUserId(userId);
        order.setStatus("待支付");
        order.setTotal(activity.getSeckillPrice());
        order.setTime(DateUtil.now());
        order.setOrderNo(DateUtil.format(new Date(), "yyyyMMdd") + System.currentTimeMillis());
        ordersMapper.insert(order);

        OrderDetail detail = new OrderDetail();
        detail.setOrderId(order.getId());
        detail.setGoodsId(goods.getId());
        detail.setGoodsName(goods.getName());
        detail.setGoodsImg(goods.getImg());
        detail.setGoodsPrice(activity.getSeckillPrice());
        detail.setNum(1);
        orderDetailMapper.insert(detail);

        // 更新秒杀库存
        activity.setTotalStock(activity.getTotalStock() - 1);
        seckillActivityMapper.updateById(activity);

        return Result.success(order.getId());
    }
}

