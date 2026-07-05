<template>
  <div class="front-container" style="width: 90%;">
    <div style="font-size: 18px; font-weight: bold; margin-bottom: 15px;">我的帖子</div>
    <div v-if="data.tableData.length === 0" style="text-align: center; padding: 60px 0; color: #999;"><p>暂无帖子</p></div>
    <div v-for="item in data.tableData" :key="item.id" class="card" style="margin-bottom: 12px; padding: 15px 20px; cursor: pointer;" @click="router.push('/front/postDetail?id=' + item.id)">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div><el-tag size="small" plain>{{ item.category }}</el-tag><span style="margin-left: 8px; font-weight: bold;">{{ item.title }}</span></div>
        <div style="font-size: 12px; color: #999;">👁 {{ item.viewCount }} 👍 {{ item.likeCount }} 💬 {{ item.replyCount }}<span style="margin-left: 10px;">{{ item.time }}</span></div>
      </div>
    </div>
    <div style="margin-top: 20px;"><el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/></div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {useRouter} from "vue-router";
const router = useRouter();
const data = reactive({ user: JSON.parse(localStorage.getItem("system-user") || "{}"), pageNum: 1, pageSize: 10, total: 0, tableData: [] });
const load = () => {
  request.get("/post/selectPage", { params: { pageNum: data.pageNum, pageSize: data.pageSize, userId: data.user.id } }).then(res => {
    data.tableData = res.data?.list || []; data.total = res.data?.total || 0;
  })
}
load();
</script>