<template>
  <div class="message-page">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-info">
        <h1 class="page-title">消息通知</h1>
        <p class="page-desc">管理UniApp用户的互动消息和系统通知</p>
      </div>
      <div class="header-actions">
        <el-button @click="markAllRead">全部已读</el-button>
        <el-button type="primary" @click="showPushDialog = true">
          <el-icon><Promotion /></el-icon>推送消息
        </el-button>
      </div>
    </div>

    <!-- 消息分类标签 -->
    <div class="message-tabs">
      <div class="tab-item" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
        <span>全部消息</span>
        <el-badge :value="counts.all" :hidden="counts.all === 0" />
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'comment' }" @click="activeTab = 'comment'">
        <el-icon><ChatDotRound /></el-icon>
        <span>评论</span>
        <el-badge :value="counts.comment" :hidden="counts.comment === 0" />
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'like' }" @click="activeTab = 'like'">
        <el-icon><Star /></el-icon>
        <span>点赞</span>
        <el-badge :value="counts.like" :hidden="counts.like === 0" />
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'follow' }" @click="activeTab = 'follow'">
        <el-icon><UserFilled /></el-icon>
        <span>关注</span>
        <el-badge :value="counts.follow" :hidden="counts.follow === 0" />
      </div>
      <div class="tab-item" :class="{ active: activeTab === 'system' }" @click="activeTab = 'system'">
        <el-icon><Bell /></el-icon>
        <span>系统</span>
        <el-badge :value="counts.system" :hidden="counts.system === 0" />
      </div>
    </div>

    <!-- 消息列表 -->
    <div class="message-list">
      <div v-for="msg in filteredMessages" :key="msg.id" class="message-item" :class="{ unread: !msg.read }" @click="readMessage(msg)">
        <div class="msg-icon" :class="msg.type">
          <el-icon><component :is="getIcon(msg.type)" /></el-icon>
        </div>
        <div class="msg-content">
          <div class="msg-header">
            <span class="msg-title">{{ msg.title }}</span>
            <span class="msg-time">{{ msg.time }}</span>
          </div>
          <p class="msg-text">{{ msg.content }}</p>
          <div class="msg-from" v-if="msg.fromUser">
            <el-avatar :size="24" :src="msg.fromUser.avatar">{{ msg.fromUser.name?.charAt(0) }}</el-avatar>
            <span>{{ msg.fromUser.name }}</span>
          </div>
        </div>
        <div class="msg-actions">
          <el-button link type="primary" @click.stop="replyMessage(msg)" v-if="msg.type === 'comment'">回复</el-button>
          <el-button link type="danger" @click.stop="deleteMessage(msg)">删除</el-button>
        </div>
      </div>

      <div v-if="filteredMessages.length === 0" class="empty-state">
        <el-icon :size="64" color="#FFB366"><Bell /></el-icon>
        <p>暂无消息</p>
      </div>
    </div>

    <!-- 推送消息弹窗 -->
    <el-dialog v-model="showPushDialog" title="推送消息给UniApp用户" width="500px">
      <el-form :model="pushForm" label-width="80px">
        <el-form-item label="推送对象">
          <el-radio-group v-model="pushForm.target">
            <el-radio value="all">全部用户</el-radio>
            <el-radio value="vip">VIP用户</el-radio>
            <el-radio value="active">活跃用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="消息标题">
          <el-input v-model="pushForm.title" placeholder="请输入消息标题" />
        </el-form-item>
        <el-form-item label="消息内容">
          <el-input v-model="pushForm.content" type="textarea" :rows="4" placeholder="请输入消息内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPushDialog = false">取消</el-button>
        <el-button type="primary" @click="sendPush">发送推送</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { ChatDotRound, Star, UserFilled, Bell, Promotion } from '@element-plus/icons-vue'

const activeTab = ref('all')
const showPushDialog = ref(false)
const counts = reactive({ all: 12, comment: 5, like: 4, follow: 2, system: 1 })
const pushForm = reactive({ target: 'all', title: '', content: '' })

const messages = ref([
  { id: 1, type: 'comment', title: '新评论', content: '用户"美食达人"评论了您的笔记《夏日清凉饮品推荐》', time: '10分钟前', read: false, fromUser: { name: '美食达人', avatar: '' } },
  { id: 2, type: 'like', title: '收到点赞', content: '您的笔记《周末探店记录》获得了15个新点赞', time: '30分钟前', read: false },
  { id: 3, type: 'follow', title: '新粉丝', content: '用户"旅行小王子"关注了您的店铺', time: '1小时前', read: false, fromUser: { name: '旅行小王子', avatar: '' } },
  { id: 4, type: 'comment', title: '新评论', content: '用户"吃货小分队"评论了您的笔记《招牌菜品介绍》', time: '2小时前', read: true, fromUser: { name: '吃货小分队', avatar: '' } },
  { id: 5, type: 'system', title: '系统通知', content: '您的优惠券"满100减20"将于3天后过期，请及时提醒用户使用', time: '3小时前', read: true },
  { id: 6, type: 'like', title: '收到点赞', content: '您的笔记《新品上市》获得了8个新点赞', time: '5小时前', read: true },
])

const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return messages.value
  return messages.value.filter(m => m.type === activeTab.value)
})

const getIcon = (type: string) => ({ comment: ChatDotRound, like: Star, follow: UserFilled, system: Bell }[type] || Bell)
const readMessage = (msg: any) => { msg.read = true }
const replyMessage = (msg: any) => { ElMessage.info('打开回复窗口') }
const deleteMessage = (msg: any) => { messages.value = messages.value.filter(m => m.id !== msg.id); ElMessage.success('删除成功') }
const markAllRead = () => { messages.value.forEach(m => m.read = true); ElMessage.success('已全部标记为已读') }
const sendPush = () => { showPushDialog.value = false; ElMessage.success('消息推送成功') }
</script>


<style scoped>
.message-page { max-width: 1000px; margin: 0 auto; }

.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; }
.page-title { font-size: 26px; font-weight: 700; color: #171717; margin: 0 0 6px 0; }
.page-desc { font-size: 14px; color: #737373; margin: 0; }
.header-actions { display: flex; gap: 12px; }

.message-tabs { display: flex; gap: 8px; margin-bottom: 24px; background: white; padding: 16px 20px; border-radius: 16px; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }
.tab-item { display: flex; align-items: center; gap: 8px; padding: 12px 20px; font-size: 14px; color: #525252; background: #F5F5F5; border-radius: 10px; cursor: pointer; transition: all 0.2s ease; }
.tab-item:hover { background: #FFF7ED; color: #FF7D00; }
.tab-item.active { background: linear-gradient(135deg, #FF7D00 0%, #FF9933 100%); color: white; }
.tab-item .el-icon { font-size: 16px; }
.tab-item :deep(.el-badge__content) { background: #EF4444; }
.tab-item.active :deep(.el-badge__content) { background: white; color: #FF7D00; }

.message-list { background: white; border-radius: 16px; overflow: hidden; box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05); }

.message-item { display: flex; align-items: flex-start; gap: 16px; padding: 20px 24px; border-bottom: 1px solid #F5F5F5; cursor: pointer; transition: background 0.2s ease; }
.message-item:hover { background: #FAFAFA; }
.message-item:last-child { border-bottom: none; }
.message-item.unread { background: #FFF7ED; }
.message-item.unread:hover { background: #FFEDD5; }

.msg-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 20px; flex-shrink: 0; }
.msg-icon.comment { background: rgba(59, 130, 246, 0.1); color: #3B82F6; }
.msg-icon.like { background: rgba(239, 68, 68, 0.1); color: #EF4444; }
.msg-icon.follow { background: rgba(16, 185, 129, 0.1); color: #10B981; }
.msg-icon.system { background: rgba(255, 125, 0, 0.1); color: #FF7D00; }

.msg-content { flex: 1; min-width: 0; }
.msg-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.msg-title { font-size: 15px; font-weight: 600; color: #171717; }
.msg-time { font-size: 12px; color: #A3A3A3; }
.msg-text { font-size: 14px; color: #525252; margin: 0 0 10px 0; line-height: 1.5; }
.msg-from { display: flex; align-items: center; gap: 8px; font-size: 13px; color: #737373; }

.msg-actions { display: flex; gap: 8px; flex-shrink: 0; }

.empty-state { display: flex; flex-direction: column; align-items: center; justify-content: center; padding: 80px 20px; }
.empty-state p { margin: 20px 0 0; font-size: 14px; color: #737373; }
</style>
