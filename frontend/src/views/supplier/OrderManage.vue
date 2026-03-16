<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { supplierListOrders, supplierConfirm, supplierReject, getOrderDetail } from '../../api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const searchForm = reactive({ status: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 0: '待支付', 1: '待发货', 2: '待揽收', 3: '运输中', 4: '待收货', 5: '已完成', 99: '已关闭' }
const statusTag: Record<number, string> = { 0: 'info', 1: 'primary', 2: 'warning', 3: 'warning', 4: 'primary', 5: 'success', 99: 'danger' }

const detailVisible = ref(false)
const orderDetail = ref<any>({})
const orderItems = ref<any[]>([])
const orderTracks = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await supplierListOrders({ page: currentPage.value, size: pageSize.value, ...searchForm })
    tableData.value = res.data; total.value = res.total || 0
  } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }

const handleConfirm = async (row: any) => {
  await ElMessageBox.confirm('确认接受此订单？', '提示')
  await supplierConfirm(row.orderId)
  ElMessage.success('已确认')
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
        <el-table-column prop="deliveryAddress" label="收货地址" min-width="180" show-overflow-tooltip />
        <el-table-column prop="createTime" label="下单时间" width="170" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
            <el-button v-if="row.orderStatus === 0" size="small" type="success" link
              @click="handleConfirm(row)">确认</el-button>
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

    <el-dialog v-model="detailVisible" title="订单详情" width="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ orderDetail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="(statusTag[orderDetail.orderStatus] as any)">{{
          statusMap[orderDetail.orderStatus] }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="消费者">{{ orderDetail.consumerName }}</el-descriptions-item>
        <el-descriptions-item label="收货地址">{{ orderDetail.deliveryAddress }}</el-descriptions-item>
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
          :type="(t.changeContent || '').includes('送达') || (t.changeContent || '').includes('收货') ? 'success' : (t.changeContent || '').includes('发货') || (t.changeContent || '').includes('分配') ? 'primary' : (t.changeContent || '').includes('物流') ? 'warning' : 'info'">
          {{ t.changeContent || '物流状态更新' }} <span style="color:#909399;margin-left:8px;font-size:12px;">{{
            t.operatorName
            }}</span>
        </el-timeline-item>
      </el-timeline>
    </el-dialog>
  </div>
</template>
