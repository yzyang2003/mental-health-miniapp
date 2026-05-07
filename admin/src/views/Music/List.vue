<template>
  <div class="music-list">
    <el-card shadow="never">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>新增音乐
        </el-button>
      </div>

      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="歌单名称" min-width="150" />
        <el-table-column prop="description" label="描述" min-width="150" show-overflow-tooltip />
        <el-table-column label="封面" width="80">
          <template #default="{ row }">
            <el-image
              v-if="row.coverImage"
              :src="row.coverImage"
              fit="cover"
              style="width: 40px; height: 40px; border-radius: 4px"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="shareUrl" label="歌单链接" min-width="200" show-overflow-tooltip />
        <el-table-column prop="durationText" label="时长" width="100" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-popconfirm title="确定删除这首音乐吗？" @confirm="handleDelete(row)">
              <template #reference>
                <el-button type="danger" link>删除</el-button>
              </template>
            </el-popconfirm>
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

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑音乐' : '新增音乐'" width="500px">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="歌单名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入歌单名称" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="封面">
          <el-input v-model="form.coverImage" placeholder="请输入封面URL" />
        </el-form-item>
        <el-form-item label="歌单链接" prop="shareUrl">
          <el-input v-model="form.shareUrl" placeholder="请输入歌单链接" />
        </el-form-item>
        <el-form-item label="时长">
          <el-input v-model="form.durationText" placeholder="如：60分钟" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getMusicList, createMusic, updateMusic, deleteMusic } from '@/api/music'

const loading = ref(false)
const saving = ref(false)
const tableData = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref(null)

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const form = reactive({
  name: '',
  description: '',
  coverImage: '',
  shareUrl: '',
  durationText: ''
})

const rules = {
  name: [{ required: true, message: '请输入歌单名称', trigger: 'blur' }],
  shareUrl: [{ required: true, message: '请输入歌单链接', trigger: 'blur' }]
}

onMounted(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMusicList({
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = res.data?.records || []
    pagination.total = res.data?.total || 0
  } catch (error) {
    console.error('加载音乐列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  editingId.value = null
  Object.assign(form, { name: '', description: '', coverImage: '', shareUrl: '', durationText: '' })
  dialogVisible.value = true
}

function handleEdit(row) {
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    description: row.description,
    coverImage: row.coverImage,
    shareUrl: row.shareUrl,
    durationText: row.durationText
  })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      await updateMusic(editingId.value, form)
      ElMessage.success('更新成功')
    } else {
      await createMusic(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await deleteMusic(row.id)
    ElMessage.success('删除成功')
    loadData()
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
</script>

<style scoped>
.music-list {
  padding: 20px;
}

.toolbar {
  margin-bottom: 16px;
}
</style>
