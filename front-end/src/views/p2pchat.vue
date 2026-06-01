<script setup>
import defaultAvatar from '@/assets/default-avatar.jpg'
import { ref, onMounted ,nextTick ,watch ,computed } from 'vue'
import { useChatStore } from '@/stores/chat';
import { useRouter } from 'vue-router'
import { useWebSocketStore } from '@/utils/websocket.js'

const chatStore = useChatStore();
const websocket = useWebSocketStore();
const router = useRouter();

if(!websocket.selectedContact){
    router.push('/');
}
async function handleSeed() {
  if(message.value.trim() === ''){  return; }
  websocket.seedMessage('p2pchat',{
    senderId: chatStore.currentUser.id,
    receiverId: websocket.selectedContact.id,
    content: message.value
  })
  //清空输入框
  message.value = '';
  //滚动到底部
  scrollToBottom();
}
// 过滤消息
const filteredHistory = computed(() => {
  return chatStore.history.filter(msg => (msg.receiverId === websocket.selectedContact.id && msg.senderId === chatStore.currentUser.id) || (msg.receiverId === chatStore.currentUser.id && msg.senderId === websocket.selectedContact.id))
})
const message = ref('');
const chatScrollbar = ref(null);
const scrollToBottom = async () => {
  await nextTick(); 
  setTimeout(() => {
    if (chatScrollbar.value) {
    chatScrollbar.value.scrollTop = chatScrollbar.value.scrollHeight;
    }
  }, 500); // 延迟 1 秒
};
watch(
  () => filteredHistory.value.length,
  () => {
    scrollToBottom(); // 消息数量变化时滚动到底部
  }
);
onMounted(() => {
  chatStore.search();
  scrollToBottom();
});
function formatTime(timeStr) {
  return new Date(timeStr).toLocaleString();
}
</script>
<template>
  <!-- 顶部导航栏（固定不动） -->
  <div class="chat-header">
    <div class="chat-title">
      <span>{{websocket.selectedContact.nickname}}</span>
      <span class="online-info" :class="{ online: websocket.selectedContact.status === 1 }">
        <!-- 动态显示“在线”或“离线” -->
        {{ websocket.selectedContact.status === 1 ? '在线' : '离线' }}
      </span>
    </div>
  </div>

  <!-- 中间聊天区域（唯一可滚动区域） -->
<div class="chat-messages" ref="chatScrollbar">
  <!-- 只渲染 receiverId 为 0 的消息 -->
  <div v-if="filteredHistory.length">
    <div 
      v-for="(msg, index) in filteredHistory" 
      :key="msg.id || index"
    >
      <div class="message-wrapper" :class="{ 'own-message': msg.senderId === chatStore.currentUser.id }">
        <div class="avatar">
          <el-avatar :src="msg.avatar || defaultAvatar" :size="36"/>
        </div>
        <div class="message-content">
          <div class="nickname">{{ msg.nickname }}</div>
          <div class="content" :class="{ 'own-bubble': msg.senderId === chatStore.currentUser.id }">
            <div>{{ msg.content }}</div>
            <div class="time">{{ formatTime(msg.sendTime) }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
    <!-- 当没有符合条件的消息时显示提示 -->
  <div v-else class="no-messages">
    暂无公共消息
  </div>
</div>

  <!-- 底部输入框（固定不动） -->
  <div class="chat-input-area">
    <el-input placeholder="输入消息..." class="message-input" v-model="message" @keyup.enter="handleSeed">
      <template #suffix>
        <div class="input-tools" @click="scrollToBottom">
         <el-icon ><Link /></el-icon>
        </div>
      </template>
    </el-input>
    <el-button type="primary" class="send-btn" @click="handleSeed">
      发送
    </el-button>
  </div>
</template>

<style scoped>
/* 顶部导航：固定高度，不滚动 */
.chat-header {
  top: 60px;
  height: 56px;
  padding: 0 16px;
  display: flex;
  flex-direction: row;
  justify-content: space-between;
  align-items: center;
  background-color: #fff;
  border-bottom: 1px solid #eee;
  width: 96%;
  z-index: 100;
}

.chat-title {
  font-size: 16px;
  font-weight: 600;
  flex-direction: row;
}

.online-info {
  font-size: 12px;
  color: #888;
  margin-left: 8px;
  position: relative;
  padding-left: 15px; /* 为绿色点留出空间 */
}
/* 离线状态（灰色点） */
.online-info::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 8px;
  height: 8px;
  background-color: #888; /* 灰色 */
  border-radius: 50%; /* 圆形 */
}
/* 在线状态（绿色点） */
.online-info.online::before {
  background-color: rgb(20, 195, 20); /* 绿色 */
}
/* 底部输入框：固定在底部，不滚动 */
.chat-input-area {
  height: 60px;
  padding: 0 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  background-color: #fff;
  border-top: 1px solid #eee;
  flex-shrink: 0;
  bottom: 0;
  width: 96%;
  z-index: 100;
}

.message-input {
  flex: 1;
  border-radius: 20px;
  height: 40px;
}
.input-tools {
  display: flex;
  width: 100%;
  color: #666;
  cursor: pointer;
}
.send-btn {
  border-radius: 20px;
  padding: 6px 20px;
}



/* 中间聊天区域：唯一可滚动区域 */
.chat-messages {
  list-style: none;
  padding: 0;
  margin: 0;
  height: 510px;
  overflow: auto;
  -ms-overflow-style: none; /* 隐藏滚动条 */
  background-color: #f5f7fa;
  padding-top: 20px;
  padding-bottom: 20px;
  position: relative;
}
.chat-messages::-webkit-scrollbar {
  display: none;
}

.message-wrapper {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
}

.own-message {
  flex-direction: row-reverse;
  justify-content: flex-start;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  border: 1px solid #ccc;
  background-color: #f0f0f0; /* 加载前的背景 */
  display: flex;
  justify-content: center;
  align-items: center;
  position: relative;
}
.avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover; /* 关键属性：覆盖容器 */
  transition: opacity 0.3s;
}
.message-content {
  display: flex;
  flex-direction: column;
  max-width: 80%;
}

.nickname {
  font-size: 12px;
  color: #888;
  margin-bottom: 4px;
  padding-left: 4px;
}

.own-message .nickname {
  text-align: right;
  display: flex;
  flex-direction: column;
  max-width: 93%;
}

.content {
  position: relative;
  padding: 8px 12px;
  border-radius: 18px;
  font-size: 14px;
  word-wrap: break-word;
  background-color: #fff;
  border: 1px solid #eee;
}

.own-bubble {
  background-color: #2B79F5;
  color: white;
  border-radius:18px;
}

.message-wrapper:not(.own-message) .content::before {
  left: -12px;
  border-right: 12px solid #fff;
}

.message-wrapper:not(.own-message) .content::after {
  content: '';
  position: absolute;
  top: 10px;
  left: -13px;
  width: 0;
  height: 0;
  border-top: 6px solid transparent;
  border-bottom: 6px solid transparent;
  border-right: 12px solid #eee;
  z-index: -1;
}


/* 消息时间样式 */
.time {
  font-size: 10px;
  color: #aaa;
  margin-top: 4px;
  display: block;
}

/* 自己的消息时间靠右 */
.own-bubble .time {
  text-align: right;
  color: #e0e0e0;
}

/* 他人的消息时间靠左 */
.content:not(.own-bubble) .time {
  text-align: left;
}
</style>    