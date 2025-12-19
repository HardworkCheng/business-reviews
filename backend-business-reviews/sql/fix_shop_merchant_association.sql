-- 修复商家店铺关联问题
-- 此脚本用于修复现有数据中shop表缺少merchantId的问题

-- 1. 检查当前数据状态
SELECT 
    s.id as shop_id,
    s.name as shop_name,
    s.merchant_id,
    m.id as merchant_id_from_merchant_table,
    m.name as merchant_name
FROM shops s
LEFT JOIN merchants m ON s.merchant_id = m.id
ORDER BY s.id;

-- 2. 查找没有关联商家的店铺
SELECT 
    id,
    name,
    merchant_id
FROM shops 
WHERE merchant_id IS NULL;

-- 3. 如果有商家但没有店铺，为每个商家创建默认店铺
-- 注意：这个脚本需要根据实际情况调整
INSERT INTO shops (
    merchant_id,
    name,
    description,
    status,
    category_id,
    business_hours,
    rating,
    taste_score,
    environment_score,
    service_score,
    review_count,
    popularity,
    created_at,
    updated_at
)
SELECT 
    m.id as merchant_id,
    CONCAT(m.name, '店铺') as name,
    '欢迎来到我的店铺' as description,
    1 as status,
    1 as category_id,
    '09:00-22:00' as business_hours,
    5.0 as rating,
    5.0 as taste_score,
    5.0 as environment_score,
    5.0 as service_score,
    0 as review_count,
    0 as popularity,
    NOW() as created_at,
    NOW() as updated_at
FROM merchants m
WHERE NOT EXISTS (
    SELECT 1 FROM shops s WHERE s.merchant_id = m.id
);

-- 4. 验证修复结果
SELECT 
    COUNT(*) as total_merchants,
    (SELECT COUNT(*) FROM shops WHERE merchant_id IS NOT NULL) as shops_with_merchant,
    (SELECT COUNT(*) FROM shops WHERE merchant_id IS NULL) as shops_without_merchant
FROM merchants;

-- 5. 显示最终的商家-店铺关联情况
SELECT 
    m.id as merchant_id,
    m.name as merchant_name,
    s.id as shop_id,
    s.name as shop_name,
    s.status as shop_status
FROM merchants m
LEFT JOIN shops s ON m.id = s.merchant_id
ORDER BY m.id, s.id;