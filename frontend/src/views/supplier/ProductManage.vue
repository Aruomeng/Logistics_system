<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { supplierListSupply, addSupply, updateSupply, onlineSupply, offlineSupply } from '../../api/supply'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'

const searchForm = reactive({ productName: '', status: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const supplyForm = reactive({
  supplyId: 0, productName: '', category: '农产品', subCategory: '', productionPlace: '',
  productionDate: '', shelfLife: 30, price: 0, stock: 0,
  seedInfo: '', fertilizerInfo: '', pesticideInfo: '', harvestInfo: '', inspectionInfo: '',
})

const auditStatusMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已驳回' }
const auditTagType: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await supplierListSupply({
      page: currentPage.value, size: pageSize.value, ...searchForm,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.productName = ''; searchForm.status = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '发布供应信息'
  isEdit.value = false
  Object.assign(supplyForm, {
    supplyId: 0, productName: '', category: '农产品', subCategory: '', productionPlace: '',
    productionDate: '', shelfLife: 30, price: 0, stock: 0,
    seedInfo: '', fertilizerInfo: '', pesticideInfo: '', harvestInfo: '', inspectionInfo: '',
  })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑供应信息'
  isEdit.value = true
  Object.assign(supplyForm, row)
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!supplyForm.productName.trim()) { ElMessage.warning('请输入产品名称'); return }
  if (isEdit.value) {
    await updateSupply(supplyForm)
    ElMessage.success('编辑成功')
  } else {
    await addSupply({
      supply: {
        productName: supplyForm.productName, category: supplyForm.category, subCategory: supplyForm.subCategory,
        productionPlace: supplyForm.productionPlace, productionDate: supplyForm.productionDate,
        shelfLife: supplyForm.shelfLife, price: supplyForm.price, stock: supplyForm.stock,
      },
      trace: {
        seedInfo: supplyForm.seedInfo, fertilizerInfo: supplyForm.fertilizerInfo,
        pesticideInfo: supplyForm.pesticideInfo, harvestInfo: supplyForm.harvestInfo,
        inspectionInfo: supplyForm.inspectionInfo,
      },
    })
    ElMessage.success('发布成功，等待管理员审核')
  }
  dialogVisible.value = false
  fetchData()
}

const handleToggleStatus = async (row: any) => {
  if (row.status === 1) {
    try {
      await secureConfirm(`确定下架您供应的产品 <b>${row.productName}</b> 吗？`, '危险操作验证')
      await offlineSupply(row.supplyId)
      ElMessage.success('已下架')
    } catch (error) {
      return
    }
  } else {
    await onlineSupply(row.supplyId)
    ElMessage.success('已上架')
  }
  fetchData()
}

const handleRestock = async (row: any) => {
  const { value } = await ElMessageBox.prompt(
    `当前库存：${row.stock} kg，请输入要追加的数量`,
    `补货 - ${row.productName}`,
    { inputPattern: /^\d+(\.\d{1,2})?$/, inputErrorMessage: '请输入有效数字', confirmButtonText: '确认补货' }
  )
  const addQty = Number(value)
  if (addQty <= 0) { ElMessage.warning('补货数量必须大于0'); return }
  await updateSupply({ supplyId: row.supplyId, stock: Number(row.stock) + addQty })
  ElMessage.success(`已补货 ${addQty} kg，当前库存 ${Number(row.stock) + addQty} kg`)
  fetchData()
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>供应管理</h2>
        <el-button type="primary" @click="handleAdd"><el-icon>
            <Plus />
          </el-icon> 发布供应</el-button>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.productName" placeholder="产品名称" clearable style="width: 200px;" />
        <el-select v-model="searchForm.status" placeholder="上架状态" clearable style="width: 120px;">
          <el-option label="上架" :value="1" /><el-option label="下架" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="productName" label="产品名称" min-width="140" />
        <el-table-column prop="category" label="品类" width="100" />
        <el-table-column prop="price" label="单价(元/kg)" width="120" />
        <el-table-column prop="recommendPrice" label="推荐价" width="100">
          <template #default="{ row }">
            <span :style="{ color: row.price > row.recommendPrice * 1.2 ? '#F56C6C' : '#67C23A' }">
              {{ row.recommendPrice || '--' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存(kg)" width="120">
          <template #default="{ row }">
            <span v-if="Number(row.stock) <= 0" style="color:#F56C6C;font-weight:600;">已售罄</span>
            <span v-else-if="Number(row.stock) < 10" style="color:#E6A23C;font-weight:600;">{{ row.stock }}
              <el-tag size="small" type="warning" style="margin-left:4px;">库存不足</el-tag>
            </span>
            <span v-else>{{ row.stock }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核" width="90">
          <template #default="{ row }">
            <el-tag :type="(auditTagType[row.auditStatus] as any)">{{ auditStatusMap[row.auditStatus] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '上架' : '下架' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" type="success" link @click="handleRestock(row)"
              :disabled="row.auditStatus !== 1">补货</el-button>
            <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" link
              @click="handleToggleStatus(row)" :disabled="row.auditStatus !== 1">
              {{ row.status === 1 ? '下架' : '上架' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form :model="supplyForm" label-width="100px">
        <el-divider content-position="left">产品信息</el-divider>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="产品名称"><el-input
                v-model="supplyForm.productName" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="品类">
              <el-select v-model="supplyForm.category" style="width:100%;"><el-option label="农产品"
                  value="农产品" /><el-option label="生产物料" value="生产物料" /></el-select>
            </el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12"><el-form-item label="子品类"><el-input v-model="supplyForm.subCategory"
                placeholder="如：蔬菜、水果" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="生产地"><el-input
                v-model="supplyForm.productionPlace" /></el-form-item></el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="8"><el-form-item label="生产日期"><el-date-picker v-model="supplyForm.productionDate" type="date"
                value-format="YYYY-MM-DD" style="width:100%;" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="保质期(天)"><el-input-number v-model="supplyForm.shelfLife" :min="1"
                style="width:100%;" /></el-form-item></el-col>
          <el-col :span="8"><el-form-item label="单价(元/kg)"><el-input-number v-model="supplyForm.price" :min="0"
                :precision="2" style="width:100%;" /></el-form-item></el-col>
        </el-row>
        <el-form-item label="库存(kg)"><el-input-number v-model="supplyForm.stock" :min="0" :precision="2"
            style="width: 200px;" /></el-form-item>

        <template v-if="!isEdit">
          <el-divider content-position="left">溯源信息</el-divider>
          <el-form-item label="种苗信息"><el-input v-model="supplyForm.seedInfo" type="textarea" :rows="2" /></el-form-item>
          <el-form-item label="施肥信息"><el-input v-model="supplyForm.fertilizerInfo" type="textarea"
              :rows="2" /></el-form-item>
          <el-form-item label="农药信息"><el-input v-model="supplyForm.pesticideInfo" type="textarea"
              :rows="2" /></el-form-item>
          <el-form-item label="采收信息"><el-input v-model="supplyForm.harvestInfo" type="textarea"
              :rows="2" /></el-form-item>
          <el-form-item label="质检报告"><el-input v-model="supplyForm.inspectionInfo" type="textarea"
              :rows="2" /></el-form-item>
        </template>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
