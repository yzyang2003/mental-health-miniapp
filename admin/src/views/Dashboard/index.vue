<template>
  <div class="dashboard-container">
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.todayNew || 0 }}</div>
              <div class="stat-label">今日新增</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon size="24"><EditPen /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalQuizCount || 0 }}</div>
              <div class="stat-label">测评次数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="12" :sm="12" :md="6">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon size="24"><Comment /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ stats.totalTopics || 0 }}</div>
              <div class="stat-label">帖子总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 - 6 个图表 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>用户注册趋势（近7天）</span></template>
          <div ref="userTrendRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>日活用户趋势（近7天）</span></template>
          <div ref="dailyActiveRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>测评总体趋势（近7天）</span></template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
    <el-row :gutter="20" class="chart-row">
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>测评量表使用率</span></template>
          <div ref="quizByTypeRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>文章统计</span></template>
          <div ref="articleChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="8">
        <el-card shadow="hover">
          <template #header><span>情绪分布</span></template>
          <div ref="emotionChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted, nextTick } from 'vue'
import { User, EditPen, TrendCharts, Comment } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getDashboardStats } from '@/api/dashboard'

const stats = ref({})
const userTrendRef = ref(null)
const dailyActiveRef = ref(null)
const trendChartRef = ref(null)
const quizByTypeRef = ref(null)
const articleChartRef = ref(null)
const emotionChartRef = ref(null)

const charts = []

onMounted(async () => {
  await loadStats()
  await nextTick()
  initCharts()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  charts.forEach(c => c?.dispose())
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  charts.forEach(c => c?.resize())
}

async function loadStats() {
  try {
    const res = await getDashboardStats()
    stats.value = res.data || {}
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

function initCharts() {
  // 1. 用户注册趋势
  if (userTrendRef.value) {
    const c = echarts.init(userTrendRef.value)
    charts.push(c)
    const data = stats.value.userTrend || []
    c.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(i => i.date?.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ data: data.map(i => i.count), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#409eff' } }]
    })
  }

  // 2. 日活用户趋势
  if (dailyActiveRef.value) {
    const c = echarts.init(dailyActiveRef.value)
    charts.push(c)
    const data = stats.value.dailyActive || []
    c.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(i => i.date?.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ data: data.map(i => i.count), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#67c23a' } }]
    })
  }

  // 3. 测评总体趋势
  if (trendChartRef.value) {
    const c = echarts.init(trendChartRef.value)
    charts.push(c)
    const data = stats.value.trendData || []
    c.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: { type: 'category', data: data.map(i => i.date?.slice(5)) },
      yAxis: { type: 'value', minInterval: 1 },
      series: [{ data: data.map(i => i.count), type: 'line', smooth: true, areaStyle: { opacity: 0.3 }, itemStyle: { color: '#e6a23c' } }]
    })
  }

  // 4. 测评量表使用率饼图
  if (quizByTypeRef.value) {
    const c = echarts.init(quizByTypeRef.value)
    charts.push(c)
    const data = stats.value.quizByType || {}
    const pieData = Object.entries(data).map(([name, value]) => ({ name, value }))
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left', textStyle: { fontSize: 11 } },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: pieData, emphasis: { itemStyle: { shadowBlur: 10 } } }]
    })
  }

  // 5. 文章统计饼图
  if (articleChartRef.value) {
    const c = echarts.init(articleChartRef.value)
    charts.push(c)
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{
        type: 'pie', radius: ['40%', '70%'],
        data: [
          { name: '文章总数', value: stats.value.totalArticles || 0 },
          { name: '帖子总数', value: stats.value.totalTopics || 0 },
          { name: '对话消息', value: stats.value.totalChatMessages || 0 }
        ],
        emphasis: { itemStyle: { shadowBlur: 10 } }
      }]
    })
  }

  // 6. 情绪分布饼图
  if (emotionChartRef.value) {
    const c = echarts.init(emotionChartRef.value)
    charts.push(c)
    const emotionData = stats.value.emotionDistribution || {}
    const emotionLabels = { happy: '开心', sad: '难过', anxious: '焦虑', angry: '生气', fearful: '害怕', neutral: '平静', surprised: '惊讶' }
    const pieData = Object.entries(emotionData).map(([key, value]) => ({ name: emotionLabels[key] || key, value }))
    if (pieData.length === 0) {
      pieData.push({ name: '暂无数据', value: 1 })
    }
    c.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: pieData, emphasis: { itemStyle: { shadowBlur: 10 } } }]
    })
  }
}
</script>

<style scoped>
.dashboard-container {
  padding: 20px;
}

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  height: 100px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.chart-row {
  margin-top: 20px;
}

.chart-container {
  height: 300px;
}
</style>
