package com.example.controller;

import com.example.common.Result;
import com.example.config.AuthValidator;
import com.example.entity.Comment;
import com.example.service.CommentService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    @Resource
    private HttpServletRequest request;

    /**
     * 新增（评论人必须是当前登录用户）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Comment comment) {
        comment.setUserId(AuthValidator.requireUserId(request));
        commentService.add(comment);
        return Result.success();
    }

    /**
     * 删除（管理员或评论作者本人）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Comment db = commentService.selectById(id);
        if (db == null) {
            return Result.error("评论不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        commentService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改（管理员或评论作者本人）
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Comment comment) {
        Comment db = commentService.selectById(comment.getId());
        if (db == null) {
            return Result.error("评论不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        comment.setUserId(db.getUserId());
        commentService.updateById(comment);
        return Result.success();
    }

    /**
     * 根据ID查询
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Comment comment = commentService.selectById(id);
        return Result.success(comment);
    }

    /**
     * 查询所有
     */
    @GetMapping("/selectAll")
    public Result selectAll(Comment comment) {
        List<Comment> list = commentService.selectAll(comment);
        return Result.success(list);
    }

    /**
     * 分页查询
     */
    @GetMapping("/selectPage")
    public Result selectPage(Comment comment,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        PageInfo<Comment> page = commentService.selectPage(comment, pageNum, pageSize);
        return Result.success(page);
    }

}
