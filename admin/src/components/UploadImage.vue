<template>
  <div class="upload-image">
    <el-upload
      class="image-uploader"
      :show-file-list="false"
      :before-upload="beforeUpload"
      :http-request="handleUpload"
    >
      <el-image v-if="modelValue" :src="modelValue" fit="cover" class="image-preview" />
      <el-icon v-else class="uploader-icon"><Plus /></el-icon>
    </el-upload>
    <div v-if="modelValue" class="image-actions">
      <el-button type="danger" size="small" @click="$emit('update:modelValue', '')">
        删除
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/upload'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  }
})

const emit = defineEmits(['update:modelValue'])

function beforeUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isImage) {
    ElMessage.error('只能上传图片文件')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB')
    return false
  }
  return true
}

async function handleUpload(options) {
  try {
    const res = await uploadFile(options.file)
    emit('update:modelValue', res.data.url)
    ElMessage.success('上传成功')
  } catch (error) {
    ElMessage.error('上传失败')
  }
}
</script>

<style scoped>
.upload-image {
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.image-uploader {
  width: 120px;
  height: 120px;
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-uploader:hover {
  border-color: #409eff;
}

.image-preview {
  width: 100%;
  height: 100%;
}

.uploader-icon {
  font-size: 28px;
  color: #8c939d;
}

.image-actions {
  margin-bottom: 8px;
}
</style>
