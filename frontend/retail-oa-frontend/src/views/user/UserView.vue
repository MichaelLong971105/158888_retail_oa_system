<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>User Management</h3>
        <p>Manage internal user accounts and role assignments.</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">Add User</el-button>
    </div>

    <el-card>
      <el-table :data="userList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="Username" min-width="160" />
        <el-table-column prop="email" label="Email" min-width="220" />
        <el-table-column prop="role" label="Role" width="140">
          <template #default="scope">
            <el-tag :type="getRoleTagType(scope.row.role)">
              {{ scope.row.role }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">Edit</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? 'Edit User' : 'Add User'"
      width="560px"
    >
      <el-form label-width="100px">
        <el-form-item label="Username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="Password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="Role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="ADMIN" value="ADMIN" />
            <el-option label="MANAGER" value="MANAGER" />
            <el-option label="STAFF" value="STAFF" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">
          Save
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createUser, deleteUser, getAllUsers, updateUser } from '../../api/user'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const userList = ref([])
const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 'STAFF'
})

const resetForm = () => {
  form.id = null
  form.username = ''
  form.email = ''
  form.password = ''
  form.role = 'STAFF'
}

const getRoleTagType = (role) => {
  if (role === 'ADMIN') {
    return 'danger'
  }

  if (role === 'MANAGER') {
    return 'warning'
  }

  return 'success'
}

const loadUsers = async () => {
  loading.value = true

  try {
    const response = await getAllUsers()
    userList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load users')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (user) => {
  form.id = user.id
  form.username = user.username || ''
  form.email = user.email || ''
  form.password = user.password || ''
  form.role = user.role || 'STAFF'
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.username.trim() || !form.email.trim() || !form.password.trim()) {
    ElMessage.warning('Username, email and password are required')
    return
  }

  const payload = {
    username: form.username,
    email: form.email,
    password: form.password,
    role: form.role
  }

  submitting.value = true

  try {
    if (form.id) {
      await updateUser(form.id, payload)
      ElMessage.success('User updated successfully')
    } else {
      await createUser(payload)
      ElMessage.success('User created successfully')
    }

    dialogVisible.value = false
    await loadUsers()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to save user')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `Delete user "${user.username}"?`,
      'Confirm',
      { type: 'warning' }
    )

    await deleteUser(user.id)
    ElMessage.success('User deleted successfully')
    await loadUsers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.error || 'Failed to delete user')
    }
  }
}

onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h3 {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #606266;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
