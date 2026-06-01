import { createRouter, createWebHistory } from 'vue-router'
import LoginView from '@/views/login.vue'
import register from '@/views/register.vue'
import ContactView from '@/views/contact.vue'
import IndexView from '@/views/index.vue'
import LayoutView from '@/views/layout.vue'
import P2pchatView from '@/views/p2pchat.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {path: '/login', name: 'login',component: LoginView},
    {path: '/register', name:'register',component: register},
    {
     path: '/', 
     name: '',
     component: LayoutView,
     redirect: 'index', //重定向
      children: [ 
        {path: 'index', name: 'index', component: IndexView},    
        {path: 'contact', name: 'contact',component: ContactView},
        {path: 'p2pchat', name: 'p2pchat',component: P2pchatView}
      ]
    },
    {
      path: '/:catchAll(.*)', // 匹配所有未定义的路由
      redirect: '/login' // 重定向到登录页面
    }
  ]
    // {
    //   path: '/about',
    //   name: 'about',
    //   component: () => import('../views/AboutView.vue')
    // }
  
})
export default router
