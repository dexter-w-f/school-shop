package com.example.controller;

import com.example.common.Result;
import com.example.entity.Orders;
import com.example.config.AuthValidator;
import com.example.service.OrdersService;
import com.example.utils.AdminControllerUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/orders")
public class OrdersController {

    @Resource
    private OrdersService ordersService;

    @Resource
    private HttpServletRequest request;

    /**
     * 新增
     */
    @PostMapping("/add")
    public Result add(@RequestBody Orders orders) {
        ordersService.add(orders);
        return Result.success(orders.getId());
    }

    /**
     * 删除。管理员可删除任意订单，普通用户只能删除自己的订单。
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Orders current = ordersService.selectById(id);
        if (current == null) {
            return Result.error("订单不存在");
        }
        if (!AdminControllerUtils.isAdmin(request) && !currentUserId.equals(current.getUserId())) {
            return Result.error("无权删除该订单");
        }
        ordersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改订单状态。
     * 管理员可执行出货/配送等流转；普通用户只能操作自己的订单（取消 / 确认收货）。
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Orders orders) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        if (orders.getId() == null) {
            return Result.error("订单ID不能为空");
        }
        boolean isAdmin = AdminControllerUtils.isAdmin(request);
        Orders current = ordersService.selectById(orders.getId());
        if (current == null) {
            return Result.error("订单不存在");
        }
        if (!isAdmin && !currentUserId.equals(current.getUserId())) {
            return Result.error("无权修改该订单");
        }
        ordersService.updateById(orders, isAdmin);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        Orders orders = ordersService.selectById(id);
        if (orders != null && !AdminControllerUtils.isAdmin(request)
                && !currentUserId.equals(orders.getUserId())) {
            return Result.error("无权查看该订单");
        }
        return Result.success(orders);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Orders orders) {
        applyScope(orders);
        List<Orders> list = ordersService.selectAll(orders);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Orders orders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        applyScope(orders);
        PageInfo<Orders> page = ordersService.selectPage(orders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员可查询全部（或按 userId 过滤），普通用户强制只能查询自己的订单。
     */
    private void applyScope(Orders orders) {
        if (AdminControllerUtils.isAdmin(request)) {
            return;
        }
        orders.setUserId(AuthValidator.requireUserId(request));
    }

}




