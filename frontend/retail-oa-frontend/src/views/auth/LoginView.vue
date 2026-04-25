<template>
  <div class="login-page">
    <el-card class="login-card" shadow="hover">
      <div class="login-header">
        <h2>Retail OA Login</h2>
        <p>Please sign in with your system account.</p>
      </div>

      <el-form :model="form" @submit.prevent="handleLogin">
        <el-form-item label="Username">
          <el-input v-model="form.username" placeholder="Enter username" />
        </el-form-item>

        <el-form-item label="Password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="Enter password"
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-alert
          v-if="errorMessage"
          :title="errorMessage"
          type="error"
          :closable="false"
          class="login-alert"
        />

        <el-button type="primary" :loading="loading" class="login-button" @click="handleLogin">
          Login
        </el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '../../api/auth'
import { getDefaultRouteByRole, setCurrentUser } from '../../utils/auth'

const router = useRouter()
const route = useRoute()

const loading = ref(false)
const errorMessage = ref('')
const form = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  errorMessage.value = ''

  if (!form.username || !form.password) {
    errorMessage.value = 'Username and password are required.'
    return
  }

  loading.value = true

  try {
    const response = await login(form)
    const user = response.data.user

    setCurrentUser(user)
    ElMessage.success('Login successful')

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : ''
    const targetPath = redirect || getDefaultRouteByRole(user.role)

    router.push(targetPath)
  } catch (error) {
    errorMessage.value = error.response?.data?.error || 'Login failed'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #eef2f7 0%, #dbe7f3 100%);
  padding: 24px;
}

.login-card {
  width: 420px;
  max-width: 100%;
}

.login-header {
  margin-bottom: 24px;
  text-align: center;
}

.login-header h2 {
  margin: 0 0 8px;
}

.login-header p {
  margin: 0;
  color: #606266;
}

.login-alert {
  margin-bottom: 16px;
}

.login-button {
  width: 100%;
}
</style>
