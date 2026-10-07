package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Reply;
import com.example.mapper.PostMapper;
import com.example.mapper.ReplyMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReplyService {
    @Resource private ReplyMapper replyMapper;
    @Resource private PostMapper postMapper;

    @Transactional
    public void add(Reply reply) {
        reply.setTime(DateUtil.now());
        replyMapper.insert(reply);
        postMapper.incrementReplyCount(reply.getPostId());
    }

    @Transactional
    public void deleteById(Integer id) {
        Reply reply = replyMapper.selectById(id);
        if (reply != null) {
            replyMapper.deleteById(id);
            if (reply.getPostId() != null) {
                postMapper.decrementReplyCount(reply.getPostId());
            }
        }
    }

    public Reply selectById(Integer id) { return replyMapper.selectById(id); }
    public List<Reply> selectAll(Reply reply) { return replyMapper.selectAll(reply); }
    public PageInfo<Reply> selectPage(Reply reply, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(replyMapper.selectAll(reply));
    }
}
