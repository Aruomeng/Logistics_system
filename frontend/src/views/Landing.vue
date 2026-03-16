<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Right, View, Connection, DataLine } from '@element-plus/icons-vue'

const router = useRouter()
const isScrolled = ref(false)

const checkScroll = () => {
    isScrolled.value = window.scrollY > 50
}

onMounted(() => {
    window.addEventListener('scroll', checkScroll)
})

onUnmounted(() => {
    window.removeEventListener('scroll', checkScroll)
})

const handleEnterSystem = () => {
    const token = localStorage.getItem('token')
    if (token && token !== 'undefined') {
        router.push('/system/dashboard')
    } else {
        router.push('/login')
    }
}

// 溯源雷达动态点位逻辑 (Dynamic Radar Points)
const radarPoints = ref<{ top: number; left: number }[]>([])
const radarKey = ref(0) // 用于强制重启点位动画，实现与扫描线同步

const generatePoints = () => {
    radarPoints.value = Array.from({ length: 10 }, () => ({
        top: Math.random() * 80 + 10,
        left: Math.random() * 80 + 10
    }))
    radarKey.value++ // 刷新 key，重启 CSS 动画
}

onMounted(() => {
    window.addEventListener('scroll', checkScroll)
    generatePoints()
    // 关键同步：3000ms 必须与 CSS 中的 scanMove 动画周期 3s 完全对齐
    setInterval(generatePoints, 3000)
})
</script>

<template>
    <div class="landing-container dark">
        <!-- 动态背景光晕 (Mesh Gradient) -->
        <div class="mesh-bg">
            <div class="blob blob-1"></div>
            <div class="blob blob-2"></div>
            <div class="blob blob-3"></div>
            <div class="noise-overlay"></div>
        </div>

        <!-- 顶部玻璃导航栏 -->
        <nav class="glass-header" :class="{ 'header-scrolled': isScrolled }">
            <div class="nav-content">
                <div class="logo-area">
                    <div class="logo-text">
                        <span class="brand">智创农链</span>
                        <span class="dot">.</span>
                    </div>
                </div>
                <div class="nav-links">
                    <a href="#features">核心优势</a>
                    <a href="#bento">解决方案</a>
                    <a href="#about">关于我们</a>
                </div>
                <div class="nav-actions">
                    <button class="btn-ghost" @click="router.push('/login')">系统登录</button>
                    <button class="btn-primary" @click="handleEnterSystem">
                        进入控制台 <el-icon>
                            <Right />
                        </el-icon>
                    </button>
                </div>
            </div>
        </nav>

        <!-- 英雄集锦视区 (Hero Section) -->
        <section class="hero-section">
            <div class="hero-content">
                <div class="badge">🚀 领先的农产品供应链数字化基座</div>
                <h1 class="hero-title">
                    重塑农业供应链的 <br />
                    <span class="gradient-text">数字未来</span>
                </h1>
                <p class="hero-desc">
                    基于全链路冷链监控、全流程溯源存证以及智能化的供需流转撮合引擎。我们为现代新农业提供银行级的透明化基石。
                </p>
                <div class="hero-actions">
                    <button class="btn-primary large" @click="handleEnterSystem">
                        立即开始体验 <el-icon>
                            <Right />
                        </el-icon>
                    </button>
                    <button class="btn-secondary large" @click="router.push('/login')">
                        了解详细文档
                    </button>
                </div>

                <!-- 数据信任区 -->
                <div class="trust-metrics">
                    <div class="metric">
                        <div class="value">99.9%</div>
                        <div class="label">冷链预警准确率</div>
                    </div>
                    <div class="metric-divider"></div>
                    <div class="metric">
                        <div class="value">100w+</div>
                        <div class="label">节点溯源存证</div>
                    </div>
                    <div class="metric-divider"></div>
                    <div class="metric">
                        <div class="value">0</div>
                        <div class="label">数据安全事故</div>
                    </div>
                </div>
            </div>
        </section>

        <!-- 便当盒功能网格 (Bento Box Features) -->
        <section id="bento" class="bento-section">
            <div class="section-title">
                <h2>一切尽在掌握</h2>
                <p>拒绝繁琐，我们用极简的模块化设计包裹了最强大的业务引擎</p>
            </div>

            <div class="bento-grid">
                <!-- 主推大卡片 -->
                <div class="bento-card large-card bento-trace">
                    <div class="bento-icon-wrapper">
                        <el-icon>
                            <View />
                        </el-icon>
                    </div>
                    <h3>全生命周期透明化溯源</h3>
                    <p>从播种、施肥、采收到最终的质检，扫码即现。让复杂的农业生产过程变成一份不可篡改的电子信任名片。</p>
                    <div class="card-visual trace-visual">
                        <div class="scanner-window">
                            <div class="scan-grid"></div>
                            <div class="scan-line"></div>
                            <div class="data-points" :key="radarKey">
                                <div v-for="(p, i) in radarPoints" :key="i" class="point" :style="{
                                    top: p.top + '%',
                                    left: p.left + '%',
                                    animationDelay: (p.top / 100) * 3 + 's'
                                }">
                                </div>
                            </div>
                            <div class="status-tag">REAL-TIME DATA VERIFIED</div>
                        </div>
                    </div>
                </div>

                <!-- 右侧上方卡片 -->
                <div class="bento-card medium-card bento-coldchain">
                    <div class="bento-icon-wrapper warning">
                        <el-icon>
                            <DataLine />
                        </el-icon>
                    </div>
                    <h3>实时温控与冷链预警</h3>
                    <p>深入干线运输的温度探视引擎，超时及温差数据即刻拦截，护航鲜活生命力。</p>
                </div>

                <!-- 右侧下方卡片 -->
                <div class="bento-card medium-card bento-match">
                    <div class="bento-icon-wrapper primary">
                        <el-icon>
                            <Connection />
                        </el-icon>
                    </div>
                    <h3>智能采购/供应撮合</h3>
                    <p>告别信息壁垒。买方发需求，卖方实时竞价匹配，打造公平透明的线上大宗交易集市。</p>
                </div>
            </div>
        </section>

        <!-- 重塑：全景展示链路板块 (Workflow Section - Vision Rebirth) -->
        <section class="workflow-section">
            <div class="container">
                <div class="section-badge">SUPPLY CHAIN VISUALIZATION</div>
                <h2 class="section-title-alt">全链路数字化资产拓扑</h2>
                <div class="workflow-container">
                    <!-- SVG 分段式精准对齐路径 - 强制贯穿各节点中心 (viewBox 0-1000, 0-400) -->
                    <svg class="workflow-svg-path" viewBox="0 0 1000 400" fill="none" preserveAspectRatio="none">
                        <path
                            d="M150,200 C230,200 270,60 330,60 C400,60 450,200 500,200 C560,200 600,340 670,340 C750,340 800,200 850,200"
                            class="path-base" />
                        <path
                            d="M150,200 C230,200 270,60 330,60 C400,60 450,200 500,200 C560,200 600,340 670,340 C750,340 800,200 850,200"
                            class="path-glow" />
                    </svg>

                    <div class="workflow-nodes">
                        <div class="step-node s-1">
                            <div class="node-card">
                                <div class="node-icon"><el-icon>
                                        <ColdDrink />
                                    </el-icon></div>
                                <div class="node-content">
                                    <span class="node-tag">PHASE 01</span>
                                    <h4>原产地采收</h4>
                                    <p class="node-desc">从源头建立数字档案，记录播种、施肥及灌溉的全生命周期数据。</p>
                                </div>
                            </div>
                        </div>
                        <div class="step-node s-2">
                            <div class="node-card">
                                <div class="node-icon"><el-icon>
                                        <Cpu />
                                    </el-icon></div>
                                <div class="node-content">
                                    <span class="node-tag">PHASE 02</span>
                                    <h4>标准化赋码</h4>
                                    <p class="node-desc">一物一码技术加持，为每一份农产品生成全球唯一的“数字身份证”。</p>
                                </div>
                            </div>
                        </div>
                        <div class="step-node s-3">
                            <div class="node-card">
                                <div class="node-icon"><el-icon>
                                        <Van />
                                    </el-icon></div>
                                <div class="node-content">
                                    <span class="node-tag">PHASE 03</span>
                                    <h4>冷链多式联运</h4>
                                    <p class="node-desc">干线运输实时测温，温差异常毫秒级响应，确保生鲜商品极致口感。</p>
                                </div>
                            </div>
                        </div>
                        <div class="step-node s-4">
                            <div class="node-card">
                                <div class="node-icon"><el-icon>
                                        <OfficeBuilding />
                                    </el-icon></div>
                                <div class="node-content">
                                    <span class="node-tag">PHASE 04</span>
                                    <h4>中心仓分拣</h4>
                                    <p class="node-desc">AI智能辅助质检分级，自动化流水线实现海量订单的高效精准分发。</p>
                                </div>
                            </div>
                        </div>
                        <div class="step-node s-5">
                            <div class="node-card">
                                <div class="node-icon"><el-icon>
                                        <House />
                                    </el-icon></div>
                                <div class="node-content">
                                    <span class="node-tag">PHASE 05</span>
                                    <h4>消费者末端</h4>
                                    <p class="node-desc">终端扫码可见全链条真相，构建从指尖到田间的深度消费信任。</p>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- 重构：高级全场景数字化解决方案 (Scenario Solutions - The Cockpit) -->
        <section id="bento" class="solutions-section">
            <div class="section-badge">INDUSTRY SCENARIOS</div>
            <h2 class="section-title-alt">全场景数字中枢</h2>

            <div class="scenarios-container">
                <!-- 方案 01: 政府/监管 - 大卡片 (Hero) -->
                <div class="scenario-card hero-card gov-theme">
                    <div class="scenario-info">
                        <span class="sc-tag">GOVERNMENT & REGULATORY</span>
                        <h3>全域合规驾驶舱</h3>
                        <p>实时掌控属地农产品流向与抽检合格率，构建智慧监管一张网。通过地理信息系统实现精准治理。</p>
                        <ul class="sc-features">
                            <li><el-icon>
                                    <Check />
                                </el-icon> 区域动态热力分布</li>
                            <li><el-icon>
                                    <Check />
                                </el-icon> 异常风险毫秒预警</li>
                        </ul>
                    </div>
                    <div class="scenario-visual">
                        <div class="viz-orbit">
                            <div class="orbit-center"></div>
                            <div class="orbit-ring"></div>
                            <div class="orbit-particle"></div>
                        </div>
                        <div class="viz-data-bars">
                            <div class="bar" style="height: 40%"></div>
                            <div class="bar" style="height: 70%"></div>
                            <div class="bar" style="height: 55%"></div>
                        </div>
                    </div>
                </div>

                <!-- 方案 02: 合作社 - 中卡片 -->
                <div class="scenario-card brand-theme">
                    <div class="scenario-info">
                        <span class="sc-tag">COOPERATIVES</span>
                        <h3>品牌防伪数字化</h3>
                        <p>一物一码技术保护原产地品牌价值，阻断假冒伪劣溯源链。</p>
                    </div>
                    <div class="scenario-visual">
                        <div class="viz-scanner">
                            <div class="scanner-laser"></div>
                            <div class="code-pattern"></div>
                        </div>
                    </div>
                </div>

                <!-- 方案 03: 物流商 - 中卡片 -->
                <div class="scenario-card logistics-theme">
                    <div class="scenario-info">
                        <span class="sc-tag">LOGISTICS</span>
                        <h3>运输资产数字化</h3>
                        <p>自动化调度与能耗监控，提升 30% 以上的空驶率转化效能。</p>
                    </div>
                    <div class="scenario-visual">
                        <div class="viz-route">
                            <div class="route-line"></div>
                            <div class="route-truck"><el-icon>
                                    <Van />
                                </el-icon></div>
                        </div>
                    </div>
                </div>

                <!-- 方案 04: 电商/企业 - 横向长卡片 -->
                <div class="scenario-card wide-card supply-theme">
                    <div class="scenario-info">
                        <span class="sc-tag">ENTERPRISE & E-COMMERCE</span>
                        <h3>直采直供链路</h3>
                        <p>砍掉中间商环节，实现从基地到餐桌的极致新鲜与价格优势。数字化订单流转，一屏掌控供需全局。</p>
                    </div>
                    <div class="scenario-visual">
                        <div class="viz-network">
                            <div class="node n1"></div>
                            <div class="node n2"></div>
                            <div class="node n3"></div>
                            <div class="line l1"></div>
                            <div class="line l2"></div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <!-- 新增：末尾行动区 (Closing CTA Section) -->
        <section class="cta-section">
            <div class="cta-bg-glow"></div>
            <div class="cta-content">
                <h2 class="cta-title">准备好开启农链数字化了吗？</h2>
                <p class="cta-desc">加入 500+ 前瞻性农业企业，共同构建透明、高效、安全的生产流通新生态。</p>
                <div class="cta-btns">
                    <button class="btn-primary large" @click="handleEnterSystem">进入管理控制台</button>
                    <button class="btn-secondary large" @click="router.push('/login')">预约专家演示</button>
                </div>
            </div>
        </section>

        <!-- 底部版权 -->
        <footer class="footer">
            <div class="footer-content">
                <div class="brand-info">
                    <div class="logo-text"><span class="brand">智创农链</span><span class="dot">.</span></div>
                    <p>© 2026 智创农链系统. 保留所有权利.</p>
                </div>
            </div>
        </footer>
    </div>
</template>

<style scoped>
/* =========== 基础设定与重置 =========== */
.landing-container {
    position: relative;
    min-height: 100vh;
    background-color: #020617;
    /* 极暗石板黑 */
    color: #f8fafc;
    font-family: 'Inter', -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
    overflow-x: hidden;
    selection-color: #ffffff;
    selection-background-color: #3b82f6;
}

/* 隐藏部分全局滚动条引起的偏移 */
::-webkit-scrollbar {
    width: 8px;
}

::-webkit-scrollbar-track {
    background: #020617;
}

::-webkit-scrollbar-thumb {
    background: #334155;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: #475569;
}

/* =========== 动态网格背景液相辉光 =========== */
.mesh-bg {
    position: fixed;
    top: 0;
    left: 0;
    width: 100vw;
    height: 100vh;
    z-index: 0;
    overflow: hidden;
    background: #020617;
    pointer-events: none;
}

.blob {
    position: absolute;
    filter: blur(90px);
    opacity: 0.6;
    border-radius: 50%;
    animation: float 20s infinite ease-in-out alternate;
}

.blob-1 {
    top: -10%;
    left: -10%;
    width: 50vw;
    height: 50vw;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.4) 0%, rgba(2, 6, 23, 0) 70%);
    animation-delay: 0s;
}

.blob-2 {
    bottom: -20%;
    right: -10%;
    width: 60vw;
    height: 60vw;
    background: radial-gradient(circle, rgba(139, 92, 246, 0.25) 0%, rgba(2, 6, 23, 0) 70%);
    animation-delay: -5s;
}

.blob-3 {
    top: 40%;
    left: 40%;
    width: 40vw;
    height: 40vw;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, rgba(2, 6, 23, 0) 70%);
    animation-delay: -10s;
}

.noise-overlay {
    position: absolute;
    inset: 0;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E");
    opacity: 0.04;
    mix-blend-mode: overlay;
}

@keyframes float {
    0% {
        transform: translate(0, 0) scale(1);
    }

    33% {
        transform: translate(5%, 5%) scale(1.05);
    }

    66% {
        transform: translate(-2%, 8%) scale(0.95);
    }

    100% {
        transform: translate(0, 0) scale(1);
    }
}

/* 所有的可见区块需要在层级上浮出 */
.glass-header,
.hero-section,
.bento-section,
.footer {
    position: relative;
    z-index: 10;
}

/* =========== 高透玻璃导航栏 =========== */
.glass-header {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 72px;
    display: flex;
    justify-content: center;
    border-bottom: 1px solid transparent;
    transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
    z-index: 1000;
    /* 确保在最顶层 */
}

.header-scrolled {
    background: rgba(2, 6, 23, 0.6);
    backdrop-filter: blur(16px);
    -webkit-backdrop-filter: blur(16px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.05);
    box-shadow: 0 4px 30px rgba(0, 0, 0, 0.1);
}

.nav-content {
    width: 100%;
    max-width: 1200px;
    padding: 0 24px;
    display: flex;
    align-items: center;
    justify-content: space-between;
}

.logo-area {
    display: flex;
    align-items: center;
    gap: 12px;
}

.logo-text {
    font-size: 22px;
    font-weight: 800;
    letter-spacing: -0.5px;
}

.brand {
    color: #f8fafc;
}

.dot {
    color: #3b82f6;
}

.nav-links {
    display: none;
}

@media (min-width: 768px) {
    .nav-links {
        display: flex;
        gap: 32px;
    }
}

.nav-links a {
    color: #94a3b8;
    text-decoration: none;
    font-size: 15px;
    font-weight: 500;
    transition: color 0.3s ease;
}

.nav-links a:hover {
    color: #f8fafc;
}

.nav-actions {
    display: flex;
    gap: 16px;
    align-items: center;
}

/* =========== 抽象按钮系统 =========== */
button {
    cursor: pointer;
    border: none;
    border-radius: 999px;
    font-weight: 600;
    font-size: 14px;
    transition: all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    position: relative;
    overflow: hidden;
    user-select: none;
}

button::before {
    content: '';
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg,
            transparent,
            rgba(255, 255, 255, 0.2),
            transparent);
    transition: 0.5s;
    pointer-events: none;
}

button:hover {
    transform: translateY(-5px) scale(1.05);
    box-shadow: 0 15px 30px -10px rgba(37, 99, 235, 0.5);
}

button:hover::before {
    left: 100%;
    transition: 0.6s;
}

button:active {
    transform: translateY(-2px) scale(0.96);
    filter: brightness(0.9);
}

.btn-ghost {
    background: transparent;
    color: #e2e8f0;
    padding: 8px 16px;
}

.btn-ghost:hover {
    color: #fff;
    background: rgba(255, 255, 255, 0.05);
}

.btn-primary {
    background: linear-gradient(135deg, #2563eb, #1d4ed8);
    color: white;
    padding: 10px 24px;
    box-shadow: 0 0 20px rgba(37, 99, 235, 0.3);
}

.btn-primary:hover {
    background: linear-gradient(135deg, #3b82f6, #2563eb);
    box-shadow: 0 15px 35px rgba(37, 99, 235, 0.5);
}

.btn-secondary {
    background: rgba(255, 255, 255, 0.05);
    color: white;
    border: 1px solid rgba(255, 255, 255, 0.1);
    backdrop-filter: blur(8px);
    padding: 10px 24px;
}

.btn-secondary:hover {
    background: rgba(255, 255, 255, 0.12);
    border-color: rgba(255, 255, 255, 0.3);
    box-shadow: 0 10px 30px rgba(255, 255, 255, 0.05);
}

button.large {
    padding: 16px 36px;
    font-size: 16px;
}

/* =========== 英雄区 (Hero Section) =========== */
.hero-section {
    padding: 180px 24px 100px;
    display: flex;
    justify-content: center;
    text-align: center;
    min-height: 100vh;
    box-sizing: border-box;
}

.hero-content {
    max-width: 900px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 16px;
    background: rgba(37, 99, 235, 0.15);
    border: 1px solid rgba(37, 99, 235, 0.3);
    color: #60a5fa;
    border-radius: 999px;
    font-size: 14px;
    font-weight: 500;
    margin-bottom: 32px;
    backdrop-filter: blur(4px);
    box-shadow: 0 0 20px rgba(37, 99, 235, 0.2) inset;
}

.hero-title {
    font-size: clamp(48px, 8vw, 88px);
    font-weight: 800;
    line-height: 1.1;
    letter-spacing: -2px;
    margin: 0 0 24px;
    color: #f8fafc;
}

.gradient-text {
    background: linear-gradient(135deg, #60a5fa 0%, #a78bfa 50%, #34d399 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
    color: transparent;
}

.hero-desc {
    font-size: clamp(16px, 2vw, 20px);
    color: #94a3b8;
    line-height: 1.6;
    max-width: 680px;
    margin: 0 auto 48px;
}

.hero-actions {
    display: flex;
    gap: 20px;
    flex-wrap: wrap;
    justify-content: center;
    margin-bottom: 80px;
}

/* 数据信任条 */
.trust-metrics {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 40px;
    padding: 30px 60px;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.02);
    border: 1px solid rgba(255, 255, 255, 0.05);
    backdrop-filter: blur(20px);
    flex-wrap: wrap;
}

.metric {
    text-align: center;
}

.metric .value {
    font-size: 36px;
    font-weight: 700;
    color: #f8fafc;
    margin-bottom: 4px;
    font-feature-settings: "tnum";
}

.metric .label {
    font-size: 13px;
    color: #64748b;
    text-transform: uppercase;
    letter-spacing: 1px;
}

.metric-divider {
    width: 1px;
    height: 40px;
    background: rgba(255, 255, 255, 0.1);
}

@media (max-width: 640px) {
    .hero-actions {
        flex-direction: column;
        width: 100%;
    }

    .hero-actions button {
        width: 100%;
    }

    .trust-metrics {
        gap: 20px;
        padding: 20px;
    }

    .metric-divider {
        display: none;
    }

    .metric {
        width: 45%;
    }
}

/* =========== 便当盒布局区 (Bento Box) =========== */
.bento-section {
    padding: 100px 24px;
    max-width: 1200px;
    margin: 0 auto;
}

.section-title {
    text-align: center;
    margin-bottom: 60px;
}

.section-title h2 {
    font-size: 40px;
    font-weight: 700;
    margin: 0 0 16px;
    letter-spacing: -1px;
}

.section-title p {
    color: #94a3b8;
    font-size: 18px;
}

.bento-grid {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    grid-auto-rows: minmax(280px, auto);
    gap: 24px;
}

.bento-card {
    background: rgba(15, 23, 42, 0.4);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 32px;
    padding: 40px;
    backdrop-filter: blur(20px);
    transition: all 0.5s cubic-bezier(0.16, 1, 0.3, 1);
    overflow: hidden;
    position: relative;
    display: flex;
    flex-direction: column;
}

.bento-card:hover {
    transform: translateY(-8px);
    border-color: rgba(255, 255, 255, 0.15);
    box-shadow: 0 20px 40px rgba(0, 0, 0, 0.4), 0 0 40px rgba(37, 99, 235, 0.1) inset;
}

.bento-icon-wrapper {
    width: 56px;
    height: 56px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.05);
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 28px;
    margin-bottom: 24px;
    color: #f8fafc;
    border: 1px solid rgba(255, 255, 255, 0.05);
}

.bento-icon-wrapper.warning {
    color: #fbbf24;
    background: rgba(251, 191, 36, 0.1);
    border-color: rgba(251, 191, 36, 0.2);
}

.bento-icon-wrapper.primary {
    color: #3b82f6;
    background: rgba(59, 130, 246, 0.1);
    border-color: rgba(59, 130, 246, 0.2);
}

.bento-card h3 {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 12px;
    letter-spacing: -0.5px;
}

.bento-card p {
    color: #94a3b8;
    font-size: 16px;
    line-height: 1.6;
    margin: 0;
}

/* 网格排布设定 */
.large-card {
    grid-column: span 8;
    grid-row: span 2;
}

.medium-card {
    grid-column: span 4;
}

@media (max-width: 1024px) {

    .large-card,
    .medium-card {
        grid-column: span 12;
    }
}

/* 局部卡片内的装饰视效 */
.card-visual {
    margin-top: 32px;
    width: 100%;
    height: 180px;
    position: relative;
    border-radius: 16px;
    background: rgba(0, 0, 0, 0.2);
    border: 1px solid rgba(255, 255, 255, 0.05);
    overflow: hidden;
}

/* 专项拉长溯源雷达视窗 */
.trace-visual {
    height: 380px;
}

.scanner-window {
    width: 100%;
    height: 100%;
    position: relative;
}

.scan-grid {
    position: absolute;
    inset: 0;
    background-image:
        linear-gradient(rgba(59, 130, 246, 0.05) 1px, transparent 1px),
        linear-gradient(90deg, rgba(59, 130, 246, 0.05) 1px, transparent 1px);
    background-size: 20px 20px;
}

.scan-line {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 4px;
    background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.5), transparent);
    box-shadow: 0 0 15px rgba(59, 130, 246, 0.8);
    animation: scanMove 3s infinite linear;
    z-index: 2;
}

.data-points {
    position: absolute;
    inset: 0;
}

.point {
    position: absolute;
    width: 6px;
    height: 6px;
    background: #60a5fa;
    border-radius: 50%;
    box-shadow: 0 0 10px #3b82f6;
    opacity: 0;
    /* 默认隐藏，等待扫描线扫过 */
    animation: pointReveal 3s infinite forwards;
}

@keyframes pointReveal {
    0% {
        opacity: 0;
        transform: scale(0);
    }

    10% {
        opacity: 1;
        transform: scale(1.2);
    }

    15% {
        transform: scale(1);
    }

    85% {
        opacity: 1;
    }

    100% {
        opacity: 0;
    }
}

.status-tag {
    position: absolute;
    bottom: 12px;
    right: 12px;
    font-size: 10px;
    font-family: monospace;
    color: #3b82f6;
    background: rgba(37, 99, 235, 0.1);
    padding: 2px 8px;
    border-radius: 4px;
    border: 1px solid rgba(37, 99, 235, 0.2);
    letter-spacing: 1px;
    animation: blink 1.5s infinite;
}

@keyframes scanMove {
    0% {
        top: 0;
    }

    100% {
        top: 100%;
    }
}

@keyframes pointPulse {
    0% {
        transform: scale(1);
        opacity: 0.5;
    }

    100% {
        transform: scale(1.5);
        opacity: 1;
    }
}

@keyframes blink {

    0%,
    100% {
        opacity: 1;
    }

    50% {
        opacity: 0.4;
    }
}

/* =========== 重塑：全景链路样式 (Pro Version) =========== */
.workflow-section {
    padding: 160px 24px;
    background: radial-gradient(circle at 50% 50%, #0f172a 0%, #020617 100%);
    overflow: hidden;
}

.workflow-container {
    max-width: 1240px;
    margin: 0 auto;
    height: 600px;
    /* 进一步拉升高度，解决拥挤感 */
    position: relative;
    padding: 0;
}

.workflow-svg-path {
    position: absolute;
    inset: 0;
    width: 100%;
    height: 100%;
    z-index: 1;
}

.path-base {
    stroke: rgba(59, 130, 246, 0.1);
    stroke-width: 2;
    stroke-linecap: round;
}

.path-glow {
    stroke: #3b82f6;
    stroke-width: 2;
    stroke-linecap: round;
    stroke-dasharray: 20 2000;
    filter: drop-shadow(0 0 8px #3b82f6);
    animation: flowLine 6s infinite linear;
}

@keyframes flowLine {
    0% {
        stroke-dashoffset: 0;
    }

    100% {
        stroke-dashoffset: -2020;
    }
}

.workflow-nodes {
    position: absolute;
    inset: 0;
    z-index: 2;
}

.step-node {
    position: absolute;
    transform: translate(-50%, -50%);
    z-index: 5;
}

.s-1 {
    top: 50%;
    left: 15%;
}

.s-2 {
    top: 15%;
    left: 33%;
}

.s-3 {
    top: 50%;
    left: 50%;
}

.s-4 {
    top: 85%;
    left: 67%;
}

.s-5 {
    top: 50%;
    left: 85%;
}

.step-node:hover {
    z-index: 50;
    /* 悬浮时置于最顶层，防止被其他节点遮挡 */
}

.node-card {
    background: rgba(15, 23, 42, 0.6);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.08);
    padding: 24px;
    border-radius: 20px;
    display: flex;
    align-items: center;
    gap: 16px;
    width: 300px;
    /* 强制锁定 300px 宽度，解决长短不一问题 */
    flex-shrink: 0;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
    cursor: default;
    position: relative;
    box-sizing: border-box;
}

.step-node:hover .node-card {
    transform: translateY(-12px) scale(1.08);
    /* 增加向上的位移悬浮感 */
    border-color: rgba(59, 130, 246, 0.5);
    background: rgba(30, 41, 59, 0.7);
    box-shadow:
        0 20px 40px rgba(0, 0, 0, 0.4),
        0 0 20px rgba(37, 99, 235, 0.2);
}

.step-node:hover .node-icon {
    transform: rotate(10deg) scale(1.1);
    background: rgba(37, 99, 235, 0.2);
    box-shadow: 0 0 15px rgba(37, 99, 235, 0.4);
}

.node-icon {
    width: 44px;
    height: 44px;
    flex: none;
    /* 拒绝任何弹性拉伸 */
    aspect-ratio: 1 / 1;
    /* 强制保持 1:1 比例 */
    flex-shrink: 0;
    /* 防止图标被拉伸变形 */
    background: rgba(37, 99, 235, 0.1);
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
    color: #3b82f6;
    border: 1px solid rgba(37, 99, 235, 0.2);
    transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.node-content {
    display: flex;
    flex-direction: column;
}

.node-tag {
    font-size: 9px;
    color: #3b82f6;
    font-weight: 800;
    letter-spacing: 1px;
    margin-bottom: 2px;
}

.node-content h4 {
    margin: 0;
    font-size: 15px;
    color: #f8fafc;
    font-weight: 600;
    white-space: nowrap;
}

.node-desc {
    margin: 8px 0 0 0;
    font-size: 12px;
    color: #64748b;
    line-height: 1.5;
    max-width: 180px;
    /* 略微收缩以保证边缘安全 */
    opacity: 0;
    max-height: 0;
    overflow: hidden;
    transition: all 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
    white-space: normal;
    /* 强制换行 */
}

.step-node:hover .node-desc {
    opacity: 1;
    max-height: 60px;
    margin-top: 10px;
}

@media (max-width: 1024px) {
    .workflow-container {
        height: 800px;
    }

    .workflow-svg-path {
        display: none;
    }

    .workflow-nodes {
        display: flex;
        flex-direction: column;
        justify-content: space-between;
        align-items: center;
        padding: 40px 0;
    }

    .step-node {
        position: static;
        transform: none;
        width: 100%;
        max-width: 300px;
    }
}

/* =========== 重构：全域场景交互舱 (Scenario Cockpit) =========== */
.scenarios-container {
    display: grid;
    grid-template-columns: repeat(12, 1fr);
    grid-gap: 24px;
    max-width: 1300px;
    margin: 40px auto;
}

.scenario-card {
    position: relative;
    background: rgba(15, 23, 42, 0.5);
    backdrop-filter: blur(30px);
    border: 1px solid rgba(255, 255, 255, 0.05);
    border-radius: 32px;
    padding: 48px;
    overflow: hidden;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    transition: all 0.6s cubic-bezier(0.23, 1, 0.32, 1);
}

.scenario-card:hover {
    transform: translateY(-10px);
    border-color: rgba(37, 99, 235, 0.3);
    box-shadow: 0 30px 60px -12px rgba(0, 0, 0, 0.5);
}

.hero-card {
    grid-column: span 7;
    height: 500px;
}

.brand-theme {
    grid-column: span 5;
    height: 500px;
}

.logistics-theme {
    grid-column: span 5;
    height: 420px;
}

.wide-card {
    grid-column: span 7;
    height: 420px;
    flex-direction: row;
    align-items: center;
    gap: 40px;
}

.scenario-info {
    position: relative;
    z-index: 5;
    max-width: 400px;
}

.sc-tag {
    font-size: 10px;
    font-weight: 800;
    color: #3b82f6;
    letter-spacing: 2px;
    text-transform: uppercase;
    display: block;
    margin-bottom: 20px;
}

.scenario-info h3 {
    font-size: 36px;
    font-weight: 800;
    color: #f8fafc;
    margin-bottom: 16px;
    line-height: 1.2;
}

.scenario-info p {
    font-size: 16px;
    color: #94a3b8;
    line-height: 1.6;
}

.sc-features {
    list-style: none;
    padding: 0;
    margin: 24px 0 0;
}

.sc-features li {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 14px;
    color: #cbd5e1;
    margin-bottom: 8px;
}

.sc-features li .el-icon {
    color: #3b82f6;
    font-size: 16px;
}

/* 模拟器装饰核心样式 */
.scenario-visual {
    position: relative;
    width: 240px;
    height: 240px;
    margin-left: auto;
    background: radial-gradient(circle at center, rgba(37, 99, 235, 0.1) 0%, transparent 70%);
}

.wide-card .scenario-visual {
    margin-left: 0;
    flex: 1;
    height: 100%;
}

/* VIZ - 政府轨道 */
.viz-orbit {
    position: relative;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}

.orbit-center {
    width: 60px;
    height: 60px;
    background: rgba(37, 99, 235, 0.3);
    border-radius: 50%;
    filter: blur(30px);
}

.orbit-ring {
    position: absolute;
    width: 160px;
    height: 160px;
    border: 1px dashed rgba(37, 99, 235, 0.3);
    border-radius: 50%;
    animation: orbitRotate 10s infinite linear;
}

@keyframes orbitRotate {
    from {
        transform: rotate(0deg);
    }

    to {
        transform: rotate(360deg);
    }
}

.viz-data-bars {
    display: flex;
    gap: 4px;
    position: absolute;
    bottom: 20%;
    right: 10%;
    align-items: flex-end;
}

.viz-data-bars .bar {
    width: 6px;
    background: #3b82f6;
    border-radius: 2px;
    animation: barGrow 1.5s infinite alternate ease-in-out;
}

.viz-data-bars .bar:nth-child(2) {
    animation-delay: 0.2s;
}

.viz-data-bars .bar:nth-child(3) {
    animation-delay: 0.4s;
}

@keyframes barGrow {
    0% {
        height: 20%;
        opacity: 0.4;
    }

    100% {
        height: 90%;
        opacity: 1;
    }
}

/* VIZ - 扫描器 */
.viz-scanner {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 20px;
}

.code-pattern {
    width: 120px;
    height: 120px;
    border: 2px solid rgba(37, 99, 235, 0.2);
    position: relative;
    background: repeating-linear-gradient(45deg, transparent, transparent 10px, rgba(37, 99, 235, 0.05) 10px, rgba(37, 99, 235, 0.05) 20px);
}

.scanner-laser {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 2px;
    background: #3b82f6;
    box-shadow: 0 0 15px #3b82f6;
    animation: scanUpDown 4s infinite ease-in-out;
}

@keyframes scanUpDown {

    0%,
    100% {
        top: 10%;
    }

    50% {
        top: 90%;
    }
}

/* VIZ - 路由 */
.viz-route {
    width: 100%;
    height: 100%;
    position: relative;
}

.route-line {
    position: absolute;
    top: 50%;
    left: 10%;
    width: 80%;
    height: 2px;
    background: rgba(255, 255, 255, 0.1);
}

.route-truck {
    position: absolute;
    transform: translate(-50%, -50%);
    top: 50%;
    color: #3b82f6;
    font-size: 24px;
    animation: truckMove 5s infinite linear;
}

@keyframes truckMove {
    0% {
        left: 10%;
        opacity: 0;
    }

    10%,
    90% {
        opacity: 1;
    }

    100% {
        left: 90%;
        opacity: 0;
    }
}

/* VIZ - 网络 */
.viz-network {
    width: 100%;
    height: 100%;
    position: relative;
}

.viz-network .node {
    position: absolute;
    width: 12px;
    height: 12px;
    background: #3b82f6;
    border-radius: 50%;
    box-shadow: 0 0 20px #3b82f6;
    animation: nodePulse 2s infinite alternate ease-in-out;
}

.n1 {
    top: 30%;
    left: 30%;
    animation-delay: 0s;
}

.n2 {
    top: 60%;
    left: 70%;
    animation-delay: 0.5s;
}

.n3 {
    top: 20%;
    left: 80%;
    animation-delay: 1.2s;
}

.viz-network .line {
    position: absolute;
    height: 2px;
    background: linear-gradient(90deg, transparent, #3b82f6, transparent);
    background-size: 200% 100%;
    opacity: 0.4;
    animation: lineFlow 3s infinite linear;
}

.l1 {
    top: 32%;
    left: 33%;
    width: 40%;
    transform: rotate(35deg);
    transform-origin: left top;
}

.l2 {
    top: 62%;
    left: 72%;
    width: 30%;
    transform: rotate(-150deg);
    transform-origin: left top;
}

@keyframes nodePulse {
    0% {
        transform: scale(1);
        opacity: 0.6;
    }

    100% {
        transform: scale(1.4);
        opacity: 1;
        box-shadow: 0 0 40px #3b82f6;
    }
}

@keyframes lineFlow {
    0% {
        background-position: 200% 0;
    }

    100% {
        background-position: -200% 0;
    }
}

@media (max-width: 1024px) {
    .scenarios-container {
        grid-template-columns: 1fr;
    }

    .hero-card,
    .brand-theme,
    .logistics-theme,
    .wide-card {
        grid-column: span 1;
        height: auto;
        min-height: 400px;
        flex-direction: column;
    }

    .scenario-info {
        max-width: 100%;
    }

    .scenario-visual {
        margin: 40px 0 0 0;
        width: 100%;
    }
}

/* =========== 新增：CTA 行动区样式 =========== */
.cta-section {
    padding: 160px 24px;
    position: relative;
    overflow: hidden;
    display: flex;
    justify-content: center;
    text-align: center;
}

.cta-bg-glow {
    position: absolute;
    width: 60%;
    height: 300px;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.15) 0%, transparent 70%);
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    filter: blur(80px);
}

.cta-content {
    max-width: 800px;
    position: relative;
    z-index: 2;
}

.cta-title {
    font-size: clamp(32px, 5vw, 64px);
    font-weight: 800;
    margin-bottom: 24px;
    line-height: 1.1;
}

.cta-desc {
    font-size: 20px;
    color: #94a3b8;
    margin-bottom: 48px;
}

.cta-btns {
    display: flex;
    gap: 20px;
    justify-content: center;
}

@media (max-width: 768px) {
    .workflow-steps {
        flex-direction: column;
        align-items: flex-start;
        padding-left: 60px;
        gap: 60px;
    }

    .workflow-line {
        left: 90px;
        width: 1px;
        height: 100%;
        top: 0;
        background: linear-gradient(to bottom, transparent, rgba(59, 130, 246, 0.5), transparent);
    }

    .solution-grid {
        grid-template-columns: 1fr;
    }

    .cta-btns {
        flex-direction: column;
    }
}

/* =========== 页脚 =========== */
.footer {
    border-top: 1px solid rgba(255, 255, 255, 0.05);
    background: rgba(2, 6, 23, 0.8);
    backdrop-filter: blur(20px);
    padding: 40px 24px;
}

.footer-content {
    max-width: 1200px;
    margin: 0 auto;
    display: flex;
    justify-content: center;
    align-items: center;
    text-align: center;
}

.brand-info p {
    color: #64748b;
    font-size: 14px;
    margin-top: 12px;
}
</style>
