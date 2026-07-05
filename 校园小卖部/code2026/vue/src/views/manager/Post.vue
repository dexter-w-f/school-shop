<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-input v-model="data.title" style="width: 300px; margin-right: 10px" placeholder="搜索帖子标题"></el-input>
      <el-select v-model="data.category" clearable placeholder="分类" style="width: 150px; margin-right: 10px" @change="load">
        <el-option label="经验分享" value="经验分享"></el-option><el-option label="问答求助" value="问答求助"></el-option><el-option label="闲聊交流" value="闲聊交流"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button><el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>
    <div class="card">
      <el-table :data="data.tableData" stripe :cell-style="{background:'#f5f7fa',color:'#606266'}">
        <el-table-column label="ID" prop="id" width="60"></el-table-column>
        <el-table-column label="标题" prop="title" show-overflow-tooltip></el-table-column>
        <el-table-column label="分类" prop="category" width="80"></el-table-column>
        <el-table-column label="作者" prop="userName" width="100"></el-table-column>
        <el-table-column label="浏览" prop="viewCount" width="60"></el-table-column>
        <el-table-column label="点赞" prop="likeCount" width="60"></el-table-column>
        <el-table-column label="回复" prop="replyCount" width="60"></el-table-column>
        <el-table-column label="置顶" prop="isTop" width="60"></el-table-column>
        <el-table-column label="精华" prop="isEssence" width="60"></el-table-column>
        <el-table-column label="状态" prop="status" width="60"></el-table-column>
        <el-table-column label="时间" prop="time" width="160"></el-table-column>
        <el-table-column label="操作" align="center" width="200" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="toggleProp(scope.row, 'isTop')">{{ scope.row.isTop === '是' ? '取消置顶' : '置顶' }}</el-button>
            <el-button size="small" @click="toggleProp(scope.row, 'isEssence')">{{ scope.row.isEssence === '是' ? '取消精华' : '精华' }}</el-button>
            <el-button size="small" :type="scope.row.status === '正常' ? 'warning' : 'success'" @click="toggleStatus(scope.row)">{{ scope.row.status === '正常' ? '隐藏' : '显示' }}</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
          </template>
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
const data = reactive({ pageNum: 1, pageSize: 10, total: 0, tableData: [], title: null, category: null });
const load = () => { request.get('/post/selectPage', { params: { pageNum: data.pageNum, pageSize: data.pageSize, title: data.title, category: data.category || null } }).then(res => { data.tableData = res.data?.list; data.total = res.data?.total; }) }; load();
const reset = () => { data.title = null; data.category = null; load(); };
const toggleProp = (row, prop) => { row[prop] = row[prop] === '是' ? '否' : '是'; request.put('/post/update', row).then(() => { load(); ElMessage.success('操作成功'); }); };
const toggleStatus = (row) => { row.status = row.status === '正常' ? '隐藏' : '正常'; request.put('/post/update', row).then(() => { load(); ElMessage.success('操作成功'); }); };
const handleDelete = (id) => { ElMessageBox.confirm('确定删除该帖子吗？', '确认', { type: 'warning' }).then(() => { request.delete('/post/delete/' + id).then(() => { load(); ElMessage.success('已删除'); }); }).catch(() => {}); };
</script>