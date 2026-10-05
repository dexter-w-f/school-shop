package com.example.mapper;

import com.example.entity.Goods;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作goods相关数据接口
*/
public interface GoodsMapper {

    /**
      * 新增
    */
    int insert(Goods goods);

    /**
      * 删除
    */
    int deleteById(Integer id);

    /**
      * 修改
    */
    int updateById(Goods goods);

    /**
      * 根据ID查询
    */
    Goods selectById(Integer id);

    /**
      * 查询所有
    */
    List<Goods> selectAll(Goods goods);


    int updateStoreDeduct(@Param("id") Integer id, @Param("num") Integer num);

    /**
     * 原子回补库存并扣减销量（取消/退款回滚库存用）。
     * 用 SQL 原子操作替代"读取-修改-写回"，避免并发下丢失更新。
     */
    int updateStoreRestore(@Param("id") Integer id, @Param("num") Integer num);

}
