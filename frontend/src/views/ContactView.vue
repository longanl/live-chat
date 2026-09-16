<script setup lang="ts">
/** 联系人页（中列）：好友概览、好友申请处理、快捷开始聊天 */
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Friend } from '@/types'
import { useChatStore } from '@/stores/chat'
import { newFriends, removeFriends } from '@/api/friends'
import UserAvatar from '@/components/common/UserAvatar.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { formatListTime } from '@/utils/time'

const router = useRouter()
const chatStore = useChatStore()
const loading = ref(false)

const onlineCount = computed(() => chatStore.friends.filter((f) => f.status === 1).length)

async function loadRequests(): Promise<void> {
  loading.value = true
  try {
    await chatStore.loadPendingRequests()
  } finally {
    loading.value = false
  }
}

async function approve(friendId: number): Promise<void> {
  try {
    const res = await newFriends(friendId)
    if (res.code === 200) {
      ElMessage.success('已添加为好友')
      await Promise.all([chatStore.getFriends(), loadRequests()])
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

async function reject(friendId: number): Promise<void> {
  try {
    const res = await removeFriends(friendId)
    if (res.code === 200) {
      ElMessage.warning('已拒绝该申请')
      await loadRequests()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

function startChat(friend: Friend): void {
  router.push(`/chat/p2p/${friend.id}`)
}

onMounted(() => {
  void loadRequests()
})
</script>

<template>
  <div class="page">
    <header class="page__header">
      <h2 class="page__title">联系人</h2>
      <p class="page__desc">管理好友关系，点击左侧联系人即可开始私聊</p>
    </header>

    <section class="stat-row">
      <div class="stat-card">
        <span class="stat-card__label">好友总数</span>
        <span class="stat-card__value">{{ chatStore.friends.length }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-card__label">在线好友</span>
        <span class="stat-card__value">{{ onlineCount }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-card__label">待处理申请</span>
        <span class="stat-card__value">{{ chatStore.pendingRequests.length }}</span>
      </div>
    </section>

    <section class="page__section">
      <div class="page__section-title">
        好友申请
        <span v-if="chatStore.pendingRequests.length" class="page__section-count">
          {{ chatStore.pendingRequests.length }}
        </span>
      </div>
      <div v-if="loading" class="page__hint">加载中…</div>
      <ul v-else-if="chatStore.pendingRequests.length" class="request-list">
        <li v-for="req in chatStore.pendingRequests" :key="req.id" class="request-item">
          <UserAvatar :src="req.avatar" :name="req.nickname" :size="40" />
          <div class="request-item__main">
            <span class="request-item__name lc-ellipsis">
              {{ req.nickname || req.username }}
            </span>
            <span class="request-item__sub">
              {{ formatListTime(req.createTime) }} 申请加你为好友
            </span>
          </div>
          <div class="request-item__actions">
            <el-button type="primary" size="small" @click="approve(req.id)">同意</el-button>
            <el-button size="small" @click="reject(req.id)">拒绝</el-button>
          </div>
        </li>
      </ul>
      <EmptyState
        v-else
        icon="Bell"
        title="暂无待处理的好友申请"
        description="新的申请会显示在这里"
      />
    </section>

    <section class="page__section">
      <div class="page__section-title">在线好友</div>
      <ul v-if="onlineCount" class="friend-grid">
        <li
          v-for="friend in chatStore.friends.filter((f) => f.status === 1)"
          :key="friend.id"
          class="friend-card"
          @click="startChat(friend)"
        >
          <UserAvatar :src="friend.avatar" :name="friend.nickname" :size="40" show-status :status="1" />
          <div class="friend-card__text">
            <span class="friend-card__name lc-ellipsis">
              {{ friend.nickname || friend.username }}
            </span>
            <span class="friend-card__sub">点击开始聊天</span>
          </div>
        </li>
      </ul>
      <EmptyState v-else icon="User" title="当前没有在线好友" description="邀请好友上线聊两句" />
    </section>
  </div>
</template>

<style scoped>
.page {
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 24px 32px;
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

.stat-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  flex-direction: column;
  gap: 6px;
  padding: 14px 16px;
  background-color: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.stat-card__label {
  font-size: 12px;
  color: var(--lc-text-3);
}

.stat-card__value {
  font-size: 22px;
  font-weight: 700;
  color: var(--lc-primary);
}

.page__section {
  margin-bottom: 22px;
}

.page__section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.page__section-count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  font-size: 12px;
  color: #fff;
  background-color: var(--lc-danger);
}

.page__hint {
  font-size: 12px;
  color: var(--lc-text-3);
}

.request-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.request-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 14px;
  background-color: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.request-item__main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.request-item__name {
  font-size: 14px;
}

.request-item__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.request-item__actions {
  display: flex;
  gap: 6px;
  flex-shrink: 0;
}

.friend-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 10px;
}

.friend-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  background-color: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
  cursor: pointer;
}

.friend-card:hover {
  border-color: var(--lc-primary);
}

.friend-card__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.friend-card__name {
  font-size: 14px;
}

.friend-card__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}
</style>
