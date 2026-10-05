package com.example.mapper;

import com.example.entity.Orders;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.math.BigDecimal;

/**
 * 操作orders相关数据接口
*/
public interface OrdersMapper {

    /**
      * 新增
    */
    int insert(Orders orders);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Orders orders);

    /**
      * 根据ID查询
    */
    Orders selectById(Integer id);

    /**
      * 查询所有
    */
    List<Orders> selectAll(Orders orders);


    List<Orders> selectTimeoutOrders(@Param("status") String status, @Param("deadline") String deadline);

    /**
     * 查询超时未完成的订单，用于7天自动确认收货
     */
    List<Orders> selectTimeoutUnfinishedOrders(@Param("deadline") String deadline);

    /**
     * 支付时按条件更新订单状态，避免并发重复支付。
     */
    int updateStatusIfPending(@Param("id") Integer id, @Param("status") String status, @Param("payType") String payType);

    /**
     * 取消时仅对未完成订单更新，避免重复回滚库存。
     */
    int updateStatusToCancelledIfNotFinished(@Param("id") Integer id, @Param("status") String status);

    /**
     * 仅当订单仍处于 fromStatus 时流转到 status，避免并发下重复流转。
     */
    int updateStatusFrom(@Param("id") Integer id, @Param("fromStatus") String fromStatus, @Param("status") String status);

    int updateTotalAndOrderNoById(Orders orders);

    /**
     * 统计总收入
     */
    BigDecimal sumTotal(@Param("status") String status);

    /**
     * 统计指定时间范围内的收入
     */
    BigDecimal sumTotalByTime(@Param("startTime") String startTime, @Param("endTime") String endTime);

}
