package com.example.mapper;

import com.example.entity.Reply;
import java.util.List;

public interface ReplyMapper {
    int insert(Reply reply);
    int deleteById(Integer id);
    int deleteByPostId(Integer postId);
    Reply selectById(Integer id);
    List<Reply> selectAll(Reply reply);
}