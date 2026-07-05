package com.example.service;

import cn.hutool.core.date.DateUtil;
import com.example.entity.Post;
import com.example.mapper.LikeRecordMapper;
import com.example.mapper.PostMapper;
import com.example.mapper.ReplyMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {
    @Resource private PostMapper postMapper;
    @Resource private ReplyMapper replyMapper;
    @Resource private LikeRecordMapper likeRecordMapper;

    @Transactional
    public void add(Post post) {
        post.setViewCount(0); post.setLikeCount(0); post.setReplyCount(0);
        post.setIsTop("否"); post.setIsEssence("否"); post.setStatus("正常");
        post.setTime(DateUtil.now());
        postMapper.insert(post);
    }

    @Transactional
    public void deleteById(Integer id) {
        replyMapper.deleteByPostId(id);
        postMapper.deleteById(id);
    }

    @Transactional
    public void updateById(Post post) { postMapper.updateById(post); }

    @Transactional
    public void updateViewCount(Integer id) {
        Post post = postMapper.selectById(id);
        if (post != null) { post.setViewCount(post.getViewCount() + 1); postMapper.updateById(post); }
    }

    @Transactional
    public int toggleLike(Integer id, Integer userId) {
        Post post = postMapper.selectById(id);
        if (post == null) return 0;
        if (likeRecordMapper.checkLiked(id, userId) > 0) {
            likeRecordMapper.deleteLike(id, userId);
            post.setLikeCount(post.getLikeCount() - 1);
            postMapper.updateById(post);
            return -1;
        } else {
            likeRecordMapper.insertLike(id, userId);
            post.setLikeCount(post.getLikeCount() + 1);
            postMapper.updateById(post);
            return 1;
        }
    }

    public boolean checkLiked(Integer postId, Integer userId) {
        return likeRecordMapper.checkLiked(postId, userId) > 0;
    }
    public Post selectById(Integer id) { return postMapper.selectById(id); }
    public List<Post> selectAll(Post post) { return postMapper.selectAll(post); }
    public PageInfo<Post> selectPage(Post post, Integer pageNum, Integer pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        return PageInfo.of(postMapper.selectAll(post));
    }
}
