-- ========================================
-- 话题功能验证脚本
-- 运行此脚本检查话题功能是否正常
-- ========================================

-- 1. 检查topics表结构
SELECT '=== 1. 检查topics表结构 ===' AS step;
SHOW CREATE TABLE topics\G

-- 2. 检查topics表是否有唯一索引
SELECT '=== 2. 检查name字段唯一索引 ===' AS step;
SHOW INDEX FROM topics WHERE Column_name = 'name';

-- 3. 检查topics表数据
SELECT '=== 3. 检查topics表数据 ===' AS step;
SELECT COUNT(*) AS total_topics FROM topics;
SELECT id, name, note_count, is_hot, status FROM topics ORDER BY is_hot DESC, note_count DESC LIMIT 10;

-- 4. 检查note_topics关联表
SELECT '=== 4. 检查note_topics关联表 ===' AS step;
SELECT COUNT(*) AS total_relations FROM note_topics;

-- 5. 检查笔记和话题的关联情况
SELECT '=== 5. 检查笔记话题关联 ===' AS step;
SELECT 
    n.id AS note_id,
    n.title,
    n.user_id,
    GROUP_CONCAT(t.name SEPARATOR ', ') AS topics,
    COUNT(t.id) AS topic_count
FROM notes n
LEFT JOIN note_topics nt ON n.id = nt.note_id
LEFT JOIN topics t ON nt.topic_id = t.id
WHERE n.status = 1
GROUP BY n.id, n.title, n.user_id
HAVING topic_count > 0
ORDER BY n.id DESC
LIMIT 10;

-- 6. 检查话题使用统计
SELECT '=== 6. 话题使用统计 ===' AS step;
SELECT 
    t.id,
    t.name,
    t.note_count AS recorded_count,
    COUNT(nt.id) AS actual_count,
    CASE 
        WHEN t.note_count = COUNT(nt.id) THEN '✓ 一致'
        ELSE '✗ 不一致'
    END AS status
FROM topics t
LEFT JOIN note_topics nt ON t.id = nt.topic_id
GROUP BY t.id, t.name, t.note_count
HAVING actual_count > 0
ORDER BY actual_count DESC
LIMIT 10;

-- 7. 检查是否有孤立的话题关联（话题不存在）
SELECT '=== 7. 检查孤立的话题关联 ===' AS step;
SELECT 
    nt.id,
    nt.note_id,
    nt.topic_id,
    '话题不存在' AS issue
FROM note_topics nt
LEFT JOIN topics t ON nt.topic_id = t.id
WHERE t.id IS NULL;

-- 8. 检查是否有孤立的话题关联（笔记不存在）
SELECT '=== 8. 检查孤立的笔记关联 ===' AS step;
SELECT 
    nt.id,
    nt.note_id,
    nt.topic_id,
    '笔记不存在' AS issue
FROM note_topics nt
LEFT JOIN notes n ON nt.note_id = n.id
WHERE n.id IS NULL;

-- 9. 最近创建的话题
SELECT '=== 9. 最近创建的话题 ===' AS step;
SELECT 
    id,
    name,
    note_count,
    is_hot,
    status,
    created_at
FROM topics
ORDER BY created_at DESC
LIMIT 10;

-- 10. 热门话题排行
SELECT '=== 10. 热门话题排行 ===' AS step;
SELECT 
    id,
    name,
    note_count,
    view_count,
    is_hot,
    CASE 
        WHEN is_hot = 1 THEN '🔥 热门'
        ELSE '普通'
    END AS type
FROM topics
WHERE status = 1
ORDER BY note_count DESC, view_count DESC
LIMIT 10;

SELECT '=== 验证完成 ===' AS result;
