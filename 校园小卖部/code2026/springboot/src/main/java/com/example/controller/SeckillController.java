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

import com.example.exception.CustomException;
import com.example.utils.RedisKeyUtils;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    private static final Set<String> ADMIN_PATHS = Set.of(
            "/seckill/list",
            "/seckill/add",
            "/seckill/delete/",
            "/seckill/update"
    );

    @Resource private SeckillActivityMapper seckillActivityMapper;
    @Resource private GoodsMapper goodsMapper;
    @Resource private UserMapper userMapper;
    @Resource private OrdersMapper ordersMapper;
    @Resource private OrderDetailMapper orderDetailMapper;
    @Resource private RedisTemplate<String, Object> redisTemplate;

    // ---- Admin CRUD ----
    @GetMapping("/list")
    public Result list(SeckillActivity activity, HttpServletRequest request) {
        requireAdmin(request);
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
    public Result add(@RequestBody SeckillActivity activity, HttpServletRequest request) {
        requireAdmin(request);
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
    public Result delete(@PathVariable Integer id, HttpServletRequest request) {
        requireAdmin(request);
        seckillActivityMapper.deleteById(id);
        redisTemplate.delete(RedisKeyUtils.seckillStockKey(id));
        return Result.success();
    }

    @PutMapping("/update")
    public Result update(@RequestBody SeckillActivity activity, HttpServletRequest request) {
        requireAdmin(request);
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
            if ("未开始".equals(a.getStatus()) && a.getStartTime() != null && a.getStartTime().compareTo(now) <= 0) {
                a.setStatus("进行中");
                seckillActivityMapper.updateById(a);
            }
            if ("进行中".equals(a.getStatus()) && a.getEndTime() != null && a.getEndTime().compareTo(now) < 0) {
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
        if (userId == null || activityId == null) {
            return Result.error("参数异常");
        }

        User currentUser = userMapper.selectById(userId);
        if (currentUser == null) {
            return Result.error("用户不存在");
        }

        SeckillActivity activity = seckillActivityMapper.selectById(activityId);
        if (activity == null) return Result.error("活动不存在");
        if (!"进行中".equals(activity.getStatus())) return Result.error("活动未开始或已结束");
        if (activity.getTotalStock() <= 0) return Result.error("已售罄");

        String userBuyKey = RedisKeyUtils.seckillUserBuyKey(activityId, userId);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(userBuyKey))) {
            return Result.error("您已参与过该秒杀活动");
        }

        Goods goods = goodsMapper.selectById(activity.getGoodsId());
        if (goods == null) return Result.error("商品不存在");

        // Redis 原子扣库存（带兜底回滚）
        String key = RedisKeyUtils.seckillStockKey(activityId);
        // setIfAbsent 保证并发下只初始化一次，避免把库存重复重置回全量
        Boolean initialized = redisTemplate.opsForValue().setIfAbsent(key, activity.getTotalStock());
        if (Boolean.TRUE.equals(initialized)) {
            redisTemplate.expire(key, RedisKeyUtils.seckillStockTtl(activity.getEndTime()));
        }
        Long stock = redisTemplate.opsForValue().decrement(key);
        if (stock == null || stock < 0) {
            redisTemplate.opsForValue().increment(key);
            return Result.error("秒杀已结束");
        }
        try {
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

            // 原子扣减秒杀库存（库存不足则影响 0 行），不再用"读-改-写"
            int stockUpdated = seckillActivityMapper.deductStock(activityId);
            if (stockUpdated == 0) {
                throw new CustomException("秒杀库存不足");
            }
            // 更新商品库存和销量
            int goodsUpdated = goodsMapper.updateStoreDeduct(goods.getId(), 1);
            if (goodsUpdated == 0) {
                throw new CustomException("商品库存不足，请稍后重试");
            }
            redisTemplate.opsForValue().set(userBuyKey, "1", RedisKeyUtils.seckillStockTtl(activity.getEndTime()));

            // 补 TTL：getExpire 返回 Long，负数/空表示 key 没有过期时间
            Long ttl = redisTemplate.getExpire(key);
            if (ttl == null || ttl < 0) {
                redisTemplate.expire(key, RedisKeyUtils.seckillStockTtl(activity.getEndTime()));
            }

            return Result.success(order.getId());
        } catch (Exception e) {
            // 回滚 Redis 库存，避免数据库失败导致超卖
            redisTemplate.opsForValue().increment(key);
            redisTemplate.expire(key, RedisKeyUtils.seckillStockTtl(activity.getEndTime()));
            throw e;
        }
    }

    private static void requireAdmin(HttpServletRequest request) {
        // 角色以服务端 token 为准，不信任客户端请求头
        com.example.utils.AdminControllerUtils.requireAdmin(request);
    }
}



