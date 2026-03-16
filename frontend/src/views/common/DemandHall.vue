<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { commonDemandList, getDemandDetail, addAccept } from '../../api/demand'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const searchForm = reactive({ title: '', category: '', region: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 1: '已发布', 2: '承接中' }

// 详情弹窗
const detailVisible = ref(false)
const demandDetail = ref<any>({})
const acceptsList = ref<any[]>([])

// 承接弹窗
const acceptDialogVisible = ref(false)
const acceptForm = reactive({
    demandId: 0, acceptScheme: '', offer: 0, deliveryTime: '', qualificationInfo: ''
})

const fetchData = async () => {
    loading.value = true
    try {
        const res: any = await commonDemandList({ page: currentPage.value, size: pageSize.value, ...searchForm })
        tableData.value = res.data
        total.value = res.total || 0
    } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.title = ''; searchForm.category = ''; searchForm.region = ''; handleSearch() }

const showDetail = async (row: any) => {
    const res: any = await getDemandDetail(row.demandId)
    demandDetail.value = res.data.demand
    acceptsList.value = res.data.accepts || []
    detailVisible.value = true
}

const openAcceptDialog = (row: any) => {
    Object.assign(acceptForm, { demandId: row.demandId, acceptScheme: '', offer: 0, deliveryTime: '', qualificationInfo: '' })
    acceptDialogVisible.value = true
}

const handleAcceptSubmit = async () => {
    if (!acceptForm.acceptScheme.trim()) { ElMessage.warning('请填写承接方案'); return }
    await addAccept(acceptForm)
    ElMessage.success('承接申请已提交')
    acceptDialogVisible.value = false
    fetchData()
}
</script>

<template>
    <div class="page-container">
        <div class="card-content">
            <div class="page-header">
                <h2>需求大厅</h2>
            </div>
            <div class="search-bar">
                <el-input v-model="searchForm.title" placeholder="需求标题" clearable style="width: 200px;" />
                <el-input v-model="searchForm.category" placeholder="产品品类" clearable style="width: 140px;" />
                <el-input v-model="searchForm.region" placeholder="区域" clearable style="width: 140px;" />
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe border>
                <el-table-column prop="title" label="需求标题" min-width="180">
                    <template #default="{ row }">
                        <el-tag v-if="row.isTop === 1" type="danger" size="small" style="margin-right: 6px;">置顶</el-tag>
                        {{ row.title }}
                    </template>
                </el-table-column>
                <el-table-column prop="publisherName" label="发布方" width="120" />
                <el-table-column prop="category" label="品类" width="100" />
                <el-table-column prop="quantity" label="数量(kg)" width="100" />
                <el-table-column prop="expectPrice" label="期望单价" width="100">
                    <template #default="{ row }"><span v-if="row.expectPrice"
                            style="color: #F56C6C; font-weight: 600;">¥{{ row.expectPrice }}</span><span
                            v-else>面议</span></template>
                </el-table-column>
                <el-table-column prop="expectTime" label="期望交付" width="120" />
                <el-table-column prop="address" label="交付地址" width="150" show-overflow-tooltip />
                <el-table-column prop="demandStatus" label="状态" width="90">
                    <template #default="{ row }">
                        <el-tag :type="row.demandStatus === 1 ? 'success' : 'warning'" size="small">{{
                            statusMap[row.demandStatus] }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                    <template #default="{ row }">
                        <el-button size="small" type="primary" link @click="showDetail(row)">详情</el-button>
                        <el-button v-if="userStore.roleType === 2" size="small" type="success" link
                            @click="openAcceptDialog(row)">承接</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
                    :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
                    @current-change="fetchData" />
            </div>
        </div>

        <!-- 详情弹窗 -->
        <el-dialog v-model="detailVisible" title="需求详情" width="720px">
            <el-descriptions :column="2" border>
                <el-descriptions-item label="标题" :span="2">{{ demandDetail.title }}</el-descriptions-item>
                <el-descriptions-item label="品类">{{ demandDetail.category }}</el-descriptions-item>
                <el-descriptions-item label="数量">{{ demandDetail.quantity }} kg</el-descriptions-item>
                <el-descriptions-item label="期望单价"><span v-if="demandDetail.expectPrice"
                        style="color: #F56C6C; font-weight: 700;">¥{{ demandDetail.expectPrice }}</span><span
                        v-else>面议</span></el-descriptions-item>
                <el-descriptions-item label="期望交付">{{ demandDetail.expectTime }}</el-descriptions-item>
                <el-descriptions-item label="交付地址" :span="2">{{ demandDetail.address }}</el-descriptions-item>
                <el-descriptions-item label="资质要求" :span="2">{{ demandDetail.qualificationRequire || '无'
                }}</el-descriptions-item>
                <el-descriptions-item label="描述" :span="2">
                    <div style="white-space: pre-wrap;">{{ demandDetail.description }}</div>
                </el-descriptions-item>
            </el-descriptions>
            <template v-if="acceptsList.length > 0">
                <h4 style="margin: 16px 0 8px;">承接申请 ({{ acceptsList.length }})</h4>
                <el-table :data="acceptsList" border size="small">
                    <el-table-column prop="supplierName" label="供应方" width="120" />
                    <el-table-column prop="offer" label="报价" width="80" />
                    <el-table-column prop="deliveryTime" label="交付时间" width="110" />
                    <el-table-column prop="acceptScheme" label="方案" show-overflow-tooltip />
                    <el-table-column prop="acceptStatus" label="状态" width="80">
                        <template #default="{ row }">
                            <el-tag
                                :type="row.acceptStatus === 1 ? 'success' : row.acceptStatus === 2 ? 'danger' : 'warning'"
                                size="small">
                                {{ row.acceptStatus === 0 ? '待审' : row.acceptStatus === 1 ? '已确认' : '已驳回' }}
                            </el-tag>
                        </template>
                    </el-table-column>
                </el-table>
            </template>
        </el-dialog>

        <!-- 承接申请弹窗（供应方） -->
        <el-dialog v-model="acceptDialogVisible" title="提交承接申请" width="600px">
            <el-form :model="acceptForm" label-width="100px">
                <el-form-item label="承接方案"><el-input v-model="acceptForm.acceptScheme" type="textarea" :rows="4"
                        placeholder="请详细描述您的承接方案" /></el-form-item>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="报价(元/kg)"><el-input-number v-model="acceptForm.offer"
                                :min="0" :precision="2" style="width: 100%;" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="交付时间"><el-date-picker v-model="acceptForm.deliveryTime"
                                type="date" value-format="YYYY-MM-DD" style="width: 100%;" /></el-form-item></el-col>
                </el-row>
                <el-form-item label="资质说明"><el-input v-model="acceptForm.qualificationInfo" type="textarea"
                        :rows="2" /></el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="acceptDialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleAcceptSubmit">提交申请</el-button>
            </template>
        </el-dialog>
    </div>
</template>
