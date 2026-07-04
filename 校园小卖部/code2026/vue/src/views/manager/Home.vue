<template>
  <div>
    <!-- 欢迎 -->
    <div style="font-size: 20px; font-weight: bold; margin-bottom: 20px;">欢迎回来，{{ data.user.name }}</div>

    <!-- 快捷操作 -->
    <div class="card" style="padding: 20px; margin-bottom: 20px;">
      <div style="font-size: 16px; font-weight: bold; margin-bottom: 15px;">快捷操作</div>
      <div style="display: flex; gap: 12px;">
        <el-button @click="$router.push('/manager/goods')" type="primary" plain>➕ 添加商品</el-button>
        <el-button @click="$router.push('/manager/orders')" type="success" plain>📦 查看订单</el-button>
        <el-button @click="$router.push('/manager/category')" type="warning" plain="false">📂 管理分类</el-button>
        <el-button @click="$router.push('/manager/user')" type="info" plain>👥 用户管理</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div style="display: flex; gap: 15px; margin-bottom: 20px;">
      <div class="card" style="flex: 1; padding: 20px; border-left: 4px solid #409eff;">
        <div style="color: #666; font-size: 13px;">销售总额</div>
        <div style="font-size: 24px; font-weight: bold; color: #409eff;">&yen;{{ data.stats.total }}</div>
      </div>
      <div class="card" style="flex: 1; padding: 20px; border-left: 4px solid #67c23a;">
        <div style="color: #666; font-size: 13px;">今日销售额</div>
        <div style="font-size: 24px; font-weight: bold; color: #67c23a;">&yen;{{ data.stats.today }}</div>
      </div>
      <div class="card" style="flex: 1; padding: 20px; border-left: 4px solid #e6a23c;">
        <div style="color: #666; font-size: 13px;">商品总数</div>
        <div style="font-size: 24px; font-weight: bold; color: #e6a23c;">{{ data.stats.goods }}</div>
      </div>
      <div class="card" style="flex: 1; padding: 20px; border-left: 4px solid #f56c6c;">
        <div style="color: #666; font-size: 13px;">注册用户</div>
        <div style="font-size: 24px; font-weight: bold; color: #f56c6c;">{{ data.stats.user }}</div>
      </div>
    </div>

    <!-- 待处理订单 + 低库存预警 -->
    <div style="display: flex; gap: 20px;">
      <div class="card" style="flex: 1; padding: 20px;">
        <div style="font-size: 16px; font-weight: bold; margin-bottom: 15px; display: flex; align-items: center; justify-content: space-between;">
          <span>待处理订单</span>
          <el-tag type="warning" size="small">{{ data.pendingOrders.length }} 笔</el-tag>
        </div>
        <div v-if="data.pendingOrders.length === 0" style="text-align: center; padding: 30px 0; color: #999;">暂无待处理订单</div>
        <div v-for="(order, idx) in data.pendingOrders.slice(0, 5)" :key="order.id" style="display: flex; align-items: center; justify-content: space-between; padding: 10px 0; border-bottom: idx === data.pendingOrders.slice(0,5).length-1 ? 'none' : '1px solid #f0f0f0';">
          <div>
            <div style="font-size: 13px; font-weight: bold;">{{ order.orderNo }}</div>
            <div style="font-size: 12px; color: #999;">{{ order.userName }} · {{ order.time }}</div>
          </div>
          <div style="text-align: right;">
            <div style="color: red; font-weight: bold;">&yen;{{ order.total }}</div>
            <div v-if="order.deliverType === '外送'" style="font-size: 12px; color: #e6a23c;">需配送</div>
            <div v-else style="font-size: 12px; color: #67c23a;">到店自提</div>
          </div>
        </div>
        <div v-if="data.pendingOrders.length > 5" style="text-align: center; margin-top: 10px;">
          <el-button @click="$router.push('/manager/orders')" link size="small">查看全部 {{ data.pendingOrders.length }} 笔</el-button>
        </div>
      </div>

      <div class="card" style="flex: 1; padding: 20px;">
        <div style="font-size: 16px; font-weight: bold; margin-bottom: 15px; display: flex; align-items: center; justify-content: space-between;">
          <span>库存预警</span>
          <el-tag v-if="data.lowStockGoods.length > 0" type="danger" size="small">{{ data.lowStockGoods.length }} 件</el-tag>
          <el-tag v-else type="success" size="small">正常</el-tag>
        </div>
        <div v-if="data.lowStockGoods.length === 0" style="text-align: center; padding: 30px 0; color: #999;">所有商品库存充足</div>
        <div v-for="(g, idx) in data.lowStockGoods.slice(0, 5)" :key="g.id" style="display: flex; align-items: center; gap: 10px; padding: 8px 0; border-bottom: idx === data.lowStockGoods.slice(0,5).length-1 ? 'none' : '1px solid #f0f0f0';">
          <img :src="g.img" style="width: 40px; height: 40px; border-radius: 4px;" />
          <div style="flex: 1;">
            <div style="font-size: 13px;">{{ g.name }}</div>
            <div style="font-size: 12px; color: #999;">已售 {{ g.saleCount || 0 }}</div>
          </div>
          <div style="color: red; font-weight: bold;">{{ g.store }}</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue";
import request from "@/utils/request";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  stats: { total: 0, today: 0, goods: 0, user: 0 },
  pendingOrders: [],
  lowStockGoods: []
})

onMounted(() => {
  request.get('/count').then(res => {
    if (res.data) data.stats = res.data
  })

  request.get('/orders/selectPage', {
    params: { pageNum: 1, pageSize: 50, status: '待接单' }
  }).then(res => {
    data.pendingOrders = res.data?.list || []
  })

  request.get('/goods/selectAll', { params: { status: '上架' } }).then(res => {
    if (res.data) data.lowStockGoods = res.data.filter(g => g.store < 10).sort((a,b) => a.store - b.store)
  })
})
</script>
