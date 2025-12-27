-- ========================================
-- 话题数据检查和修复脚本
-- ========================================

-- 1. 检查topics表是否有数据
SELECT '=== 检查topics表数据 ===' AS info;
SELECT COUNT(*) AS topic_count FROM topics;
SELECT * FROM topics LIMIT 10;

-- 2. 检查note_topics关联表数据
SELECT '=== 检查note_topics关联表 ===' AS info;
SELECT COUNT(*) AS relation_count FROM note_topics;
SELECT nt.*, t.name AS topic_name 
FROM note_topics nt 
LEFT JOIN topics t ON nt.topic_id = t.id 
LIMIT 10;

-- 3. 检查笔记表中是否有笔记
SELECT '=== 检查notes表数据 ===' AS info;
SELECT COUNT(*) AS note_count FROM notes WHERE status = 1;

-- 4. 如果topics表为空,插入一些热门话题数据
INSERT INTO topics (name, description, is_hot, status) VALUES
('美食探店', '分享你的美食体验', 1, 1),
('周末好去处', '周末休闲娱乐推荐', 1, 1),
('打卡圣地', '网红打卡地点分享', 1, 1),
('优惠活动', '商家优惠信息分享', 1, 1),
('新店开业', '新开业商家推荐', 1, 1),
('环境优美', '环境好的店铺推荐', 1, 1),
('服务贴心', '服务好的商家分享', 1, 1),
('性价比高', '高性价比商家推荐', 1, 1),
('约会圣地', '适合约会的地方', 1, 1),
('亲子好去处', '适合带孩子的地方', 1, 1),
('下午茶', '下午茶推荐', 1, 1),
('夜宵好去处', '夜宵美食推荐', 1, 1),
('健康养生', '健康养生类商家', 1, 1),
('美容美发', '美容美发推荐', 1, 1),
('休闲娱乐', '休闲娱乐场所', 1, 1),
('运动健身', '运动健身场所', 1, 1),
('文艺小资', '文艺范儿的店铺', 1, 1),
('网红店', '网红店铺推荐', 1, 1),
('老字号', '传统老字号推荐', 1, 1),
('特色小吃', '特色小吃推荐', 1, 1)
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 5. 再次检查topics表数据
SELECT '=== 插入后检查topics表 ===' AS info;
SELECT COUNT(*) AS topic_count FROM topics;
SELECT id, name, is_hot, status FROM topics ORDER BY is_hot DESC, id ASC;

-- 6. 检查是否有笔记关联了话题
SELECT '=== 检查笔记话题关联情况 ===' AS info;
SELECT 
    n.id AS note_id,
    n.title,
    GROUP_CONCAT(t.name SEPARATOR ', ') AS topics
FROM notes n
LEFT JOIN note_topics nt ON n.id = nt.note_id
LEFT JOIN topics t ON nt.topic_id = t.id
WHERE n.status = 1
GROUP BY n.id
LIMIT 10;
