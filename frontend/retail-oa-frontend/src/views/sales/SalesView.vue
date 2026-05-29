<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h3>Sales Management</h3>
        <p>Review imported sales, record manual transactions, and generate POS mock data.</p>
      </div>
      <div class="header-actions">
        <el-button type="success" plain @click="openMockDialog">Generate Mock Sales</el-button>
        <el-button type="primary" @click="openCreateDialog">Record Sale</el-button>
      </div>
    </div>

    <el-row :gutter="16" class="summary-row">
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Today's Sales Amount</div>
          <div class="summary-value">{{ formatCurrency(dashboard.todaySalesAmount) }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Today's Sales Count</div>
          <div class="summary-value">{{ dashboard.todaySalesCount || 0 }}</div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :lg="8">
        <el-card class="summary-card">
          <div class="summary-label">Top Selling Products Today</div>
          <div class="summary-value">{{ dashboard.topSellingProducts?.length || 0 }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-card>
      <div class="toolbar">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          value-format="YYYY-MM-DD"
        />
        <el-select v-model="sourceFilter" placeholder="Source" clearable style="width: 180px">
          <el-option label="MANUAL" value="MANUAL" />
          <el-option label="POS" value="POS" />
          <el-option label="MOCK" value="MOCK" />
        </el-select>
        <el-button type="primary" @click="loadSales">Search</el-button>
      </div>

      <el-table :data="salesList" style="width: 100%" v-loading="loading">
        <el-table-column type="expand">
          <template #default="scope">
            <el-table :data="scope.row.items" size="small">
              <el-table-column prop="productName" label="Product" min-width="160" />
              <el-table-column prop="productSku" label="SKU" width="140" />
              <el-table-column prop="quantity" label="Qty" width="90" />
              <el-table-column prop="unitPrice" label="Unit Price" width="120">
                <template #default="itemScope">
                  {{ formatCurrency(itemScope.row.unitPrice) }}
                </template>
              </el-table-column>
              <el-table-column prop="lineAmount" label="Line Amount" width="140">
                <template #default="itemScope">
                  {{ formatCurrency(itemScope.row.lineAmount) }}
                </template>
              </el-table-column>
            </el-table>
          </template>
        </el-table-column>
        <el-table-column prop="saleNumber" label="Sale No." width="180" />
        <el-table-column prop="source" label="Source" width="110">
          <template #default="scope">
            <el-tag :type="getSourceTagType(scope.row.source)">{{ scope.row.source }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="cashierName" label="Cashier" width="140" />
        <el-table-column prop="totalAmount" label="Total Amount" width="140">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="saleTime" label="Sale Time" width="180" />
        <el-table-column label="Items" width="100">
          <template #default="scope">
            {{ scope.row.items?.length || 0 }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="Remark" min-width="180" show-overflow-tooltip />
      </el-table>
    </el-card>

    <el-card>
      <template #header>
        <div class="card-header">
          <span>Top 10 Best-Selling Products Today</span>
        </div>
      </template>

      <el-table :data="dashboard.topSellingProducts || []" size="small">
        <el-table-column type="index" label="#" width="60" />
        <el-table-column prop="productName" label="Product" min-width="180" />
        <el-table-column prop="productSku" label="SKU" width="140" />
        <el-table-column prop="totalQuantity" label="Sold Qty" width="110" />
        <el-table-column prop="totalAmount" label="Sales Amount" width="150">
          <template #default="scope">
            {{ formatCurrency(scope.row.totalAmount) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="createDialogVisible" title="Record Manual Sale" width="760px">
      <el-form label-width="110px">
        <el-form-item label="Cashier Name">
          <el-input v-model="saleForm.cashierName" />
        </el-form-item>
        <el-form-item label="Remark">
          <el-input v-model="saleForm.remark" />
        </el-form-item>

        <div class="item-block">
          <div class="item-header">
            <span>Sale Items</span>
            <el-button type="primary" plain @click="addSaleItem">Add Item</el-button>
          </div>

          <div v-for="(item, index) in saleForm.items" :key="index" class="item-row">
            <el-select
              v-model="item.productId"
              placeholder="Product"
              style="width: 38%"
              @change="handleProductChange(index)"
            >
              <el-option
                v-for="product in productOptions"
                :key="product.id"
                :label="`${product.name} (${product.sku})`"
                :value="product.id"
              />
            </el-select>
            <el-input-number v-model="item.quantity" :min="1" :step="1" style="width: 20%" />
            <el-input-number v-model="item.unitPrice" :min="0.01" :precision="2" :step="1" style="width: 22%" />
            <div class="line-amount">{{ formatCurrency(getLineAmount(item)) }}</div>
            <el-button
              type="danger"
              plain
              :disabled="saleForm.items.length === 1"
              @click="removeSaleItem(index)"
            >
              Remove
            </el-button>
          </div>
        </div>

        <div class="sale-total">Order Total: {{ formatCurrency(getOrderTotal()) }}</div>
      </el-form>

      <template #footer>
        <el-button @click="createDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="submittingSale" @click="submitSale">
          Save
        </el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="mockDialogVisible" title="Generate Mock Sales" width="460px">
      <el-form label-width="110px">
        <el-form-item label="Count">
          <el-input-number v-model="mockForm.count" :min="1" :max="50" style="width: 100%" />
        </el-form-item>
        <el-form-item label="Sale Date">
          <el-date-picker v-model="mockForm.saleDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="mockDialogVisible = false">Cancel</el-button>
        <el-button type="primary" :loading="generatingMock" @click="submitMockSales">
          Generate
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { createSale, generateMockSales, getSales, getSalesDashboard } from '../../api/sales'
import { getAllProducts } from '../../api/product'
import { authState } from '../../utils/auth'

const loading = ref(false)
const submittingSale = ref(false)
const generatingMock = ref(false)
const createDialogVisible = ref(false)
const mockDialogVisible = ref(false)
const salesList = ref([])
const productOptions = ref([])
const dateRange = ref([])
const sourceFilter = ref('')
const dashboard = ref({
  todaySalesAmount: 0,
  todaySalesCount: 0,
  topSellingProducts: []
})
const saleForm = reactive({
  cashierName: '',
  remark: '',
  items: [
    {
      productId: null,
      quantity: 1,
      unitPrice: 1
    }
  ]
})
const mockForm = reactive({
  count: 5,
  saleDate: ''
})

const formatCurrency = (value) => {
  const amount = Number(value || 0)
  return `$${amount.toFixed(2)}`
}

const getSourceTagType = (source) => {
  if (source === 'POS') {
    return 'primary'
  }

  if (source === 'MOCK') {
    return 'warning'
  }

  return 'success'
}

const resetSaleForm = () => {
  saleForm.cashierName = authState.user?.username || ''
  saleForm.remark = ''
  saleForm.items = [
    {
      productId: null,
      quantity: 1,
      unitPrice: 1
    }
  ]
}

const addSaleItem = () => {
  saleForm.items.push({
    productId: null,
    quantity: 1,
    unitPrice: 1
  })
}

const removeSaleItem = (index) => {
  if (saleForm.items.length === 1) {
    return
  }

  saleForm.items.splice(index, 1)
}

const syncItemPrice = (item) => {
  const product = productOptions.value.find(option => option.id === item.productId)
  if (!product) {
    return
  }

  // Default to the product master price, while still allowing the cashier to override it.
  item.unitPrice = Number(product.price || 0)
}

const handleProductChange = (index) => {
  const item = saleForm.items[index]
  if (!item) {
    return
  }

  syncItemPrice(item)
}

const getLineAmount = (item) => {
  const quantity = Number(item.quantity || 0)
  const unitPrice = Number(item.unitPrice || 0)
  return quantity * unitPrice
}

const getOrderTotal = () => {
  return saleForm.items.reduce((total, item) => total + getLineAmount(item), 0)
}

const loadProducts = async () => {
  const response = await getAllProducts()
  productOptions.value = (response.data || []).filter(product => product.status === 'ACTIVE')
}

const loadDashboard = async () => {
  const response = await getSalesDashboard()
  dashboard.value = response.data || dashboard.value
}

const loadSales = async () => {
  loading.value = true

  try {
    const params = {}
    // Keep filters out of the query string unless the user has actively chosen them.
    if (dateRange.value?.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    if (sourceFilter.value) {
      params.source = sourceFilter.value
    }

    const response = await getSales(params)
    salesList.value = response.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to load sales records')
  } finally {
    loading.value = false
  }
}

const openCreateDialog = async () => {
  resetSaleForm()
  await loadProducts()
  createDialogVisible.value = true
}

const openMockDialog = () => {
  mockForm.count = 5
  mockForm.saleDate = ''
  mockDialogVisible.value = true
}

const submitSale = async () => {
  if (!saleForm.cashierName.trim()) {
    ElMessage.warning('Cashier name is required')
    return
  }

  // Manual sales share the same backend ingestion path as POS sales, so stock checks happen server-side.
  const hasInvalidItem = saleForm.items.some(item => !item.productId || !item.quantity || !item.unitPrice)
  if (hasInvalidItem) {
    ElMessage.warning('Please complete all sale items')
    return
  }

  submittingSale.value = true

  try {
    await createSale({
      cashierUserId: authState.user?.id || null,
      cashierName: saleForm.cashierName.trim(),
      remark: saleForm.remark.trim() || null,
      items: saleForm.items.map(item => ({
        productId: item.productId,
        quantity: item.quantity,
        unitPrice: item.unitPrice
      }))
    })

    ElMessage.success('Sale recorded successfully')
    createDialogVisible.value = false
    await Promise.all([loadSales(), loadDashboard(), loadProducts()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to save sale')
  } finally {
    submittingSale.value = false
  }
}

const submitMockSales = async () => {
  generatingMock.value = true

  try {
    await generateMockSales({
      count: mockForm.count,
      saleDate: mockForm.saleDate || null
    })

    ElMessage.success('Mock sales generated successfully')
    mockDialogVisible.value = false
    await Promise.all([loadSales(), loadDashboard(), loadProducts()])
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to generate mock sales')
  } finally {
    generatingMock.value = false
  }
}

onMounted(async () => {
  try {
    await Promise.all([loadProducts(), loadSales(), loadDashboard()])
    resetSaleForm()
  } catch (error) {
    ElMessage.error(error.response?.data?.error || 'Failed to initialize sales page')
  }
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

.header-actions {
  display: flex;
  gap: 12px;
}

.summary-row {
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

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
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
  gap: 12px;
  align-items: center;
}

.line-amount {
  width: 14%;
  min-width: 110px;
  color: #606266;
  font-weight: 600;
}

.sale-total {
  margin-top: 16px;
  text-align: right;
  font-size: 16px;
  font-weight: 700;
  color: #303133;
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .header-actions,
  .item-row {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
