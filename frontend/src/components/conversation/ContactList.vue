<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Friend } from '@/types'
import { useChatStore } from '@/stores/chat'
import { addFriends } from '@/api/friends'
import { ElMessage } from 'element-plus'
import UserAvatar from '@/components/common/UserAvatar.vue'
import EmptyState from '@/components/common/EmptyState.vue'

const emit = defineEmits<{ (e: 'select', friend: Friend): void }>()

const chatStore = useChatStore()
const keyword = ref('')
const showAddDialog = ref(false)
const addUsername = ref('')
const adding = ref(false)

const filtered = computed(() => {
  const kw = keyword.value.trim().toLowerCase()
  if (!kw) return chatStore.friends
  return chatStore.friends.filter((f) =>
    `${f.nickname ?? ''} ${f.username ?? ''}`.toLowerCase().includes(kw)
  )
})

const onlineFriends = computed(() => filtered.value.filter((f) => f.status === 1))
const offlineFriends = computed(() => filtered.value.filter((f) => f.status !== 1))

async function submitAdd(): Promise<void> {
  const username = addUsername.value.trim()
  if (!username) {
    ElMessage.warning('请输入用户名')
    return
  }
  adding.value = true
  try {
    const res = await addFriends(username)
    if (res.code === 200) {
      ElMessage.success(res.msg || '好友请求已发送')
      showAddDialog.value = false
      addUsername.value = ''
    } else {
      ElMessage.error(res.msg || '好友请求发送失败')
    }
  } catch (error) {
    console.error('发送好友请求失败', error)
    ElMessage.error('好友请求发送失败')
  } finally {
    adding.value = false
  }
}
</script>

<template>
  <div class="contact-list">
    <div class="contact-list__top">
      <el-input v-model="keyword" placeholder="搜索联系人" clearable>
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <div class="contact-list__actions">
        <el-button type="primary" @click="showAddDialog = true">
          <el-icon><Plus /></el-icon>
          添加联系人
        </el-button>
      </div>
    </div>

    <div class="contact-list__body">
      <template v-if="filtered.length">
        <div class="contact-list__group-title">在线（{{ onlineFriends.length }}）</div>
        <ul class="contact-list__items">
          <li
            v-for="friend in onlineFriends"
            :key="`on-${friend.id}`"
            class="contact-item"
            @click="emit('select', friend)"
          >
            <UserAvatar :src="friend.avatar" :name="friend.nickname" :size="40" show-status :status="1" />
            <div class="contact-item__main">
              <span class="contact-item__name lc-ellipsis">{{ friend.nickname || friend.username }}</span>
              <span class="contact-item__sub lc-ellipsis">@{{ friend.username }}</span>
            </div>
          </li>
        </ul>

        <div class="contact-list__group-title">离线（{{ offlineFriends.length }}）</div>
        <ul class="contact-list__items">
          <li
            v-for="friend in offlineFriends"
            :key="`off-${friend.id}`"
            class="contact-item"
            @click="emit('select', friend)"
          >
            <UserAvatar :src="friend.avatar" :name="friend.nickname" :size="40" show-status :status="0" />
            <div class="contact-item__main">
              <span class="contact-item__name lc-ellipsis">{{ friend.nickname || friend.username }}</span>
              <span class="contact-item__sub lc-ellipsis">@{{ friend.username }}</span>
            </div>
          </li>
        </ul>
      </template>

      <EmptyState
        v-else
        icon="User"
        :title="keyword ? '没有匹配的联系人' : '还没有好友'
        "
        description="点击上方按钮，按用户名添加好友"
      />
    </div>

    <!-- 添加联系人 -->
    <el-dialog v-model="showAddDialog" title="添加联系人" width="380px">
      <el-input
        v-model="addUsername"
        placeholder="请输入对方的用户名"
        clearable
        @keyup.enter="submitAdd"
      />
      <p class="contact-list__hint">对方同意后即出现在联系人列表中。</p>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" :loading="adding" @click="submitAdd">发送申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.contact-list {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
}

.contact-list__top {
  padding: 12px 12px 8px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.contact-list__actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.contact-list__body {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 0 8px 12px;
}

.contact-list__group-title {
  font-size: 12px;
  color: var(--lc-text-3);
  padding: 8px;
}

.contact-list__items {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 10px;
  border-radius: var(--lc-radius);
  cursor: pointer;
}

.contact-item:hover {
  background-color: var(--lc-surface-2);
}

.contact-item__main {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.contact-item__name {
  font-size: 14px;
  color: var(--lc-text);
}

.contact-item__sub {
  font-size: 12px;
  color: var(--lc-text-3);
}

.contact-list__hint {
  margin: 10px 0 0;
  font-size: 12px;
  color: var(--lc-text-3);
}
</style>
