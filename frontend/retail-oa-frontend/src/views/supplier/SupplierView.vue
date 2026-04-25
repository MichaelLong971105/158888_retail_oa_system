<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Supplier Management</h3>
        <p>Maintain supplier master data for procurement orders.</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">Add Supplier</el-button>
    </div>

    <el-card>
      <el-table :data="supplierList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="Supplier Name" min-width="180" />
        <el-table-column prop="contactPerson" label="Contact Person" width="160" />
        <el-table-column prop="phone" label="Phone" width="150" />
        <el-table-column prop="email" label="Email" min-width="200" />
        <el-table-column prop="address" label="Address" min-width="220" />
        <el-table-column prop="remark" label="Remark" min-width="180" />
        <el-table-column prop="createdAt" label="Created At" width="180" />
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
      :title="form.id ? 'Edit Supplier' : 'Add Supplier'"
      width="620px"
    >
      <el-form label-width="120px">
        <el-form-item label="Name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="Contact Person">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="Phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="Email">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="Address">
          <el-input v-model="form.address" />
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
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
import {
  createSupplier,
  deleteSupplier,
  getAllSuppliers,
  updateSupplier
} from '../../api/supplier'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const supplierList = ref([])
const form = reactive({
  id: null,
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  remark: ''
})

const resetForm = () => {
  form.id = null
  form.name = ''
  form.contactPerson = ''
  form.phone = ''
  form.email = ''
  form.address = ''
  form.remark = ''
}

const loadSuppliers = async () => {
  loading.value = true

  try {
    const response = await getAllSuppliers()
    supplierList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || error.response?.data || 'Failed to load suppliers')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = () => {
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (supplier) => {
  form.id = supplier.id
  form.name = supplier.name || ''
  form.contactPerson = supplier.contactPerson || ''
  form.phone = supplier.phone || ''
  form.email = supplier.email || ''
  form.address = supplier.address || ''
  form.remark = supplier.remark || ''
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name.trim()) {
    ElMessage.warning('Supplier name is required')
    return
  }

  const payload = {
    name: form.name,
    contactPerson: form.contactPerson,
    phone: form.phone,
    email: form.email,
    address: form.address,
    remark: form.remark
  }

  submitting.value = true

  try {
    if (form.id) {
      await updateSupplier(form.id, payload)
      ElMessage.success('Supplier updated successfully')
    } else {
      await createSupplier(payload)
      ElMessage.success('Supplier created successfully')
    }

    dialogVisible.value = false
    await loadSuppliers()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || error.response?.data || 'Failed to save supplier')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (supplier) => {
  try {
    await ElMessageBox.confirm(
      `Delete supplier "${supplier.name}"?`,
      'Confirm',
      { type: 'warning' }
    )

    await deleteSupplier(supplier.id)
    ElMessage.success('Supplier deleted successfully')
    await loadSuppliers()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.error || error.response?.data || 'Failed to delete supplier')
    }
  }
}

onMounted(() => {
  loadSuppliers()
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
