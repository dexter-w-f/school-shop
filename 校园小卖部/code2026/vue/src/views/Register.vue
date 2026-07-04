<template>
  <div class="login-container">
    <div class="login-box">
      <div style="font-weight: bold; font-size: 30px; text-align: center; margin-bottom: 30px; color: #19e348">欢 迎 注 册</div>
      <el-form :model="data.form"  ref="formRef" :rules="data.rules">
        <el-form-item prop="username">
        <el-form-item prop="name">
          <el-input :prefix-icon="User" size="large" v-model="data.form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item prop="username">
          <el-input :prefix-icon="Message" size="large" v-model="data.form.username" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.password" placeholder="请输入密码" show-password />
        </el-form-item>
        <el-form-item prop="newPassword">
          <el-input :prefix-icon="Lock" size="large" v-model="data.form.newPassword" placeholder="请确认密码" show-password />
        </el-form-item>
        <!-- 验证码 -->
        <el-form-item prop="captchaCode">
          <div style="display: flex; gap: 10px; width: 100%;">
            <el-input v-model="data.form.captchaCode" placeholder="请输入验证码" style="flex: 1;" />
            <el-button @click="sendCaptcha" :disabled="data.captchaCountdown > 0" style="width: 130px; flex-shrink: 0;">
              {{ data.captchaCountdown > 0 ? data.captchaCountdown + '秒后重试' : '获取验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item>
          <el-button size="large"  style="width: 100%;background-color:#0c9c7a;border-color: #0c9c7a; color:white" @click="register">注 册</el-button>
        </el-form-item>
      </el-form>
      <div style="text-align: right;">
       已有账号？请 <a href="/login">登录</a>
      </div>
    </div>

  </div>
</template>

<script setup>
  import { reactive, ref } from "vue";
  import { User, Message, Lock } from "@element-plus/icons-vue";
  import request from "@/utils/request";
  import {ElMessage} from "element-plus";
  import router from "@/router";

  const data = reactive({
    form: { role: '普通用户' },
    captchaCountdown: 0,
    rules: {
      username: [
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
      ],
      username: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度至少6位', trigger: 'blur' }
      ],
      newPassword: [
        { required: true, message: '请确认密码', trigger: 'blur' },
        { validator: validateConfirmPassword, trigger: 'blur' }
      ],
      captchaCode: [
        { required: true, message: '请输入验证码', trigger: 'blur' },
      ]
    }
  })

  const formRef = ref()

  let countdownTimer = null
  const sendCaptcha = () => {
    if (!data.form.username) { ElMessage.warning('请先输入账号'); return }
    data.captchaCountdown = 60
    countdownTimer = setInterval(() => {
      data.captchaCountdown--
      if (data.captchaCountdown <= 0) clearInterval(countdownTimer)
    }, 1000)
    request.post('/captcha/send', { username: data.form.username }).then(res => {
      if (res.code === '200') {
        ElMessage.success('验证码已发送到您的邮箱，请查收')
      } else {
        ElMessage.error(res.msg)
        clearInterval(countdownTimer)
        data.captchaCountdown = 0
      }
    })
  }

  // 验证两次密码是否一致
  function validateConfirmPassword(rule, value, callback) {
    if (value !== data.form.password) {
      callback(new Error('两次输入的密码不一致'))
    } else {
      callback()
    }
  }

  // 点击登录按钮的时候会触发这个方法
  const register = () => {
    formRef.value.validate((valid => {
      if (valid) {
        // 调用后台的接口
        request.post('/register', data.form).then(res => {
          if (res.code === '200') {
            ElMessage.success("注册成功")
            if (countdownTimer) clearInterval(countdownTimer)
            router.push('/login')

          } else {
            ElMessage.error(res.msg)
          }
        })
      }
    })).catch(error => {
      console.error(error)
    })
  }

</script>

<style scoped>
.login-container {
  height: 100vh;
  overflow:hidden;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #3f833f;
  background-size: cover;
}
.login-box {
  width: 350px;
  padding: 50px 30px;
  border-radius: 5px;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.3);
  background-color: #fff;
}
</style>
