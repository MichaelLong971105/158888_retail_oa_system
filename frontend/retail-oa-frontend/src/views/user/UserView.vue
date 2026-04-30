<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>User Management</h3>
        <p>Manage internal user accounts, account status, and additional permissions.</p>
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
        <el-table-column label="Status" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.enabled ? 'success' : 'danger'">
              {{ scope.row.enabled ? 'ACTIVE' : 'DISABLED' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Extra Permissions" min-width="260">
          <template #default="scope">
            <div v-if="scope.row.additionalPermissions?.length">
              {{ scope.row.additionalPermissions.join(', ') }}
            </div>
            <span v-else class="empty-text">None</span>
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
      width="680px"
    >
      <el-form label-width="130px">
        <el-form-item label="Username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item :label="form.id ? 'New Password' : 'Password'">
          <el-input v-model="form.password" type="password" show-password />
          <div v-if="form.id" class="helper-text">Leave blank to keep the current password.</div>
        </el-form-item>
        <el-form-item label="Role">
          <el-select v-model="form.role" style="width: 100%">
            <el-option label="ADMIN" value="ADMIN" />
            <el-option label="MANAGER" value="MANAGER" />
            <el-option label="STAFF" value="STAFF" />
          </el-select>
        </el-form-item>
        <el-form-item label="Enabled">
          <el-switch v-model="form.enabled" />
        </el-form-item>
        <el-form-item label="Extra Permissions">
          <el-select
            v-model="form.additionalPermissions"
            multiple
            collapse-tags
            collapse-tags-tooltip
            style="width: 100%"
          >
            <el-option
              v-for="permission in permissionOptions"
              :key="permission.value"
              :label="permission.label"
              :value="permission.value"
            />
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

const permissionOptions = [
  { value: 'MANAGE_USERS', label: 'Manage Users' },
  { value: 'MANAGE_PRODUCTS', label: 'Manage Products' },
  { value: 'MANAGE_SUPPLIERS', label: 'Manage Suppliers' },
  { value: 'MANAGE_ORDERS', label: 'Manage Orders' },
  { value: 'MANAGE_INVENTORY', label: 'Manage Inventory' },
  { value: 'VIEW_SALES', label: 'View Sales' },
  { value: 'MANAGE_SALES', label: 'Manage Sales' },
  { value: 'MANAGE_POS', label: 'Manage POS Integration' },
  { value: 'MANAGE_ATTENDANCE', label: 'Manage Attendance' },
  { value: 'APPROVE_LEAVE', label: 'Approve Leave' },
  { value: 'VIEW_ATTENDANCE', label: 'View Attendance' }
]

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const userList = ref([])
const form = reactive({
  id: null,
  username: '',
  email: '',
  password: '',
  role: 'STAFF',
  enabled: true,
  additionalPermissions: []
})

const resetForm = () => {
  form.id = null
  form.username = ''
  form.email = ''
  form.password = ''
  form.role = 'STAFF'
  form.enabled = true
  form.additionalPermissions = []
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
  form.password = ''
  form.role = user.role || 'STAFF'
  form.enabled = user.enabled !== false
  form.additionalPermissions = [...(user.additionalPermissions || [])]
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.username.trim() || !form.email.trim()) {
    ElMessage.warning('Username and email are required')
    return
  }

  if (!form.id && !form.password.trim()) {
    ElMessage.warning('Password is required when creating a user')
    return
  }

  const payload = {
    username: form.username.trim(),
    email: form.email.trim(),
    password: form.password,
    role: form.role,
    enabled: form.enabled,
    additionalPermissions: form.additionalPermissions
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

.helper-text,
.empty-text {
  color: #909399;
  font-size: 12px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}
</style>
