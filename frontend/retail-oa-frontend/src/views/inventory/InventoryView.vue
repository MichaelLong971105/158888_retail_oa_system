<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Inventory Management</h3>
        <p>Perform stock in, stock out, and check product inventory logs.</p>
      </div>
    </div>

    <el-card>
      <el-table :data="productList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="Product Name" min-width="180" />
        <el-table-column prop="sku" label="SKU" width="140" />
        <el-table-column prop="supplierName" label="Supplier" min-width="150" />
        <el-table-column prop="unit" label="Unit" width="90" />
        <el-table-column label="Status" width="140">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Current Stock" width="140">
          <template #default="scope">
            <el-tag :type="isLowStock(scope.row) ? 'danger' : 'success'">
              {{ scope.row.stock }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="minStock" label="Min Stock" width="110" />
        <el-table-column prop="price" label="Price" width="120">
          <template #default="scope">
            {{ formatCurrency(scope.row.price) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="260" fixed="right">
          <template #default="scope">
            <el-button
              link
              type="success"
              :disabled="scope.row.status !== 'ACTIVE'"
              @click="openStockDialog('IN', scope.row)"
            >
              Stock In
            </el-button>
            <el-button
              link
              type="warning"
              :disabled="scope.row.status !== 'ACTIVE'"
              @click="openStockDialog('OUT', scope.row)"
            >
              Stock Out
            </el-button>
            <el-button link type="primary" @click="openLogDrawer(scope.row)">
              Logs
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'IN' ? 'Stock In' : 'Stock Out'"
      width="480px"
    >
      <el-form label-width="100px">
        <el-form-item label="Product">
          <el-input :model-value="selectedProduct?.name || ''" disabled />
        </el-form-item>
        <el-form-item label="Quantity">
          <el-input-number v-model="stockForm.quantity" :min="1" :step="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="stockForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitting" @click="submitStockChange">
          Confirm
        </el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="logDrawerVisible" title="Inventory Logs" size="50%">
      <el-table :data="inventoryLogs" style="width: 100%" v-loading="logsLoading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="changeType" label="Type" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.changeType === 'IN' ? 'success' : 'warning'">
              {{ scope.row.changeType }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="quantity" label="Quantity" width="120" />
        <el-table-column prop="remark" label="Remark" min-width="220" />
        <el-table-column prop="createdAt" label="Created At" width="180" />
      </el-table>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import {
  getAllProducts,
  getProductInventoryLogs,
  stockInProduct,
  stockOutProduct
} from '../../api/product'

const loading = ref(false)
const logsLoading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const logDrawerVisible = ref(false)
const dialogMode = ref('IN')
const productList = ref([])
const inventoryLogs = ref([])
const selectedProduct = ref(null)
const stockForm = reactive({
  quantity: 1,
  remark: ''
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

const loadProducts = async () => {
  loading.value = true

  try {
    const response = await getAllProducts()
    productList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load inventory data')
  } finally {
    loading.value = false
  }
}

const resetStockForm = () => {
  stockForm.quantity = 1
  stockForm.remark = ''
}

const openStockDialog = (mode, product) => {
  dialogMode.value = mode
  selectedProduct.value = product
  resetStockForm()
  dialogVisible.value = true
}

const submitStockChange = async () => {
  if (!selectedProduct.value) {
    return
  }

  submitting.value = true

  try {
    if (dialogMode.value === 'IN') {
      await stockInProduct(selectedProduct.value.id, stockForm)
      ElMessage.success('Stock in completed')
    } else {
      await stockOutProduct(selectedProduct.value.id, stockForm)
      ElMessage.success('Stock out completed')
    }

    dialogVisible.value = false
    await loadProducts()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to update stock')
  } finally {
    submitting.value = false
  }
}

const openLogDrawer = async (product) => {
  selectedProduct.value = product
  logDrawerVisible.value = true
  logsLoading.value = true

  try {
    const response = await getProductInventoryLogs(product.id)
    inventoryLogs.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load inventory logs')
  } finally {
    logsLoading.value = false
  }
}

onMounted(() => {
  loadProducts()
})
</script>

<style scoped>
.page-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.page-header h3 {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #606266;
}
</style>
