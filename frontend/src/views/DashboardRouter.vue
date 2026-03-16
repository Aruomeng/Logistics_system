<script setup lang="ts">
import { useUserStore } from '../stores/user'
import AdminDashboard from './admin/Dashboard.vue'
import SupplierDashboard from './supplier/Dashboard.vue'
import LogisticsDashboard from './logistics/Dashboard.vue'
import ConsumerDashboard from './consumer/Dashboard.vue'

const userStore = useUserStore()
</script>

<template>
    <AdminDashboard v-if="userStore.roleType === 1" />
    <SupplierDashboard v-else-if="userStore.roleType === 2" />
    <LogisticsDashboard v-else-if="userStore.roleType === 3" />
    <ConsumerDashboard v-else-if="userStore.roleType === 4" />
</template>
