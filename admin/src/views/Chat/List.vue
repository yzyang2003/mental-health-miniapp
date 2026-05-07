<template>
  <div class="chat-list">
    <el-card shadow="never">
      <el-form :inline="true" class="search-form">
        <el-form-item label="OpenID">
          <el-input v-model="searchForm.openid" placeholder="搜索openid" clearable />
        </el-form-item>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="搜索消息内容" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="openid" label="用户OpenID" min-width="280" show-overflow-tooltip />
        <el-table-column prop="lastMessage" label="最新消息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="messageCount" label="消息数" width="100" />
        <el-table-column prop="lastActiveTime" label="最近活跃" width="180">
          <template #default="{ row }">
            {{ formatTime(row.lastActiveTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewChat(row)">查看对话</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 对话详情弹窗 -->
    <el-drawer v-model="chatVisible" :title="`对话记录 - ${chatOpenid}`" size="600px">
      <div class="chat-messages">
        <div
          v-for="msg in chatMessages"
          :key="msg.id"
          class="message-item"
          :class="msg.role"
        >
          <div class="message-bubble">
            <div class="message-content">{{ msg.content }}</div>
            <div class="message-time">{{ formatTime(msg.createTime) }}</div>
          </div>
        </div>
        <div v-if="chatMessages.length === 0" class="no-messages">暂无对话记录</div>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getChatList, getUserChat } from '@/api/chat'

const loading = ref(false)
const tableData = ref([])
const chatVisible = ref(false)
const chatOpenid = ref('')
const chatMessages = ref([])

const searchForm = reactive({
  openid: '',
  keyword: ''
})

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getChatList(searchForm)
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载对话列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleViewChat(row) {
  chatOpenid.value = row.openid
  chatVisible.value = true
  try {
    const res = await getUserChat(row.openid)
    chatMessages.value = res.data || []
  } catch (error) {
    chatMessages.value = []
  }
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.chat-list {
  padding: 20px;
}

.search-form {
  margin-bottom: 16px;
}

.chat-messages {
  padding: 16px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
}

.message-item.user {
  justify-content: flex-end;
}

.message-item.assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 80%;
  padding: 12px 16px;
  border-radius: 12px;
  position: relative;
}

.message-item.user .message-bubble {
  background: #409eff;
  color: #fff;
  border-bottom-right-radius: 4px;
}

.message-item.assistant .message-bubble {
  background: #f4f4f5;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-content {
  line-height: 1.6;
  word-break: break-word;
  white-space: pre-wrap;
}

.message-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
}

.message-item.user .message-time {
  text-align: right;
}

.no-messages {
  text-align: center;
  color: #909399;
  padding: 40px;
}
</style>
