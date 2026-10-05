package com.example.controller;

import com.example.common.Result;
import com.example.entity.User;
import com.example.service.UserService;
import com.example.utils.AdminControllerUtils;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserService userService;

    @Resource
    private HttpServletRequest request;
/**
   * 分页查询
   * @param pageNum
   * @param pageSize
   * @param name
   */
    @GetMapping("/selectPage")
   public Result selectPag(@RequestParam(defaultValue = "1") Integer pageNum,
                            @RequestParam(defaultValue = "10") Integer pageSize,
                            @RequestParam(required = false) String name)
    {
       AdminControllerUtils.requireAdmin(request);
       PageInfo<User> pageInfo = userService.selectPage(pageNum,pageSize,name);
       return Result.success(pageInfo);
    }
/**
   * 删除数据
   * @param id
   */
   @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Integer id){
        AdminControllerUtils.requireAdmin(request);
        userService.deleteById(id);
        return Result.success();
   }
   /**
   * 新增数据
   * @param user
   */
   @PostMapping("/add")
    public Result add(@RequestBody User user){
       AdminControllerUtils.requireAdmin(request);
       userService.add(user);
       return Result.success();
   }

    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
       // 仅本人或管理员可查看用户详情（返回体包含敏感字段）
       Integer currentUserId = com.example.config.AuthValidator.requireUserId(request);
       if (!AdminControllerUtils.isAdmin(request) && !currentUserId.equals(id)) {
           return Result.error("无权查看该用户信息");
       }
       User user = userService.selectById(id);
       if (user != null) {
           // 不要把密码哈希返回给前端
           user.setPassword(null);
       }
        return Result.success(user);
    }


    @PutMapping("/update")
    public Result update(@RequestBody User user,@RequestHeader("X-Current-UserId") Integer currentUserId){
       if (currentUserId == null || !currentUserId.equals(user.getId())) {
           return Result.error("只能修改当前登录账号的信息");
       }

       User dbUser = userService.selectById(currentUserId);
       if (dbUser == null) {
           return Result.error("用户不存在");
       }

       User updateUser = new User();
       updateUser.setId(currentUserId);
       updateUser.setName(user.getName());
       updateUser.setAvatar(user.getAvatar());
       // 修改密码必须提供并校验原密码，避免免旧密码改密
       if (user.getNewPassword() != null && !user.getNewPassword().isBlank()) {
           if (!com.example.utils.PasswordUtils.matches(user.getOldPassword(), dbUser.getPassword())) {
               return Result.error("原密码错误");
           }
           updateUser.setPassword(user.getNewPassword());
       }
       userService.updateProfile(updateUser);
       return Result.success();
   }



}
