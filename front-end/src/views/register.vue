<script setup>
import { ref, reactive } from 'vue';
import { registerApi } from '@/api/login';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus'; // 从 element-plus 引入
import { UserFilled, Plus } from '@element-plus/icons-vue'; // 引入图标

const router = useRouter();

// 注册表单数据
const registerForm = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  avatar: null,
});

const formRef = ref(null);

// 头像上传
const handleAvatarSuccess = (response) => {
  registerForm.avatar = response.data;
};

// 表单验证规则
const rules = reactive({
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
    { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (_rule, value) => {
        if (value !== registerForm.password) {
          return Promise.reject('两次输入的密码不一致');
        } else {
          return Promise.resolve();
        }
      }, trigger: 'blur' }
  ]
});

// 处理头像上传
const beforeAvatarUpload = (file) => {
  const isJpgOrPng = file.type === 'image/jpeg' || file.type === 'image/png';
  if (!isJpgOrPng) {
    ElMessage.error('请上传JPG/PNG格式的图片');
    return false;
  }
  const isLt2M = file.size / 1024 / 1024 < 2;
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过2MB');
    return false;
  }
  return true;
};

// 注册处理
const handleRegister = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      // 调用注册接口
      const result = await registerApi(registerForm);
      if (result.code === 200) {
        ElMessage.success('注册成功，即将前往登录页面');
        router.push('/login');
      } else {
        ElMessage.error(result.message);
      }
    } else {
      ElMessage.error('表单验证失败，请检查输入');
    }
  });
};
</script>

<template>
  <div class="register-container">    
    <!-- 注册表单卡片 -->
    <el-card class="register-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><UserFilled /></el-icon>
          <span>创建新账号</span>
        </div>
      </template>
      
      <el-form :model="registerForm" ref="formRef" label-width="80px" class="register-form" :rules="rules">
        <!-- 基本信息 -->
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="请设置用户名" clearable />
        </el-form-item>
        
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="registerForm.nickname" placeholder="请输入您的昵称" clearable />
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password"
            type="password"
            placeholder="请设置密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="请再次输入密码"
            show-password
          />
        </el-form-item>
         

        <el-form-item label="头像">
          <el-upload
            class="avatar-uploader"
            action="/api/uploadavatar687"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="registerForm.avatar" :src="registerForm.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
          <div>文件大小不超过2MB(延迟有1~2秒)</div>
        </el-form-item>
      </el-form>
        <el-form-item>
          <el-button type="primary" class="register-btn" @click="handleRegister">注册</el-button>
        </el-form-item>
    </el-card>
    
    <!-- 底部信息 -->
    <div class="footer-info">
      <p>已有账号? <router-link to="/login" class="login-link">立即登录</router-link></p>
      <p class="copyright">© 2025 ChatApp. 保留所有权利</p>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  height: 100%;
  padding-top: 80px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.back-link {
  width: 100%;
  max-width: 450px;
  margin-bottom: 20px;
}

.register-card {
  width: 100%;
  max-width: 450px;
  margin-bottom: 40px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 18px;
  font-weight: 500;
}

.header-icon {
  margin-right: 10px;
  color: #3B82F6;
  font-size: 20px;
}

.register-form {
  padding: 30px 36px 20px; /* 增加上下左右内边距 */
}

.register-form .el-form-item {
  margin-bottom: 20px; /* 增加表单元素间距 */
}

.register-form .el-input {
  height: 40px; /* 增加输入框高度 */
}

.register-form .el-input__inner {
  height: 100%;
  border-radius: 12px; /* 增加圆角 */
  padding: 0 20px; /* 增加左右内边距 */
  font-size: 16px; /* 增大字体 */
  border: 1.5px solid #e2e8f0; /* 增加边框粗细 */
}

.register-form .el-input__prefix {
  margin-right: 12px; /* 增大前缀图标间距 */
  font-size: 18px; /* 增大图标尺寸 */
}

.register-form .el-checkbox__label {
  font-size: 15px; /* 增大复选框文字 */
}

.register-btn {
  height: 40px; /* 增大按钮高度 */
  border-radius: 14px; /* 增加按钮圆角 */
  font-size: 18px; /* 增大按钮文字 */
}

.avatar-uploader .el-upload {
  border: 1px solid #d9d9d9;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-uploader .el-upload:hover {
  border-color: #3B82F6;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}

.avatar {
  width: 120px;
  height: 120px;
  display: block;
  object-fit: cover;
  border-radius: 50%;
}

.register-btn {
  width: 100%;
  padding: 10px 0;
  font-size: 16px;
  background: linear-gradient(135deg, #3B82F6 0%, #10B981 100%);
}

.agreement-link {
  color: #3B82F6;
  text-decoration: none;
}

.agreement-link:hover {
  text-decoration: underline;
}

.footer-info {
  text-align: center;
  color: #64748b;
  font-size: 14px;
}

.login-link {
  text-decoration: none;
  font-weight: 500;
}

.login-link:hover {
  text-decoration: underline;
}

.copyright {
  margin-top: 10px;
  font-size: 12px;
}
</style>