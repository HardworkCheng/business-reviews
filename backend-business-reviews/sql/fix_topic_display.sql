-- ============================================
-- 修复话题回显问题 - 测试数据脚本
-- ============================================

-- 1. 首先检查topics表是否有数据
SELECT COUNT(*) as topic_count FROM topics;

-- 2. 插入一些测试话题（如果不存在）
INSERT INTO topics (name, description, is_hot, status) VALUES
('美食', '分享美食体验', 1, 1),
('推荐', '值得推荐的好店', 1, 1),
('必吃', '必吃榜单', 1, 1),
('探店', '探店打卡', 1, 1),
('甜品', '甜品推荐', 1, 1),
('火锅', '火锅美食', 1, 1),
('咖啡', '咖啡馆推荐', 1, 1),
('日料', '日本料理', 0, 1),
('川菜', '川菜美食', 0, 1),
('烧烤', '烧烤推荐', 0, 1)
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    is_hot = VALUES(is_hot),
    status = VALUES(status);

-- 3. 查看插入的话题
SELECT * FROM topics ORDER BY id;

-- 4. 为现有笔记添加话题关联（示例）
-- 注意：这里需要根据实际的笔记ID来调整

-- 查看现有笔记
SELECT id, title, user_id, created_at FROM notes WHERE status = 1 ORDER BY id DESC LIMIT 10;

-- 为最新的几条笔记添加话题（请根据实际笔记ID调整）
-- 假设笔记ID为1-10，为它们添加话题

-- 为笔记1添加"美食"和"推荐"话题
INSERT IGNORE INTO note_topics (note_id, topic_id) 
SELECT 1, id FROM topics WHERE name IN ('美食', '推荐');

-- 为笔记2添加"探店"和"必吃"话题
INSERT IGNORE INTO note_topics (note_id, topic_id) 
SELECT 2, id FROM topics WHERE name IN ('探店', '必吃');

-- 为笔记3添加"甜品"话题
INSERT IGNORE INTO note_topics (note_id, topic_id) 
SELECT 3, id FROM topics WHERE name = '甜品';

-- 5. 验证话题关联
SELECT 
    n.id as note_id,
    n.title,
    n.user_id,
    t.id as topic_id,
    t.name as topic_name
FROM notes n
LEFT JOIN note_topics nt ON n.id = nt.note_id
LEFT JOIN topics t ON nt.topic_id = t.id
WHERE n.status = 1
ORDER BY n.id DESC, t.id;

-- 6. 查看特定笔记的话题
-- 请将 <NOTE_ID> 替换为你要测试的笔记ID
-- SELECT 
--     n.id as note_id,
--     n.title,
--     t.id as topic_id,
--     t.name as topic_name
-- FROM notes n
-- LEFT JOIN note_topics nt ON n.id = nt.note_id
-- LEFT JOIN topics t ON nt.topic_id = t.id
-- WHERE n.id = <NOTE_ID>;

-- 7. 清理测试数据（如果需要）
-- DELETE FROM note_topics WHERE note_id IN (1, 2, 3);
-- DELETE FROM topics WHERE name IN ('美食', '推荐', '必吃', '探店', '甜品', '火锅', '咖啡', '日料', '川菜', '烧烤');
