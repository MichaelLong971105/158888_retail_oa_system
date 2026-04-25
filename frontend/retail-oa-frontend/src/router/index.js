import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'
import { ensureAuthLoaded, getCurrentRole, getDefaultRouteByRole, hasAnyRole, isLoggedIn } from '../utils/auth'

import DashboardView from '../views/dashboard/DashboardView.vue'
import ProductView from '../views/product/ProductView.vue'
import SupplierView from '../views/supplier/SupplierView.vue'
import OrderView from '../views/order/OrderView.vue'
import InventoryView from '../views/inventory/InventoryView.vue'
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
          roles: ['ADMIN', 'MANAGER', 'STAFF']
        }
      },
      {
        path: 'suppliers',
        name: 'Suppliers',
        component: SupplierView,
        meta: {
          roles: ['ADMIN', 'MANAGER']
        }
      },
      {
        path: 'orders',
        name: 'Orders',
        component: OrderView,
        meta: {
          roles: ['ADMIN', 'MANAGER']
        }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: InventoryView,
        meta: {
          roles: ['ADMIN', 'MANAGER', 'STAFF']
        }
      },
      {
        path: 'users',
        name: 'Users',
        component: UserView,
        meta: {
          roles: ['ADMIN']
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

  if (!hasAnyRole(to.meta.roles || [])) {
    return getDefaultRouteByRole(user.role)
  }

  return true
})

export default router
