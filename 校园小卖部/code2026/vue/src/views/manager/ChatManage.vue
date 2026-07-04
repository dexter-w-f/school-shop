<template>
  <div style="display: flex; gap: 15px; height: calc(100vh - 100px);">
    <!-- 用户列表 -->
    <div class="card" style="width: 300px; flex-shrink: 0; display: flex; flex-direction: column; overflow: hidden;">
      <div style="font-size: 16px; font-weight: bold; padding: 15px; border-bottom: 1px solid #eee;">
        咨询用户 <el-tag size="small" style="margin-left: 8px;">{{ data.sessions.length }}</el-tag>
      </div>
      <div style="flex: 1; overflow-y: auto;">
        <div v-for="s in data.sessions" :key="s.userId"
          @click="selectUser(s)"
          :style="{
            padding: '12px 15px', cursor: 'pointer', borderBottom: '1px solid #f5f5f5',
            background: data.selectedUser?.userId === s.userId ? '#f0f7ff' : '#fff'
          }">
          <div style="display: flex; justify-content: space-between; align-items: center;">
            <span style="font-weight: bold;">{{ s.userName }}</span>
            <span style="font-size: 12px; color: #999;">{{ s.lastTime?.substring(5, 16) }}</span>
          </div>
          <div style="font-size: 13px; color: #666; margin-top: 3px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">
            {{ s.lastQuestion?.substring(0, 30) }}{{ s.lastQuestion?.length > 30 ? '...' : '' }}
          </div>
          <div style="font-size: 12px; color: #999; margin-top: 2px;">共 {{ s.messageCount }} 条消息</div>
        </div>
        <div v-if="data.sessions.length === 0" style="text-align: center; padding: 40px 0; color: #999;">暂无咨询记录</div>
      </div>
    </div>

    <!-- 聊天记录 -->
    <div class="card" style="flex: 1; display: flex; flex-direction: column; overflow: hidden;">
      <div v-if="!data.selectedUser" style="flex: 1; display: flex; align-items: center; justify-content: center; color: #999; font-size: 16px;">
        请选择一个用户查看聊天记录
      </div>
      <template v-else>
        <div style="font-size: 16px; font-weight: bold; padding: 15px; border-bottom: 1px solid #eee; background: #fafafa;">
          {{ data.selectedUser.userName }} 的咨询记录
        </div>
        <div ref="msgBox" style="flex: 1; overflow-y: auto; padding: 15px;">
          <div v-for="(msg, idx) in data.history" :key="idx" style="margin-bottom: 15px;">
            <!-- 用户问题 -->
            <div style="display: flex; justify-content: flex-end; margin-bottom: 5px;">
              <div style="max-width: 70%; background: #409eff; color: #fff; padding: 8px 12px; border-radius: 8px 0 8px 8px; font-size: 13px; line-height: 1.5;">
                {{ msg.question }}
              </div>
            </div>
            <!-- AI回复 -->
            <div style="display: flex; gap: 8px;">
              <div style="width: 28px; height: 28px; border-radius: 50%; background: #67c23a; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 11px; flex-shrink: 0;">AI</div>
              <div style="max-width: 70%; background: #f5f7fa; padding: 8px 12px; border-radius: 0 8px 8px 8px; font-size: 13px; line-height: 1.5; white-space: pre-wrap;">
                {{ msg.answer }}
              </div>
            </div>
            <div style="text-align: center; font-size: 11px; color: #ccc; margin-top: 3px;">{{ msg.time }}</div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, nextTick } from "vue"
import request from "@/utils/request"

const msgBox = ref(null)
const data = reactive({
  sessions: [],
  selectedUser: null,
  history: []
})

const loadSessions = () => {
  request.get('/chat/manage/sessions').then(res => {
    if (res.code === '200') data.sessions = res.data || []
  })
}

const selectUser = (session) => {
  data.selectedUser = session
  request.get('/chat/manage/history/' + session.userId).then(res => {
    if (res.code === '200') {
      data.history = res.data || []
      nextTick(() => { if (msgBox.value) msgBox.value.scrollTop = msgBox.value.scrollHeight })
    }
  })
}

loadSessions()
</script>
