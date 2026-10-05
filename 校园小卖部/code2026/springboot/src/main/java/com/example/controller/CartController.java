package com.example.controller;

import com.example.common.Result;
import com.example.entity.Cart;
import com.example.service.CartService;
import com.example.config.AuthValidator;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/cart")
public class CartController {

    @Resource
    private CartService cartService;

    @Resource
    private HttpServletRequest request;

    /**
     * 新增（userId 一律取自登录会话，避免给他人购物车塞数据）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Cart cart) {
        cart.setUserId(AuthValidator.requireUserId(request));
        cartService.add(cart);
        return Result.success();
    }

    /**
     * 删除（只能删自己的）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Cart db = cartService.selectById(id);
        if (db == null) {
            return Result.error("购物车记录不存在");
        }
        if (!currentUserId.equals(db.getUserId())) {
            return Result.error("无权操作该购物车记录");
        }
        cartService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改（只能改自己的）
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Cart cart) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Cart db = cartService.selectById(cart.getId());
        if (db == null) {
            return Result.error("购物车记录不存在");
        }
        if (!currentUserId.equals(db.getUserId())) {
            return Result.error("无权操作该购物车记录");
        }
        // 不允许通过改购物车转移归属
        cart.setUserId(db.getUserId());
        cartService.updateById(cart);
        return Result.success();
    }

    /**
     * 根据ID查询（只能查自己的）
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Cart cart = cartService.selectById(id);
        if (cart == null) {
            return Result.error("购物车记录不存在");
        }
        if (!currentUserId.equals(cart.getUserId())) {
            return Result.error("无权查看该购物车记录");
        }
        return Result.success(cart);
    }

    /**
     * 查询所有：强制只查当前登录用户的购物车
     */
    @GetMapping("/selectAll")
    public Result selectAll(Cart cart) {
        cart.setUserId(AuthValidator.requireUserId(request));
        List<Cart> list = cartService.selectAll(cart);
        return Result.success(list);
    }

    /**
     * 分页查询：强制只查当前登录用户的购物车
     */
    @GetMapping("/selectPage")
    public Result selectPage(Cart cart,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        cart.setUserId(AuthValidator.requireUserId(request));
        PageInfo<Cart> page = cartService.selectPage(cart, pageNum, pageSize);
        return Result.success(page);
    }

}


