<template>
  <div>
    <!-- 浮动客服按钮 -->
    <div @click="toggle" 
      style="position: fixed; bottom: 80px; right: 20px; width: 50px; height: 50px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; cursor: pointer; z-index: 999; box-shadow: 0 4px 12px rgba(64,158,255,0.4); font-size: 24px;">
      💬
    </div>

    <!-- 聊天弹窗 -->
    <div v-if="data.open" style="position: fixed; bottom: 145px; right: 20px; width: 350px; height: 450px; background: #fff; border-radius: 12px; box-shadow: 0 4px 20px rgba(0,0,0,0.15); z-index: 1000; display: flex; flex-direction: column; overflow: hidden;">
      <!-- 标题栏 -->
      <div style="background: #409eff; color: #fff; padding: 12px 15px; display: flex; align-items: center; justify-content: space-between;">
        <span style="font-weight: bold;">小卖部助手</span>
        <span @click="data.open = false" style="cursor: pointer; font-size: 18px;">&times;</span>
      </div>

      <!-- 消息列表 -->
      <div ref="msgBox" style="flex: 1; padding: 15px; overflow-y: auto; background: #f5f7fa;">
        <div v-for="(msg, idx) in data.messages" :key="idx" style="margin-bottom: 12px; display: flex; flex-direction: column; align-items: flex-start;">
          <!-- AI 消息 -->
          <div v-if="msg.role === 'ai'" style="display: flex; gap: 8px; max-width: 90%;">
            <div style="width: 30px; height: 30px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px; flex-shrink: 0;">AI</div>
            <div style="background: #fff; padding: 8px 12px; border-radius: 0 8px 8px 8px; font-size: 13px; line-height: 1.5; color: #333; box-shadow: 0 1px 3px rgba(0,0,0,0.05); white-space: pre-wrap;">{{ msg.content }}</div>
          </div>
          <!-- 用户消息 -->
          <div v-if="msg.role === 'user'" style="align-self: flex-end; max-width: 80%;">
            <div style="background: #409eff; color: #fff; padding: 8px 12px; border-radius: 8px 0 8px 8px; font-size: 13px; line-height: 1.5;">{{ msg.content }}</div>
          </div>
        </div>
        <!-- 加载中 -->
        <div v-if="data.loading" style="display: flex; gap: 8px; align-items: center;">
          <div style="width: 30px; height: 30px; border-radius: 50%; background: #409eff; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 12px;">AI</div>
          <div style="background: #fff; padding: 8px 12px; border-radius: 0 8px 8px 8px; font-size: 13px; color: #999;">
            <span style="display: inline-block; animation: blink 1.4s infinite;">.</span>
            <span style="display: inline-block; animation: blink 1.4s infinite; animation-delay: 0.2s;">.</span>
            <span style="display: inline-block; animation: blink 1.4s infinite; animation-delay: 0.4s;">.</span>
          </div>
        </div>
      </div>

      <!-- 输入框 -->
      <div style="padding: 10px; border-top: 1px solid #eee; display: flex; gap: 8px;">
        <el-input v-model="data.input" placeholder="输入您的问题..." @keyup.enter="send" :disabled="data.loading" size="small" />
        <el-button @click="send" type="primary" size="small" :disabled="!data.input.trim() || data.loading">发送</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick, watch } from "vue";
import request from "@/utils/request";

const msgBox = ref(null)
const data = reactive({
  open: false,
  input: '',
  loading: false,
  messages: [
    { role: 'ai', content: '你好！我是小卖部助手，有什么可以帮你的吗？😊' }
  ]
})

const toggle = () => {
  data.open = !data.open
  scrollToBottom()
}

const send = () => {
  const question = data.input.trim()
  if (!question || data.loading) return
  data.input = ''
  data.messages.push({ role: 'user', content: question })
  data.loading = true
  scrollToBottom()

  request.post('/chat/send', { question }).then(res => {
    if (res.code === '200') {
      data.messages.push({ role: 'ai', content: res.data })
    } else {
      data.messages.push({ role: 'ai', content: '抱歉，我暂时无法回答，请联系管理员。' })
    }
  }).catch(() => {
    data.messages.push({ role: 'ai', content: '网络异常，请稍后再试。' })
  }).finally(() => {
    data.loading = false
    scrollToBottom()
  })
}

const scrollToBottom = () => {
  nextTick(() => {
    if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight
  })
}
</script>

<style scoped>
@keyframes blink { 0%,100% { opacity: 0.2; } 50% { opacity: 1; } }
</style>
