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
import java.util.Set;

@Component
public class OrderTimeoutTask {

    private static final Log log = LogFactory.get();

    @Resource
    private OrdersMapper ordersMapper;

    @Resource
    private OrdersService ordersService;

    private static final Set<String> AUTO_CONFIRM_STATUSES = Set.of(
            "待支付", "待接单", "已出货", "已配送", "待收货"
    );

    /**
     * 每分钟执行一次，自动取消超过30分钟仍未接单的订单
     */
    @Scheduled(fixedRate = 60000)
    public void cancelTimeoutOrders() {
        String deadline = LocalDateTime.now().minusMinutes(30)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Orders> timeoutOrders = ordersMapper.selectTimeoutOrders("待支付", deadline);

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

    /**
     * 每天执行一次，自动确认超过7天仍未完成的订单
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void confirmReceiptTimeoutOrders() {
        String deadline = LocalDateTime.now().minusDays(7)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        List<Orders> timeoutOrders = ordersMapper.selectTimeoutUnfinishedOrders(deadline);
        for (Orders order : timeoutOrders) {
            try {
                if (!AUTO_CONFIRM_STATUSES.contains(order.getStatus())) {
                    continue;
                }

                order.setStatus("已完成");
                ordersService.autoConfirmReceiptTimeoutOrders(order);
                log.info("已自动确认超时订单: {}", order.getOrderNo());
            } catch (Exception e) {
                log.error(e, "自动确认订单失败: {}", order.getOrderNo());
            }
        }
    }
 }
