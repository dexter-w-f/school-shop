package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Goods;
import com.example.entity.OrderDetail;
import com.example.entity.Orders;
import com.example.entity.RefundOrders;
import com.example.entity.User;
import com.example.exception.CustomException;
import com.example.mapper.GoodsMapper;
import com.example.mapper.OrderDetailMapper;
import com.example.mapper.OrdersMapper;
import com.example.mapper.RefundOrdersMapper;
import com.example.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RefundOrdersService {

    @Resource
    private RefundOrdersMapper refundOrdersMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private AlipayService alipayService;
    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    private UserMapper userMapper;

    /**
     * 用户提交售后申请
     */
    @Transactional
    public void add(RefundOrders refundOrders) {
        // 校验订单是否存在
        Orders order = ordersMapper.selectById(refundOrders.getOrderId());
        if (order == null) {
            throw new CustomException("订单不存在");
        }
        // 获取用户信息
        User user = userMapper.selectById(order.getUserId());
        // 校验订单状态是否允许申请售后
        String status = order.getStatus();
        if (!"待接单".equals(status) && !"已出货".equals(status) && !"已配送".equals(status) && !"已完成".equals(status)) {
            throw new CustomException("当前订单状态不允许申请售后");
        }
        // 校验退款金额不超过订单总额
        if (refundOrders.getAmount() != null && refundOrders.getAmount().compareTo(order.getTotal()) > 0) {
            throw new CustomException("退款金额不能超过订单总额");
        }
        // 检查该订单是否已有进行中的售后申请
        RefundOrders check = new RefundOrders();
        check.setOrderId(refundOrders.getOrderId());
        List<RefundOrders> existing = refundOrdersMapper.selectAll(check);
        for (RefundOrders r : existing) {
            if ("待审核".equals(r.getStatus()) || "已通过".equals(r.getStatus())) {
                throw new CustomException("该订单已有进行中的售后申请，请等待处理");
            }
        }
        // 补充订单信息
        refundOrders.setOrderNo(order.getOrderNo());
        refundOrders.setUserId(order.getUserId());
        if (user != null) {
            refundOrders.setUserName(user.getName());
        }
        // 从订单详情中补充商品信息
        OrderDetail detailParam = new OrderDetail();
        detailParam.setOrderId(order.getId());
        List<OrderDetail> detailList = orderDetailMapper.selectAll(detailParam);
        if (!detailList.isEmpty()) {
            OrderDetail first = detailList.get(0);
            refundOrders.setGoodsId(first.getGoodsId());
            refundOrders.setGoodsName(first.getGoodsName());
            refundOrders.setGoodsImg(first.getGoodsImg());
        }
        refundOrders.setStatus("待审核");
        refundOrders.setTime(DateUtil.now());
        refundOrdersMapper.insert(refundOrders);
    }

    /**
     * 管理员审核售后 - 通过（同意退款）
     */
    @Transactional
    public void approve(Integer id, String reply) {
        RefundOrders refund = refundOrdersMapper.selectById(id);
        if (refund == null) {
            throw new CustomException("售后单不存在");
        }
        if (!"待审核".equals(refund.getStatus())) {
            throw new CustomException("售后单状态异常，无法审核");
        }
        refund.setStatus("已通过");
        refund.setReply(reply);
        refund.setHandleTime(DateUtil.now());
        refundOrdersMapper.updateById(refund);
    }

    /**
     * 管理员拒绝售后
     */
    @Transactional
    public void reject(Integer id, String reply) {
        RefundOrders refund = refundOrdersMapper.selectById(id);
        if (refund == null) {
            throw new CustomException("售后单不存在");
        }
        if (!"待审核".equals(refund.getStatus())) {
            throw new CustomException("售后单状态异常，无法审核");
        }
        refund.setStatus("已拒绝");
        refund.setReply(reply);
        refund.setHandleTime(DateUtil.now());
        refundOrdersMapper.updateById(refund);
    }

    /**
     * 管理员执行退款（仅退款/退货退款 → 实际退款到用户余额）
     */
    @Transactional
    public void refund(Integer id) {
        RefundOrders refund = refundOrdersMapper.selectById(id);
        if (refund == null) {
            throw new CustomException("售后单不存在");
        }
        if (!"已通过".equals(refund.getStatus())) {
            throw new CustomException("售后单未通过审核，无法退款");
        }
        // 获取订单信息，根据支付方式选择退款渠道
        Orders order = ordersMapper.selectById(refund.getOrderId());

        // 支付宝支付：走支付宝退款API（钱退回用户支付宝）
        if (order != null && "支付宝".equals(order.getPayType())) {
            boolean refunded = alipayService.refund(order.getOrderNo(), refund.getAmount().toString());
            if (!refunded) {
                throw new CustomException("支付宝退款失败，请重试");
            }
        } else if (order != null && "余额支付".equals(order.getPayType())) {
            // 余额支付：退钱到用户余额
            User user = userMapper.selectById(refund.getUserId());
            if (user == null) {
                throw new CustomException("用户不存在");
            }
            user.setAccount(user.getAccount().add(refund.getAmount()));
            userMapper.updateById(user);
        }
        // 模拟支付：不涉及实际资金变动，只更新订单状态

        refund.setStatus("已退款");
        refund.setHandleTime(DateUtil.now());
        refundOrdersMapper.updateById(refund);

        // 如果是仅退款或退货退款类型，将订单状态改为"已取消"（退回库存）
        if (order != null && !"已完成".equals(order.getStatus())) {
            // 恢复库存：加回库存、扣减销量
            OrderDetail detailParam2 = new OrderDetail();
            detailParam2.setOrderId(order.getId());
            List<OrderDetail> detailList2 = orderDetailMapper.selectAll(detailParam2);
            for (OrderDetail detail : detailList2) {
                Goods goods = goodsMapper.selectById(detail.getGoodsId());
                if (goods != null) {
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
            order.setStatus("已取消");
            ordersMapper.updateById(order);
        }
    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        refundOrdersMapper.deleteById(id);
    }

    /**
     * 修改
     */
    @Transactional
    public void updateById(RefundOrders refundOrders) {
        refundOrdersMapper.updateById(refundOrders);
    }

    /**
     * 根据ID查询
     */
    public RefundOrders selectById(Integer id) {
        return refundOrdersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<RefundOrders> selectAll(RefundOrders refundOrders) {
        return refundOrdersMapper.selectAll(refundOrders);
    }

    /**
     * 分页查询
     */
    public PageInfo<RefundOrders> selectPage(RefundOrders refundOrders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<RefundOrders> list = refundOrdersMapper.selectAll(refundOrders);
        return PageInfo.of(list);
    }
}