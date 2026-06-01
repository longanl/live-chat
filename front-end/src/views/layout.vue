<script setup>
import defaultAvatar from '@/assets/default-avatar.jpg'
import Avatar from '@/assets/1.png'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chat';
import { useWebSocketStore } from '@/utils/websocket.js'
import { modifyPassword,logoutApi} from '@/api/login.js'
import { onMounted, reactive, ref } from 'vue'
import { updateProfile } from '@/api/index.js'
import { ElMessage } from 'element-plus';


const websocket = useWebSocketStore();
const chatStore = useChatStore();
const router = useRouter();
const returnMain = () => router.push({path:'/index'})
//退出登录
async function  logout (){
  try{
    const res = await logoutApi(chatStore.currentUser.id);
    if(res.code === 200){
      console.log("退出登录成功");
    }
    localStorage.removeItem('user');
    websocket.closeWebSocket();
    router.push({path:'/login'});
  }catch(error){    
    console.log(error);
  }
}
onMounted(() => {
  if(chatStore.currentUser.id === null){
    router.push({path:'/login'});
  }
  chatStore.getFriends();
})


// 对话框控制
const showPasswordDialog = ref(false)
const showImformationDialog = ref(false)

// 表单引用和数据
const formRef = ref(null)
const profileForm = ref(null)
const profileFormData= reactive({
  id: chatStore.currentUser.id,
  nickname: chatStore.currentUser.nickname,
  avatar: chatStore.currentUser.avatar
})
const formData = reactive({
  id: chatStore.currentUser.id,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 验证规则
const rules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' },
    { 
      validator: (rule, value) => {
        if (value && value === formData.oldPassword) {
          return Promise.reject('新密码不能与原密码相同')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { 
      validator: (rule, value) => {
        if (value && value !== formData.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur'
    }
  ]
})
// 表单验证规则
const profilerules = reactive({
  nickname: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
  ]
});
//关闭表单
const closePasswordDialogForm = () => {
  showPasswordDialog.value = false
  //重置表单的校验规则-提示信息
  if (formRef.value){
    formRef.value.resetFields();
  }
  
}

// 关闭资料表单
const closeProfileForm = () => {
  showImformationDialog.value = false
  if(profileForm.value){
    profileForm.value.resetFields();
  }
}
// 提交表单
const submitForm = () => {
  formRef.value.validate(valid => {
    if (valid) {
      handlePasswordChange()
    } else {
      ElMessage.error('请检查输入的格式是否正确')
      return false
    }
  })
}
// 提交资料表单
const submitProfileForm = () => {
  profileForm.value.validate(valid => {
    if (valid) {
      handleProfileSubmit()
    } else {
      ElMessage.error('请检查输入的格式是否正确')
      return false
    }
  })
}
// 处理资料修改逻辑
const handleProfileSubmit = async () =>{
  try{
    const res = await updateProfile(profileFormData);
    if(res.code === 200){
      ElMessage.success('资料修改成功')
      // 关闭对话框
      showImformationDialog.value = false
      // 更新用户信息
      chatStore.currentUser.nickname = profileFormData.nickname;
      chatStore.currentUser.avatar = profileFormData.avatar;
      localStorage.setItem('user',JSON.stringify(chatStore.currentUser));
      // 重置表单
      profileForm.value.resetFields();
    }else{
      showNotification('失败', '资料修改失败，请稍后重试', 'error')
    }
  }catch(error){
    console.log(error);
  }
}
// 处理密码修改逻辑
const handlePasswordChange = async () => {
  try {
    // 发送修改密码请求
    const res = await modifyPassword(formData);
    if(res.code === 200){
      ElMessage.success('密码修改成功，请重新登录')
      // 关闭对话框
       showPasswordDialog.value = false
      // 重置表单
      if (formRef.value){
      formRef.value.resetFields();
      }
      // 退出登录
      logout();
    }else{
        showNotification('失败', '密码修改失败，请稍后重试', 'error')
    }
  } catch (error) {
    ElMessage.error("密码错误，请重新输入")
    // 重置表单
    if (formRef.value){
    formRef.value.resetFields();
    }
  }
}
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
// 头像上传
const handleAvatarSuccess = (response) => {
  profileFormData.avatar = response.data;
};
const selectContact=(contact)=>{
  websocket.selectContact(contact);
  chatStore.updateHistory(chatStore.currentUser.id,contact.id);
  router.push('/p2pchat');
}



</script>
<template>
  <div class="chat-app-container">
    <!-- 顶部导航栏 -->
    <el-header height="60px" class="app-header">
      <div class="app-logo">ChatApp</div>
      <el-menu
        mode="horizontal"
        background-color="transparent"
        text-color="#fff"
        active-text-color="#fff"
        :ellipsis="false"
        router
      >
        <el-menu-item index="/index"><el-icon><House /></el-icon> 首页</el-menu-item>
        <el-menu-item index="/contact"><el-icon><Phone /></el-icon> 联系人</el-menu-item>
        <div class="user-info" id="userInfo">
          <div class="avatar" @click="showImformationDialog = true">
            <el-avatar :size="40" :src="chatStore.currentUser.avatar || defaultAvatar" />
          </div>
          <div class="nickname">{{ chatStore.currentUser.nickname }}</div>
            <el-dropdown>
              <span class="el-dropdown-link">
               <el-icon><ArrowDownBold /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="showImformationDialog = true"><el-icon><EditPen /></el-icon>修改资料</el-dropdown-item>
                  <el-dropdown-item divided @click="showPasswordDialog = true"><el-icon><Edit /></el-icon>修改密码</el-dropdown-item>
                  <el-dropdown-item divided @click="logout"><el-icon><SwitchButton /></el-icon>退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
        </div>
      </el-menu>
        
    </el-header>

    <!-- 修改资料对话框 -->
    <el-dialog
      title="修改资料"
      v-model="showImformationDialog" 
      width="450px"
      :close-on-click-modal="false"
    ><!-- 基本信息 -->
      <el-form ref="profileForm" :model="profileFormData" :rules="profilerules" label-width="80px" class="profile-form">
        <el-form-item label="用户名" prop="username">
          {{ chatStore.currentUser.username }}(不可修改)
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="profileFormData.nickname" placeholder="请输入您的昵称" clearable />
        </el-form-item>
         
        <el-form-item label="头像" prop="avatar">
          <el-upload
            class="avatar-uploader"
            action="/api/uploadavatar687"
            :show-file-list="false"
            :on-success="handleAvatarSuccess"
            :before-upload="beforeAvatarUpload">
            <img v-if="profileFormData.avatar" :src="profileFormData.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
            
          </el-upload>
          <div>文件大小不超过2MB(延迟有1~2秒)</div>

        </el-form-item>
      </el-form>
      <div class="dialog-footer">
        <el-button @click="closeProfileForm">取消</el-button>
        <el-button type="primary" @click="submitProfileForm">确认修改</el-button>
      </div>
    </el-dialog>


    <!-- 修改密码对话框 -->
    <el-dialog 
      v-model="showPasswordDialog" 
      title="修改密码" 
      width="450px"
      :close-on-click-modal="false"
    >
    <el-form 
      ref="formRef" 
      :model="formData" 
      :rules="rules" 
      label-width="120px"
      class="password-form"
    >
      <!-- 原密码 -->
      <el-form-item label="原密码" prop="oldPassword">
        <el-input 
          v-model="formData.oldPassword" 
          type="password" 
          placeholder="请输入原密码"
          show-password
        />
      </el-form-item>
      
      <!-- 新密码 -->
      <el-form-item label="新密码" prop="newPassword">
        <el-input 
          v-model="formData.newPassword" 
          type="password" 
          placeholder="请输入新密码"
          show-password
        />
      </el-form-item>
      
      <!-- 确认新密码 -->
      <el-form-item label="确认新密码" prop="confirmPassword">
        <el-input 
          v-model="formData.confirmPassword" 
          type="password" 
          placeholder="请再次输入新密码"
          show-password
        />
    </el-form-item>
    </el-form>

    <div class="dialog-footer">
      <el-button @click="closePasswordDialogForm">取消</el-button>
      <el-button type="primary" @click="submitForm">确认修改</el-button>
    </div>
    </el-dialog>

    <!-- 主体布局：侧边栏 + 聊天区 + 信息栏 -->
    <el-container class="main-container">

      <!-- 左侧侧边栏 -->
      <el-aside width="240px" class="sidebar">
        <!-- 搜索框 -->
        <el-input
          placeholder="搜索聊天或联系人"
          prefix-icon="Search"
          class="search-input"
        />

        <!-- 会话列表 -->
        <div class="session-list">
          <div class="session-group">
            <div class="group-title">活跃</div>
            <div class="session-item active" @click="returnMain">
              <div class="contact-avatar">
                <img :src="Avatar" alt="技术讨论群头像" class="el-avatar el-avatar--circle">
              </div>
              <div class="session-info">
                <div class="session-name">技术讨论群</div>
                <div class="session-last-msg">最新消息预览</div>
              </div>
            </div>
          </div>
          <div class="session-group">
            <div class="group-title">最近</div>
            <div class="session-item" v-for="contact in chatStore.filteredContacts" :key="contact.id" @click="selectContact(contact)">
              <div class="contact-avatar">
                <img :src="contact.avatar || defaultAvatar" class="el-avatar el-avatar--circle">
              </div>
              <div class="session-info">
                <div class="session-name">{{ contact.nickname }}</div>
              </div>
              <div class="unread-count " v-if="chatStore.getUnreadCount(contact.id) > 0">{{ chatStore.getUnreadCount(contact.id) }}</div>
            </div>
          </div>
        </div>
      </el-aside>

      <!-- 中间聊天区域 -->
      <el-main>
        <router-view></router-view>
      </el-main>

      <!-- 右侧信息栏 -->
      <el-aside width="280px" class="info-aside">
        <div class="group-info">
          <div class="group-avatar">
            <img :src="Avatar" class="el-avatar el-avatar--circle">
          </div>
          <div class="group-name">
            技术讨论群
            <div class="group-desc">用于技术交流和问题讨论</div>
          </div>
        </div>
        <div class="group-meta">
          <div class="meta-item">
            <span>创建者</span>
            <span>管理员</span>
          </div>
          <div class="meta-item">
            <span>创建时间</span>
            <span>2025-07-15</span>
          </div>
          <div class="meta-item">
            <span>成员数量</span>
            <span>{{  websocket.onlineCount }}人</span>
          </div>
          <div class="meta-item">
            <span>在线成员</span>
            <span>{{websocket.onlineUserList.length}}人</span>
          </div>
        </div>
        <el-button type="primary" class="group-detail-btn" @click="returnMain">
          返回群组
        </el-button>
        <div class="member-list-title">在线成员</div>

          <div class="member-list" v-for="(user, index) in websocket.onlineUserList" :key="user.id || index">
            <span class="group-number">
                <div class="contact-avatar">
                  <img :src="user.avatar || defaultAvatar" class="el-avatar el-avatar--circle">
                </div>
                <div class="session-info">
                  <div class="session-name">{{ user.nickname }}</div>
                </div>
            </span>
          </div>
      </el-aside>
    </el-container>
  </div>
</template>

<style scoped>
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
.el-dropdown-link{
  color: #ffffff;
  font-size: 16px;
  margin: 0 10px;
}
.user-info {
  display: flex;
  align-items: center;
  cursor: pointer; 
  margin-left: 10px;
}
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
  border: 1px solid #ccc;
  background-color: #ecf0f1;
  background-size: cover;
  background-position: center;
}

.nickname {
  margin-right: 10px;
  white-space: nowrap; /* 防止昵称换行 */
  overflow: hidden; /* 超出部分隐藏 */
  text-overflow: ellipsis; /* 超出部分显示省略号 */
  max-width: 100px; /* 限制昵称最大宽度 */
}


.password-change-wrapper {
  padding: 20px;
}

.password-form {
  padding: 10px 0;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}

/* 表单验证提示样式 */
::v-deep .el-form-item__error {
  font-size: 12px;
  padding-top: 4px;
}



#changePassword{
  color: #333;
}
#logout{
  color: #333;
}
.member-info {
  margin: 15px;
}

.contact-avatar {
  margin-right: 12px;
}

.chat-app-container {
  height: 100vh;
  background-color: #f5f7fa;
}

.app-header {
  background-color: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.app-logo {
  font-size: 20px;
  font-weight: bold;
}

.main-container {
  display: flex;
  height: calc(100% - 60px);
}

.sidebar {
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
}

.search-input {
  margin: 5px;
  width: 230px;
}

.sidebar-menu {
  border-right: none;
}

.session-group {
  margin: 10px;
}

.group-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.session-item {
  display: flex;
  align-items: center;
  padding: 5px;
  cursor: pointer;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.session-item:hover {
  background-color: #e6f3ff;
}

.session-info {
  flex: 1;
}

.session-name {
  font-weight: bold;
  margin-bottom: 2px;
}

.session-last-msg {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.unread-count {
  background-color: #f56c6c;
  color: #fff;
  font-size: 12px;
  padding: 2px 6px;
  border-radius: 12px;
  min-width: 20px;
  text-align: center;
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 20px;
  background-color: #fff;
  border-right: 1px solid #e6e6e6;
}

.chat-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 10px;
  border-bottom: 1px solid #f0f0f0;
}

.chat-title {
  font-size: 18px;
  font-weight: bold;
}

.online-info {
  font-size: 12px;
  color: #999;
  margin-left: 10px;
}

.chat-actions {
  display: flex;
}

.chat-actions i {
  margin-left: 15px;
  cursor: pointer;
  font-size: 18px;
  color: #999;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  margin-bottom: 20px;
  padding-right: 10px;
}

.message-item {
  display: flex;
  margin-bottom: 15px;
}

.message-item.mine {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  color: #666;
  margin: 0 10px;
}

.message-content {
  display: flex;
  flex-direction: column;
  max-width: 70%;
}

.message-sender {
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.message-text {
  padding: 8px 12px;
  border-radius: 18px;
  color: #333;
  word-wrap: break-word;
  background-color: #e4e7ed;
}

.message-item.mine .message-text {
  background-color: #409eff;
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  align-self: flex-end;
}

.message-input-area {
  display: flex;
  align-items: center;
}

.message-input {
  flex: 1;
  border-radius: 20px;
}

.input-actions {
  display: flex;
  gap: 10px;
  margin-right: 10px;
}

.send-btn {
  margin-left: 10px;
}

.info-aside {
  background-color: #fff;
  padding: 20px;
  border-left: 1px solid #e6e6e6;
}

.group-info {
  display: flex;
  align-items: center;
  margin-bottom: 15px;
}

.group-avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: #666;
  margin-right: 10px;
}

.group-name {
  font-size: 16px;
  font-weight: bold;
}

.group-desc {
  font-size: 12px;
  color: #999;
}

.group-meta {
  margin-bottom: 15px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 5px;
  color: #666;
}

.group-detail-btn {
  width: 100%;
  margin-bottom: 15px;
}

.member-list-title {
  font-weight: bold;
  margin-bottom: 10px;
}

.group-number {
  display: flex;
  align-items: center;
  padding: 5px;
  border-radius: 4px;
  overflow: auto;
}
.member-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #666;
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>