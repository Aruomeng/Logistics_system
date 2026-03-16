<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { adminDemandList, auditDemand, forceOfflineDemand } from '../../api/demand'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'

const searchForm = reactive({ title: '', demandStatus: undefined as number | undefined, publisherName: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 0: '待发布', 1: '已发布', 2: '承接中', 3: '已对接', 4: '已完成', 99: '已关闭' }
const statusTag: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'primary', 4: '', 99: 'danger' }
const auditMap: Record<number, string> = { 0: '待审核', 1: '已通过', 2: '已驳回' }
const auditTagType: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }

const fetchData = async () => {
    loading.value = true
    try {
        const res: any = await adminDemandList({ page: currentPage.value, size: pageSize.value, ...searchForm })
        tableData.value = res.data
        total.value = res.total || 0
    } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.title = ''; searchForm.demandStatus = undefined; searchForm.publisherName = ''; handleSearch() }

const handleAudit = async (row: any, status: number) => {
    const label = status === 1 ? '通过' : '驳回'
    let remark = ''
    if (status === 2) {
        const { value } = await ElMessageBox.prompt('请输入驳回原因', '审核驳回', {})
        remark = value
    } else {
        await ElMessageBox.confirm(`确定通过需求 "${row.title}" 的审核？`, '审核确认', { type: 'info' })
    }
    await auditDemand(row.demandId, status, remark)
    ElMessage.success(`已${label}`)
    fetchData()
}

const handleForceOffline = async (row: any) => {
    try {
        await secureConfirm(`确定强制下架需求 <b>${row.title}</b> 吗？`, '危险操作验证')
        await forceOfflineDemand(row.demandId)
        ElMessage.success('安全指令已放行，已强制下架')
        fetchData()
    } catch (error) {
        // 中止执行
    }
}
</script>

<template>
    <div class="page-container">
        <div class="card-content">
            <div class="page-header">
                <h2>需求审核</h2>
            </div>
            <div class="search-bar">
                <el-input v-model="searchForm.title" placeholder="需求标题" clearable style="width: 200px;" />
                <el-input v-model="searchForm.publisherName" placeholder="发布方" clearable style="width: 140px;" />
                <el-select v-model="searchForm.demandStatus" placeholder="需求状态" clearable style="width: 140px;">
                    <el-option label="待发布" :value="0" /><el-option label="已发布" :value="1" />
                    <el-option label="承接中" :value="2" /><el-option label="已对接" :value="3" />
                    <el-option label="已关闭" :value="99" />
                </el-select>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe border>
                <el-table-column prop="demandNo" label="需求编号" width="200" />
                <el-table-column prop="title" label="标题" min-width="160" />
                <el-table-column prop="publisherName" label="发布方" width="120" />
                <el-table-column prop="category" label="品类" width="90" />
                <el-table-column prop="quantity" label="数量(kg)" width="90" />
                <el-table-column prop="expectPrice" label="期望价" width="90" />
                <el-table-column prop="auditStatus" label="审核" width="90">
                    <template #default="{ row }">
                        <el-tag :type="(auditTagType[row.auditStatus] as any)" size="small">{{ auditMap[row.auditStatus]
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="demandStatus" label="状态" width="90">
                    <template #default="{ row }">
                        <el-tag :type="(statusTag[row.demandStatus] as any)" size="small">{{ statusMap[row.demandStatus]
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createTime" label="发布时间" width="170" />
                <el-table-column label="操作" width="220" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="row.auditStatus === 0" size="small" type="success" link
                            @click="handleAudit(row, 1)">通过</el-button>
                        <el-button v-if="row.auditStatus === 0" size="small" type="danger" link
                            @click="handleAudit(row, 2)">驳回</el-button>
                        <el-button v-if="row.demandStatus === 1 || row.demandStatus === 2" size="small" type="warning"
                            link @click="handleForceOffline(row)">强制下架</el-button>
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
