<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo, updateUserInfo, modifyPassword } from '../../api/user'
import { useUserStore } from '../../stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false)

const profileForm = reactive({
  userId: 0,
  username: '',
  phone: '',
  email: '',
  qq: '',
  gender: 0 as number,
})

const passwordForm = reactive({
  oldPwd: '',
  newPwd: '',
  confirmPwd: '',
})

const activeTab = ref('profile')
const profileRef = ref()
const passwordRef = ref()

const passwordRules = {
  oldPwd: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPwd: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' },
  ],
  confirmPwd: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPwd) {
          callback(new Error('两次输入密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const res: any = await getUserInfo()
    Object.assign(profileForm, res.data)
  } finally {
    loading.value = false
  }
}

onMounted(fetchProfile)

const handleUpdateProfile = async () => {
  await updateUserInfo(profileForm)
  ElMessage.success('个人信息更新成功')
  fetchProfile()
}

const handleModifyPassword = async () => {
  await passwordRef.value?.validate()
  await modifyPassword({ oldPwd: passwordForm.oldPwd, newPwd: passwordForm.newPwd })
  ElMessage.success('密码修改成功，请重新登录')
  userStore.logout()
  window.location.href = '/login'
}
</script>

<template>
  <div class="page-container">
    <div class="card-content">
      <h2 style="margin-bottom: 20px;">个人中心</h2>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="profile">
          <el-form ref="profileRef" :model="profileForm" label-width="80px" style="max-width: 500px;"
            v-loading="loading">
            <el-form-item label="用户名">
              <el-input :value="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="角色">
              <el-tag>{{ userStore.roleName }}</el-tag>
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="QQ">
              <el-input v-model="profileForm.qq" placeholder="请输入QQ号" />
            </el-form-item>
            <el-form-item label="性别">
              <el-radio-group v-model="profileForm.gender">
                <el-radio :value="0">未知</el-radio>
                <el-radio :value="1">男</el-radio>
                <el-radio :value="2">女</el-radio>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="修改密码" name="password">
          <el-form ref="passwordRef" :model="passwordForm" :rules="passwordRules" label-width="100px"
            style="max-width: 500px;">
            <el-form-item label="原密码" prop="oldPwd">
              <el-input v-model="passwordForm.oldPwd" type="password" show-password placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPwd">
              <el-input v-model="passwordForm.newPwd" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPwd">
              <el-input v-model="passwordForm.confirmPwd" type="password" show-password placeholder="请确认新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleModifyPassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>
