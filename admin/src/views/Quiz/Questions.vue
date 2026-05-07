<template>
  <div class="quiz-questions">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div>
            <el-button @click="goBack" style="margin-right: 16px">返回</el-button>
            <span>题目管理 - {{ quizTitle }}</span>
          </div>
          <el-button type="primary" @click="handleAdd">新增题目</el-button>
        </div>
      </template>

      <el-table :data="questions" v-loading="loading" stripe>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="content" label="题目内容" min-width="300" show-overflow-tooltip />
        <el-table-column label="选项数" width="100">
          <template #default="{ row }">
            {{ row.options?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row, $index }">
            <el-button type="primary" link @click="handleEdit(row, $index)">编辑</el-button>
            <el-button type="danger" link @click="handleRemove($index)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="save-bar">
        <el-button type="primary" :loading="saving" @click="handleSaveAll">
          保存所有更改
        </el-button>
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingIndex >= 0 ? '编辑题目' : '新增题目'" width="600px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="题目内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="1" />
        </el-form-item>
        <el-form-item label="选项" required>
          <div v-for="(option, index) in form.options" :key="index" class="option-item">
            <el-input v-model="option.text" placeholder="选项文本" style="width: 200px" />
            <el-input-number v-model="option.score" placeholder="分数" style="width: 120px; margin-left: 8px" />
            <el-button
              type="danger"
              :icon="Delete"
              circle
              size="small"
              @click="form.options.splice(index, 1)"
              style="margin-left: 8px"
            />
          </div>
          <el-button type="primary" link @click="addOption" style="margin-top: 8px">
            <el-icon><Plus /></el-icon> 添加选项
          </el-button>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveQuestion">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import { getQuizDetail, getQuizQuestions, saveQuizQuestions } from '@/api/quiz'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const quizTitle = ref('')
const questions = ref([])
const dialogVisible = ref(false)
const editingIndex = ref(-1)
const formRef = ref(null)

const form = reactive({
  content: '',
  sortOrder: 1,
  options: [{ text: '', score: 0 }]
})

const rules = {
  content: [{ required: true, message: '请输入题目内容', trigger: 'blur' }]
}

onMounted(async () => {
  await loadQuizInfo()
  await loadQuestions()
})

async function loadQuizInfo() {
  try {
    const res = await getQuizDetail(route.params.id)
    quizTitle.value = res.data?.title || ''
  } catch (error) {
    console.error('加载问卷信息失败:', error)
  }
}

async function loadQuestions() {
  loading.value = true
  try {
    const res = await getQuizQuestions(route.params.id)
    questions.value = res.data || []
  } catch (error) {
    console.error('加载题目失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingIndex.value = -1
  Object.assign(form, {
    content: '',
    sortOrder: questions.value.length + 1,
    options: [{ text: '', score: 0 }]
  })
  dialogVisible.value = true
}

function handleEdit(row, index) {
  editingIndex.value = index
  Object.assign(form, {
    content: row.content,
    sortOrder: row.sortOrder,
    options: JSON.parse(JSON.stringify(row.options || []))
  })
  dialogVisible.value = true
}

function addOption() {
  form.options.push({ text: '', score: 0 })
}

async function handleRemove(index) {
  await ElMessageBox.confirm('确定删除这道题目吗？', '提示', { type: 'warning' })
  questions.value.splice(index, 1)
}

function handleSaveQuestion() {
  const question = {
    content: form.content,
    sortOrder: form.sortOrder,
    options: form.options.filter(opt => opt.text.trim()),
    status: 1
  }

  if (editingIndex.value >= 0) {
    questions.value[editingIndex.value] = {
      ...questions.value[editingIndex.value],
      ...question
    }
  } else {
    questions.value.push(question)
  }

  dialogVisible.value = false
}

async function handleSaveAll() {
  saving.value = true
  try {
    await saveQuizQuestions(route.params.id, questions.value)
    ElMessage.success('保存成功')
    await loadQuestions()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/quiz')
}
</script>

<style scoped>
.quiz-questions {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.option-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.save-bar {
  margin-top: 20px;
  text-align: center;
}
</style>
