<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { commonListSupply } from '../../api/supply'
import { createOrder } from '../../api/order'
import { ElMessage } from 'element-plus'

const router = useRouter()
const searchForm = reactive({
  productName: '', category: '', productionPlace: '',
  minPrice: undefined as number | undefined, maxPrice: undefined as number | undefined,
})
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(12)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await commonListSupply({
      page: currentPage.value, size: pageSize.value, ...searchForm,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => {
  Object.assign(searchForm, { productName: '', category: '', productionPlace: '', minPrice: undefined, maxPrice: undefined })
  handleSearch()
}

// ========== 购买弹窗 ==========
const buyVisible = ref(false)
const buyItem = ref<any>({})
const buyForm = reactive({ quantity: 1, deliveryAddress: '' })
const subtotal = computed(() => {
  const price = Number(buyItem.value.price) || 0
  return (price * buyForm.quantity).toFixed(2)
})

const openBuy = (item: any) => {
  buyItem.value = item
  buyForm.quantity = 1
  buyForm.deliveryAddress = ''
  buyVisible.value = true
}

const handleBuy = async () => {
  if (buyForm.quantity <= 0) { ElMessage.warning('请输入有效数量'); return }
  if (!buyForm.deliveryAddress.trim()) { ElMessage.warning('请输入收货地址'); return }
  if (buyForm.quantity > Number(buyItem.value.stock)) { ElMessage.warning('购买数量超过库存'); return }
  await createOrder({
    items: [{ supplyId: buyItem.value.supplyId, quantity: buyForm.quantity, productName: buyItem.value.productName }],
    deliveryAddress: buyForm.deliveryAddress,
  })
  ElMessage.success('下单成功！等待供应方确认')
  buyVisible.value = false
  fetchData()
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>供应大厅</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.productName" placeholder="产品名称" clearable style="width: 160px;" />
        <el-select v-model="searchForm.category" placeholder="品类" clearable style="width: 120px;">
          <el-option label="农产品" value="农产品" /><el-option label="生产物料" value="生产物料" />
        </el-select>
        <el-input v-model="searchForm.productionPlace" placeholder="产地" clearable style="width: 140px;" />
        <el-input-number v-model="searchForm.minPrice" :min="0" :precision="2" placeholder="最低价"
          controls-position="right" style="width: 120px;" />
        <span style="color: #909399;">~</span>
        <el-input-number v-model="searchForm.maxPrice" :min="0" :precision="2" placeholder="最高价"
          controls-position="right" style="width: 120px;" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <!-- 卡片列表展示 -->
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="item in tableData" :key="item.supplyId"
          style="margin-bottom: 16px;">
          <el-card shadow="hover">
            <template #header>
              <div style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 600;">{{ item.productName }}</span>
                <el-tag size="small">{{ item.category }}</el-tag>
              </div>
            </template>
            <div style="font-size: 13px; color: var(--el-text-color-regular); line-height: 2;">
              <div>产地：{{ item.productionPlace }}</div>
              <div>供应方：{{ item.supplierName }}</div>
              <div>库存：{{ item.stock }} kg</div>
              <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 4px;">
                <span style="font-size: 20px; font-weight: 700; color: #F56C6C;">¥{{ item.price }}<small
                    style="font-size: 12px; font-weight: 400;">/kg</small></span>
              </div>
              <div style="display: flex; gap: 8px; margin-top: 8px;">
                <el-button type="primary" size="small" @click="openBuy(item)">
                  <el-icon><ShoppingCart /></el-icon> 立即购买
                </el-button>
                <el-button size="small" @click="router.push(`/system/trace/${item.traceCode}`)">查看溯源</el-button>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="24" v-if="tableData.length === 0 && !loading">
          <el-empty description="暂无供应信息" />
        </el-col>
      </el-row>
      <div style="display: flex; justify-content: center; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          layout="prev, pager, next" @current-change="fetchData" />
      </div>
    </div>

    <!-- 购买弹窗 -->
    <el-dialog v-model="buyVisible" title="购买商品" width="500px" destroy-on-close>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="商品名称">{{ buyItem.productName }}</el-descriptions-item>
        <el-descriptions-item label="供应方">{{ buyItem.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="单价"><span style="color:#F56C6C;font-weight:600;">¥{{ buyItem.price }}/kg</span></el-descriptions-item>
        <el-descriptions-item label="库存">{{ buyItem.stock }} kg</el-descriptions-item>
      </el-descriptions>
      <el-form style="margin-top: 20px;" label-width="90px">
        <el-form-item label="购买数量">
          <el-input-number v-model="buyForm.quantity" :min="0.1" :max="Number(buyItem.stock)" :precision="1"
            :step="1" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="buyForm.deliveryAddress" placeholder="请输入详细收货地址" />
        </el-form-item>
        <el-form-item label="订单小计">
          <span style="font-size: 22px; font-weight: 700; color: #F56C6C;">¥{{ subtotal }}</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" @click="handleBuy">确认下单</el-button>
      </template>
    </el-dialog>
  </div>
</template>
