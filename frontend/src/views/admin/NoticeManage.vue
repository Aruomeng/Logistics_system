<script setup lang="ts">
import { ref, reactive, onMounted, shallowRef, onBeforeUnmount } from 'vue'
import { adminListNotices, addNotice, updateNotice, deleteNotice, toggleTop } from '../../api/notice'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'
import '@wangeditor/editor/dist/css/style.css'
import { Editor, Toolbar } from '@wangeditor/editor-for-vue'

const searchForm = reactive({ title: '', isTop: undefined as number | undefined })
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const noticeForm = reactive({
  noticeId: 0,
  title: '',
  content: '',
  isTop: 0,
})
const fileList = ref<any[]>([])

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await adminListNotices({
      page: currentPage.value, size: pageSize.value, ...searchForm,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

// 富文本配置
const editorRef = shallowRef()
const mode = 'default'
const toolbarConfig = {}
const editorConfig = { placeholder: '请输入公告内容...' }

// 组件销毁时，也及时销毁编辑器
onBeforeUnmount(() => {
  const editor = editorRef.value
  if (editor == null) return
  editor.destroy()
})
const handleCreated = (editor: any) => {
  editorRef.value = editor
}

onMounted(fetchData)

const handleSearch = () => { currentPage.value = 1; fetchData() }
const handleReset = () => { searchForm.title = ''; searchForm.isTop = undefined; handleSearch() }

const handleAdd = () => {
  dialogTitle.value = '发布公告'
  isEdit.value = false
  Object.assign(noticeForm, { noticeId: 0, title: '', content: '', isTop: 0 })
  fileList.value = []
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑公告'
  isEdit.value = true
  Object.assign(noticeForm, { noticeId: row.noticeId, title: row.title, content: row.content, isTop: row.isTop })
  fileList.value = []
  dialogVisible.value = true
}

const handleSubmit = async () => {
  if (!noticeForm.title.trim()) { ElMessage.warning('请输入公告标题'); return }

  // wangeditor 空的内容是 '<p><br></p>'
  if (!noticeForm.content || noticeForm.content === '<p><br></p>') {
    ElMessage.warning('请输入公告内容');
    return
  }

  const formData = new FormData()
  if (isEdit.value) formData.append('noticeId', String(noticeForm.noticeId))
  formData.append('title', noticeForm.title)
  formData.append('content', noticeForm.content)
  formData.append('isTop', String(noticeForm.isTop))
  fileList.value.forEach((f: any) => formData.append('attachments', f.raw))

  if (isEdit.value) {
    await updateNotice(formData)
    ElMessage.success('编辑成功')
  } else {
    await addNotice(formData)
    ElMessage.success('发布成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row: any) => {
  try {
    await secureConfirm(`确定要永久删除公告 <b>${row.title}</b> 吗？`, '危险操作验证')
    await deleteNotice(row.noticeId)
    ElMessage.success('安全指令已放行，删除成功')
    fetchData()
  } catch (error) {
    // 中止执行
  }
}

const handleToggleTop = async (row: any) => {
  const newTop = row.isTop === 1 ? 0 : 1
  await toggleTop(row.noticeId, newTop)
  ElMessage.success(newTop === 1 ? '已置顶' : '已取消置顶')
  fetchData()
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>公告管理</h2>
        <el-button type="primary" @click="handleAdd"><el-icon>
            <Plus />
          </el-icon> 发布公告</el-button>
      </div>
      <div class="search-bar">
        <el-input v-model="searchForm.title" placeholder="公告标题" clearable style="width: 200px;" />
        <el-select v-model="searchForm.isTop" placeholder="是否置顶" clearable style="width: 120px;">
          <el-option label="置顶" :value="1" /><el-option label="普通" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="title" label="标题" min-width="200">
          <template #default="{ row }">
            <el-tag v-if="row.isTop === 1" type="danger" size="small" style="margin-right: 6px;">置顶</el-tag>
            {{ row.title }}
          </template>
        </el-table-column>
        <el-table-column prop="publisherName" label="发布人" width="120" />
        <el-table-column prop="createTime" label="发布时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button size="small" :type="row.isTop === 1 ? 'info' : 'warning'" link @click="handleToggleTop(row)">
              {{ row.isTop === 1 ? '取消置顶' : '置顶' }}
            </el-button>
            <el-button size="small" type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form :model="noticeForm" label-width="80px">
        <el-form-item label="标题">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容">
          <div style="border: 1px solid #dcdfe6; width: 100%; z-index: 100;">
            <Toolbar style="border-bottom: 1px solid #dcdfe6" :editor="editorRef" :defaultConfig="toolbarConfig"
              :mode="mode" />
            <Editor style="height: 300px; overflow-y: hidden;" v-model="noticeForm.content"
              :defaultConfig="editorConfig" :mode="mode" @onCreated="handleCreated" />
          </div>
        </el-form-item>
        <el-form-item label="置顶">
          <el-switch v-model="noticeForm.isTop" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="附件">
          <el-upload v-model:file-list="fileList" :auto-upload="false" multiple>
            <el-button type="primary" plain><el-icon>
                <Upload />
              </el-icon> 选择文件</el-button>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
