package com.example.mapper;

import org.apache.ibatis.annotations.Param;

import com.example.entity.RefundOrders;
import java.util.List;

public interface RefundOrdersMapper {
    int insert(RefundOrders refundOrders);
    int deleteById(Integer id);
    int updateById(RefundOrders refundOrders);
    RefundOrders selectById(Integer id);
    List<RefundOrders> selectAll(RefundOrders refundOrders);

    /**
     * 仅在售后单处于已通过状态时更新为已退款，避免并发重复退款。
     */
    int updateStatusIfApproved(@Param("id") Integer id, @Param("status") String status);
}
