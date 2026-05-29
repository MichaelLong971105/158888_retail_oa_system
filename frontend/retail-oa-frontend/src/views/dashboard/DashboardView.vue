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
        <el-card class="summary-card accent-blue">
          <div class="summary-label">Products</div>
          <div class="summary-value">{{ summary.totalProducts }}</div>
          <div class="summary-meta">Active master data</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card accent-cyan">
          <div class="summary-label">Suppliers</div>
          <div class="summary-value">{{ displayValue(summary.totalSuppliers) }}</div>
          <div class="summary-meta">Procurement network</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card accent-violet">
          <div class="summary-label">Users</div>
          <div class="summary-value">{{ displayValue(summary.totalUsers) }}</div>
          <div class="summary-meta">System accounts</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="6">
        <el-card class="summary-card accent-green">
          <div class="summary-label">Orders</div>
          <div class="summary-value">{{ displayValue(orderStats.totalOrders) }}</div>
          <div class="summary-meta">Purchase workflow</div>
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
                <div class="metric-caption">Awaiting action</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box received">
                <div class="metric-title">Received</div>
                <div class="metric-number">{{ displayValue(orderStats.receivedOrders) }}</div>
                <div class="metric-caption">Completed intake</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-box cancelled">
                <div class="metric-title">Cancelled</div>
                <div class="metric-number">{{ displayValue(orderStats.cancelledOrders) }}</div>
                <div class="metric-caption">Stopped flow</div>
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
        <el-card class="inventory-card">
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
          <div class="stock-health">
            <span>Stock health</span>
            <div class="health-track">
              <div class="health-fill" :style="{ width: stockHealthWidth }"></div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row v-if="canViewSales" :gutter="16" class="content-row">
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Today's Sales Amount</div>
          <div class="summary-value money">{{ formatCurrency(salesDashboard.todaySalesAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Today's Sales Count</div>
          <div class="summary-value">{{ salesDashboard.todaySalesCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Top Selling SKUs</div>
          <div class="summary-value">{{ salesDashboard.topSellingProducts?.length || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card v-if="canViewSales">
      <template #header>
        <div class="card-header">
          <span>Top 10 Best-Selling Products Today</span>
        </div>
      </template>

      <el-table :data="salesDashboard.topSellingProducts || []" style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="productName" label="Product" min-width="180" />
        <el-table-column prop="productSku" label="SKU" width="140" />
        <el-table-column prop="totalQuantity" label="Sold Qty" width="120" />
        <el-table-column prop="totalAmount" label="Sales Amount" width="160">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllProducts } from '../../api/product'
import { getAllSuppliers } from '../../api/supplier'
import { getAllUsers } from '../../api/user'
import { getAllOrders, getOrderStats } from '../../api/order'
import { getSalesDashboard } from '../../api/sales'
import { canAccess } from '../../utils/auth'

const loading = ref(false)
const recentOrders = ref([])
const orderStats = ref({})
const salesDashboard = ref({
  todaySalesAmount: 0,
  todaySalesCount: 0,
  topSellingProducts: []
})
const canViewOrders = canAccess(['ADMIN', 'MANAGER'], ['MANAGE_ORDERS'])
const canViewSuppliers = canAccess(['ADMIN', 'MANAGER'], ['MANAGE_SUPPLIERS'])
const canViewUsers = canAccess(['ADMIN'], ['MANAGE_USERS'])
const canViewSales = canAccess(['ADMIN', 'MANAGER'], ['VIEW_SALES', 'MANAGE_SALES', 'MANAGE_POS'])
const summary = reactive({
  totalProducts: 0,
  totalSuppliers: null,
  totalUsers: null,
  totalStock: 0,
  lowStockProducts: 0
})

const stockHealthWidth = computed(() => {
  if (!summary.totalProducts) {
    return '0%'
  }

  const healthyProducts = Math.max(summary.totalProducts - summary.lowStockProducts, 0)
  return `${Math.round((healthyProducts / summary.totalProducts) * 100)}%`
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

    // Build the dashboard from only the modules the current user is allowed to see.
    // The response unpacking below must stay in this same order.
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

    if (canViewSales) {
      requests.push(getSalesDashboard())
    }

    const responses = await Promise.all(requests)

    const products = productsResponse.data || []
    let responseIndex = 0
    let suppliers = []
    let users = []
    let orders = []
    let stats = {}
    let dashboard = salesDashboard.value

    if (canViewSuppliers) {
      // Responses are read in the same order as the permission-gated request list above.
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
      responseIndex += 1
    }

    if (canViewSales) {
      dashboard = responses[responseIndex]?.data || salesDashboard.value
    }

    summary.totalProducts = products.length
    summary.totalSuppliers = canViewSuppliers ? suppliers.length : null
    summary.totalUsers = canViewUsers ? users.length : null
    // Inventory health is derived client-side from product master data to avoid another dashboard endpoint.
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
    salesDashboard.value = dashboard
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
  min-height: 136px;
  position: relative;
  overflow: hidden;
}

.summary-card::before {
  content: "";
  position: absolute;
  inset: 0 auto 0 0;
  width: 4px;
  background: var(--card-accent, var(--app-primary));
}

.accent-blue {
  --card-accent: #2563eb;
}

.accent-cyan {
  --card-accent: #0891b2;
}

.accent-violet {
  --card-accent: #7c3aed;
}

.accent-green {
  --card-accent: #10b981;
}

.summary-label {
  color: var(--app-muted);
  font-size: 14px;
  font-weight: 700;
}

.summary-value {
  margin-top: 12px;
  font-size: 32px;
  font-weight: 850;
  color: #111827;
}

.summary-value.money {
  font-size: 28px;
}

.summary-meta {
  margin-top: 8px;
  color: #667085;
  font-size: 13px;
}

.metric-box {
  min-height: 126px;
  border-radius: 8px;
  padding: 16px;
  color: #fff;
  box-shadow: 0 16px 30px rgba(15, 23, 42, 0.12);
}

.metric-box.pending {
  background: linear-gradient(135deg, #b45309, #f59e0b);
}

.metric-box.received {
  background: linear-gradient(135deg, #047857, #10b981);
}

.metric-box.cancelled {
  background: linear-gradient(135deg, #b91c1c, #ef4444);
}

.metric-title {
  font-size: 14px;
  font-weight: 800;
  opacity: 0.9;
}

.metric-number {
  margin-top: 10px;
  font-size: 28px;
  font-weight: 900;
}

.metric-caption {
  margin-top: 10px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 12px;
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
  background: var(--app-surface-soft);
  border: 1px solid #e5edf5;
  border-radius: 8px;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.amount-item span {
  color: var(--app-muted);
  font-weight: 650;
}

.amount-item strong {
  font-size: 24px;
  color: #111827;
}

.amount-item.danger strong {
  color: var(--app-danger);
}

.stock-health {
  margin-top: 18px;
}

.stock-health span {
  color: var(--app-muted);
  font-size: 13px;
  font-weight: 700;
}

.health-track {
  height: 10px;
  margin-top: 10px;
  overflow: hidden;
  border-radius: 999px;
  background: #fee2e2;
}

.health-fill {
  height: 100%;
  min-width: 4px;
  border-radius: inherit;
  background: linear-gradient(90deg, #10b981, #22c55e);
  transition: width 220ms ease;
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
