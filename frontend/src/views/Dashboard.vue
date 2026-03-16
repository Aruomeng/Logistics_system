<script setup lang="ts">
import { useUserStore } from '../stores/user'

const userStore = useUserStore()
</script>

<template>
  <div class="page-container">
    <el-row :gutter="16">
      <el-col :span="24">
        <div class="card-content welcome">
          <h2>👋 欢迎回来，{{ userStore.userInfo?.username }}</h2>
          <p>当前角色：<el-tag>{{ userStore.roleName }}</el-tag></p>
        </div>
      </el-col>
    </el-row>
    <el-row :gutter="16" style="margin-top: 16px;">
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #409EFF;">
          <div class="stat-value">--</div>
          <div class="stat-label">供应信息</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #67C23A;">
          <div class="stat-value">--</div>
          <div class="stat-label">进行中订单</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #E6A23C;">
          <div class="stat-value">--</div>
          <div class="stat-label">待处理</div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card" style="border-left: 4px solid #F56C6C;">
          <div class="stat-value">--</div>
          <div class="stat-label">系统公告</div>
        </div>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.welcome {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  padding: 32px;
  border-radius: 12px;
}
.welcome h2 { font-size: 24px; margin-bottom: 8px; }
.welcome p { font-size: 14px; opacity: 0.9; }
.welcome .el-tag { color: #fff; background: rgba(255,255,255,0.2); border: none; }

.stat-card {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.stat-value { font-size: 28px; font-weight: 700; color: #303133; }
.stat-label { font-size: 14px; color: #909399; margin-top: 4px; }
</style>
