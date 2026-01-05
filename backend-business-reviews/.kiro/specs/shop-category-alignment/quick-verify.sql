-- ========================================
-- 快速验证脚本
-- ========================================
-- 用于快速检查类目对齐的关键指标
-- ========================================

-- 显示验证开始信息
SELECT '========================================' AS '';
SELECT '类目对齐快速验证' AS '';
SELECT '========================================' AS '';
SELECT '' AS '';

-- 1. 检查类目数量
SELECT '1. 检查类目数量（应该是8）' AS '';
SELECT COUNT(*) as '启用的类目数量'
FROM categories
WHERE status = 1;
SELECT '' AS '';

-- 2. 检查类目名称和顺序
SELECT '2. 检查类目名称和顺序' AS '';
SELECT 
    id as 'ID',
    name as '类目名称',
    sort_order as '排序',
    status as '状态'
FROM categories
WHERE status = 1
ORDER BY sort_order;
SELECT '' AS '';

-- 3. 验证类目名称是否与预期一致
SELECT '3. 验证类目名称一致性' AS '';
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 1 AND name = '美食') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 2 AND name = 'KTV') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 3 AND name = '美发') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 4 AND name = '美甲') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 5 AND name = '足疗') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 6 AND name = '美容') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 7 AND name = '游乐') = 1
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND id = 8 AND name = '酒吧') = 1
        THEN '✓ 通过 - 所有类目名称正确'
        ELSE '✗ 失败 - 类目名称不匹配'
    END as '验证结果';
SELECT '' AS '';

-- 4. 检查每个类目关联的店铺数量
SELECT '4. 每个类目的店铺数量' AS '';
SELECT 
    c.id as 'ID',
    c.name as '类目名称',
    COUNT(s.id) as '店铺数量'
FROM categories c
LEFT JOIN shops s ON c.id = s.category_id
WHERE c.status = 1
GROUP BY c.id, c.name
ORDER BY c.sort_order;
SELECT '' AS '';

-- 5. 检查是否有店铺使用了无效的类目ID
SELECT '5. 检查无效的类目关联' AS '';
SELECT 
    CASE 
        WHEN COUNT(*) = 0 THEN '✓ 通过 - 没有无效的类目关联'
        ELSE CONCAT('✗ 失败 - 发现 ', COUNT(*), ' 个店铺使用了无效的类目ID')
    END as '验证结果'
FROM shops s
LEFT JOIN categories c ON s.category_id = c.id
WHERE s.category_id IS NOT NULL 
  AND (c.id IS NULL OR c.status != 1);

-- 如果有无效关联，显示详情
SELECT 
    s.id as '店铺ID',
    s.name as '店铺名称',
    s.category_id as '类目ID',
    '无效的类目ID' as '问题'
FROM shops s
LEFT JOIN categories c ON s.category_id = c.id
WHERE s.category_id IS NOT NULL 
  AND (c.id IS NULL OR c.status != 1)
LIMIT 10;
SELECT '' AS '';

-- 6. 总体验证结果
SELECT '========================================' AS '';
SELECT '总体验证结果' AS '';
SELECT '========================================' AS '';
SELECT 
    CASE 
        WHEN (SELECT COUNT(*) FROM categories WHERE status = 1) = 8
         AND (SELECT COUNT(*) FROM categories WHERE status = 1 AND name IN ('美食', 'KTV', '美发', '美甲', '足疗', '美容', '游乐', '酒吧')) = 8
         AND (SELECT COUNT(*) FROM shops s LEFT JOIN categories c ON s.category_id = c.id WHERE s.category_id IS NOT NULL AND (c.id IS NULL OR c.status != 1)) = 0
        THEN '✓✓✓ 全部通过 - 类目对齐验证成功 ✓✓✓'
        ELSE '✗✗✗ 验证失败 - 请检查上述问题 ✗✗✗'
    END as '最终结果';
SELECT '' AS '';

-- 7. 显示预期的类目列表（供对比）
SELECT '预期的类目列表（供对比）' AS '';
SELECT 1 as 'ID', '美食' as '类目名称'
UNION ALL SELECT 2, 'KTV'
UNION ALL SELECT 3, '美发'
UNION ALL SELECT 4, '美甲'
UNION ALL SELECT 5, '足疗'
UNION ALL SELECT 6, '美容'
UNION ALL SELECT 7, '游乐'
UNION ALL SELECT 8, '酒吧';
