<template>
  <div class="article-edit">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>{{ isEdit ? '编辑文章' : '新建文章' }}</span>
          <el-button @click="goBack">返回</el-button>
        </div>
      </template>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <!-- AI 填充 -->
        <el-form-item label="AI 填充">
          <div style="display: flex; gap: 8px; width: 100%">
            <el-input v-model="aiFillUrl" placeholder="粘贴网页链接，自动填充标题和内容" />
            <el-button type="primary" :loading="aiFilling" @click="handleAiFill">AI 填充</el-button>
          </div>
        </el-form-item>

        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">发布</el-radio>
            <el-radio :value="0">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="正文" prop="content">
          <RichTextEditor v-model="form.content" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getArticleDetail, createArticle, updateArticle, aiFillArticle } from '@/api/article'
import RichTextEditor from '@/components/RichTextEditor.vue'

const route = useRoute()
const router = useRouter()
const formRef = ref(null)
const saving = ref(false)
const aiFillUrl = ref('')
const aiFilling = ref(false)

const isEdit = computed(() => !!route.params.id)

const form = reactive({
  title: '',
  summary: '',
  content: '',
  status: 1
})

const rules = {
  title: [{ required: true, message: '请输入文章标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入文章正文', trigger: 'blur' }]
}

onMounted(async () => {
  if (isEdit.value) {
    await loadArticle()
  }
})

async function loadArticle() {
  try {
    const res = await getArticleDetail(route.params.id)
    Object.assign(form, res.data)
  } catch (error) {
    ElMessage.error('加载文章失败')
    goBack()
  }
}

async function handleAiFill() {
  if (!aiFillUrl.value) {
    ElMessage.warning('请输入网页链接')
    return
  }
  aiFilling.value = true
  try {
    const res = await aiFillArticle(aiFillUrl.value)
    if (res.data.title) form.title = res.data.title
    if (res.data.content) form.content = res.data.content
    if (res.data.summary) form.summary = res.data.summary
    ElMessage.success('AI 填充完成')
  } catch (error) {
    ElMessage.error('AI 填充失败')
  } finally {
    aiFilling.value = false
  }
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (isEdit.value) {
      await updateArticle(route.params.id, form)
      ElMessage.success('更新成功')
    } else {
      await createArticle(form)
      ElMessage.success('创建成功')
    }
    goBack()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

function goBack() {
  router.push('/article')
}
</script>

<style scoped>
.article-edit {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
