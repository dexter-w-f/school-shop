package com.example.mapper;

import com.example.entity.RefundOrders;
import java.util.List;

public interface RefundOrdersMapper {
    int insert(RefundOrders refundOrders);
    int deleteById(Integer id);
    int updateById(RefundOrders refundOrders);
    RefundOrders selectById(Integer id);
    List<RefundOrders> selectAll(RefundOrders refundOrders);
}
