<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { commonListSupply } from '../../api/supply'

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
          <el-card shadow="hover" style="cursor: pointer;" @click="router.push(`/system/trace/${item.traceCode}`)">
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
                <el-button type="primary" size="small"
                  @click.stop="router.push(`/system/trace/${item.traceCode}`)">查看溯源</el-button>
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
  </div>
</template>
