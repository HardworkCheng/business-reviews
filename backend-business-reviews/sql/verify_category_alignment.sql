-- ========================================
-- 类目数据验证脚本
-- ========================================
-- 用于验证类目名称是否与UniApp首页一致
-- ========================================

-- 1. 查看当前所有类目数据
SELECT 
    id,
    name,
    icon,
    color,
    sort_order,
    status,
    created_at,
    updated_at
FROM categories
ORDER BY sort_order;

-- 2. 检查启用状态的类目数量（应该是8个）
SELECT COUNT(*) as active_category_count
FROM categories
WHERE status = 1;

-- 3. 检查类目名称是否与UniApp一致
-- 预期结果：8行数据，每行matched = 1
SELECT 
    c.id,
    c.name as db_name,
    CASE c.id
        WHEN 1 THEN '美食'
        WHEN 2 THEN 'KTV'
        WHEN 3 THEN '美发'
        WHEN 4 THEN '美甲'
        WHEN 5 THEN '足疗'
        WHEN 6 THEN '美容'
        WHEN 7 THEN '游乐'
        WHEN 8 THEN '酒吧'
    END as expected_name,
    CASE 
        WHEN c.name = CASE c.id
            WHEN 1 THEN '美食'
            WHEN 2 THEN 'KTV'
            WHEN 3 THEN '美发'
            WHEN 4 THEN '美甲'
            WHEN 5 THEN '足疗'
            WHEN 6 THEN '美容'
            WHEN 7 THEN '游乐'
            WHEN 8 THEN '酒吧'
        END THEN 1
        ELSE 0
    END as matched
FROM categories c
WHERE c.status = 1 AND c.id BETWEEN 1 AND 8
ORDER BY c.sort_order;

-- 4. 检查是否有不匹配的类目
SELECT 
    id,
    name,
    'Name mismatch' as issue
FROM categories
WHERE status = 1 
AND id BETWEEN 1 AND 8
AND name NOT IN ('美食', 'KTV', '美发', '美甲', '足疗', '美容', '游乐', '酒吧');

-- 5. 检查关联的店铺数量
SELECT 
    c.id,
    c.name,
    COUNT(s.id) as shop_count
FROM categories c
LEFT JOIN shops s ON c.id = s.category_id
WHERE c.status = 1
GROUP BY c.id, c.name
ORDER BY c.sort_order;

-- ========================================
-- 验证通过标准：
-- 1. active_category_count = 8
-- 2. 所有类目的matched = 1
-- 3. 不匹配查询返回0行
-- ========================================
