package com.example.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RandomUtil;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.*;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;

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
       User user = userMapper.selectById(orders.getUserId());
       if (user == null) {
           throw new CustomException("用户不存在");
       }

       // 先计算总价并校验库存
       for (Cart cart : cartList) {
          Integer goodsId = cart.getGoodsId();
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
       ordersMapper.updateById(orders);//更新订单

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
     * 修改
     */
   @Transactional
   public void updateById(Orders orders) {
       Orders current = ordersMapper.selectById(orders.getId());
        if(current == null || "已取消".equals(current.getStatus())) {
           return;
       }
        if("已取消".equals(orders.getStatus())){
           Integer userId = orders.getUserId();
           User user = userMapper.selectById(userId);
           // 如果是已支付的订单取消，才需要退余额
           boolean wasPaid = !"待支付".equals(current.getStatus());
           if (wasPaid && user != null) {
               user.setAccount(user.getAccount().add(orders.getTotal()));
               userMapper.updateById(user);
           }
           //
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orders.getId());
            List<OrderDetail> orderDetailList =orderDetailMapper.selectAll(orderDetail);
           for (OrderDetail detail : orderDetailList) {
                Integer goodsId = detail.getGoodsId();
               Goods goods = goodsMapper.selectById(goodsId);
                if(goods != null){
                    goods.setStore(goods.getStore() + detail.getNum());
                    goods.setSaleCount(goods.getSaleCount() - detail.getNum());
                    goodsMapper.updateById(goods);
                }
            }
        }
        ordersMapper.updateById(orders);
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
    public void pay(Integer orderId, String payType) {
        Orders orders = ordersMapper.selectById(orderId);
        if (orders == null) {
            throw new CustomException("订单不存在");
        }
        if (!"待支付".equals(orders.getStatus())) {
            throw new CustomException("订单状态异常，无法支付");
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
        ordersMapper.updateById(orders);
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
            order.setPayType("支付宝");
            order.setStatus("待接单");
            ordersMapper.updateById(order);
        }
        return status;
    }

    /**
     * 处理支付宝异步通知
     */
    public boolean processAlipayNotify(Map<String, String> params) {
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
            if ("待支付".equals(order.getStatus())) {
                order.setPayType("支付宝");
                order.setStatus("待接单");
                ordersMapper.updateById(order);
            }
            return true;
        }
        return false;
    }

}
