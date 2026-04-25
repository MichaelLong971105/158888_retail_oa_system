<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Order Management</h3>
        <p>Create and track purchase orders from suppliers.</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">Create Order</el-button>
    </div>

    <el-card>
      <div class="toolbar">
        <el-select v-model="statusFilter" placeholder="Filter by status" clearable style="width: 220px" @change="loadOrders">
          <el-option label="All Statuses" value="" />
          <el-option label="PENDING" value="PENDING" />
          <el-option label="RECEIVED" value="RECEIVED" />
          <el-option label="CANCELLED" value="CANCELLED" />
        </el-select>
      </div>

      <el-table :data="orderList" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNumber" label="Order No." width="180" />
        <el-table-column prop="supplierName" label="Supplier" min-width="180" />
        <el-table-column prop="totalAmount" label="Amount" width="140">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="Status" width="130">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="Created At" width="180" />
        <el-table-column label="Items" min-width="260">
          <template #default="scope">
            <div v-for="item in scope.row.items" :key="item.id || `${scope.row.id}-${item.productId}`" class="item-line">
              {{ item.productName }} x {{ item.quantity }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="220" fixed="right">
          <template #default="scope">
            <el-button
              v-if="scope.row.status === 'PENDING'"
              link
              type="success"
              @click="handleStatusUpdate(scope.row, 'RECEIVED')"
            >
              Mark Received
            </el-button>
            <el-button
              v-if="scope.row.status === 'PENDING'"
              link
              type="danger"
              @click="handleStatusUpdate(scope.row, 'CANCELLED')"
            >
              Cancel
            </el-button>
            <span v-if="scope.row.status !== 'PENDING'" class="terminal-text">Terminal</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="Create Order" width="760px">
      <el-form label-width="110px">
        <el-form-item label="Supplier">
          <el-select v-model="orderForm.supplierId" placeholder="Select supplier" style="width: 100%">
            <el-option
              v-for="supplier in supplierOptions"
              :key="supplier.id"
              :label="supplier.name"
              :value="supplier.id"
            />
          </el-select>
        </el-form-item>

        <div class="item-block">
          <div class="item-header">
            <span>Order Items</span>
            <el-button type="primary" plain @click="addOrderItem">Add Item</el-button>
          </div>

          <div v-for="(item, index) in orderForm.items" :key="index" class="item-row">
            <el-select v-model="item.productId" placeholder="Product" style="width: 46%">
              <el-option
                v-for="product in productOptions"
                :key="product.id"
                :label="`${product.name} (${product.sku})`"
                :value="product.id"
              />
            </el-select>
            <el-input-number v-model="item.quantity" :min="1" :step="1" style="width: 26%" />
            <el-button
              type="danger"
              plain
              :disabled="orderForm.items.length === 1"
              @click="removeOrderItem(index)"
            >
              Remove
            </el-button>
          </div>
        </div>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submitting" @click="submitOrder">
          Create
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createOrder, getAllOrders, updateOrderStatus } from '../../api/order'
import { getAllSuppliers } from '../../api/supplier'
import { getAllProducts } from '../../api/product'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const statusFilter = ref('')
const orderList = ref([])
const supplierOptions = ref([])
const productOptions = ref([])
const orderForm = reactive({
  supplierId: null,
  items: [
    {
      productId: null,
      quantity: 1
    }
  ]
})

const formatCurrency = (value) => {
  const amount = Number(value || 0)
  return `$${amount.toFixed(2)}`
}

const getStatusType = (status) => {
  if (status === 'RECEIVED') {
    return 'success'
  }

  if (status === 'CANCELLED') {
    return 'danger'
  }

  return 'warning'
}

const resetOrderForm = () => {
  orderForm.supplierId = null
  orderForm.items = [
    {
      productId: null,
      quantity: 1
    }
  ]
}

const loadOrders = async () => {
  loading.value = true

  try {
    const response = await getAllOrders(statusFilter.value)
    orderList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load orders')
  } finally {
    loading.value = false
  }
}

const loadOptions = async () => {
  try {
    const [suppliersResponse, productsResponse] = await Promise.all([
      getAllSuppliers(),
      getAllProducts()
    ])

    supplierOptions.value = suppliersResponse.data || []
    productOptions.value = (productsResponse.data || []).filter(product => product.status === 'ACTIVE')
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load order form data')
  }
}

const openCreateDialog = async () => {
  resetOrderForm()
  await loadOptions()
  dialogVisible.value = true
}

const addOrderItem = () => {
  orderForm.items.push({
    productId: null,
    quantity: 1
  })
}

const removeOrderItem = (index) => {
  if (orderForm.items.length === 1) {
    return
  }

  orderForm.items.splice(index, 1)
}

const submitOrder = async () => {
  if (!orderForm.supplierId) {
    ElMessage.warning('Please select a supplier')
    return
  }

  const hasInvalidItem = orderForm.items.some(item => !item.productId || !item.quantity)

  if (hasInvalidItem) {
    ElMessage.warning('Please complete all order items')
    return
  }

  submitting.value = true

  try {
    await createOrder({
      supplierId: orderForm.supplierId,
      items: orderForm.items.map(item => ({
        productId: item.productId,
        quantity: item.quantity
      }))
    })

    ElMessage.success('Order created successfully')
    dialogVisible.value = false
    await loadOrders()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to create order')
  } finally {
    submitting.value = false
  }
}

const handleStatusUpdate = async (order, nextStatus) => {
  const actionText = nextStatus === 'RECEIVED' ? 'mark this order as received' : 'cancel this order'

  try {
    await ElMessageBox.confirm(
      `Do you want to ${actionText}?`,
      'Confirm',
      { type: 'warning' }
    )

    await updateOrderStatus(order.id, nextStatus)
    ElMessage.success('Order status updated successfully')
    await loadOrders()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.error || 'Failed to update order status')
    }
  }
}

onMounted(async () => {
  await Promise.all([loadOrders(), loadOptions()])
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

.item-line {
  line-height: 1.8;
}

.terminal-text {
  color: #909399;
}

.item-block {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-row {
  display: flex;
  align-items: center;
  gap: 12px;
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

  .item-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
