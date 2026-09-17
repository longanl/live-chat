<script setup lang="ts">
/** 注册页：头像可选，校验规则与后端 RegisterDTO 对齐 */
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { registerApi } from '@/api/users'
import { uploadFile } from '@/api/upload'
import type { RegisterForm } from '@/types'
import UserAvatar from '@/components/common/UserAvatar.vue'

const router = useRouter()
const formRef = ref<FormInstance | null>(null)
const loading = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)

const form = reactive<RegisterForm>({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  avatar: null
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

function beforeAvatarUpload(file: File): boolean {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png'
  if (!isImage) {
    ElMessage.error('请上传 JPG / PNG 格式的图片')
    return false
  }
  if (file.size / 1024 / 1024 >= 2) {
    ElMessage.error('图片大小不能超过 2MB')
    return false
  }
  return true
}

function pickAvatar(): void {
  avatarInput.value?.click()
}

/** 头像经统一 uploadFile 封装上传，成功后回填 URL（不再走 /uploadavatar687） */
async function onAvatarChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !beforeAvatarUpload(file)) return
  uploadingAvatar.value = true
  try {
    const res = await uploadFile(file, 'image')
    if (res.code === 200 && res.data?.url) {
      form.avatar = res.data.url
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.msg || '头像上传失败')
    }
  } catch (error) {
    console.error('头像上传失败', error)
    ElMessage.error('头像上传失败')
  } finally {
    uploadingAvatar.value = false
  }
}

async function handleRegister(): Promise<void> {
  const instance = formRef.value
  if (!instance) return
  await instance.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      const res = await registerApi(form)
      if (res.code === 200) {
        ElMessage.success('注册成功，请登录')
        router.push('/login')
      } else {
        ElMessage.error(res.msg || res.message || '注册失败')
      }
    } catch (error) {
      console.error('注册失败', error)
    } finally {
      loading.value = false
    }
  })
}
</script>

<template>
  <div class="auth">
    <div class="auth-card">
      <h2 class="auth-card__title">创建账号</h2>
      <p class="auth-card__subtitle">只需几秒，即可开始聊天</p>

      <el-form ref="formRef" :model="form" :rules="rules" label-position="top">
        <el-form-item label="头像（可选）">
          <div class="avatar-uploader" @click="pickAvatar">
            <UserAvatar :src="form.avatar" :name="form.nickname || form.username" :size="64" />
            <span class="avatar-uploader__mask">
              {{ uploadingAvatar ? '上传中…' : '上传头像' }}
            </span>
            <input
              ref="avatarInput"
              type="file"
              accept="image/jpeg,image/png"
              class="avatar-uploader__input"
              @change="onAvatarChange"
            />
          </div>
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="登录使用，3-20 个字符" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="聊天时展示的名字" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="6-20 位" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入密码"
            @keyup.enter="handleRegister"
          />
        </el-form-item>
        <el-button
          type="primary"
          size="large"
          class="auth-card__submit"
          :loading="loading"
          @click="handleRegister"
        >
          注册
        </el-button>
      </el-form>

      <div class="auth-card__footer">
        已有账号？
        <router-link to="/login">返回登录</router-link>
      </div>
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
  width: 420px;
  padding: 32px 32px 24px;
  background-color: var(--lc-surface);
  border-radius: var(--lc-radius-lg);
  box-shadow: var(--lc-shadow);
}

.auth-card__title {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.auth-card__subtitle {
  margin: 6px 0 20px;
  font-size: 13px;
  color: var(--lc-text-3);
}

.auth-card__submit {
  width: 100%;
  margin-top: 4px;
}

.auth-card__footer {
  margin-top: 16px;
  font-size: 13px;
  color: var(--lc-text-2);
  text-align: center;
}

.avatar-uploader {
  display: inline-flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.avatar-uploader__input {
  display: none;
}

.avatar-uploader__mask {
  font-size: 12px;
  color: var(--lc-primary);
}
</style>
