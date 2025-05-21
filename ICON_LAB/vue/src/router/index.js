import Vue from 'vue'
import VueRouter from 'vue-router'
import NProgress from 'nprogress'
import store from '@/store'
import 'nprogress/nprogress.css'
import { projectName } from '../../config/config.default'

Vue.use(VueRouter)

const routes = [
  //通用路由
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      title: '注册'
    }
  },
  {
    path: '/404',
    name: '404',
    component: () => import('../views/404.vue'),
    meta: {
      title: '页面失踪啦'
    }
  },
  //下面都是前台路由
  {
    path: '/front',
    name: 'Front',
    component: () => import('../views/front/Front'),
    children: [
      // 前台子路由
      {
        path: 'home',
        name: 'FrontHome',
        component: () => import('../views/front/Home.vue'),
        meta: {
          title: '首页'
        }
      },
      {
        path: 'password',
        name: 'FrontPassword',
        component: () => import('../views/front/Password.vue'),
        meta: {
          title: '修改密码'
        }
      },
      {
        path: 'person',
        name: 'FrontPerson',
        component: () => import('../views/front/Person.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'detail',
        name: 'FrontDetail',
        component: () => import('../views/front/Detail.vue'),
        meta: {
          title: '详情页面'
        }
      },
      {
        path: 'submit',
        name: 'FrontSubmit',
        component: () => import('../views/front/Submit.vue'),
        meta: {
          title: '发布作品'
        }
      },
      {
        path: 'personal',
        name: 'FrontPersonal',
        component: () => import('../views/front/personal.vue'),
        meta: {
          title: '作者主页'
        }
      },
      {
        path: 'mine',
        name: 'FrontMine',
        component: () => import('../views/front/Mine.vue'),
        meta: {
          title: '我的主页'
        }
      },
      {
        path: 'stylize',
        name: 'FrontStylize',
        component: () => import('../views/front/Stylize.vue'),
        meta: {
          title: '风格迁移'
        }
      },
      {
        path: 'openicon',
        name: 'FrontOpenIcon',
        component: () => import('../views/front/OpenIcon.vue'),
        meta: {
          title: '开源图标'
        }
      },
      {
        path: 'recharge',
        name: 'FrontRecharge',
        component: () => import('../views/front/Recharge.vue'),
        meta: {
          title: '充值'
        }
      },
      {
        path: '/front/library',
        name: 'LibraryView',
        component: () => import('@/views/front/LibraryView.vue'),
        meta: { title: '图库详情' }
      },
      {
        path: '/front/search',
        name: 'SearchResults',
        component: () => import('@/views/front/SearchResults.vue'),
        meta: { title: '搜索结果' }
      },
      // 前台子路由
    ]
  },
  //下面都是后台路由
  {
    path: '/',
    name: 'Manage',
    component: () => import('../views/Manage.vue'),
    redirect: '/home',
    children: [
      // 后台子路由
      {
        path: 'home',
        name: '后台主页',
        component: () => import('../views/Home.vue'),
        meta: {
          title: '后台主页'
        }
      },
      {
        path: 'user',
        name: '用户管理',
        component: () => import('../views/User.vue'),
        meta: {
          title: '用户管理'
        }
      },
      {
        path: 'person',
        name: '个人信息',
        component: () => import('../views/Person.vue'),
        meta: {
          title: '个人信息'
        }
      },
      {
        path: 'password',
        name: '修改密码',
        component: () => import('../views/Password.vue'),
        meta: {
          title: '修改密码'
        }
      },
      {
        path: 'type',
        name: '分类管理',
        component: () => import('../views/Type.vue'),
        meta: {
          title: '分类管理'
        }
      },
      {
        path: 'creation',
        name: '作品管理',
        component: () => import('../views/Creation.vue'),
        meta: {
          title: '作品管理'
        }
      },
      {
        path: 'echarts',
        name: '数据统计',
        component: () => import('../views/Echarts.vue'),
        meta: {
          title: '数据统计'
        }
      },
      {
        path: 'rate',
        name: '充值管理',
        component: () => import('../views/Rate.vue'),
        meta: {
          title: '充值比例管理'
        }
      },
      {
        path: 'record',
        name: '流水记录',
        component: () => import('../views/Record.vue'),
        meta: {
          title: '流水记录'
        }
      },
      {
        path: 'log',
        name: '系统日志',
        component: () => import('../views/Log.vue'),
        meta: {
          title: '系统日志'
        }
      },
      // 后台子路由
    ]
  },
]

const router = new VueRouter({
  mode: 'history',
  routes
})

// 重置路由
export const resetRouter = () => {
  router.matcher = new VueRouter({
    mode: 'history',
    routes
  })
}

router.beforeEach((to ,from, next) => {
  NProgress.start() // 开启加载进度条
  localStorage.setItem('currentPathName', to.name) // 设置当前的路由名称
  store.commit('setPath')
  const user = JSON.parse(localStorage.getItem("user") || '{}');
  //如果没有匹配到路由，跳转404
  if (to.matched.length===0){
    next('/404')
  }
  if (to.path === '/home') {
    if (user.role) {
      if (user.role !== 'ROLE_ADMIN') {
        next('/front/home')
      } else {
        next()
      }
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

router.afterEach((to) => {
  NProgress.done() // 关闭加载进度条
  document.title = to.meta.title ? `${to.meta.title} - ${projectName}` : projectName // 设置页面标题
})



export default router

