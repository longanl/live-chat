// WebSocket 连接由原始方案迁移为 STOMP over WebSocket（Spring @EnableWebSocketMessageBroker）后，
// 本 Store 保留统一公开 API（initWebSocket / seedMessage / closeWebSocket / states）。
// 调用方：main.ts（冷启动）、router/index.ts（路由守卫兜底）、views/auth/LoginView.vue（登录成功）。
import { defineStore } from 'pinia';
import { useChatStore } from '@/stores/chat';
import { ref, computed } from 'vue';
import { ElMessage } from 'element-plus';
import { Client, type IMessage } from '@stomp/stompjs';
import type { ChatMessage, OnlineUser, UserInfo } from '@/types';

/** WebSocket 连接状态 */
type ConnectionStatus = 'disconnected' | 'connecting' | 'connected' | 'error';

export const useWebSocketStore = defineStore('websocket', () => {
  const chatStore = useChatStore();
  const onlineUserList = ref<OnlineUser[]>([]);
  const onlineCount = ref(0);
  const connectionStatus = ref<ConnectionStatus>('disconnected');
  const errorMessage = ref('');
  const reconnectCount = ref(0);

  let stompClient: Client | null = null;
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  let manualClose = false;

  const MAX_RECONNECT_ATTEMPTS = 5;

  /** 当前会话的登录用户（含 JWT token） */
  const currentUser = (): UserInfo =>
    JSON.parse(localStorage.getItem('user') ?? 'null') || {};

  /**
   * 聊天消息统一入口：群聊与私聊都交给 chat store 落库、按 id 去重、累加未读。
   * 私聊的「对方」由服务端会话列表给出，前端不再依赖内存中的选中联系人
   * （旧的 selectedContact 方案在刷新后即丢失会话上下文）。
   */
  const handleChatMessage = (msg: ChatMessage): void => {
    chatStore.applyMessage(msg);
  };

  const parseBody = (payload: string): any => JSON.parse(payload);

  const initWebSocket = (userId: number, isReconnect = false): void => {
    // 已在建连 / 已连接时直接返回：main.ts 冷启动、路由守卫与登录成功会先后调用本方法，
    // 若不做去重会覆盖 stompClient，遗留的旧连接关闭后触发多余重连（约 3s）与重复请求。
    if (connectionStatus.value === 'connecting' || connectionStatus.value === 'connected') {
      return;
    }
    const user = currentUser();
    if (!userId) {
      connectionStatus.value = 'error';
      errorMessage.value = '用户ID不能为空';
      return;
    }
    if (!user.token) {
      connectionStatus.value = 'error';
      errorMessage.value = '缺少登录凭证，请重新登录';
      return;
    }

    if (stompClient && stompClient.connected) {
      closeWebSocket();
    }

    connectionStatus.value = 'connecting';
    if (!isReconnect) {
      reconnectCount.value = 0;
    }
    manualClose = false;

    try {
      const wsBase = (import.meta.env.VITE_WS_URL as string | undefined)?.replace(/\/$/, '');
      const origin =
        wsBase && wsBase.length > 0
          ? wsBase
          : `${window.location.protocol === 'https:' ? 'wss' : 'ws'}://${window.location.host}`;

      stompClient = new Client({
        brokerURL: `${origin}/ws`,
        connectHeaders: { token: user.token },
        reconnectDelay: 0,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
        onConnect: () => {
          connectionStatus.value = 'connected';
          errorMessage.value = '';
          console.log(`STOMP 连接成功 userId=${userId}`);

          // 断线期间的推送不会重放：重连成功后主动补拉未读与当前会话最新消息
          if (isReconnect) {
            void chatStore.syncAfterReconnect();
          }

          // 群聊：通配订阅所有会话主题（SimpleBroker 支持 Ant 风格通配），多群天然兼容
          stompClient?.subscribe('/topic/conv/*', (frame: IMessage) => {
            handleChatMessage(parseBody(frame.body));
          });
          // 私聊：STOMP 会解析 /user/queue 定向路由到当前会话
          stompClient?.subscribe('/user/queue/messages', (frame: IMessage) => {
            handleChatMessage(parseBody(frame.body));
          });
          // 在线用户列表 / 在线人数广播
          stompClient?.subscribe('/topic/online/users', (frame: IMessage) => {
            onlineUserList.value = parseBody(frame.body);
          });
          stompClient?.subscribe('/topic/online/count', (frame: IMessage) => {
            onlineCount.value = parseBody(frame.body);
          });
          // 初始在线快照（请求-响应，避免订阅竞态丢首帧）
          stompClient?.subscribe('/user/queue/presence', (frame: IMessage) => {
            const snapshot = parseBody(frame.body);
            if (snapshot) {
              if (Array.isArray(snapshot.users)) onlineUserList.value = snapshot.users;
              if (typeof snapshot.count === 'number') onlineCount.value = snapshot.count;
            }
          });
          // 服务端业务异常定向提示（如非好友私聊），不断开连接
          stompClient?.subscribe('/user/queue/errors', (frame: IMessage) => {
            const payload = parseBody(frame.body);
            if (payload?.message) ElMessage.error(payload.message);
          });
          stompClient?.publish({
            destination: '/app/presence.snapshot',
            body: '{}',
            headers: { 'content-type': 'application/json' }
          });
        },
        onWebSocketClose: () => {
          connectionStatus.value = 'disconnected';
          if (manualClose) {
            manualClose = false;
            return;
          }
          if (reconnectCount.value < MAX_RECONNECT_ATTEMPTS) {
            reconnectCount.value++;
            console.log(`尝试重连 (${reconnectCount.value}/${MAX_RECONNECT_ATTEMPTS})`);
            reconnectTimer = setTimeout(() => {
              initWebSocket(userId, true);
            }, 3000);
          } else {
            errorMessage.value = '重连失败，已达到最大尝试次数';
            ElMessage.error('连接已断开，请刷新页面');
          }
        },
        onStompError: (frame) => {
          // 鉴权失败等服务端错误帧：停止重连，避免循环无效重试
          connectionStatus.value = 'error';
          errorMessage.value = frame.headers?.message || 'WebSocket 连接失败，请重新登录';
          reconnectCount.value = MAX_RECONNECT_ATTEMPTS;
          console.error('STOMP 错误帧', frame.headers?.message);
        },
        onWebSocketError: (event) => {
          connectionStatus.value = 'error';
          errorMessage.value = `连接错误: ${(event as unknown as ErrorEvent).message}`;
          console.error('WebSocket 错误', event);
        }
      });
      stompClient.activate();
    } catch (error) {
      connectionStatus.value = 'error';
      errorMessage.value = `初始化失败: ${(error as Error).message}`;
      console.error('WebSocket 初始化错误', error);
    }
  };

  /**
   * 发送一条聊天消息。
   * @param dataType 'group' | 'p2pchat'（映射到 STOMP 目的地）
   * @param message 消息体，由后端 JWT Principal 决定发送者；
   *                群聊需带 conversationId（后端校验群成员资格后广播到 /topic/conv/{id}），
   *                私聊需带 receiverId（后端懒建/复用私聊会话）。
   */
  const seedMessage = (dataType: string, message: any): boolean => {
    if (stompClient && stompClient.connected) {
      const destination =
        dataType === 'group' ? '/app/chat.group' : '/app/chat.p2p';
      stompClient.publish({
        destination,
        body: JSON.stringify(message),
        headers: { 'content-type': 'application/json' }
      });
      return true;
    }
    console.warn('WebSocket 未连接，无法发送消息');
    return false;
  };

  const closeWebSocket = (): void => {
    if (reconnectTimer) {
      clearTimeout(reconnectTimer);
      reconnectTimer = null;
    }
    if (stompClient) {
      manualClose = true;
      console.log('WebSocket 关闭连接');
      void stompClient.deactivate();
      stompClient = null;
    }
    connectionStatus.value = 'disconnected';
    reconnectCount.value = 0;
  };

  /** 当前在线用户列表（含自己） */
  const isConnected = computed(() => connectionStatus.value === 'connected');

  return {
    onlineUserList,
    connectionStatus,
    errorMessage,
    reconnectCount,
    isConnected,
    initWebSocket,
    seedMessage,
    closeWebSocket,
    onlineCount
  };
});