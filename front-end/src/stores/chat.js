import { defineStore } from 'pinia';
import { queryAll } from '@/api/index';
import { Friendslist,deleteFriend } from '@/api/friends';
import { update } from '@/api/index';
import { ref } from 'vue';
import { ElMessage } from 'element-plus'

export const useChatStore = defineStore('chat', () => {
  // 状态定义
  const history = ref([]);
  const filteredContacts = ref([]);
  const currentUser = ref(JSON.parse(localStorage.getItem('user')) || {});
  const errorMessage = ref(''); // 新增：错误信息
  // 异步搜索方法
  const search = async () => {
    errorMessage.value = ''; //清空错误信息
    try {
      const res = await queryAll();
      if (res.code === 200) {
        history.value = res.data;
      } else {
        errorMessage.value = res.message || '请求失败';
        console.error('API错误:', errorMessage.value);
      }
    } catch (error) {
      errorMessage.value = error.message || '网络错误';
      console.error('网络错误:', errorMessage.value);
    }
  }
  const getFriends = async () => {
    errorMessage.value = ''; //清空错误信息
    try {
      const res = await Friendslist(currentUser.value.id);
      if (res.code === 200) {
        filteredContacts.value = res.data;
      } else {
        errorMessage.value = res.message || '请求失败';
        console.error('API错误:', errorMessage.value);
      }
    } catch (error) {
      errorMessage.value = error.message || '网络错误';
      console.error('网络错误:', errorMessage.value);
    }
  }
  const deletePerson = async (userId, friendId) => {
    try{
    const res= await deleteFriend(userId, friendId);
    if(res.code === 200){
      ElMessage.success('删除成功')
      getFriends()
    }else{
      ElMessage.error('删除失败')
    }
    }catch(err){
      ElMessage.error('删除失败')
    }
  }
  const getUnreadCount =  (friendId) => {
    let count = 0
    for(let i = 0; i < history.value.length; i++){
      if(history.value[i].senderId === friendId && history.value[i].receiverId === currentUser.value.id && history.value[i].readStatus === 0){
        count++
      }
    }
    return count
  }
  const updateHistory = async(userId, friendId) =>{
    try{
    const res = await update(userId, friendId);
    if(res.code === 200){
      search()
      console.log('更新成功')
    }else{
    }
    }catch(err){
      console.log(err)
    }
  }

  return {
    history,
    currentUser,
    search,
    errorMessage,
    filteredContacts,
    getFriends,
    deletePerson,
    updateHistory,
    getUnreadCount
  }
});