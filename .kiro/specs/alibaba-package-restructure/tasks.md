# Implementation Plan

## 阶段一：Mapper层清理

- [x] 1. 清理Mapper模块结构






  - [x] 1.1 合并mapper.dataobject子包中的Mapper到根目录


    - 将 `CommentDOMapper.java` 等文件移动到 `mapper` 包根目录
    - 删除重复的Mapper接口，保留命名规范的版本
    - 更新所有import语句
    - _Requirements: 5.1, 5.2_

  - [x] 1.2 编写属性测试验证Mapper包结构


    - **Property 4: Mapper Package Cleanliness**
    - **Validates: Requirements 5.1, 5.2**


  - [x] 1.3 删除mapper.dataobject子包

    - 确认所有Mapper已迁移后删除空目录
    - 更新MyBatis扫描配置
    - _Requirements: 5.3_

- [x] 2. Checkpoint - 确保Mapper层编译通过





  - Ensure all tests pass, ask the user if questions arise.

## 阶段二：Controller层重构

- [x] 3. 重构Controller层包结构






  - [x] 3.1 创建web.app包并迁移用户端控制器


    - 创建 `web.app` 包
    - 移动 AuthController -> web.app.AuthController
    - 移动 UserController -> web.app.UserController
    - 移动 ShopController -> web.app.ShopController
    - 移动 NoteController -> web.app.NoteController
    - 移动 CommentController -> web.app.CommentController
    - 移动 CouponController -> web.app.CouponController
    - 移动 MessageController -> web.app.MessageController
    - 移动 CommonController -> web.app.CommonController
    - 移动 UploadController -> web.app.UploadController
    - _Requirements: 1.1, 1.3_


  - [x] 3.2 重组web.merchant包结构

    - 将 `merchant.controller` 包重命名为 `web.merchant`
    - 移动 MerchantAuthController -> web.merchant.MerchantAuthController
    - 移动其他商户端控制器到 web.merchant
    - _Requirements: 1.2_


  - [x] 3.3 编写属性测试验证Controller包组织

    - **Property 1: Controller Package Organization**
    - **Validates: Requirements 1.1, 1.2**



  - [x] 3.4 更新WebMvcConfig和组件扫描配置
    - 更新拦截器路径配置
    - 更新组件扫描路径
    - _Requirements: 1.4_


  - [x] 3.5 删除旧的controller包

    - 确认所有控制器已迁移后删除空目录
    - _Requirements: 1.3_

- [x] 4. Checkpoint - 确保Controller层编译通过





  - Ensure all tests pass, ask the user if questions arise.

## 阶段三：Service层接口重构

- [x] 5. 重构Service接口包结构






  - [x] 5.1 整理service.app包（用户端服务接口）

    - 确认 `service.mobile` 包中的接口
    - 将 `service.mobile` 重命名为 `service.app`
    - 移动根目录的用户端接口到 service.app
    - _Requirements: 2.2_


  - [x] 5.2 整理service.merchant包（商户端服务接口）

    - 确认 `service.merchant` 包中的接口完整性
    - 移动根目录的商户端接口到 service.merchant
    - _Requirements: 2.1_


  - [x] 5.3 整理service.common包（公共服务接口）

    - 确认 `service.common` 包中的接口
    - 移动 CommonService, OssService, UploadService 到 service.common
    - _Requirements: 2.3_


  - [x] 5.4 删除根目录重复的Service接口

    - 删除 `service` 包根目录下的重复接口文件
    - 保留子包中的版本
    - _Requirements: 2.4_


  - [x] 5.5 编写属性测试验证Service接口包组织


    - **Property 2: Service Interface Package Organization**
    - **Validates: Requirements 2.1, 2.2, 2.3, 2.4**

- [x] 6. Checkpoint - 确保Service接口层编译通过
  - Ensure all tests pass, ask the user if questions arise.

## 阶段四：Service层实现重构

- [x] 7. 重构Service实现包结构








  - [x] 7.1 创建service.impl.app包并迁移用户端实现


    - 创建 `service.impl.app` 包
    - 移动 AuthServiceImpl -> service.impl.app
    - 移动 UserServiceImpl -> service.impl.app
    - 移动 ShopServiceImpl -> service.impl.app
    - 移动 NoteServiceImpl -> service.impl.app
    - 移动 CommentServiceImpl -> service.impl.app
    - 移动 MessageServiceImpl -> service.impl.app
    - _Requirements: 3.2_

  - [x] 7.2 创建service.impl.merchant包并迁移商户端实现


    - 创建 `service.impl.merchant` 包
    - 移动 MerchantAuthServiceImpl -> service.impl.merchant
    - 移动 MerchantShopServiceImpl -> service.impl.merchant
    - 移动 MerchantNoteServiceImpl -> service.impl.merchant
    - 移动 MerchantCommentServiceImpl -> service.impl.merchant
    - 移动 MerchantCouponServiceImpl -> service.impl.merchant
    - 移动 MerchantDashboardServiceImpl -> service.impl.merchant
    - _Requirements: 3.1_


  - [x] 7.3 创建service.impl.common包并迁移公共实现

    - 创建 `service.impl.common` 包
    - 移动 CommonServiceImpl -> service.impl.common
    - 移动 OssServiceImpl -> service.impl.common
    - 移动 UploadServiceImpl -> service.impl.common
    - _Requirements: 3.3_

  - [x] 7.4 编写属性测试验证Service实现包组织


    - **Property 3: Service Implementation Package Organization**
    - **Validates: Requirements 3.1, 3.2, 3.3, 3.4**


  - [x] 7.5 删除service.impl根目录的实现类

    - 确认所有实现类已迁移后清理
    - _Requirements: 3.4_

- [x] 8. Checkpoint - 确保Service实现层编译通过





  - Ensure all tests pass, ask the user if questions arise.

## 阶段五：清理refactored包

- [x] 9. 合并refactored包内容







  - [x] 9.1 评估并合并UserServiceV2

    - 比较 UserServiceV2 与 UserService
    - 将优秀的方法合并到主 UserService
    - 更新 UserServiceImpl 实现
    - _Requirements: 4.1, 4.2_


  - [x] 9.2 评估并合并ShopServiceV2

    - 比较 ShopServiceV2 与 ShopService
    - 将优秀的方法合并到主 ShopService
    - 更新 ShopServiceImpl 实现
    - _Requirements: 4.1, 4.2_


  - [x] 9.3 评估并合并NoteServiceV2

    - 比较 NoteServiceV2 与 NoteService
    - 将优秀的方法合并到主 NoteService
    - 更新 NoteServiceImpl 实现
    - _Requirements: 4.1, 4.2_


  - [x] 9.4 删除refactored包

    - 删除 service.refactored 包及其所有内容
    - 更新所有引用
    - _Requirements: 4.3, 4.4, 4.5_


  - [x] 9.5 编写属性测试验证refactored包已删除

    - **Property 8: Refactored Package Removal**
    - **Validates: Requirements 4.3, 4.4**

- [x] 10. Checkpoint - 确保refactored包清理后编译通过





  - Ensure all tests pass, ask the user if questions arise.

## 阶段六：DTO/VO组织优化

- [x] 11. 整理DTO包结构







  - [x] 11.1 整理model.dto.app包

    - 确认 `model.dto.mobile` 包内容
    - 将 `model.dto.mobile` 重命名为 `model.dto.app`
    - 移动用户端专用DTO到 model.dto.app
    - _Requirements: 7.2_


  - [x] 11.2 整理model.dto.merchant包

    - 确认 `model.dto.merchant` 包内容
    - 移动商户端专用DTO到 model.dto.merchant
    - _Requirements: 7.1_


  - [x] 11.3 保留共享DTO在根目录


    - 识别共享的DTO（如LoginByCodeDTO, SendCodeDTO等）
    - 保留在 model.dto 根目录
    - _Requirements: 7.5_

- [x] 12. 整理VO包结构

  - [x] 12.1 整理model.vo.app包
    - 确认 `model.vo.mobile` 包内容
    - 将 `model.vo.mobile` 重命名为 `model.vo.app`
    - 移动用户端专用VO到 model.vo.app
    - _Requirements: 7.4_

  - [x] 12.2 整理model.vo.merchant包
    - 确认 `model.vo.merchant` 包内容
    - 移动商户端专用VO到 model.vo.merchant
    - _Requirements: 7.3_

  - [x] 12.3 保留共享VO在根目录
    - 识别共享的VO（如CategoryVO, LoginVO等）
    - 保留在 model.vo 根目录
    - _Requirements: 7.5_

- [x] 13. Checkpoint - 确保DTO/VO重组后编译通过
  - Ensure all tests pass, ask the user if questions arise.

## 阶段七：命名规范验证

- [x] 14. 验证并修正命名规范

  - [x] 14.1 验证DO类命名
    - 扫描 model.dataobject 包
    - 确保所有类名以 DO 结尾
    - _Requirements: 8.1_

  - [x] 14.2 验证DTO类命名
    - 扫描 model.dto 包及子包
    - 确保所有类名以 DTO 结尾
    - _Requirements: 8.2_

  - [x] 14.3 验证VO类命名
    - 扫描 model.vo 包及子包
    - 确保所有类名以 VO 结尾
    - _Requirements: 8.3_

  - [x] 14.4 验证Service接口命名
    - 扫描所有Service接口
    - 确保命名符合 XxxService 模式
    - _Requirements: 8.4_

  - [x] 14.5 验证Service实现命名
    - 扫描所有Service实现类
    - 确保命名符合 XxxServiceImpl 模式
    - _Requirements: 8.5_

  - [x] 14.6 编写属性测试验证命名规范
    - **Property 6: POJO Naming Convention**
    - **Validates: Requirements 8.1, 8.2, 8.3, 8.4, 8.5**

- [x] 15. Checkpoint - 确保命名规范验证通过
  - Ensure all tests pass, ask the user if questions arise.

## 阶段八：Manager层验证

- [x] 16. 验证Manager层使用规范

  - [x] 16.1 验证OssManager封装
    - 确认所有OSS操作通过OssManager
    - 检查OssManager只被Service层注入
    - _Requirements: 9.1, 9.2_

  - [x] 16.2 验证SmsManager封装
    - 确认所有SMS操作通过SmsManager
    - 检查SmsManager只被Service层注入
    - _Requirements: 9.1, 9.3_

  - [x] 16.3 编写属性测试验证Manager注入规范
    - **Property 7: Manager Layer Injection**
    - **Validates: Requirements 9.4**

## 阶段九：最终验证

- [x] 17. 编译和测试验证

  - [x] 17.1 运行Maven编译
    - 执行 `mvn clean compile`
    - 修复所有编译错误
    - _Requirements: 10.1_

  - [ ] 17.2 运行单元测试
    - 执行 `mvn test`
    - 确保所有测试通过
    - _Requirements: 10.2_

  - [ ] 17.3 启动应用验证Bean注入
    - 启动Spring Boot应用
    - 验证无循环依赖错误
    - _Requirements: 10.4_

- [ ] 18. Final Checkpoint - 重构完成
  - Ensure all tests pass, ask the user if questions arise.
  - 所有包结构符合阿里巴巴Java开发手册规范
  - 商户端和用户端逻辑清晰分离
   - refactored包已完全清理
