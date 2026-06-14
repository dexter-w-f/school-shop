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
             <el-button type="primary">编辑</el-button>
             <el-button type="danger">删除</el-button>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <div class="card" >
      <el-pagination background layout="total,prev, pager, next" :total="1"/>
    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import {Search} from "@element-plus/icons-vue";

const data = reactive({
  name:null,
  tableData:[
    {username:'aaa',name:'bbbb',role:'cccc',account:'232'}
  ]
})

const load = () => {

}

const reset = () =>{

}
</script>