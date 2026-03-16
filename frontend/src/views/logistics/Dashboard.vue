<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { logisticsListOrders } from '../../api/order'

const router = useRouter()
const stats = ref({ pending: 0, delivering: 0, delivered: 0, total: 0 })
const deliveringOrders = ref<any[]>([])

onMounted(async () => {
    try {
        const res: any = await logisticsListOrders({ page: 1, size: 100 })
        const orders = res.data || []
        stats.value.total = res.total || orders.length
        stats.value.pending = orders.filter((o: any) => o.orderStatus === 2).length
        stats.value.delivering = orders.filter((o: any) => o.orderStatus === 3).length
        stats.value.delivered = orders.filter((o: any) => o.orderStatus >= 4).length
        deliveringOrders.value = orders.filter((o: any) => o.orderStatus === 2 || o.orderStatus === 3).slice(0, 8)
    } catch { /* 忽略 */ }
})

const statusMap: Record<number, { text: string; type: string }> = {
    2: { text: '待揽收', type: 'warning' },
    3: { text: '配送中', type: 'primary' },
    4: { text: '已送达', type: 'success' },
    5: { text: '已完成', type: 'success' },
}
</script>

<template>
    <div class="page-container">
        <div class="card-content">
            <h2 style="margin-bottom: 20px;">🚛 物流工作台</h2>

            <!-- 统计卡片 -->
            <el-row :gutter="16" style="margin-bottom: 24px;">
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #E6A23C;">{{ stats.pending }}</div>
                        <div style="color: #909399; margin-top: 4px;">待揽收</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #409EFF;">{{ stats.delivering }}</div>
                        <div style="color: #909399; margin-top: 4px;">配送中</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #67C23A;">{{ stats.delivered }}</div>
                        <div style="color: #909399; margin-top: 4px;">已送达</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #909399;">{{ stats.total }}</div>
                        <div style="color: #909399; margin-top: 4px;">总单量</div>
                    </el-card>
                </el-col>
            </el-row>

            <!-- 待处理运单 -->
            <el-card shadow="hover">
                <template #header>
                    <div style="display: flex; justify-content: space-between; align-items: center;">
                        <span style="font-weight: 600;">📋 待处理运单</span>
                        <el-button link type="primary" @click="router.push('/system/logistics/orders')">查看全部</el-button>
                    </div>
                </template>
                <el-table :data="deliveringOrders" stripe v-if="deliveringOrders.length > 0">
                    <el-table-column prop="orderNo" label="订单号" min-width="160" />
                    <el-table-column prop="supplierName" label="供应方" width="120" />
                    <el-table-column prop="consumerName" label="消费者" width="120" />
                    <el-table-column prop="deliveryAddress" label="收货地址" min-width="180" show-overflow-tooltip />
                    <el-table-column label="状态" width="90">
                        <template #default="{ row }">
                            <el-tag :type="(statusMap[row.orderStatus]?.type as any) || ''" size="small">
                                {{ statusMap[row.orderStatus]?.text || '未知' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
                <el-empty v-else description="当前没有待处理运单 🎉" />
            </el-card>
        </div>
    </div>
</template>
