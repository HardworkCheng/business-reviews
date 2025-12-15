-- 检查用户22是否存在
SELECT id, phone, username, avatar, created_at 
FROM users 
WHERE id = 22;

-- 检查用户20是否存在  
SELECT id, phone, username, avatar, created_at 
FROM users 
WHERE id = 20;

-- 查看所有用户
SELECT id, phone, username, avatar, created_at 
FROM users 
ORDER BY id DESC 
LIMIT 20;

-- 如果用户22不存在,但手机号16750152199存在,查找实际的userId
SELECT id, phone, username, avatar, created_at 
FROM users 
WHERE phone = '16750152199';
