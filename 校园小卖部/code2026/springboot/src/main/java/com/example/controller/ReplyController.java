package com.example.controller;

import com.example.common.Result;
import com.example.entity.Reply;
import com.example.service.ReplyService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/reply")
public class ReplyController {
    @Resource private ReplyService replyService;

    @PostMapping("/add") public Result add(@RequestBody Reply reply) { replyService.add(reply); return Result.success(); }
    @DeleteMapping("/delete/{id}") public Result deleteById(@PathVariable Integer id) { replyService.deleteById(id); return Result.success(); }
    @GetMapping("/selectAll") public Result selectAll(Reply reply) { return Result.success(replyService.selectAll(reply)); }
    @GetMapping("/selectPage") public Result selectPage(Reply reply, @RequestParam(defaultValue = "1") Integer pageNum, @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(replyService.selectPage(reply, pageNum, pageSize));
    }
}