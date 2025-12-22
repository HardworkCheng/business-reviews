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
          <el-input v-model="form.title" placeholder="请输入优惠券名称" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="优惠券类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择优惠券类型" @change="handleTypeChange" style="width: 100%;">
            <el-option label="满减券" :value="1" />
            <el-option label="折扣券" :value="2" />
            <el-option label="代金券" :value="3" />
          </el-select>
        </el-form-item>
        
        <!-- 满减券 (type=1): 需要 amount 和 minAmount -->
        <template v-if="form.type === 1">
          <el-form-item label="优惠金额" prop="amount">
            <el-input-number 
              v-model="form.amount" 
              :min="0.01" 
              :precision="2"
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
              placeholder="请输入优惠金额"
            />
            <div class="form-tip">用户使用时可抵扣的金额</div>
          </el-form-item>
          <el-form-item label="最低消费" prop="minAmount">
            <el-input-number 
              v-model="form.minAmount" 
              :min="0" 
              :precision="2"
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
              placeholder="请输入最低消费金额"
            />
            <div class="form-tip">满多少元可使用，0表示无门槛</div>
          </el-form-item>
        </template>
        
        <!-- 折扣券 (type=2): 需要 discount 和 minAmount -->
        <template v-else-if="form.type === 2">
          <el-form-item label="折扣" prop="discount">
            <el-input-number 
              v-model="form.discount" 
              :min="0.01" 
              :max="0.99"
              :precision="2"
              :step="0.05" 
              controls-position="right" 
              style="width: 100%;"
              placeholder="请输入折扣，如0.8表示8折"
            />
            <div class="form-tip">输入0.8表示8折，0.75表示7.5折</div>
          </el-form-item>
          <el-form-item label="最低消费" prop="minAmount">
            <el-input-number 
              v-model="form.minAmount" 
              :min="0" 
              :precision="2"
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
              placeholder="请输入最低消费金额"
            />
            <div class="form-tip">满多少元可使用，0表示无门槛</div>
          </el-form-item>
        </template>
        
        <!-- 代金券 (type=3): 只需要 amount -->
        <template v-else-if="form.type === 3">
          <el-form-item label="代金金额" prop="amount">
            <el-input-number 
              v-model="form.amount" 
              :min="0.01" 
              :precision="2"
              :step="1" 
              controls-position="right" 
              style="width: 100%;"
              placeholder="请输入代金金额"
            />
            <div class="form-tip">代金券面值，无使用门槛</div>
          </el-form-item>
        </template>
        
        <el-form-item label="适用店铺" prop="shopId">
          <el-select 
            v-model="form.shopId" 
            placeholder="全部店铺可用" 
            clearable
            style="width: 100%;"
          >
            <el-option 
              v-for="shop in shopList" 
              :key="shop.id" 
              :label="shop.name" 
              :value="shop.id" 
            />
          </el-select>
          <div class="form-tip">不选择表示全部店铺可用</div>
        </el-form-item>
        
        <el-form-item label="发行总量" prop="totalCount">
          <el-input-number 
            v-model="form.totalCount" 
            :min="1" 
            :step="1" 
            controls-position="right" 
            style="width: 100%;"
            placeholder="请输入发行总量"
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
          <span class="switch-label">{{ form.stackable ? '可与其他优惠券同时使用' : '不可与其他优惠券同时使用' }}</span>
        </el-form-item>
        
        <el-form-item label="使用说明" prop="description">
          <el-input 
            v-model="form.description" 
            type="textarea" 
            :rows="3" 
            placeholder="请输入优惠券使用说明"
            maxlength="500"
            show-word-limit
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
import { ref, reactive, onMounted, computed } from 'vue'
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

// 表单数据 - 与数据库设计一致
const form = ref({
  title: '',
  type: 1,                    // 1满减券，2折扣券，3代金券
  amount: null as number | null,           // 优惠金额（满减券、代金券）
  discount: null as number | null,         // 折扣（折扣券，如0.8表示8折）
  minAmount: 0,               // 最低消费金额
  totalCount: 100,            // 发行总量
  perUserLimit: 1,            // 每人限领
  validityPeriod: [] as string[],
  startTime: '',
  endTime: '',
  stackable: false,           // 是否可叠加
  shopId: null as number | null,           // 适用店铺ID（null表示全部店铺）
  description: ''             // 使用说明
})

// 动态验证规则
const validateAmount = (rule: any, value: any, callback: any) => {
  if (form.value.type === 1 || form.value.type === 3) {
    if (!value || value <= 0) {
      callback(new Error('请输入有效的优惠金额'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateDiscount = (rule: any, value: any, callback: any) => {
  if (form.value.type === 2) {
    if (!value || value <= 0 || value >= 1) {
      callback(new Error('折扣应在0.01-0.99之间'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

const validateMinAmount = (rule: any, value: any, callback: any) => {
  if (form.value.type === 1 || form.value.type === 2) {
    if (value === null || value === undefined || value < 0) {
      callback(new Error('最低消费金额不能为负数'))
    } else {
      callback()
    }
  } else {
    callback()
  }
}

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入优惠券名称', trigger: 'blur' },
    { max: 100, message: '名称不能超过100个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择优惠券类型', trigger: 'change' }
  ],
  amount: [
    { validator: validateAmount, trigger: 'blur' }
  ],
  discount: [
    { validator: validateDiscount, trigger: 'blur' }
  ],
  minAmount: [
    { validator: validateMinAmount, trigger: 'blur' }
  ],
  totalCount: [
    { required: true, message: '请输入发行总量', trigger: 'blur' }
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
  form.value.amount = null
  form.value.discount = null
  form.value.minAmount = 0
  
  // 清除验证状态
  if (formRef.value) {
    formRef.value.clearValidate(['amount', 'discount', 'minAmount'])
  }
}

// 获取门店列表
const fetchShopList = async () => {
  try {
    const res = await getShopList({ pageNum: 1, pageSize: 100 })
    shopList.value = res.list || res.records || []
  } catch (error) {
    console.error('获取门店列表失败:', error)
  }
}

// 构建提交数据
const buildSubmitData = () => {
  const data: any = {
    title: form.value.title,
    type: form.value.type,
    totalCount: form.value.totalCount,
    perUserLimit: form.value.perUserLimit,
    startTime: form.value.startTime,
    endTime: form.value.endTime,
    stackable: form.value.stackable,
    shopId: form.value.shopId,
    description: form.value.description
  }
  
  // 根据类型设置对应字段
  if (form.value.type === 1) {
    // 满减券
    data.amount = form.value.amount
    data.minAmount = form.value.minAmount
    data.discount = null
  } else if (form.value.type === 2) {
    // 折扣券
    data.discount = form.value.discount
    data.minAmount = form.value.minAmount
    data.amount = null
  } else if (form.value.type === 3) {
    // 代金券
    data.amount = form.value.amount
    data.minAmount = 0
    data.discount = null
  }
  
  return data
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid: boolean) => {
    if (valid) {
      try {
        loading.value = true
        
        // 处理时间
        if (form.value.validityPeriod && form.value.validityPeriod.length === 2) {
          form.value.startTime = form.value.validityPeriod[0]
          form.value.endTime = form.value.validityPeriod[1]
        }
        
        const submitData = buildSubmitData()
        
        if (isEdit.value) {
          await updateCoupon(couponId.value, submitData)
          ElMessage.success('更新成功')
        } else {
          await createCoupon(submitData)
          ElMessage.success('创建成功')
        }
        
        router.push('/coupons')
      } catch (error: any) {
        ElMessage.error(error.message || (isEdit.value ? '更新失败' : '创建失败'))
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
    console.log('获取优惠券详情:', data)
    
    // 映射数据到表单
    form.value.title = data.title
    form.value.type = data.type
    form.value.amount = data.amount
    form.value.discount = data.discount
    form.value.minAmount = data.minAmount || 0
    form.value.totalCount = data.totalCount
    form.value.perUserLimit = data.perUserLimit || 1
    form.value.stackable = data.stackable === 1 || data.stackable === true
    form.value.shopId = data.shopId ? Number(data.shopId) : null
    form.value.description = data.description || ''
    
    // 处理时间 - 后端返回的格式可能是 "2025-01-01T00:00:00" 或 "2025-01-01 00:00:00"
    // 需要统一转换为 "YYYY-MM-DD HH:mm:ss" 格式
    const formatDateTime = (dateStr: string | null | undefined): string => {
      if (!dateStr) return ''
      // 将 T 替换为空格，处理 LocalDateTime.toString() 的格式
      let formatted = dateStr.replace('T', ' ')
      // 如果没有秒数，补充 :00
      if (formatted.length === 16) {
        formatted += ':00'
      }
      return formatted
    }
    
    const startTime = formatDateTime(data.startTime)
    const endTime = formatDateTime(data.endTime)
    
    console.log('处理后的时间:', { startTime, endTime })
    
    if (startTime && endTime) {
      form.value.validityPeriod = [startTime, endTime]
      form.value.startTime = startTime
      form.value.endTime = endTime
    }
  } catch (error) {
    console.error('获取优惠券详情失败:', error)
    ElMessage.error('获取优惠券详情失败')
  }
}

// 页面加载时初始化
onMounted(() => {
  fetchShopList()
  
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

.form-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.switch-label {
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
}
</style>
