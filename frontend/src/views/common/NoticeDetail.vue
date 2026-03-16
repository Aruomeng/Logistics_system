<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getNoticeDetail } from '../../api/notice'

const route = useRoute()
const router = useRouter()
const notice = ref<any>(null)
const attachments = ref<any[]>([])
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const noticeId = Number(route.params.id)
    const res: any = await getNoticeDetail(noticeId)
    notice.value = res.data.notice
    attachments.value = res.data.attachments || []
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-container">
    <div class="card-content" v-loading="loading">
      <el-button link type="primary" @click="router.back()" style="margin-bottom: 16px;">
        <el-icon>
          <ArrowLeft />
        </el-icon> 返回列表
      </el-button>

      <template v-if="notice">
        <h2 style="font-size: 22px; margin-bottom: 12px;">
          <el-tag v-if="notice.isTop === 1" type="danger" size="small" style="margin-right: 8px;">置顶</el-tag>
          {{ notice.title }}
        </h2>
        <div style="color: #909399; font-size: 13px; margin-bottom: 24px;">
          发布人：{{ notice.publisherName }} &nbsp;|&nbsp; 发布时间：{{ notice.createTime }}
        </div>
        <el-divider />
        <div class="notice-content" v-html="notice.content" style="line-height: 1.8; min-height: 200px;" />

        <template v-if="attachments.length > 0">
          <el-divider />
          <h4 style="margin-bottom: 12px;">附件</h4>
          <div v-for="att in attachments" :key="att.naId" style="margin-bottom: 8px;">
            <el-link :href="att.filePath" target="_blank" type="primary">
              <el-icon>
                <Document />
              </el-icon> {{ att.fileName }}
              <span style="color: #909399; margin-left: 8px;">({{ (att.fileSize / 1024).toFixed(1) }}KB)</span>
            </el-link>
          </div>
        </template>
      </template>
    </div>
  </div>
</template>
