<script setup lang="ts">
/** 登录页：左侧品牌区 + 右侧表单（不再依赖未引入的 Font Awesome 与虚假数据） */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { loginApi } from '@/api/users'
import { useWebSocketStore } from '@/stores/websocket'
import type { LoginForm } from '@/types'

const router = useRouter()
const websocket = useWebSocketStore()
const formRef = ref<FormInstance | null>(null)
const loading = ref(false)

const form = reactive<LoginForm>({
  username: '',
  password: '',
  rememberMe: false
})

const rules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin(): Promise<void> {
  const instance = formRef.value
  if (!instance) return
  await instance.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await loginApi(form)
      if (res.code === 200 && res.data) {
        localStorage.setItem('user', JSON.stringify(res.data))
        // 立即建连，无需整页刷新
        websocket.initWebSocket(res.data.id)
        ElMessage.success('登录成功')
        router.push('/chat')
      } else {
        ElMessage.error(res.msg || '用户名或密码错误')
      }
    } catch (error) {
      console.error('登录失败', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="auth">
    <div class="auth-card">
      <aside class="auth-brand">
        <div class="auth-brand__logo">
          <el-icon><ChatLineRound /></el-icon>
          Live Chat
        </div>
        <p class="auth-brand__slogan">和好友实时聊天，随时随地</p>
        <ul class="auth-brand__features">
          <li><el-icon><ChatDotRound /></el-icon>私聊与多人群聊</li>
          <li><el-icon><User /></el-icon>好友申请与在线状态</li>
          <li><el-icon><Picture /></el-icon>图片、视频与文件传输</li>
        </ul>
      </aside>

      <section class="auth-form">
        <h2 class="auth-form__title">欢迎回来</h2>
        <p class="auth-form__subtitle">登录后继续你的对话</p>
        <el-form ref="formRef" :model="form" :rules="rules" @submit.prevent="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" size="large" placeholder="用户名">
              <template #prefix><el-icon><User /></el-icon></template>
            </el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              size="large"
              placeholder="密码"
              show-password
              @keyup.enter="handleLogin"
            >
              <template #prefix><el-icon><Lock /></el-icon></template>
            </el-input>
          </el-form-item>
          <div class="auth-form__row">
            <el-checkbox v-model="form.rememberMe">记住我</el-checkbox>
          </div>
          <el-button
            type="primary"
            size="large"
            class="auth-form__submit"
            :loading="loading"
            @click="handleLogin"
          >
            登录
          </el-button>
        </el-form>
        <div class="auth-form__footer">
          还没有账号？
          <router-link to="/register">立即注册</router-link>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
.auth {
  min-height: 100dvh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: linear-gradient(135deg, #eaf2ff 0%, #f7f9fc 60%, #eef7f5 100%);
}

.auth-card {
  display: grid;
  grid-template-columns: 320px 380px;
  background-color: var(--lc-surface);
  border-radius: var(--lc-radius-lg);
  box-shadow: var(--lc-shadow);
  overflow: hidden;
}

.auth-brand {
  display: flex;
  flex-direction: column;
  gap: 18px;
  padding: 36px 28px;
  color: #fff;
  background: linear-gradient(160deg, #2b79f5 0%, #1f5fd0 100%);
}

.auth-brand__logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
}

.auth-brand__slogan {
  margin: 0;
  font-size: 14px;
  opacity: 0.92;
}

.auth-brand__features {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
  font-size: 13px;
  margin-top: auto;
}

.auth-brand__features li {
  display: flex;
  align-items: center;
  gap: 8px;
  opacity: 0.95;
}

.auth-form {
  padding: 40px 36px;
}

.auth-form__title {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.auth-form__subtitle {
  margin: 6px 0 22px;
  font-size: 13px;
  color: var(--lc-text-3);
}

.auth-form__row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.auth-form__submit {
  width: 100%;
}

.auth-form__footer {
  margin-top: 18px;
  font-size: 13px;
  color: var(--lc-text-2);
  text-align: center;
}

@media (max-width: 760px) {
  .auth-card {
    grid-template-columns: 1fr;
  }

  .auth-brand {
    padding: 24px;
  }

  .auth-brand__features {
    display: none;
  }
}
</style>
