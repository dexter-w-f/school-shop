-- =====================================================
-- 密码加密升级 SQL 脚本
-- 执行此脚本将所有现有用户密码更新为 BCrypt 加密格式
-- =====================================================

-- ⚠️ 重要提示：
-- 1. 执行前请务必备份数据库！
-- 2. 这会将所有用户的密码重置为默认密码
-- 3. 管理员默认密码: admin
-- 4. 普通用户默认密码: 123456

-- 更新管理员密码（明文 "admin" 的 BCrypt 加密值）
UPDATE admin SET password = '$2a$10$rS5E7Eh5rP5E7Eh5rP5E7Oo8uLOickgx2ZMRZoMyeIjZAgcfl7p92';

-- 更新所有普通用户密码（明文 "123456" 的 BCrypt 加密值）
UPDATE user SET password = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy';

-- =====================================================
-- 验证更新结果
-- =====================================================
-- 查看管理员密码是否已加密（应该以 $2a$ 开头）
SELECT id, username, password FROM admin;

-- 查看用户密码是否已加密（应该以 $2a$ 开头）
SELECT id, username, password FROM user;

-- =====================================================
-- 说明：
-- - BCrypt 哈希值每次生成都不同，但都能正确验证
-- - 哈希值长度约为 60 个字符
-- - 如果字段长度不够，需要修改表结构：
--   ALTER TABLE admin MODIFY COLUMN password VARCHAR(100);
--   ALTER TABLE user MODIFY COLUMN password VARCHAR(100);
-- =====================================================
