# Implementation Plan

- [x] 1. 数据库类目名称修正





  - 创建SQL脚本修正categories表中的类目名称
  - 执行SQL脚本更新数据库
  - 验证修正后的数据正确性
  - _Requirements: 3.2, 3.3_

- [ ]* 1.1 编写数据验证查询
  - **Property 4: 类目名称一致性**
  - **Validates: Requirements 3.2**

- [x] 2. 后端API开发





- [x] 2.1 在CommonController中添加获取类目列表接口


  - 实现GET /api/common/categories接口
  - 添加接口文档注释
  - _Requirements: 2.1, 2.3_

- [x] 2.2 在CommonService中实现getCategories方法


  - 查询status=1的类目
  - 按sort_order升序排序
  - 转换为CategoryVO对象
  - _Requirements: 2.1, 3.1_

- [ ]* 2.3 编写类目列表查询的属性测试
  - **Property 1: 类目列表完整性**
  - **Validates: Requirements 2.1, 2.3**

- [ ]* 2.4 编写类目排序的属性测试
  - **Property 2: 类目排序一致性**
  - **Validates: Requirements 3.1**

- [x] 2.5 在MerchantShopServiceImpl中添加类目ID验证


  - 在保存店铺信息时验证categoryId有效性
  - 如果categoryId无效则抛出异常
  - _Requirements: 2.4_

- [ ]* 2.6 编写类目ID验证的属性测试
  - **Property 3: 类目ID有效性验证**
  - **Validates: Requirements 2.4**

- [ ]* 2.7 编写后端单元测试
  - 测试getCategories方法返回正确数量
  - 测试类目排序逻辑
  - 测试只返回status=1的类目
  - _Requirements: 2.1, 3.1_

- [x] 3. 前端API集成




- [x] 3.1 在shop.ts中添加getCategories API方法


  - 定义CategoryVO接口
  - 实现getCategories函数
  - 添加错误处理
  - _Requirements: 2.2, 2.3_

- [x] 3.2 修改shop/list.vue组件


  - 移除硬编码的categories数组
  - 添加loadCategories方法
  - 在onMounted中调用loadCategories
  - 添加降级方案处理API失败情况
  - _Requirements: 1.1, 2.2_

- [x] 3.3 修改shop/create.vue组件（如果存在）


  - 同样移除硬编码并使用动态加载
  - 确保新增店铺时也使用统一的类目列表
  - _Requirements: 1.1, 2.2_

- [ ]* 3.4 编写前端集成测试
  - 测试API调用成功场景
  - 测试API调用失败降级场景
  - 测试类目选择和保存流程
  - _Requirements: 1.1, 1.2, 1.3_

- [x] 4. 数据同步验证






- [x] 4.1 验证商家运营中心类目显示

  - 登录商家运营中心
  - 检查类目下拉框显示8个类目
  - 验证类目名称与UniApp一致
  - _Requirements: 1.1, 3.2_


- [x] 4.2 验证类目保存和读取

  - 修改店铺类目并保存
  - 刷新页面验证类目正确显示
  - 检查数据库categoryId字段正确更新
  - _Requirements: 1.2, 1.3_


- [x] 4.3 验证UniApp端类目筛选

  - 在UniApp按类目筛选店铺
  - 验证修改后的店铺出现在正确的类目下
  - 测试所有8个类目的筛选功能
  - _Requirements: 1.4, 1.5_

- [ ]* 4.4 编写端到端测试
  - 测试完整的类目修改流程
  - 验证前后端数据一致性
  - 测试实时同步效果
  - _Requirements: 1.5, 2.5_

- [x] 5. 最终检查点





  - 确保所有测试通过
  - 验证数据一致性
  - 检查日志无异常
  - 询问用户是否有问题
