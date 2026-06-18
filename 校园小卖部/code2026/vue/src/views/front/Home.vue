<template>
  <div class="front-container">
    <div style="display: flex; grid-gap:20px;margin-bottom: 15px">
      <div style="flex:1">
        <el-carousel height="450px">
          <el-carousel-item v-for="item in data.carouselList" :key="item.id">
            <img @click="router.push('/front/goodsDetail?id=' + item.goodsId)" :src="item.img" style="width: 100%; height: 450px;cursor: pointer">
          </el-carousel-item>
        </el-carousel>
      </div>
      <div class="card" style="width: 200px">
        <div style="padding-bottom: 5px;border-bottom: 1px solid red">
          <div style="color:red;font-size: 20px; font-weight: bold;margin-left: 2px">推荐商品</div>
        </div>
        <div style="padding: 20px 0">
          <div class="top-item" @click="router.push('/front/goodsDetail?id=' + item.id)" style="cursor: pointer;display: flex;grid-gap: 10px;margin-bottom: 20px" v-for="item in data.recommendGoods" :key="item.id">
            <img style="width: 70px;height: 70px" :src="item.img">
            <div>
              <div class="line2" style="margin-bottom: 5px">{{ item.name }}</div>
              <div style="color: red"><span>￥</span><b>{{ item.price }}</b></div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!--  --------------------- --------------    --------------------     ------     -->
    <div class="card" style="padding: 20px">
      <div style="display: flex; align-items: flex-end;padding-bottom: 5px;border-bottom: 1px solid red;margin-bottom: 10px">
      <div style="flex: 1;display: flex;align-items: center">
        <img src="@/assets/imgs/hot.png" style="width: 25px">
        <div style="color:red;font-size: 20px; font-weight: bold;margin-left: 2px">热销商品</div>
      </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer;font-size: 13px">查看更多</div>
      </div>
      <div style="margin-bottom: 40px">
        <el-row gutter="20">
          <el-col :span="6" v-for="item in data.hotGoods" :key="item.id">
            <div  @click="router.push('/front/goodsDetail?id=' + item.id)"  class="item" style="width: 100%; border-radius: 5px;height: 280px" >
              <img :src="item.img" style="width: 100%;height:200px; border-radius: 5px 5px 0 0">
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px">{{item.name}}</div>
                <div><span style="color: red">￥</span> <b style="color: red;font-size: 15px">{{item.price}}</b>
                  <span style="margin-left: 10px;color: #666"> 销量：{{item.saleCount}} </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <!--    -----     ----------              -------------        -------------       -----------  -->
    <div class="card" style="padding: 20px">
      <div style="display: flex; align-items: flex-end;padding-bottom: 5px;border-bottom: 1px solid red;margin-bottom: 10px">
        <div style="flex: 1;display: flex;align-items: center">
          <img src="@/assets/imgs/new.png" style="width: 25px">
          <div style="color:red;font-size: 20px; font-weight: bold;margin-left: 2px">最新上架</div>
        </div>
        <div @click="router.push('/front/goods')" style="cursor: pointer;font-size: 13px">查看更多</div>
      </div>
      <div>
        <el-row gutter="20">
          <el-col :span="6" v-for="item in data.newGoods" :key="item.id">
            <div  @click="router.push('/front/goodsDetail?id=' + item.id)" class="item" style="width: 100%; border-radius: 5px;height: 280px" >
              <img :src="item.img" style="width: 100%;height:200px; border-radius: 5px 5px 0 0">
              <div style="padding: 5px">
                <div class="line1" style="font-size: 16px">{{item.name}}</div>
                <div><span style="color: red">￥</span> <b style="color: red;font-size: 15px">{{item.price}}</b>
                  <span style="margin-left: 10px;color: #666"> 销量：{{item.saleCount}} </span>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </div>
    </div>
    <!-- -- --------- ---------         ---------------        ---------- ---------      -------- -->
  </div>
</template>

<script setup>
import {reactive} from "vue";
import router from "@/router";
import request from "@/utils/request"
const data = reactive({
  carouselList: [],
  hotGoods:[],
  recommendGoods:[],
  newGoods:[]
})

request.get('/carousel/selectAll').then(res => {
  data.carouselList = res.data
})

request.get('/goods/selectAll',{
  params:{
    status:'上架'
  }
}).then(res => {
  data.hotGoods = res.data.sort((v1,v2) =>v2.saleCount - v1.saleCount).splice(0,4)
})

request.get('/goods/selectAll',{
  params:{
    status:'上架'
  }
}).then(res => {
  data.newGoods = res.data.splice(0,4)
})

request.get('/goods/selectAll',{
  params:{
    status:'上架'
  }
}).then(res => {
  data.recommendGoods = res.data.filter(v => v.recommend === '是').splice(0,5)
})

</script>

<style>
.item{
  cursor: pointer;
}
.item:hover{
  border: 1px solid red
}
.top-item:hover{
  color: red;
}
</style>