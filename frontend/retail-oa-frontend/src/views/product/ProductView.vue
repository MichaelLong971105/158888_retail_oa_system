<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Product Management</h3>
        <p>Maintain complete product master data for inventory and procurement.</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">Add Product</el-button>
    </div>

    <el-card>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="Filter by status" clearable style="width: 220px">
          <el-option label="All Statuses" value="" />
          <el-option label="ACTIVE" value="ACTIVE" />
          <el-option label="INACTIVE" value="INACTIVE" />
          <el-option label="DISCONTINUED" value="DISCONTINUED" />
        </el-select>
      </div>

      <el-table :data="filteredProducts" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="Product Name" min-width="180" />
        <el-table-column prop="sku" label="SKU" width="140" />
        <el-table-column prop="barcode" label="Barcode" width="150" />
        <el-table-column prop="brand" label="Brand" width="130" />
        <el-table-column prop="category" label="Category" width="130" />
        <el-table-column prop="specification" label="Specification" width="140" />
        <el-table-column prop="unit" label="Unit" width="90" />
        <el-table-column prop="supplierName" label="Supplier" min-width="160" />
        <el-table-column prop="price" label="Price" width="120">
          <template #default="scope">
            {{ formatCurrency(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column label="Stock" width="110">
          <template #default="scope">
            <el-tag :type="isLowStock(scope.row) ? 'danger' : 'success'">
              {{ scope.row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="Min Stock" width="110" />
        <el-table-column label="Status" width="140">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="Description" min-width="180" show-overflow-tooltip />
        <el-table-column prop="updatedAt" label="Updated At" width="180" />
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
      :title="form.id ? 'Edit Product' : 'Add Product'"
      width="760px"
    >
      <el-form label-width="120px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Product Name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="SKU">
              <el-input v-model="form.sku" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Barcode">
              <el-input v-model="form.barcode" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Brand">
              <el-input v-model="form.brand" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Category">
              <el-input v-model="form.category" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Specification">
              <el-input v-model="form.specification" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Unit">
              <el-input v-model="form.unit" placeholder="e.g. pcs / bottle / box" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Supplier">
              <el-select v-model="form.supplierId" clearable style="width: 100%">
                <el-option
                  v-for="supplier in supplierOptions"
                  :key="supplier.id"
                  :label="supplier.name"
                  :value="supplier.id"
                />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Price">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Stock">
              <el-input-number v-model="form.stock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="Min Stock">
              <el-input-number v-model="form.minStock" :min="0" :step="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="Status">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="ACTIVE" value="ACTIVE" />
                <el-option label="INACTIVE" value="INACTIVE" />
                <el-option label="DISCONTINUED" value="DISCONTINUED" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="Description">
          <el-input v-model="form.description" type="textarea" :rows="3" />
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  createProduct,
  deleteProduct,
  getAllProducts,
  updateProduct
} from '../../api/product'
import { getAllSuppliers } from '../../api/supplier'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const statusFilter = ref('')
const productList = ref([])
const supplierOptions = ref([])
const form = reactive({
  id: null,
  name: '',
  sku: '',
  barcode: '',
  category: '',
  brand: '',
  specification: '',
  unit: '',
  price: 0,
  stock: 0,
  minStock: 0,
  status: 'ACTIVE',
  description: '',
  supplierId: null
})

const filteredProducts = computed(() => {
  if (!statusFilter.value) {
    return productList.value
  }

  return productList.value.filter(product => product.status === statusFilter.value)
})

const formatCurrency = (value) => {
  const amount = Number(value || 0)
  return `$${amount.toFixed(2)}`
}

const getStatusTagType = (status) => {
  if (status === 'ACTIVE') {
    return 'success'
  }

  if (status === 'INACTIVE') {
    return 'warning'
  }

  return 'info'
}

const isLowStock = (product) => {
  return Number(product.stock || 0) <= Number(product.minStock || 0)
}

const resetForm = () => {
  form.id = null
  form.name = ''
  form.sku = ''
  form.barcode = ''
  form.category = ''
  form.brand = ''
  form.specification = ''
  form.unit = ''
  form.price = 0
  form.stock = 0
  form.minStock = 0
  form.status = 'ACTIVE'
  form.description = ''
  form.supplierId = null
}

const normalizeOptional = (value) => {
  const normalized = typeof value === 'string' ? value.trim() : value
  return normalized ? normalized : null
}

const loadProducts = async () => {
  loading.value = true

  try {
    const response = await getAllProducts()
    productList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load products')
  } finally {
    loading.value = false
  }
}

const loadSuppliers = async () => {
  try {
    const response = await getAllSuppliers()
    supplierOptions.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || error.response?.data || 'Failed to load suppliers')
  }
}

const openCreateDialog = async () => {
  resetForm()
  await loadSuppliers()
  dialogVisible.value = true
}

const openEditDialog = async (product) => {
  await loadSuppliers()
  form.id = product.id
  form.name = product.name || ''
  form.sku = product.sku || ''
  form.barcode = product.barcode || ''
  form.category = product.category || ''
  form.brand = product.brand || ''
  form.specification = product.specification || ''
  form.unit = product.unit || ''
  form.price = Number(product.price || 0)
  form.stock = Number(product.stock || 0)
  form.minStock = Number(product.minStock || 0)
  form.status = product.status || 'ACTIVE'
  form.description = product.description || ''
  form.supplierId = product.supplierId || null
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!form.name.trim() || !form.sku.trim() || !form.unit.trim()) {
    ElMessage.warning('Product name, SKU and unit are required')
    return
  }

  const payload = {
    name: form.name.trim(),
    sku: form.sku.trim(),
    barcode: normalizeOptional(form.barcode),
    category: normalizeOptional(form.category),
    brand: normalizeOptional(form.brand),
    specification: normalizeOptional(form.specification),
    unit: form.unit.trim(),
    price: Number(form.price || 0),
    stock: Number(form.stock || 0),
    minStock: Number(form.minStock || 0),
    status: form.status,
    description: normalizeOptional(form.description),
    supplierId: form.supplierId || null
  }

  submitting.value = true

  try {
    if (form.id) {
      await updateProduct(form.id, payload)
      ElMessage.success('Product updated successfully')
    } else {
      await createProduct(payload)
      ElMessage.success('Product created successfully')
    }

    dialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to save product')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (product) => {
  try {
    await ElMessageBox.confirm(
      `Delete product "${product.name}"?`,
      'Confirm',
      { type: 'warning' }
    )

    await deleteProduct(product.id)
    ElMessage.success('Product deleted successfully')
    await loadProducts()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.error || 'Failed to delete product')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadProducts(), loadSuppliers()])
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

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 16px;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .toolbar {
    justify-content: flex-start;
  }
}
</style>
