<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">Retail OA System</div>

        <el-menu
          router
          :default-active="$route.path"
          class="menu"
        >
        <el-menu-item
          v-for="item in visibleMenuItems"
          :key="item.index"
          :index="item.index"
        >
          <el-icon><component :is="item.icon" /></el-icon>
          <span>{{ item.label }}</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="header">
        <div class="header-content">
          <h2>Retail OA Management System</h2>

          <div class="user-actions">
            <span class="user-text">
              {{ authState.user?.username }} ({{ authState.user?.role }})
            </span>
            <el-button type="danger" plain @click="handleLogout">Logout</el-button>
          </div>
        </div>
      </el-header>

      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Calendar,
  DataBoard,
  Goods,
  Histogram,
  List,
  OfficeBuilding,
  Setting,
  User
} from '@element-plus/icons-vue'
import { logout } from '../api/auth'
import { authState, canAccess, clearCurrentUser } from '../utils/auth'

const router = useRouter()

const menuItems = [
  { index: '/dashboard', label: 'Dashboard', icon: DataBoard, roles: ['ADMIN', 'MANAGER', 'STAFF'] },
  { index: '/attendance', label: 'Attendance', icon: Calendar, roles: ['ADMIN', 'MANAGER', 'STAFF'], permissions: ['VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 'APPROVE_LEAVE'] },
  { index: '/products', label: 'Product Management', icon: Goods, roles: ['ADMIN', 'MANAGER', 'STAFF'], permissions: ['MANAGE_PRODUCTS'] },
  { index: '/suppliers', label: 'Supplier Management', icon: OfficeBuilding, roles: ['ADMIN', 'MANAGER'], permissions: ['MANAGE_SUPPLIERS'] },
  { index: '/orders', label: 'Order Management', icon: List, roles: ['ADMIN', 'MANAGER'], permissions: ['MANAGE_ORDERS'] },
  { index: '/inventory', label: 'Inventory Management', icon: Setting, roles: ['ADMIN', 'MANAGER', 'STAFF'], permissions: ['MANAGE_INVENTORY'] },
  { index: '/sales', label: 'Sales Management', icon: Histogram, roles: ['ADMIN', 'MANAGER'], permissions: ['VIEW_SALES', 'MANAGE_SALES', 'MANAGE_POS'] },
  { index: '/users', label: 'User Management', icon: User, roles: ['ADMIN'], permissions: ['MANAGE_USERS'] }
]

const visibleMenuItems = computed(() =>
  menuItems.filter(item => canAccess(item.roles || [], item.permissions || []))
)

const handleLogout = async () => {
  try {
    await logout()
  } catch (error) {
    // Clear local auth state even if the backend session has already expired.
  } finally {
    clearCurrentUser()
    ElMessage.success('Logged out')
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.sidebar {
  background-color: #304156;
  color: white;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 20px;
  font-weight: bold;
  color: white;
  border-bottom: 1px solid #1f2d3d;
}

.menu {
  border-right: none;
  --el-menu-bg-color: #1f2937;
  --el-menu-text-color: #d1d5db;
  --el-menu-hover-bg-color: #111827;
  --el-menu-active-color: #ffffff;
}

.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-content h2 {
  margin: 0;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-text {
  color: #606266;
}

.main-content {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
