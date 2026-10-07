package com.example.mapper;

import org.apache.ibatis.annotations.Param;
import com.example.entity.Post;
import java.util.List;

public interface PostMapper {
    int insert(Post post);
    int deleteById(Integer id);
    int updateById(Post post);
    Post selectById(Integer id);
    List<Post> selectAll(Post post);

    int incrementViewCount(@Param("id") Integer id);

    int incrementLikeCount(@Param("id") Integer id);

    int decrementLikeCount(@Param("id") Integer id);

    int incrementReplyCount(@Param("id") Integer id);

    int decrementReplyCount(@Param("id") Integer id);
}


