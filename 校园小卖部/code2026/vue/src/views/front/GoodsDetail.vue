<template>
  <div class="front-container" style="">
    <div class="card" style="padding: 20px;display: flex; grid-gap: 20px;margin-bottom: 10px">
      <img :src="data.goods.img" style="width: 300px;height: 300px;">
      <div style="flex: 1">
        <div style="display: flex; align-items: flex-start; grid-gap: 20px ;justify-content: space-between; margin-bottom: 10px">
        <div style="font-size: 22px;font-weight: bold;line-height: 25px">
          <el-tag style="margin-right: 5px;background-color: red;color: white;"  type="danger" v-if="data.goods.recommend === '是'">推荐</el-tag>
          {{data.goods.name}}</div>
          <div style="width: 60px;cursor: pointer;color: #666">
            <el-icon style="position: relative;top:3px" size="18"><Star /></el-icon>收藏
          </div>
        </div>
        <div style="margin-bottom: 20px">
          <span style="color: red;font-size: 18px">￥</span> <b style="color:red;font-size: 30px;">{{data.goods.price}}</b>
          <span style="color: #666;margin-left: 20px">累计销量 {{data.goods.saleCount}}</span>
          <span style="color: #666;margin-left: 20px">剩余库存{{data.goods.store}}</span>
        </div>
        <div  style="margin-bottom: 20px;padding: 10px;background-color: lavenderblush;border-radius: 5px;line-height: 25px;text-align: justify">{{data.goods.description}}</div>
        <div>
          <el-input-number style="width: 150px;height: 40px;" :min="1" v-model="data.num"></el-input-number>
          <el-button style="height: 40px;margin-left: 5px" type="danger">加入购物车</el-button>
          <el-button style="height: 40px;margin-left: 5px" type="danger">立即购买</el-button>
        </div>
      </div>
    </div>


  <div class="card" style="padding: 20px;margin-bottom: 50px">
    <div style="font-size: 20px;padding-bottom: 10px;border-bottom: 1px solid #ddd">
      <span @click="changeTab('商品详情')" style="cursor: pointer" :class="{'current-active':data.current==='商品详情'}">商品详情</span>
      <span @click="changeTab('商品评论')" :class="{'current-active':data.current==='商品评论'}" style="cursor: pointer;margin-left: 20px">商品评论</span>
    </div>
    <div v-if="data.current==='商品详情'"  style="padding: 10px" v-html="data.goods.content"></div>
    <div v-if="data.current==='商品评论'" style="min-height: 700px">
      <div v-if="data.commentList.length === 0" style="padding: 50px;text-align: center;color: #666">暂无评论...</div>
      <div v-if="data.commentList.length > 0" style="padding: 20px;text-align: center">

      </div>
    </div>
   </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import router from "@/router";
import request from "@/utils/request";
const data = reactive({
  id:router.currentRoute.value.query.id,
  goods:{},
  num:1,
  current:'商品详情',
  commentList:[],
})

const changeTab = (tabName) => {
  data.current = tabName
}

const load = () => {
  request.get('/goods/selectById/'+ data.id,).then(res => {
    data.goods = res.data
  })
}
load()
</script>

<style>
.current-active{
  color: red;
  border-bottom: 1px solid red;
  padding-bottom: 10px
}
</style>