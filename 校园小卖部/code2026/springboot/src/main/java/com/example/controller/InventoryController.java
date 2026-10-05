package com.example.controller;

import com.example.common.Result;
import com.example.entity.Goods;
import com.example.mapper.GoodsMapper;
import com.example.utils.AdminControllerUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 库存管理接口
 */
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private HttpServletRequest request;

    /**
     * 获取所有商品库存列表
     */
    @GetMapping("/list")
    public Result list() {
        AdminControllerUtils.requireAdmin(request);
        Goods query = new Goods();
        query.setStatus("上架");
        List<Goods> list = goodsMapper.selectAll(query);
        return Result.success(list);
    }

    /**
     * 调整库存
     */
    @PutMapping("/update")
    public Result update(@RequestParam Integer goodsId, @RequestParam Integer store) {
        AdminControllerUtils.requireAdmin(request);
        if (store == null || store < 0) {
            return Result.error("库存不能为负数");
        }
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) return Result.error("商品不存在");
        goods.setStore(store);
        goodsMapper.updateById(goods);
        return Result.success();
    }
}
