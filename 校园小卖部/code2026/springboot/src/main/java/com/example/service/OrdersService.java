package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.example.config.AuthValidator;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Map;

/**
 * 业务处理
 **/
@Service
public class OrdersService {

    @Resource
    private OrdersMapper ordersMapper;
    @Resource
    GoodsMapper goodsMapper;
    @Resource
    UserMapper userMapper;
    @Resource
    OrderDetailMapper orderDetailMapper;
    @Resource
    CartMapper cartMapper;
    @Resource
    RefundOrdersMapper refundOrdersMapper;
    @Resource
    private AlipayService alipayService;
    /**
     * 新增
     */
    @Transactional
    public void add(Orders orders) {
        orders.setStatus("待支付");
        orders.setTime(DateUtil.now());
        //随机订单号
        String orderNo = DateUtil.format(new Date(),"yyyyMMdd") + System.currentTimeMillis() + RandomUtil.randomNumbers(4);
        orders.setOrderNo(orderNo);
        ordersMapper.insert(orders);
        Integer orderId = orders.getId();

       List<Cart> cartList = orders.getCartList();
       BigDecimal totalPrice = BigDecimal.ZERO;
       if (cartList == null || cartList.isEmpty()) {
           throw new CustomException("购物车不能为空");
       }
       User user = userMapper.selectById(orders.getUserId());
       if (user == null) {
           throw new CustomException("用户不存在");
       }

       // 先计算总价并校验库存
       for (Cart cart : cartList) {
          Integer goodsId = cart.getGoodsId();
          // 数量必须为正：负数会让总价变小，并导致 updateStoreDeduct 反向加库存
          if (cart.getNum() == null || cart.getNum() <= 0) {
              throw new CustomException("商品数量不合法");
          }
          Goods goods = goodsMapper.selectById(goodsId);
           if(goods == null){
               throw new CustomException("商品不存在");
            }
           if (goods.getStore() < cart.getNum()) {
               throw new CustomException(goods.getName() + "库存不足");
            }
            totalPrice = totalPrice.add(goods.getPrice().multiply(BigDecimal.valueOf(cart.getNum())));
        }



        // 执行实际扣库存、创建订单详情等操作
        for (Cart cart : cartList) {
            Integer goodsId = cart.getGoodsId();
            int affected = goodsMapper.updateStoreDeduct(goodsId, cart.getNum());
            if (affected == 0) {
                throw new CustomException("商品已售罄");
            }
            Goods goods = goodsMapper.selectById(goodsId);
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setNum(cart.getNum());
            orderDetail.setGoodsId(goodsId);
            orderDetail.setGoodsImg(goods.getImg());
            orderDetail.setGoodsName(goods.getName());
            orderDetail.setOrderId(orderId);
            orderDetail.setGoodsPrice(goods.getPrice());
            orderDetailMapper.insert(orderDetail);
            //删除购物车
            if (cart.getId()!= null ) {
                cartMapper.deleteById(cart.getId());
            }
        }
        // 余额在支付时扣除
       orders.setTotal(totalPrice);
       if (orderId != null) {
           ordersMapper.updateTotalAndOrderNoById(orders);
       }

    }

    /**
     * 删除
     */
    @Transactional
    public void deleteById(Integer id) {
        ordersMapper.deleteById(id);
        orderDetailMapper.deleteByOrderId(id);
    }

    /**
     * 修改订单状态。
     *
     * @param allowManageStatus true 表示调用者具备管理员权限，可执行出货/配送/完成等管理动作；
     *                          false 表示普通用户，只能取消或确认收货。
     */
  @Transactional
  public void updateById(Orders orders, boolean allowManageStatus) {
      Orders current = ordersMapper.selectById(orders.getId());
       if (current == null) {
           throw new CustomException("订单不存在");
       }
       String target = orders.getStatus();
       if (target == null || target.isBlank()) {
           return;
       }
       // 终态订单：明确拒绝"取消已完成/已取消订单"这类请求，
       // 避免接口返回成功但实际什么都没改（误导前端）。
       if ("已取消".equals(current.getStatus()) || "已完成".equals(current.getStatus())) {
           if ("已取消".equals(target)) {
               throw new CustomException("订单已是终态，无法取消");
           }
           return;
       }
       if ("已取消".equals(target)) {
           Set<String> cancellable = java.util.Set.of("待支付", "待接单", "已出货", "已配送", "待收货");
           if (!cancellable.contains(current.getStatus())) {
               throw new CustomException("当前订单状态不允许取消");
           }
           // 与售后退款互斥：已有售后单时不允许再走"取消订单"退款通道，
           // 否则同一笔订单会被退两次（取消退一次 + 售后执行退款再退一次）。
           RefundOrders refundCheck = new RefundOrders();
           refundCheck.setOrderId(current.getId());
           for (RefundOrders r : refundOrdersMapper.selectAll(refundCheck)) {
               if ("待审核".equals(r.getStatus()) || "已通过".equals(r.getStatus()) || "已退款".equals(r.getStatus())) {
                   throw new CustomException("该订单已有售后申请，请通过售后流程处理");
               }
           }
           // 关键：先用条件更新"抢占"状态到「已取消」，再执行退款与回补库存。
           // 否则两个并发的取消请求会同时通过上面的状态校验，各自退一次款、各回补一次库存。
           int claimed = ordersMapper.updateStatusToCancelledIfNotFinished(current.getId(), "已取消");
           if (claimed == 0) {
               throw new CustomException("订单状态已变更，请刷新后重试");
           }

           Integer userId = current.getUserId();
           User user = userMapper.selectById(userId);
           // 退款渠道判断：
           // - pay_type 为「余额支付」或为空（下单时尚未记录支付方式）→ 退回余额；
           // - pay_type 明确为支付宝/微信等第三方渠道 → 不回退余额，避免与渠道退款叠加成重复退款。
           // 注意：若只判断 equals("余额支付")，会把 pay_type 为 NULL 的订单也漏掉，
           // 导致用户取消订单后钱既不退余额、也不走渠道，等于白扣。
           String payType = current.getPayType();
           boolean thirdPartyPaid = payType != null && !payType.isBlank() && !"余额支付".equals(payType);
           if (!thirdPartyPaid && user != null && !"待支付".equals(current.getStatus())) {
               user.setAccount(user.getAccount().add(current.getTotal()));
               userMapper.updateById(user);
           }
           List<OrderDetail> orderDetailList = current.getOrderDetailList();
           if (orderDetailList == null) {
               OrderDetail query = new OrderDetail();
               query.setOrderId(current.getId());
               orderDetailList = orderDetailMapper.selectAll(query);
           }
           for (OrderDetail detail : orderDetailList) {
               // 原子回补库存，避免并发下"读取-修改-写回"丢失更新
               goodsMapper.updateStoreRestore(detail.getGoodsId(), detail.getNum());
           }
           return;
       }

       // 非取消类状态流转，必须匹配状态机，避免"接口返回成功但状态没变"的静默失败
       String expectedFrom = expectedFromStatus(current.getStatus(), target);
       if (expectedFrom == null) {
           throw new CustomException("当前订单状态不允许变更为" + target);
       }
       if (!allowManageStatus && !"已完成".equals(target)) {
           throw new CustomException("无权执行该订单操作");
       }
       int updated = ordersMapper.updateStatusFrom(current.getId(), expectedFrom, target);
       if (updated == 0) {
           throw new CustomException("订单状态已变更，请刷新后重试");
       }
   }

    /**
     * 订单状态机：返回目标状态应当来自的前置状态；不合法时返回 null。
     */
    private String expectedFromStatus(String currentStatus, String target) {
        switch (target) {
            case "已出货":
                return "待接单".equals(currentStatus) ? "待接单" : null;
            case "已配送":
                return "待接单".equals(currentStatus) ? "待接单" : null;
            case "待收货":
                return "已出货".equals(currentStatus) ? "已出货" : null;
            case "已完成":
                // 用户确认收货，或管理端直接完成
                if ("已出货".equals(currentStatus) || "已配送".equals(currentStatus) || "待收货".equals(currentStatus)) {
                    return currentStatus;
                }
                return null;
            default:
                return null;
        }
    }

    /**
     * 兼容旧调用方：默认按普通用户权限处理。
     */
    @Transactional
    public void updateById(Orders orders) {
        updateById(orders, false);
    }

    /**
     * 根据ID查询
     */
    public Orders selectById(Integer id) {
        return ordersMapper.selectById(id);
    }

    /**
     * 查询所有
     */
    public List<Orders> selectAll(Orders orders) {
        return ordersMapper.selectAll(orders);
    }

    /**
     * 分页查询
     */
    public PageInfo<Orders> selectPage(Orders orders, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Orders> list = ordersMapper.selectAll(orders);
        for (Orders o : list) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(o.getId());
            List<OrderDetail> orderDetailList =orderDetailMapper.selectAll(orderDetail);
            o.setOrderDetailList(orderDetailList);
        }
        return PageInfo.of(list);
    }





    /**
     * 支付订单
     */
    @Transactional
    public boolean pay(Integer orderId, String payType) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new CustomException("订单不存在");
        }
        if (!"待支付".equals(orders.getStatus())) {
            return false;
        }

        User user = userMapper.selectById(orders.getUserId());
        if (user == null) {
            throw new CustomException("用户不存在");
        }

        // 余额支付：扣余额
        if ("余额支付".equals(payType)) {
            if (user.getAccount().compareTo(orders.getTotal()) < 0) {
                throw new CustomException("余额不足，请选择其他支付方式");
            }
            user.setAccount(user.getAccount().subtract(orders.getTotal()));
            userMapper.updateById(user);
        }
        // 支付宝模拟/微信模拟：不扣余额，只记录支付方式

        orders.setPayType(payType);
        orders.setStatus("待接单");
        int updated = ordersMapper.updateStatusIfPending(orders.getId(), orders.getStatus(), orders.getPayType());
        if (updated == 0) {
            return false;
        }
        return true;
    }

    @Transactional
    public boolean payByUser(Integer orderId, String payType, Integer currentUserId) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new CustomException("订单不存在");
        }
        if (!currentUserId.equals(orders.getUserId())) {
            throw new CustomException("无权支付该订单");
        }
        return pay(orderId, payType);
    }


    /**
     * 创建支付宝支付
     */
    public Map<String, Object> createAlipayPayment(Integer orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null || !"待支付".equals(order.getStatus())) {
            return null;
        }
        String qrCode = alipayService.createPayment(
                order.getOrderNo(),
                order.getTotal().toString(),
                "校园小卖部 - " + order.getOrderNo()
        );
        if (qrCode == null) {
            return null;
        }
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("qrCode", qrCode);
        result.put("orderNo", order.getOrderNo());
        result.put("total", order.getTotal());
        return result;
    }

    /**
     * 查询支付宝支付状态
     */
    public String queryPaymentStatus(Integer orderId) {
        Orders order = ordersMapper.selectById(orderId);
        if (order == null) {
            return "FAIL";
        }
        // 如果已经是待接单状态（已支付），直接返回成功
        if (!"待支付".equals(order.getStatus())) {
            return "SUCCESS";
        }
        String status = alipayService.queryPayment(order.getOrderNo());
        if ("SUCCESS".equals(status)) {
            ordersMapper.updateStatusIfPending(order.getId(), "待接单", "支付宝");
        }
        return status;
    }

    /**
     * 处理支付宝异步通知。
     * 必须先通过 RSA2 验签，否则任何人都能伪造回调把订单置为已支付。
     */
    public boolean processAlipayNotify(Map<String, String> params) {
        if (!alipayService.verifyNotify(params)) {
            System.err.println("【支付宝】异步通知验签失败，已拒绝");
            return false;
        }
        String outTradeNo = params.get("out_trade_no");
        String tradeStatus = params.get("trade_status");
        if (outTradeNo == null || !"TRADE_SUCCESS".equals(tradeStatus)) {
            return false;
        }
        // 查找订单并更新状态
        Orders check = new Orders();
        check.setOrderNo(outTradeNo);
        List<Orders> list = ordersMapper.selectAll(check);
        if (!list.isEmpty()) {
            Orders order = list.get(0);
            // 校验回调金额与订单金额一致，防止改价回调
            String notifyAmount = params.get("total_amount");
            if (notifyAmount != null && order.getTotal() != null) {
                try {
                    if (new BigDecimal(notifyAmount).compareTo(order.getTotal()) != 0) {
                        System.err.println("【支付宝】异步通知金额不一致，已拒绝: " + notifyAmount + " != " + order.getTotal());
                        return false;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("【支付宝】异步通知金额格式非法，已拒绝: " + notifyAmount);
                    return false;
                }
            }
            if ("待支付".equals(order.getStatus())) {
                ordersMapper.updateStatusIfPending(order.getId(), "待接单", "支付宝");
            }
            return true;
        }
        return false;
    }

    /**
     * 自动确认超过7天的订单为已完成
     */
    @Transactional
    public void autoConfirmReceiptTimeoutOrders(Orders orders) {
        if (orders == null || orders.getId() == null) {
            return;
        }
        Orders current = ordersMapper.selectById(orders.getId());
        if (current == null || "已取消".equals(current.getStatus()) || "已完成".equals(current.getStatus())) {
            return;
        }
        current.setStatus("已完成");
        ordersMapper.updateById(current);
    }

}


