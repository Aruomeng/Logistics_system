<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminListSupply, auditSupply, forceOfflineSupply } from '../../api/supply'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'

const searchForm = reactive({ productName: '', auditStatus: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const auditMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已驳回' }
const auditTagType: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await adminListSupply({
      page: currentPage.value, size: pageSize.value, ...searchForm,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.productName = ''; searchForm.auditStatus = undefined; handleSearch() }

const handleAudit = async (row: any, status: number) => {
  const label = status === 1 ? '通过' : '驳回'
  await ElMessageBox.confirm(`确定${label}该供应信息 "${row.productName}" 吗？`, '审核确认', { type: 'warning' })
  await auditSupply(row.supplyId, status)
  ElMessage.success(`已${label}`)
  fetchData()
}

const handleForceOffline = async (row: any) => {
  try {
    await secureConfirm(`确定强制下架该商品 <b>${row.productName}</b> 吗？`, '危险操作验证')
    await forceOfflineSupply(row.supplyId)
    ElMessage.success('安全指令已放行，已强制下架')
    fetchData()
  } catch (error) {
    // 中止执行
  }
}

const handleExport = () => {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams()
  if (searchForm.auditStatus !== undefined) params.set('auditStatus', String(searchForm.auditStatus))
  window.open(`/api/v1/export/supplies?${params.toString()}&token=${token}`, '_blank')
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>供应审核</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.productName" placeholder="产品名称" clearable style="width: 200px;" />
        <el-select v-model="searchForm.auditStatus" placeholder="审核状态" clearable style="width: 120px;">
          <el-option label="待审核" :value="0" /><el-option label="已通过" :value="1" /><el-option label="已驳回" :value="2" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
        <el-button type="success" @click="handleExport">
          <el-icon>
            <Download />
          </el-icon> 导出Excel
        </el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="supplierName" label="供应方" width="120" />
        <el-table-column prop="category" label="品类" width="100" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="recommendPrice" label="推荐价" width="100" />
        <el-table-column prop="stock" label="库存(kg)" width="100" />
        <el-table-column prop="productionPlace" label="产地" width="140" />
        <el-table-column prop="auditStatus" label="审核状态" width="100">
          <template #default="{ row }">
            <el-tag :type="(auditTagType[row.auditStatus] as any)">{{ auditMap[row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="上架" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status === 1 ? '上架' : '下架'
            }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.auditStatus === 0" size="small" type="success" link
              @click="handleAudit(row, 1)">通过</el-button>
            <el-button v-if="row.auditStatus === 0" size="small" type="danger" link
              @click="handleAudit(row, 2)">驳回</el-button>
            <el-button v-if="row.status === 1" size="small" type="warning" link
              @click="handleForceOffline(row)">强制下架</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>
