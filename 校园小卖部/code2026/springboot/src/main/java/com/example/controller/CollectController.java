package com.example.controller;

import com.example.common.Result;
import com.example.config.AuthValidator;
import com.example.entity.Collect;
import com.example.service.CollectService;
import com.github.pagehelper.PageInfo;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 前端操作接口
 **/
@RestController
@RequestMapping("/collect")
public class CollectController {

    @Resource
    private CollectService collectService;

    @Resource
    private HttpServletRequest request;

    /**
     * 新增收藏（收藏人必须是当前登录用户）
     */
    @PostMapping("/add")
    public Result add(@RequestBody Collect collect) {
        collect.setUserId(AuthValidator.requireUserId(request));
        collectService.add(collect);
        return Result.success();
    }

    /**
     * 删除（管理员或收藏人本人）
     */
    @DeleteMapping("/delete/{id}")
    public Result deleteById(@PathVariable Integer id) {
        Collect db = collectService.selectById(id);
        if (db == null) {
            return Result.error("收藏不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        collectService.deleteById(id);
        return Result.success();
    }

    /**
     * 修改（管理员或收藏人本人）
     */
    @PutMapping("/update")
    public Result updateById(@RequestBody Collect collect) {
        Collect db = collectService.selectById(collect.getId());
        if (db == null) {
            return Result.error("收藏不存在");
        }
        AuthValidator.requireAdminOrOwner(request, db.getUserId());
        collect.setUserId(db.getUserId());
        collectService.updateById(collect);
        return Result.success();
    }

    /**
     * 根据ID查询（管理员或收藏人本人）
     */
    @GetMapping("/selectById/{id}")
    public Result selectById(@PathVariable Integer id) {
        Collect collect = collectService.selectById(id);
        if (collect == null) {
            return Result.error("收藏不存在");
        }
        AuthValidator.requireAdminOrOwner(request, collect.getUserId());
        return Result.success(collect);
    }

    /**
     * 查询所有（普通用户只能查自己的收藏）
     */
    @GetMapping("/selectAll")
    public Result selectAll(Collect collect) {
        applyScope(collect);
        List<Collect> list = collectService.selectAll(collect);
        return Result.success(list);
    }

    /**
     * 分页查询（普通用户只能查自己的收藏）
     */
    @GetMapping("/selectPage")
    public Result selectPage(Collect collect,
                             @RequestParam(defaultValue = "1") Integer pageNum,
                             @RequestParam(defaultValue = "10") Integer pageSize) {
        applyScope(collect);
        PageInfo<Collect> page = collectService.selectPage(collect, pageNum, pageSize);
        return Result.success(page);
    }

    private void applyScope(Collect collect) {
        if (com.example.utils.AdminControllerUtils.isAdmin(request)) {
            return;
        }
        collect.setUserId(AuthValidator.requireUserId(request));
    }

}
