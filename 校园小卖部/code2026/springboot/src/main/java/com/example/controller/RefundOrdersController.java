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

    private Integer requireAdminOrOrderOwner(RefundOrders refundOrders) {
        if (refundOrders == null || refundOrders.getId() == null) {
            return AdminControllerUtils.requireAdmin(request);
        }
        RefundOrders db = refundOrdersService.selectById(refundOrders.getId());
        if (db == null) {
            // 记录不存在时不泄露信息，要求管理员权限
            return AdminControllerUtils.requireAdmin(request);
        }
        if (AdminControllerUtils.isAdmin(request)) {
            return AuthValidator.requireUserId(request);
        }
        Integer currentUserId = AuthValidator.requireUserId(request);
        if (!currentUserId.equals(db.getUserId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN, "无权操作该售后单");
        }
        return currentUserId;
    }

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
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        RefundOrders refundOrders = new RefundOrders();
        refundOrders.setId(id);
        requireAdminOrOrderOwner(refundOrders);
        refundOrdersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody RefundOrders refundOrders) {
        requireAdminOrOrderOwner(refundOrders);
        refundOrdersService.updateById(refundOrders);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        AdminControllerUtils.requireAdmin(request);
        RefundOrders refundOrders = refundOrdersService.selectById(id);
        return Result.success(refundOrders);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(RefundOrders refundOrders) {
        AdminControllerUtils.requireAdmin(request);
        List<RefundOrders> list = refundOrdersService.selectAll(refundOrders);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(RefundOrders refundOrders,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        AdminControllerUtils.requireAdmin(request);
        PageInfo<RefundOrders> page = refundOrdersService.selectPage(refundOrders, pageNum, pageSize);
        return Result.success(page);
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




