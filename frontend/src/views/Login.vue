<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessage } from 'element-plus'
import request from '../utils/request'
import { User, Lock, Key, Grape, Monitor, ShoppingCart, Van, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  captchaCode: '',
  captchaKey: '',
})
const captchaImage = ref('')
const activeTab = ref('account') // 'account' | 'sms'

// Parallax tracking
const mouseX = ref(0)
const mouseY = ref(0)
let animationFrameId: number

const handleMouseMove = (e: MouseEvent) => {
  if (animationFrameId) cancelAnimationFrame(animationFrameId)
  animationFrameId = requestAnimationFrame(() => {
    const { innerWidth, innerHeight } = window
    mouseX.value = (e.clientX / innerWidth) * 2 - 1
    mouseY.value = (e.clientY / innerHeight) * 2 - 1
  })
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const formRef = ref()

const refreshCaptcha = async () => {
  try {
    const res: any = await request.get('/captcha/image')
    form.captchaKey = res.data.captchaKey
    captchaImage.value = res.data.captchaImage
  } catch {
    // 忽略加载失败
  }
}

onMounted(() => {
  refreshCaptcha()
  window.addEventListener('mousemove', handleMouseMove)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', handleMouseMove)
  if (animationFrameId) cancelAnimationFrame(animationFrameId)
})

const handleLogin = async () => {
  if (activeTab.value !== 'account') {
    ElMessage.info('演示环境暂未开启短信登录，请使用密码登录')
    return
  }
  await formRef.value?.validate()
  loading.value = true
  try {
    const res: any = await request.post('/user/login', {
      username: form.username,
      password: form.password,
      captchaKey: form.captchaKey,
      captchaCode: form.captchaCode,
    })
    userStore.setLogin(res.data.token, res.data.userInfo)
    ElMessage.success('登录成功，欢迎回来！')
    router.push('/system/dashboard')
  } catch (e) {
    refreshCaptcha()
  } finally {
    loading.value = false
  }
}

const quickFill = (username: string) => {
  form.username = username
  form.password = 'admin123'
  ElMessage.success(`已填充 ${username} 账号，可直接登录`)
}
</script>

<template>
  <div class="login-wrapper">
    <!-- 动态背景层 (释放为全屏全域底图) -->
    <div class="mesh-bg">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
    </div>

    <!-- 统一中央高透玻璃层 -->
    <div class="glass-container">

      <!-- 左栏：品牌传递视差区 -->
      <div class="left-content">
        <div class="parallax-container"
          :style="{ transform: `translate(${mouseX * -20}px, ${mouseY * -20}px) rotateX(${mouseY * 4}deg) rotateY(${mouseX * -4}deg)` }">
          <div class="brand-content">
            <div class="logo-circle">
              <el-icon :size="48">
                <Grape />
              </el-icon>
            </div>
            <h1 class="brand-title">
              数字农产品<br />供应链中枢
            </h1>
            <p class="brand-desc">
              构筑全域透明、智能协同的新一代农业生态网络。<br />
              连接从田间地头到消费餐桌的每一个关键节点。
            </p>

            <!-- 悬浮微交互信息卡片 -->
            <div class="interactive-cards">
              <div class="info-card">
                <div class="card-icon blue"><el-icon>
                    <Monitor />
                  </el-icon></div>
                <div class="card-text">
                  <h4>全局监控面板</h4>
                  <p>全自动数据聚合分析，辅助决策</p>
                </div>
              </div>
              <div class="info-card delay-1">
                <div class="card-icon amber"><el-icon>
                    <Van />
                  </el-icon></div>
                <div class="card-text">
                  <h4>智能冷链物流</h4>
                  <p>轨迹实时追踪，保障农产品鲜活</p>
                </div>
              </div>
              <div class="info-card delay-2">
                <div class="card-icon green"><el-icon>
                    <ShoppingCart />
                  </el-icon></div>
                <div class="card-text">
                  <h4>供需精准匹配</h4>
                  <p>多维度需求预测，资源最优分配</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右栏：操作登录区 -->
      <div class="right-content">
        <div class="form-wrapper">
          <!-- 移动端显示的 Logo -->
          <div class="mobile-header">
            <el-icon :size="32" color="#2563eb">
              <Grape />
            </el-icon>
            <h3>物流供应链系统</h3>
          </div>

          <div class="login-header">
            <h2>系统登录</h2>
            <p class="welcome-text">欢迎回来！请使用您的账号和密码进行认证。</p>
          </div>

          <!-- 登录类型切换（丰富页面结构） -->
          <div class="login-tabs">
            <button type="button" class="tab-btn" :class="{ active: activeTab === 'account' }"
              @click="activeTab = 'account'">
              密码登录
            </button>
            <button type="button" class="tab-btn" :class="{ active: activeTab === 'sms' }" @click="activeTab = 'sms'">
              短信登录
            </button>
          </div>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="login-form"
            @submit.prevent="handleLogin">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="管理员 / 供应商 / 物流商 账号" :prefix-icon="User"
                class="modern-input" />
            </el-form-item>

            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="请输入密码（统一：admin123）" :prefix-icon="Lock"
                show-password class="modern-input" />
            </el-form-item>

            <el-form-item prop="captchaCode">
              <div class="captcha-flex">
                <el-input v-model="form.captchaCode" placeholder="请输入右侧验证码" :prefix-icon="Key" class="modern-input"
                  @keyup.enter="handleLogin" />
                <div class="captcha-img-container" @click="refreshCaptcha" title="点击刷新验证码">
                  <img v-if="captchaImage" :src="captchaImage" alt="captcha" />
                  <el-icon v-else class="is-loading">
                    <Loading />
                  </el-icon>
                </div>
              </div>
            </el-form-item>

            <div class="form-options">
              <el-checkbox>自动登录</el-checkbox>
              <a href="#" class="forgot-pwd" @click.prevent>忘记密码？</a>
            </div>

            <el-form-item>
              <el-button type="primary" :loading="loading" class="submit-btn" native-type="submit" @click="handleLogin">
                {{ activeTab === 'account' ? '进入系统' : '发送验证码' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="divider-box">
            <span class="line"></span>
            <span class="text">快速体验账号 (点击一键填充)</span>
            <span class="line"></span>
          </div>

          <!-- 可交互的快速填充账号库，填补右侧区域 -->
          <div class="quick-login-grid">
            <div class="quick-card admin" @click="quickFill('admin')">
              <div class="avatar">
                <span>Admin</span>
              </div>
              <div class="details">
                <span class="role">平台管理员</span>
                <span class="username">内置最高权限</span>
              </div>
            </div>
            <div class="quick-card supplier" @click="quickFill('supplier01')">
              <div class="avatar">
                <span>Suplr</span>
              </div>
              <div class="details">
                <span class="role">果蔬供应方</span>
                <span class="username">发布供应管理</span>
              </div>
            </div>
            <div class="quick-card logistics" @click="quickFill('logistics01')">
              <div class="avatar">
                <span>Logis</span>
              </div>
              <div class="details">
                <span class="role">冷链物流商</span>
                <span class="username">订单跟踪发货</span>
              </div>
            </div>
            <div class="quick-card consumer" @click="quickFill('consumer01')">
              <div class="avatar">
                <span>Cons</span>
              </div>
              <div class="details">
                <span class="role">采购终端端</span>
                <span class="username">创建需求订单</span>
              </div>
            </div>
          </div>

          <div class="form-footer">
            <p>© 2026 Logistics Supply Chain. All rights reserved.</p>
          </div>

        </div>
      </div>

    </div>
  </div>
</template>

<style scoped>
.login-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  position: relative;
  background-color: #0b1120;
  /* 极暗科技底色 */
  font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
}

/* 动态网格模糊全屏背景 */
.mesh-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  z-index: 0;
  background-image:
    radial-gradient(rgba(255, 255, 255, 0.05) 1.5px, transparent 1.5px),
    radial-gradient(rgba(255, 255, 255, 0.05) 1.5px, transparent 1.5px);
  background-size: 40px 40px;
  background-position: 0 0, 20px 20px;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.6;
  animation: orbFloat 25s infinite alternate cubic-bezier(0.45, 0.05, 0.55, 0.95);
}

.orb-1 {
  width: 60vw;
  height: 60vw;
  background: radial-gradient(circle, #3b82f6 0%, transparent 70%);
  top: -20%;
  left: -20%;
}

.orb-2 {
  width: 50vw;
  height: 50vw;
  background: radial-gradient(circle, #8b5cf6 0%, transparent 70%);
  bottom: -15%;
  right: -15%;
  animation-delay: -5s;
}

.orb-3 {
  width: 55vw;
  height: 55vw;
  background: radial-gradient(circle, #0ea5e9 0%, transparent 70%);
  top: 10%;
  left: 20%;
  animation-duration: 30s;
  animation-direction: reverse;
}

.orb-4 {
  width: 65vw;
  height: 65vw;
  background: radial-gradient(circle, #f43f5e 0%, transparent 70%);
  bottom: -20%;
  left: -20%;
  animation-delay: -10s;
  animation-duration: 28s;
}

@keyframes orbFloat {
  0% {
    transform: translate(0, 0) scale(1) rotate(0deg);
  }

  50% {
    transform: translate(8%, 12%) scale(1.15) rotate(180deg);
  }

  100% {
    transform: translate(-8%, -8%) scale(0.9) rotate(360deg);
  }
}

/* --- 容器 --- */
.glass-container {
  display: flex;
  position: relative;
  z-index: 10;
  width: 85%;
  max-width: 1200px;
  min-height: 720px;
  height: auto;
  margin: 40px 0;
  background: rgba(20, 25, 40, 0.4);
  backdrop-filter: blur(28px);
  -webkit-backdrop-filter: blur(28px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  border-radius: 32px;
  box-shadow: 0 30px 60px -15px rgba(0, 0, 0, 0.5), inset 0 0 0 1px rgba(255, 255, 255, 0.05);
  overflow: hidden;
}

.left-content {
  flex: 5.5;
  display: flex;
  align-items: center;
  justify-content: center;
  perspective: 1000px;
  border-right: 1px solid rgba(255, 255, 255, 0.05);
}

.right-content {
  flex: 4.5;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  min-width: 480px;
  max-width: 600px;
  padding: 10px 0;
}

/* 左侧内容视差 */
.parallax-container {
  position: relative;
  width: 85%;
  max-width: 580px;
  transition: transform 0.1s ease-out;
  transform-style: preserve-3d;
}

.brand-content {
  color: #f8fafc;
}

.logo-circle {
  width: 84px;
  height: 84px;
  background: linear-gradient(135deg, #3b82f6, #6366f1);
  border-radius: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  box-shadow: 0 12px 30px rgba(59, 130, 246, 0.4);
  color: white;
  transform: translateZ(20px);
}

.brand-title {
  font-size: 44px;
  line-height: 1.25;
  font-weight: 800;
  margin: 0 0 16px 0;
  background: linear-gradient(135deg, #ffffff 0%, #a5b4fc 100%);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
  transform: translateZ(30px);
}

.brand-desc {
  font-size: 17px;
  line-height: 1.6;
  color: #94a3b8;
  margin-bottom: 48px;
  transform: translateZ(10px);
}

/* 交互卡片 */
.interactive-cards {
  display: flex;
  flex-direction: column;
  gap: 16px;
  transform: translateZ(20px);
}

.info-card {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 18px 24px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.04);
  border-radius: 20px;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  cursor: default;
}

.info-card:hover {
  background: rgba(255, 255, 255, 0.06);
  border-color: rgba(255, 255, 255, 0.15);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.3);
  transform: translateX(12px) scale(1.02);
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 52px;
  height: 52px;
  border-radius: 14px;
  font-size: 26px;
  transition: transform 0.3s;
}

.info-card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
}

.card-icon.blue {
  background: rgba(59, 130, 246, 0.15);
  color: #60a5fa;
}

.card-icon.amber {
  background: rgba(245, 158, 11, 0.15);
  color: #fbbf24;
}

.card-icon.green {
  background: rgba(16, 185, 129, 0.15);
  color: #34d399;
}

.card-text h4 {
  margin: 0 0 6px 0;
  font-size: 17px;
  font-weight: 600;
  color: #f1f5f9;
}

.card-text p {
  margin: 0;
  font-size: 14px;
  color: #64748b;
}

.form-wrapper {
  width: 100%;
  max-width: 420px;
  padding: 40px;
}

.mobile-header {
  display: none;
  align-items: center;
  gap: 12px;
  margin-bottom: 40px;
}

.mobile-header h3 {
  margin: 0;
  font-size: 22px;
  color: #f8fafc;
  font-weight: 700;
}

.login-header h2 {
  font-size: 34px;
  font-weight: 800;
  color: #f8fafc;
  margin: 0 0 10px 0;
  letter-spacing: -0.5px;
}

.welcome-text {
  color: #94a3b8;
  font-size: 14px;
  margin: 0 0 20px 0;
}

/* 自定义Tabs */
.login-tabs {
  display: flex;
  padding: 4px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 16px;
}

.tab-btn {
  flex: 1;
  padding: 8px 0;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #94a3b8;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.tab-btn:hover {
  color: #f8fafc;
}

.tab-btn.active {
  background: rgba(255, 255, 255, 0.1);
  color: #ffffff;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 深度定制表单 */
:deep(.modern-input .el-input__wrapper) {
  padding: 0 16px;
  height: 48px;
  border-radius: 12px;
  background-color: rgba(255, 255, 255, 0.05);
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1) inset;
  transition: all 0.3s ease;
}

:deep(.modern-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.25) inset;
  background-color: rgba(255, 255, 255, 0.08);
}

:deep(.modern-input .el-input__wrapper.is-focus) {
  background-color: rgba(255, 255, 255, 0.1);
  box-shadow: 0 0 0 2px #3b82f6 inset, 0 4px 14px rgba(59, 130, 246, 0.15) !important;
}

:deep(.modern-input .el-input__inner) {
  font-size: 15px;
  color: #f8fafc;
}

:deep(.modern-input .el-input__inner::placeholder) {
  color: #64748b;
}

:deep(.modern-input .el-input__prefix-inner) {
  font-size: 20px;
  color: #64748b;
  margin-right: 8px;
}

:deep(.modern-input .el-input__wrapper.is-focus .el-input__prefix-inner) {
  color: #3b82f6;
}

.captcha-flex {
  display: flex;
  gap: 12px;
  width: 100%;
}

.captcha-flex .el-input {
  flex: 1;
}

.captcha-img-container {
  flex-shrink: 0;
  width: 120px;
  height: 48px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  overflow: hidden;
  background: rgba(255, 255, 255, 0.05);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.captcha-img-container:hover {
  border-color: #3b82f6;
  box-shadow: 0 4px 12px rgba(59, 130, 246, 0.1);
  opacity: 0.9;
}

.captcha-img-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -14px 0 16px 0;
}

/* Fix checkbox text color */
:deep(.el-checkbox__label) {
  color: #94a3b8;
}

:deep(.el-checkbox.is-checked .el-checkbox__label) {
  color: #3b82f6;
}

.forgot-pwd {
  font-size: 14px;
  color: #3b82f6;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s;
}

.forgot-pwd:hover {
  color: #60a5fa;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  height: 48px;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  border: none;
  letter-spacing: 0.5px;
  background: linear-gradient(135deg, #3b82f6, #2563eb);
  box-shadow: 0 8px 20px -6px rgba(37, 99, 235, 0.6);
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: white;
}

.submit-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px -8px rgba(37, 99, 235, 0.8);
  background: linear-gradient(135deg, #4f46e5, #3b82f6);
}

.submit-btn:active {
  transform: translateY(1px);
}

/* 右侧下半部分 */
.divider-box {
  display: flex;
  align-items: center;
  margin: 16px 0 12px;
}

.divider-box .line {
  flex: 1;
  height: 1px;
  background: rgba(255, 255, 255, 0.1);
}

.divider-box .text {
  padding: 0 16px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

/* 高级网格化账号体验区 */
.quick-login-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
}

.quick-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.05);
  background: rgba(255, 255, 255, 0.03);
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.quick-card:hover {
  background: rgba(255, 255, 255, 0.08);
  border-color: rgba(255, 255, 255, 0.15);
  transform: translateY(-3px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.quick-card .avatar {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 11px;
  color: white;
  text-transform: uppercase;
  flex-shrink: 0;
}

.quick-card.admin .avatar {
  background: linear-gradient(135deg, #3b82f6, #1d4ed8);
}

.quick-card.supplier .avatar {
  background: linear-gradient(135deg, #10b981, #047857);
}

.quick-card.logistics .avatar {
  background: linear-gradient(135deg, #f59e0b, #b45309);
}

.quick-card.consumer .avatar {
  background: linear-gradient(135deg, #ef4444, #b91c1c);
}

.quick-card .details {
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.quick-card .role {
  font-size: 14px;
  font-weight: 700;
  color: #f1f5f9;
  margin-bottom: 2px;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.quick-card .username {
  font-size: 11px;
  color: #94a3b8;
  font-weight: 500;
  white-space: nowrap;
  text-overflow: ellipsis;
  overflow: hidden;
}

.form-footer {
  margin-top: 16px;
  text-align: center;
  color: #64748b;
  font-size: 12px;
}

/* 响应式适配 */
@media screen and (max-width: 1024px) {
  .left-content {
    display: none;
  }

  .glass-container {
    width: 90%;
    max-width: 500px;
  }

  .right-content {
    flex: 1;
    min-width: 0;
  }
}

@media screen and (max-width: 800px) {
  .mobile-header {
    display: flex;
  }
}
</style>
