package com.example.service;

import com.example.entity.User;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    public PageInfo<User> selectPage(Integer pageNum,Integer pageSize){
        PageHelper.startPage(pageNum,pageSize);
        return PageInfo.of()
    }
}

