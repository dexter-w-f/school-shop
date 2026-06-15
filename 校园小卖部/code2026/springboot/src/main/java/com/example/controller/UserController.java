package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;
/**
   * 分页查询
   * @param pageNum
   * @param pageSize
   * @param name
   */
    @GetMapping("/selectPag")
    public Result selectPag(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String name)
    {
       PageInfo<User> pageInfo = userService.selectPage(pageNum,pageSize,name);
       return Result.success(pageInfo);
    }
/**
   * 删除数据
   * @param id
   */
   @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        userService.deleteById(id);
        return Result.success();
   }
   /**
   * 新增数据
   * @param user
   */
   @PostMapping("/add")
    public Result add(@RequestBody User user){
       userService.add(user);
       return Result.success();
   }
   @PutMapping("/update")
    public Result update(@RequestBody User user){
       userService.update(user);
       return Result.success();
   }



}
