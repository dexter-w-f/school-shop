<template>
  <div class="front-container" style="width: 50%">
    <div class="card">
     个人信息页面
      <el-form  ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="username" label="账号" >
          <el-input :disabled="data.form.id!==undefined" v-model="data.form.username" placeholder="请输入账号" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="name" label="姓名" >
          <el-input v-model="data.form.name" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="avatar" label="头像" >
          <el-upload :action="baseURL+'/files/upload'" list-type="picture" :on-success="handleFileUpload">
            <el-button type="primary">点击上传图片</el-button>
          </el-upload>
        </el-form-item>


      </el-form>
    </div>
  </div>

</template>

<script setup>

import {reactive} from "vue";
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user'))
})
</script>