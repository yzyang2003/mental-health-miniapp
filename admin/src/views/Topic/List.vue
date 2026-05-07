<template>
  <div class="topic-list">
    <el-card shadow="never">
      <!-- 状态筛选 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="loadData">
          <el-radio-button :value="null">全部</el-radio-button>
          <el-radio-button :value="0">审核中</el-radio-button>
          <el-radio-button :value="1">正常</el-radio-button>
          <el-radio-button :value="2">已删除</el-radio-button>
        </el-radio-group>
      </div>

      <!-- 表格 -->
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="内容" min-width="300">
          <template #default="{ row }">
            <div class="content-preview">{{ row.content?.substring(0, 50) }}{{ row.content?.length > 50 ? '...' : '' }}</div>
            <div v-if="row.images?.length" class="image-thumbnails">
              <el-image
                v-for="(img, index) in row.images.slice(0, 3)"
                :key="index"
                :src="img"
                :preview-src-list="row.images"
                fit="cover"
                style="width: 30px; height: 30px; margin-right: 4px; border-radius: 2px"
              >
                <template #error>
                  <div class="image-error">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <span v-if="row.images.length > 3" class="more-images">+{{ row.images.length - 3 }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="publisherOpenid" label="发布者" width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
              {{ row.status === 1 ? '正常' : row.status === 0 ? '审核中' : '已删除' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">查看详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :total="pagination.total"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="loadData"
        @current-change="loadData"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="帖子详情" width="700px">
      <div v-if="currentTopic" class="topic-detail">
        <div class="topic-content">
          <p>{{ currentTopic.content }}</p>
          <div v-if="currentTopic.images?.length" class="topic-images">
            <el-image
              v-for="(img, index) in currentTopic.images"
              :key="index"
              :src="img"
              :preview-src-list="currentTopic.images"
              fit="cover"
              style="width: 120px; height: 120px; border-radius: 4px"
            >
              <template #error>
                <div class="image-error">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
          </div>
        </div>

        <el-divider />

        <div class="replies-section">
          <h4>回复列表 ({{ replies.length }})</h4>
          <div v-if="replies.length === 0" class="no-replies">暂无回复</div>
          <div v-else class="reply-list">
            <div v-for="reply in replies" :key="reply.id" class="reply-item">
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-meta">
                <span>{{ reply.replierOpenid }}</span>
                <span>{{ formatTime(reply.createTime) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="topic-actions">
          <el-button
            v-if="currentTopic.status === 0"
            type="success"
            @click="handleApprove"
          >
            审核通过
          </el-button>
          <el-button
            v-if="currentTopic.status !== 2"
            type="danger"
            @click="handleDelete"
          >
            删除
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Picture } from '@element-plus/icons-vue'
import { getTopicList, getTopicDetail, updateTopicStatus } from '@/api/topic'

const loading = ref(false)
const tableData = ref([])
const statusFilter = ref(null)
const detailVisible = ref(false)
const currentTopic = ref(null)
const replies = ref([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getTopicList({
      page: pagination.page,
      size: pagination.size,
      status: statusFilter.value
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载帖子列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row) {
  try {
    const res = await getTopicDetail(row.id)
    currentTopic.value = res.data.topic
    replies.value = res.data.replies || []
    detailVisible.value = true
  } catch (error) {
    ElMessage.error('加载详情失败')
  }
}

async function handleApprove() {
  try {
    await updateTopicStatus(currentTopic.value.id, 1)
    ElMessage.success('审核通过')
    detailVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function handleDelete() {
  try {
    await updateTopicStatus(currentTopic.value.id, 2)
    ElMessage.success('已删除')
    detailVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.topic-list {
  padding: 20px;
}

.filter-bar {
  margin-bottom: 16px;
}

.content-preview {
  color: #303133;
}

.image-count {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.image-thumbnails {
  display: flex;
  align-items: center;
  margin-top: 4px;
}

.more-images {
  color: #909399;
  font-size: 12px;
  margin-left: 4px;
}

.image-error {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
  background: #f5f7fa;
  color: #c0c4cc;
}

.topic-content {
  margin-bottom: 16px;
}

.topic-content p {
  margin-bottom: 12px;
  line-height: 1.6;
}

.topic-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.replies-section h4 {
  margin-bottom: 12px;
  color: #303133;
}

.no-replies {
  color: #909399;
  text-align: center;
  padding: 20px;
}

.reply-list {
  max-height: 300px;
  overflow-y: auto;
}

.reply-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 8px;
}

.reply-content {
  color: #303133;
  margin-bottom: 8px;
}

.reply-meta {
  display: flex;
  justify-content: space-between;
  color: #909399;
  font-size: 12px;
}

.topic-actions {
  margin-top: 20px;
  text-align: right;
}
</style>
