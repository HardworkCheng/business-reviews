<template>
  <div class="coupon-create">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑优惠券' : '新建优惠券' }}</span>
        </div>
      </template>
      
      <el-form 
        :model="form" 
        :rules="rules" 
        ref="formRef" 
        label-width="120px"
        style="max-width: 600px;"
      >
        <el-form-item label="优惠券名称" prop="title">
          <el-input v-model="form.title" placeholder="请输入优惠券名称" />
        </el-form-item>
        
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择优惠券类型" @change="handleTypeChange">
            <el-option label="现金券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="专属券" :value="3" />
            <el-option label="新人券" :value="4" />
          </el-select>
        </el-form-item>
        
        <!-- 现金券 -->
        <template v-if="form.type === 1">
          <el-form-item label="减免金额" prop="amount">
            <el-input-number 
              v-model="form.amount" 
              :min="0" 
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="满减门槛" prop="threshold">
            <el-input-number 
              v-model="form.threshold" 
              :min="0" 
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
            />
          </el-form-item>
        </template>
        
        <!-- 折扣券 -->
        <template v-else-if="form.type === 2">
          <el-form-item label="折扣比例" prop="discountRate">
            <el-slider 
              v-model="form.discountRate" 
              :min="0" 
              :max="100" 
              :step="1" 
              show-input 
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="最高减免" prop="maxDiscount">
            <el-input-number 
              v-model="form.maxDiscount" 
              :min="0" 
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
            />
          </el-form-item>
        </template>
        
        <!-- 专属券 -->
        <template v-else-if="form.type === 3">
          <el-form-item label="减免金额" prop="amount">
            <el-input-number 
              v-model="form.amount" 
              :min="0" 
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="满减门槛" prop="threshold">
            <el-input-number 
              v-model="form.threshold" 
              :min="0" 
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
            />
          </el-form-item>
          <el-form-item label="适用门店">
            <el-select 
              v-model="form.applicableShops" 
              multiple 
              placeholder="请选择适用门店" 
              style="width: 100%;"
            >
              <el-option 
                v-for="shop in shopList" 
                :key="shop.id" 
                :label="shop.name" 
                :value="shop.id" 
              />
            </el-select>
          </el-form-item>
        </template>
        
        <el-form-item label="总数量" prop="totalCount">
          <el-input-number 
            v-model="form.totalCount" 
            :min="0" 
            :step="1" 
            controls-position="right" 
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="每人限领" prop="perUserLimit">
          <el-input-number 
            v-model="form.perUserLimit" 
            :min="1" 
            :step="1" 
            controls-position="right" 
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="有效期" prop="validityPeriod">
          <el-date-picker
            v-model="form.validityPeriod"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 100%;"
          />
        </el-form-item>
        
        <el-form-item label="是否可叠加" prop="stackable">
          <el-switch v-model="form.stackable" />
        </el-form-item>
        
        <el-form-item label="优惠券描述" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入优惠券描述" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">
            {{ isEdit ? '更新' : '创建' }}
          </el-button>
          <el-button @click="$router.back()">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createCoupon, updateCoupon, getCouponDetail } from '@/api/coupon'
import { getShopList } from '@/api/shop'

// 路由
const route = useRoute()
const router = useRouter()

// 表单引用
const formRef = ref()

// 是否为编辑模式
const isEdit = ref(false)
const couponId = ref(0)

// 门店列表
const shopList = ref<any[]>([])

// 表单数据
const form = ref({
  title: '',
  type: 1,
  amount: 0,
  threshold: 0,
  discountRate: 0,
  maxDiscount: 0,
  totalCount: 0,
  perUserLimit: 1,
  validityPeriod: [] as string[],
  startTime: '',
  endTime: '',
  stackable: false,
  applicableShops: [] as number[],
  description: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择优惠券类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入减免金额', trigger: 'blur' }
  ],
  threshold: [
    { required: true, message: '请输入满减门槛', trigger: 'blur' }
  ],
  discountRate: [
    { required: true, message: '请输入折扣比例', trigger: 'blur' }
  ],
  totalCount: [
    { required: true, message: '请输入总数量', trigger: 'blur' }
  ],
  perUserLimit: [
    { required: true, message: '请输入每人限领数量', trigger: 'blur' }
  ],
  validityPeriod: [
    { required: true, message: '请选择有效期', trigger: 'change' }
  ]
}

// 加载状态
const loading = ref(false)

// 处理类型变化
const handleTypeChange = (val: number) => {
  // 重置相关字段
  form.value.amount = 0
  form.value.threshold = 0
  form.value.discountRate = 0
  form.value.maxDiscount = 0
}

// 获取门店列表
const fetchShopList = async () => {
  try {
    const res = await getShopList({ pageNum: 1, pageSize: 100 })
    shopList.value = res.list || res.records || []
  } catch (error) {
    ElMessage.error('获取门店列表失败')
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        
        // 处理时间
        if (form.value.validityPeriod.length === 2) {
          form.value.startTime = form.value.validityPeriod[0]
          form.value.endTime = form.value.validityPeriod[1]
        }
        
        if (isEdit.value) {
          // 编辑模式
          await updateCoupon(couponId.value, form.value)
          ElMessage.success('更新成功')
        } else {
          // 新增模式
          await createCoupon(form.value)
          ElMessage.success('创建成功')
        }
        
        router.push('/coupons')
      } catch (error) {
        ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
      } finally {
        loading.value = false
      }
    }
  })
}

// 获取优惠券详情（编辑模式）
const fetchCouponDetail = async (id: number) => {
  try {
    const data = await getCouponDetail(id)
    form.value = { ...data }
    // 处理时间
    if (data.startTime && data.endTime) {
      form.value.validityPeriod = [data.startTime, data.endTime]
    }
  } catch (error) {
    ElMessage.error('获取优惠券详情失败')
  }
}

// 页面加载时初始化
onMounted(() => {
  // 获取门店列表
  fetchShopList()
  
  // 检查是否为编辑模式
  if (route.params.id) {
    isEdit.value = true
    couponId.value = Number(route.params.id)
    fetchCouponDetail(couponId.value)
  }
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>