import request from "@/utils/request";

export const queryAll = () => request.get("/messages/history");
export const  update = (userId,friendId) => request.post("/messages/update",{ userId, friendId });
export const updateProfile = (updateProfile) => request.post("/users/updateProfile",updateProfile);
