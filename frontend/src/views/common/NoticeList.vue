<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { commonListNotices } from '../../api/notice'

const router = useRouter()
const searchForm = reactive({ title: '' })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await commonListNotices({
      page: currentPage.value, size: pageSize.value, title: searchForm.title,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }

const goDetail = (id: number) => router.push(`/system/notice/${id}`)
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>系统公告</h2>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="搜索公告" clearable style="width: 260px;"
          @keyup.enter="handleSearch" />
        <el-button type="primary" @click="handleSearch">搜索</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe style="cursor: pointer;"
        @row-click="(row: any) => goDetail(row.noticeId)">
        <el-table-column prop="title" label="标题" min-width="300">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small" style="margin-right: 6px;">置顶</el-tag>
            <span style="color: #409EFF;">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="createTime" label="发布时间" width="170" />
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @current-change="fetchData" />
      </div>
    </div>
  </div>
</template>
