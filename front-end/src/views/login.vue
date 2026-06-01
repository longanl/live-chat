<script setup>
import { ref , onScopeDispose} from 'vue'
import { useRouter } from 'vue-router'
import { loginApi } from '@/api/login'
import { ElMessage } from 'element-plus'
const router = useRouter()
const form = ref({
  username: '',
  password: '',
  rememberMe: false
})
const handleLogin = async () => {
  try {
    // 这里添加实际的登录API调用
    const result = await loginApi(form.value);
    if(result.code === 200){
      // 登录成功
      ElMessage.success("登陆成功")
      //  存储user
      localStorage.setItem('user', JSON.stringify(result.data))
      // 跳转到首页
      router.push('/')
    }else{
      // 登录失败
      ElMessage.error("登陆失败")
    }
  } catch (error) {
    // 登录失败
      ElMessage.error("密码错误"+error)
  }
}
onScopeDispose(() => {
  //刷新页面
  window.location.reload()
})
</script>
<template>
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧品牌区 -->
      <div class="login-card-left">
        <div class="brand-title">
          <i class="fas fa-comment-dots"></i> ChatApp
        </div>
        <p class="brand-slogan">连接世界，沟通无限</p>
        <div class="features">
          <div class="feature-item">
            <i class="fas fa-users"></i>
            <span>1200+ 活跃用户</span>
          </div>
          <div class="feature-item">
            <i class="fas fa-comments"></i>
            <span>实时聊天</span>
          </div>
          <div class="feature-item">
            <i class="fas fa-lock"></i>
            <span>安全加密</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区 -->
      <div class="login-card-right">
        <h2 class="login-title">欢迎回来</h2>
        <form @submit.prevent="handleLogin">
          <div class="form-group">
            <label for="username">用户名</label>
            <div class="input-wrapper">
              <i class="fas fa-user"></i>
              <input 
                type="text" 
                id="username" 
                v-model="form.username"
                required
                placeholder="请输入用户名或邮箱"
              >
            </div>
          </div>
          
          <div class="form-group">
            <label for="password">密码</label>
            <div class="input-wrapper">
              <i class="fas fa-lock"></i>
              <input 
                type="password" 
                id="password" 
                v-model="form.password"
                required
                placeholder="请输入密码"
              >
            </div>
          </div>
          
          <div class="form-group form-options">
            <div class="remember-me">
              <input 
                type="checkbox" 
                id="remember-me" 
                v-model="form.rememberMe"
              >
              <label for="remember-me">记住我</label>
            </div>
            <a href="#" class="forgot-password">忘记密码?</a>
          </div>
          
          <button type="submit" class="login-button">
            登录
          </button>
          
          <div class="divider">
            <span>或者继续使用</span>
          </div>
          
        </form>
        
        <div class="register-link">
          <span>还没有账号?</span>
          <router-link to="/register">立即注册</router-link>
        </div>
      </div>
    </div>
  </div>
</template>



<style scoped>

/* 基础样式 */
* {
  box-sizing: border-box;
  margin: 0;
  padding: 0;
}

body {
  font-family: 'Inter', sans-serif;
  background-color: #f3f4f6;
}

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.login-card {
  display: flex;
  flex-direction: column;
  max-width: 900px;
  width: 100%;
  border-radius: 16px;
  box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

/* 左侧品牌区 */
.login-card-left {
  background: linear-gradient(135deg, #3B82F6 0%, #10B981 100%);
  color: white;
  padding: 40px;
  text-align: center;
}

.brand-title {
  font-size: 2.5rem;
  font-weight: bold;
  margin-bottom: 20px;
}

.brand-slogan {
  font-size: 1.2rem;
  margin-bottom: 40px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.feature-item {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 1.1rem;
}

/* 右侧登录表单区 */
.login-card-right {
  background-color: white;
  padding: 40px;
}

.login-title {
  font-size: 1.8rem;
  font-weight: bold;
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

label {
  display: block;
  font-size: 0.9rem;
  font-weight: medium;
  margin-bottom: 5px;
}

.input-wrapper {
  position: relative;
}

.input-wrapper i {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  color: #9ca3af;
}

.input-wrapper input {
  width: 100%;
  padding: 10px 10px 10px 35px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.3s, box-shadow 0.3s;
}

.input-wrapper input:focus {
  border-color: #3B82F6;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9rem;
}

.remember-me {
  display: flex;
  align-items: center;
  gap: 5px;
}

.forgot-password {
  color: #3B82F6;
  text-decoration: none;
  transition: color 0.3s;
}

.forgot-password:hover {
  color: #2563eb;
}

.login-button {
  width: 100%;
  padding: 10px;
  background: linear-gradient(135deg, #10B981 0%, #3B82F6 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 1rem;
  font-weight: medium;
  cursor: pointer;
  transition: background-color 0.3s;
}

.login-button:hover {
  background-color: #2563eb;
}

.login-button:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.3);
}

.divider {
  position: relative;
  text-align: center;
  margin: 25px 0;
}

.divider::before {
  content: '';
  position: absolute;
  top: 50%;
  left: 0;
  width: 100%;
  border-top: 1px solid #e5e7eb;
  z-index: 1;
}

.divider span {
  position: relative;
  z-index: 2;
  background-color: white;
  padding: 0 10px;
  color: #6b7280;
  font-size: 0.9rem;
}

.social-login {
  display: flex;
  justify-content: center;
  gap: 15px;
}

.social-button {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 40px;
  height: 40px;
  border-radius: 50%;
  color: #6b7280;
  text-decoration: none;
  transition: background-color 0.3s, color 0.3s;
}

.social-button:hover {
  background-color: #f3f4f6;
  color: #4b5563;
}

.register-link {
  margin-top: 20px;
  text-align: center;
  font-size: 0.9rem;
}

.register-link span {
  color: #6b7280;
}

.register-link router-link {
  color: #3B82F6;
  text-decoration: none;
  font-weight: medium;
  transition: color 0.3s;
}

.register-link router-link:hover {
  color: #2563eb;
}

/* 响应式设计 */
@media (min-width: 768px) {
  .login-card {
    flex-direction: row;
  }
  
  .login-card-left, .login-card-right {
    width: 50%;
  }
}
</style>