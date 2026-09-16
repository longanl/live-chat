import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getHistory, getUnread, markRead } from '@/api/messages';
import { Friendslist, deleteFriend, lookNewFriend } from '@/api/friends';
import {
  addGroupMembers,
  createGroup,
  getConversations,
  getConversationMembers,
  quitGroup
} from '@/api/conversations';
import { ElMessage } from 'element-plus';
import type { ChatMessage, Conversation, Friend, UnreadStat, UserInfo } from '@/types';
import { HISTORY_PAGE_SIZE } from '@/utils/constants';
import { CONVERSATION_TYPE_GROUP, CONVERSATION_TYPE_P2P } from '@/utils/conversation';

/**
 * 聊天核心状态：
 * - conversations：我的会话列表（群聊 + 已建立的私聊），权威数据来自 /conversations；
 * - historyByConv：按会话组织的消息，游标回溯分页；
 * - unreadMap：未读数，本地实时累加并与服务端校准。
 */
export const useChatStore = defineStore('chat', () => {
  // ==================== 状态 ====================
  /** 按会话组织的历史消息 */
  const historyByConv = ref<Record<number, ChatMessage[]>>({});
  /** 会话 -> 未读数（本地实时累加，服务端拉取后校准） */
  const unreadMap = ref<Record<number, number>>({});
  /** 会话是否已加载到底（没有更早的消息） */
  const loadedAll = ref<Record<number, boolean>>({});
  /** 当前正在查看的会话 */
  const activeConversationId = ref<number | null>(null);
  /** 我的会话列表（群聊 + 已建立的私聊） */
  const conversations = ref<Conversation[]>([]);
  /** 好友列表（联系人页使用） */
  const friends = ref<Friend[]>([]);
  /** 好友列表是否已加载完成（用于区分「尚未加载」与「确实非好友」） */
  const friendsLoaded = ref(false);
  /** 待处理的好友申请（顶栏红点 / 联系人页共用） */
  const pendingRequests = ref<Friend[]>([]);
  const currentUser = ref<UserInfo>(JSON.parse(localStorage.getItem('user') ?? 'null') || {});
  const errorMessage = ref('');

  // ==================== 查询辅助 ====================
  /** 取某会话的消息列表 */
  const messagesOf = (conversationId?: number | null): ChatMessage[] =>
    conversationId ? historyByConv.value[conversationId] ?? [] : [];

  /** 会话是否已加载到底 */
  const hasLoadedAll = (conversationId?: number | null): boolean =>
    !!conversationId && !!loadedAll.value[conversationId];

  /** 由会话ID取会话 */
  const conversationOf = (conversationId?: number | null): Conversation | null =>
    conversationId
      ? conversations.value.find((c) => c.conversationId === conversationId) ?? null
      : null;

  /** 由对方用户ID取私聊会话（尚未聊过时为 null，首次发言由后端懒建会话） */
  const conversationByPeer = (peerId?: number | null): Conversation | null =>
    peerId
      ? conversations.value.find(
          (c) => c.type === CONVERSATION_TYPE_P2P && c.peerId === peerId
        ) ?? null
      : null;

  /** 某会话未读数：本地累加值优先，缺省回落服务端列表值 */
  const unreadOf = (conversationId?: number | null): number => {
    if (!conversationId) return 0;
    const local = unreadMap.value[conversationId];
    if (local !== undefined) return local;
    return conversationOf(conversationId)?.unreadCount ?? 0;
  };

  // ==================== 派生数据 ====================
  /** 群聊会话（含内置群） */
  const groupConversations = computed(() =>
    conversations.value.filter((c) => c.type === CONVERSATION_TYPE_GROUP)
  );
  /** 私聊会话（已有消息往来） */
  const p2pConversations = computed(() =>
    conversations.value.filter((c) => c.type === CONVERSATION_TYPE_P2P)
  );
  /** 当前查看的会话对象 */
  const activeConversation = computed(() => conversationOf(activeConversationId.value));

  // ==================== 会话列表 ====================
  /**
   * 拉取我的会话列表，并用服务端未读数校准本地累加值。
   * 被邀请入群、首次私聊建立会话后都以此接口对齐。
   */
  const loadConversations = async (): Promise<void> => {
    if (!currentUser.value.id) return;
    try {
      const res = await getConversations();
      if (res.code === 200 && res.data) {
        conversations.value = res.data;
        const map: Record<number, number> = {};
        res.data.forEach((c) => {
          map[c.conversationId] = c.unreadCount ?? 0;
        });
        unreadMap.value = map;
      } else {
        errorMessage.value = res.msg || '加载会话列表失败';
      }
    } catch (error) {
      errorMessage.value = (error as Error).message || '网络异常';
      console.error('加载会话列表失败', error);
    }
  };

  // ==================== 历史消息 ====================
  /**
   * 加载会话历史；prepend=true 时在顶部追加更早的消息。
   *
   * 是否加载到底只依据返回条数：不足一页（含 0 条）即认为没有更早的消息，
   * 避免空会话在反复上滚时重复请求。
   */
  const loadConversation = async (conversationId: number, prepend = false): Promise<void> => {
    if (!conversationId) return;
    const limit = HISTORY_PAGE_SIZE;
    const list = historyByConv.value[conversationId] ?? [];
    const beforeId = prepend ? list.find((m) => typeof m.id === 'number')?.id : undefined;
    try {
      const res = await getHistory(conversationId, beforeId, limit);
      if (res.code === 200 && res.data) {
        const exist = new Set(list.map((m) => m.id));
        const fresh = res.data.filter((m) => !exist.has(m.id));
        historyByConv.value = {
          ...historyByConv.value,
          // 仅上滚回溯需要按 id 去重后前插；加载最新一页直接用服务端结果覆盖。
          // 覆盖分支若也去重，缓存已有同一页时会被过滤成空数组，来回切换后消息会"消失"。
          [conversationId]: beforeId ? [...fresh, ...list] : res.data
        };
        // 是否触底只由本页条数决定：满页即仍有更早消息，避免会话消息增长后误判为已加载完
        loadedAll.value = { ...loadedAll.value, [conversationId]: res.data.length < limit };
      } else {
        errorMessage.value = res.msg || '加载历史失败';
      }
    } catch (error) {
      errorMessage.value = (error as Error).message || '网络异常';
      console.error('加载历史失败', error);
      ElMessage.error('加载历史消息失败，请检查网络');
    }
  };

  // ==================== 未读 / 已读 ====================
  /** 拉取服务端未读数（断线重连补偿用） */
  const refreshUnread = async (): Promise<void> => {
    if (!currentUser.value.id) return;
    try {
      const res = await getUnread();
      if (res.code === 200 && res.data) {
        const map: Record<number, number> = {};
        res.data.forEach((u: UnreadStat) => {
          if (u.conversationId) map[u.conversationId] = u.count ?? 0;
        });
        unreadMap.value = map;
      }
    } catch (error) {
      console.error('获取未读数失败', error);
    }
  };

  /**
   * 断线重连后的补偿：
   * 1) 重新拉取服务端未读数（断线期间的推送已丢失，只能靠服务端对齐）；
   * 2) 重新加载当前查看会话的最新一页，由 loadConversation 内部按 id 去重补齐；
   * 3) 会话列表（最后一条消息）同样重新对齐。
   */
  const syncAfterReconnect = async (): Promise<void> => {
    await Promise.all([refreshUnread(), loadConversations()]);
    const convId = activeConversationId.value;
    if (convId) await loadConversation(convId);
  };

  /** 标记会话已读（本地先归零，再推进服务端游标） */
  const markConversationRead = async (conversationId?: number | null): Promise<void> => {
    if (!conversationId) return;
    unreadMap.value = { ...unreadMap.value, [conversationId]: 0 };
    const conv = conversationOf(conversationId);
    if (conv) conv.unreadCount = 0;
    try {
      await markRead(conversationId);
    } catch (error) {
      console.error('标记已读失败', error);
    }
  };

  /** 设置当前查看的会话 */
  const setActiveConversation = (conversationId?: number | null): void => {
    activeConversationId.value = conversationId ?? null;
  };

  // ==================== 消息接收 ====================
  /**
   * 收到一条消息（WebSocket 推送）：
   * 1) 与历史分页结果按 id 去重后入列；
   * 2) 非本人且非当前查看会话时累加未读；
   * 3) 同步会话列表的最后一条消息并置顶；本地尚无该会话（如刚被邀请入群）时补拉会话列表。
   */
  const applyMessage = (msg: ChatMessage): void => {
    if (!msg.conversationId) return;
    const convId = msg.conversationId;
    const list = historyByConv.value[convId] ?? [];
    if (msg.id && list.some((m) => m.id === msg.id)) return;
    historyByConv.value = { ...historyByConv.value, [convId]: [...list, msg] };

    const mine = msg.senderId === currentUser.value.id;
    if (!mine && activeConversationId.value !== convId) {
      unreadMap.value = { ...unreadMap.value, [convId]: (unreadMap.value[convId] ?? 0) + 1 };
    }

    const index = conversations.value.findIndex((c) => c.conversationId === convId);
    if (index === -1) {
      void loadConversations();
      return;
    }
    const [conv] = conversations.value.splice(index, 1);
    conv.lastMessageId = msg.id ?? conv.lastMessageId;
    conv.lastMessageType = msg.messageType ?? 1;
    conv.lastContent = msg.content ?? '';
    conv.lastFileName = msg.fileName ?? null;
    conv.lastSenderNickname = mine ? currentUser.value.nickname : msg.nickname;
    conv.lastSendTime = msg.sendTime ?? new Date().toISOString();
    conversations.value.unshift(conv);
  };

  // ==================== 群组操作 ====================
  /** 创建群聊：成功后刷新会话列表，返回新会话ID */
  const createNewGroup = async (
    name: string,
    memberIds: number[] = []
  ): Promise<number | null> => {
    try {
      const res = await createGroup(name, memberIds);
      if (res.code === 200 && res.data) {
        ElMessage.success('群聊创建成功');
        await loadConversations();
        return res.data;
      }
      ElMessage.error(res.msg || '创建群聊失败');
      return null;
    } catch (error) {
      console.error('创建群聊失败', error);
      ElMessage.error('创建群聊失败');
      return null;
    }
  };

  /** 邀请成员入群 */
  const inviteMembers = async (
    conversationId: number,
    userIds: number[]
  ): Promise<boolean> => {
    try {
      const res = await addGroupMembers(conversationId, userIds);
      if (res.code === 200) {
        ElMessage.success('邀请成功');
        await loadConversations();
        return true;
      }
      ElMessage.error(res.msg || '邀请失败');
      return false;
    } catch (error) {
      console.error('邀请成员失败', error);
      ElMessage.error('邀请失败');
      return false;
    }
  };

  /** 退出群聊 */
  const quitGroupConversation = async (conversationId: number): Promise<boolean> => {
    try {
      const res = await quitGroup(conversationId);
      if (res.code === 200) {
        ElMessage.success('已退出群聊');
        await loadConversations();
        return true;
      }
      ElMessage.error(res.msg || '退出失败');
      return false;
    } catch (error) {
      console.error('退出群聊失败', error);
      ElMessage.error('退出失败');
      return false;
    }
  };

  /** 会话成员列表（信息栏使用，不落入全局状态） */
  const loadMembers = async (conversationId: number): Promise<Friend[]> => {
    try {
      const res = await getConversationMembers(conversationId);
      if (res.code === 200 && res.data) return res.data;
    } catch (error) {
      console.error('加载会话成员失败', error);
    }
    return [];
  };

  // ==================== 好友 ====================
  /** 好友列表（含与该好友的私聊会话ID） */
  const getFriends = async (): Promise<void> => {
    if (!currentUser.value.id) return;
    errorMessage.value = '';
    try {
      const res = await Friendslist();
      if (res.code === 200 && res.data) {
        friends.value = res.data;
        friendsLoaded.value = true;
      } else {
        errorMessage.value = res.msg || '请求失败';
      }
    } catch (error) {
      errorMessage.value = (error as Error).message || '网络错误';
      console.error('加载好友列表失败', error);
    }
  };

  /** 删除好友 */
  const deletePerson = async (friendId: number): Promise<void> => {
    try {
      const res = await deleteFriend(friendId);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        await getFriends();
      } else {
        ElMessage.error(res.msg || '删除失败');
      }
    } catch (error) {
      console.error('删除好友失败', error);
      ElMessage.error('删除失败');
    }
  };

  /** 拉取待处理的好友申请（顶栏红点 / 联系人页共用） */
  const loadPendingRequests = async (): Promise<void> => {
    try {
      const res = await lookNewFriend();
      if (res.code === 200 && res.data) {
        pendingRequests.value = res.data;
      }
    } catch (error) {
      console.error('加载好友申请失败', error);
    }
  };

  return {
    // 状态
    currentUser,
    errorMessage,
    friends,
    friendsLoaded,
    pendingRequests,
    conversations,
    historyByConv,
    unreadMap,
    loadedAll,
    activeConversationId,
    // 派生
    activeConversation,
    groupConversations,
    p2pConversations,
    // 查询
    messagesOf,
    hasLoadedAll,
    unreadOf,
    conversationOf,
    conversationByPeer,
    // 会话
    loadConversations,
    loadConversation,
    refreshUnread,
    syncAfterReconnect,
    markConversationRead,
    setActiveConversation,
    applyMessage,
    // 群组
    createNewGroup,
    inviteMembers,
    quitGroupConversation,
    loadMembers,
    // 好友
    getFriends,
    loadPendingRequests,
    deletePerson
  };
});
