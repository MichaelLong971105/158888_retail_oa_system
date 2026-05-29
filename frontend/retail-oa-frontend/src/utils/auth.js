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
    // Corrupt cached auth data should not trap the app in an invalid logged-in state.
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

  // Keep the UI responsive after reloads; the session is revalidated by ensureAuthLoaded().
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

  // A role or an explicitly granted permission is enough to unlock a module.
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
    // Local storage is only a fast hint; the backend session is still the source of truth.
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
