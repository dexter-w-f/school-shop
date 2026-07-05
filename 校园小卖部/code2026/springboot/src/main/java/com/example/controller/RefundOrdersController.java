package com.example.controller;

import com.example.common.Result;
import com.example.entity.RefundOrders;
import com.example.service.RefundOrdersService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/refundOrders")
public class RefundOrdersController {

    @Resource
    private RefundOrdersService refundOrdersService;

    /**
     * 用户提交售后申请
     */
    @PostMapping("/add")
    public Result add(@RequestBody RefundOrders refundOrders) {
        refundOrdersService.add(refundOrders);
        return Result.success();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        refundOrdersService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody RefundOrders refundOrders) {
        refundOrdersService.updateById(refundOrders);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        RefundOrders refundOrders = refundOrdersService.selectById(id);
        return Result.success(refundOrders);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(RefundOrders refundOrders) {
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
        PageInfo<RefundOrders> page = refundOrdersService.selectPage(refundOrders, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 管理员审核通过
     */
    @PutMapping("/approve")
    public Result approve(@RequestParam Integer id, @RequestParam(defaultValue = "") String reply) {
        refundOrdersService.approve(id, reply);
        return Result.success();
    }

    /**
     * 管理员审核拒绝
     */
    @PutMapping("/reject")
    public Result reject(@RequestParam Integer id, @RequestParam(defaultValue = "") String reply) {
        refundOrdersService.reject(id, reply);
        return Result.success();
    }

    /**
     * 管理员执行退款（将金额退到用户余额）
     */
    @PutMapping("/refund")
    public Result refund(@RequestParam Integer id) {
        refundOrdersService.refund(id);
        return Result.success();
    }
}
