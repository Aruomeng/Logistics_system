<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getSupplyDetail } from '../../api/supply'

const route = useRoute()
const router = useRouter()
const supply = ref<any>(null)
const trace = ref<any>(null)
const loading = ref(false)
const mobilePreviewVisible = ref(false)

const getQrUrl = () => {
  if (!supply.value || !supply.value.traceCode) return ''

  // 构造纯文本溯源卡片 (适配手机自带相机的直接文本读取)
  let text = `【智创农链 官方溯源】\n\n`
  text += `[商品档案]\n`
  text += `产品：${supply.value.productName}\n`
  text += `品类：${supply.value.category}\n`
  text += `产地：${supply.value.productionPlace}\n`
  text += `供应：${supply.value.supplierName}\n`
  text += `产期：${supply.value.productionDate || '--'}\n`
  text += `保质：${supply.value.shelfLife} 天\n`
  text += `真伪戳：${supply.value.traceCode}\n`

  if (trace.value) {
    text += `\n[流转节点]\n`
    traceSteps.forEach(step => {
      if (trace.value[step.key]) {
        text += `- ${step.label}: ${trace.value[step.key]}\n`
      }
    })
  }

  return `https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=${encodeURIComponent(text)}&margin=1`
}

onMounted(async () => {
  loading.value = true
  try {
    const code = route.params.code as string
    // code 可能是 supplyId 数字，也可能是 traceCode 字符串，统一交给 API 函数判断
    const res: any = await getSupplyDetail(code)
    supply.value = res.data.supply
    trace.value = res.data.trace
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
})

const traceSteps = [
  { key: 'seedInfo', label: '种苗信息', icon: 'Sunny' },
  { key: 'fertilizerInfo', label: '施肥记录', icon: 'Cherry' },
  { key: 'pesticideInfo', label: '农药使用', icon: 'Warning' },
  { key: 'harvestInfo', label: '采收记录', icon: 'Scissor' },
  { key: 'inspectionInfo', label: '质检报告', icon: 'CircleCheck' },
]

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/home')
  }
}
</script>

<template>
  <div class="page-container">
    <div class="card-content" v-loading="loading">
      <el-button link type="primary" @click="goBack" style="margin-bottom: 16px;">
        <el-icon>
          <ArrowLeft />
        </el-icon> 返回
      </el-button>

      <template v-if="supply">
        <el-row :gutter="24">
          <!-- 左：产品信息 -->
          <el-col :span="14">
            <h2 style="font-size: 22px; margin-bottom: 16px;">{{ supply.productName }}</h2>
            <el-descriptions :column="2" border>
              <el-descriptions-item label="品类">{{ supply.category }}</el-descriptions-item>
              <el-descriptions-item label="子品类">{{ supply.subCategory || '--' }}</el-descriptions-item>
              <el-descriptions-item label="产地">{{ supply.productionPlace }}</el-descriptions-item>
              <el-descriptions-item label="供应方">{{ supply.supplierName }}</el-descriptions-item>
              <el-descriptions-item label="生产日期">{{ supply.productionDate || '--' }}</el-descriptions-item>
              <el-descriptions-item label="保质期">{{ supply.shelfLife }} 天</el-descriptions-item>
              <el-descriptions-item label="单价">
                <span style="color: #F56C6C; font-weight: 700; font-size: 18px;">¥{{ supply.price }}</span> /kg
              </el-descriptions-item>
              <el-descriptions-item label="库存">{{ supply.stock }} kg</el-descriptions-item>
              <el-descriptions-item label="溯源码">
                <el-tag type="success">{{ supply.traceCode }}</el-tag>
              </el-descriptions-item>
            </el-descriptions>
          </el-col>
          <!-- 右：溯源二维码 -->
          <el-col :span="10" style="text-align: center;">
            <div
              style="background: var(--el-fill-color-lighter); border-radius: 12px; padding: 24px; border: 1px solid var(--el-border-color-lighter);">
              <div v-if="supply.traceCode"
                style="display: inline-block; background: #ffffff; padding: 12px; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05);">
                <img :src="getQrUrl()" alt="溯源二维码" style="width: 180px; height: 180px; display: block;" />
              </div>
              <div v-else
                style="width: 200px; height: 200px; display: flex; align-items: center; justify-content: center; margin: 0 auto; background: var(--el-fill-color-dark); border-radius: 12px; color: var(--el-text-color-secondary);">
                暂无溯源码
              </div>
              <div v-if="supply.traceCode" style="margin-top: 20px;">
                <el-button type="primary" plain round icon="FullScreen" @click="mobilePreviewVisible = true">
                  模拟手机扫码预览
                </el-button>
                <p style="margin-top: 12px; color: var(--el-text-color-secondary); font-size: 13px;">(系统未部署前，可点击直接体验沉浸式
                  H5 溯源卡片)</p>
              </div>
            </div>
          </el-col>
        </el-row>

        <!-- 溯源时间线 -->
        <template v-if="trace">
          <el-divider />
          <h3 style="margin-bottom: 16px;">🔍 溯源信息</h3>
          <el-timeline>
            <el-timeline-item v-for="step in traceSteps" :key="step.key" :timestamp="step.label" placement="top"
              :color="(trace as any)[step.key] ? '#67C23A' : '#e4e7ed'">
              <el-card shadow="hover" v-if="(trace as any)[step.key]">
                <p style="white-space: pre-wrap;">{{ (trace as any)[step.key] }}</p>
              </el-card>
              <span v-else style="color: #c0c4cc;">暂未记录</span>
            </el-timeline-item>
          </el-timeline>
        </template>
      </template>
    </div>

    <!-- 虚拟手机 H5 溯源沉浸式弹窗 -->
    <el-dialog v-model="mobilePreviewVisible" width="460px" :show-close="false" class="mobile-mock-dialog" center
      destroy-on-close>
      <div class="mock-phone-container">
        <!-- 手机硬件外壳与刘海屏 -->
        <div class="phone-notch"></div>
        <div class="phone-screen" v-if="supply">

          <!-- H5 头部 Header -->
          <div class="h5-header">
            <el-icon @click="mobilePreviewVisible = false" class="back-icon">
              <Close />
            </el-icon>
            <span>全链路溯源认证</span>
            <el-icon class="more-icon">
              <MoreFilled />
            </el-icon>
          </div>

          <!-- 核心内容区 -->
          <div class="h5-content">
            <!-- 顶部商品光环 -->
            <div class="product-halo">
              <div class="halo-bg"></div>
              <el-icon class="halo-icon">
                <Grape />
              </el-icon>
            </div>

            <h1 class="h5-title">{{ supply.productName }}</h1>
            <div class="h5-tags">
              <span class="h5-tag verified">
                <el-icon>
                  <CircleCheck />
                </el-icon>官方溯源认证
              </span>
              <span class="h5-tag">{{ supply.category }}</span>
              <span class="h5-tag">{{ supply.productionPlace }}</span>
            </div>

            <!-- 数据区块 -->
            <div class="h5-card">
              <div class="h5-card-header">基础信息</div>
              <div class="h5-info-row">
                <span class="label">溯源码</span>
                <span class="value code">{{ supply.traceCode }}</span>
              </div>
              <div class="h5-info-row">
                <span class="label">供应主体</span>
                <span class="value">{{ supply.supplierName }}</span>
              </div>
              <div class="h5-info-row">
                <span class="label">生产日期</span>
                <span class="value">{{ supply.productionDate || '--' }}</span>
              </div>
              <div class="h5-info-row">
                <span class="label">保质期</span>
                <span class="value">{{ supply.shelfLife }} 天</span>
              </div>
            </div>

            <!-- 溯源动线追踪 -->
            <div class="h5-card" v-if="trace">
              <div class="h5-card-header">全链路径捕捉</div>
              <div class="h5-timeline">
                <div class="h5-step" v-for="(step, index) in traceSteps" :key="step.key"
                  :class="{ 'active': (trace as any)[step.key], 'last': index === traceSteps.length - 1 }">
                  <div class="step-indicator">
                    <div class="dot"></div>
                    <div class="line" v-if="index !== traceSteps.length - 1"></div>
                  </div>
                  <div class="step-content">
                    <div class="step-title">{{ step.label }}</div>
                    <div class="step-desc" v-if="(trace as any)[step.key]">{{ (trace as any)[step.key] }}</div>
                    <div class="step-empty" v-else>等待节点上传数据</div>
                  </div>
                </div>
              </div>
            </div>

            <div class="h5-footer">由 智创农链系统 提供技术支持</div>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 虚拟手机壳外围 */
:deep(.mobile-mock-dialog) {
  background: transparent !important;
  box-shadow: none !important;
}

:deep(.mobile-mock-dialog .el-dialog__header) {
  display: none;
}

:deep(.mobile-mock-dialog .el-dialog__body) {
  padding: 0;
  display: flex;
  justify-content: center;
}

/* 拟真手机物理机身定型 */
.mock-phone-container {
  width: 375px;
  height: 760px;
  background-color: #000;
  border-radius: 48px;
  position: relative;
  box-shadow:
    0 0 0 10px #1e293b,
    0 40px 80px -20px rgba(0, 0, 0, 0.6),
    inset 0 0 0 2px rgba(255, 255, 255, 0.1);
  overflow: hidden;
}

/* 顶部原生刘海 */
.phone-notch {
  position: absolute;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 140px;
  height: 28px;
  background-color: #000;
  border-bottom-left-radius: 18px;
  border-bottom-right-radius: 18px;
  z-index: 100;
}

/* 内屏 H5 的暗黑色系现代视效 */
.phone-screen {
  width: 100%;
  height: 100%;
  background: #f1f5f9;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* H5 的顶部导航 */
.h5-header {
  padding: 44px 20px 16px;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  font-size: 16px;
  color: #1e293b;
  position: sticky;
  top: 0;
  z-index: 90;
}

.h5-header .el-icon {
  font-size: 20px;
  cursor: pointer;
  color: #64748b;
}

/* 滚动主内容 */
.h5-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, #eff6ff 0%, #f1f5f9 100%);
}

.h5-content::-webkit-scrollbar {
  display: none;
  /* 隐藏滚动条呈现原生感 */
}

/* 顶部大号展示光晕 */
.product-halo {
  margin: 10px auto 20px;
  width: 80px;
  height: 80px;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.halo-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border-radius: 24px;
  transform: rotate(10deg);
  opacity: 0.15;
}

.halo-icon {
  font-size: 40px;
  color: #3b82f6;
  position: relative;
  z-index: 2;
}

/* 标题与标签 */
.h5-title {
  text-align: center;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  margin: 0 0 12px 0;
}

.h5-tags {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.h5-tag {
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 12px;
  background: #e2e8f0;
  color: #475569;
}

.h5-tag.verified {
  background: #dcfce7;
  color: #166534;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 内容卡片骨架 */
.h5-card {
  background: #ffffff;
  border-radius: 20px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px -10px rgba(0, 0, 0, 0.05);
}

.h5-card-header {
  font-size: 15px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 16px;
}

.h5-info-row {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid #f1f5f9;
}

.h5-info-row:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.h5-info-row .label {
  color: #64748b;
  font-size: 14px;
}

.h5-info-row .value {
  color: #0f172a;
  font-size: 14px;
  font-weight: 500;
  text-align: right;
}

.h5-info-row .value.code {
  font-family: monospace;
  background: #f1f5f9;
  padding: 2px 6px;
  border-radius: 4px;
}

/* 连接步进器重制 */
.h5-timeline {
  padding: 8px 0;
}

.h5-step {
  display: flex;
  gap: 16px;
}

.h5-step .step-indicator {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 20px;
}

.h5-step .dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #cbd5e1;
  border: 3px solid #f8fafc;
  z-index: 2;
  transition: all 0.3s;
}

.h5-step.active .dot {
  background: #10b981;
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.2);
}

.h5-step .line {
  width: 2px;
  flex: 1;
  background: #e2e8f0;
  margin: 4px 0;
}

.h5-step.active:not(.last) .line {
  background: #34d399;
}

.h5-step .step-content {
  flex: 1;
  padding-bottom: 24px;
}

.h5-step.last .step-content {
  padding-bottom: 0;
}

.h5-step .step-title {
  font-size: 14px;
  font-weight: 600;
  color: #475569;
  margin-bottom: 4px;
}

.h5-step.active .step-title {
  color: #0f172a;
}

.h5-step .step-desc {
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
  background: #f8fafc;
  padding: 10px;
  border-radius: 8px;
}

.h5-step .step-empty {
  font-size: 12px;
  color: #94a3b8;
}

.h5-footer {
  text-align: center;
  font-size: 12px;
  color: #94a3b8;
  margin: 20px 0 30px;
}
</style>
