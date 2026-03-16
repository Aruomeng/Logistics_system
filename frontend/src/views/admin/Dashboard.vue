<script setup lang="ts">
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview, getOrderTrend, getCategoryDistribution, getPlaceRanking, getSupplierRanking, getInventoryWarning, getInventoryTurnover, getColdChainWarning } from '../../api/analysis'

const overview = ref<any>({})
const warningList = ref<any[]>([])
const coldChainData = ref<any>({})

const trendChart = ref<HTMLElement>()
const categoryChart = ref<HTMLElement>()
const placeChart = ref<HTMLElement>()
const supplierChart = ref<HTMLElement>()
const turnoverChart = ref<HTMLElement>()
const coldChainChart = ref<HTMLElement>()

onMounted(async () => {
    // 总览
    const overviewRes: any = await getOverview()
    overview.value = overviewRes.data

    // 库存预警
    const warningRes: any = await getInventoryWarning()
    warningList.value = warningRes.data || []

    await nextTick()

    // 7天订单趋势（无边界柔顺折线带通透背景）
    const trendRes: any = await getOrderTrend()
    const trendData = trendRes.data || []
    if (trendChart.value) {
        const chart = echarts.init(trendChart.value)
        chart.setOption({
            tooltip: { trigger: 'axis', backgroundColor: 'rgba(255, 255, 255, 0.8)', padding: [10, 16], borderRadius: 12, borderWidth: 0, textStyle: { color: '#1e293b', fontWeight: 600 } },
            xAxis: {
                type: 'category',
                data: trendData.map((d: any) => d.date),
                axisLine: { show: false },
                axisTick: { show: false },
                axisLabel: { color: '#94a3b8', margin: 16 }
            },
            yAxis: {
                type: 'value',
                splitLine: { show: true, lineStyle: { type: 'dashed', color: 'rgba(226, 232, 240, 0.6)' } },
                axisLabel: { show: false }
            },
            series: [{
                data: trendData.map((d: any) => d.count),
                type: 'line',
                smooth: 0.4,
                symbolSize: 0,
                lineStyle: { width: 4, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#8b5cf6' }]) },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(59, 130, 246, 0.4)' }, { offset: 1, color: 'rgba(139, 92, 246, 0)' }])
                }
            }],
            grid: { left: -20, right: 0, top: 20, bottom: 20 },
        })
        window.addEventListener('resize', () => chart.resize())
    }

    // 品类分布（极简甜甜圈图）
    const catRes: any = await getCategoryDistribution()
    const catData = catRes.data || []
    if (categoryChart.value) {
        const chart = echarts.init(categoryChart.value)
        chart.setOption({
            tooltip: { trigger: 'item', backgroundColor: 'rgba(255,255,255,0.9)', borderRadius: 12, borderWidth: 0, textStyle: { fontWeight: 600 } },
            legend: {
                bottom: '0',
                icon: 'circle',
                itemWidth: 10,
                itemHeight: 10,
                itemGap: 16,
                padding: [0, 20, 0, 20],
                textStyle: { color: '#64748b' }
            },
            series: [{
                type: 'pie',
                radius: ['50%', '75%'],
                center: ['50%', '38%'],
                avoidLabelOverlap: false,
                itemStyle: { borderRadius: 8, borderColor: '#fff', borderWidth: 2 },
                label: { show: false },
                data: catData,
            }],
            color: ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#06b6d4']
        })
        window.addEventListener('resize', () => chart.resize())
    }

    // 产地排行（胶囊柱状图）
    const placeRes: any = await getPlaceRanking()
    const placeData = placeRes.data || []
    if (placeChart.value) {
        const chart = echarts.init(placeChart.value)
        chart.setOption({
            tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderRadius: 12, borderWidth: 0, axisPointer: { type: 'none' } },
            xAxis: {
                type: 'category',
                data: placeData.map((d: any) => d.place),
                axisLine: { show: false },
                axisTick: { show: false },
                axisLabel: {
                    interval: 0,
                    color: '#94a3b8',
                    margin: 16,
                    fontSize: 11,
                    rotate: 35,
                    formatter: (val: string) => {
                        return val.length > 5 ? val.substring(0, 4) + '...' : val
                    }
                }
            },
            yAxis: { show: false },
            series: [{
                data: placeData.map((d: any) => d.count),
                type: 'bar',
                barWidth: '16px',
                barCategoryGap: '10%',
                itemStyle: {
                    borderRadius: [100, 100, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#10b981' }, { offset: 1, color: 'rgba(16, 185, 129, 0.2)' }])
                }
            }],
            grid: { left: -10, right: -10, top: 20, bottom: 65, containLabel: false },
        })
        window.addEventListener('resize', () => chart.resize())
    }

    // 供应方交易额（极简横向胶囊）
    const supRes: any = await getSupplierRanking()
    const supData = supRes.data || []
    if (supplierChart.value) {
        const chart = echarts.init(supplierChart.value)
        chart.setOption({
            tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderRadius: 12, borderWidth: 0, axisPointer: { type: 'none' } },
            xAxis: { show: false },
            yAxis: { type: 'category', data: supData.map((d: any) => d.name).reverse(), axisLine: { show: false }, axisTick: { show: false }, axisLabel: { interval: 0, color: '#64748b', fontSize: 12, fontWeight: 500, margin: 16 } },
            series: [{
                data: supData.map((d: any) => d.amount).reverse(),
                type: 'bar',
                barWidth: '40%',
                itemStyle: {
                    borderRadius: [0, 100, 100, 0],
                    color: new echarts.graphic.LinearGradient(1, 0, 0, 0, [{ offset: 0, color: '#f59e0b' }, { offset: 1, color: 'rgba(245, 158, 11, 0.2)' }])
                }
            }],
            grid: { left: 90, right: 20, top: 10, bottom: 10 },
        })
        window.addEventListener('resize', () => chart.resize())
    }

    // 周转率监控（胶囊柱状图）
    const turnoverRes: any = await getInventoryTurnover()
    const turnoverData = turnoverRes.data || []
    if (turnoverChart.value) {
        const chart = echarts.init(turnoverChart.value)
        chart.setOption({
            tooltip: { trigger: 'axis', backgroundColor: 'rgba(255,255,255,0.9)', borderRadius: 12, borderWidth: 0, axisPointer: { type: 'none' } },
            xAxis: {
                type: 'category',
                data: turnoverData.map((d: any) => d.category),
                axisLine: { show: false },
                axisTick: { show: false },
                axisLabel: {
                    interval: 0,
                    color: '#94a3b8',
                    margin: 16,
                    fontSize: 11,
                    rotate: 30,
                    formatter: (val: string) => {
                        return val.length > 3 ? val.substring(0, 3) + '..' : val
                    }
                }
            },
            yAxis: { show: false },
            series: [{
                data: turnoverData.map((d: any) => d.turnoverDays),
                type: 'bar',
                barWidth: '16px',
                barCategoryGap: '10%',
                itemStyle: {
                    borderRadius: [100, 100, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: '#ec4899' }, { offset: 1, color: 'rgba(236, 72, 153, 0.2)' }])
                }
            }],
            grid: { left: -10, right: -10, top: 20, bottom: 65, containLabel: false },
        })
        window.addEventListener('resize', () => chart.resize())
    }

    // 冷链异常率监控（仪表盘无框化）
    const coldChainRes: any = await getColdChainWarning()
    coldChainData.value = coldChainRes.data || {}
    if (coldChainChart.value) {
        const chart = echarts.init(coldChainChart.value)
        chart.setOption({
            series: [{
                type: 'gauge',
                center: ['50%', '65%'],
                radius: '90%',
                startAngle: 200,
                endAngle: -20,
                pointer: { show: false },
                progress: { show: true, overlap: false, roundCap: true, clip: false, itemStyle: { borderWidth: 0, color: new echarts.graphic.LinearGradient(0, 0, 1, 0, [{ offset: 0, color: '#3b82f6' }, { offset: 1, color: '#ef4444' }]) } },
                axisLine: { lineStyle: { width: 18, color: [[1, 'rgba(226, 232, 240, 0.6)']] } },
                splitLine: { show: false },
                axisTick: { show: false },
                axisLabel: { show: false },
                detail: { valueAnimation: true, formatter: '{value}%', fontSize: 36, fontWeight: 700, color: '#6366f1', offsetCenter: [0, '0%'] },
                data: [{ value: Number((coldChainData.value.exceptionRate || 0).toFixed(1)) }]
            }]
        })
        window.addEventListener('resize', () => chart.resize())
    }
})
</script>

<template>
    <div class="page-container dashboard-bento">
        <!-- 去除传统包裹框，以 Bento Grid (便当盒) 模式直接裸铺 -->

        <!-- 第一维度：核心 KPI 聚合行 -->
        <div class="bento-kpi-grid">
            <el-card class="bento-kpi-card">
                <div class="kpi-header">
                    <div class="kpi-icon blue">
                        <el-icon>
                            <Box />
                        </el-icon>
                    </div>
                    <div class="kpi-label">全栈商品储备</div>
                </div>
                <div class="kpi-body">
                    <div class="kpi-value">{{ overview.totalSupply || 0 }}<span class="unit">项</span></div>
                </div>
            </el-card>

            <el-card class="bento-kpi-card">
                <div class="kpi-header">
                    <div class="kpi-icon green">
                        <el-icon>
                            <Monitor />
                        </el-icon>
                    </div>
                    <div class="kpi-label">活跃上架中</div>
                </div>
                <div class="kpi-body">
                    <div class="kpi-value">{{ overview.onlineSupply || 0 }}<span class="unit">个</span></div>
                </div>
            </el-card>

            <el-card class="bento-kpi-card">
                <div class="kpi-header">
                    <div class="kpi-icon purple">
                        <el-icon>
                            <Tickets />
                        </el-icon>
                    </div>
                    <div class="kpi-label">累计处理订单</div>
                </div>
                <div class="kpi-body">
                    <div class="kpi-value">{{ overview.totalOrder || 0 }}<span class="unit">单</span></div>
                </div>
            </el-card>

            <el-card class="bento-kpi-card">
                <div class="kpi-header">
                    <div class="kpi-icon red">
                        <el-icon>
                            <Money />
                        </el-icon>
                    </div>
                    <div class="kpi-label">总流水营收</div>
                </div>
                <div class="kpi-body">
                    <div class="kpi-value">¥ {{ overview.totalAmount || 0 }}</div>
                </div>
            </el-card>
        </div>

        <!-- 第二维度：核心引擎走势与环比结构 -->
        <div class="bento-main-grid">
            <el-card class="bento-panel">
                <div class="panel-header">近 7 日高频交易波动中枢</div>
                <div ref="trendChart" class="panel-chart"></div>
            </el-card>

            <el-card class="bento-panel">
                <div class="panel-header">宏观品类吞吐量占比</div>
                <div ref="categoryChart" class="panel-chart"></div>
            </el-card>
        </div>

        <!-- 第三维度：供给源地、周转与冷链风控 -->
        <div class="bento-sub-grid">
            <el-card class="bento-panel">
                <div class="panel-header">热销产地辐射 TopRanks</div>
                <div ref="placeChart" class="panel-chart"></div>
            </el-card>

            <el-card class="bento-panel">
                <div class="panel-header">供应商 GMV 矩阵榜</div>
                <div ref="supplierChart" class="panel-chart"></div>
            </el-card>

            <el-card class="bento-panel">
                <div class="panel-header">商品动销周期 (出库均时)</div>
                <div ref="turnoverChart" class="panel-chart"></div>
            </el-card>

            <el-card class="bento-panel warning-panel">
                <div class="panel-header">🚨 全链路冷链超时熔断探针</div>
                <div ref="coldChainChart" class="panel-chart gauge"></div>
                <div class="gauge-footer" v-if="(coldChainData?.exceptionRate || 0) > 10">警告：干线冷库异常波动</div>
                <div class="gauge-footer safe" v-else>系统健康运转中</div>
            </el-card>
        </div>

        <!-- 底部聚合与警报容器 -->
        <el-card class="bento-panel full-width">
            <div class="panel-header">🚨 临期与熔断预警调度仓 (低于100kg基线)</div>
            <el-table :data="warningList" stripe class="ultra-modern-table" v-if="warningList.length > 0">
                <el-table-column prop="productName" label="货物标的" />
                <el-table-column prop="supplierName" label="所属供应基站" width="180" />
                <el-table-column prop="category" label="分类标签" width="120" />
                <el-table-column prop="stock" label="熔断报警线 (kg)" width="160">
                    <template #default="{ row }">
                        <span class="danger-badge">{{ row.stock }} / 100</span>
                    </template>
                </el-table-column>
            </el-table>
            <el-empty v-else description="暂无警戒通报，仓储动线畅通" />
        </el-card>
    </div>
</template>

<style scoped>
/* Bento Box 核心引擎布局 */
.dashboard-bento {
    display: flex;
    flex-direction: column;
    gap: 24px;
}

.bento-kpi-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
    gap: 24px;
}

.bento-main-grid {
    display: grid;
    grid-template-columns: 2fr 1fr;
    gap: 24px;
}

.bento-sub-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 24px;
}

.full-width {
    width: 100%;
}

/* KPI 微互动卡片 */
.bento-kpi-card {
    padding: 24px;
    display: flex;
    flex-direction: column !important;
    justify-content: space-between;
    height: 100%;
    cursor: pointer;
}

.kpi-header {
    display: flex;
    align-items: center;
    gap: 12px;
}

.kpi-icon {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: flex;
    justify-content: center;
    align-items: center;
    font-size: 28px;
    color: #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
    transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.kpi-icon.blue {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
}

.kpi-icon.green {
    background: linear-gradient(135deg, #10b981, #059669);
}

.kpi-icon.purple {
    background: linear-gradient(135deg, #8b5cf6, #7c3aed);
}

.kpi-icon.red {
    background: linear-gradient(135deg, #f43f5e, #e11d48);
}

.kpi-label {
    font-size: 15px;
    font-weight: 600;
    color: #64748b;
    letter-spacing: 0.5px;
}

.kpi-body {
    display: flex;
    justify-content: center;
    align-items: flex-end;
    width: 100%;
    margin-top: 24px;
}

.kpi-value {
    font-size: 42px;
    font-weight: 900;
    color: #0f172a;
    line-height: 1;
    letter-spacing: -0.02em;
    white-space: nowrap;
    /* 强制不折行，防止 ¥ 符号和数字断列 */
}

.kpi-value .unit {
    font-size: 16px;
    font-weight: 600;
    color: #94a3b8;
    margin-left: 6px;
    margin-bottom: 4px;
}

/* 图表便当基础壳 */
.bento-panel {
    padding: 24px;
    display: flex;
    flex-direction: column;
}

.panel-header {
    font-size: 16px;
    font-weight: 700;
    color: #1e293b;
    margin-bottom: 20px;
    letter-spacing: -0.01em;
}

.panel-chart {
    flex: 1;
    min-height: 280px;
    width: 100%;
}

.panel-chart.gauge {
    min-height: 200px;
}

/* 异常组件专署刻画 */
.warning-panel {
    border-top: 4px solid #ef4444 !important;
    background: linear-gradient(to bottom, rgba(239, 68, 68, 0.05), rgba(255, 255, 255, 0)) !important;
}

.gauge-footer {
    text-align: center;
    font-size: 13px;
    font-weight: 600;
    color: #ef4444;
    margin-top: -20px;
    /* 压榨仪表盘底部空白 */
    background: rgba(239, 68, 68, 0.1);
    padding: 6px 12px;
    border-radius: 8px;
    width: fit-content;
    margin-inline: auto;
}

.gauge-footer.safe {
    color: #10b981;
    background: rgba(16, 185, 129, 0.1);
}

.danger-badge {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
    padding: 4px 10px;
    border-radius: 100px;
    font-weight: 700;
    font-size: 13px;
    box-shadow: 0 2px 4px rgba(239, 68, 68, 0.1);
}

@media screen and (max-width: 1440px) {
    .bento-sub-grid {
        grid-template-columns: repeat(2, 1fr);
    }
}

@media screen and (max-width: 1024px) {
    .bento-main-grid {
        grid-template-columns: 1fr;
    }
}
</style>
