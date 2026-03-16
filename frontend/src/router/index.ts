import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/home',
    name: 'Landing',
    component: () => import('../views/Landing.vue'),
    meta: { title: '首页 - 智创农链', requiresAuth: false },
  },
  {
    path: '/',
    redirect: '/home',
  },
  {
    path: '/trace/:code',
    name: 'PublicTraceDetail',
    component: () => import('../views/common/TraceDetail.vue'),
    meta: { title: '产品溯源档案', requiresAuth: false },
  },
  {
    path: '/system',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/system/dashboard',
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/DashboardRouter.vue'),
        meta: { title: '工作台', icon: 'HomeFilled' },
      },
      // ========== 管理员路由 ==========
      {
        path: 'admin/users',
        name: 'AdminUsers',
        component: () => import('../views/admin/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User', roles: [1] },
      },
      {
        path: 'admin/notices',
        name: 'AdminNotices',
        component: () => import('../views/admin/NoticeManage.vue'),
        meta: { title: '公告管理', icon: 'Bell', roles: [1] },
      },
      {
        path: 'admin/supply-audit',
        name: 'AdminSupplyAudit',
        component: () => import('../views/admin/SupplyAudit.vue'),
        meta: { title: '供应审核', icon: 'Checked', roles: [1] },
      },
      {
        path: 'admin/orders',
        name: 'AdminOrders',
        component: () => import('../views/admin/OrderManage.vue'),
        meta: { title: '订单管理', icon: 'Document', roles: [1] },
      },
      {
        path: 'admin/analysis',
        name: 'AdminAnalysis',
        component: () => import('../views/admin/Dashboard.vue'),
        meta: { title: '数据分析', icon: 'DataAnalysis', roles: [1], hidden: true },
      },
      {
        path: 'admin/logs',
        name: 'AdminLogs',
        component: () => import('../views/admin/OperationLogs.vue'),
        meta: { title: '操作日志', icon: 'Tickets', roles: [1] },
      },
      {
        path: 'admin/demands',
        name: 'AdminDemands',
        component: () => import('../views/admin/DemandAudit.vue'),
        meta: { title: '需求审核', icon: 'List', roles: [1] },
      },
      // ========== 供应方路由 ==========
      {
        path: 'supplier/products',
        name: 'SupplierProducts',
        component: () => import('../views/supplier/ProductManage.vue'),
        meta: { title: '供应管理', icon: 'Goods', roles: [2] },
      },
      {
        path: 'supplier/orders',
        name: 'SupplierOrders',
        component: () => import('../views/supplier/OrderManage.vue'),
        meta: { title: '订单管理', icon: 'Document', roles: [2] },
      },
      // ========== 物流方路由 ==========
      {
        path: 'logistics/orders',
        name: 'LogisticsOrders',
        component: () => import('../views/logistics/OrderManage.vue'),
        meta: { title: '物流订单', icon: 'Van', roles: [3] },
      },
      // ========== 消费者路由 ==========
      {
        path: 'consumer/supply-list',
        name: 'ConsumerSupplyList',
        component: () => import('../views/consumer/SupplyList.vue'),
        meta: { title: '供应大厅', icon: 'ShoppingCart', roles: [4] },
      },
      {
        path: 'consumer/orders',
        name: 'ConsumerOrders',
        component: () => import('../views/consumer/OrderManage.vue'),
        meta: { title: '我的订单', icon: 'Document', roles: [4] },
      },
      {
        path: 'consumer/demands',
        name: 'ConsumerDemands',
        component: () => import('../views/consumer/DemandManage.vue'),
        meta: { title: '我的需求', icon: 'Edit', roles: [4] },
      },
      // ========== 公共路由 ==========
      {
        path: 'demands',
        name: 'DemandHall',
        component: () => import('../views/common/DemandHall.vue'),
        meta: { title: '需求大厅', icon: 'Opportunity' },
      },
      {
        path: 'notices',
        name: 'NoticeList',
        component: () => import('../views/common/NoticeList.vue'),
        meta: { title: '系统公告', icon: 'Bell' },
      },
      {
        path: 'notice/:id',
        name: 'NoticeDetail',
        component: () => import('../views/common/NoticeDetail.vue'),
        meta: { title: '公告详情', hidden: true },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('../views/common/Profile.vue'),
        meta: { title: '个人中心', hidden: true },
      },
      {
        path: 'trace/:code',
        name: 'TraceDetail',
        component: () => import('../views/common/TraceDetail.vue'),
        meta: { title: '溯源详情', hidden: true },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫
router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  const userInfoStr = localStorage.getItem('userInfo')

  // 设置页面标题
  document.title = (to.meta.title as string) ? `${to.meta.title} - 供应链管理系统` : '供应链管理系统'

  // 不需要认证的页面
  if (to.meta.requiresAuth === false) {
    // 只有在访问登录页且已登录时，才强制跳转到后台
    if (token && to.path === '/login') {
      next('/system/dashboard')
    } else {
      next()
    }
    return
  }

  // 需要认证
  if (!token) {
    next('/login')
    return
  }

  // 角色权限校验
  const roles = to.meta.roles as number[] | undefined
  if (roles && userInfoStr) {
    const userInfo = JSON.parse(userInfoStr)
    if (!roles.includes(userInfo.roleType)) {
      next('/system/dashboard')
      return
    }
  }

  next()
})

export default router
