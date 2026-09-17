import axios, { type AxiosRequestConfig } from 'axios'
import type { ApiResponse } from '@/types'
import router from '../router'
import { ElMessage } from 'element-plus'

//创建axios实例对象
const instance = axios.create({
  baseURL: '/api',
  timeout: 600000
})

//axios的响应 response 拦截器
instance.interceptors.response.use(
  (response) => { //成功回调
    // 后端统一返回 { code, message, data }，此处直接解包业务数据体
    return response.data
  },
  (error: any) => { //失败回调
    if(error.response.status == 401){ //全等
      //提示信息
      ElMessage.error('登录超时，请重新登录');
      //跳转到登录页面
      router.push('/login');
    }else {
      ElMessage.error('接口访问异常');
    } 
    return Promise.reject(error)
  }
)

//axios的请求 request 拦截器
instance.interceptors.request.use(
  (config) => {
    //获取token
    const loginUser = JSON.parse(localStorage.getItem('user') ?? 'null');
    if (loginUser && loginUser.token) {
      //设置请求头
      config.headers.token = loginUser.token
    }
    return config;
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 对外暴露的请求对象：
 * 响应拦截器已把 AxiosResponse 解包为后端业务体 { code, message, data }，
 * 因此这里借助 axios 泛型的第二个参数，把返回值声明为 ApiResponse<T>。
 * 调用方式与原来的 axios 实例保持一致（request.get / post / put / delete / patch）。
 */
const request = {
  get<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.get<any, ApiResponse<T>>(url, config)
  },
  post<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.post<any, ApiResponse<T>>(url, data, config)
  },
  put<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.put<any, ApiResponse<T>>(url, data, config)
  },
  delete<T = any>(url: string, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.delete<any, ApiResponse<T>>(url, config)
  },
  patch<T = any>(url: string, data?: any, config?: AxiosRequestConfig): Promise<ApiResponse<T>> {
    return instance.patch<any, ApiResponse<T>>(url, data, config)
  }
}

export default request