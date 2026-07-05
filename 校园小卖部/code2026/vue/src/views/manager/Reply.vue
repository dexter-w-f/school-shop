<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.content" style="width: 300px; margin-right: 10px" placeholder="搜索回复内容"></el-input>
      <el-button type="primary" @click="load">查询</el-button><el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card">
      <el-table :data="data.tableData" stripe :cell-style="{background:'#f5f7fa',color:'#606266'}">
        <el-table-column label="ID" prop="id" width="60"></el-table-column>
        <el-table-column label="帖子ID" prop="postId" width="70"></el-table-column>
        <el-table-column label="回复人" prop="userName" width="100"></el-table-column>
        <el-table-column label="内容" prop="content" show-overflow-tooltip></el-table-column>
        <el-table-column label="时间" prop="time" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="100" fixed="right">
          <template #default="scope"><el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button></template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px"><el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/></div>
    </div>
  </div>
</template>
<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
const data = reactive({ pageNum: 1, pageSize: 10, total: 0, tableData: [], content: null });
const load = () => { request.get('/reply/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, content: data.content } }).then(res => { data.tableData = res.data?.list; data.total = res.data?.total; }) }; load();
const reset = () => { data.content = null; load(); };
const handleDelete = (id) => { ElMessageBox.confirm('确定删除该回复吗？', '确认', { type: 'warning' }).then(() => { request.delete('/reply/delete/' + id).then(() => { load(); ElMessage.success('已删除'); }); }).catch(() => {}); };
</script>