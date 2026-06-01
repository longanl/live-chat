import request from "@/utils/request";

export const loginApi = (data) => request.post("/users/login",data);
export const registerApi = (data) => request.post("/users/register",data);
export const modifyPassword = (data) => request.put("/users/modifyPassword",data);
export const logoutApi = (id) => request.get(`/users/logout/${id}`);