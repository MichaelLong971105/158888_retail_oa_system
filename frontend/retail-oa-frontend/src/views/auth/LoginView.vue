<template>
  <div class="login-page">
    <section class="login-hero">
      <div class="brand-pill">Retail OA Console</div>
      <h1>Run store operations from one calm workspace.</h1>
      <p>Track procurement, inventory, attendance, users, and sales performance without switching systems.</p>
      <div class="signal-grid">
        <div>
          <strong>Live</strong>
          <span>Operational status</span>
        </div>
        <div>
          <strong>Role-based</strong>
          <span>Secure access</span>
        </div>
        <div>
          <strong>Data-ready</strong>
          <span>Daily reporting</span>
        </div>
      </div>
    </section>

    <el-card class="login-card" shadow="never">
      <div class="login-header">
        <div class="login-mark">RO</div>
        <h2>Welcome back</h2>
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
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 440px);
  align-items: center;
  gap: 48px;
  background:
    radial-gradient(circle at 16% 18%, rgba(37, 99, 235, 0.2), transparent 28rem),
    linear-gradient(135deg, #0f2035 0%, #132f4f 48%, #eef3f8 48%, #f8fafc 100%);
  padding: 56px clamp(24px, 7vw, 92px);
}

.login-hero {
  color: #fff;
  max-width: 680px;
}

.brand-pill {
  width: fit-content;
  padding: 8px 12px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.1);
  color: #dbeafe;
  font-size: 13px;
  font-weight: 800;
}

.login-hero h1 {
  margin: 22px 0 16px;
  max-width: 620px;
  font-size: clamp(40px, 6vw, 68px);
  line-height: 1.02;
  letter-spacing: 0;
}

.login-hero p {
  margin: 0;
  max-width: 560px;
  color: #cbd5e1;
  font-size: 18px;
}

.signal-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-top: 36px;
}

.signal-grid div {
  min-height: 92px;
  padding: 16px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.1);
}

.signal-grid strong,
.signal-grid span {
  display: block;
}

.signal-grid strong {
  font-size: 18px;
}

.signal-grid span {
  margin-top: 6px;
  color: #cbd5e1;
  font-size: 13px;
}

.login-card {
  width: 100%;
  max-width: 100%;
  border: 1px solid rgba(148, 163, 184, 0.32);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.18);
}

.login-header {
  margin-bottom: 24px;
  text-align: center;
}

.login-mark {
  width: 48px;
  height: 48px;
  margin: 0 auto 16px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #38bdf8, #2563eb);
  color: #fff;
  font-weight: 900;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 26px;
  color: #111827;
}

.login-header p {
  margin: 0;
  color: var(--app-muted);
}

.login-alert {
  margin-bottom: 16px;
}

.login-button {
  width: 100%;
  margin-top: 4px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
    background: linear-gradient(180deg, #0f2035 0%, #183a60 42%, #eef3f8 42%, #f8fafc 100%);
    gap: 28px;
  }

  .login-hero h1 {
    font-size: 40px;
  }
}

@media (max-width: 640px) {
  .login-page {
    padding: 28px 16px;
  }

  .signal-grid {
    grid-template-columns: 1fr;
  }
}
</style>
