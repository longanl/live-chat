<script setup lang="ts">
/** 群聊信息栏：群资料 + 成员列表 + 退出群聊 */
import { computed, ref } from 'vue'
import type { Conversation, Friend, PageResult } from '@/types'
import { conversationTitle } from '@/utils/conversation'
import { formatDate } from '@/utils/time'
import UserAvatar from '@/components/common/UserAvatar.vue'

const props = withDefaults(
  defineProps<{
    conversation?: Conversation | null
    members?: PageResult<Friend>
    loading?: boolean
    /** 当前登录用户ID，用于群主标识与「我」标记 */
    selfId?: number
  }>(),
  {
    conversation: null,
    members: () => ({ total: 0, page: 1, size: 50, list: [] }),
    loading: false,
    selfId: undefined
  }
)

const emit = defineEmits<{ (e: 'quit'): void; (e: 'invite'): void }>()

const memberPage = ref(1)
const memberSize = ref(50)

const memberList = computed(() => props.members?.list ?? [])
const totalMembers = computed(() => props.members?.total ?? 0)

const title = computed(() => conversationTitle(props.conversation))
const ownerId = computed(() => props.conversation?.ownerId ?? null)
const onlineMembers = computed(() => memberList.value.filter((m) => m.status === 1))
const isOwner = computed(() => !!ownerId.value && ownerId.value === props.selfId)

/** 群主展示名：优先成员列表中的昵称 */
const ownerName = computed(() => {
  if (!ownerId.value) return '—'
  if (ownerId.value === props.selfId) return '我'
  const owner = memberList.value.find((m) => m.id === ownerId.value)
  return owner?.nickname || owner?.username || `用户 ${ownerId.value}`
})

function onMemberPageChange(page: number, size: number): void {
  memberPage.value = page
  memberSize.value = size
}
</script>

<template>
  <div class="info-panel">
    <div class="info-panel__profile">
      <UserAvatar :name="title" :size="64" shape="square" />
      <div class="info-panel__name">{{ title }}</div>
      <div class="info-panel__sub">群聊</div>
    </div>

    <div class="info-panel__grid">
      <div class="info-panel__cell">
        <span class="info-panel__label">群主</span>
        <span class="info-panel__value">
          {{ ownerName }}
        </span>
      </div>
      <div class="info-panel__cell">
        <span class="info-panel__label">创建时间</span>
        <span class="info-panel__value">{{ formatDate(conversation?.createTime) || '—' }}</span>
      </div>
      <div class="info-panel__cell">
        <span class="info-panel__label">成员数量</span>
        <span class="info-panel__value">
          {{ totalMembers }} 人
        </span>
      </div>
      <div class="info-panel__cell">
        <span class="info-panel__label">在线成员</span>
        <span class="info-panel__value">{{ onlineMembers.length }} 人</span>
      </div>
    </div>

    <div class="info-panel__actions">
      <el-button class="info-panel__btn" @click="emit('invite')">
        <el-icon><Plus /></el-icon>
        邀请成员
      </el-button>
      <el-button
        class="info-panel__btn"
        :disabled="isOwner"
        :title="isOwner ? '群主不能退出群聊' : ''"
        @click="emit('quit')"
      >
        <el-icon><SwitchButton /></el-icon>
        退出群聊
      </el-button>
    </div>

      <div class="info-panel__section">
        <div class="info-panel__section-title">群成员（{{ totalMembers }}）</div>
        <div v-if="loading" class="info-panel__hint">加载中…</div>
        <ul v-else class="info-panel__list">
          <li v-for="member in memberList" :key="member.id" class="info-panel__member">
            <UserAvatar
              :src="member.avatar"
              :name="member.nickname"
              :size="32"
              show-status
              :status="member.status"
            />
            <div class="info-panel__member-text">
              <span class="info-panel__member-name lc-ellipsis">
                {{ member.nickname || member.username }}
                <em v-if="member.id === selfId" class="info-panel__tag">我</em>
              </span>
              <span class="info-panel__member-sub lc-ellipsis">
                {{ member.status === 1 ? '在线' : '离线' }}
              </span>
            </div>
          </li>
        </ul>
        <div v-if="!loading && memberList.length" class="info-panel__foot">
          <el-pagination
            v-model:current-page="memberPage"
            v-model:page-size="memberSize"
            :total="totalMembers"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next"
            small
            @current-change="(p) => { memberPage = p }"
            @size-change="(s) => { memberSize = s }"
          />
        </div>
      </div>
        </li>
      </ul>
    </div>
  </div>
</template>

<style scoped>
.info-panel {
  height: 100%;
  min-height: 0;
  overflow-y: auto;
  padding: 20px 16px;
}

.info-panel__profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--lc-border);
}

.info-panel__name {
  font-size: 16px;
  font-weight: 600;
  text-align: center;
  word-break: break-all;
}

.info-panel__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.info-panel__grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
  padding: 14px 0;
}

.info-panel__cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 8px 10px;
  background-color: var(--lc-surface-2);
  border-radius: var(--lc-radius-sm);
}

.info-panel__label {
  font-size: 12px;
  color: var(--lc-text-3);
}

.info-panel__value {
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-text);
}

.info-panel__actions {
  display: flex;
  gap: 8px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--lc-border);
}

.info-panel__btn {
  flex: 1;
  margin: 0;
}

.info-panel__section {
  padding-top: 14px;
}

.info-panel__section-title {
  font-size: 13px;
  font-weight: 600;
  color: var(--lc-text-2);
  margin-bottom: 10px;
}

.info-panel__hint {
  font-size: 12px;
  color: var(--lc-text-3);
}

.info-panel__list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-panel__foot {
  padding: 8px 0 0;
}

.info-panel__member {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 4px;
  border-radius: var(--lc-radius-sm);
}

.info-panel__member:hover {
  background-color: var(--lc-surface-2);
}

.info-panel__member-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.info-panel__member-name {
  font-size: 13px;
  color: var(--lc-text);
}

.info-panel__member-sub {
  font-size: 11px;
  color: var(--lc-text-3);
}

.info-panel__tag {
  font-style: normal;
  font-size: 10px;
  color: var(--lc-primary);
  border: 1px solid var(--lc-border-strong);
  border-radius: 4px;
  padding: 0 3px;
  margin-left: 4px;
}
</style>