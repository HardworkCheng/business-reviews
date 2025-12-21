# Implementation Plan

- [x] 1. 合并Controller文件




  - [x] 1.1 将CouponController从mobile模块移动到web模块的controller包


    - 复制 `backend-business-reviews-mobile/src/main/java/com/businessreviews/controller/CouponController.java` 到 `backend-business-reviews-web/src/main/java/com/businessreviews/controller/`


    - _Requirements: 2.1_




  - [x] 1.2 将MessageController从mobile模块移动到web模块的controller包


    - 复制 `backend-business-reviews-mobile/src/main/java/com/businessreviews/controller/MessageController.java` 到 `backend-business-reviews-web/src/main/java/com/businessreviews/controller/`




    - _Requirements: 2.1_
  - [ ] 1.3 将UploadController从mobile模块移动到web模块的controller包
    - 复制 `backend-business-reviews-mobile/src/main/java/com/businessreviews/controller/UploadController.java` 到 `backend-business-reviews-web/src/main/java/com/businessreviews/controller/`
    - _Requirements: 2.1_







- [x] 2. 合并pom.xml依赖配置



  - [-] 2.1 更新web模块的pom.xml添加WebSocket依赖

    - 添加 `spring-boot-starter-websocket` 依赖


    - _Requirements: 5.1_




  - [x] 2.2 更新父pom.xml移除mobile模块声明



    - 从 `<modules>` 中移除 `backend-business-reviews-mobile`
    - _Requirements: 5.2_

- [ ] 3. 合并application.yml配置文件
  - [ ] 3.1 更新web模块的application.yml
    - 将端口改为8080
    - 添加SMS短信配置
    - 合并Redis数据库配置（使用database: 0）
    - 添加JWT header和prefix配置
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [ ] 4. 清理启动类
  - [ ] 4.1 删除MerchantApplication启动类
    - 删除 `backend-business-reviews-web/src/main/java/com/businessreviews/merchant/MerchantApplication.java`
    - _Requirements: 1.3_
  - [ ] 4.2 验证BusinessReviewsApplication启动类配置
    - 确保包含 @EnableAsync、@EnableScheduling、@MapperScan 注解
    - _Requirements: 1.2, 1.4_

- [ ] 5. 清理重复的配置类
  - [ ] 5.1 删除MerchantWebConfig配置类
    - 删除 `backend-business-reviews-web/src/main/java/com/businessreviews/merchant/config/MerchantWebConfig.java`
    - WebMvcConfig已包含商家端拦截器配置
    - _Requirements: 3.2_

- [ ] 6. Checkpoint - 验证编译
  - Ensure all tests pass, ask the user if questions arise.

- [ ] 7. 删除mobile模块
  - [ ] 7.1 删除backend-business-reviews-mobile整个目录
    - 删除 `backend-business-reviews-mobile/` 目录及其所有内容
    - _Requirements: 7.1_

- [ ] 8. Final Checkpoint - 验证项目编译和运行
  - Ensure all tests pass, ask the user if questions arise.

