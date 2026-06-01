<script setup>
import { ref, computed } from 'vue'
import defaultAvatar from '@/assets/default-avatar.jpg'
import { addFriends,lookNewFriend,newFriends,removeFriends } from '@/api/friends'
import { useChatStore } from '@/stores/chat'
import { useWebSocketStore } from '@/utils/websocket.js'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
const router = useRouter()
const chatStore = useChatStore()
const websocket = useWebSocketStore()


// 弹窗
const showAddDialog = ref(false)
const showNewFriendDialog = ref(false)



// 添加好友相关
const newContactusername = ref('')// 待添加的联系人
// 添加联系人
const addContact = async() => {
  try{
    const res = await addFriends(chatStore.currentUser.id,newContactusername.value)
    if(res.code === 200){
      ElMessage.success('好友请求发送成功')
      showAddDialog.value = false
      //重置表单的校验规则-提示信息
    }
  }catch(err){
    console.log(err)
    ElMessage.error('好友请求发送失败')
  }
}

// 备注相关
const showRemarkDialogVisible = ref(false)
const currentRemark = ref('')
const remark = ref('')
// 显示备注弹窗
const showRemarkDialog = () => {
  showRemarkDialogVisible.value = true
}

// 保存备注
const saveRemark = () => {
  currentRemark.value = remark.value
  showRemarkDialogVisible.value = false
}

//新好友相关
const newFriendList = ref([])//新朋友列表
//查看新朋友列表
const lookupNewFriend = async () =>{
  try{
    const res = await lookNewFriend(chatStore.currentUser.id);
    if(res.code === 200){
      newFriendList.value = res.data;
      showNewFriendDialog.value = true;
    }else{
      console.log(res.message);
    }
  }catch(err){
    console.log(err);
  }
}
//同意添加好友
const handleAgree = async (friendId) => {
  try{
    const res = await newFriends(chatStore.currentUser.id, friendId)
    if(res.code === 200){
      ElMessage.success("添加成功")
      chatStore.getFriends()
      showNewFriendDialog.value = false
    }
  }catch(err){
    ElMessage.error('好友请求发送失败')
  }
}
//拒绝添加好友
const handleReject = async (friendId) => {
  try{
    const res = await removeFriends(chatStore.currentUser.id,friendId)
    if(res.code === 200){
      ElMessage.warning("拒绝成功")
      showNewFriendDialog.value = false
    }
  }catch(err){
    ElMessage.error('好友请求发送失败')
  }
}

// 删除相关
const showDeleteDialog = ref(false)
const deleteId = ref(null) // 待删除的联系人ID
// 全部联系人
const contacts = computed(() => chatStore.filteredContacts || [])
// 在线联系人
const onlineContacts = computed(() => 
  (chatStore.filteredContacts || []).filter(c => c.status === 1)
)
// 离线联系人
const offlineContacts = computed(() => 
  (chatStore.filteredContacts || []).filter(c => c.status === 0)
)

// 选择联系人
const selectContact = (contact) => {
  websocket.selectedContact = contact
}
const enterChat = (contact) =>{
  websocket.selectContact(contact);
  chatStore.updateHistory(chatStore.currentUser.id,contact.id);
  router.push('/p2pchat');
}
//删除相关
const showDeleteConfirm = (id) => {
  deleteId.value = id
  showDeleteDialog.value = true
}
// 确认删除
const confirmDelete = () => {
  chatStore.deletePerson(chatStore.currentUser.id, deleteId.value);
  showDeleteDialog.value = false
}



function formatTime(timeStr) {
  return new Date(timeStr).toLocaleString();
}
</script>
<template>
  <div class="contact-page">
    <!-- 顶部搜索区（优化为圆角矩形） -->
    <div class="top-container">
      <div class="top-bar">
        <h2 class="page-title">联系人</h2>
        <div class="search-container">
          <svg class="search-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M11 19C15.4183 19 19 15.4183 19 11C19 6.58172 15.4183 3 11 3C6.58172 3 3 6.58172 3 11C3 15.4183 6.58172 19 11 19Z" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M21 21L16.65 16.65" stroke="#999" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <input 
            type="text" 
            v-model="contacts.nickname" 
            placeholder="搜索联系人或备注" 
            class="search-input"
          >
        </div>
      </div>

      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button type="primary" size="default" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon> 添加联系人
        </el-button>
        <el-button type="primary" size="default" @click=lookupNewFriend()>
          <el-icon><Iphone /></el-icon> 新朋友
        </el-button>
      </div>
    </div>

  <!-- 新朋友弹窗 -->
  <el-dialog 
  v-model="showNewFriendDialog" 
  title="新朋友" 
  width="350px" 
  :close-on-click-modal="false"
  >
  <div class="friend-request-container">
    <el-card 
      class="request-card" 
      shadow="hover" 
      v-for="(request, index) in newFriendList" 
      :key="request.id || index"
    >
      <template #header>
        <div class="card-header">
          <span class="request-time">{{ formatTime(request.createTime) }}</span>
        </div>
      </template>
      
      <!-- 用户信息 -->
      <div class="user-info">
        <img 
          :src="request.avatar || defaultAvatar" 
          alt="用户头像" 
          class="avatar"
        >
        <div class="user-detail">
          <div class="nickname">昵称：{{ request.nickname || '未知用户' }}</div>
        </div>
      </div>
      
      <!-- 验证消息 -->
      <div class="verify-message">
        <span class="message-label">验证消息：</span>
        <span>{{ request.nickname }}请求加您好友</span>
      </div>
      
      <!-- 操作按钮 -->
      <div class="action-buttons">
        <el-button 
          type="primary" 
          @click="handleAgree(request.id)"
        >
          同意
        </el-button>

        <el-button 
          type="danger" 
          @click="handleReject(request.id)"
        >
          拒绝
        </el-button>
      </div>
    </el-card>
    
    <!-- 空状态 -->
    <div class="empty-state" v-if="newFriendList.length === 0">
      <i class="el-icon-information"></i>
      <p>暂无新朋友请求</p>
    </div>
  </div>
  </el-dialog>

    <!-- 联系人分组 -->
    <div class="contact-groups">
      <!-- 在线联系人 -->
      <div class="group-section">
        <div class="group-header">
          <span class="group-name">在线联系人</span>
          <span class="group-count">{{ onlineContacts.length }}</span>
        </div>
        <ul class="contact-list">
          <li 
            v-for="contact in onlineContacts" 
            :key="contact.id"
            class="contact-item"
            @click="selectContact(contact)"
            @dblclick="enterChat(contact)"
            :class="{ 'selected': selectedContact?.id === contact.id }"
          >
            <div class="avatar-wrapper">
              <img :src="contact.avatar || defaultAvatar" alt="头像" class="avatar">
              <span class="online-indicator"></span>
            </div>
            <div class="contact-info">
              <div class="contact-name">
                {{ currentRemark || contact.nickname }} <!-- 优先显示备注 -->
                <template v-if="currentRemark">
                  <span class="original-name">({{ contact.nickname }})</span>
                </template>
              </div>
              <div class="contact-meta">双击进入聊天</div>
            </div>
            <div class="contact-actions">
              <el-tooltip effect="dark" content="编辑备注">
                <el-icon 
                  class="action-icon edit-icon" 
                  @click.stop="showRemarkDialog()"
                >
                  <Edit />
                </el-icon>
              </el-tooltip>
              <el-tooltip effect="dark" content="删除联系人">
                <el-icon 
                  class="action-icon delete-icon" 
                  @click.stop="showDeleteConfirm(contact.id)"
                >
                  <Delete />
                </el-icon>
              </el-tooltip>
            </div>
          </li>
        </ul>
      </div>

      <!-- 离线联系人 -->
      <div class="group-section">
        <div class="group-header">
          <span class="group-name">离线联系人</span>
          <span class="group-count">{{ offlineContacts.length }}</span>
        </div>
        <ul class="contact-list">
          <li 
            v-for="contact in offlineContacts" 
            :key="contact.id"
            class="contact-item"
            @click="selectContact(contact)"
            @dblclick="enterChat(contact)"
            :class="{ 'selected': selectedContact?.id === contact.id }"
          >
            <div class="avatar-wrapper">
              <img :src="contact.avatar || defaultAvatar" alt="头像" class="avatar offline-avatar">
            </div>
            <div class="contact-info">
              <div class="contact-name">
                {{ currentRemark || contact.nickname }} <!-- 优先显示备注 -->
                <template v-if="currentRemark">
                  <span class="original-name">({{ contact.nickname }})</span>
                </template>
              </div>
              <div class="contact-meta">{{ contact.nickname }}</div>
            </div>
            <div class="contact-actions">
              <el-tooltip effect="dark" content="编辑备注">
                <el-icon 
                  class="action-icon edit-icon" 
                  @click.stop="showRemarkDialog()"
                >
                  <Edit />
                </el-icon>
              </el-tooltip>
              <el-tooltip effect="dark" content="删除联系人">
                <el-icon 
                  class="action-icon delete-icon" 
                  @click.stop="showDeleteConfirm(contact.id)"
                >
                  <Delete />
                </el-icon>
              </el-tooltip>
            </div>
          </li>
        </ul>
      </div>
    </div>

    <!-- 添加联系人弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加联系人" width="350px" class="custom-dialog">
      <el-form :model="newContactusername" label-width="80px" class="add-form">
        <el-form-item label="用户名" prop="username" :rules="[{ required: true, message: '请输入用户名', trigger: 'blur' }]">
          <el-input v-model="newContactusername" placeholder="请输入用户名"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="addContact" class="confirm-btn">确定</el-button>
      </template>
    </el-dialog>

    <!-- 备注弹窗 -->
    <el-dialog v-model="showRemarkDialogVisible" title="设置备注" width="350px" class="custom-dialog">
      <el-form  label-width="80px" class="remark-form">
        <el-form-item label="备注名" prop="remark">
          <el-input v-model="remark" placeholder="请输入备注名（选填）"></el-input>
          <div class="remark-hint">提示：由于作者不想再改数据库了，此功能有BUG，请谅解</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showRemarkDialogVisible = false" class="cancel-btn">取消</el-button>
        <el-button type="primary" @click="saveRemark" class="confirm-btn">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <el-dialog 
      v-model="showDeleteDialog" 
      title="删除联系人" 
      width="350px" 
      class="delete-dialog"
    >
      <div class="delete-content">
        <el-icon class="warning-icon"><Warning /></el-icon>
        <p>确定要删除该联系人吗？此操作不可撤销。</p>
      </div>
      <template #footer>
        <el-button @click="showDeleteDialog = false" class="cancel-btn">取消</el-button>
        <el-button type="danger" @click="confirmDelete" class="delete-btn">删除</el-button>
      </template>
    </el-dialog>
  </div>
</template>



<style scoped>
.contact-page {
  background-color: #f7f7f7;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

/* 顶部区域美化 */
.top-container {
  background-color: white;
  border-radius: 0 0 12px 12px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  overflow: hidden;
  margin-bottom: 15px;
}

.top-bar {
  background-color: #0084ffd6;
  padding: 20px 20px 15px;
  border-radius: 0 0 12px 12px;
  position: relative;
  overflow: hidden;
}

/* 顶部装饰 */
.top-bar::before {
  content: '';
  position: absolute;
  top: 0;
  right: 20px;
  width: 120px;
  height: 120px;
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 50%;
  transform: translateY(-60%);
}

.page-title {
  margin: 0 0 15px 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
  position: relative;
  z-index: 1;
}

.search-container {
  position: relative;
  background-color: rgba(255, 255, 255, 0.25);
  border-radius: 24px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  backdrop-filter: blur(5px);
  position: relative;
  z-index: 1;
}

.search-icon {
  margin-right: 10px;
  flex-shrink: 0;
  color: rgba(255, 255, 255, 0.8);
}

.search-input {
  background: transparent;
  border: none;
  color: white;
  width: 100%;
  outline: none;
  font-size: 14px;
}

.search-input::placeholder {
  color: rgba(255, 255, 255, 0.6);
}

/* 操作按钮 */
.action-buttons {
  padding: 12px 20px;
  background-color: white;
  border-bottom: 1px solid #f0f0f0;
}

.add-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 132, 255, 0.3);
}

/* 联系人分组样式 */
.contact-groups {
  flex: 1;
  overflow-y: auto;
  padding: 15px 20px;
}

.group-section {
  margin-bottom: 30px;
  background-color: white;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  overflow: hidden;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  font-size: 13px;
  color: #666;
  background-color: #fafafa;
  border-bottom: 1px solid #f0f0f0;
}

.group-count {
  background-color: #e8f3ff;
  color: #0084ffda;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

/* 联系人列表样式 */
.contact-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.contact-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background-color: white;
  border-bottom: 1px solid #f7f7f7;
  cursor: pointer;
  transition: background-color 0.2s;
  position: relative;
}

.contact-item:last-child {
  border-bottom: none;
}

.contact-item:hover {
  background-color: #f9f9f9;
}

.contact-item.selected {
  background-color: #f0f7ff;
}

.contact-item.selected::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  width: 3px;
  background-color: #0084ff;
}

/* 头像样式 */
.avatar-wrapper {
  position: relative;
  margin-right: 15px;
  flex-shrink: 0;
}

.avatar {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #f0f0f0;
}

.offline-avatar {
  filter: grayscale(70%);
}

.online-indicator {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 14px;
  height: 14px;
  background-color: #00c853;
  border: 2px solid white;
  border-radius: 50%;
  box-shadow: 0 0 0 1px rgba(0, 200, 83, 0.3);
}

/* 联系人信息样式 */
.contact-info {
  flex: 1;
  overflow: hidden;
}

.contact-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  display: flex;
  align-items: center;
  gap: 6px;
}

.original-name {
  font-size: 12px;
  color: #999;
  font-weight: normal;
}

.contact-meta {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 操作图标 */
.contact-actions {
  display: flex;
  gap: 15px;
  opacity: 0;
  transition: opacity 0.2s;
  margin-left: 10px;
}

.contact-item:hover .contact-actions {
  opacity: 1;
}

.action-icon {
  width: 20px;
  height: 20px;
  cursor: pointer;
  transition: transform 0.2s;
}

.action-icon:hover {
  transform: scale(1.15);
}

.edit-icon {
  color: #0084ff;
}

.delete-icon {
  color: #ff4d4f;
}

/* 弹窗样式 */
.custom-dialog {
  border-radius: 12px;
  overflow: hidden;
}

.custom-dialog .el-dialog__header {
  background-color: #f7f9fc;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.custom-dialog .el-dialog__title {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}

.custom-dialog .el-dialog__body {
  padding: 24px 20px;
}

.add-form, .remark-form {
  margin-top: 10px;
}

.status-item {
  margin-bottom: 10px;
}

.status-radio {
  display: flex;
  gap: 20px;
  margin-top: 5px;
}

.radio-option {
  padding: 4px 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.radio-option.is-checked {
  background-color: #f0f7ff;
  color: #0084ffb7;
}

.remark-hint {
  margin-top: 6px;
  font-size: 12px;
  color: #999;
}

.delete-dialog .el-dialog__body {
  padding: 30px 20px;
}

.delete-content {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #666;
  font-size: 14px;
}

.warning-icon {
  color: #faad14;
  font-size: 20px;
}

/* 按钮样式 */
.cancel-btn, .confirm-btn {
  border-radius: 6px;
  padding: 6px 16px;
  font-size: 14px;
}

.cancel-btn {
  background-color: #f5f5f5;
  color: #666;
  border: none;
}

.cancel-btn:hover {
  background-color: #eeeeee;
  color: #333;
}

.confirm-btn {
  background-color: #0084ffe0;
  border: none;
}

.confirm-btn:hover {
  background-color: #0073e6c0;
}

.delete-dialog .confirm-btn {
  background-color: #ff4d4f;
}

.delete-dialog .confirm-btn:hover {
  background-color: #f5222d;
}

/* 提示样式 */
.el-tooltip__popper {
  border-radius: 6px;
  padding: 5px 10px;
  font-size: 12px;
}

/* 新朋友弹窗整体样式 */
.friend-request-container {
  padding: 10px 5px;
  max-height: 500px; /* 限制高度，超出可滚动 */
  overflow-y: auto;
}
/* 隐藏 WebKit 内核浏览器（Chrome、Safari 等）的滚动条 */
.friend-request-container::-webkit-scrollbar {
  display: none; /* 直接隐藏滚动条 */
}

/* 卡片样式 */
.request-card {
  margin-bottom: 12px;
  border-radius: 8px;
  border: 1px solid #f0f0f0;
  transition: all 0.2s ease;
}
.request-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  border-color: #e5e5e5;
}

/* 卡片头部（时间） */
.card-header {
  padding: 8px 15px;
  color: #999;
  font-size: 12px;
  border-bottom: 1px dashed #f0f0f0;
}

/* 用户信息区域 */
.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px;
}

/* 头像样式 */
.avatar {
  width: 54px;
  height: 54px;
  border-radius: 50%;
  object-fit: cover;
  border: 1px solid #f0f0f0;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.03);
}

/* 用户详情文字 */
.user-detail {
  .nickname {
    font-size: 15px;
    font-weight: 500;
    color: #333;
    line-height: 1.4;
  }
  .username {
    font-size: 12px;
    color: #666;
    margin-top: 2px;
  }
}

/* 验证消息区域 */
.verify-message {
  padding: 0 15px 12px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
  border-top: 1px solid #f7f7f7;
  margin: 0 15px;
}
.message-label {
  color: #999;
  margin-right: 4px;
}

/* 操作按钮区域 */
.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 15px;
  border-top: 1px solid #f7f7f7;
  margin-top: 5px;
}
.action-buttons .el-button--small {
  padding: 4px 14px;
  border-radius: 4px;
  font-size: 12px;
}
.action-buttons .el-button--primary {
  background-color: #409eff;
  border-color: #409eff;
}
.action-buttons .el-button--danger {
  background-color: #f56c6c;
  border-color: #f56c6c;
}

/* 空状态样式 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
  text-align: center;
}
.empty-state .el-icon-information {
  font-size: 36px;
  margin-bottom: 15px;
  color: #ddd;
}
.empty-state p {
  font-size: 14px;
  margin: 0;
}

/* 弹窗样式覆盖 */
:deep(.el-dialog__body) {
  padding: 10px 20px;
}
:deep(l-dialog__header) {
  padding: 15px 20px;
  border-bottom: 1px solid #f5f5f5;
}
:deep(l-dialog__title) {
  font-size: 16px;
  color: #333;
  font-weight: 500;
}
:deep(l-dialog__footer) {
  padding: 12px 20px;
  border-top: 1px solid #f5f5f5;
}
</style>