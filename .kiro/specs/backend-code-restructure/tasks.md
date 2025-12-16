# Implementation Plan

## 后端代码分层重构任务列表

- [x] 1. 创建项目架构文档




  - [ ] 1.1 在项目根目录创建ARCHITECTURE.md架构说明文档
    - 包含模块依赖关系图
    - 包含MVC分层结构图




    - 包含Mobile端和Web端对应关系表
    - _Requirements: 6.1, 6.2, 6.3, 6.4_

- [x] 2. 重构Service层包结构

  - [ ] 2.1 创建Service层新包结构
    - 创建 service/mobile/ 包
    - 创建 service/merchant/ 包
    - 创建 service/common/ 包
    - 创建对应的impl子包
    - _Requirements: 1.1_
  - [ ] 2.2 迁移Mobile端Service到mobile包
    - 移动 AuthService 到 service/mobile/
    - 移动 NoteService 到 service/mobile/

    - 移动 ShopService 到 service/mobile/
    - 移动 CommentService 到 service/mobile/
    - 移动 UserService 到 service/mobile/
    - 移动 MessageService 到 service/mobile/
    - 移动对应的Impl类到 service/impl/mobile/
    - 更新所有import语句
    - _Requirements: 1.2_
  - [ ] 2.3 迁移Merchant端Service到merchant包
    - 移动 MerchantAuthService 到 service/merchant/

    - 移动 MerchantNoteService 到 service/merchant/
    - 移动 MerchantShopService 到 service/merchant/
    - 移动 MerchantCommentService 到 service/merchant/
    - 移动 MerchantCouponService 到 service/merchant/
    - 移动 MerchantDashboardService 到 service/merchant/
    - 移动对应的Impl类到 service/impl/merchant/


    - 更新所有import语句
    - _Requirements: 1.3_




  - [ ] 2.4 迁移共用Service到common包
    - 移动 OssService 到 service/common/
    - 移动 UploadService 到 service/common/
    - 移动 CommonService 到 service/common/
    - 移动对应的Impl类到 service/impl/common/
    - 更新所有import语句

    - _Requirements: 1.4_
  - [ ] 2.5 创建Service模块README.md
    - 说明包结构和各包用途
    - 标注Mobile端和Web端的区别
    - _Requirements: 3.1, 3.2, 3.3_

- [ ] 3. 重构DTO层包结构
  - [ ] 3.1 创建DTO层新包结构
    - 创建 dto/common/request/ 包
    - 创建 dto/common/response/ 包
    - 创建 dto/mobile/request/ 包
    - 创建 dto/mobile/response/ 包
    - 创建 dto/merchant/request/ 包
    - 创建 dto/merchant/response/ 包
    - _Requirements: 2.1_
  - [ ] 3.2 迁移Mobile端DTO到mobile包
    - 移动 LoginByCodeRequest 到 dto/mobile/request/
    - 移动 SendCodeRequest 到 dto/mobile/request/
    - 移动 PublishNoteRequest 到 dto/mobile/request/
    - 移动 OAuthLoginRequest 到 dto/mobile/request/
    - 移动 AddCommentRequest 到 dto/mobile/request/
    - 移动 PostCommentRequest 到 dto/mobile/request/
    - 移动 PostShopReviewRequest 到 dto/mobile/request/
    - 移动 FollowRequest 到 dto/mobile/request/
    - 移动 FollowUserRequest 到 dto/mobile/request/
    - 移动 UpdateUserInfoRequest 到 dto/mobile/request/
    - 移动 ChangePasswordRequest 到 dto/mobile/request/
    - 移动 ChangePhoneRequest 到 dto/mobile/request/
    - 移动 SendMessageRequest 到 dto/mobile/request/
    - 移动 MarkNoticesReadRequest 到 dto/mobile/request/
    - 移动 LoginResponse 到 dto/mobile/response/
    - 移动 NoteDetailResponse 到 dto/mobile/response/
    - 移动 NoteItemResponse 到 dto/mobile/response/
    - 移动 NoteListItemResponse 到 dto/mobile/response/
    - 移动 UserInfoResponse 到 dto/mobile/response/
    - 移动 UserProfileResponse 到 dto/mobile/response/
    - 移动 UserItemResponse 到 dto/mobile/response/
    - 移动 ShopDetailResponse 到 dto/mobile/response/
    - 移动 ShopItemResponse 到 dto/mobile/response/
    - 移动 ShopListItemResponse 到 dto/mobile/response/
    - 移动 CommentResponse 到 dto/mobile/response/
    - 移动 MessageResponse 到 dto/mobile/response/
    - 移动 NotificationResponse 到 dto/mobile/response/

    - 移动 NoticeResponse 到 dto/mobile/response/
    - 移动 ConversationResponse 到 dto/mobile/response/
    - 移动 ConversationItemResponse 到 dto/mobile/response/
    - 移动 PrivateMessageResponse 到 dto/mobile/response/
    - 移动 ChatListItemResponse 到 dto/mobile/response/
    - 移动 ChatMessageResponse 到 dto/mobile/response/
    - 移动 CouponDetailResponse 到 dto/mobile/response/

    - 移动 CouponItemResponse 到 dto/mobile/response/
    - 移动 FavoriteItemResponse 到 dto/mobile/response/
    - 移动 HistoryItemResponse 到 dto/mobile/response/


    - 移动 CategoryResponse 到 dto/mobile/response/
    - 移动 TopicResponse 到 dto/mobile/response/



    - 移动 ShopReviewResponse 到 dto/mobile/response/
    - 移动 UnreadCountResponse 到 dto/mobile/response/
    - 更新所有import语句
    - _Requirements: 2.2_
  - [ ] 3.3 迁移Merchant端DTO到merchant包
    - 移动 MerchantLoginRequest 到 dto/merchant/request/
    - 移动 MerchantRegisterRequest 到 dto/merchant/request/
    - 移动 CreateCouponRequest 到 dto/merchant/request/
    - 移动 MerchantLoginResponse 到 dto/merchant/response/
    - 移动 MerchantUserInfoResponse 到 dto/merchant/response/

    - 更新所有import语句
    - _Requirements: 2.3_
  - [ ] 3.4 迁移共用DTO到common包
    - 移动 PageResult 到 dto/common/
    - 更新所有import语句
    - _Requirements: 2.4_
  - [ ] 3.5 创建Entity模块README.md
    - 说明Entity和DTO的包结构

    - 说明各端DTO的用途
    - _Requirements: 3.1, 3.2, 3.3_




- [x] 4. 更新Controller层引用

  - [ ] 4.1 更新Mobile端Controller的import语句
    - 更新 AuthController 的import
    - 更新 NoteController 的import




    - 更新 ShopController 的import
    - 更新 CommentController 的import

    - 更新 UserController 的import
    - 更新 MessageController 的import
    - 更新 CouponController 的import




    - 更新 CommonController 的import
    - 更新 UploadController 的import

    - _Requirements: 4.1_
  - [ ] 4.2 更新Web端Controller的import语句
    - 更新 MerchantAuthController 的import


    - 更新 MerchantNoteController 的import
    - 更新 MerchantShopController 的import
    - 更新 MerchantCommentController 的import
    - 更新 MerchantCouponController 的import
    - 更新 MerchantDashboardController 的import
    - 更新 MerchantUploadController 的import
    - _Requirements: 4.2_
  - [ ] 4.3 添加Controller类级别注释
    - 为所有Mobile端Controller添加清晰的类注释说明API用途
    - 为所有Web端Controller添加清晰的类注释说明API用途
    - _Requirements: 4.3_
  - [ ] 4.4 创建Mobile模块README.md
    - 说明Mobile端Controller的职责
    - 列出所有API端点
    - _Requirements: 3.1, 3.2, 3.3_
  - [ ] 4.5 创建Web模块README.md
    - 说明Merchant端Controller的职责
    - 列出所有API端点
    - _Requirements: 3.1, 3.2, 3.3_

- [ ] 5. 更新Mapper层和Common层
  - [ ] 5.1 创建Mapper模块README.md
    - 说明Mapper层的统一设计
    - 说明命名规范
    - _Requirements: 3.1, 5.1, 5.2, 5.3_
  - [ ] 5.2 创建Common模块README.md
    - 说明公共模块的包结构
    - 说明各工具类的用途
    - _Requirements: 3.1, 3.2_

- [ ] 6. 验证和编译测试
  - [ ] 6.1 验证所有模块编译通过
    - 运行 mvn clean compile 确保无编译错误
    - 检查所有import语句是否正确
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 2.1, 2.2, 2.3, 2.4_
  - [ ] 6.2 验证应用启动正常
    - 启动Mobile端应用验证
    - 启动Web端应用验证
    - _Requirements: 4.1, 4.2_

- [ ] 7. Checkpoint - 确保所有测试通过
  - 确保所有测试通过，如有问题请询问用户
