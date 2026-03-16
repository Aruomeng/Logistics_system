<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { listUsers, addUser, adminUpdateUser, deleteUser, resetPassword } from '../../api/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { secureConfirm } from '../../utils/secureConfirm'

// 搜索条件
const searchForm = reactive({
  username: '',
  roleType: undefined as number | undefined,
  status: undefined as number | undefined,
})

// 表格数据
const tableData = ref<any[]>([])
const total = ref(0)
const currentPage = ref(1)
const pageSize = ref(10)
const loading = ref(false)

// 弹窗
const dialogVisible = ref(false)
const dialogTitle = ref('')
const isEdit = ref(false)
const formRef = ref()
const userForm = reactive({
  userId: 0,
  username: '',
  password: '',
  roleType: 4 as number,
  phone: '',
  email: '',
  status: 1,
})

const roleOptions = [
  { label: '管理员', value: 1 },
  { label: '供应方', value: 2 },
  { label: '物流方', value: 3 },
  { label: '消费者', value: 4 },
]

const statusMap: Record<number, string> = { 0: '禁用', 1: '启用' }
const roleMap: Record<number, string> = { 1: '管理员', 2: '供应方', 3: '物流方', 4: '消费者' }

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  roleType: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

const fetchData = async () => {
  loading.value = true
  try {
    const res: any = await listUsers({
      page: currentPage.value,
      size: pageSize.value,
      ...searchForm,
    })
    tableData.value = res.data
    total.value = res.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(fetchData)

const handleSearch = () => {
  currentPage.value = 1
  fetchData()
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.roleType = undefined
  searchForm.status = undefined
  handleSearch()
}

const handleAdd = () => {
  dialogTitle.value = '新增用户'
  isEdit.value = false
  Object.assign(userForm, { userId: 0, username: '', password: '', roleType: 4, phone: '', email: '', status: 1 })
  dialogVisible.value = true
}

const handleEdit = (row: any) => {
  dialogTitle.value = '编辑用户'
  isEdit.value = true
  Object.assign(userForm, {
    userId: row.userId,
    username: row.username,
    password: '',
    roleType: row.roleType,
    phone: row.phone || '',
    email: row.email || '',
    status: row.status,
  })
  dialogVisible.value = true
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  if (isEdit.value) {
    await adminUpdateUser(userForm)
    ElMessage.success('编辑成功')
  } else {
    await addUser(userForm)
    ElMessage.success('新增成功')
  }
  dialogVisible.value = false
  fetchData()
}

const handleDelete = async (row: any) => {
  try {
    await secureConfirm(`确定要永久删除用户 <b>${row.username}</b> 吗？`, '危险操作验证')
    await deleteUser(row.userId)
    ElMessage.success('安全指令已放行，删除成功')
    fetchData()
  } catch (error) {
    // 已取消或密码验证失败，中止执行
  }
}

const handleResetPwd = async (row: any) => {
  await ElMessageBox.confirm(`确定要重置 "${row.username}" 的密码为 123456 吗？`, '提示', { type: 'warning' })
  await resetPassword(row.userId)
  ElMessage.success('密码已重置为 123456')
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <div class="page-header">
        <h2>用户管理</h2>
        <el-button type="primary" @click="handleAdd">
          <el-icon>
            <Plus />
          </el-icon> 新增用户
        </el-button>
      </div>

      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input v-model="searchForm.username" placeholder="用户名" clearable style="width: 200px;" />
        <el-select v-model="searchForm.roleType" placeholder="角色" clearable style="width: 140px;">
          <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
        <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px;">
          <el-option label="启用" :value="1" />
          <el-option label="禁用" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe border style="width: 100%;">
        <el-table-column prop="userId" label="ID" width="180" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="roleType" label="角色" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.roleType === 1 ? 'danger' : row.roleType === 2 ? 'success' : row.roleType === 3 ? 'warning' : 'info'">
              {{ roleMap[row.roleType] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ statusMap[row.status] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="260" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button size="small" type="primary" link @click="handleEdit(row)">编辑</el-button>
              <el-button size="small" type="warning" link @click="handleResetPwd(row)">重置密码</el-button>
              <el-button size="small" type="danger" link @click="handleDelete(row)"
                :disabled="row.roleType === 1">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="display: flex; justify-content: flex-end; margin-top: 16px;">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="total"
          :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next, jumper" @size-change="fetchData"
          @current-change="fetchData" />
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="userForm" :rules="rules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" :disabled="isEdit" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="userForm.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="roleType">
          <el-select v-model="userForm.roleType" :disabled="isEdit" style="width: 100%;">
            <el-option v-for="r in roleOptions" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="isEdit" label="状态">
          <el-switch v-model="userForm.status" :active-value="1" :inactive-value="0" active-text="启用"
            inactive-text="禁用" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>
