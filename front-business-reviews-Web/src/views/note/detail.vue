<template>
  <!-- 弹窗式笔记详情 - 类似小红书风格 -->
  <div class="note-detail-modal" @click.self="goBack">
    <div class="modal-content" v-loading="loading">
      <!-- 关闭按钮 -->
      <button class="close-btn" @click="goBack">
        <el-icon><Close /></el-icon>
      </button>

      <!-- 左侧：图片区域 -->
      <div class="media-panel" 
           @touchstart="handleTouchStart" 
           @touchmove="handleTouchMove" 
           @touchend="handleTouchEnd"
           @mousedown="handleMouseDown"
           @mousemove="handleMouseMove"
           @mouseup="handleMouseUp"
           @mouseleave="handleMouseUp">
        <div class="image-carousel" v-if="images.length > 0">
          <div class="main-image-wrapper">
            <img :src="images[currentImageIndex]" :alt="noteData.title" @error="handleImageError" class="main-image" draggable="false" />
          </div>
          
          <!-- 图片导航按钮 -->
          <button v-if="images.length > 1 && currentImageIndex > 0" class="nav-btn prev" @click.stop="prevImage">
            <el-icon><ArrowLeft /></el-icon>
          </button>
          <button v-if="images.length > 1 && currentImageIndex < images.length - 1" class="nav-btn next" @click.stop="nextImage">
            <el-icon><ArrowRight /></el-icon>
          </button>
          
          <!-- 图片计数器/指示器 -->
          <div class="image-indicators" v-if="images.length > 1">
            <span 
              v-for="(_, index) in images" 
              :key="index" 
              class="indicator-dot"
              :class="{ active: index === currentImageIndex }"
              @click.stop="currentImageIndex = index"
            ></span>
          </div>
        </div>
        
        <div class="no-image" v-else>
          <el-icon :size="60"><Picture /></el-icon>
          <p>暂无图片</p>
        </div>
      </div>

      <!-- 右侧：内容和评论区域 -->
      <div class="content-panel">
        <!-- 作者信息 -->
        <div class="author-header">
          <div class="author-info">
            <img :src="noteData.authorAvatar || '/default-avatar.png'" class="author-avatar" />
            <div class="author-meta">
              <span class="author-name">{{ noteData.authorName || noteData.author || '商家' }}</span>
              <span class="publish-time">{{ formatDate(noteData.createdAt) }}</span>
            </div>
          </div>
          <el-tag :type="getStatusType(noteData.status)" size="small">{{ getStatusText(noteData.status) }}</el-tag>
        </div>

        <!-- 可滚动的内容区域 -->
        <div class="scrollable-content">
          <!-- 标题 -->
          <h1 class="note-title">{{ noteData.title }}</h1>

          <!-- 正文内容 -->
          <div class="note-body">
            <p>{{ noteData.content }}</p>
          </div>

          <!-- 关联店铺 -->
          <div class="shop-tag" v-if="noteData.shopName">
            <el-icon><Shop /></el-icon>
            <span>{{ noteData.shopName }}</span>
          </div>

          <!-- 位置信息 -->
          <div class="location-tag" v-if="noteData.location">
            <el-icon><Location /></el-icon>
            <span>{{ noteData.location }}</span>
          </div>

          <!-- 互动数据 -->
          <div class="stats-bar">
            <span class="stat"><el-icon><View /></el-icon>{{ noteData.viewCount || 0 }}</span>
            <span class="stat"><el-icon><Star /></el-icon>{{ noteData.likeCount || 0 }}</span>
            <span class="stat"><el-icon><ChatDotRound /></el-icon>{{ noteData.commentCount || 0 }}</span>
            <span class="stat"><el-icon><Collection /></el-icon>{{ noteData.favoriteCount || 0 }}</span>
          </div>

          <!-- 操作按钮 -->
          <div class="action-bar">
            <el-button size="small" @click="editNote"><el-icon><Edit /></el-icon>编辑</el-button>
            <el-button v-if="noteData.status !== 1" size="small" type="success" @click="publishNoteAction"><el-icon><Upload /></el-icon>发布</el-button>
            <el-button v-if="noteData.status === 1" size="small" type="warning" @click="offlineNoteAction"><el-icon><Download /></el-icon>下线</el-button>
            <el-button size="small" type="danger" @click="deleteNoteAction"><el-icon><Delete /></el-icon>删除</el-button>
          </div>

          <!-- 分割线 -->
          <div class="divider"></div>

          <!-- 评论区域 -->
          <div class="comments-section">
            <div class="comments-header">
              <h3>评论 <span class="comment-count">{{ comments.length }}</span></h3>
            </div>

            <!-- 评论列表 -->
            <div class="comments-list" v-if="comments.length > 0">
              <div v-for="comment in comments" :key="comment.id" class="comment-item">
                <img :src="comment.userAvatar || '/default-avatar.png'" class="comment-avatar" />
                <div class="comment-content">
                  <div class="comment-header">
                    <span class="comment-user">{{ comment.userName }}</span>
                    <span class="comment-time">{{ formatCommentTime(comment.createdAt) }}</span>
                  </div>
                  <p class="comment-text">{{ comment.content }}</p>
                  <div class="comment-actions">
                    <span class="action-item" @click="likeComment(comment)">
                      <el-icon><Star /></el-icon>{{ comment.likeCount || 0 }}
                    </span>
                    <span class="action-item" @click="replyComment(comment)">
                      <el-icon><ChatDotRound /></el-icon>回复
                    </span>
                  </div>
                  
                  <!-- 子评论 -->
                  <div v-if="comment.replies && comment.replies.length > 0" class="replies-list">
                    <div v-for="reply in comment.replies" :key="reply.id" class="reply-item">
                      <img :src="reply.userAvatar || '/default-avatar.png'" class="reply-avatar" />
                      <div class="reply-content">
                        <span class="reply-user">{{ reply.userName }}</span>
                        <span class="reply-text">{{ reply.content }}</span>
                        <span class="reply-time">{{ formatCommentTime(reply.createdAt) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- 空评论状态 -->
            <div v-else class="no-comments">
              <el-icon :size="40"><ChatDotRound /></el-icon>
              <p>暂无评论</p>
            </div>
          </div>
        </div>

        <!-- 底部评论输入框 -->
        <div class="comment-input-bar">
          <el-input 
            v-model="newComment" 
            :placeholder="replyTo ? `回复 @${replyTo.userName}` : '发表评论...'"
            class="comment-input"
            @keyup.enter="submitComment"
          >
            <template #prefix>
              <el-icon><Edit /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="small" @click="submitComment" :disabled="!newComment.trim()">
            发送
          </el-button>
          <el-button v-if="replyTo" size="small" @click="cancelReply">取消</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Close, ArrowLeft, ArrowRight, Picture, Shop, Location, View, Star, 
  ChatDotRound, Collection, Edit, Upload, Download, Delete
} from '@element-plus/icons-vue'
import { getNoteDetail, deleteNote, publishNote, offlineNote } from '@/api/note'
import { getNoteComments, createNoteComment } from '@/api/comment'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const currentImageIndex = ref(0)
const noteData = ref<any>({})
const comments = ref<any[]>([])
const newComment = ref('')
const replyTo = ref<any>(null)
const commentsLoading = ref(false)

// 解析图片列表 - 只显示上传的图片
const images = computed(() => {
  const imgList: string[] = []
  
  // 解析images字段
  if (noteData.value.images) {
    try {
      let parsed = noteData.value.images
      if (typeof parsed === 'string') {
        if (parsed.startsWith('[')) {
          parsed = JSON.parse(parsed)
        } else {
          parsed = parsed.split(',').filter((s: string) => s.trim())
        }
      }
      if (Array.isArray(parsed)) {
        parsed.forEach((img: string) => {
          if (img && img.trim()) {
            imgList.push(img.trim())
          }
        })
      }
    } catch {
      const splitImages = noteData.value.images.split(',').filter((s: string) => s.trim())
      imgList.push(...splitImages)
    }
  }
  
  // 如果没有images，使用coverImage
  if (imgList.length === 0 && noteData.value.coverImage) {
    imgList.push(noteData.value.coverImage)
  }
  
  return imgList
})

const getStatusType = (status: number) => {
  const types: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger' }
  return types[status] || 'info'
}

const getStatusText = (status: number) => {
  const texts: Record<number, string> = { 0: '草稿', 1: '已发布', 2: '待审核', 3: '已下线' }
  return texts[status] || '未知'
}

const formatDate = (date: string) => {
  if (!date) return ''
  try {
    const d = new Date(date)
    return d.toLocaleDateString('zh-CN', { year: 'numeric', month: 'long', day: 'numeric' })
  } catch {
    return date
  }
}

const formatCommentTime = (date: string) => {
  if (!date) return ''
  try {
    const d = new Date(date)
    const now = new Date()
    const diff = now.getTime() - d.getTime()
    const minutes = Math.floor(diff / 60000)
    const hours = Math.floor(diff / 3600000)
    const days = Math.floor(diff / 86400000)
    
    if (minutes < 1) return '刚刚'
    if (minutes < 60) return `${minutes}分钟前`
    if (hours < 24) return `${hours}小时前`
    if (days < 7) return `${days}天前`
    return d.toLocaleDateString('zh-CN')
  } catch {
    return date
  }
}

const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.src = '/default-cover.png'
}

const prevImage = () => {
  if (currentImageIndex.value > 0) currentImageIndex.value--
}

const nextImage = () => {
  if (currentImageIndex.value < images.value.length - 1) currentImageIndex.value++
}

// 滑动切换图片
const touchStartX = ref(0)
const touchEndX = ref(0)
const isDragging = ref(false)
const minSwipeDistance = 50

const handleTouchStart = (e: TouchEvent) => {
  touchStartX.value = e.touches[0].clientX
}

const handleTouchMove = (e: TouchEvent) => {
  touchEndX.value = e.touches[0].clientX
}

const handleTouchEnd = () => {
  const diff = touchStartX.value - touchEndX.value
  if (Math.abs(diff) > minSwipeDistance) {
    if (diff > 0) {
      nextImage()
    } else {
      prevImage()
    }
  }
  touchStartX.value = 0
  touchEndX.value = 0
}

// 鼠标拖动切换
const handleMouseDown = (e: MouseEvent) => {
  isDragging.value = true
  touchStartX.value = e.clientX
}

const handleMouseMove = (e: MouseEvent) => {
  if (isDragging.value) {
    touchEndX.value = e.clientX
  }
}

const handleMouseUp = () => {
  if (isDragging.value) {
    const diff = touchStartX.value - touchEndX.value
    if (Math.abs(diff) > minSwipeDistance) {
      if (diff > 0) {
        nextImage()
      } else {
        prevImage()
      }
    }
  }
  isDragging.value = false
  touchStartX.value = 0
  touchEndX.value = 0
}

const goBack = () => {
  router.push('/notes')
}

const editNote = () => {
  router.push(`/notes/edit/${noteData.value.id}`)
}

const publishNoteAction = async () => {
  try {
    await ElMessageBox.confirm('确定要发布这篇笔记吗？', '发布确认', { type: 'info' })
    await publishNote(noteData.value.id)
    ElMessage.success('发布成功')
    fetchNoteDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('发布失败')
  }
}

const offlineNoteAction = async () => {
  try {
    await ElMessageBox.confirm('确定要下线这篇笔记吗？', '下线确认', { type: 'warning' })
    await offlineNote(noteData.value.id)
    ElMessage.success('下线成功')
    fetchNoteDetail()
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('下线失败')
  }
}

const deleteNoteAction = async () => {
  try {
    await ElMessageBox.confirm('确定要删除这篇笔记吗？', '删除确认', { type: 'error' })
    await deleteNote(noteData.value.id)
    ElMessage.success('删除成功')
    router.push('/notes')
  } catch (e: any) {
    if (e !== 'cancel') ElMessage.error('删除失败')
  }
}

// 评论相关方法
const fetchComments = async () => {
  const id = route.params.id as string
  if (!id) return
  
  try {
    commentsLoading.value = true
    const res = await getNoteComments(parseInt(id))
    comments.value = res.list || res || []
    console.log('📝 评论列表:', comments.value)
  } catch (e: any) {
    console.error('获取评论失败:', e)
    // 使用模拟数据
    comments.value = []
  } finally {
    commentsLoading.value = false
  }
}

const submitComment = async () => {
  if (!newComment.value.trim()) return
  
  const id = route.params.id as string
  try {
    await createNoteComment(parseInt(id), {
      content: newComment.value,
      parentId: replyTo.value?.id || null
    })
    ElMessage.success('评论成功')
    newComment.value = ''
    replyTo.value = null
    fetchComments()
    // 更新评论数
    if (noteData.value.commentCount !== undefined) {
      noteData.value.commentCount++
    }
  } catch (e: any) {
    ElMessage.error('评论失败: ' + (e.message || '未知错误'))
  }
}

const likeComment = (comment: any) => {
  // TODO: 实现点赞功能
  ElMessage.info('点赞功能开发中')
}

const replyComment = (comment: any) => {
  replyTo.value = comment
}

const cancelReply = () => {
  replyTo.value = null
}

const fetchNoteDetail = async () => {
  const id = route.params.id as string
  if (!id) {
    ElMessage.error('笔记ID不存在')
    router.push('/notes')
    return
  }
  
  try {
    loading.value = true
    const res = await getNoteDetail(parseInt(id))
    noteData.value = res
    console.log('📝 笔记详情:', res)
  } catch (e: any) {
    ElMessage.error('获取笔记详情失败')
    router.push('/notes')
  } finally {
    loading.value = false
  }
}

// 键盘事件处理
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape') goBack()
  if (e.key === 'ArrowLeft') prevImage()
  if (e.key === 'ArrowRight') nextImage()
}

onMounted(() => {
  fetchNoteDetail()
  fetchComments()
  document.addEventListener('keydown', handleKeydown)
  document.body.style.overflow = 'hidden' // 禁止背景滚动
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = '' // 恢复背景滚动
})
</script>


<style scoped>
/* 弹窗遮罩层 - 使用商家运营中心的橙色主题 */
.note-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 40px;
}

/* 弹窗内容 */
.modal-content {
  display: flex;
  width: 100%;
  max-width: 1100px;
  height: 85vh;
  max-height: 750px;
  background: white;
  border-radius: 16px;
  overflow: hidden;
  position: relative;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: all 0.2s;
}

.close-btn:hover {
  background: rgba(0, 0, 0, 0.7);
  transform: scale(1.1);
}

/* 左侧图片区域 - 白色背景，无黑边 */
.media-panel {
  width: 55%;
  background: #f8f8f8;
  display: flex;
  flex-direction: column;
  position: relative;
  cursor: grab;
  user-select: none;
}

.media-panel:active {
  cursor: grabbing;
}

.image-carousel {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.main-image-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.main-image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  pointer-events: none;
}

.nav-btn {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  border: none;
  background: rgba(255, 255, 255, 0.95);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  opacity: 0;
}

.media-panel:hover .nav-btn {
  opacity: 1;
}

.nav-btn.prev { left: 12px; }
.nav-btn.next { right: 12px; }

.nav-btn:hover {
  background: white;
  transform: translateY(-50%) scale(1.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

/* 图片指示器小圆点 */
.image-indicators {
  position: absolute;
  bottom: 16px;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  gap: 6px;
}

.indicator-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: all 0.2s;
}

.indicator-dot.active {
  background: #FF7D00;
  width: 20px;
  border-radius: 4px;
}

.indicator-dot:hover:not(.active) {
  background: rgba(0, 0, 0, 0.4);
}

.no-image {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  background: #f8f8f8;
}

/* 右侧内容区域 */
.content-panel {
  width: 45%;
  display: flex;
  flex-direction: column;
  background: white;
}

/* 作者信息头部 */
.author-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  object-fit: cover;
}

.author-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-size: 15px;
  font-weight: 600;
  color: #171717;
}

.publish-time {
  font-size: 12px;
  color: #999;
}

/* 可滚动内容区域 - 关键：只有这部分滚动 */
.scrollable-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.scrollable-content::-webkit-scrollbar {
  width: 6px;
}

.scrollable-content::-webkit-scrollbar-track {
  background: #f5f5f5;
}

.scrollable-content::-webkit-scrollbar-thumb {
  background: #ddd;
  border-radius: 3px;
}

.scrollable-content::-webkit-scrollbar-thumb:hover {
  background: #ccc;
}

.note-title {
  font-size: 20px;
  font-weight: 700;
  color: #171717;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.note-body {
  font-size: 15px;
  color: #333;
  line-height: 1.8;
  margin-bottom: 16px;
}

.note-body p {
  margin: 0;
  white-space: pre-wrap;
}

.shop-tag, .location-tag {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  padding: 8px 14px;
  border-radius: 8px;
  margin-right: 8px;
  margin-bottom: 12px;
}

.shop-tag {
  background: #FFF7ED;
  color: #FF7D00;
}

.location-tag {
  background: #f5f5f5;
  color: #666;
}

.stats-bar {
  display: flex;
  gap: 24px;
  padding: 16px 0;
  border-top: 1px solid #f5f5f5;
  margin-bottom: 12px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: #666;
}

.stat .el-icon {
  font-size: 18px;
  color: #999;
}

.action-bar {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.action-bar .el-button {
  border-radius: 8px;
}

.divider {
  height: 1px;
  background: #f0f0f0;
  margin: 16px 0;
}

/* 评论区域 */
.comments-section {
  margin-top: 8px;
}

.comments-header {
  margin-bottom: 16px;
}

.comments-header h3 {
  font-size: 16px;
  font-weight: 600;
  color: #171717;
  margin: 0;
}

.comment-count {
  color: #999;
  font-weight: normal;
  margin-left: 8px;
}

.comments-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}

.comment-content {
  flex: 1;
  min-width: 0;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-user {
  font-size: 14px;
  font-weight: 600;
  color: #171717;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0 0 8px 0;
}

.comment-actions {
  display: flex;
  gap: 16px;
}

.action-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #999;
  cursor: pointer;
  transition: color 0.2s;
}

.action-item:hover {
  color: #FF7D00;
}

.action-item .el-icon {
  font-size: 14px;
}

/* 子评论 */
.replies-list {
  margin-top: 12px;
  padding-left: 12px;
  border-left: 2px solid #f0f0f0;
}

.reply-item {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;
}

.reply-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  object-fit: cover;
}

.reply-content {
  font-size: 13px;
  color: #666;
}

.reply-user {
  color: #FF7D00;
  font-weight: 500;
  margin-right: 6px;
}

.reply-time {
  color: #999;
  font-size: 12px;
  margin-left: 8px;
}

.no-comments {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px 0;
  color: #999;
}

.no-comments p {
  margin-top: 12px;
  font-size: 14px;
}

/* 底部评论输入框 */
.comment-input-bar {
  display: flex;
  gap: 8px;
  padding: 12px 20px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.comment-input {
  flex: 1;
}

.comment-input :deep(.el-input__wrapper) {
  border-radius: 20px;
  background: white;
}

/* 响应式 */
@media (max-width: 900px) {
  .note-detail-modal {
    padding: 0;
  }
  
  .modal-content {
    flex-direction: column;
    max-width: 100%;
    height: 100vh;
    max-height: 100vh;
    border-radius: 0;
  }
  
  .media-panel {
    width: 100%;
    height: 45%;
  }
  
  .content-panel {
    width: 100%;
    height: 55%;
  }
}
</style>
