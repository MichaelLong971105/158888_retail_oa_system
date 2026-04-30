import { reactive } from 'vue'
import { getCurrentUser } from '../api/auth'

const STORAGE_KEY = 'retail-oa-current-user'

function loadStoredUser() {
  const raw = localStorage.getItem(STORAGE_KEY)

  if (!raw) {
    return null
  }

  try {
    return JSON.parse(raw)
  } catch (error) {
    localStorage.removeItem(STORAGE_KEY)
    return null
  }
}

export const authState = reactive({
  user: loadStoredUser(),
  initialized: false
})

export function setCurrentUser(user) {
  authState.user = user
  authState.initialized = true

  if (user) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(user))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function clearCurrentUser() {
  setCurrentUser(null)
}

export function getCurrentRole() {
  return authState.user?.role || null
}

export function getCurrentPermissions() {
  return authState.user?.permissions || []
}

export function isLoggedIn() {
  return !!authState.user
}

export function hasAnyRole(roles = []) {
  if (roles.length === 0) {
    return true
  }

  const currentRole = getCurrentRole()
  return !!currentRole && roles.includes(currentRole)
}

export function hasAnyPermission(permissions = []) {
  if (permissions.length === 0) {
    return true
  }

  const currentPermissions = new Set(getCurrentPermissions())
  return permissions.some(permission => currentPermissions.has(permission))
}

export function canAccess(roles = [], permissions = []) {
  if (roles.length === 0 && permissions.length === 0) {
    return true
  }

  return hasAnyRole(roles) || hasAnyPermission(permissions)
}

export async function ensureAuthLoaded() {
  if (authState.initialized && authState.user) {
    return authState.user
  }

  if (authState.initialized && !authState.user) {
    return null
  }

  try {
    const response = await getCurrentUser()
    setCurrentUser(response.data)
    return response.data
  } catch (error) {
    clearCurrentUser()
    return null
  }
}

export function getDefaultRouteByRole(role) {
  if (role === 'ADMIN' || role === 'MANAGER' || role === 'STAFF') {
    return '/dashboard'
  }

  return '/login'
}
