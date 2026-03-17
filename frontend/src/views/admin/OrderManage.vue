<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminListOrders, assignLogistics, getOrderDetail } from '../../api/order'
import { listUsers } from '../../api/user'
import { ElMessage } from 'element-plus'

const searchForm = reactive({ status: undefined as number | undefined, orderNo: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 0: '待确认', 1: '待发货', 2: '待揽收', 3: '运输中', 4: '待收货', 5: '已完成', 99: '已关闭' }
const statusTag: Record<number, string> = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'primary', 5: 'success', 99: 'danger' }

// 物流方选择弹窗
const assignVisible = ref(false)
const assignOrderId = ref(0)
const logisticsUsers = ref<any[]>([])
const selectedLogistics = ref<any>(null)

// 详情
const detailVisible = ref(false)
const orderDetail = ref<any>({})
const orderItems = ref<any[]>([])
const orderTracks = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await adminListOrders({ page: currentPage.value, size: pageSize.value, ...searchForm })
    tableData.value = res.data; total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.status = undefined; searchForm.orderNo = ''; handleSearch() }

const openAssign = async (row: any) => {
  assignOrderId.value = row.orderId
  selectedLogistics.value = null
  // 查询物流方用户
  const res: any = await listUsers({ page: 1, size: 100, roleType: 3, status: 1 })
  logisticsUsers.value = res.data
  assignVisible.value = true
}

const handleAssign = async () => {
  if (!selectedLogistics.value) { ElMessage.warning('请选择物流方'); return }
  const user = logisticsUsers.value.find((u: any) => u.userId === selectedLogistics.value)
  await assignLogistics(assignOrderId.value, user.userId, user.username)
  ElMessage.success('物流分配成功')
  assignVisible.value = false
  fetchData()
}

const showDetail = async (row: any) => {
  const res: any = await getOrderDetail(row.orderId)
  orderDetail.value = res.data.order; orderItems.value = res.data.details || []; orderTracks.value = res.data.tracks || []
  detailVisible.value = true
}

const handleExport = () => {
  const token = localStorage.getItem('token')
  const params = new URLSearchParams()
  if (searchForm.status !== undefined) params.set('orderStatus', String(searchForm.status))
  window.open(`/api/v1/export/orders?${params.toString()}&token=${token}`, '_blank')
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>订单管理</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.orderNo" placeholder="订单号" clearable style="width: 200px;" />
        <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 140px;">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
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
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="consumerName" label="消费者" width="100" />
        <el-table-column prop="supplierName" label="供应方" width="100" />
        <el-table-column prop="logisticsName" label="物流方" width="100">
          <template #default="{ row }">{{ row.logisticsName || '--' }}</template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="100">
          <template #default="{ row }"><span style="color:#F56C6C;font-weight:600;">¥{{ row.totalAmount
          }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="(statusTag[row.orderStatus] as any)">{{ statusMap[row.orderStatus]
          }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 1 && !row.logisticsId" size="small" type="warning" link
              @click="openAssign(row)">分配物流</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>

    <!-- 分配物流弹窗 -->
    <el-dialog v-model="assignVisible" title="分配物流方" width="420px">
      <el-select v-model="selectedLogistics" placeholder="请选择物流方" style="width: 100%;">
        <el-option v-for="u in logisticsUsers" :key="u.userId" :label="u.username" :value="u.userId" />
      </el-select>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAssign">确定</el-button>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="(statusTag[orderDetail.orderStatus] as any)">{{
          statusMap[orderDetail.orderStatus] }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="消费者">{{ orderDetail.consumerName }}</el-descriptions-item>
        <el-descriptions-item label="供应方">{{ orderDetail.supplierName }}</el-descriptions-item>
        <el-descriptions-item label="物流方">{{ orderDetail.logisticsName || '--' }}</el-descriptions-item>
        <el-descriptions-item label="总金额"><span style="color:#F56C6C;font-weight:700;">¥{{ orderDetail.totalAmount
        }}</span></el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.deliveryAddress }}</el-descriptions-item>
      </el-descriptions>
      <h4 style="margin: 16px 0 8px;">商品明细</h4>
      <el-table :data="orderItems" border size="small">
        <el-table-column prop="productName" label="商品" /><el-table-column prop="quantity" label="数量(kg)" width="100" />
        <el-table-column prop="unitPrice" label="单价" width="80" /><el-table-column prop="totalPrice" label="小计"
          width="100" />
      </el-table>
      <h4 style="margin: 16px 0 8px;">物流轨迹</h4>
      <el-timeline>
        <el-timeline-item v-for="t in orderTracks" :key="t.trackId" :timestamp="t.createTime" placement="top"
          :type="(t.changeContent || '').includes('送达') || (t.changeContent || '').includes('收货') ? 'success' : (t.changeContent || '').includes('发货') || (t.changeContent || '').includes('分配') ? 'primary' : (t.changeContent || '').includes('物流') ? 'warning' : 'info'">
          {{ t.changeContent || '状态更新' }} <span style="color:#909399;margin-left:8px;font-size:12px;">{{ t.operatorName
            }}</span>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>
