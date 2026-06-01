import request from "@/utils/request";

export function addFriends(userId, friendUsername) {
  return request.post("/friends/add", {userId,friendUsername});
}
export function Friendslist(id) {
  return request.get(`/friends/list/${id}`);
}
export function lookNewFriend(id) {
  return request.get(`/friends/require/${id}`);
}
export function newFriends(userId, friendId) {
  return request.post("/friends/approve", { userId, friendId });
}
export function removeFriends(userId, friendId) {
  return request.post("/friends/reject", { userId, friendId });
}
export function deleteFriend(userId, friendId) {
  return request.post("/friends/delete", { userId, friendId });
}