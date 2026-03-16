<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { myDemandList, addDemand, updateDemand, onlineDemand, offlineDemand, closeDemand, getAcceptList, confirmAccept, rejectAccept } from '../../api/demand'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'

const searchForm = reactive({ title: '', demandStatus: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const statusMap: Record<number, string> = { 0: '待发布', 1: '已发布', 2: '承接中', 3: '已对接', 4: '已完成', 99: '已关闭' }
const statusTag: Record<number, string> = { 0: 'info', 1: 'success', 2: 'warning', 3: 'primary', 4: '', 99: 'danger' }

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const demandForm = reactive({
    demandId: 0, title: '', description: '', category: '', subCategory: '',
    quantity: 0, expectPrice: 0, expectTime: '', address: '', qualificationRequire: ''
})

// 承接弹窗
const acceptVisible = ref(false)
const acceptList = ref<any[]>([])
const acceptLoading = ref(false)
const currentDemandId = ref(0)

const fetchData = async () => {
    loading.value = true
    try {
        const res: any = await myDemandList({ page: currentPage.value, size: pageSize.value, ...searchForm })
        tableData.value = res.data
        total.value = res.total || 0
    } finally { loading.value = false }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.title = ''; searchForm.demandStatus = undefined; handleSearch() }

const handleAdd = () => {
    dialogTitle.value = '发布采购需求'
    isEdit.value = false
    Object.assign(demandForm, { demandId: 0, title: '', description: '', category: '', subCategory: '', quantity: 0, expectPrice: 0, expectTime: '', address: '', qualificationRequire: '' })
    dialogVisible.value = true
}

const handleEdit = (row: any) => {
    dialogTitle.value = '编辑需求'
    isEdit.value = true
    Object.assign(demandForm, row)
    dialogVisible.value = true
}

const handleSubmit = async () => {
    if (!demandForm.title.trim()) { ElMessage.warning('请输入需求标题'); return }
    if (isEdit.value) {
        await updateDemand(demandForm)
        ElMessage.success('编辑成功')
    } else {
        await addDemand(demandForm)
        ElMessage.success('发布成功，等待审核')
    }
    dialogVisible.value = false
    fetchData()
}

const handleOnline = async (row: any) => {
    await onlineDemand(row.demandId)
    ElMessage.success('已上架')
    fetchData()
}

const handleOffline = async (row: any) => {
    try {
        await secureConfirm(`确定下架您的需求 <b>${row.title}</b> 吗？`, '危险操作验证')
        await offlineDemand(row.demandId)
        ElMessage.success('安全指令已放行，已下架')
        fetchData()
    } catch (error) {
        // 中止执行
    }
}

const handleClose = async (row: any) => {
    const { value } = await ElMessageBox.prompt('请输入关闭原因', '关闭需求', { confirmButtonText: '确定', cancelButtonText: '取消' })
    await closeDemand(row.demandId, value)
    ElMessage.success('已关闭')
    fetchData()
}

const showAccepts = async (row: any) => {
    currentDemandId.value = row.demandId
    acceptVisible.value = true
    acceptLoading.value = true
    try {
        const res: any = await getAcceptList({ page: 1, size: 50, demandId: row.demandId })
        acceptList.value = res.data || []
    } finally { acceptLoading.value = false }
}

const handleConfirmAccept = async (accept: any) => {
    await ElMessageBox.confirm(`确定选择 "${accept.supplierName}" 的承接方案？`, '确认承接', { type: 'info' })
    await confirmAccept(accept.acceptId)
    ElMessage.success('已确认承接')
    showAccepts({ demandId: currentDemandId.value })
    fetchData()
}

const handleRejectAccept = async (accept: any) => {
    const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回承接', {})
    await rejectAccept(accept.acceptId, value)
    ElMessage.success('已驳回')
    showAccepts({ demandId: currentDemandId.value })
}

const acceptStatusMap: Record<number, string> = { 0: '待审核', 1: '已确认', 2: '已驳回' }
const acceptTagType: Record<number, string> = { 0: 'warning', 1: 'success', 2: 'danger' }
</script>

<template>
    <div class="page-container">
        <div class="card-content">
            <div class="page-header">
                <h2>我的需求</h2>
                <el-button type="primary" @click="handleAdd"><el-icon>
                        <Plus />
                    </el-icon> 发布需求</el-button>
            </div>
            <div class="search-bar">
                <el-input v-model="searchForm.title" placeholder="需求标题" clearable style="width: 200px;" />
                <el-select v-model="searchForm.demandStatus" placeholder="需求状态" clearable style="width: 140px;">
                    <el-option label="待发布" :value="0" /><el-option label="已发布" :value="1" />
                    <el-option label="承接中" :value="2" /><el-option label="已对接" :value="3" />
                    <el-option label="已完成" :value="4" /><el-option label="已关闭" :value="99" />
                </el-select>
                <el-button type="primary" @click="handleSearch">搜索</el-button>
                <el-button @click="handleReset">重置</el-button>
            </div>
            <el-table :data="tableData" v-loading="loading" stripe border>
                <el-table-column prop="demandNo" label="需求编号" width="200" />
                <el-table-column prop="title" label="标题" min-width="160" />
                <el-table-column prop="category" label="品类" width="100" />
                <el-table-column prop="quantity" label="数量(kg)" width="100" />
                <el-table-column prop="expectPrice" label="期望单价" width="100">
                    <template #default="{ row }"><span v-if="row.expectPrice">¥{{ row.expectPrice }}</span><span
                            v-else>面议</span></template>
                </el-table-column>
                <el-table-column prop="expectTime" label="期望交付" width="120" />
                <el-table-column prop="demandStatus" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="(statusTag[row.demandStatus] as any)" size="small">{{ statusMap[row.demandStatus]
                        }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="auditStatus" label="审核" width="80">
                    <template #default="{ row }">
                        <el-tag :type="row.auditStatus === 1 ? 'success' : row.auditStatus === 2 ? 'danger' : 'info'"
                            size="small">
                            {{ row.auditStatus === 0 ? '待审' : row.auditStatus === 1 ? '通过' : '驳回' }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="280" fixed="right">
                    <template #default="{ row }">
                        <el-button v-if="row.demandStatus <= 1" size="small" type="primary" link
                            @click="handleEdit(row)">编辑</el-button>
                        <el-button v-if="row.auditStatus === 1 && row.demandStatus === 0" size="small" type="success"
                            link @click="handleOnline(row)">上架</el-button>
                        <el-button v-if="row.demandStatus === 1" size="small" type="info" link
                            @click="handleOffline(row)">下架</el-button>
                        <el-button v-if="row.demandStatus >= 2 && row.demandStatus < 99" size="small" type="warning"
                            link @click="showAccepts(row)">查看承接</el-button>
                        <el-button v-if="row.demandStatus < 4 && row.demandStatus !== 99" size="small" type="danger"
                            link @click="handleClose(row)">关闭</el-button>
                    </template>
                </el-table-column>
            </el-table>
            <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
                <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
                    :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
                    @current-change="fetchData" />
            </div>
        </div>

        <!-- 发布/编辑弹窗 -->
        <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
            <el-form :model="demandForm" label-width="100px">
                <el-form-item label="需求标题"><el-input v-model="demandForm.title" placeholder="请输入需求标题" /></el-form-item>
                <el-form-item label="详细描述"><el-input v-model="demandForm.description" type="textarea"
                        :rows="4" /></el-form-item>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="产品品类"><el-input
                                v-model="demandForm.category" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="子品类"><el-input
                                v-model="demandForm.subCategory" /></el-form-item></el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="采购数量(kg)"><el-input-number v-model="demandForm.quantity"
                                :min="0" style="width: 100%;" /></el-form-item></el-col>
                    <el-col :span="12"><el-form-item label="期望单价"><el-input-number v-model="demandForm.expectPrice"
                                :min="0" :precision="2" style="width: 100%;" /></el-form-item></el-col>
                </el-row>
                <el-row :gutter="16">
                    <el-col :span="12"><el-form-item label="期望交付"><el-date-picker v-model="demandForm.expectTime"
                                type="date" value-format="YYYY-MM-DD" style="width: 100%;" /></el-form-item></el-col>
                </el-row>
                <el-form-item label="交付地址"><el-input v-model="demandForm.address" /></el-form-item>
                <el-form-item label="资质要求"><el-input v-model="demandForm.qualificationRequire" type="textarea"
                        :rows="2" /></el-form-item>
            </el-form>
            <template #footer>
                <el-button @click="dialogVisible = false">取消</el-button>
                <el-button type="primary" @click="handleSubmit">确定</el-button>
            </template>
        </el-dialog>

        <!-- 承接申请列表弹窗 -->
        <el-dialog v-model="acceptVisible" title="承接申请列表" width="800px">
            <el-table :data="acceptList" v-loading="acceptLoading" stripe border>
                <el-table-column prop="supplierName" label="供应方" width="120" />
                <el-table-column prop="acceptScheme" label="承接方案" min-width="200" show-overflow-tooltip />
                <el-table-column prop="offer" label="报价(元/kg)" width="110" />
                <el-table-column prop="deliveryTime" label="承诺交付" width="120" />
                <el-table-column prop="acceptStatus" label="状态" width="100">
                    <template #default="{ row }">
                        <el-tag :type="(acceptTagType[row.acceptStatus] as any)" size="small">{{
                            acceptStatusMap[row.acceptStatus] }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="160" fixed="right">
                    <template #default="{ row }">
                        <template v-if="row.acceptStatus === 0">
                            <el-button size="small" type="success" link @click="handleConfirmAccept(row)">确认</el-button>
                            <el-button size="small" type="danger" link @click="handleRejectAccept(row)">驳回</el-button>
                        </template>
                        <span v-else style="color: #909399;">{{ acceptStatusMap[row.acceptStatus] }}</span>
                    </template>
                </el-table-column>
            </el-table>
        </el-dialog>
    </div>
</template>
