# Mapper模块 (backend-business-reviews-mapper)

## 模块职责

数据访问层，使用MyBatis-Plus提供数据库操作接口。所有Mapper接口被Mobile端和Web端共用。

## 包结构

```
com.businessreviews.mapper/
├── UserMapper.java            # 用户数据访问
├── NoteMapper.java            # 笔记数据访问
├── ShopMapper.java            # 店铺数据访问
├── CommentMapper.java         # 评论数据访问
├── CouponMapper.java          # 优惠券数据访问
├── MessageMapper.java         # 消息数据访问
├── ConversationMapper.java    # 会话数据访问
├── NotificationMapper.java    # 通知数据访问
├── CategoryMapper.java        # 分类数据访问
├── TopicMapper.java           # 话题数据访问
├── TagMapper.java             # 标签数据访问
├── MerchantMapper.java        # 商家数据访问
├── MerchantUserMapper.java    # 商家用户数据访问
├── UserFollowMapper.java      # 用户关注数据访问
├── UserNoteLikeMapper.java    # 笔记点赞数据访问
├── UserNoteBookmarkMapper.java # 笔记收藏数据访问
├── UserShopFavoriteMapper.java # 店铺收藏数据访问
├── UserCouponMapper.java      # 用户优惠券数据访问
├── BrowseHistoryMapper.java   # 浏览历史数据访问
├── SearchHistoryMapper.java   # 搜索历史数据访问
└── ...                        # 其他Mapper
```

## 设计原则

### 统一设计
- 所有Mapper接口被Mobile端和Web端共用
- 避免重复的数据访问代码
- 使用MyBatis-Plus的BaseMapper提供基础CRUD操作

### 命名规范
- 基础查询方法：`selectXxx`、`insertXxx`、`updateXxx`、`deleteXxx`
- 复杂查询方法：使用描述性名称，如`selectByCondition`、`selectWithPagination`
- 特定端方法（如需要）：添加端标识，如`selectForMobile`、`selectForMerchant`

## Mapper列表

### 用户相关
| Mapper | 说明 |
|--------|------|
| UserMapper | 用户基础数据访问 |
| UserFollowMapper | 用户关注关系 |
| UserStatsMapper | 用户统计数据 |

### 笔记相关
| Mapper | 说明 |
|--------|------|
| NoteMapper | 笔记基础数据访问 |
| NoteTagMapper | 笔记标签关联 |
| NoteTopicMapper | 笔记话题关联 |
| NoteCommentMapper | 笔记评论 |
| UserNoteLikeMapper | 笔记点赞 |
| UserNoteBookmarkMapper | 笔记收藏 |

### 店铺相关
| Mapper | 说明 |
|--------|------|
| ShopMapper | 店铺基础数据访问 |
| ShopTagMapper | 店铺标签关联 |
| ShopReviewMapper | 店铺评价 |
| UserShopFavoriteMapper | 店铺收藏 |

### 商家相关
| Mapper | 说明 |
|--------|------|
| MerchantMapper | 商家基础数据访问 |
| MerchantUserMapper | 商家用户数据访问 |

### 优惠券相关
| Mapper | 说明 |
|--------|------|
| CouponMapper | 优惠券基础数据访问 |
| UserCouponMapper | 用户优惠券领取记录 |

### 消息相关
| Mapper | 说明 |
|--------|------|
| MessageMapper | 私信消息 |
| ConversationMapper | 会话管理 |
| NotificationMapper | 系统通知 |
| SystemNoticeMapper | 系统公告 |

### 其他
| Mapper | 说明 |
|--------|------|
| CategoryMapper | 分类数据 |
| TopicMapper | 话题数据 |
| TagMapper | 标签数据 |
| BrowseHistoryMapper | 浏览历史 |
| SearchHistoryMapper | 搜索历史 |
| VerificationCodeMapper | 验证码 |

## 依赖关系

```
mapper模块
    ├── 依赖 → entity模块 (实体类)
    └── 依赖 → common模块 (工具类)
```

## 注意事项

1. **共用设计**: Mapper层不区分Mobile端和Web端，所有接口共用
2. **MyBatis-Plus**: 继承BaseMapper获得基础CRUD能力
3. **XML映射**: 复杂SQL放在resources/mapper/*.xml中
4. **分页查询**: 使用MyBatis-Plus的Page对象进行分页
