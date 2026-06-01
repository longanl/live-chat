// stores/websocket.js
import { defineStore } from 'pinia';
import { useChatStore } from '@/stores/chat'; // 导入另一个 Store
import { ref, computed, onBeforeUnmount } from 'vue';


export const useWebSocketStore = defineStore('websocket', () => {
  // 依赖其他 Store（函数式编程：依赖注入）
  const chatStore = useChatStore();
  // 选中的联系人
  const selectedContact = ref(null);
  // 状态管理（使用函数式编程思想：不可变数据 + 纯函数）
  const onlineUserList = ref([]);        // 在线用户列表
  const onlineCount =ref(0);           // 在线人数
  const connectionStatus = ref('disconnected'); // 连接状态：disconnected, connecting, connected, error
  const errorMessage = ref('');       // 错误信息
  const reconnectCount = ref(0);      // 重连尝试次数
  
  let websocket = null;               // WebSocket 实例（非响应式）
  let reconnectTimer = null;          // 重连定时器
  
  // 常量定义（函数式编程中推荐使用不可变常量）
  const MAX_RECONNECT_ATTEMPTS = 5;   // 最大重连次数

  // 初始化 WebSocket 连接（纯函数：相同输入产生相同输出）
  const initWebSocket = (userId) => {
    // 输入验证
    if (!userId) {
      connectionStatus.value = 'error';
      errorMessage.value = '用户ID不能为空';
      return;
    }

    // 清理现有连接（函数组合：将多个操作组合成一个流程）
    if (websocket && websocket.readyState !== WebSocket.CLOSED) {
      closeWebSocket();
    }

    // 状态更新（使用纯函数更新状态）
    connectionStatus.value = 'connecting';
    reconnectCount.value = 0;
    
    try {
      // 创建新连接
      websocket = new WebSocket(`ws://localhost:8080/ws/${userId}`);
      
      // 事件处理函数（函数式编程：将行为封装为独立函数）
      websocket.onopen = () => {
        connectionStatus.value = 'connected';
        errorMessage.value = '';
        console.log('WebSocket 连接成功');
      };
      // 消息处理（使用函数式过滤和不可变数据原则）
      websocket.onmessage = (event) => {
        try {
          console.log('WebSocket 收到消息');
          const data = JSON.parse(event.data);
          // 消息分类处理（函数式分支逻辑）
          if (data.type === 'group' || data.type === 'p2pchat') {
            // 使用数组过滤实现消息去重（纯函数操作）
            // 直接添加到历史消息数组（不可变更新）
            chatStore.history = [...chatStore.history, data.content];
          } 
          else if (data.type === 'onlineUsers') {
            // 直接替换在线用户列表（纯函数更新）
            onlineUserList.value = data.content;
          }else if(data.type === 'onlineCount'){
            // 直接更新在线人数（纯函数更新）
            onlineCount.value = data.content;
          }
        } catch (parseError) {
          console.error('解析消息失败', parseError);
        }
      };
      
      // 错误处理（函数式编程：将错误视为一等公民）
      websocket.onerror = (error) => {
        connectionStatus.value = 'error';
        errorMessage.value = `连接错误: ${error.message}`;
        console.error('WebSocket 错误', error);
      };
      
      // 关闭处理（包含重试逻辑）
      websocket.onclose = (event) => {
        connectionStatus.value = 'disconnected';
        
        // 函数式条件判断：根据关闭代码决定是否重连
        if (event.code !== 1000) { // 非正常关闭
          if (reconnectCount.value < MAX_RECONNECT_ATTEMPTS) {
            // 函数组合：递增计数 + 设置定时器
            reconnectCount.value++;
            console.log(`尝试重连 (${reconnectCount.value}/${MAX_RECONNECT_ATTEMPTS})`);
            
            reconnectTimer = setTimeout(() => {
              initWebSocket(userId); // 递归调用（函数式编程常见模式）
            }, 3000);
          } else {
            errorMessage.value = '重连失败，已达到最大尝试次数';
            console.log(errorMessage.value);
          }
        }
      };
      
    } catch (error) {
      connectionStatus.value = 'error';
      errorMessage.value = `初始化失败: ${error.message}`;
      console.error('WebSocket 初始化错误', error);
    }
  };

  // 发送消息（纯函数：只负责发送，不处理状态）
  const seedMessage = (dataTpye,message) => {
    if (websocket && websocket.readyState === WebSocket.OPEN) {
      const data = {
        type: dataTpye,
        content: message
      };
      websocket.send(JSON.stringify(data)); 
      return true; // 返回操作结果（函数式编程）
    } else {
      console.warn('WebSocket 未连接，无法发送消息');
      return false;
    }
  };

  // 关闭连接（纯函数：清理资源）
  const closeWebSocket = () => {
    // 清理定时器（纯函数操作）
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    
    // 关闭连接（副作用操作）
    if (websocket) {
      websocket.onclose = null; // 防止重连回调
      console.log('WebSocket 关闭连接');
      websocket.close();
      websocket = null;
    }
    
    // 更新状态（纯函数）
    connectionStatus.value = 'disconnected';
    reconnectCount.value = 0;
  };

  // 生命周期管理（使用 Vue 组合式 API 的副作用钩子）
  onBeforeUnmount(() => {
    closeWebSocket(); // 组件卸载时清理资源（纯函数调用）
  });

  // 计算属性（函数式编程：纯计算逻辑）
  const isConnected = computed(() => connectionStatus.value === 'connected');
  
  // 使用数组过滤计算未读消息（纯函数操作）
  const unreadCount = computed(() => 
    messages.value.filter(msg => !msg.read).length
  );

  const selectContact = (contact) => {
    selectedContact.value = contact
  }

  // 返回 store 接口（函数式编程：明确暴露接口）
  return {
    onlineUserList,
    connectionStatus,
    errorMessage,
    reconnectCount,
    isConnected,
    unreadCount,
    initWebSocket,
    seedMessage,
    closeWebSocket,
    onlineCount,
    selectedContact,
    selectContact
  };
});