-- ========================================
-- 修正类目名称以匹配UniApp首页显示
-- ========================================
-- 目的：统一商家运营中心与UniApp移动端的经营类目名称
-- 执行前请备份categories表数据
-- ========================================

-- 备份当前数据（可选，建议执行）
-- CREATE TABLE categories_backup AS SELECT * FROM categories;

-- 修正类目名称
UPDATE categories SET name = '美发' WHERE id = 3;
UPDATE categories SET name = '美甲' WHERE id = 4;
UPDATE categories SET name = '足疗' WHERE id = 5;
UPDATE categories SET name = '美容' WHERE id = 6;
UPDATE categories SET name = '游乐' WHERE id = 7;

-- 验证修正结果
SELECT id, name, icon, color, sort_order, status 
FROM categories 
WHERE status = 1 
ORDER BY sort_order;

-- ========================================
-- 预期结果：
-- id | name | sort_order
-- 1  | 美食 | 1
-- 2  | KTV  | 2
-- 3  | 美发 | 3
-- 4  | 美甲 | 4
-- 5  | 足疗 | 5
-- 6  | 美容 | 6
-- 7  | 游乐 | 7
-- 8  | 酒吧 | 8
-- ========================================
