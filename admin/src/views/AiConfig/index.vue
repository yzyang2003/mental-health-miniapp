<template>
  <div class="ai-config">
    <el-card shadow="never">
      <template #header>
        <span>AI 配置</span>
      </template>

      <el-form label-width="120px">
        <el-form-item label="系统提示词">
          <el-input
            v-model="systemPrompt"
            type="textarea"
            :rows="10"
            placeholder="请输入 AI 系统提示词"
          />
        </el-form-item>

        <el-form-item label="情绪关键词">
          <div class="keyword-tags">
            <el-tag
              v-for="(keyword, index) in emotionKeywords"
              :key="index"
              closable
              @close="emotionKeywords.splice(index, 1)"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ keyword }}
            </el-tag>
            <el-input
              v-if="showEmotionInput"
              v-model="newEmotionKeyword"
              size="small"
              style="width: 120px"
              @keyup.enter="addEmotionKeyword"
              @blur="addEmotionKeyword"
            />
            <el-button v-else size="small" @click="showEmotionInput = true">
              + 添加关键词
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="危机关键词">
          <div class="keyword-tags">
            <el-tag
              v-for="(keyword, index) in crisisKeywords"
              :key="index"
              type="danger"
              closable
              @close="crisisKeywords.splice(index, 1)"
              style="margin-right: 8px; margin-bottom: 8px"
            >
              {{ keyword }}
            </el-tag>
            <el-input
              v-if="showCrisisInput"
              v-model="newCrisisKeyword"
              size="small"
              style="width: 120px"
              @keyup.enter="addCrisisKeyword"
              @blur="addCrisisKeyword"
            />
            <el-button v-else size="small" @click="showCrisisInput = true">
              + 添加关键词
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave">保存配置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const systemPrompt = ref('你是一个专业的校园心理陪伴助手，名叫"小爱"。你的职责是倾听学生的心声，提供情感支持和专业建议。请用温暖、理解的态度与用户交流，避免使用过于专业的术语，让对话感觉自然流畅。')

const emotionKeywords = ref(['开心', '难过', '焦虑', '生气', '害怕', '平静', '惊讶'])
const crisisKeywords = ref(['自杀', '轻生', '不想活', '自残', '结束生命'])

const showEmotionInput = ref(false)
const newEmotionKeyword = ref('')
const showCrisisInput = ref(false)
const newCrisisKeyword = ref('')

function addEmotionKeyword() {
  if (newEmotionKeyword.value.trim()) {
    emotionKeywords.value.push(newEmotionKeyword.value.trim())
    newEmotionKeyword.value = ''
  }
  showEmotionInput.value = false
}

function addCrisisKeyword() {
  if (newCrisisKeyword.value.trim()) {
    crisisKeywords.value.push(newCrisisKeyword.value.trim())
    newCrisisKeyword.value = ''
  }
  showCrisisInput.value = false
}

function handleSave() {
  ElMessage.success('配置保存成功（功能待实现）')
}
</script>

<style scoped>
.ai-config {
  padding: 20px;
}

.keyword-tags {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}
</style>
