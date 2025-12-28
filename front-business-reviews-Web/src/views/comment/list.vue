<template>
  <div class="comment-list-page">
    <!-- 数据概览看板 -->
    <el-row :gutter="24" class="dashboard-row">
      <el-col :span="6">
        <div class="data-card">
          <div class="card-left">
            <div class="card-title">今日新增评论</div>
            <h2 class="card-number">{{ dashboardData.todayNewComments }}</h2>
            <div :class="['trend', getTrendClass(dashboardData.todayTrend)]">
              <el-icon v-if="dashboardData.todayTrend > 0"><Top /></el-icon>
              <el-icon v-else-if="dashboardData.todayTrend < 0"><Bottom /></el-icon>
              <el-icon v-else><Minus /></el-icon>
              {{ Math.abs(dashboardData.todayTrend) }}% {{ getTrendText(dashboardData.todayTrend) }}
            </div>
          </div>
          <div class="card-icon-box icon-blue">
            <el-icon><ChatLineSquare /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="data-card">
          <div class="card-left">
            <div class="card-title">店铺综合评分</div>
            <h2 class="card-number">{{ dashboardData.averageRating.toFixed(1) }}</h2>
            <div :class="['trend', getTrendClass(dashboardData.ratingTrend)]">
              <el-icon v-if="dashboardData.ratingTrend > 0"><Top /></el-icon>
              <el-icon v-else-if="dashboardData.ratingTrend < 0"><Bottom /></el-icon>
              <el-icon v-else><Minus /></el-icon>
              {{ dashboardData.ratingTrend === 0 ? '持平' : Math.abs(dashboardData.ratingTrend) + '%' }}
            </div>
          </div>
          <div class="card-icon-box icon-orange">
            <el-icon><StarFilled /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="data-card">
          <div class="card-left">
            <div class="card-title">待回复内容</div>
            <h2 class="card-number">{{ dashboardData.pendingReply }}</h2>
            <div :class="['trend', getTrendClass(dashboardData.replyTrend)]">
              <el-icon v-if="dashboardData.replyTrend > 0"><Top /></el-icon>
              <el-icon v-else-if="dashboardData.replyTrend < 0"><Bottom /></el-icon>
              <el-icon v-else><Minus /></el-icon>
              {{ Math.abs(dashboardData.replyTrend) }}% 效率提升
            </div>
          </div>
          <div class="card-icon-box icon-green">
            <el-icon><MessageBox /></el-icon>
          </div>
        </div>
      </el-col>
      
      <el-col :span="6">
        <div class="data-card">
          <div class="card-left">
            <div class="card-title">差评/投诉待处理</div>
            <h2 class="card-number" style="color: #d73948">{{ dashboardData.negativeComments }}</h2>
            <div class="trend">
              <span style="color: #999; font-weight: normal;">需优先处理</span>
            </div>
          </div>
          <div class="card-icon-box icon-red">
            <el-icon><Warning /></el-icon>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 主内容区域 -->
    <div class="main-panel">
      <div class="toolbar">
        <el-tabs v-model="activeTab" @tab-click="handleTabClick" class="custom-tabs">
          <el-tab-pane :label="`全部评论 (${tabCounts.all})`" name="all"></el-tab-pane>
          <el-tab-pane :label="`正常显示 (${tabCounts.published})`" name="published"></el-tab-pane>
          <el-tab-pane :label="`差评/投诉 (${tabCounts.negative})`" name="negative"></el-tab-pane>
          <el-tab-pane :label="`已删除 (${tabCounts.deleted})`" name="deleted"></el-tab-pane>
        </el-tabs>

        <div style="display: flex; gap: 10px;">
          <el-select 
            v-model="selectedShopId" 
            placeholder="选择门店" 
            size="default"
            style="width: 200px;"
            clearable
            @change="handleShopChange"
          >
            <el-option label="全部门店" :value="null" />
            <el-option 
              v-for="shop in shopList" 
              :key="shop.id" 
              :label="shop.name" 
              :value="shop.id" 
            />
          </el-select>
          <el-input 
            v-model="searchForm.keyword" 
            placeholder="搜索内容、用户或订单号..." 
            size="default"
            style="width: 260px;"
            @input="handleSearch"
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          <el-button type="primary" size="default" plain @click="handleExport">
            <el-icon><Download /></el-icon>
            导出数据
          </el-button>
          <el-button 
            type="success" 
            size="default" 
            @click="handleAIAnalysis"
            :disabled="!selectedShopId"
            :loading="aiAnalysisLoading"
          >
            <el-icon><MagicStick /></el-icon>
            AI智能分析
          </el-button>
        </div>
      </div>

      <el-table 
        :data="commentList" 
        v-loading="loading"
        class="custom-table"
        :header-cell-style="{borderBottom: '1px solid #f0f0f0'}"
        :cell-style="{borderBottom: '1px solid #f0f0f0'}"
      >
        <el-table-column label="用户信息" width="220">
          <template #default="scope">
            <div class="user-info">
              <img :src="scope.row.avatar" class="user-avatar" @error="handleImageError">
              <div class="user-meta-box">
                <div class="user-name">{{ scope.row.author }}</div>
                <div>
                  <el-tag v-if="scope.row.isVip" size="small" type="warning" effect="dark" class="user-tag">VIP</el-tag>
                  <el-tag v-if="scope.row.isAnonymous" size="small" type="info" effect="plain" class="user-tag">匿名</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="评论详情" min-width="400">
          <template #default="scope">
            <div class="review-main">
              <div class="review-header">
                <el-rate 
                  v-if="scope.row.rating"
                  v-model="scope.row.rating" 
                  disabled 
                  show-score 
                  text-color="#3e6ae1"
                />
                <span class="review-time">{{ formatTime(scope.row.time) }}</span>
                <span v-if="scope.row.noteTitle" class="goods-badge">笔记: {{ scope.row.noteTitle }}</span>
              </div>
              
              <div class="review-text">{{ scope.row.content }}</div>

              <div v-if="scope.row.images && scope.row.images.length" style="margin-bottom: 10px;">
                <el-image 
                  v-for="(img, idx) in scope.row.images" 
                  :key="idx"
                  style="width: 60px; height: 60px; border-radius: 4px; margin-right: 8px; object-fit: cover;"
                  :src="img" 
                  :preview-src-list="scope.row.images"
                  fit="cover"
                />
              </div>
              
              <div v-if="scope.row.reply" class="merchant-reply">
                <strong>商家回复:</strong> {{ scope.row.reply }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="状态" width="120" align="center">
          <template #default="scope">
            <div v-if="scope.row.status === 'published'" style="color: #52c41a">
              <span class="status-dot" style="background: #52c41a"></span>正常显示
            </div>
            <div v-else-if="scope.row.status === 'deleted'" style="color: #bfbfbf">
              <span class="status-dot" style="background: #bfbfbf"></span>已删除
            </div>
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-tooltip content="回复" placement="top">
              <el-button 
                class="action-btn btn-reply" 
                circle 
                size="small"
                @click="handleReply(scope.row)"
              >
                <el-icon><ChatDotRound /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip :content="scope.row.isTop ? '取消置顶' : '置顶'" placement="top">
              <el-button 
                class="action-btn btn-top" 
                circle 
                size="small"
                @click="handleTop(scope.row)"
              >
                <el-icon><Top /></el-icon>
              </el-button>
            </el-tooltip>
            <el-tooltip content="删除/屏蔽" placement="top">
              <el-button 
                v-if="scope.row.status !== 'deleted'"
                class="action-btn btn-del" 
                circle 
                size="small"
                @click="handleDelete(scope.row)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 回复评论对话框 -->
    <el-dialog 
      v-model="replyDialogVisible" 
      title="回复评论" 
      width="500px"
    >
      <el-form :model="replyForm" :rules="replyRules" ref="replyFormRef">
        <el-form-item label="回复内容" prop="content">
          <el-input 
            v-model="replyForm.content" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入回复内容" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replyLoading">确定</el-button>
      </template>
    </el-dialog>

    <!-- AI分析报告弹窗 -->
    <el-dialog 
      v-model="aiReportDialogVisible" 
      title="" 
      width="680px"
      :show-close="true"
      class="ai-report-dialog"
      :close-on-click-modal="false"
    >
      <div class="ai-report-container">
        <!-- 报告头部 -->
        <div class="report-header">
          <div class="header-icon">
            <el-icon :size="32" color="#fff"><MagicStick /></el-icon>
          </div>
          <div class="header-content">
            <h2 class="report-title">AI 智能分析周报</h2>
            <p class="report-subtitle">{{ aiReportData?.period || '本周' }} · 共分析 {{ aiReportData?.reviewCount || 0 }} 条评论</p>
          </div>
        </div>

        <!-- 情感评分 -->
        <div class="score-section">
          <div class="score-circle" :style="{ '--score-color': getScoreColor(aiReportData?.sentimentScore || 0) }">
            <div class="score-value">{{ aiReportData?.sentimentScore || 0 }}</div>
            <div class="score-label">情感评分</div>
          </div>
          <div class="score-description">
            <span class="score-text" :style="{ color: getScoreColor(aiReportData?.sentimentScore || 0) }">
              {{ getScoreDescription(aiReportData?.sentimentScore || 0) }}
            </span>
            <p class="summary-text">{{ aiReportData?.summary || '暂无摘要' }}</p>
          </div>
        </div>

        <!-- 优点分析 -->
        <div class="analysis-section pros-section">
          <div class="section-header">
            <el-icon :size="20" color="#10B981"><CircleCheckFilled /></el-icon>
            <h3>优势亮点</h3>
          </div>
          <ul class="analysis-list">
            <li v-for="(pro, index) in (aiReportData?.pros || [])" :key="index" class="pros-item">
              <span class="item-bullet pros-bullet"></span>
              {{ pro }}
            </li>
            <li v-if="!aiReportData?.pros?.length" class="empty-item">暂无优势分析</li>
          </ul>
        </div>

        <!-- 缺点分析 -->
        <div class="analysis-section cons-section">
          <div class="section-header">
            <el-icon :size="20" color="#EF4444"><WarningFilled /></el-icon>
            <h3>待改进项</h3>
          </div>
          <ul class="analysis-list">
            <li v-for="(con, index) in (aiReportData?.cons || [])" :key="index" class="cons-item">
              <span class="item-bullet cons-bullet"></span>
              {{ con }}
            </li>
            <li v-if="!aiReportData?.cons?.length" class="empty-item">暂无待改进项</li>
          </ul>
        </div>

        <!-- 建议 -->
        <div class="advice-section">
          <div class="section-header">
            <el-icon :size="20" color="#3B82F6"><Promotion /></el-icon>
            <h3>智能建议</h3>
          </div>
          <div class="advice-content">
            <p>{{ aiReportData?.advice || '暂无建议' }}</p>
          </div>
        </div>

        <!-- 报告底部 -->
        <div class="report-footer">
          <span class="generated-time">生成时间：{{ aiReportData?.generatedAt || '-' }}</span>
          <span class="powered-by">Powered by DeepSeek AI</span>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  ChatLineSquare, 
  StarFilled, 
  MessageBox, 
  Warning, 
  Top, 
  Bottom, 
  Minus,
  Search,
  Download,
  ChatDotRound,
  Delete,
  MagicStick,
  CircleCheckFilled,
  WarningFilled,
  Promotion
} from '@element-plus/icons-vue'
import { 
  getCommentDashboard,
  getCommentList, 
  replyComment as replyCommentApi, 
  deleteComment as deleteCommentApi,
  topComment as topCommentApi,
  exportComments as exportCommentsApi,
  getMerchantShops,
  getAIWeeklyReport,
  type WeeklyReportData
} from '@/api/comment'

// 门店列表
const shopList = ref<any[]>([])

// 选中的门店ID
const selectedShopId = ref<number | null>(null)

// 数据概览数据
const dashboardData = ref({
  todayNewComments: 0,
  todayTrend: 0,
  averageRating: 0,
  ratingTrend: 0,
  pendingReply: 0,
  replyTrend: 0,
  negativeComments: 0,
  todayDeletedCount: 0
})

// Tab计数
const tabCounts = ref({
  all: 0,
  published: 0,
  negative: 0,
  deleted: 0
})

// 当前激活的Tab
const activeTab = ref('all')

// 搜索表单
const searchForm = reactive({
  keyword: ''
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 评论列表
const commentList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 回复对话框
const replyDialogVisible = ref(false)
const replyForm = ref({
  commentId: 0,
  content: ''
})
const replyFormRef = ref()
const replyLoading = ref(false)

// 回复表单验证规则
const replyRules = {
  content: [
    { required: true, message: '请输入回复内容', trigger: 'blur' },
    { min: 5, max: 500, message: '回复内容长度在 5 到 500 个字符', trigger: 'blur' }
  ]
}

// AI分析相关
const aiAnalysisLoading = ref(false)
const aiReportDialogVisible = ref(false)
const aiReportData = ref<WeeklyReportData | null>(null)

// AI分析处理
const handleAIAnalysis = async () => {
  if (!selectedShopId.value) {
    ElMessage.warning('请先选择一个门店进行分析')
    return
  }
  
  try {
    aiAnalysisLoading.value = true
    ElMessage.info('正在生成AI分析报告，请稍候...')
    
    const res = await getAIWeeklyReport(selectedShopId.value)
    aiReportData.value = res
    aiReportDialogVisible.value = true
    
    ElMessage.success('AI分析报告生成成功')
  } catch (error: any) {
    console.error('AI分析失败:', error)
    ElMessage.error(error.message || 'AI分析失败，请稍后重试')
  } finally {
    aiAnalysisLoading.value = false
  }
}

// 根据评分获取颜色
const getScoreColor = (score: number): string => {
  if (score >= 8) return '#10B981'  // 优秀 - 绿色
  if (score >= 6) return '#3B82F6'  // 良好 - 蓝色
  if (score >= 4) return '#F59E0B'  // 一般 - 橙色
  return '#EF4444'                   // 较差 - 红色
}

// 根据评分获取描述
const getScoreDescription = (score: number): string => {
  if (score >= 9) return '🌟 顾客口碑极佳'
  if (score >= 8) return '😊 整体评价优秀'
  if (score >= 7) return '👍 评价较为积极'
  if (score >= 6) return '📊 评价中等偏上'
  if (score >= 5) return '📈 评价表现一般'
  if (score >= 4) return '⚠️ 存在改进空间'
  if (score >= 3) return '😕 需要重点关注'
  return '🚨 亟需改善提升'
}

// 搜索防抖定时器
let searchTimer: any = null

// 获取数据概览
const fetchDashboard = async () => {
  try {
    const params = selectedShopId.value ? { shopId: selectedShopId.value } : {}
    const res = await getCommentDashboard(params)
    dashboardData.value = res || {
      todayNewComments: 0,
      todayTrend: 0,
      averageRating: 0,
      ratingTrend: 0,
      pendingReply: 0,
      replyTrend: 0,
      negativeComments: 0,
      todayDeletedCount: 0
    }
  } catch (error) {
    console.error('获取数据概览失败:', error)
  }
}

// 获取评论列表
const fetchComments = async () => {
  try {
    loading.value = true
    
    // 将前端的status字符串映射为后端的数字
    let statusValue: number | undefined = undefined
    let isNegative: boolean | undefined = undefined
    
    if (activeTab.value === 'published') {
      statusValue = 1
    } else if (activeTab.value === 'deleted') {
      statusValue = 2
    } else if (activeTab.value === 'negative') {
      // 差评Tab：查询评分<3分的评论
      isNegative = true
    }
    // 'all' 不传任何筛选参数
    
    const params: any = {
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize,
      status: statusValue,
      keyword: searchForm.keyword || undefined,
      isNegative: isNegative
    }
    
    // 如果选择了门店，添加shopId参数
    if (selectedShopId.value) {
      params.shopId = selectedShopId.value
    }
    
    const res = await getCommentList(params)
    commentList.value = res.list || res.records || []
    pagination.value.total = res.total || 0
    
    // 更新Tab计数
    if (res.tabCounts) {
      tabCounts.value = res.tabCounts
    }
  } catch (error) {
    console.error('获取评论列表失败:', error)
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 获取门店列表
const fetchShops = async () => {
  try {
    const res = await getMerchantShops()
    shopList.value = res.list || res.records || []
  } catch (error) {
    console.error('获取门店列表失败:', error)
  }
}

// 门店切换
const handleShopChange = () => {
  pagination.value.currentPage = 1
  fetchDashboard()
  fetchComments()
}

// Tab切换
const handleTabClick = () => {
  pagination.value.currentPage = 1
  fetchComments()
}

// 搜索处理（防抖）
const handleSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    pagination.value.currentPage = 1
    fetchComments()
  }, 500)
}

// 导出数据
const handleExport = async () => {
  try {
    ElMessage.info('正在导出数据...')
    const params = {
      status: activeTab.value === 'all' ? undefined : activeTab.value,
      keyword: searchForm.keyword || undefined
    }
    
    const blob = await exportCommentsApi(params)
    
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `评论数据_${new Date().toISOString().slice(0, 10)}.xlsx`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    
    ElMessage.success('导出成功')
  } catch (error) {
    ElMessage.error('导出失败')
  }
}

// 回复评论
const handleReply = (row: any) => {
  replyForm.value.commentId = row.id
  replyForm.value.content = ''
  replyDialogVisible.value = true
}

// 提交回复
const submitReply = async () => {
  if (!replyFormRef.value) return
  
  await replyFormRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        replyLoading.value = true
        await replyCommentApi(replyForm.value.commentId, {
          content: replyForm.value.content
        })
        ElMessage.success('回复成功')
        replyDialogVisible.value = false
        fetchComments()
        fetchDashboard()
      } catch (error) {
        ElMessage.error('回复失败')
      } finally {
        replyLoading.value = false
      }
    }
  })
}

// 置顶评论
const handleTop = async (row: any) => {
  try {
    const isTop = !row.isTop
    await topCommentApi(row.id, isTop)
    ElMessage.success(isTop ? '置顶成功' : '取消置顶成功')
    fetchComments()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除评论
const handleDelete = (row: any) => {
  const todayCount = dashboardData.value.todayDeletedCount || 0
  // 如果已经达到上限，虽然后端会拦截，前端也可以直接提示（可选，这里保持后端拦截为主，但文案显示当前状态）
  
  const contentHtml = `
    <div style="text-align: center;">
      <div style="font-size: 16px; color: #171a20; font-weight: 500; margin-bottom: 16px;">
        确定要删除这条评论吗？
      </div>
      <div style="background: #f9f9f9; border-left: 3px solid #d73948; padding: 12px 16px; border-radius: 0 4px 4px 0; text-align: left;">
        <div style="font-size: 14px; color: #5c5e62; margin-bottom: 4px;">
          今日已删除 <span style="color: #d73948; font-weight: 600; font-size: 16px; font-family: DINAlternate-Bold, sans-serif;">${todayCount}</span>/2 条评论
        </div>
        <div style="font-size: 12px; color: #9aa0a6;">
          删除后无法恢复，请谨慎操作
        </div>
      </div>
    </div>
  `

  ElMessageBox.confirm(
    contentHtml,
    '删除提示',
    {
      confirmButtonText: '确认删除',
      cancelButtonText: '暂不删除',
      confirmButtonType: 'danger',
      type: 'error',
      dangerouslyUseHTMLString: true,
      center: true,
      showClose: false,
      customClass: 'merchant-delete-dialog'
    }
  ).then(async () => {
    try {
      await deleteCommentApi(row.id)
      ElMessage.success('删除成功')
      fetchComments()
      fetchDashboard()
    } catch (error) {
      // 错误已经由request拦截器处理/显示了（如果是业务异常）
    }
  })
}

// 分页变化
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  fetchComments()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  fetchComments()
}

// 获取趋势类名
const getTrendClass = (trend: number) => {
  if (trend > 0) return 'up'
  if (trend < 0) return 'down'
  return ''
}

// 获取趋势文本
const getTrendText = (trend: number) => {
  if (trend > 0) return '较昨日'
  if (trend < 0) return '较昨日'
  return '持平'
}

// 格式化时间
const formatTime = (time: any) => {
  if (!time) return ''
  // 如果是LocalDateTime对象，转换为字符串
  const timeStr = typeof time === 'string' ? time : time.toString()
  return timeStr.replace('T', ' ').substring(0, 16)
}

// 图片加载失败处理 - 使用与UniApp一致的默认头像
const handleImageError = (e: Event) => {
  const target = e.target as HTMLImageElement
  // 使用阿里云OSS上的默认头像，与UniApp保持一致
  const defaultAvatars = [
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head1.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head2.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head3.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head4.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head5.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head6.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head7.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head8.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head9.png',
    'https://cheng-9.oss-cn-beijing.aliyuncs.com/head_photo/headphoto/head10.png'
  ]
  // 随机选择一个默认头像
  const randomIndex = Math.floor(Math.random() * defaultAvatars.length)
  target.src = defaultAvatars[randomIndex]
}

// 页面加载时获取数据
onMounted(() => {
  fetchShops()
  fetchDashboard()
  fetchComments()
})
</script>

<style scoped lang="scss">
@import '@/styles/comment-management.scss';

.comment-list-page {
  padding: 40px;
  max-width: 1400px;
  margin: 0 auto;
  background-color: #f9f9f9;
  min-height: 100vh;
}

// AI分析报告弹窗样式
.ai-report-container {
  padding: 0;
  
  .report-header {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 24px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 12px;
    margin-bottom: 24px;
    
    .header-icon {
      width: 56px;
      height: 56px;
      background: rgba(255, 255, 255, 0.2);
      border-radius: 14px;
      display: flex;
      align-items: center;
      justify-content: center;
      backdrop-filter: blur(10px);
    }
    
    .header-content {
      .report-title {
        margin: 0;
        font-size: 22px;
        font-weight: 700;
        color: #fff;
        letter-spacing: 1px;
      }
      
      .report-subtitle {
        margin: 6px 0 0;
        font-size: 14px;
        color: rgba(255, 255, 255, 0.85);
      }
    }
  }
  
  .score-section {
    display: flex;
    align-items: center;
    gap: 24px;
    padding: 24px;
    background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
    border-radius: 12px;
    margin-bottom: 20px;
    
    .score-circle {
      width: 100px;
      height: 100px;
      border-radius: 50%;
      background: conic-gradient(
        var(--score-color) calc(var(--score-color) * 10%),
        #e2e8f0 0
      );
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      position: relative;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
      
      &::before {
        content: '';
        position: absolute;
        width: 80px;
        height: 80px;
        background: #fff;
        border-radius: 50%;
      }
      
      .score-value {
        position: relative;
        z-index: 1;
        font-size: 32px;
        font-weight: 700;
        color: var(--score-color);
        line-height: 1;
      }
      
      .score-label {
        position: relative;
        z-index: 1;
        font-size: 11px;
        color: #64748b;
        margin-top: 4px;
      }
    }
    
    .score-description {
      flex: 1;
      
      .score-text {
        font-size: 18px;
        font-weight: 600;
      }
      
      .summary-text {
        margin: 8px 0 0;
        font-size: 14px;
        color: #475569;
        line-height: 1.6;
      }
    }
  }
  
  .analysis-section {
    background: #fff;
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    border: 1px solid #e2e8f0;
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 14px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }
    }
    
    .analysis-list {
      list-style: none;
      margin: 0;
      padding: 0;
      
      li {
        display: flex;
        align-items: flex-start;
        gap: 10px;
        padding: 10px 0;
        font-size: 14px;
        color: #334155;
        line-height: 1.5;
        border-bottom: 1px dashed #e2e8f0;
        
        &:last-child {
          border-bottom: none;
        }
      }
      
      .item-bullet {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-top: 6px;
        flex-shrink: 0;
      }
      
      .pros-bullet {
        background: linear-gradient(135deg, #10B981 0%, #34D399 100%);
      }
      
      .cons-bullet {
        background: linear-gradient(135deg, #EF4444 0%, #F87171 100%);
      }
      
      .empty-item {
        color: #94a3b8;
        font-style: italic;
      }
    }
  }
  
  .pros-section {
    background: linear-gradient(135deg, #ecfdf5 0%, #f0fdf4 100%);
    border-color: #bbf7d0;
  }
  
  .cons-section {
    background: linear-gradient(135deg, #fef2f2 0%, #fff1f2 100%);
    border-color: #fecaca;
  }
  
  .advice-section {
    background: linear-gradient(135deg, #eff6ff 0%, #f0f9ff 100%);
    border-radius: 12px;
    padding: 20px;
    margin-bottom: 16px;
    border: 1px solid #bfdbfe;
    
    .section-header {
      display: flex;
      align-items: center;
      gap: 10px;
      margin-bottom: 14px;
      
      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 600;
        color: #1e293b;
      }
    }
    
    .advice-content {
      p {
        margin: 0;
        font-size: 14px;
        color: #334155;
        line-height: 1.7;
        padding: 16px;
        background: #fff;
        border-radius: 8px;
        border-left: 4px solid #3B82F6;
      }
    }
  }
  
  .report-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: 16px;
    border-top: 1px solid #e2e8f0;
    
    .generated-time {
      font-size: 12px;
      color: #94a3b8;
    }
    
    .powered-by {
      font-size: 12px;
      color: #94a3b8;
      font-style: italic;
    }
  }
}

// 覆盖弹窗默认样式
:deep(.ai-report-dialog) {
  .el-dialog__header {
    display: none;
  }
  
  .el-dialog__body {
    padding: 24px;
  }
}
</style>
