<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { consumerListOrders } from '../../api/order'
import { commonListSupply } from '../../api/supply'

const router = useRouter()
const stats = ref({ total: 0, pending: 0, delivering: 0, finished: 0 })
const recentOrders = ref<any[]>([])
const hotProducts = ref<any[]>([])

onMounted(async () => {
    // 我的订单
    try {
        const res: any = await consumerListOrders({ page: 1, size: 100 })
        const orders = res.data || []
        stats.value.total = res.total || orders.length
        stats.value.pending = orders.filter((o: any) => o.orderStatus <= 1).length
        stats.value.delivering = orders.filter((o: any) => o.orderStatus === 3).length
        stats.value.finished = orders.filter((o: any) => o.orderStatus === 5).length
        recentOrders.value = orders.slice(0, 5)
    } catch { /* 忽略 */ }

    // 热门供应推荐
    try {
        const res: any = await commonListSupply({ page: 1, size: 6 })
        hotProducts.value = res.data || []
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
            <h2 style="margin-bottom: 20px;">🛒 消费者中心</h2>

            <!-- 统计卡片 -->
            <el-row :gutter="16" style="margin-bottom: 24px;">
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #409EFF;">{{ stats.total }}</div>
                        <div style="color: #909399; margin-top: 4px;">全部订单</div>
                    </el-card>
                </el-col>
                <el-col :span="6">
                    <el-card shadow="hover" style="text-align: center;">
                        <div style="font-size: 28px; font-weight: 700; color: #E6A23C;">{{ stats.pending }}</div>
                        <div style="color: #909399; margin-top: 4px;">待处理</div>
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
                        <div style="font-size: 28px; font-weight: 700; color: #67C23A;">{{ stats.finished }}</div>
                        <div style="color: #909399; margin-top: 4px;">已完成</div>
                    </el-card>
                </el-col>
            </el-row>

            <el-row :gutter="16">
                <!-- 我的订单 -->
                <el-col :span="12">
                    <el-card shadow="hover">
                        <template #header>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-weight: 600;">📦 我的近期订单</span>
                                <el-button link type="primary"
                                    @click="router.push('/system/consumer/orders')">查看全部</el-button>
                            </div>
                        </template>
                        <el-table :data="recentOrders" stripe v-if="recentOrders.length > 0" size="small">
                            <el-table-column prop="orderNo" label="订单号" min-width="140" />
                            <el-table-column prop="supplierName" label="供应方" width="100" />
                            <el-table-column prop="totalAmount" label="金额" width="90">
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
                        <el-empty v-else description="暂无订单" />
                    </el-card>
                </el-col>

                <!-- 热门推荐 -->
                <el-col :span="12">
                    <el-card shadow="hover">
                        <template #header>
                            <div style="display: flex; justify-content: space-between; align-items: center;">
                                <span style="font-weight: 600;">🔥 热门供应推荐</span>
                                <el-button link type="primary"
                                    @click="router.push('/system/consumer/supply-list')">去供应大厅</el-button>
                            </div>
                        </template>
                        <div v-if="hotProducts.length > 0"
                            style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">
                            <el-card v-for="p in hotProducts" :key="p.supplyId" shadow="hover" style="cursor: pointer;"
                                @click="router.push(`/system/trace/${p.supplyId}`)">
                                <div
                                    style="font-weight: 600; margin-bottom: 4px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">
                                    {{ p.productName }}
                                </div>
                                <div style="font-size: 12px; color: #909399;">{{ p.productionPlace }} · {{ p.subCategory
                                }}</div>
                                <div style="margin-top: 6px;">
                                    <span style="color: #F56C6C; font-weight: 700; font-size: 16px;">¥{{ p.price
                                    }}</span>
                                    <span style="color: #909399; font-size: 12px;">/kg</span>
                                </div>
                            </el-card>
                        </div>
                        <el-empty v-else description="暂无推荐" />
                    </el-card>
                </el-col>
            </el-row>
        </div>
    </div>
</template>
