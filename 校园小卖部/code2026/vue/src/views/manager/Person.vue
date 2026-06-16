<template>
  <div >
    <div class="card" style="width: 50%">

      <el-form  ref="formRef" :model="data.user" :rules="data.rules" label-width="80px" style="padding-right: 30px">
        <el-form-item prop="avatar" label="头像" >
          <el-upload
              class="avatar-uploader"
              :action="baseURL+'/files/upload'"
              :show-file-list="false"
              :on-success="handleFileUpload"
          >
            <img v-if="data.user.avatar" :src="data.user.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>

        <el-form-item prop="username" label="账号" >
          <el-input :disabled="data.user.id!==undefined" v-model="data.user.username" placeholder="请输入账号" autocomplete="off" />
        </el-form-item>
        <el-form-item prop="name" label="姓名" >
          <el-input v-model="data.user.name" placeholder="请输入姓名" autocomplete="off" />
        </el-form-item>


        <div style="text-align: center">
          <el-button type="primary" size="large" @click="update">保 存</el-button>
        </div>


      </el-form>
    </div>
  </div>

</template>

<script setup>

import {reactive,ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

const emit = defineEmits(['updateUser'])

const  baseURL =import.meta.env.VITE_BASE_URL

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user')),
  rules:{
    username:[
      {required:true,message:'请输入账号',trigger:'blur'}
    ]
  }
})
const loadUser = () => {
  request.get(`/admin/selectById/${data.user.id}`).then(res =>{
    if (res.code === '200') {
      // 使用 Object.assign 保持响应式
      Object.assign(data.user, res.data)
      //存储最新信息
      localStorage.setItem('system-user', JSON.stringify(data.user))
      emit('updateUser')
    }
  }).catch(err => {
    console.error('加载用户信息失败:', err)
  })
}
loadUser()
const handleFileUpload = (res) =>{
  if (res.code === '200') {
    data.user.avatar = res.data
  } else {
    ElMessage.error('上传失败')
  }
}

const update = () => {
  request.put('/admin/update', data.user).then(res => {
    if (res.code === '200') {
      ElMessage.success('更新成功')
      loadUser()

    } else {
      ElMessage.error(res.msg)
    }
  })
}
</script>
<style scoped>
.avatar-uploader .avatar {
  width: 130px;
  height: 130px;
  display: block;
}
</style>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader .el-upload:hover {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 130px;
  height: 130px;
  text-align: center;
}
</style>
