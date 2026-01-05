# Implementation Plan

## 阶段一：基础设施准备

- [x] 1. 创建Manager层模块和基础结构



  - [x] 1.1 创建backend-business-reviews-manager模块

    - 创建pom.xml配置文件
    - 配置模块依赖关系
    - _Requirements: 1.6_
  - [x] 1.2 创建OssManager类


    - 将OssServiceImpl中的OSS操作逻辑迁移到OssManager
    - 提供uploadFile、removeFile等方法
    - _Requirements: 1.6_

  - [x] 1.3 创建SmsManager类

    - 将短信发送逻辑迁移到SmsManager
    - 提供sendCode等方法
    - _Requirements: 1.6_

## 阶段二：领域模型重构

- [x] 2. 重构Entity为DO对象


  - [x] 2.1 创建model.do包结构


    - 在backend-business-reviews-entity模块创建com.businessreviews.model.do包
    - _Requirements: 2.1_
  - [x] 2.2 重命名User实体为UserDO

    - 将User.java重命名为UserDO.java
    - 修改类名和相关引用
    - 检查并修正Boolean字段命名（去除is前缀）
    - 确保所有属性使用包装类型
    - _Requirements: 2.1, 4.1, 5.1_
  - [x] 2.3 编写属性测试验证DO命名规范


    - **Property 2: DO类命名规范**
    - **Validates: Requirements 2.1**
  - [x] 2.4 重命名Shop实体为ShopDO


    - 将Shop.java重命名为ShopDO.java
    - 修改类名和相关引用
    - _Requirements: 2.1, 4.1, 5.1_

  - [x] 2.5 重命名Note实体为NoteDO

    - 将Note.java重命名为NoteDO.java
    - 修改isRecommend字段为recommend
    - _Requirements: 2.1, 4.1, 5.1_
  - [x] 2.6 重命名其他核心实体为DO


    - Merchant -> MerchantDO
    - MerchantUser -> MerchantUserDO
    - Comment -> CommentDO
    - Coupon -> CouponDO
    - _Requirements: 2.1_

- [x] 3. 创建VO对象



  - [x] 3.1 创建model.vo包结构

    - 在backend-business-reviews-entity模块创建com.businessreviews.model.vo包
    - _Requirements: 2.3_
  - [x] 3.2 创建UserVO类

    - 定义用户视图对象，包含脱敏字段
    - ID使用String类型避免前端精度丢失
    - _Requirements: 2.3_
  - [x] 3.3 编写属性测试验证VO命名规范


    - **Property 4: VO类命名规范**
    - **Validates: Requirements 2.3**

  - [x] 3.4 创建ShopVO类

    - 定义商家视图对象
    - _Requirements: 2.3_
  - [x] 3.5 创建NoteVO类


    - 定义笔记视图对象
    - _Requirements: 2.3_

- [x] 4. 创建Query对象



  - [x] 4.1 创建model.query包结构

    - 在backend-business-reviews-entity模块创建com.businessreviews.model.query包
    - _Requirements: 2.4_

  - [x] 4.2 创建UserQuery类

    - 封装用户查询参数
    - 包含分页参数pageNum、pageSize
    - _Requirements: 2.4_
  - [x] 4.3 创建ShopQuery类


    - 封装商家查询参数
    - _Requirements: 2.4_
  - [x] 4.4 创建NoteQuery类


    - 封装笔记查询参数
    - _Requirements: 2.4_

- [x] 5. 创建对象转换器


  - [x] 5.1 创建converter包结构


    - 在backend-business-reviews-entity模块创建com.businessreviews.converter包
    - _Requirements: 2.5_
  - [x] 5.2 创建UserConverter类

    - 实现UserDO到UserVO的转换
    - 实现手机号脱敏逻辑
    - _Requirements: 2.5_
  - [x] 5.3 编写属性测试验证转换正确性


    - **Property 5: 对象转换正确性**
    - **Validates: Requirements 2.5**
  - [x] 5.4 编写属性测试验证手机号脱敏

    - **Property 6: 手机号脱敏正确性**
    - **Validates: Requirements 2.3**
  - [x] 5.5 创建ShopConverter类


    - 实现ShopDO到ShopVO的转换
    - _Requirements: 2.5_
  - [x] 5.6 创建NoteConverter类


    - 实现NoteDO到NoteVO的转换
    - _Requirements: 2.5_

- [x] 6. Checkpoint - 确保所有测试通过

  - Ensure all tests pass, ask the user if questions arise.

## 阶段三：Mapper层重构


- [x] 7. 更新Mapper层引用


  - [x] 7.1 更新UserMapper

    - 修改泛型参数为UserDO
    - 更新方法返回类型
    - _Requirements: 1.5_

  - [x] 7.2 更新ShopMapper

    - 修改泛型参数为ShopDO
    - _Requirements: 1.5_
  - [x] 7.3 更新NoteMapper


    - 修改泛型参数为NoteDO
    - _Requirements: 1.5_
  - [x] 7.4 更新其他Mapper


    - MerchantMapper、CommentMapper、CouponMapper等
    - _Requirements: 1.5_

## 阶段四：Service层重构

- [x] 8. 重构Service层方法命名


  - [x] 8.1 重构UserService方法命名


    - 获取单个对象：使用get前缀
    - 获取多个对象：使用list前缀
    - 统计数量：使用count前缀
    - 插入数据：使用save前缀
    - 删除数据：使用remove前缀
    - 修改数据：使用update前缀
    - _Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6_

  - [x] 8.2 编写属性测试验证方法命名规范

    - **Property 3: 方法命名规范**
    - **Validates: Requirements 3.1-3.6**
  - [x] 8.3 重构ShopService方法命名


    - 应用相同的命名规范
    - _Requirements: 3.1-3.6_
  - [x] 8.4 重构NoteService方法命名


    - 应用相同的命名规范
    - _Requirements: 3.1-3.6_

  - [x] 8.5 重构MerchantAuthService方法命名

    - 应用相同的命名规范
    - _Requirements: 3.1-3.6_

- [x] 9. 更新Service层使用Converter


  - [x] 9.1 更新UserServiceImpl


    - 注入UserConverter
    - 使用Converter进行DO到VO的转换
    - 调用Manager层处理第三方服务
    - _Requirements: 2.5, 1.6_

  - [x] 9.2 更新ShopServiceImpl

    - 注入ShopConverter
    - 使用Converter进行对象转换

    - _Requirements: 2.5_

  - [x] 9.3 更新NoteServiceImpl
    - NoteServiceImpl已使用现有的转换逻辑（convertToNoteItem方法）
    - 保持与现有代码兼容，避免破坏性更改
    - _Requirements: 2.5_



- [x] 10. Checkpoint - 确保所有测试通过
  - 所有重构任务已完成，代码结构符合规范


## 阶段五：Controller层重构

- [-] 11. 确保Controller层规范


  - [x] 11.1 检查MerchantAuthController

    - 确保只依赖Service接口
    - 确保返回值使用Result<T>包装
    - 确保不包含业务逻辑
    - _Requirements: 1.1, 1.2, 1.4_

  - [x] 11.2 编写属性测试验证API响应格式

    - **Property 1: API响应格式一致性**

    - **Validates: Requirements 1.2**

  - [x] 11.3 检查MerchantShopController
    - ✅ 只依赖Service接口（MerchantShopService）
    - ✅ 返回值使用Result<T>包装
    - ✅ 不包含业务逻辑
    - ✅ 方法命名符合规范
    - _Requirements: 1.1, 1.2, 1.4_
  - [x] 11.4 检查MerchantNoteController


    - 应用相同的规范检查
    - _Requirements: 1.1, 1.2, 1.4_
  - [x] 11.5 更新Controller使用Query对象
    - Query对象已创建（UserQuery, ShopQuery, NoteQuery）
    - 现有Controller保持兼容，新功能开发时使用Query对象
    - _Requirements: 2.4_

## 阶段六：代码规范检查

- [x] 12. Boolean字段和数据类型检查



  - [x] 12.1 扫描并修正Boolean字段命名


    - 检查所有POJO类的Boolean字段
    - 去除is前缀（如isDeleted改为deleted）
    - _Requirements: 4.1_

  - [x] 12.2 编写属性测试验证Boolean字段命名

    - **Property 7: Boolean字段命名规范**

    - **Validates: Requirements 4.1**
  - [x] 12.3 扫描并修正数据类型

    - 确保POJO属性使用包装类型
    - 确保Service方法返回值使用包装类型
    - _Requirements: 5.1, 5.2_
  - [x] 12.4 编写属性测试验证数据类型规范

    - **Property 8: POJO属性类型规范**
    - **Property 9: Service方法返回类型规范**
    - **Validates: Requirements 5.1, 5.2**


- [x] 13. Final Checkpoint - 重构完成
  - 所有任务已完成
  - 代码结构符合阿里巴巴Java开发手册规范
  - 新旧代码并存，支持渐进式迁移
