<template>
  <div class="comment-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>评论管理</span>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="评论内容">
          <el-input v-model="searchForm.content" placeholder="请输入评论内容" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="已删除" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchComments">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 评论列表 -->
      <el-table :data="commentList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="content" label="评论内容" />
        <el-table-column prop="targetType" label="评论对象" width="120">
          <template #default="scope">
            {{ scope.row.targetType === 1 ? '笔记' : '门店' }}
          </template>
        </el-table-column>
        <el-table-column prop="targetTitle" label="对象标题" width="150" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
              {{ scope.row.status === 1 ? '正常' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="评论时间" width="180" />
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="replyComment(scope.row)">回复</el-button>
            <el-button 
              v-if="scope.row.status === 1"
              link 
              type="danger" 
              @click="deleteComment(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.currentPage"
        v-model:page-size="pagination.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        style="margin-top: 20px; justify-content: flex-end;"
      />
    </el-card>
    
    <!-- 回复评论对话框 -->
    <el-dialog v-model="replyDialogVisible" title="回复评论" width="500px">
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getCommentList, replyComment as replyCommentApi, deleteComment as deleteCommentApi } from '@/api/comment'

// 搜索表单
const searchForm = ref({
  content: '',
  status: undefined as number | undefined
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
    { required: true, message: '请输入回复内容', trigger: 'blur' }
  ]
}

// 搜索评论
const searchComments = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm.value,
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    
    const res = await getCommentList(params)
    commentList.value = res.list || res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('获取评论列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    content: '',
    status: undefined
  }
  pagination.value.currentPage = 1
  searchComments()
}

// 回复评论
const replyComment = (row: any) => {
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
        searchComments()
      } catch (error) {
        ElMessage.error('回复失败')
      } finally {
        replyLoading.value = false
      }
    }
  })
}

// 删除评论
const deleteComment = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除这条评论吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCommentApi(row.id)
      ElMessage.success('删除成功')
      searchComments()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 分页变化
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  searchComments()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  searchComments()
}

// 页面加载时获取数据
onMounted(() => {
  searchComments()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>