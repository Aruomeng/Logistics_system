<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import request from '../../utils/request'

const searchForm = reactive({ module: '', operatorName: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(15)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await request.get('/log/list', {
      params: { page: currentPage.value, size: pageSize.value, ...searchForm },
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}
onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.module = ''; searchForm.operatorName = ''; handleSearch() }
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>操作日志</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.module" placeholder="操作模块" clearable style="width: 160px;" />
        <el-input v-model="searchForm.operatorName" placeholder="操作人" clearable style="width: 140px;" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border size="small">
        <el-table-column prop="module" label="模块" width="100" />
        <el-table-column prop="operationType" label="操作类型" width="80" />
        <el-table-column prop="description" label="描述" min-width="160" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="操作人" width="100" />
        <el-table-column prop="operatorIp" label="IP" width="130" />
        <el-table-column prop="method" label="方法" min-width="200" show-overflow-tooltip />
        <el-table-column prop="costTime" label="耗时(ms)" width="90" />
        <el-table-column prop="status" label="结果" width="70">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '成功' : '失败'
              }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="操作时间" width="170" />
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[15, 30, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>
