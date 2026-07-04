package com.example.controller;

import com.example.common.Result;
import com.example.entity.ChatMessage;
import com.example.mapper.ChatMessageMapper;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat/manage")
public class ChatManageController {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @GetMapping("/sessions")
    public Result sessions() {
        return Result.success(chatMessageMapper.selectSessions());
    }

    @GetMapping("/history/{userId}")
    public Result history(@PathVariable Integer userId) {
        return Result.success(chatMessageMapper.selectByUserId(userId));
    }
}
