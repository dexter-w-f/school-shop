 package com.example.task;
 
 import cn.hutool.log.Log;
 import cn.hutool.log.LogFactory;
 import com.example.entity.Orders;
 import com.example.mapper.OrdersMapper;
 import com.example.service.OrdersService;
 import jakarta.annotation.Resource;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 import java.time.LocalDateTime;
 import java.time.format.DateTimeFormatter;
 import java.util.List;
 
 @Component
 public class OrderTimeoutTask {
 
     private static final Log log = LogFactory.get();
 
     @Resource
     private OrdersMapper ordersMapper;
 
     @Resource
     private OrdersService ordersService;
 
     /**
      * 每分钟执行一次，自动取消超过30分钟仍未接单的订单
      */
     @Scheduled(fixedRate = 60000)
     public void cancelTimeoutOrders() {
         String deadline = LocalDateTime.now().minusMinutes(30)
                 .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
 
         List<Orders> timeoutOrders = ordersMapper.selectTimeoutOrders("待接单", deadline);
 
         for (Orders order : timeoutOrders) {
             try {
                 order.setStatus("已取消");
                 ordersService.updateById(order);
                 log.info("已自动取消超时订单: {}", order.getOrderNo());
             } catch (Exception e) {
                 log.error(e, "自动取消订单失败: {}", order.getOrderNo());
             }
         }
     }
 }
