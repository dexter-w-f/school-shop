package com.example.controller;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.example.common.Result;
import com.example.entity.*;
import com.example.exception.CustomException;
import com.example.mapper.OrderDetailMapper;
import com.example.service.*;
import com.example.utils.TokenUtils;
import com.example.utils.LoginAttemptLimiter;
import org.springframework.data.redis.core.RedisTemplate;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


@RestController
public class WebController {

    @Resource
    private AdminService adminService;
    @Resource
    private UserService userService;
    @Resource
    OrdersService ordersService;
    @Resource
    GoodsService goodsService;
    @Resource
    CategoryService categoryService;
    @Resource
    OrderDetailMapper orderDetailMapper;

    @Resource
    private LoginAttemptLimiter loginAttemptLimiter;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private com.example.mapper.OrdersMapper ordersMapper;

    /**
     * 默认请求接口
     */
    @GetMapping("/")
    public Result hello() {
        return Result.success();
    }

    /**
     * 登录
     */
    @PostMapping("/login")
    public Result login(@RequestBody Account account) {
        if (loginAttemptLimiter.isLocked(account.getUsername())) {
            return Result.error("登录失败次数过多，请15分钟后重试");
        }
        try {
        Account ac = null;
        if ("管理员".equals(account.getRole())) {
            ac = adminService.login(account);
        }
        if ("普通用户".equals(account.getRole())) {
            ac = userService.login(account);
        }
        if (ac == null) {
            loginAttemptLimiter.recordFailure(account.getUsername());
            return Result.error("用户不存在");
        }
        loginAttemptLimiter.reset(account.getUsername());
        String token = TokenUtils.generateToken(ac.getId(), ac.getRole());
        ac.setToken(token);
        // 不要把密码哈希返回给前端
        ac.setPassword(null);
        return Result.success(ac);
        } catch (CustomException e) {
            loginAttemptLimiter.recordFailure(account.getUsername());
            return Result.error(e.getMsg());
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public Result register(@RequestBody Map<String, String> params) {
        String username = params.get("username");
        String password = params.get("password");
        String newPassword = params.get("newPassword");
        String captchaCode = params.get("captchaCode");

        // 校验验证码
        String key = "captcha:" + username;
        String stored = (String) redisTemplate.opsForValue().get(key);
        if (stored == null) return Result.error("验证码已过期，请重新获取");
        if (!stored.equals(captchaCode)) return Result.error("验证码错误");
        redisTemplate.delete(key);

        if (!password.equals(newPassword)) {
            return Result.error("两次密码不一致");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        user.setNewPassword(newPassword);
        user.setRole(params.getOrDefault("role", "普通用户"));
        user.setName(params.get("name"));
        userService.add(user);
        return Result.success();
    }

    /**
     * 修改密码（只能修改当前登录账号自己的密码）
     */
    @PutMapping("/updatePassword")
    public Result updatePassword(@RequestBody Account account, HttpServletRequest request) {
        Integer currentUserId = com.example.config.AuthValidator.requireUserId(request);
        // 目标账号必须与当前会话一致，role 也不能由请求体决定，避免改他人密码/越权
        if (account.getId() == null || !currentUserId.equals(account.getId())) {
            return Result.error("只能修改当前登录账号的密码");
        }
        String role = (String) request.getAttribute("currentRole");
        if (role == null) {
            return Result.error("登录状态异常，请重新登录");
        }
        if ("管理员".equals(role)) {
            adminService.updatePassword(account);
        } else {
            userService.updatePassword(account);
        }
        TokenUtils.removeByUserId(account.getId());
        return Result.success();
    }
    @GetMapping("/count")
    public Result count() {
        String startOfToday = DateUtil.today() + " 00:00:00";
        String endOfToday = DateUtil.today() + " 23:59:59";
        BigDecimal total = ordersMapper.sumTotal("已取消");
        BigDecimal today = ordersMapper.sumTotalByTime(startOfToday, endOfToday);
       Integer goods = goodsService.selectAll(null).size();
       Integer user = userService.selectAll("").size();
        Map<String,Object> map = new HashMap<>();
        map.put("total",total);
        map.put("today",today);
        map.put("goods",goods);
        map.put("user",user);
        return Result.success(map);
    }
    @GetMapping("/selectLine")
    public Result selectLine() {
        Date date = new Date();
        DateTime start = DateUtil.offsetDay(date, -6);
        List<DateTime> dateTimes = DateUtil.rangeToList(start, date, DateField.DAY_OF_YEAR);
        List<String> dateStrList = dateTimes.stream().map(DateUtil::formatDate).sorted().toList();
        ArrayList<BigDecimal> countList = new ArrayList<>();
        for (String day : dateStrList) {
            BigDecimal total = ordersMapper.sumTotalByTime(day + " 00:00:00", day + " 23:59:59");
            countList.add(total);
        }

        Map<String,Object> map = new HashMap<>();
        map.put("date",dateStrList);
        map.put("count",countList);
        return Result.success(map);
    }

    @GetMapping("/selectPie")
    public Result selectPie() {
        List<Orders> orders = ordersService.selectAll(null);
        List<Integer> orderIds = orders.stream()
                .filter(o -> !"已取消".equals(o.getStatus()))
                .map(Orders::getId)
                .toList();
        List<Map<String,Object>> categoryAmounts = orderDetailMapper.selectCategoryAmountGroup(orderIds);
        Map<Integer, Map<String, Object>> amountByCategoryId = categoryAmounts.stream()
                .collect(Collectors.toMap(m -> ((Number) m.get("categoryId")).intValue(), m -> m, (a, b) -> a));
        List<Map<String,Object>> list = new ArrayList<>();
        for (Category category : categoryService.selectAll(null)) {
            Map<String, Object> amount = amountByCategoryId.get(category.getId());
            BigDecimal total = amount == null ? BigDecimal.ZERO : new BigDecimal(amount.getOrDefault("amount", BigDecimal.ZERO).toString());
            if (total.compareTo(BigDecimal.ZERO) > 0) {
                Map<String, Object> map = new HashMap<>();
                map.put("name", category.getName());
                map.put("value", total);
                list.add(map);
            }
        }
        return Result.success(list);
   }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String token = request.getHeader("token");
        if (token != null) {
            TokenUtils.removeToken(token);
        }
        return Result.success();
    }

}
