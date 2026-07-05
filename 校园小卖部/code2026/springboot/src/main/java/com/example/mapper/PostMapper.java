package com.example.mapper;

import com.example.entity.Post;
import java.util.List;

public interface PostMapper {
    int insert(Post post);
    int deleteById(Integer id);
    int updateById(Post post);
    Post selectById(Integer id);
    List<Post> selectAll(Post post);
}