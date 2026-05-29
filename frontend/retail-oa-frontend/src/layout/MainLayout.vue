<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="sidebar">
      <div class="logo">
        <span class="logo-mark">RO</span>
        <span class="logo-text">Retail OA</span>
      </div>

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
          <div>
            <h2>Retail Operations Console</h2>
            <p>Inventory, procurement, sales, attendance, and account control.</p>
          </div>

          <div class="user-actions">
            <span class="user-text">
              <strong>{{ authState.user?.username }}</strong>
              <small>{{ authState.user?.role }}</small>
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

// Sidebar visibility follows the same role/permission model as the router guard.
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
  background: var(--app-bg);
}

.sidebar {
  background:
    linear-gradient(180deg, rgba(37, 99, 235, 0.14), transparent 240px),
    var(--app-sidebar);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  color: #fff;
}

.logo {
  height: 68px;
  padding: 0 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: #fff;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-mark {
  width: 38px;
  height: 38px;
  border-radius: 8px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #38bdf8, #2563eb);
  box-shadow: 0 12px 24px rgba(37, 99, 235, 0.28);
  font-size: 14px;
  font-weight: 900;
}

.logo-text {
  font-size: 18px;
  font-weight: 850;
  letter-spacing: 0;
}

.menu {
  padding: 12px 10px;
  border-right: none;
  --el-menu-bg-color: transparent;
  --el-menu-text-color: #cbd5e1;
  --el-menu-hover-bg-color: rgba(255, 255, 255, 0.08);
  --el-menu-active-color: #ffffff;
}

.menu :deep(.el-menu-item) {
  height: 44px;
  margin: 4px 0;
  border-radius: 8px;
  font-weight: 700;
}

.menu :deep(.el-menu-item.is-active) {
  background: rgba(37, 99, 235, 0.92);
  box-shadow: 0 10px 24px rgba(37, 99, 235, 0.22);
}

.menu :deep(.el-icon) {
  font-size: 18px;
}

.header {
  height: 76px;
  background-color: rgba(255, 255, 255, 0.9);
  border-bottom: 1px solid rgba(148, 163, 184, 0.24);
  backdrop-filter: blur(14px);
  padding: 0 24px;
}

.header-content {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
}

.header-content h2 {
  margin: 0;
  color: #111827;
  font-size: 20px;
  font-weight: 850;
}

.header-content p {
  margin: 4px 0 0;
  color: var(--app-muted);
  font-size: 13px;
}

.user-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-text {
  min-height: 42px;
  padding: 6px 12px;
  border: 1px solid #dce5ef;
  border-radius: 8px;
  background: #f8fafc;
  color: #344054;
  display: flex;
  flex-direction: column;
  justify-content: center;
  line-height: 1.2;
}

.user-text small {
  margin-top: 3px;
  color: var(--app-muted);
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.04em;
}

.main-content {
  background: transparent;
  padding: 24px;
  overflow: auto;
}

@media (max-width: 900px) {
  .layout-container {
    height: auto;
    min-height: 100vh;
  }

  .sidebar {
    width: 78px !important;
  }

  .logo {
    justify-content: center;
    padding: 0;
  }

  .logo-text,
  .menu :deep(.el-menu-item span) {
    display: none;
  }

  .menu {
    padding-inline: 8px;
  }

  .menu :deep(.el-menu-item) {
    justify-content: center;
    padding: 0;
  }
}

@media (max-width: 640px) {
  .header {
    height: auto;
    padding: 14px;
  }

  .header-content {
    align-items: flex-start;
    flex-direction: column;
  }

  .user-actions {
    width: 100%;
    justify-content: space-between;
  }

  .main-content {
    padding: 14px;
  }
}
</style>
