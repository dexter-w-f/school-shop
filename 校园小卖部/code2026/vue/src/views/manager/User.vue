<template>
  <div>
   <div class="card" style="margin-bottom: 5px">
     <el-input style="width: 300px" v-model="data.name"  placeholder="请输入名称查询" :prefix-icon="Search"/>
     <el-button @click="load" type="primary" style="margin-left: 10px">查询</el-button>
     <el-button @click="reset" type="info">重置</el-button>
   </div>

    <div class="card">
      <div style="margin-bottom: 10px">
        <el-button @click="reset" type="info">新增</el-button>
      </div>

    </div>
  </div>
</template>

<script setup>
import {reactive} from "vue";
import {Search} from "@element-plus/icons-vue";

const data = reactive({
  name:null
})

const load = () => {

}

const reset = () =>{

}
</script>