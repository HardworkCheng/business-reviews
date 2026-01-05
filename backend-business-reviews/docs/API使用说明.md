# API 使用说明

## 目录结构

```
api/
├── request.js      # 请求封装基础
├── auth.js         # 认证模块API
├── user.js         # 用户模块API
├── note.js         # 笔记模块API
├── comment.js      # 评论模块API
├── shop.js         # 商家模块API
├── message.js      # 消息模块API
├── upload.js       # 上传模块API
├── common.js       # 公共模块API（分类、话题、地图、搜索）
└── index.js        # 统一导出入口
```

---

## 快速开始

### 1. 引入API

在页面中引入API：

```javascript
import api from '@/api'
```

### 2. 调用接口

所有接口都返回Promise，可以使用`async/await`或`.then()`方式调用：

```javascript
// 方式1: async/await（推荐）
async function getData() {
  try {
    const result = await api.note.getRecommendedNotes(1, 10)
    console.log(result)
  } catch (error) {
    console.error(error)
  }
}

// 方式2: .then()
api.note.getRecommendedNotes(1, 10)
  .then(result => {
    console.log(result)
  })
  .catch(error => {
    console.error(error)
  })
```

### 3. 配置BASE_URL

在 `api/request.js` 中修改BASE_URL配置：

```javascript
const BASE_URL = {
  development: 'http://localhost:8080/api',
  production: 'https://api.business-reviews.com/api'
}
```

---

## 模块详解

## 一、认证模块 (auth)

### 1.1 发送验证码

```javascript
// 登录验证码
await api.auth.sendCode('13800138000', 1)

// 注册验证码
await api.auth.sendCode('13800138000', 2)

// 重置密码验证码
await api.auth.sendCode('13800138000', 3)
```

### 1.2 验证码登录

```javascript
const result = await api.auth.loginByCode('13800138000', '123456')

// 返回数据：
// {
//   token: "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
//   userInfo: {
//     userId: "1001",
//     username: "美食探店小王",
//     avatar: "https://example.com/avatar.jpg",
//     phone: "138****8000"
//   }
// }

// 如需获取完整手机号用于敏感操作，请调用 GET /user/phone 接口

// 保存token和用户信息
uni.setStorageSync('token', result.token)
uni.setStorageSync('userInfo', result.userInfo)
```

### 1.3 第三方登录

```javascript
// 微信登录
await api.auth.oauthLogin('wechat', 'oauth_code')

// QQ登录
await api.auth.oauthLogin('qq', 'oauth_code')

// 微博登录
await api.auth.oauthLogin('weibo', 'oauth_code')
```

### 1.4 退出登录

```javascript
await api.auth.logout()

// 清除本地存储
uni.removeStorageSync('token')
uni.removeStorageSync('userInfo')

// 跳转到登录页
uni.reLaunch({ url: '/pages/login/login' })
```

---

## 二、用户模块 (user)

### 2.1 获取当前用户信息

```javascript
const userInfo = await api.user.getUserInfo()

// 返回数据：
// {
//   userId: "1001",
//   username: "美食探店小王",
//   avatar: "https://example.com/avatar.jpg",
//   bio: "热爱美食，喜欢探店 🍜",
//   phone: "138****8000",
//   gender: 1,
//   birthday: "1995-01-01",
//   followingCount: 128,
//   followerCount: 3456,
//   likeCount: 12345,
//   favoriteCount: 567,
//   noteCount: 89
// }
```

### 2.2 更新用户信息

```javascript
await api.user.updateUserInfo({
  username: "美食探店小王",
  avatar: "https://example.com/avatar.jpg",
  bio: "热爱美食，喜欢探店",
  gender: 1,
  birthday: "1995-01-01"
})
```

### 2.3 获取我的笔记列表

```javascript
const result = await api.user.getMyNotes(1, 10)

// 返回数据：
// {
//   list: [...],
//   total: 89,
//   pageNum: 1,
//   pageSize: 10,
//   hasMore: true
// }
```

### 2.4 获取我的收藏列表

```javascript
// 获取收藏的笔记
const notes = await api.user.getMyFavorites(1, 1, 10)

// 获取收藏的商家
const shops = await api.user.getMyFavorites(2, 1, 10)
```

### 2.5 获取浏览历史

```javascript
const history = await api.user.getBrowseHistory(1, 20)
```

### 2.6 关注/取消关注用户

```javascript
// 关注用户
await api.user.followUser('1002')

// 取消关注用户
await api.user.unfollowUser('1002')
```

---

## 三、笔记模块 (note)

### 3.1 获取推荐笔记列表

```javascript
const result = await api.note.getRecommendedNotes(1, 10)

// 返回数据：
// {
//   list: [
//     {
//       id: "1001",
//       image: "https://example.com/note1.jpg",
//       title: "杭州超好吃的烤肉店！人均100+",
//       author: "美食探店小王",
//       authorAvatar: "https://example.com/avatar.jpg",
//       authorId: "2001",
//       likes: 1234,
//       views: 5678,
//       tag: "热门",
//       tagClass: "tag-hot",
//       createdAt: "2024-12-01 10:30:00"
//     }
//   ],
//   pageNum: 1,
//   pageSize: 10,
//   hasMore: true
// }
```

### 3.2 获取笔记详情

```javascript
const noteDetail = await api.note.getNoteDetail('1001')

// 返回数据：
// {
//   id: "1001",
//   image: "https://example.com/note1.jpg",
//   images: ["https://example.com/note1-1.jpg", "https://example.com/note1-2.jpg"],
//   title: "杭州超好吃的烤肉店！",
//   content: "这家店真的太好吃了，强烈推荐...",
//   author: "美食探店小王",
//   authorAvatar: "https://example.com/avatar.jpg",
//   authorId: "2001",
//   publishTime: "2小时前",
//   tags: ["美食", "探店", "杭州"],
//   likeCount: 1234,
//   commentCount: 128,
//   viewCount: 3456,
//   favoriteCount: 567,
//   isLiked: false,
//   isBookmarked: false,
//   location: "杭州·上塘路",
//   shopId: "3001",
//   shopName: "蔡馬洪涛烤肉",
//   createdAt: "2024-12-01 10:30:00"
// }
```

### 3.3 发布笔记

```javascript
const result = await api.note.publishNote({
  title: "笔记标题",
  content: "笔记内容",
  images: [
    "https://example.com/image1.jpg",
    "https://example.com/image2.jpg"
  ],
  shopId: "3001",
  location: "杭州·上塘路",
  latitude: 30.2741,
  longitude: 120.1551,
  tags: ["美食", "探店"],
  topics: ["1", "2"]
})

// 返回数据：
// { noteId: "1001" }
```

### 3.4 点赞/取消点赞笔记

```javascript
// 点赞
const result = await api.note.likeNote('1001')
// 返回: { likeCount: 1235 }

// 取消点赞
const result = await api.note.unlikeNote('1001')
// 返回: { likeCount: 1234 }
```

### 3.5 收藏/取消收藏笔记

```javascript
// 收藏
await api.note.bookmarkNote('1001')

// 取消收藏
await api.note.unbookmarkNote('1001')
```

### 3.6 删除笔记

```javascript
await api.note.deleteNote('1001')
```

---

## 四、评论模块 (comment)

### 4.1 获取笔记评论列表

```javascript
const result = await api.comment.getNoteComments('1001', 1, 20)

// 返回数据：
// {
//   list: [
//     {
//       id: "10001",
//       author: "美食爱好者",
//       authorId: "2002",
//       avatar: "https://example.com/avatar2.jpg",
//       content: "看起来真不错！",
//       time: "2024-12-01 11:00:00",
//       likes: 45,
//       liked: false,
//       replyCount: 3,
//       replies: [...]
//     }
//   ],
//   total: 128,
//   hasMore: true
// }
```

### 4.2 发表评论

```javascript
// 发表评论
const result = await api.comment.postComment('1001', {
  content: "评论内容",
  parentId: null
})

// 回复评论
const result = await api.comment.postComment('1001', {
  content: "回复内容",
  parentId: "10001"
})

// 返回数据：
// { commentId: "10001" }
```

### 4.3 点赞/取消点赞评论

```javascript
// 点赞评论
const result = await api.comment.likeComment('10001')
// 返回: { likeCount: 46 }

// 取消点赞评论
const result = await api.comment.unlikeComment('10001')
// 返回: { likeCount: 45 }
```

### 4.4 删除评论

```javascript
await api.comment.deleteComment('10001')
```

---

## 五、商家模块 (shop)

### 5.1 获取商家列表

```javascript
const result = await api.shop.getShopList({
  category: '美食',
  sortField: 'distance',  // distance/popularity/rating/price
  sortOrder: 'asc',       // asc/desc
  keyword: '烤肉',
  latitude: 30.2741,
  longitude: 120.1551,
  pageNum: 1,
  pageSize: 10
})

// 返回数据：
// {
//   list: [
//     {
//       id: "3001",
//       name: "蔡馬洪涛烤肉·老北京铜锅涮羊肉",
//       image: "https://example.com/shop1.jpg",
//       rating: 4.8,
//       reviewCount: 1234,
//       tags: ["烤肉", "火锅", "人气榜"],
//       location: "拱墅区",
//       address: "上塘路1035号",
//       distance: "1.2km",
//       popularity: 9876,
//       averagePrice: 120.00,
//       latitude: 30.2751,
//       longitude: 120.1561
//     }
//   ],
//   total: 150,
//   hasMore: true
// }
```

### 5.2 获取商家详情

```javascript
const shopDetail = await api.shop.getShopDetail('3001')

// 返回数据：
// {
//   id: "3001",
//   name: "蔡馬洪涛烤肉·老北京铜锅涮羊肉",
//   headerImage: "https://example.com/shop1-header.jpg",
//   images: [...],
//   description: "正宗老北京烤肉，精选优质食材...",
//   rating: 4.6,
//   reviewCount: 1460,
//   tasteScore: 4.9,
//   environmentScore: 4.8,
//   serviceScore: 4.7,
//   address: "上塘路1035号（中国工商银行旁）",
//   businessHours: "11:30 - 03:00",
//   phone: "0571-12345678",
//   averagePrice: 120.00,
//   tags: ["烤肉", "火锅", "人气榜", "停车方便"],
//   isFavorited: false,
//   latitude: 30.2751,
//   longitude: 120.1561,
//   distance: "1.2km"
// }
```

### 5.3 获取商家评价列表

```javascript
// 按最新排序
const reviews = await api.shop.getShopReviews('3001', 1, 10, 'latest')

// 按评分排序
const reviews = await api.shop.getShopReviews('3001', 1, 10, 'rating')
```

### 5.4 发表商家评价

```javascript
const result = await api.shop.postShopReview('3001', {
  rating: 5.0,
  tasteScore: 5.0,
  environmentScore: 4.5,
  serviceScore: 5.0,
  content: "味道非常棒！",
  images: [
    "https://example.com/review1.jpg",
    "https://example.com/review2.jpg"
  ]
})

// 返回数据：
// { reviewId: "20001" }
```

### 5.5 收藏/取消收藏商家

```javascript
// 收藏商家
await api.shop.favoriteShop('3001')

// 取消收藏商家
await api.shop.unfavoriteShop('3001')
```

---

## 六、消息模块 (message)

### 6.1 获取聊天列表

```javascript
const chatList = await api.message.getChatList()

// 返回数据：
// [
//   {
//     id: "5001",
//     name: "小红",
//     userId: "2002",
//     avatar: "https://example.com/avatar2.jpg",
//     lastMessage: "这家店真的超级好吃！",
//     time: "10:30",
//     unread: 2,
//     online: true
//   }
// ]
```

### 6.2 获取聊天消息

```javascript
const result = await api.message.getChatMessages('5001', 1, 20)

// 返回数据：
// {
//   list: [
//     {
//       id: "60001",
//       fromUserId: "2002",
//       toUserId: "2001",
//       content: "这家店真的超级好吃！",
//       messageType: 1,
//       isRead: true,
//       createdAt: "2024-12-03 10:30:00"
//     }
//   ],
//   hasMore: false
// }
```

### 6.3 发送消息

```javascript
// 发送文本消息
const result = await api.message.sendMessage('2002', {
  content: "你好！",
  messageType: 1
})

// 发送图片消息
const result = await api.message.sendMessage('2002', {
  content: "https://example.com/image.jpg",
  messageType: 2
})

// 返回数据：
// { messageId: "60001" }
```

### 6.4 获取系统通知列表

```javascript
const result = await api.message.getNotices(1, 20)

// 返回数据：
// {
//   list: [
//     {
//       id: "70001",
//       user: "美食达人",
//       userId: "2003",
//       avatar: "https://example.com/avatar3.jpg",
//       action: "点赞了你的笔记",
//       time: "5分钟前",
//       icon: "❤️",
//       type: "like",
//       image: "https://example.com/note.jpg",
//       targetId: "1001",
//       isRead: false
//     }
//   ],
//   total: 50,
//   unreadCount: 12,
//   hasMore: true
// }
```

### 6.5 标记通知为已读

```javascript
// 标记指定通知为已读
await api.message.markNoticesRead(['70001', '70002'])

// 标记全部通知为已读
await api.message.markNoticesRead([])
```

### 6.6 获取未读消息数

```javascript
const result = await api.message.getUnreadCount()

// 返回数据：
// {
//   chatUnread: 5,
//   noticeUnread: 12,
//   totalUnread: 17
// }
```

---

## 七、上传模块 (upload)

### 7.1 上传单张图片

```javascript
// 选择图片
uni.chooseImage({
  count: 1,
  success: async (res) => {
    const filePath = res.tempFilePaths[0]
    
    // 上传图片
    const result = await api.upload.uploadImage(filePath)
    console.log(result.url)  // 图片URL
  }
})
```

### 7.2 批量上传图片

```javascript
// 选择多张图片
uni.chooseImage({
  count: 9,
  success: async (res) => {
    const filePaths = res.tempFilePaths
    
    // 批量上传
    const result = await api.upload.uploadImages(filePaths)
    console.log(result.urls)  // 图片URL数组
  }
})
```

---

## 八、公共模块 (common)

### 8.1 获取分类列表

```javascript
const categories = await api.common.getCategories()

// 返回数据：
// [
//   {
//     id: 1,
//     name: "美食",
//     icon: "🍜",
//     color: "#FFD166"
//   },
//   {
//     id: 2,
//     name: "KTV",
//     icon: "🎤",
//     color: "#EF476F"
//   }
// ]
```

### 8.2 获取热门话题

```javascript
const result = await api.common.getHotTopics(1, 10)

// 返回数据：
// {
//   list: [
//     {
//       id: "8001",
//       name: "杭州美食探店",
//       description: "分享杭州地区的美食探店体验",
//       coverImage: "https://example.com/topic1.jpg",
//       noteCount: 12345,
//       viewCount: 567890,
//       isHot: true
//     }
//   ],
//   hasMore: true
// }
```

### 8.3 搜索话题

```javascript
const result = await api.common.searchTopics('美食', 1, 10)
```

### 8.4 获取附近商家

```javascript
const shops = await api.common.getNearbyShops({
  latitude: 30.2741,
  longitude: 120.1551,
  category: '美食',
  radius: 5000  // 5公里
})

// 返回数据：
// [
//   {
//     id: "3001",
//     name: "蔡馬洪涛烤肉",
//     rating: 4.6,
//     reviewCount: 1460,
//     distance: "170m",
//     image: "https://example.com/shop1.jpg",
//     latitude: 30.2751,
//     longitude: 120.1561,
//     tags: ["烤肉", "火锅"]
//   }
// ]
```

### 8.5 综合搜索

```javascript
// 搜索全部
const result = await api.common.search('烤肉', 'all', 1, 10)

// 只搜索笔记
const result = await api.common.search('烤肉', 'note', 1, 10)

// 只搜索商家
const result = await api.common.search('烤肉', 'shop', 1, 10)

// 返回数据：
// {
//   notes: {
//     list: [...],
//     total: 50
//   },
//   shops: {
//     list: [...],
//     total: 30
//   }
// }
```

### 8.6 获取搜索建议

```javascript
const result = await api.common.getSearchSuggest('烤')

// 返回数据：
// {
//   suggestions: ["烤肉", "烤鱼", "烤串"]
// }
```

### 8.7 获取热门搜索

```javascript
const result = await api.common.getHotSearch()

// 返回数据：
// {
//   hotWords: ["火锅", "烧烤", "日料", "西餐"]
// }
```

---

## 错误处理

### 自动处理的错误

1. **401 未登录或Token过期**
   - 自动清除token
   - 自动跳转到登录页
   - 显示提示："登录已过期，请重新登录"

2. **其他错误**
   - 自动显示错误提示
   - Promise reject，可在catch中处理

### 手动处理错误

```javascript
try {
  const result = await api.note.getRecommendedNotes(1, 10)
  console.log(result)
} catch (error) {
  // 这里可以处理特定的错误
  console.error('获取笔记失败:', error)
  
  // 可以根据错误码进行不同处理
  if (error.code === 40401) {
    // 资源不存在
    uni.showToast({ title: '笔记不存在', icon: 'none' })
  }
}
```

---

## 完整示例

### 登录流程

```javascript
<script setup>
import { ref } from 'vue'
import api from '@/api'

const phone = ref('')
const code = ref('')
const countdown = ref(0)

// 发送验证码
const sendCode = async () => {
  if (!phone.value) {
    uni.showToast({ title: '请输入手机号', icon: 'none' })
    return
  }
  
  try {
    await api.auth.sendCode(phone.value, 1)
    uni.showToast({ title: '验证码已发送', icon: 'success' })
    
    // 开始倒计时
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

// 登录
const login = async () => {
  if (!phone.value || !code.value) {
    uni.showToast({ title: '请输入手机号和验证码', icon: 'none' })
    return
  }
  
  try {
    const result = await api.auth.loginByCode(phone.value, code.value)
    
    // 保存token和用户信息
    uni.setStorageSync('token', result.token)
    uni.setStorageSync('userInfo', result.userInfo)
    
    uni.showToast({ title: '登录成功', icon: 'success' })
    
    // 跳转到首页
    setTimeout(() => {
      uni.switchTab({ url: '/pages/index/index' })
    }, 1500)
  } catch (error) {
    console.error('登录失败:', error)
  }
}
</script>
```

### 发布笔记流程

```javascript
<script setup>
import { ref } from 'vue'
import api from '@/api'

const title = ref('')
const content = ref('')
const images = ref([])

// 选择图片
const chooseImage = () => {
  uni.chooseImage({
    count: 9 - images.value.length,
    success: (res) => {
      images.value = [...images.value, ...res.tempFilePaths]
    }
  })
}

// 发布笔记
const publishNote = async () => {
  if (!title.value || !content.value) {
    uni.showToast({ title: '请填写标题和内容', icon: 'none' })
    return
  }
  
  try {
    uni.showLoading({ title: '发布中...' })
    
    // 1. 上传图片
    let imageUrls = []
    if (images.value.length > 0) {
      const uploadResult = await api.upload.uploadImages(images.value)
      imageUrls = uploadResult.urls
    }
    
    // 2. 发布笔记
    const result = await api.note.publishNote({
      title: title.value,
      content: content.value,
      images: imageUrls,
      tags: ["美食", "探店"],
      topics: []
    })
    
    uni.hideLoading()
    uni.showToast({ title: '发布成功', icon: 'success' })
    
    // 跳转到笔记详情页
    setTimeout(() => {
      uni.navigateTo({
        url: `/pages/note-detail/note-detail?id=${result.noteId}`
      })
    }, 1500)
  } catch (error) {
    uni.hideLoading()
    console.error('发布失败:', error)
  }
}
</script>
```

---

## 注意事项

1. **所有接口调用都需要处理异常**，建议使用 try-catch 包裹

2. **需要登录的接口会自动添加Token**，无需手动处理

3. **Token过期会自动跳转登录页**，无需手动处理401错误

4. **分页接口注意hasMore字段**，判断是否还有更多数据

5. **上传图片前建议压缩**，避免上传过大的文件

6. **BASE_URL需要根据环境配置**，在 `api/request.js` 中修改

7. **所有接口返回的都是data部分**，已经过统一封装处理

---

## API清单

### 认证模块 (4个)
- `api.auth.sendCode()` - 发送验证码
- `api.auth.loginByCode()` - 验证码登录
- `api.auth.oauthLogin()` - 第三方登录
- `api.auth.logout()` - 退出登录

### 用户模块 (7个)
- `api.user.getUserInfo()` - 获取当前用户信息
- `api.user.updateUserInfo()` - 更新用户信息
- `api.user.getMyNotes()` - 获取我的笔记列表
- `api.user.getMyFavorites()` - 获取我的收藏列表
- `api.user.getBrowseHistory()` - 获取浏览历史
- `api.user.followUser()` - 关注用户
- `api.user.unfollowUser()` - 取消关注用户

### 笔记模块 (8个)
- `api.note.getRecommendedNotes()` - 获取推荐笔记列表
- `api.note.getNoteDetail()` - 获取笔记详情
- `api.note.publishNote()` - 发布笔记
- `api.note.likeNote()` - 点赞笔记
- `api.note.unlikeNote()` - 取消点赞笔记
- `api.note.bookmarkNote()` - 收藏笔记
- `api.note.unbookmarkNote()` - 取消收藏笔记
- `api.note.deleteNote()` - 删除笔记

### 评论模块 (5个)
- `api.comment.getNoteComments()` - 获取笔记评论列表
- `api.comment.postComment()` - 发表评论
- `api.comment.likeComment()` - 点赞评论
- `api.comment.unlikeComment()` - 取消点赞评论
- `api.comment.deleteComment()` - 删除评论

### 商家模块 (6个)
- `api.shop.getShopList()` - 获取商家列表
- `api.shop.getShopDetail()` - 获取商家详情
- `api.shop.getShopReviews()` - 获取商家评价列表
- `api.shop.postShopReview()` - 发表商家评价
- `api.shop.favoriteShop()` - 收藏商家
- `api.shop.unfavoriteShop()` - 取消收藏商家

### 消息模块 (6个)
- `api.message.getChatList()` - 获取聊天列表
- `api.message.getChatMessages()` - 获取聊天消息
- `api.message.sendMessage()` - 发送消息
- `api.message.getNotices()` - 获取系统通知列表
- `api.message.markNoticesRead()` - 标记通知为已读
- `api.message.getUnreadCount()` - 获取未读消息数

### 上传模块 (2个)
- `api.upload.uploadImage()` - 上传单张图片
- `api.upload.uploadImages()` - 批量上传图片

### 公共模块 (7个)
- `api.common.getCategories()` - 获取分类列表
- `api.common.getHotTopics()` - 获取热门话题
- `api.common.searchTopics()` - 搜索话题
- `api.common.getNearbyShops()` - 获取附近商家
- `api.common.search()` - 综合搜索
- `api.common.getSearchSuggest()` - 获取搜索建议
- `api.common.getHotSearch()` - 获取热门搜索

**总计：45个API接口**

---

## 更新日志

### v1.0.0 (2024-12-03)
- 初始版本
- 完成所有模块的API封装
- 支持45个接口调用
