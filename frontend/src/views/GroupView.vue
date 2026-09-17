<script setup lang="ts">
/** 群组页（中列）：我的群聊概览、创建群聊、进入群聊 */
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { Conversation } from '@/types'
import { useChatStore } from '@/stores/chat'
import UserAvatar from '@/components/common/UserAvatar.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import CreateGroupDialog from '@/components/conversation/CreateGroupDialog.vue'
import { formatDate } from '@/utils/time'

const router = useRouter()
const chatStore = useChatStore()
const showCreateDialog = ref(false)

const groups = computed(() => chatStore.groupConversations)
const ownedCount = computed(
  () => groups.value.filter((g) => g.ownerId === chatStore.currentUser.id).length
)

function enterGroup(group: Conversation): void {
  router.push(`/chat/group/${group.conversationId}`)
}

function handleCreated(conversationId: number): void {
  router.push(`/chat/group/${conversationId}`)
}
</script>

<template>
  <div class="page">
    <header class="page__header">
      <div>
        <h2 class="page__title">群组</h2>
        <p class="page__desc">创建群聊、邀请好友，一起讨论</p>
      </div>
      <el-button type="primary" @click="showCreateDialog = true">
        <el-icon><Plus /></el-icon>
        创建群聊
      </el-button>
    </header>

    <section class="stat-row">
      <div class="stat-card">
        <span class="stat-card__label">我参与的群聊</span>
        <span class="stat-card__value">{{ groups.length }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-card__label">我创建的群聊</span>
        <span class="stat-card__value">{{ ownedCount }}</span>
      </div>
    </section>

    <section class="page__section">
      <div class="page__section-title">全部群聊</div>
      <ul v-if="groups.length" class="group-grid">
        <li v-for="group in groups" :key="group.conversationId" class="group-card">
          <div class="group-card__head" @click="enterGroup(group)">
            <UserAvatar :name="group.name" :size="48" shape="square" />
            <div class="group-card__text">
              <span class="group-card__name lc-ellipsis">{{ group.name || '未命名群聊' }}</span>
              <span class="group-card__sub">
                {{ group.memberCount ?? 0 }} 名成员 · 创建于 {{ formatDate(group.createTime) }}
              </span>
            </div>
          </div>
          <div class="group-card__foot">
            <el-button size="small" type="primary" plain @click="enterGroup(group)">
              进入群聊
            </el-button>
            <span v-if="group.ownerId === chatStore.currentUser.id" class="group-card__tag">我是群主</span>
          </div>
        </li>
      </ul>

      <EmptyState
        v-else
        icon="UserFilled"
        title="还没有群聊"
        description="创建一个群，把好友拉进来一起聊"
      />
    </section>

    <CreateGroupDialog v-model="showCreateDialog" @created="handleCreated" />
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
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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

.page__section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 10px;
}

.group-grid {
  list-style: none;
  margin: 0;
  padding: 0;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.group-card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background-color: var(--lc-surface);
  border: 1px solid var(--lc-border);
  border-radius: var(--lc-radius);
}

.group-card__head {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
}

.group-card__text {
  display: flex;
  flex-direction: column;
  min-width: 0;
  gap: 4px;
}

.group-card__name {
  font-size: 15px;
  font-weight: 600;
}

.group-card__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.group-card__foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--lc-border);
}

.group-card__tag {
  font-size: 12px;
  color: var(--lc-primary);
}
</style>
