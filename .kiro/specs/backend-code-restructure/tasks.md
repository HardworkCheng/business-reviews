# Implementation Plan

## 阶段一：Entity 迁移为 DO

- [x] 1. 迁移核心实体类到 model.dataobject








  - [x] 1.1 迁移 User.java 为 UserDO.java


    - 将 entity/User.java 移动到 model/dataobject/UserDO.java
    - 重命名类名为 UserDO
    - 检查并修正 Boolean 字段命名
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.3, 1.5_

  - [x] 1.2 迁移 Shop.java 为 ShopDO.java


    - 将 entity/Shop.java 移动到 model/dataobject/ShopDO.java
    - 重命名类名为 ShopDO
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 1.3 迁移 Note.java 为 NoteDO.java


    - 将 entity/Note.java 移动到 model/dataobject/NoteDO.java
    - 重命名类名为 NoteDO
    - 修正 isRecommend -> recommend，添加 @JsonProperty
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.3, 1.4, 1.5_

  - [x] 1.4 迁移 Comment.java 为 CommentDO.java


    - 将 entity/Comment.java 移动到 model/dataobject/CommentDO.java
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 1.5 迁移 Coupon.java 为 CouponDO.java


    - 将 entity/Coupon.java 移动到 model/dataobject/CouponDO.java
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 1.6 迁移 Merchant.java 为 MerchantDO.java


    - 将 entity/Merchant.java 移动到 model/dataobject/MerchantDO.java
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 1.7 迁移 MerchantUser.java 为 MerchantUserDO.java


    - 将 entity/MerchantUser.java 移动到 model/dataobject/MerchantUserDO.java
    - 更新所有引用
    - _Requirements: 1.1, 1.2, 1.5_

- [x] 2. 迁移其他实体类到 model.dataobject

  - [x] 2.1 迁移消息相关实体
    - ChatMessage -> ChatMessageDO
    - ChatSession -> ChatSessionDO
    - Conversation -> ConversationDO
    - Message -> MessageDO
    - PrivateMessage -> PrivateMessageDO
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 2.2 迁移用户行为相关实体
    - BrowseHistory -> BrowseHistoryDO
    - UserBrowseHistory -> UserBrowseHistoryDO
    - UserFavorite -> UserFavoriteDO
    - UserFollow -> UserFollowDO
    - UserShopFavorite -> UserShopFavoriteDO
    - SearchHistory -> SearchHistoryDO
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 2.3 迁移笔记相关实体
    - NoteComment -> NoteCommentDO
    - NoteTag -> NoteTagDO
    - NoteTopic -> NoteTopicDO
    - UserNoteBookmark -> UserNoteBookmarkDO
    - UserNoteLike -> UserNoteLikeDO
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 2.4 迁移商家相关实体
    - ShopReview -> ShopReviewDO
    - ShopTag -> ShopTagDO
    - UserCoupon -> UserCouponDO
    - _Requirements: 1.1, 1.2, 1.5_

  - [x] 2.5 迁移其他实体
    - Category -> CategoryDO
    - CommentLike -> CommentLikeDO
    - UserCommentLike -> UserCommentLikeDO
    - Notification -> NotificationDO
    - SystemNotice -> SystemNoticeDO
    - Tag -> TagDO
    - Topic -> TopicDO
    - UserStats -> UserStatsDO
    - VerificationCode -> VerificationCodeDO
    - _Requirements: 1.1, 1.2, 1.5_

- [ ]* 2.6 编写属性测试验证 DO 命名规范
    - **Property 1: DO 类命名规范**
    - **Validates: Requirements 1.2**

- [x] 3. Checkpoint - 确保 Entity 迁移完成


  - Ensure all tests pass, ask the user if questions arise.

## 阶段二：Request 迁移为 DTO

- [x] 4. 迁移 dto.request 包下的请求类





  - [x] 4.1 迁移认证相关请求类

    - LoginByCodeRequest -> LoginByCodeDTO
    - LoginByPasswordRequest -> LoginByPasswordDTO
    - SendCodeRequest -> SendCodeDTO
    - OAuthLoginRequest -> OAuthLoginDTO
    - _Requirements: 2.1, 2.4, 2.5_


  - [x] 4.2 迁移用户相关请求类

    - UpdateUserInfoRequest -> UpdateUserInfoDTO
    - ChangePasswordRequest -> ChangePasswordDTO
    - ChangePhoneRequest -> ChangePhoneDTO
    - FollowRequest -> FollowDTO
    - FollowUserRequest -> FollowUserDTO
    - _Requirements: 2.1, 2.4, 2.5_


  - [x] 4.3 迁移内容相关请求类

    - PublishNoteRequest -> PublishNoteDTO
    - AddCommentRequest -> AddCommentDTO
    - PostCommentRequest -> PostCommentDTO
    - PostShopReviewRequest -> PostShopReviewDTO
    - _Requirements: 2.1, 2.4, 2.5_


  - [x] 4.4 迁移其他请求类

    - CreateCouponRequest -> CreateCouponDTO
    - MarkNoticesReadRequest -> MarkNoticesReadDTO
    - SendMessageRequest -> SendMessageDTO
    - MerchantLoginRequest -> MerchantLoginDTO
    - MerchantRegisterRequest -> MerchantRegisterDTO
    - _Requirements: 2.1, 2.4, 2.5_

- [x] 5. 迁移 dto.merchant.request 和 dto.mobile.request 包




  - [x] 5.1 迁移 merchant/request 包下的类到 model/dto/merchant

    - CreateCouponRequest -> CreateCouponDTO
    - MerchantLoginRequest -> MerchantLoginDTO
    - MerchantRegisterRequest -> MerchantRegisterDTO
    - _Requirements: 2.2, 2.4, 2.5_


  - [x] 5.2 迁移 mobile/request 包下的类到 model/dto/mobile

    - AddCommentRequest -> AddCommentDTO
    - LoginByCodeRequest -> LoginByCodeDTO
    - OAuthLoginRequest -> OAuthLoginDTO
    - PublishNoteRequest -> PublishNoteDTO
    - SendCodeRequest -> SendCodeDTO
    - _Requirements: 2.3, 2.4, 2.5_

- [ ]* 5.3 编写属性测试验证 DTO 命名规范
    - **Property 2: DTO 类命名规范**
    - **Validates: Requirements 2.4**

- [x] 6. Checkpoint - 确保 Request 迁移完成

  - Ensure all tests pass, ask the user if questions arise.

## 阶段三：Response 迁移为 VO


- [x] 7. 迁移 dto.response 包下的响应类



  - [x] 7.1 迁移用户相关响应类

    - UserInfoResponse -> UserInfoVO
    - UserItemResponse -> UserItemVO
    - UserProfileResponse -> UserProfileVO
    - LoginResponse -> LoginVO
    - _Requirements: 3.1, 3.4, 3.5_


  - [x] 7.2 迁移商家相关响应类


    - ShopDetailResponse -> ShopDetailVO
    - ShopItemResponse -> ShopItemVO
    - ShopListItemResponse -> ShopListItemVO
    - ShopReviewResponse -> ShopReviewVO
    - CategoryResponse -> CategoryVO
    - _Requirements: 3.1, 3.4, 3.5_

  - [x] 7.3 迁移笔记相关响应类


    - NoteDetailResponse -> NoteDetailVO
    - NoteItemResponse -> NoteItemVO
    - NoteListItemResponse -> NoteListItemVO
    - CommentResponse -> CommentVO
    - TopicResponse -> TopicVO
    - _Requirements: 3.1, 3.4, 3.5_


  - [x] 7.4 迁移消息相关响应类

    - ChatListItemResponse -> ChatListItemVO
    - ChatMessageResponse -> ChatMessageVO
    - ConversationItemResponse -> ConversationItemVO
    - ConversationResponse -> ConversationVO
    - MessageResponse -> MessageVO
    - PrivateMessageResponse -> PrivateMessageVO
    - _Requirements: 3.1, 3.4, 3.5_

  - [x] 7.5 迁移其他响应类


    - CouponDetailResponse -> CouponDetailVO
    - CouponItemResponse -> CouponItemVO
    - FavoriteItemResponse -> FavoriteItemVO
    - HistoryItemResponse -> HistoryItemVO
    - NoticeResponse -> NoticeVO
    - NotificationResponse -> NotificationVO
    - UnreadCountResponse -> UnreadCountVO
    - MerchantLoginResponse -> MerchantLoginVO
    - MerchantUserInfoResponse -> MerchantUserInfoVO
    - _Requirements: 3.1, 3.4, 3.5_

- [x] 8. 迁移 dto.merchant.response 和 dto.mobile.response 包



  - [x] 8.1 迁移 merchant/response 包下的类到 model/vo/merchant






    - MerchantLoginResponse -> MerchantLoginVO
    - MerchantUserInfoResponse -> MerchantUserInfoVO
    - _Requirements: 3.2, 3.4, 3.5_

  - [x] 8.2 迁移 mobile/response 包下的类到 model/vo/mobile


    - LoginResponse -> LoginVO
    - NoteItemResponse -> NoteItemVO
    - UserInfoResponse -> UserInfoVO
    - _Requirements: 3.3, 3.4, 3.5_

- [ ]* 8.3 编写属性测试验证 VO 命名规范
    - **Property 3: VO 类命名规范**
    - **Validates: Requirements 3.4**

- [x] 9. Checkpoint - 确保 Response 迁移完成


  - Ensure all tests pass, ask the user if questions arise.

## 阶段四：Boolean 字段规范化

- [x] 10. 检查并修正 Boolean 字段




  - [x] 10.1 扫描所有 DO 类的 Boolean 字段

    - 检查是否有以 is 开头的字段
    - 去除 is 前缀
    - 添加 @JsonProperty 注解
    - _Requirements: 5.1, 5.2_



  - [x] 10.2 扫描所有 DTO 类的 Boolean 字段

    - 检查是否有以 is 开头的字段
    - 去除 is 前缀
    - 添加 @JsonProperty 注解
    - _Requirements: 5.1, 5.2_


  - [x] 10.3 扫描所有 VO 类的 Boolean 字段

    - 检查是否有以 is 开头的字段
    - 去除 is 前缀
    - 添加 @JsonProperty 注解
    - _Requirements: 5.1, 5.2_

- [ ]* 10.4 编写属性测试验证 Boolean 字段命名规范
    - **Property 4: Boolean 字段命名规范**
    - **Validates: Requirements 1.3, 5.1**

## 阶段五：清理和验证

- [x] 11. 清理旧包结构




  - [x] 11.1 删除空的 entity 包



    - 确认所有类已迁移
    - 删除 entity 包
    - _Requirements: 1.1_



  - [x] 11.2 删除空的 dto.request 包


    - 确认所有类已迁移
    - 删除 dto/request 包
    - _Requirements: 2.1_



  - [x] 11.3 删除空的 dto.response 包


    - 确认所有类已迁移
    - 删除 dto/response 包
    - _Requirements: 3.1_



  - [x] 11.4 删除空的 dto.merchant 和 dto.mobile 包


    - 确认所有类已迁移
    - 删除相关包
    - _Requirements: 2.2, 2.3, 3.2, 3.3_

- [x] 12. Final Checkpoint - 确保所有测试通过





  - Ensure all tests pass, ask the user if questions arise.
  - 验证项目编译通过
  - 验证 API 接口正常工作
  - _Requirements: 4.3, 4.4, 6.1, 6.2, 6.3_
