<script setup lang="ts">
/**
 * 创建群聊弹窗：输入群名称 + 勾选好友（可空，仅自己也能建群）。
 * 成功后把新会话ID抛给父级，由父级决定跳转。
 */
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { useChatStore } from '@/stores/chat'

const props = withDefaults(defineProps<{ modelValue?: boolean }>(), { modelValue: false })

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'created', conversationId: number): void
}>()

const chatStore = useChatStore()
const name = ref('')
const selected = ref<number[]>([])
const creating = ref(false)

const visible = computed({
  get: () => props.modelValue,
  set: (value: boolean) => emit('update:modelValue', value)
})

const options = computed(() =>
  chatStore.friends.list.map((friend) => ({
    value: friend.id,
    label: friend.nickname || friend.username || `用户${friend.id}`
  }))
)

// 每次打开重置表单，并确保好友列表已就绪
watch(visible, (open) => {
  if (!open) return
  name.value = ''
  selected.value = []
  if (!chatStore.friends.list.length) void chatStore.getFriends()
})

async function submit(): Promise<void> {
  const groupName = name.value.trim()
  if (!groupName) {
    ElMessage.warning('请输入群名称')
    return
  }
  creating.value = true
  try {
    const conversationId = await chatStore.createNewGroup(groupName, selected.value)
    if (conversationId) {
      emit('created', conversationId)
      visible.value = false
    }
  } finally {
    creating.value = false
  }
}
</script>

<template>
  <el-dialog v-model="visible" title="创建群聊" width="440px">
    <el-form label-width="72px" @submit.prevent>
      <el-form-item label="群名称">
        <el-input
          v-model="name"
          maxlength="100"
          show-word-limit
          placeholder="请输入群名称"
          clearable
          @keyup.enter="submit"
        />
      </el-form-item>
      <el-form-item label="邀请好友">
        <el-select
          v-model="selected"
          multiple
          filterable
          collapse-tags
          collapse-tags-tooltip
          placeholder="选择要拉进群的好友（可稍后再邀请）"
          class="create-group__select"
        >
          <el-option
            v-for="option in options"
            :key="option.value"
            :label="option.label"
            :value="option.value"
          />
        </el-select>
        <p v-if="!options.length" class="create-group__hint">
          暂无好友可选，可先创建群聊，之后再从群里邀请。
        </p>
      </el-form-item>
    </el-form>

    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="creating" @click="submit">创建</el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.create-group__select {
  width: 100%;
}

.create-group__hint {
  margin: 6px 0 0;
  font-size: 12px;
  color: var(--lc-text-3);
  line-height: 1.5;
}
</style>
