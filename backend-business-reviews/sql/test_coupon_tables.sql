-- ========================================
-- 测试脚本：验证优惠券表是否创建成功
-- ========================================

USE `business_reviews`;

-- 检查所有表是否存在
SELECT 
  'coupons' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'coupons'

UNION ALL

SELECT 
  'user_coupons' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'user_coupons'

UNION ALL

SELECT 
  'seckill_activities' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'seckill_activities'

UNION ALL

SELECT 
  'seckill_coupons' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'seckill_coupons'

UNION ALL

SELECT 
  'user_seckill_records' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'user_seckill_records'

UNION ALL

SELECT 
  'coupon_view_logs' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'coupon_view_logs'

UNION ALL

SELECT 
  'coupon_search_logs' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'coupon_search_logs'

UNION ALL

SELECT 
  'coupon_statistics' AS table_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.tables 
WHERE table_schema = 'business_reviews' AND table_name = 'coupon_statistics';

-- 检查视图是否存在
SELECT 
  'v_user_coupon_details' AS view_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.views 
WHERE table_schema = 'business_reviews' AND table_name = 'v_user_coupon_details'

UNION ALL

SELECT 
  'v_available_coupons' AS view_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.views 
WHERE table_schema = 'business_reviews' AND table_name = 'v_available_coupons';

-- 检查存储过程是否存在
SELECT 
  'sp_claim_coupon' AS procedure_name,
  CASE WHEN COUNT(*) > 0 THEN '✓ 存在' ELSE '✗ 不存在' END AS status
FROM information_schema.routines 
WHERE routine_schema = 'business_reviews' AND routine_name = 'sp_claim_coupon';

-- 显示秒杀活动示例数据
SELECT '秒杀活动示例数据：' AS info;
SELECT * FROM seckill_activities LIMIT 5;
