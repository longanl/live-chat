import axios from 'axios'
import router from '../router'
import { ElMessage } from 'element-plus'

//创建axios实例对象
const request = axios.create({
  baseURL: '/api',
  timeout: 600000
})

//axios的响应 response 拦截器
request.interceptors.response.use(
  (response) => { //成功回调
    return response.data
  },
  (error) => { //失败回调
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
request.interceptors.request.use(
  (config) => {
    //获取token
    const loginUser = JSON.parse(localStorage.getItem('user'));
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


export default request