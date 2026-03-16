<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { supplierListSupply } from '../../api/supply'
import { supplierListOrders } from '../../api/order'

const router = useRouter()
const stats = ref({ totalSupply: 0, onlineSupply: 0, pendingOrders: 0, totalAmount: 0 })
const pendingOrders = ref<any[]>([])
const myProducts = ref<any[]>([])

onMounted(async () => {
    // 我的供应
    try {
        const supplyRes: any = await supplierListSupply({ page: 1, size: 100 })
        const list = supplyRes.data || []
        stats.value.totalSupply = supplyRes.total || list.length
        stats.value.onlineSupply = list.filter((s: any) => s.status === 1).length
        myProducts.value = list.slice(0, 5)
    } catch { /* 忽略 */ }

    // 我的订单
    try {
        const orderRes: any = await supplierListOrders({ page: 1, size: 100 })
        const orders = orderRes.data || []
        stats.value.pendingOrders = orders.filter((o: any) => o.orderStatus === 0).length
        stats.value.totalAmount = orders
            .filter((o: any) => o.orderStatus === 5)
            .reduce((sum: number, o: any) => sum + (o.totalAmount || 0), 0)
        pendingOrders.value = orders.filter((o: any) => o.orderStatus === 0).slice(0, 5)
    } catch { /* 忽略 */ }
})

const statusMap: Record<number, { text: string; type: string }> = {
    0: { text: '待确认', type: 'warning' },
    1: { text: '已确认', type: 'info' },
    2: { text: '已分配物流', type: '' },
    3: { text: '配送中', type: 'primary' },
    4: { text: '已送达', type: 'success' },
    5: { text: '已完成', type: 'success' },
    6: { text: '已取消', type: 'danger' },
}
</script>

<template>
    <div class="page-container">
        <div class="card-content">
            <h2 style="margin-bottom: 20px;">📦 供应方工作台</h2>

            <!-- 统计卡片 -->
            <el-row :gutter="16" style="margin-bottom: 24px;">
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #409EFF;">{{ stats.totalSupply }}</div>
                        <div style="color: #909399; margin-top: 4px;">我的供应</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #67C23A;">{{ stats.onlineSupply }}</div>
                        <div style="color: #909399; margin-top: 4px;">上架中</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #E6A23C;">{{ stats.pendingOrders }}</div>
                        <div style="color: #909399; margin-top: 4px;">待处理订单</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #F56C6C;">¥{{ stats.totalAmount.toFixed(2)
                        }}</div>
                        <div style="color: #909399; margin-top: 4px;">已完成交易额</div>
                    </el-card>
                </el-col>
            </el-row>

            <el-row :gutter="16">
                <!-- 待处理订单 -->
                <el-col :span="12">
                    <el-card shadow="hover">
                        <template #header>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-weight: 600;">🔔 待处理订单</span>
                                <el-button link type="primary"
                                    @click="router.push('/system/supplier/orders')">查看全部</el-button>
                            </div>
                        </template>
                        <el-table :data="pendingOrders" stripe v-if="pendingOrders.length > 0" size="small">
                            <el-table-column prop="orderNo" label="订单号" min-width="140" />
                            <el-table-column prop="consumerName" label="消费者" width="100" />
                            <el-table-column prop="totalAmount" label="金额" width="100">
                                <template #default="{ row }"><span style="color: #F56C6C;">¥{{ row.totalAmount
                                }}</span></template>
                            </el-table-column>
                            <el-table-column label="状态" width="80">
                                <template #default="{ row }">
                                    <el-tag :type="(statusMap[row.orderStatus]?.type as any) || ''" size="small">{{
                                        statusMap[row.orderStatus]?.text }}</el-tag>
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-empty v-else description="暂无待处理订单 🎉" />
                    </el-card>
                </el-col>

                <!-- 我的产品 -->
                <el-col :span="12">
                    <el-card shadow="hover">
                        <template #header>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-weight: 600;">🌾 我的供应产品</span>
                                <el-button link type="primary"
                                    @click="router.push('/system/supplier/products')">管理产品</el-button>
                            </div>
                        </template>
                        <el-table :data="myProducts" stripe v-if="myProducts.length > 0" size="small">
                            <el-table-column prop="productName" label="产品名" min-width="120" />
                            <el-table-column prop="category" label="品类" width="80" />
                            <el-table-column prop="price" label="单价" width="80">
                                <template #default="{ row }">¥{{ row.price }}</template>
                            </el-table-column>
                            <el-table-column prop="stock" label="库存(kg)" width="90" />
                            <el-table-column label="状态" width="70">
                                <template #default="{ row }">
                                    <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">{{ row.status ===
                                        1 ? '上架' : '下架' }}</el-tag>
                                </template>
                            </el-table-column>
                        </el-table>
                        <el-empty v-else description="暂无供应产品" />
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>
