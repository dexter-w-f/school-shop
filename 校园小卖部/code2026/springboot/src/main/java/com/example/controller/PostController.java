package com.example.controller;

import com.example.common.Result;
import com.example.entity.Post;
import com.example.service.PostService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/post")
public class PostController {
    @Resource private PostService postService;

    @PostMapping("/add") public Result add(@RequestBody Post post) { postService.add(post); return Result.success(); }
    @DeleteMapping("/delete/{id}") public Result deleteById(@PathVariable Integer id) { postService.deleteById(id); return Result.success(); }
    @PutMapping("/update") public Result updateById(@RequestBody Post post) { postService.updateById(post); return Result.success(); }
    @GetMapping("/selectById/{id}") public Result selectById(@PathVariable Integer id) { return Result.success(postService.selectById(id)); }
    @GetMapping("/selectAll") public Result selectAll(Post post) { return Result.success(postService.selectAll(post)); }
    @GetMapping("/selectPage") public Result selectPage(Post post, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(postService.selectPage(post, pageNum, pageSize));
    }
    @PutMapping("/view/{id}") public Result updateViewCount(@PathVariable Integer id) { postService.updateViewCount(id); return Result.success(); }
    @PutMapping("/like/{id}") public Result updateLikeCount(@PathVariable Integer id, @RequestParam Integer userId) {
        return Result.success(postService.toggleLike(id, userId));
    }
    @GetMapping("/checkLiked") public Result checkLiked(@RequestParam Integer postId, @RequestParam Integer userId) {
        return Result.success(postService.checkLiked(postId, userId));
    }
}
