<template>
  <div class="front-container" style="width: 90%;">
    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px;">
      <div style="display: flex; grid-gap: 10px; align-items: center;">
        <el-radio-group v-model="data.category" @change="load" size="small">
          <el-radio-button value="">全部</el-radio-button>
          <el-radio-button value="经验分享">经验分享</el-radio-button>
          <el-radio-button value="问答求助">问答求助</el-radio-button>
          <el-radio-button value="闲聊交流">闲聊交流</el-radio-button>
        </el-radio-group>
        <el-select v-model="data.orderBy" @change="load" size="small" style="width: 100px;">
          <el-option label="最新" value="new"></el-option>
          <el-option label="最热" value="hot"></el-option>
        </el-select>
      </div>
      <el-button type="primary" @click="router.push('/front/postAdd')">发 帖</el-button>
    </div>

    <div v-if="data.tableData.length === 0" style="text-align: center; padding: 60px 0; color: #999;"><p>暂无帖子</p></div>

    <div v-for="item in data.tableData" :key="item.id" class="card" style="margin-bottom: 12px; padding: 18px 22px; cursor: pointer;" @click="router.push('/front/postDetail?id=' + item.id)">
      <div style="display: flex; justify-content: space-between; align-items: flex-start;">
        <div style="flex: 1;">
          <div style="display: flex; align-items: center; grid-gap: 8px; margin-bottom: 6px;">
            <el-tag v-if="item.isTop === '是'" size="small" type="danger">置顶</el-tag>
            <el-tag v-if="item.isEssence === '是'" size="small" type="warning">精华</el-tag>
            <el-tag size="small" plain>{{ item.category }}</el-tag>
            <span style="font-size: 16px; font-weight: bold; color: #333;">{{ item.title }}</span>
          </div>
          <div style="font-size: 13px; color: #999; display: flex; align-items: center; grid-gap: 15px;">
            <span>{{ item.userName }}</span>
            <span>{{ item.time }}</span>
            <span>👁 {{ item.viewCount }}</span>
            <span>👍 {{ item.likeCount }}</span>
            <span>💬 {{ item.replyCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <div style="margin-top: 20px;">
      <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {useRouter} from "vue-router";
const router = useRouter();

const data = reactive({
  pageNum: 1, pageSize: 10, total: 0, tableData: [],
  category: '', orderBy: 'new'
})

const load = () => {
  request.get('/post/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, category: data.category || null }
  }).then(res => {
    data.tableData = res.data?.list || []
    data.total = res.data?.total || 0
  })
}
load()
</script>