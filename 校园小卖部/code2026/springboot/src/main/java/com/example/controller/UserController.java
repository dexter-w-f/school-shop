package com.example.controller;

import com.example.common.Result;
import com.example.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
    public Result selectPag(@RequestParam Integer pageNum,@RequestParam Integer pageSize) {
    }
}
