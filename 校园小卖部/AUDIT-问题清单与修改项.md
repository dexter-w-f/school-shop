# 校园小卖部 项目审计报告 —— 问题清单与需要修改的项

- **审计对象**：`C:\Users\34048\Desktop\校园小卖部`
- **技术栈**：SpringBoot 3.3.1 + Vue 3 + MyBatis(XML) + Redis + 支付宝沙箱
- **审计日期**：本次会话
- **规模**：86 个 Java（6018 行）、41 个 Vue（5523 行）、16 个 mapper XML、8 个 SQL 脚本

## 审计方法与已验证证据（非纯静态阅读）

| 验证手段 | 结果 |
|---|---|
| `mvn -o compile`（真实执行） | **失败，5 个编译错误**（见 P0-1） |
| `@vue/compiler-sfc` 解析全部 41 个 .vue | 41/41 通过，模板无语法错误 |
| 144 处前端接口调用 ↔ 102 个后端端点交叉比对 | 路径全部匹配，无 404 类缺陷 |
| 全量扫描 mapper 中 `${}` 拼接 | 0 处，**无 SQL 注入**（已排除） |
| 全量扫描依赖/资源引用、字符编码 | 无断链；发现 1 个中文乱码文件 |
| 端口/服务探活 | Redis 未启动、MySQL 已启动、前后端均未运行 |

> **总体结论**：项目当前**无法交付**。后端编译不过（P0-1），数据库脚本建不出库（P0-2）；即使修好这两项，仍有 5 条主链路会在运行期报错或静默失效，并存在可白拿商品、可双倍套现的资金链路漏洞。

---

## 一、P0 阻断级（现在根本跑不起来）

### P0-1 后端编译失败，5 个错误 ⛔ 最高优先级

`mvn -o compile` 实测输出：

| 文件:行 | 错误 | 需要的修改 |
|---|---|---|
| `校园小卖部/code2026/springboot/src/main/java/com/example/controller/OrdersController.java:23` | `找不到符号: 类 HttpServletRequest`（被当字段 `@Resource` 注入但**未 import**） | 加 `import jakarta.servlet.http.HttpServletRequest;` 或改为方法形参 |
| `OrdersController.java:77` | `找不到符号: 类 List` | 加 `import java.util.List;` |
| `.../mapper/OrderDetailMapper.java:43` | `找不到符号: 类 Param` | 加 `import org.apache.ibatis.annotations.Param;` |
| `.../controller/SeckillController.java:48` | `找不到符号: 变量 request`（`list()` 调 `requireAdmin(request)`，无该字段/形参） | 方法加 `HttpServletRequest request` 形参 |
| `SeckillController.java:66` | 同上（`add()`） | 同上 |

佐证：`target/classes` 下无任何 `.class`，`createdFiles.lst` 为空，印证最后一次构建即失败。
**此项不修，其余验证无从谈起。**

### P0-2 数据库脚本不完整，无法从零建库 ⛔

- 基础脚本 `校园小卖部/code2026.sql`（41 行）**只有一张 `admin` 表**。
- 代码需要的 `user`、`goods`、`orders`、`order_detail`、`cart`、`category`、`collect`、`comment`、`carousel` 九张核心表**在任何脚本里都没有 CREATE 语句**。
- 连带后果：`alter_orders_paytype.sql:2`、`alter_order_goods_version.sql:2`、`update_passwords.sql:16` 等 7 个增量脚本**从第一句就失败**。
- `docker-compose.yml:15-16` 只挂载 `code2026.sql` + `update_passwords.sql`，7 个 alter 全漏 → 容器初始化必然半坏。
- 基础脚本头注释 `Source Schema: system`，而 `application.yml:10` 连的是 `shop`。

**需要的修改**：补一份完整 `schema.sql`（16 张表 DDL + 索引 + 种子数据），修正 compose 挂载与顺序。这是清单中工作量最大的一项。

---

## 二、P1 严重（主流程必现故障）

### P1-1 管理端订单主流程完全失效（前后端状态机不对齐）

1. **管理员看不到任何订单**：管理端 `vue/src/views/manager/Orders.vue:122` 调 `/orders/selectPage`，但 `OrdersController.java:88-89` 强制 `orders.setUserId(currentUserId)` → 只查管理员自己下的单，**列表恒为空**。
2. **"出货"按钮点了没反应却提示成功**：界面把状态改为 `已出货`/`已配送` 后 PUT `/orders/update`，但 `OrdersService.java:121-156` 的 `updateById` **只处理 `已取消` 一种状态**，其余静默 return；控制器仍返回 `code 200` → 提示"操作成功"但数据库未变。
3. **用户永远走不到"确认收货"**：`front/UserOrders.vue:76` 按钮条件是 `已出货`/`已配送`，而这两个状态**后端从未设置过** → 订单卡在"待接单"，流程断死。
4. **管理端"删除"订单必然失败**：`OrdersController.java:39-43` 校验订单归属，管理员删用户订单被拒。

**需要的修改**：为管理端提供独立查询接口（不强制 userId）；补全状态机 `待接单→已出货/已配送→待收货→已完成`；或按文档所述新增 `PUT /orders/out`、`/orders/delivery`。

> ⚠️ 根目录 `BugFixSummary.md` 声称已新增 `/orders/out`、`/orders/delivery` 并统一走 `isAllowedStatusTransition`，但**代码中全部不存在**（grep 0 命中）。该文档已失效，需作废或重写。

### P1-2 秒杀列表/活动接口必抛 SQL 异常

`springboot/src/main/resources/mapper/SeckillActivityMapper.xml:16,17,21,27,48,49` 引用 `goods_name`、`goods_img`，但 `alter_seckill.sql` 建表无此两列 → `GET /seckill/list`、`/seckill/active` 报 `1054 Unknown column`。
**修改**：select 改 `left join goods` 取 `g.name`/`g.img` 别名；insert/update 删掉这两个 `<if>`。

### P1-3 五个 MyBatis 方法无对应 SQL，帖子功能 500

`mapper/PostMapper.java:14-22` 的 `incrementViewCount`、`incrementLikeCount`、`decrementLikeCount`、`incrementReplyCount`、`decrementReplyCount` 在 `PostMapper.xml` 中**无任何语句**，却被 `PostService`、`ReplyService` 调用 → `Invalid bound statement`。
**修改**：补 `PostMapper.xml` 中 5 条语句。

### P1-4 后台首页统计接口必 500（MyBatis 参数绑定错误）

`mapper/UserMapper.java:9` 为 `selectAll(String name)` 且**缺 `@Param`**，`UserMapper.xml:10-11` 使用 `#{name}` → 抛 `There is no getter for property named 'name' in 'class java.lang.String'`。
受影响：`GET /count`（`manager/Home.vue`、`manager/DataManager.vue` 都调）、`/user/selectPage`。
**修改**：加 `@Param("name")`。

### P1-5 客服聊天记录接口必 500

`ChatMessageMapper.xml:29` 的 `selectSessions` 无条件 SELECT `user_name`，`alter_chat_message.sql` 的 `chat_message` 表无该列 → `/chat/manage/sessions` 报 `1054`。
**修改**：补列或改 SQL。

### P1-6 邮箱验证码链路整体不可用

- `vue/email-sender.js` `require("nodemailer")`，但 **nodemailer 既不在 package.json 也不在 node_modules** → 脚本必崩。
- `CaptchaController.java:43` 路径拼接为 `user.dir + "/?????/code2026/vue/email-sender.js"` —— 中文目录名已成 `?????` 乱码，路径必然不存在。
- 结论：验证码**从未真正发出**。

**修改**：安装并声明 nodemailer 依赖；脚本路径改为可配置项；修复文件编码。

---

## 三、P2 安全问题（按可利用性排序）

### P2-1 ⚠️ 验证码直接回显在响应里（注册无门槛）

`controller/CaptchaController.java:37,51,53,56`：限流、成功、失败**三条分支全都 `return Result.success(code)`**，把 6 位验证码原样返回；该接口在鉴权白名单内。
**修改**：响应只返回"已发送"，不得包含验证码。

### P2-2 ⚠️ 8 个控制器的写接口零鉴权

`SecurityConfig` 用 `anyRequest().permitAll()` 关闭 Spring Security；唯一门禁 `AuthInterceptor` **只校验"是否登录"、不校验角色**。任意普通用户带自己 token 即可：

| 文件 | 无鉴权接口 |
|---|---|
| `controller/GoodsController.java:25,34,43` | 增/删/改商品（可改价格、库存） |
| `controller/UserController.java:24,36,45` | 全站用户分页、删除任意用户、新增用户 |
| `controller/CategoryController.java`、`CarouselController.java`、`CommentController.java` | 各自增/删/改 |
| `controller/PostController.java:16-18`、`ReplyController.java:16-17` | 增/删/改 |
| `controller/ChatManageController.java:18,23` | 读取**全站**客服聊天记录 |

**修改**：统一补 `AdminControllerUtils.requireAdmin(request)`。

### P2-3 ⚠️ 订单越权：可取消/退款他人订单

- `controller/OrdersController.java:51-55` 的 `/orders/update` **无归属校验、无状态白名单** → 可把任意他人订单改成"已取消"并触发退款分支。
- `OrdersController.java:28-32` 的 `/orders/add` 直接信任 body 的 `userId` → 可替他人下单、扣他人商品库存。
- `controller/PaymentController.java:45-53,58-65` 的 `alipayPay`/`queryStatus` 只校验"已登录"，不校验订单归属。

**修改**：归属校验；userId 一律取自 `AuthValidator.requireUserId(request)`；状态枚举白名单。

### P2-4 ⚠️ 支付链路可白拿商品 / 双重套现

- **回调不验签**：`PaymentController.java:70-89` → `OrdersService.java:286-304` 只检查 `out_trade_no` 与 `trade_status=TRADE_SUCCESS`，无签名校验 → 伪造回调即可把订单置为已支付。
- **退余额不判支付方式**：`OrdersService.java:134-138` 只判断"不是待支付"就退余额 → 支付宝支付订单也会退到余额（两边都拿）。
- **取消退款 + 售后退款可叠加**：取消走 `OrdersService.java:126-138` 退一次，售后走 `RefundOrdersService.java:168-179` 再退一次；且 `RefundOrdersService.java:186` 仅在订单"尚未取消"时回滚库存 → 稳定双倍套现。

**修改**：补 `AlipaySignature.rsaCheckV1` 验签 + 金额比对；按 `payType` 决定退款渠道；引入幂等退款流水表（唯一约束/条件更新）。

### P2-5 ⚠️ 明文凭据入库（已实测确认）

| 位置 | 泄露内容 |
|---|---|
| `springboot/src/main/resources/application.yml:9` | 数据库口令 `123456` |
| `application.yml:32` | DeepSeek API Key（明文，值已在此报告中隐去） |
| `application.yml:40` | **支付宝应用私钥完整明文** |
| `vue/email-sender.js:6` | 163 邮箱账号 + **SMTP 授权码（明文，已在此报告中隐去）** |

**修改**：全部外置为环境变量/配置中心，并**轮换**上述全部密钥（改配置无法撤销已泄露的密钥）。

### P2-6 ⚠️ 全局关闭 HTTPS 证书校验

`config/AlipayConfig.java:59-75` 调用 `disableSSLValidation()`，设置 `HttpsURLConnection.setDefaultSSLSocketFactory` + `HostnameVerifier` 恒 true —— **JVM 全局**生效，使进程内所有 HTTPS 调用（含 DeepSeek）失去中间人防护。
**修改**：删除该方法及其调用。

### P2-7 其他安全项

| 问题 | 位置 | 修改 |
|---|---|---|
| 鉴权失败被吞：401/403 变成 HTTP 200 + code 500 | `exception/GlobalExceptionHandler.java:19-24` | 单独处理 `ResponseStatusException` 并透传状态码 |
| 仅凭 `X-Current-Role` 请求头判管理员 | `controller/FileController.java:47,60,115`、`SeckillController.java:199-203` | 改调 `AdminControllerUtils.requireAdmin` |
| token 无签名无过期（内存 UUID），重启掉线、多实例不共享 | `utils/TokenUtils.java` | 换 JWT（带 exp+签名）或 Redis |
| XSS：用户可控内容走 `v-html` | `front/PostDetail.vue:13`、`front/GoodsDetail.vue:60`、`manager/Goods.vue:112` | 富文本净化 |
| 隐私泄露：AI 可复述他人余额/订单 | `controller/ChatController.java:53,75-94` | userId 取自会话 |
| `/user/update` 可免旧密码改密 | `controller/UserController.java:58-78` | 改密走校验旧密码的接口 |
| 路径穿越（疑似，利用性受限） | `controller/FileController.java:45-53,92-108` | `Path.normalize()` + 前缀校验；改捕 `Exception` |

> ✅ **已排除的两个常见误报**：全项目 mapper **无 `${}` 拼接，不存在 SQL 注入**；密码使用 **BCrypt（带盐）**，算法正确，无明文回退。

---

## 四、P3 中等与低优先级

### 中

| 问题 | 位置 | 修改 |
|---|---|---|
| 时间字段未判空 → NPE | `SeckillController.java:103,107` | 判空后再比较 |
| 秒杀库存初始化非原子 + DB 读改写 → **可超卖** | `SeckillController.java:144-150,177-178`；`SeckillActivityMapper.xml` | `setIfAbsent` 初始化；补 `update total_stock = total_stock - 1 where total_stock >= 1` 并判受影响行数 |
| 秒杀 `userId` 由参数指定 | `SeckillController.java:118` | userId 取自会话 |
| `getExpire` 与 `Boolean.FALSE.equals` 比较，TTL 兜底失效 | `SeckillController.java:186` | 改判 `null`/`< 0` |
| 库存可被设为**负数** | `controller/InventoryController.java:41-49` | 校验 `store >= 0` |
| `sum(o.total)` 按明细行重复累加 → 统计虚高 | `mapper/OrderDetailMapper.xml:74-91` | 改 `sum(od.goods_price * od.num)` 或先去重 |
| 限流可绕过（读取路径 `remove` 清空计数） | `utils/LoginAttemptLimiter.java:24-27` | 改 Redis 原子计数+TTL，不在读路径删计数 |
| `cartList` 未判空（NPE）+ 数量无正负校验（**负数反向加库存**） | `service/OrdersService.java:57-75` | 非空 + `num > 0` + 上限校验 |
| 定时任务状态集合缺"待支付" | `task/OrderTimeoutTask.java:28-30,56-75` | 补状态或在 SQL 过滤 + 建索引 |
| 新增用户/注册"查重+插入"无事务 | `service/UserService.java:34-51`、`AdminService.java:28-43` | 补 `@Transactional` + 唯一索引 |
| BCrypt 哈希疑似无效 → 迁移后**全员无法登录**（⚠️ 疑似，未实测） | `update_passwords.sql:13,16`、`密码加密升级说明.md:34-40` | 用 BCrypt 现算后重新迁移 |
| 死列：`goods.version`（乐观锁）、`order_detail.activity_id` 全项目零引用 | `alter_order_goods_version.sql` | 实现或删除 |
| 缺索引：`orders(status,time)`、`reply.post_id`、`post(user_id,status)`、`refund_orders(order_id,user_id)` | SQL 脚本 | 补索引 |
| `select *` 把 password 哈希返回前端 | `UserMapper.xml:8`、`AdminMapper.xml:8,16` | 显式列白名单 |
| 支付宝 `bizContent` 字符串拼 JSON | `service/AlipayService.java:32-34,85-86` | 用 JSON 库序列化 |

### 低（清理项）

| 问题 | 位置 | 修改 |
|---|---|---|
| 5 个遗留文件污染交付物 | `springboot/ChatController.java.tmp`（同名类完整副本）、`pom.xml.bak`、`sources.txt`（含机器绝对路径）、`compile.log`、`codex-settings.xml`（个人仓库 `D:/dexter/m2repo`） | 全部删除 / 移出项目 |
| 10 个依赖重复声明（部分 `provided/optional`，有 `NoClassDefFoundError` 风险）；插件缺 version | `springboot/pom.xml` | 删除重复块；补插件版本或用 starter-parent |
| `forbidAdminUser` 判空后无条件抛 403，逻辑自相矛盾且无调用 | `config/AuthValidator.java:18-24` | 删除 |
| `PasswordTest.java` 位于 `src/main/java`，会打进生产包并打印默认口令 | `utils/PasswordTest.java` | 移入 `src/test` 或删除 |
| 死代码 | `RefundOrdersService.java:142-144`（不可达）、`RefundOrdersController.java:48-58`（`isEmpty()` 恒 false）、`SeckillController.ADMIN_PATHS` 常量、多处未用 import | 清理 |
| 管理端路由无角色守卫 | `vue/src/router/index.js:61-63` 的 `beforeEach` 只做滚动 | 加角色守卫 |
| 上传图片对普通用户全部 403 | `controller/FileController.java:46-51`（`<img>` 无法携带自定义头） | 改静态资源映射或放行下载 |
| 中文乱码 | `CaptchaController.java:27,43,44,52,55` | 修复文件编码 |
| 无效参数 | `WebController.java:135` `sumTotal("已取消")` 参数在 SQL 中未被引用 | 删除参数或让 SQL 使用 |

---

## 五、修复顺序建议

1. **补 4 个 import + 2 个方法形参** → 让 `mvn compile` 通过（P0-1）。
2. **补齐完整建库脚本**，并修 `SeckillActivityMapper.xml`、`PostMapper.xml`、`UserMapper.java`、`ChatMessageMapper.xml` 四处映射缺陷（P0-2、P1-2~P1-5）。
3. **补全订单状态机**并给管理端独立查询接口（P1-1）。
4. **安全加固**：鉴权收口、支付验签、密钥外置与轮换、验证码不回显（P2）。
5. **清理与加固**：P3 各项、遗留文件、pom 去重、索引。

> 前三步做完项目才可能启动并跑通主流程。

---

## 六、文档一致性提醒

根目录 `BugFixSummary.md` 记录的"已修复"项在当前代码中**均不存在**（`/orders/out`、`/orders/delivery`、`isAllowedStatusTransition`、`GoodsDetail` 改文本渲染）。该文档已失效，建议作废或按本报告重写，否则会持续误导维护判断。
