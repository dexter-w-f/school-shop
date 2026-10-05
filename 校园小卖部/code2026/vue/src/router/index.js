import {createRouter, createWebHistory} from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/manager',
      component: () => import('@/views/Manager.vue'),
      redirect: '/manager/home',
      children: [
        { path: 'home', component: () => import('@/views/manager/Home.vue')},
        { path: 'admin', component: () => import('@/views/manager/Admin.vue')},
        { path: 'user', component: () => import('@/views/manager/User.vue')},
        { path: 'person', component: () => import('@/views/manager/Person.vue')},
        { path: 'password', component: () => import('@/views/manager/Password.vue')},
        { path: 'category', component: () => import('@/views/manager/Category.vue')},
        { path: 'goods', component: () => import('@/views/manager/Goods.vue')},
        { path: 'inventory', component: () => import('@/views/manager/Inventory.vue')},
        { path: 'seckill', component: () => import('@/views/manager/Seckill.vue')},
        { path: 'carousel', component: () => import('@/views/manager/Carousel.vue')},
        { path: 'collect', component: () => import('@/views/manager/Collect.vue')},
        { path: 'orders', component: () => import('@/views/manager/Orders.vue')},
        { path: 'comment', component: () => import('@/views/manager/Comment.vue')},
        { path: 'chatManage', component: () => import('@/views/manager/ChatManage.vue')},
        { path: 'refund', component: () => import('@/views/manager/Refund.vue')},
        { path: 'post', component: () => import('@/views/manager/Post.vue')},
        { path: 'reply', component: () => import('@/views/manager/Reply.vue')},
        { path: 'dataManager', component: () => import('@/views/manager/DataManager.vue')},

      ]
    },
      {
          path: '/front',
          component: () => import('@/views/Front.vue'),
          redirect: '/front/home',
          children: [
              { path: 'home', component: () => import('@/views/front/Home.vue')},
              { path: 'person', component: () => import('@/views/front/Person.vue')},
              { path: 'password', component: () => import('@/views/front/Password.vue')},
              { path: 'goods', component: () => import('@/views/front/Goods.vue')},
              { path: 'goodsDetail', component: () => import('@/views/front/GoodsDetail.vue')},
              { path: 'userCollect', component: () => import('@/views/front/UserCollect.vue')},
              { path: 'cart', component: () => import('@/views/front/Cart.vue')},
              { path: 'userOrders', component: () => import('@/views/front/UserOrders.vue')},
              { path: 'userComment', component: () => import('@/views/front/UserComment.vue')},
              { path: 'compare', component: () => import('@/views/front/Compare.vue')},
              { path: 'payment', component: () => import('@/views/front/Payment.vue')},
              { path: 'userRefund', component: () => import('@/views/front/UserRefund.vue')},
              { path: 'postList', component: () => import('@/views/front/PostList.vue')},
              { path: 'postAdd', component: () => import('@/views/front/PostAdd.vue')},
              { path: 'postDetail', component: () => import('@/views/front/PostDetail.vue')},
              { path: 'myPost', component: () => import('@/views/front/MyPost.vue')}
          ]
      },
    { path: '/login', component: () => import('@/views/Login.vue') },
      { path: '/register', component: () => import('@/views/Register.vue') }

  ]
})
router.beforeEach((to) => {
    window.scroll({top:0,behavior:'smooth'})

    let user = {}
    try {
        user = JSON.parse(localStorage.getItem('system-user') || '{}')
    } catch (e) {
        user = {}
    }

    // 登录/注册页直接放行
    if (to.path === '/login' || to.path === '/register') {
        return true
    }

    // 未登录一律回到登录页
    if (!user.token) {
        return '/login'
    }

    // 管理端仅管理员可进入
    if (to.path.startsWith('/manager') && user.role !== '管理员') {
        return '/front/home'
    }

    // 用户端仅普通用户可进入（管理员没有购物车/订单等用户数据）
    if (to.path.startsWith('/front') && user.role === '管理员') {
        return '/manager/home'
    }

    return true
})

export default router





