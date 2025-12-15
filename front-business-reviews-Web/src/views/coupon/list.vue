<template>
  <div class="coupon-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>优惠券管理</span>
          <el-button type="primary" @click="$router.push('/coupons/create')">
            <el-icon><Plus /></el-icon>
            新建优惠券
          </el-button>
        </div>
      </template>
      
      <!-- 搜索条件 -->
      <el-form :model="searchForm" inline>
        <el-form-item label="优惠券名称">
          <el-input v-model="searchForm.title" placeholder="请输入优惠券名称" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="searchForm.type" placeholder="请选择类型" clearable>
            <el-option label="现金券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="专属券" :value="3" />
            <el-option label="新人券" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="未开始" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已结束" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchCoupons">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 优惠券列表 -->
      <el-table :data="couponList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="优惠券名称" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="scope">
            <el-tag :type="getCouponTypeTag(scope.row.type)">
              {{ getCouponTypeName(scope.row.type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalCount" label="总数量" width="100" />
        <el-table-column prop="remainCount" label="剩余数量" width="100" />
        <el-table-column prop="startTime" label="开始时间" width="180" />
        <el-table-column prop="endTime" label="结束时间" width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getCouponStatusTag(scope.row.status)">
              {{ getCouponStatusName(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button link type="primary" @click="editCoupon(scope.row)">编辑</el-button>
            <el-button link type="success" @click="viewStatistics(scope.row)">统计</el-button>
            <el-button link type="danger" @click="deleteCoupon(scope.row)">删除</el-button>
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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getCouponList, deleteCoupon as deleteCouponApi } from '@/api/coupon'

const router = useRouter()

// 搜索表单
const searchForm = ref({
  title: '',
  type: undefined as number | undefined,
  status: undefined as number | undefined
})

// 分页
const pagination = ref({
  currentPage: 1,
  pageSize: 10,
  total: 0
})

// 优惠券列表
const couponList = ref<any[]>([])

// 加载状态
const loading = ref(false)

// 获取优惠券类型名称
const getCouponTypeName = (type: number) => {
  const types: Record<number, string> = {
    1: '现金券',
    2: '折扣券',
    3: '专属券',
    4: '新人券'
  }
  return types[type] || '未知'
}

// 获取优惠券类型标签
const getCouponTypeTag = (type: number) => {
  const tags: Record<number, string> = {
    1: 'primary',
    2: 'success',
    3: 'warning',
    4: 'danger'
  }
  return tags[type] || 'info'
}

// 获取优惠券状态名称
const getCouponStatusName = (status: number) => {
  const statuses: Record<number, string> = {
    0: '未开始',
    1: '进行中',
    2: '已结束'
  }
  return statuses[status] || '未知'
}

// 获取优惠券状态标签
const getCouponStatusTag = (status: number) => {
  const tags: Record<number, string> = {
    0: 'info',
    1: 'success',
    2: 'danger'
  }
  return tags[status] || 'info'
}

// 搜索优惠券
const searchCoupons = async () => {
  try {
    loading.value = true
    const params = {
      ...searchForm.value,
      pageNum: pagination.value.currentPage,
      pageSize: pagination.value.pageSize
    }
    
    const res = await getCouponList(params)
    couponList.value = res.list || res.records || []
    pagination.value.total = res.total || 0
  } catch (error) {
    ElMessage.error('获取优惠券列表失败')
  } finally {
    loading.value = false
  }
}

// 重置搜索
const resetSearch = () => {
  searchForm.value = {
    title: '',
    type: undefined,
    status: undefined
  }
  pagination.value.currentPage = 1
  searchCoupons()
}

// 编辑优惠券
const editCoupon = (row: any) => {
  router.push(`/coupons/edit/${row.id}`)
}

// 查看统计
const viewStatistics = (row: any) => {
  router.push(`/coupons/statistics/${row.id}`)
}

// 删除优惠券
const deleteCoupon = (row: any) => {
  ElMessageBox.confirm(
    `确定要删除优惠券 "${row.title}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteCouponApi(row.id)
      ElMessage.success('删除成功')
      searchCoupons()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  })
}

// 分页变化
const handleSizeChange = (val: number) => {
  pagination.value.pageSize = val
  searchCoupons()
}

const handleCurrentChange = (val: number) => {
  pagination.value.currentPage = val
  searchCoupons()
}

// 页面加载时获取数据
onMounted(() => {
  searchCoupons()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>