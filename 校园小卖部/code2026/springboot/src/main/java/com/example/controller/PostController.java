package com.example.controller;

import com.example.common.Result;
import com.example.config.AuthValidator;
import com.example.entity.Post;
import com.example.service.PostService;
import com.example.utils.AdminControllerUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    @Resource private PostService postService;

    @Resource
    private HttpServletRequest request;

    @PostMapping("/add")
    public Result add(@RequestBody Post post) {
        // 发帖人必须是当前登录用户，避免伪造他人身份发帖
        post.setUserId(AuthValidator.requireUserId(request));
        postService.add(post);
        return Result.success();
    }

    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Post db = postService.selectById(id);
        if (db == null) {
            return Result.error("帖子不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        postService.deleteById(id);
        return Result.success();
    }

    @PutMapping("/update")
    public Result updateById(@RequestBody Post post) {
        Post db = postService.selectById(post.getId());
        if (db == null) {
            return Result.error("帖子不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        // 不允许通过改帖接口转移作者
        post.setUserId(db.getUserId());
        postService.updateById(post);
        return Result.success();
    }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        return Result.success(postService.selectById(id));
    }

    @GetMapping("/selectAll")
    public Result selectAll(Post post) {
        return Result.success(postService.selectAll(post));
    }

    @GetMapping("/selectPage")
    public Result selectPage(Post post, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.selectPage(post, pageNum, pageSize));
    }

    @PutMapping("/view/{id}")
    public Result updateViewCount(@PathVariable Integer id) {
        postService.updateViewCount(id);
        return Result.success();
    }

    // 点赞人一律取自登录会话，忽略请求里传入的 userId
    @PutMapping("/like/{id}")
    public Result updateLikeCount(@PathVariable Integer id) {
        Integer userId = AuthValidator.requireUserId(request);
        return Result.success(postService.toggleLike(id, userId));
    }

    @GetMapping("/checkLiked")
    public Result checkLiked(@RequestParam Integer postId) {
        Integer userId = AuthValidator.requireUserId(request);
        return Result.success(postService.checkLiked(postId, userId));
    }
}
