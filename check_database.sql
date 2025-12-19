-- 检查商家店铺关联状态
SELECT 
    COUNT(*) as total_merchants
FROM merchants;

SELECT 
    COUNT(*) as total_shops
FROM shops;

SELECT 
    COUNT(*) as shops_with_merchant
FROM shops 
WHERE merchant_id IS NOT NULL;

SELECT 
    COUNT(*) as shops_without_merchant
FROM shops 
WHERE merchant_id IS NULL;

-- 显示商家-店铺关联情况
SELECT 
    m.id as merchant_id,
    m.name as merchant_name,
    s.id as shop_id,
    s.name as shop_name,
    s.merchant_id
FROM merchants m
LEFT JOIN shops s ON m.id = s.merchant_id
ORDER BY m.id
LIMIT 10;