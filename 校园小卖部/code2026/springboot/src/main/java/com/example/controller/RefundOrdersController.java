package com.example.controller;

import com.example.common.Result;
import com.example.entity.RefundOrders;
import com.example.utils.AdminControllerUtils;
import com.example.config.AuthValidator;
import com.example.service.RefundOrdersService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.example.entity.Orders;
import com.example.service.OrdersService;

@RestController
@RequestMapping("/refundOrders")
public class RefundOrdersController {

    @Resource
    private RefundOrdersService refundOrdersService;

    @Resource
    private OrdersService ordersService;

    @Resource
    private HttpServletRequest request;

    /**
     * 用户提交售后申请（只能对自己的订单申请）
     */
    @PostMapping("/add")
    public Result add(@RequestBody RefundOrders refundOrders) {
        Integer currentUserId = AuthValidator.requireUserId(request);
        if (refundOrders == null || refundOrders.getOrderId() == null) {
            return Result.error("订单ID不能为空");
        }
        Orders order = ordersService.selectById(refundOrders.getOrderId());
        if (order == null) {
            return Result.error("订单不存在");
        }
        if (!currentUserId.equals(order.getUserId())) {
            return Result.error("无权对该订单申请售后");
        }
        refundOrdersService.add(refundOrders);
        return Result.success();
    }

    /**
     * 删除（仅管理员；管理端售后台账）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        AdminControllerUtils.requireAdmin(request);
        refundOrdersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改（仅管理员）。
     *
     * 说明：原先允许"售后单归属用户"调用，普通用户可以改自己的售后单状态/金额，
     * 会绕过"审核通过 → 执行退款"的正规流程。管理端页面并未使用该接口，故收紧为仅管理员。
     * 状态流转请走 /approve、/reject、/refund。
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody RefundOrders refundOrders) {
        AdminControllerUtils.requireAdmin(request);
        refundOrdersService.updateById(refundOrders);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    /**
     * 根据ID查询（管理员可查任意；普通用户只能查自己的售后单）
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        RefundOrders db = refundOrdersService.selectById(id);
        if (db == null) {
            return Result.error("售后单不存在");
        }
        if (!AdminControllerUtils.isAdmin(request)) {
            Integer currentUserId = AuthValidator.requireUserId(request);
            if (!currentUserId.equals(db.getUserId())) {
                return Result.error("无权查看该售后单");
            }
        }
        return Result.success(db);
    }

    /**
     * 查询所有：管理员看全部，普通用户只能看自己的售后单
     */
    @GetMapping("/selectAll")
    public Result selectAll(RefundOrders refundOrders) {
        applyScope(refundOrders);
        List<RefundOrders> list = refundOrdersService.selectAll(refundOrders);
        return Result.success(list);
    }

    /**
     * 分页查询：管理员看全部，普通用户只能看自己的售后单
     * （用户端"我的售后"页面 UserRefund.vue 依赖该接口，不能限制为仅管理员）
     */
    @GetMapping("/selectPage")
    public Result selectPage(RefundOrders refundOrders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        applyScope(refundOrders);
        PageInfo<RefundOrders> page = refundOrdersService.selectPage(refundOrders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员可查询全部（或按 userId 过滤）；普通用户强制只能查询自己的售后单。
     */
    private void applyScope(RefundOrders refundOrders) {
        if (AdminControllerUtils.isAdmin(request)) {
            return;
        }
        refundOrders.setUserId(AuthValidator.requireUserId(request));
    }

    /**
     * 管理员审核通过
     */
    @PutMapping("/approve")
    public Result approve(@RequestParam Integer id, @RequestParam(defaultValue = "") String reply) {
        AdminControllerUtils.requireAdmin(request);
        refundOrdersService.approve(id, reply);
        return Result.success();
    }

    /**
     * 管理员审核拒绝
     */
    @PutMapping("/reject")
    public Result reject(@RequestParam Integer id, @RequestParam(defaultValue = "") String reply) {
        AdminControllerUtils.requireAdmin(request);
        refundOrdersService.reject(id, reply);
        return Result.success();
    }

    /**
     * 管理员执行退款（将金额退到用户余额）
     */
    @PutMapping("/refund")
    public Result refund(@RequestParam Integer id) {
        AdminControllerUtils.requireAdmin(request);
        refundOrdersService.refund(id);
        return Result.success();
    }
}




