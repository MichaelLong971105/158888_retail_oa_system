import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { canAccess, ensureAuthLoaded, getCurrentRole, getDefaultRouteByRole, isLoggedIn } from '../utils/auth'

import DashboardView from '../views/dashboard/DashboardView.vue'
import ProductView from '../views/product/ProductView.vue'
import SupplierView from '../views/supplier/SupplierView.vue'
import OrderView from '../views/order/OrderView.vue'
import InventoryView from '../views/inventory/InventoryView.vue'
import SalesView from '../views/sales/SalesView.vue'
import AttendanceView from '../views/attendance/AttendanceView.vue'
import UserView from '../views/user/UserView.vue'
import LoginView from '../views/auth/LoginView.vue'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: LoginView,
    meta: {
      public: true
    }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: DashboardView,
        meta: {
          roles: ['ADMIN', 'MANAGER', 'STAFF']
        }
      },
      {
        path: 'products',
        name: 'Products',
        component: ProductView,
        meta: {
          roles: ['ADMIN', 'MANAGER', 'STAFF'],
          permissions: ['MANAGE_PRODUCTS']
        }
      },
      {
        path: 'suppliers',
        name: 'Suppliers',
        component: SupplierView,
        meta: {
          roles: ['ADMIN', 'MANAGER'],
          permissions: ['MANAGE_SUPPLIERS']
        }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: OrderView,
        meta: {
          roles: ['ADMIN', 'MANAGER'],
          permissions: ['MANAGE_ORDERS']
        }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: InventoryView,
        meta: {
          roles: ['ADMIN', 'MANAGER', 'STAFF'],
          permissions: ['MANAGE_INVENTORY']
        }
      },
      {
        path: 'sales',
        name: 'Sales',
        component: SalesView,
        meta: {
          roles: ['ADMIN', 'MANAGER'],
          permissions: ['VIEW_SALES', 'MANAGE_SALES', 'MANAGE_POS']
        }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: AttendanceView,
        meta: {
          roles: ['ADMIN', 'MANAGER', 'STAFF'],
          permissions: ['VIEW_ATTENDANCE', 'MANAGE_ATTENDANCE', 'APPROVE_LEAVE']
        }
      },
      {
        path: 'users',
        name: 'Users',
        component: UserView,
        meta: {
          roles: ['ADMIN'],
          permissions: ['MANAGE_USERS']
        }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.meta.public) {
    if (to.path === '/login' && isLoggedIn()) {
      return getDefaultRouteByRole(getCurrentRole())
    }

    return true
  }

  const user = await ensureAuthLoaded()

  if (!user) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath
      }
    }
  }

  if (!canAccess(to.meta.roles || [], to.meta.permissions || [])) {
    return getDefaultRouteByRole(user.role)
  }

  return true
})

export default router
