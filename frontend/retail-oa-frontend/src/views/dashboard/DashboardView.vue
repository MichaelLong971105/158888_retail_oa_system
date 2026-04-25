<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Dashboard</h3>
        <p>Overview of current retail OA operations.</p>
      </div>
      <el-button type="primary" :loading="loading" @click="loadDashboardData">
        Refresh
      </el-button>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Products</div>
          <div class="summary-value">{{ summary.totalProducts }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Suppliers</div>
          <div class="summary-value">{{ displayValue(summary.totalSuppliers) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Users</div>
          <div class="summary-value">{{ displayValue(summary.totalUsers) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card">
          <div class="summary-label">Orders</div>
          <div class="summary-value">{{ displayValue(orderStats.totalOrders) }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="content-row">
      <el-col :xs="24" :lg="14">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Order Statistics</span>
            </div>
          </template>

          <el-row :gutter="12">
            <el-col :span="8">
              <div class="metric-box pending">
                <div class="metric-title">Pending</div>
                <div class="metric-number">{{ displayValue(orderStats.pendingOrders) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box received">
                <div class="metric-title">Received</div>
                <div class="metric-number">{{ displayValue(orderStats.receivedOrders) }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box cancelled">
                <div class="metric-title">Cancelled</div>
                <div class="metric-number">{{ displayValue(orderStats.cancelledOrders) }}</div>
              </div>
            </el-col>
          </el-row>

          <div class="amount-grid">
            <div class="amount-item">
              <span>Total Procurement Amount</span>
              <strong>{{ formatCurrency(orderStats.totalAmount, canViewOrders) }}</strong>
            </div>
            <div class="amount-item">
              <span>Received Amount</span>
              <strong>{{ formatCurrency(orderStats.receivedAmount, canViewOrders) }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :lg="10">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>Inventory Snapshot</span>
            </div>
          </template>

          <div class="amount-grid compact">
            <div class="amount-item">
              <span>Total Stock Units</span>
              <strong>{{ summary.totalStock }}</strong>
            </div>
            <div class="amount-item danger">
              <span>Low Stock Products</span>
              <strong>{{ summary.lowStockProducts }}</strong>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>Recent Orders</span>
        </div>
      </template>

      <el-table :data="recentOrders" style="width: 100%" v-loading="loading">
        <el-table-column prop="orderNumber" label="Order No." width="180" />
        <el-table-column prop="supplierName" label="Supplier" />
        <el-table-column prop="totalAmount" label="Amount" width="140">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="Status" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="Created At" width="180" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllProducts } from '../../api/product'
import { getAllSuppliers } from '../../api/supplier'
import { getAllUsers } from '../../api/user'
import { getAllOrders, getOrderStats } from '../../api/order'
import { getCurrentRole } from '../../utils/auth'

const loading = ref(false)
const recentOrders = ref([])
const orderStats = ref({})
const currentRole = getCurrentRole()
const canViewOrders = currentRole === 'ADMIN' || currentRole === 'MANAGER'
const canViewSuppliers = currentRole === 'ADMIN' || currentRole === 'MANAGER'
const canViewUsers = currentRole === 'ADMIN'
const summary = reactive({
  totalProducts: 0,
  totalSuppliers: null,
  totalUsers: null,
  totalStock: 0,
  lowStockProducts: 0
})

const formatCurrency = (value, enabled = true) => {
  if (!enabled) {
    return '-'
  }

  const amount = Number(value || 0)
  return `$${amount.toFixed(2)}`
}

const displayValue = (value) => {
  return value === null || value === undefined ? '-' : value
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

const loadDashboardData = async () => {
  loading.value = true

  try {
    const productsResponse = await getAllProducts()
    const requests = []

    if (canViewSuppliers) {
      requests.push(getAllSuppliers())
    }

    if (canViewUsers) {
      requests.push(getAllUsers())
    }

    if (canViewOrders) {
      requests.push(getAllOrders())
      requests.push(getOrderStats())
    }

    const responses = await Promise.all(requests)

    const products = productsResponse.data || []
    let responseIndex = 0
    let suppliers = []
    let users = []
    let orders = []
    let stats = {}

    if (canViewSuppliers) {
      suppliers = responses[responseIndex]?.data || []
      responseIndex += 1
    }

    if (canViewUsers) {
      users = responses[responseIndex]?.data || []
      responseIndex += 1
    }

    if (canViewOrders) {
      orders = responses[responseIndex]?.data || []
      responseIndex += 1
      stats = responses[responseIndex]?.data || {}
    }

    summary.totalProducts = products.length
    summary.totalSuppliers = canViewSuppliers ? suppliers.length : null
    summary.totalUsers = canViewUsers ? users.length : null
    summary.totalStock = products.reduce((total, product) => total + Number(product.stock || 0), 0)
    summary.lowStockProducts = products.filter(product =>
      Number(product.stock || 0) <= Number(product.minStock || 0)
    ).length

    recentOrders.value = canViewOrders
      ? orders
        .slice()
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5)
      : []

    orderStats.value = stats
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load dashboard data')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadDashboardData()
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

.page-header h3,
.card-header {
  margin: 0;
}

.page-header p {
  margin: 6px 0 0;
  color: #606266;
}

.summary-row,
.content-row {
  margin: 0;
}

.summary-card {
  min-height: 120px;
}

.summary-label {
  color: #909399;
  font-size: 14px;
}

.summary-value {
  margin-top: 16px;
  font-size: 32px;
  font-weight: 700;
  color: #303133;
}

.metric-box {
  border-radius: 12px;
  padding: 16px;
  color: #fff;
}

.metric-box.pending {
  background: linear-gradient(135deg, #e6a23c, #f3c87a);
}

.metric-box.received {
  background: linear-gradient(135deg, #67c23a, #95d475);
}

.metric-box.cancelled {
  background: linear-gradient(135deg, #f56c6c, #f89898);
}

.metric-title {
  font-size: 14px;
}

.metric-number {
  margin-top: 12px;
  font-size: 28px;
  font-weight: 700;
}

.amount-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  margin-top: 20px;
}

.amount-grid.compact {
  margin-top: 0;
  grid-template-columns: 1fr;
}

.amount-item {
  background: #f5f7fa;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-item span {
  color: #606266;
}

.amount-item strong {
  font-size: 24px;
  color: #303133;
}

.amount-item.danger strong {
  color: #f56c6c;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .amount-grid {
    grid-template-columns: 1fr;
  }
}
</style>
