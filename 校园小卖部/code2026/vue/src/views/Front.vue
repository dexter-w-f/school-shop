<template>
  <div>
    <div style="height: 60px; background-color: #2e3143; display: flex; align-items: center; ">
      <div style="width: 20%;">
        <div style="padding-left: 20px; display: flex; align-items: center">
          <img src="@/assets/imgs/logo.png" alt="" style="width: 40px">
          <div style="font-weight: bold; font-size: 24px; margin-left: 5px; color: #fff">校园小卖部</div>
        </div>
      </div>
      <div style="width: 60%;height: 60px;display: flex;align-items: center" >
        <div style="flex: 1">
          <el-menu router :default-active="router.currentRoute.value.path" style="background-color:#313237" ellipsis mode="horizontal">
            <el-menu-item  index='/front/home'>首页</el-menu-item>
            <el-menu-item  index='/front/goods'>精选商品</el-menu-item>
            <el-menu-item  index='/front/cart'>购物车</el-menu-item>
            <el-menu-item  index='/front/userOrders'>我的订单</el-menu-item>
          </el-menu>
        </div>
           <div style="width: fit-content" v-if="router.currentRoute.value.path !== '/front/goods'">
             <el-input @keyup.enter="search" prefix-icon="Search" style="width: 300px;height: 40px" v-model="data.goodsName" placeholder="请输入商品名称"></el-input>
           </div>
      </div>
      <div style="width: 20%;text-align: right; padding-right: 10px; ">
        <el-dropdown>
         <div style="display: flex; align-items: center;">
           <img style="width: 40px; height: 40px; border-radius: 50%" :src="data.user.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'" >
           <span style="color: #fff; margin-left: 5px">{{ data.user.name ||'' }}</span>
         </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="router.push('/front/userCollect')">我的收藏</el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/userComment')">我的评价</el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/person')">个人信息</el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/password')">修改密码</el-dropdown-item>
              <el-dropdown-item @click="router.push('/front/userRecharge')">我的充值</el-dropdown-item>
              <el-dropdown-item @click="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

      </div>
    </div>
    <div style="background-color:#ced4df">
      <router-view @updateUser="updateUser" />
    </div>


  </div>

</template>
<script setup>
import { reactive } from "vue";
import router from "@/router";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  goodsName:null
})

const search = () => {
  if(data.goodsName){
    router.push('/front/goods?name=' + data.goodsName)
    data.goodsName = null
  }

}

if(!data.user?.id){
  router.push('/login')
}
const logout = () => {
  request.post('/logout').then(() => {
    localStorage.removeItem('system-user')
    router.push('/login')
    ElMessage.success('退出成功')
  }).catch(() => {
    localStorage.removeItem('system-user')
    router.push('/login')
  })
}

const updateUser = () => {
  const newUser = JSON.parse(localStorage.getItem('system-user') || '{}')
  // 使用 Object.assign 保持响应式
  Object.assign(data.user, newUser)
}

</script>

<style>
.el-tooltip__trigger{
  cursor: pointer;
  outline: none !important;
}
.el-menu--horizontal{
  border: none!important;
}
.el-menu--horizontal .el-menu-item{
  border: none;
  height: 60px;
}
.el-menu--horizontal .el-menu-item{
  border: none;
  color: white;
}
.el-menu--horizontal .el-menu-item.is-active{
  border: none;
  color: white!important;
  background-color: #0c9c7a !important;
}
.el-menu--horizontal .el-menu-item:not(.is-active):hover{
 color: white!important;
  background-color: #9ec6bc !important;
}
</style>
