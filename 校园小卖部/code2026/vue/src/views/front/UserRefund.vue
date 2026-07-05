<template>
  <div class="front-container" style="width: 90%;">
    <div style="margin-bottom: 10px;">
      <el-select v-model="data.statusFilter" clearable placeholder="筛选状态" @change="load" style="width: 150px; margin-right: 10px">
        <el-option label="待审核" value="待审核"></el-option>
        <el-option label="已通过" value="已通过"></el-option>
        <el-option label="已退款" value="已退款"></el-option>
        <el-option label="已拒绝" value="已拒绝"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查 询</el-button>
    </div>

    <div v-if="data.tableData.length === 0" style="text-align: center; padding: 60px 0; color: #999;">
      <el-icon :size="60"><Folder /></el-icon>
      <p style="margin-top: 10px">暂无售后记录</p>
    </div>

    <div v-for="item in data.tableData" :key="item.id" class="card" style="margin-bottom: 15px; padding: 20px;">
      <div style="display: flex; align-items: flex-start; grid-gap: 20px;">
        <img :src="item.goodsImg" style="width: 100px; height: 100px; border-radius: 5px; object-fit: cover;" v-if="item.goodsImg">
        <div style="flex: 1;">
          <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px;">
            <div>
              <span style="font-weight: bold; font-size: 16px;">{{ item.goodsName || '售后申请' }}</span>
              <el-tag style="margin-left: 10px;" :type="item.status === '已退款' ? 'success' : item.status === '已拒绝' ? 'danger' : item.status === '已通过' ? 'primary' : 'warning'">{{ item.status }}</el-tag>
            </div>
            <div>
              <span style="color: #999; font-size: 13px;">{{ item.time }}</span>
            </div>
          </div>
          <div style="color: #666; font-size: 14px; line-height: 1.8;">
            <div><b>售后类型：</b>{{ item.type }}</div>
            <div><b>退款金额：</b><span style="color: red; font-weight: bold;">¥{{ item.amount }}</span></div>
            <div><b>退款原因：</b>{{ item.reason }}</div>
            <div v-if="item.description"><b>问题描述：</b>{{ item.description }}</div>
            <div v-if="item.reply" style="color: #409eff;"><b>管理员回复：</b>{{ item.reply }}</div>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 20px">
      <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  statusFilter: null
})

const load = () => {
  request.get('/refundOrders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      userId: data.user.id,
      status: data.statusFilter
    }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}
load()
</script>

<style scoped>
.card:hover {
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  transition: box-shadow 0.3s;
}
</style>
