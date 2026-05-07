<template>
  <div class="user-list">
    <el-card shadow="never">
      <el-form :inline="true" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchKeyword" placeholder="搜索昵称/openid" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadData">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="openid" label="OpenID" width="280" show-overflow-tooltip />
        <el-table-column label="头像" width="60">
          <template #default="{ row }">
            <el-avatar :size="32" :src="row.avatarUrl" />
          </template>
        </el-table-column>
        <el-table-column prop="nickName" label="昵称" width="120" />
        <el-table-column label="地区" width="150">
          <template #default="{ row }">
            {{ [row.province, row.city].filter(Boolean).join(' / ') || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="性别" width="80">
          <template #default="{ row }">
            {{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleViewDetail(row)">详情</el-button>
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

    <!-- 详情抽屉 -->
    <el-drawer v-model="detailVisible" title="用户详情" size="600px">
      <div v-if="currentUser">
        <!-- 基本信息 -->
        <div class="user-info-section">
          <div class="user-header">
            <el-avatar :size="64" :src="currentUser.avatarUrl" />
            <div class="info-text">
              <h3>{{ currentUser.nickName || '未设置昵称' }}</h3>
              <p>OpenID: {{ currentUser.openid }}</p>
              <p>地区: {{ [currentUser.province, currentUser.city].filter(Boolean).join(' / ') || '未知' }}</p>
            </div>
          </div>

          <el-divider />

          <!-- 统计数据 -->
          <el-row :gutter="16" class="stat-row">
            <el-col :span="8">
              <el-statistic title="测评次数" :value="userDetail.quizCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="AI对话" :value="userDetail.chatCount || 0" />
            </el-col>
            <el-col :span="8">
              <el-statistic title="树洞帖子" :value="userDetail.topicCount || 0" />
            </el-col>
          </el-row>
        </div>

        <el-divider />

        <!-- 标签页 -->
        <el-tabs v-model="activeTab" @tab-change="handleTabChange">
          <el-tab-pane label="测评记录" name="quizzes">
            <el-table :data="quizRecords" v-loading="quizLoading" stripe size="small">
              <el-table-column prop="questionnaireId" label="问卷ID" width="80" />
              <el-table-column prop="score" label="得分" width="80" />
              <el-table-column prop="scorePercent" label="得分率" width="80">
                <template #default="{ row }">{{ row.scorePercent }}%</template>
              </el-table-column>
              <el-table-column prop="conclusion" label="结论" min-width="120" show-overflow-tooltip />
              <el-table-column prop="createTime" label="时间" width="160">
                <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
              </el-table-column>
            </el-table>
            <div v-if="quizRecords.length === 0 && !quizLoading" class="no-data">暂无测评记录</div>
          </el-tab-pane>

          <el-tab-pane label="对话记录" name="chats">
            <div class="chat-list" v-loading="chatLoading">
              <div v-for="msg in chatRecords" :key="msg.id" class="chat-item" :class="msg.role">
                <div class="chat-bubble">
                  <div class="chat-role">{{ msg.role === 'user' ? '用户' : 'AI助手' }}</div>
                  <div class="chat-content">{{ msg.content?.substring(0, 200) }}{{ msg.content?.length > 200 ? '...' : '' }}</div>
                  <div class="chat-time">{{ formatTime(msg.createTime) }}</div>
                </div>
              </div>
              <div v-if="chatRecords.length === 0 && !chatLoading" class="no-data">暂无对话记录</div>
            </div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-drawer>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, getUserDetail, getUserQuizzes, getUserChats } from '@/api/user'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const detailVisible = ref(false)
const currentUser = ref(null)
const userDetail = ref({})
const activeTab = ref('quizzes')
const quizRecords = ref([])
const chatRecords = ref([])
const quizLoading = ref(false)
const chatLoading = ref(false)

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
    const res = await getUserList({
      page: pagination.page,
      size: pagination.size,
      keyword: searchKeyword.value
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

async function handleViewDetail(row) {
  currentUser.value = row
  activeTab.value = 'quizzes'
  quizRecords.value = []
  chatRecords.value = []

  try {
    const res = await getUserDetail(row.openid)
    userDetail.value = res.data || {}
  } catch (error) {
    userDetail.value = {}
  }

  detailVisible.value = true
  loadTabData('quizzes', row.openid)
}

async function handleTabChange(tab) {
  if (currentUser.value) {
    loadTabData(tab, currentUser.value.openid)
  }
}

async function loadTabData(tab, openid) {
  if (tab === 'quizzes' && quizRecords.value.length === 0) {
    quizLoading.value = true
    try {
      const res = await getUserQuizzes(openid)
      quizRecords.value = res.data || []
    } catch (error) {
      quizRecords.value = []
    } finally {
      quizLoading.value = false
    }
  } else if (tab === 'chats' && chatRecords.value.length === 0) {
    chatLoading.value = true
    try {
      const res = await getUserChats(openid)
      chatRecords.value = res.data || []
    } catch (error) {
      chatRecords.value = []
    } finally {
      chatLoading.value = false
    }
  }
}

function formatTime(time) {
  if (!time) return '-'
  return new Date(time).toLocaleString('zh-CN')
}
</script>

<style scoped>
.user-list {
  padding: 20px;
}

.search-form {
  margin-bottom: 16px;
}

.user-info-section {
  padding: 16px;
}

.user-header {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-text h3 {
  margin: 0 0 8px 0;
  color: #303133;
}

.info-text p {
  margin: 4px 0;
  color: #606266;
  font-size: 14px;
}

.stat-row {
  margin-top: 16px;
}

.chat-list {
  max-height: 400px;
  overflow-y: auto;
}

.chat-item {
  margin-bottom: 12px;
  display: flex;
}

.chat-item.user {
  justify-content: flex-end;
}

.chat-item.assistant {
  justify-content: flex-start;
}

.chat-bubble {
  max-width: 80%;
  padding: 10px 14px;
  border-radius: 10px;
  background: #f4f4f5;
}

.chat-item.user .chat-bubble {
  background: #409eff;
  color: #fff;
}

.chat-role {
  font-size: 11px;
  font-weight: bold;
  margin-bottom: 4px;
  opacity: 0.7;
}

.chat-content {
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}

.chat-time {
  font-size: 11px;
  opacity: 0.6;
  margin-top: 4px;
}

.no-data {
  text-align: center;
  color: #909399;
  padding: 20px;
}
</style>
