-- =====================================================
-- 密码加密升级 SQL 脚本
-- 将现有账号密码统一重置为 BCrypt 加密的默认口令
-- =====================================================
USE `shop`;

-- ⚠️ 重要提示：
-- 1. 执行前请务必备份数据库！
-- 2. 这会将所有账号的密码重置为默认口令
-- 3. 管理员默认口令: admin
-- 4. 普通用户默认口令: 123456
--
-- ✅ 本文件中的哈希已用项目自身的 BCryptPasswordEncoder 实测校验通过
--    （此前版本里的哈希经实测**无法通过校验**，执行后会导致所有账号登录失败）
--    如需重新生成，运行 src/test/java/com/example/utils/PasswordTest.java

-- 更新管理员密码（明文 "admin" 的 BCrypt 值，已校验）
UPDATE `admin` SET password = '$2a$10$jcUgvGD5J2HaR/gRdFP4muew4sPUqB9KQSVcXfK57s5Db/up..0vS';

-- 更新所有普通用户密码（明文 "123456" 的 BCrypt 值，已校验）
UPDATE `user` SET password = '$2a$10$yomG3iKZ20vfhVO54lSwFewElQIjU2974z91f7ilcKRK1lZuTy1V.';

-- =====================================================
-- 验证更新结果
-- =====================================================
SELECT id, username, LEFT(password, 7) AS hash_prefix, LENGTH(password) AS hash_len FROM `admin`;
SELECT id, username, LEFT(password, 7) AS hash_prefix, LENGTH(password) AS hash_len FROM `user`;

-- =====================================================
-- 说明：
-- - BCrypt 哈希值每次生成都不同，但都能正确验证
-- - 哈希值长度为 60 个字符；字段长度不足时需先扩容：
--   ALTER TABLE `admin` MODIFY COLUMN `password` VARCHAR(100);
--   ALTER TABLE `user`  MODIFY COLUMN `password` VARCHAR(100);
-- =====================================================
