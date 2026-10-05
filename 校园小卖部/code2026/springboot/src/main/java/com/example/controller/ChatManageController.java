package com.example.controller;

import com.example.common.Result;
import com.example.entity.ChatMessage;
import com.example.mapper.ChatMessageMapper;
import com.example.utils.AdminControllerUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat/manage")
public class ChatManageController {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private HttpServletRequest request;

    @GetMapping("/sessions")
    public Result sessions() {
        AdminControllerUtils.requireAdmin(request);
        return Result.success(chatMessageMapper.selectSessions());
    }

    @GetMapping("/history/{userId}")
    public Result history(@PathVariable Integer userId) {
        AdminControllerUtils.requireAdmin(request);
        return Result.success(chatMessageMapper.selectByUserId(userId));
    }
}
