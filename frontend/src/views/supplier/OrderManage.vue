<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { supplierListOrders, supplierConfirmAndAssign, supplierReject, getOrderDetail } from '../../api/order'
import { getLogisticsUsers } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ status: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 0: '待确认', 1: '待发货', 2: '待揽收', 3: '运输中', 4: '待收货', 5: '已完成', 99: '已关闭' }
const statusTag: Record<number, string> = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'primary', 5: 'success', 99: 'danger' }

const detailVisible = ref(false)
const orderDetail = ref<any>({})
const orderItems = ref<any[]>([])
const orderTracks = ref<any[]>([])

// 确认+分配物流弹窗
const confirmVisible = ref(false)
const confirmOrderId = ref<any>(null)
const logisticsOptions = ref<any[]>([])
const selectedLogisticsId = ref<any>(null)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await supplierListOrders({ page: currentPage.value, size: pageSize.value, ...searchForm })
    tableData.value = res.data; total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }

const openConfirm = async (row: any) => {
  confirmOrderId.value = row.orderId
  selectedLogisticsId.value = null
  try {
    const res: any = await getLogisticsUsers()
    logisticsOptions.value = res.data || []
  } catch { logisticsOptions.value = [] }
  confirmVisible.value = true
}

const handleConfirm = async () => {
  if (!selectedLogisticsId.value) { ElMessage.warning('请选择物流方'); return }
  const selected = logisticsOptions.value.find((u: any) => u.userId === selectedLogisticsId.value)
  if (!selected) { ElMessage.warning('物流方信息获取失败'); return }
  await supplierConfirmAndAssign(confirmOrderId.value, selected.userId, selected.username)
  ElMessage.success('订单已确认，物流方已分配')
  confirmVisible.value = false
  fetchData()
}

const handleReject = async (row: any) => {
  const { value } = await ElMessageBox.prompt('请输入拒绝原因', '拒绝订单', { inputPlaceholder: '原因', type: 'warning' })
  await supplierReject(row.orderId, value)
  ElMessage.success('已拒绝')
  fetchData()
}

const showDetail = async (row: any) => {
  const res: any = await getOrderDetail(row.orderId)
  orderDetail.value = res.data.order; orderItems.value = res.data.details || []; orderTracks.value = res.data.tracks || []
  detailVisible.value = true
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>订单管理</h2>
      </div>
      <div class="search-bar">
        <el-select v-model="searchForm.status" placeholder="订单状态" clearable style="width: 140px;">
          <el-option v-for="(v, k) in statusMap" :key="k" :label="v" :value="Number(k)" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="consumerName" label="消费者" width="120" />
        <el-table-column prop="totalAmount" label="总金额" width="100">
          <template #default="{ row }"><span style="color:#F56C6C;font-weight:600;">¥{{ row.totalAmount
          }}</span></template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="(statusTag[row.orderStatus] as any)">{{ statusMap[row.orderStatus]
          }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="logisticsName" label="物流方" width="120">
          <template #default="{ row }">{{ row.logisticsName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="deliveryAddress" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="250" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 0" size="small" type="success" link
              @click="openConfirm(row)">确认并分配物流</el-button>
            <el-button v-if="row.orderStatus === 0" size="small" type="danger" link
              @click="handleReject(row)">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>

    <!-- 确认+分配物流弹窗 -->
    <el-dialog v-model="confirmVisible" title="确认订单并分配物流" width="480px" destroy-on-close>
      <el-alert type="info" :closable="false" show-icon style="margin-bottom: 16px;">
        确认订单后，系统将自动通知选定的物流方进行揽收。
      </el-alert>
      <el-form label-width="90px">
        <el-form-item label="选择物流方" required>
          <el-select v-model="selectedLogisticsId" placeholder="请选择物流承运方" style="width: 100%;">
            <el-option v-for="u in logisticsOptions" :key="u.userId" :label="u.username" :value="u.userId" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="confirmVisible = false">取消</el-button>
        <el-button type="primary" @click="handleConfirm">确认并分配</el-button>
      </template>
    </el-dialog>

    <!-- 订单详情弹窗 -->
    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="(statusTag[orderDetail.orderStatus] as any)">{{
          statusMap[orderDetail.orderStatus] }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="消费者">{{ orderDetail.consumerName }}</el-descriptions-item>
        <el-descriptions-item label="物流方">{{ orderDetail.logisticsName || '未分配' }}</el-descriptions-item>
        <el-descriptions-item label="收货地址" :span="2">{{ orderDetail.deliveryAddress }}</el-descriptions-item>
        <el-descriptions-item label="总金额"><span style="color:#F56C6C;font-weight:700;">¥{{ orderDetail.totalAmount
        }}</span></el-descriptions-item>
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
          :type="(t.changeContent || '').includes('送达') || (t.changeContent || '').includes('收货') ? 'success' : (t.changeContent || '').includes('发货') || (t.changeContent || '').includes('分配') ? 'primary' : (t.changeContent || '').includes('物流') || (t.changeContent || '').includes('揽收') ? 'warning' : 'info'">
          {{ t.changeContent || '物流状态更新' }} <span style="color:#909399;margin-left:8px;font-size:12px;">{{
            t.operatorName
            }}</span>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>
