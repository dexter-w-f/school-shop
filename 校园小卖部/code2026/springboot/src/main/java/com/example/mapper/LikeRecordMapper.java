package com.example.mapper;
import org.apache.ibatis.annotations.Param;

public interface LikeRecordMapper {
    int deleteLike(@Param("postId") Integer postId, @Param("userId") Integer userId);
    int insertLike(@Param("postId") Integer postId, @Param("userId") Integer userId);
    int checkLiked(@Param("postId") Integer postId, @Param("userId") Integer userId);
}