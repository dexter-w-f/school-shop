<template>
  <div>
   <div class="card" style="margin-bottom: 5px">
     <el-input style="width: 300px" v-model="data.name"  placeholder="请输入名称查询" :prefix-icon="Search"/>
     <el-button @click="load" type="primary" style="margin-left: 10px">查询</el-button>
     <el-button @click="reset" type="info">重置</el-button>
   </div>

    <div class="card" style="margin-bottom: 5px">
      <div style="margin-bottom: 10px">
        <el-button @click="reset" type="primary">新增</el-button>
      </div>
      <div>
        <el-table :data="data.tableData" stripe style="width: 100%">
          <el-table-column prop="username" label="账号" />
           <el-table-column prop="name" label="姓名"  />
           <el-table-column prop="role" label="角色"  />
          <el-table-column prop="account" label="账户余额"/>

          <el-table-column label="操作" width="180px" fixed="right">
            <template #default="scope">
             <el-button type="primary">编辑</el-button>
             <el-button type="danger" @click="del(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="card" >
      <el-pagination v-model:current-page="data.pageNum" v-model:page-size="data.pageSize"
                     @current-change="load" background layout="total,prev, pager, next" :total="data.total"/>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import {Search} from "@element-plus/icons-vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const data = reactive({
  name:null,
  tableData:[],
  total:0,
  pageNum:1,
  pageSize:5
})


//分页查询数据
const load = () => {
  request.get('/user/selectPag',{
    params:{
      pageNum:data.pageNum,
      pageSize:data.pageSize,
      name:data.name,
    }
      }

  ).then(res =>{
    if(res.code === '200'){
      data.tableData = res.data?.list
      data.total = res.data?.total
    }else{
      ElMessage.error(res.msg)
    }
      }
  )
}
load()
const reset = () =>{
  data.name = null
  load()
}

const del =(id)=>{
  request.delete('/user/delete/'+ id).then(res =>{
        if(res.code === '200'){
          ElMessage.success('操作成功')
          load()
        }else{
          ElMessage.error(res.msg)
        }
      })
}
</script>