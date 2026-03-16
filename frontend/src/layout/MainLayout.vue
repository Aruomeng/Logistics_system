<script setup lang="ts">
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '../stores/user'
import { ElMessageBox, ElNotification } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const sseSource = ref<EventSource | null>(null)
const isCollapse = ref(false)
const isFullscreen = ref(false)

const isDark = ref(localStorage.getItem('theme-dark') === 'true')
if (isDark.value) {
    document.documentElement.classList.add('dark')
}

const toggleDarkMode = (event: MouseEvent) => {
    const isAppearing = !isDark.value

    const x = event.clientX
    const y = event.clientY
    const endRadius = Math.hypot(
        Math.max(x, innerWidth - x),
        Math.max(y, innerHeight - y)
    )

    const performToggle = () => {
        isDark.value = !isDark.value
        localStorage.setItem('theme-dark', String(isDark.value))
        if (isDark.value) {
            document.documentElement.classList.add('dark')
        } else {
            document.documentElement.classList.remove('dark')
        }
    }

    // 支持 View Transitions API 的新型动画
    if (!document.startViewTransition) {
        performToggle()
        return
    }

    const transition = document.startViewTransition(() => {
        performToggle()
    })

    transition.ready.then(() => {
        const clipPath = [
            `circle(0px at ${x}px ${y}px)`,
            `circle(${endRadius}px at ${x}px ${y}px)`
        ]

        document.documentElement.animate(
            {
                clipPath: isAppearing ? clipPath : [...clipPath].reverse(),
            },
            {
                duration: 450,
                easing: 'cubic-bezier(0.25, 1, 0.5, 1)',
                pseudoElement: isAppearing
                    ? '::view-transition-new(root)'
                    : '::view-transition-old(root)',
            }
        )
    })
}

const themeDrawerVisible = ref(false)
const themeSettings = ref({
    primaryColor: localStorage.getItem('theme-primary') || '#2563eb',
    fontSize: localStorage.getItem('theme-font-size') || '14px',
    fontFamily: localStorage.getItem('theme-font-family') || '-apple-system, BlinkMacSystemFont, "SF Pro Display", "Segoe UI", Roboto, Helvetica, Arial, sans-serif',
    borderRadius: localStorage.getItem('theme-border-radius') || '20px',
    sidebarStyle: localStorage.getItem('theme-sidebar') || 'dark'
})

const fontOptions = [
    { label: '系统默认 (Apple UI)', value: '-apple-system, BlinkMacSystemFont, "SF Pro Display", "Segoe UI", Roboto, Helvetica, Arial, sans-serif' },
    { label: '得意黑 (Smiley Sans)', value: '"Smiley Sans Oblique", sans-serif' },
    { label: '霞鹜文楷 (LXGW WenKai)', value: '"LXGW WenKai Screen", sans-serif' },
    { label: '思源宋体 (Noto Serif)', value: '"Noto Serif SC", serif' },
]

const fontSizeOptions = [
    { label: '小号 (12px)', value: '12px' },
    { label: '标准 (14px)', value: '14px' },
    { label: '大号 (16px)', value: '16px' },
    { label: '特大号 (18px)', value: '18px' },
]

const radiusOptions = [
    { label: '锐利直角 (4px)', value: '4px' },
    { label: '系统微切 (8px)', value: '8px' },
    { label: '柔和视感 (16px)', value: '16px' },
    { label: '苹果 Bento (20px)', value: '20px' },
    { label: '无极凝胶 (24px)', value: '24px' },
]

const sidebarOptions = [
    { label: '极光暗夜 (Dark)', value: 'dark' },
    { label: '凝脂白玉 (Light)', value: 'light' },
]

const applyTheme = () => {
    const root = document.documentElement
    root.style.setProperty('--primary', themeSettings.value.primaryColor)
    root.style.setProperty('--el-color-primary', themeSettings.value.primaryColor)
    root.style.setProperty('--font-size', themeSettings.value.fontSize)
    root.style.setProperty('--el-font-size-base', themeSettings.value.fontSize)
    root.style.setProperty('--font-family', themeSettings.value.fontFamily)
    root.style.setProperty('--card-radius', themeSettings.value.borderRadius)

    // 控制侧边栏的主题风格
    if (themeSettings.value.sidebarStyle === 'dark') {
        root.style.setProperty('--sidebar-bg', 'rgba(15, 23, 42, 0.85)')
        root.style.setProperty('--sidebar-text', '#94a3b8')
        root.style.setProperty('--sidebar-border', 'rgba(255, 255, 255, 0.05)')
        root.style.setProperty('--sidebar-hover', 'rgba(255, 255, 255, 0.06)')
        root.style.setProperty('--sidebar-title', '#ffffff')
    } else {
        root.style.setProperty('--sidebar-bg', 'rgba(255, 255, 255, 0.85)')
        root.style.setProperty('--sidebar-text', '#475569')
        root.style.setProperty('--sidebar-border', 'rgba(0, 0, 0, 0.05)')
        root.style.setProperty('--sidebar-hover', 'rgba(0, 0, 0, 0.04)')
        root.style.setProperty('--sidebar-title', '#1e293b')
    }

    localStorage.setItem('theme-primary', themeSettings.value.primaryColor)
    localStorage.setItem('theme-font-size', themeSettings.value.fontSize)
    localStorage.setItem('theme-font-family', themeSettings.value.fontFamily)
    localStorage.setItem('theme-border-radius', themeSettings.value.borderRadius)
    localStorage.setItem('theme-sidebar', themeSettings.value.sidebarStyle)
}

watch(themeSettings, () => applyTheme(), { deep: true })

const toggleCollapse = () => {
    isCollapse.value = !isCollapse.value
}

const toggleFullscreen = () => {
    if (!document.fullscreenElement) {
        document.documentElement.requestFullscreen()
    } else {
        if (document.exitFullscreen) {
            document.exitFullscreen()
        }
    }
}

/** 根据角色过滤侧边栏菜单 */
const menuItems = computed(() => {
    const layoutRoute = router.getRoutes().find((r) => r.path === '/system')
    if (!layoutRoute || !layoutRoute.children) return []
    return layoutRoute.children
        .filter((child) => {
            if (!child.meta?.title || child.meta?.hidden) return false
            const roles = child.meta?.roles as number[] | undefined
            if (roles && !roles.includes(userStore.roleType)) return false
            return true
        })
        .map((child) => ({
            path: '/system/' + child.path,
            title: child.meta?.title as string,
            icon: child.meta?.icon as string,
        }))
})

/** SSE 实时公告订阅与全局监听器 */
onMounted(() => {
    applyTheme() // 挂载时立即运用本地自定义主题

    document.addEventListener('fullscreenchange', () => {
        isFullscreen.value = !!document.fullscreenElement
    })

    const userId = userStore.userInfo?.userId
    if (userId) {
        const sse = new EventSource(`/api/v1/notice/common/subscribe?userId=${userId}`)
        sse.addEventListener('notice', (ev) => {
            try {
                const data = JSON.parse(ev.data)
                ElNotification({
                    title: '📢 新公告',
                    message: data.title || '有新的系统公告',
                    type: 'info',
                    duration: 5000,
                    onClick: () => router.push('/notices'),
                })
            } catch { /* ignore */ }
        })
        sse.onerror = () => {
            // 连接断开时静默处理，不影响用户使用
        }
        sseSource.value = sse
    }
})

onUnmounted(() => {
    sseSource.value?.close()
})

const handleLogout = async () => {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
    })
    sseSource.value?.close()
    userStore.logout()
    router.push('/login')
}
</script>

<template>
    <el-container class="main-layout">
        <!-- 侧边栏 -->
        <el-aside :width="isCollapse ? '64px' : 'var(--sidebar-width)'" class="sidebar">
            <div class="logo">
                <el-icon size="24" color="#409EFF">
                    <Grape />
                </el-icon>
                <transition name="fade">
                    <span v-show="!isCollapse" class="logo-text">供应链管理系统</span>
                </transition>
            </div>
            <el-menu :default-active="route.path" router background-color="transparent" text-color="var(--sidebar-text)"
                active-text-color="var(--sidebar-active)" :collapse="isCollapse" :collapse-transition="false"
                class="transparent-menu">
                <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
                    <el-icon>
                        <component :is="item.icon" />
                    </el-icon>
                    <span>{{ item.title }}</span>
                </el-menu-item>
            </el-menu>
        </el-aside>

        <!-- 右侧内容区 -->
        <el-container>
            <!-- 顶部导航 -->
            <el-header class="header">
                <div class="header-left">
                    <el-icon class="collapse-btn" size="20" @click="toggleCollapse">
                        <component :is="isCollapse ? 'Expand' : 'Fold'" />
                    </el-icon>
                    <el-breadcrumb separator="/">
                        <el-breadcrumb-item :to="{ path: '/system/dashboard' }">首页</el-breadcrumb-item>
                        <el-breadcrumb-item v-if="route.meta.title">
                            {{ route.meta.title }}
                        </el-breadcrumb-item>
                    </el-breadcrumb>
                </div>
                <div class="header-right">
                    <el-tooltip :content="isDark ? '切换亮色模式' : '切换暗黑模式'" placement="bottom">
                        <div class="action-btn" :class="{ 'is-active': isDark }" @click="toggleDarkMode"
                            style="margin-right: 12px;">
                            <el-icon size="18">
                                <component :is="isDark ? 'Moon' : 'Sunny'" />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tooltip :content="isFullscreen ? '退出全屏' : '全屏'" placement="bottom">
                        <div class="action-btn" :class="{ 'is-active': isFullscreen }" @click="toggleFullscreen">
                            <el-icon size="18">
                                <FullScreen />
                            </el-icon>
                        </div>
                    </el-tooltip>
                    <el-tag type="info" size="small" style="margin-left: 16px; margin-right: 12px;">
                        {{ userStore.roleName }}
                    </el-tag>
                    <el-dropdown>
                        <span class="user-info">
                            <el-avatar :size="32" :src="userStore.userInfo?.avatar" icon="User" />
                            <span class="username">{{ userStore.userInfo?.username }}</span>
                        </span>
                        <template #dropdown>
                            <el-dropdown-menu>
                                <el-dropdown-item @click="themeDrawerVisible = true">
                                    <el-icon>
                                        <Brush />
                                    </el-icon> 风格设置
                                </el-dropdown-item>
                                <el-dropdown-item divided @click="router.push('/system/profile')">
                                    <el-icon>
                                        <User />
                                    </el-icon> 个人中心
                                </el-dropdown-item>
                                <el-dropdown-item divided @click="handleLogout">
                                    <el-icon>
                                        <SwitchButton />
                                    </el-icon> 退出登录
                                </el-dropdown-item>
                            </el-dropdown-menu>
                        </template>
                    </el-dropdown>
                </div>
            </el-header>

            <!-- 内容区 -->
            <el-main class="main-content">
                <router-view />
            </el-main>
        </el-container>
    </el-container>

    <!-- 风格偏好设置抽屉 -->
    <el-drawer v-model="themeDrawerVisible" title="全局风格偏好配置" size="340px">
        <el-form label-position="top">
            <el-form-item label="核心主题色">
                <el-color-picker v-model="themeSettings.primaryColor" color-format="hex"
                    :predefine="['#2563eb', '#10b981', '#f59e0b', '#8b5cf6', '#ec4899']" />
            </el-form-item>
            <el-form-item label="全局字体族 (Font Family)">
                <el-select v-model="themeSettings.fontFamily">
                    <el-option v-for="font in fontOptions" :key="font.value" :label="font.label" :value="font.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="全局基础字号">
                <el-select v-model="themeSettings.fontSize">
                    <el-option v-for="size in fontSizeOptions" :key="size.value" :label="size.label"
                        :value="size.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="系统组件曲率 (Border Radius)">
                <el-select v-model="themeSettings.borderRadius">
                    <el-option v-for="radius in radiusOptions" :key="radius.value" :label="radius.label"
                        :value="radius.value" />
                </el-select>
            </el-form-item>
            <el-form-item label="侧边栏模式 (Sidebar Style)">
                <el-radio-group v-model="themeSettings.sidebarStyle">
                    <el-radio v-for="style in sidebarOptions" :key="style.value" :value="style.value">{{ style.label
                        }}</el-radio>
                </el-radio-group>
            </el-form-item>
        </el-form>
        <template #footer>
            <el-button @click="themeDrawerVisible = false" type="primary" style="width: 100%;">设定完毕</el-button>
        </template>
    </el-drawer>
</template>

<style scoped>
.main-layout {
    height: 100vh;
}

.sidebar {
    background: var(--sidebar-bg);
    /* 提升通透率以配合底部的弥散发光 */
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    overflow-y: auto;
    transition: width 0.3s cubic-bezier(0.645, 0.045, 0.355, 1), background-color 0.4s;
    display: flex;
    flex-direction: column;
    border-right: 1px solid var(--sidebar-border);
    /* 极细微高光侧边 */
    box-shadow: 4px 0 24px rgba(0, 0, 0, 0.15);
    /* 加宽阴影散布 */
    z-index: 50;
    /* 防止被 Bento 卡片盖住 */
}

.logo {
    height: var(--header-height);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
    color: var(--sidebar-title);
    font-size: 16px;
    font-weight: 600;
    white-space: nowrap;
    transition: color 0.3s;
}

.fade-enter-active,
.fade-leave-active {
    transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
    opacity: 0;
}

.transparent-menu {
    border-right: none;
    flex: 1;
}

:deep(.el-menu-item) {
    transition: all 0.3s ease;
    margin: 4px 12px;
    border-radius: 8px;
    height: 44px;
    line-height: 44px;
    color: var(--sidebar-text);
}

:deep(.el-menu-item:hover) {
    background-color: var(--sidebar-hover) !important;
}

:deep(.el-menu-item.is-active) {
    background-color: var(--primary) !important;
    color: #fff !important;
    box-shadow: 0 4px 6px -1px rgba(37, 99, 235, 0.4);
}

/* 折叠状态下图标居中，去除自定义 margin 带来的偏移 */
:deep(.el-menu--collapse .el-menu-item) {
    margin: 4px auto;
    padding: 0 !important;
    width: 44px;
    justify-content: center;
}

:deep(.el-menu--collapse .el-tooltip__trigger) {
    padding: 0 !important;
    display: flex;
    justify-content: center;
    align-items: center;
    width: 100%;
}

:deep(.el-menu--collapse .el-icon) {
    margin: 0 !important;
}

.header {
    height: var(--header-height);
    background: rgba(255, 255, 255, 0.6);
    backdrop-filter: blur(24px);
    -webkit-backdrop-filter: blur(24px);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 32px;
    border-bottom: 1px solid rgba(255, 255, 255, 0.3);
    box-shadow: 0 4px 6px -4px rgba(0, 0, 0, 0.05);
    /* 只留最底部细微下沉影 */
    position: sticky;
    top: 0;
    z-index: 40;
}

.header-left {
    display: flex;
    align-items: center;
}

.collapse-btn {
    cursor: pointer;
    font-size: 20px;
    margin-right: 16px;
    color: #606266;
    transition: color 0.3s;
}

.collapse-btn:hover {
    color: var(--primary, #409eff);
}

.header-right {
    display: flex;
    align-items: center;
}

.action-btn {
    width: 36px;
    height: 36px;
    border-radius: 12px;
    display: flex;
    justify-content: center;
    align-items: center;
    cursor: pointer;
    background: rgba(148, 163, 184, 0.1);
    color: #475569;
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn:hover {
    background: var(--primary-light, rgba(37, 99, 235, 0.1));
    color: var(--primary, #2563eb);
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.15);
}

.action-btn:active {
    transform: translateY(1px) scale(0.95);
    box-shadow: none;
}

.action-btn .el-icon {
    transition: transform 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.action-btn.is-active .el-icon {
    transform: scale(0.9) rotate(360deg);
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
    cursor: pointer;
}

.username {
    font-size: 14px;
    color: #606266;
}

.main-content {
    background: transparent;
    overflow-y: auto;
    padding: 20px;
}
</style>
