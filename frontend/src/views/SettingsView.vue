<script setup lang="ts">
/**
 * 设置页（中列）：个人资料、修改密码、退出登录。
 * 旧版把改资料/改密塞在顶栏下拉的弹窗里，这里统一收进设置页。
 */
import { computed, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useChatStore } from '@/stores/chat'
import { useWebSocketStore } from '@/stores/websocket'
import { logoutApi, modifyPassword, updateProfile } from '@/api/users'
import { uploadFile } from '@/api/upload'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { formatDate } from '@/utils/time'

const router = useRouter()
const chatStore = useChatStore()
const websocket = useWebSocketStore()

const profileFormRef = ref<FormInstance | null>(null)
const passwordFormRef = ref<FormInstance | null>(null)
const savingProfile = ref(false)
const savingPassword = ref(false)
const avatarInput = ref<HTMLInputElement | null>(null)
const uploadingAvatar = ref(false)

const profileForm = reactive({
  nickname: chatStore.currentUser.nickname ?? '',
  avatar: chatStore.currentUser.avatar ?? ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules: FormRules = {
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为 6-20 位', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value && value === passwordForm.oldPassword) {
          callback(new Error('新密码不能与原密码相同'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
          return
        }
        callback()
      },
      trigger: 'blur'
    }
  ]
}

const username = computed(() => chatStore.currentUser.username ?? '—')
const joinedAt = computed(() => formatDate(chatStore.currentUser.createTime))

/** 头像上传仅允许图片且不超过 2MB */
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

/** 头像经统一 uploadFile 封装上传，成功后回填，保存资料时一并提交 */
async function onAvatarChange(event: Event): Promise<void> {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !beforeAvatarUpload(file)) return
  uploadingAvatar.value = true
  try {
    const res = await uploadFile(file, 'image')
    if (res.code === 200 && res.data?.url) {
      profileForm.avatar = res.data.url
      ElMessage.success('头像已上传，记得保存资料')
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

async function saveProfile(): Promise<void> {
  const form = profileFormRef.value
  if (!form) return
  await form.validate(async (valid) => {
    if (!valid) return
    savingProfile.value = true
    try {
      const res = await updateProfile({
        nickname: profileForm.nickname,
        avatar: profileForm.avatar
      })
      if (res.code === 200) {
        chatStore.currentUser.nickname = profileForm.nickname
        chatStore.currentUser.avatar = profileForm.avatar
        localStorage.setItem('user', JSON.stringify(chatStore.currentUser))
        ElMessage.success('资料已更新')
      } else {
        ElMessage.error(res.msg || '资料更新失败')
      }
    } catch (error) {
      console.error('更新资料失败', error)
      ElMessage.error('资料更新失败')
    } finally {
      savingProfile.value = false
    }
  })
}

async function savePassword(): Promise<void> {
  const form = passwordFormRef.value
  if (!form) return
  await form.validate(async (valid) => {
    if (!valid) return
    savingPassword.value = true
    try {
      const res = await modifyPassword({ ...passwordForm })
      if (res.code === 200) {
        ElMessage.success('密码已修改，请重新登录')
        await handleLogout(false)
      } else {
        ElMessage.error(res.msg || '密码修改失败')
      }
    } catch (error) {
      console.error('修改密码失败', error)
      ElMessage.error('密码错误，请重新输入')
    } finally {
      savingPassword.value = false
    }
  })
}

async function handleLogout(confirm = true): Promise<void> {
  if (confirm) {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '退出登录', {
        confirmButtonText: '退出',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch {
      return
    }
  }
  try {
    await logoutApi()
  } catch (error) {
    console.error('退出登录接口异常', error)
  }
  localStorage.removeItem('user')
  websocket.closeWebSocket()
  router.push('/login')
}
</script>

<template>
  <div class="page">
    <header class="page__header">
      <h2 class="page__title">设置</h2>
      <p class="page__desc">管理个人资料与账号安全</p>
    </header>

    <section class="panel">
      <div class="panel__title">个人资料</div>
      <div class="profile">
        <div class="avatar-uploader" @click="pickAvatar">
          <UserAvatar :src="profileForm.avatar" :name="profileForm.nickname" :size="72" />
          <span class="avatar-uploader__mask">
            {{ uploadingAvatar ? '上传中…' : '更换头像' }}
          </span>
          <input
            ref="avatarInput"
            type="file"
            accept="image/jpeg,image/png"
            class="avatar-uploader__input"
            @change="onAvatarChange"
          />
        </div>

        <el-form
          ref="profileFormRef"
          :model="profileForm"
          :rules="profileRules"
          label-width="72px"
          class="profile__form"
        >
          <el-form-item label="用户名">
            <span class="profile__readonly">{{ username }}（不可修改）</span>
          </el-form-item>
          <el-form-item label="注册时间">
            <span class="profile__readonly">{{ joinedAt || '—' }}</span>
          </el-form-item>
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="profileForm.nickname" placeholder="请输入昵称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="savingProfile" @click="saveProfile">
              保存资料
            </el-button>
          </el-form-item>
        </el-form>
      </div>
      <p class="panel__hint">头像支持 JPG / PNG，大小不超过 2MB，上传后需点「保存资料」生效。</p>
    </section>

    <section class="panel">
      <div class="panel__title">修改密码</div>
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="96px"
        class="password-form"
      >
        <el-form-item label="原密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入原密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="savingPassword" @click="savePassword">
            修改密码
          </el-button>
          <span class="panel__hint panel__hint--inline">修改成功后需要重新登录</span>
        </el-form-item>
      </el-form>
    </section>

    <section class="panel">
      <div class="panel__title">账号</div>
      <el-button class="logout-btn" @click="handleLogout(true)">
        <el-icon><SwitchButton /></el-icon>
        退出登录
      </el-button>
    </section>
  </div>
</template>

<style scoped>
.page {
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px 32px;
  max-width: 720px;
}

.page__header {
  margin-bottom: 16px;
}

.page__title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
}

.page__desc {
  margin: 4px 0 0;
  font-size: 13px;
  color: var(--lc-text-3);
}

.panel {
  padding: 18px;
  margin-bottom: 16px;
  background-color: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.panel__title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 14px;
}

.panel__hint {
  margin: 8px 0 0;
  font-size: 12px;
  color: var(--lc-text-3);
}

.panel__hint--inline {
  margin: 0 0 0 12px;
}

.profile {
  display: flex;
  gap: 24px;
  align-items: flex-start;
}

.profile__form {
  flex: 1;
  min-width: 0;
}

.profile__readonly {
  font-size: 13px;
  color: var(--lc-text-2);
}

.avatar-uploader {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
}

.avatar-uploader__input {
  display: none;
}

.avatar-uploader__mask {
  font-size: 12px;
  color: var(--lc-primary);
}

.password-form {
  max-width: 420px;
}

.logout-btn {
  color: var(--lc-danger);
  border-color: var(--lc-border-strong);
}

.logout-btn:hover {
  color: #fff;
  background-color: var(--lc-danger);
  border-color: var(--lc-danger);
}
</style>