-- 检查用户表数据
SELECT id, phone, username, password, gender, birthday, bio, wechat_openid, qq_openid, weibo_uid, created_at 
FROM users 
ORDER BY id DESC 
LIMIT 10;

-- 如果发现某个用户的phone为空,可以手动更新:
-- UPDATE users SET phone = '13800138000' WHERE id = 1;

-- 清除Redis缓存的方法(如果使用Redis-CLI):
-- KEYS user:info:*
-- DEL user:info:1  (将1替换为实际的用户ID)
