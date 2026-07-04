<template>
  <div class="front-container">
    <!-- 统计卡片 -->
    <div style="display: flex; gap: 15px; margin-bottom: 20px;">
      <div class="card" style="flex: 1; padding: 20px;">
        <div style="color: #666; font-size: 14px; margin-bottom: 5px;">账户余额</div>
        <div style="color: red; font-size: 28px; font-weight: bold;">&yen;{{ data.user.account }}</div>
      </div>
      <div class="card" style="flex: 1; padding: 20px;">
        <div style="color: #666; font-size: 14px; margin-bottom: 5px;">累计充值</div>
        <div style="color: #409eff; font-size: 28px; font-weight: bold;">&yen;{{ data.stats.totalMoney }}</div>
      </div>
      <div class="card" style="flex: 1; padding: 20px;">
        <div style="color: #666; font-size: 14px; margin-bottom: 5px;">充值次数</div>
        <div style="color: #67c23a; font-size: 28px; font-weight: bold;">{{ data.stats.count }} 次</div>
      </div>
    </div>

    <div class="card" style="padding: 20px">
      <div style="margin-bottom: 20px;display: flex;align-items: center;">
        <div style="flex: 1;">
          <el-date-picker style="width: 250px;margin-right: 10px" v-model="data.time" type="date" placeholder="按日期查询" format="yyyy-MM-dd" value-format="yyyy-MM-dd" />
          <el-button type="primary" @click="load">筛选</el-button>
          <el-button @click="data.time=null; load()">重置</el-button>
        </div>
        <el-button type="danger" @click="handleAdd" size="large">+ 充值</el-button>
      </div>

      <el-table :data="data.tableData" stripe>
        <el-table-column label="充值金额" prop="money">
          <template #default="scope"><b style="color: red">{{ scope.row.money }}元</b></template>
        </el-table-column>
        <el-table-column label="支付方式" prop="type"></el-table-column>
        <el-table-column label="充值时间" prop="time"></el-table-column>
      </el-table>
      <div style="margin-top: 20px">
        <el-pagination @current-change="load" layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <!-- 充值弹窗 -->
    <el-dialog title="充值" width="400px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <div style="padding: 10px 0;">
        <div style="margin-bottom: 20px;">
          <div style="font-size: 14px; color: #666; margin-bottom: 10px;">选择金额</div>
          <div style="display: grid; grid-template-columns: repeat(4, 1fr); gap: 10px;">
            <div v-for="amt in [10,20,50,100]" :key="amt"
              @click="data.form.money = amt"
              :style="{
                padding: '12px 0', textAlign: 'center', borderRadius: '6px', cursor: 'pointer',
                border: '2px solid ' + (data.form.money === amt ? '#409eff' : '#e0e0e0'),
                background: data.form.money === amt ? '#f0f7ff' : '#fff',
                fontWeight: data.form.money === amt ? 'bold' : 'normal',
                color: data.form.money === amt ? '#409eff' : '#333', fontSize: '16px'
              }">{{ amt }}元</div>
          </div>
          <div style="margin-top: 10px;">
            <el-input-number :min="1" :max="9999" v-model="data.form.money" placeholder="自定义金额" style="width: 100%;" />
          </div>
        </div>
        <div>
          <div style="font-size: 14px; color: #666; margin-bottom: 10px;">支付方式</div>
          <div style="display: flex; gap: 10px;">
            <div v-for="pay in [{label:'微信支付',icon:'📱'},{label:'支付宝支付',icon:'💳'}]" :key="pay.label"
              @click="data.form.type = pay.label"
              :style="{
                flex: 1, padding: '12px 0', textAlign: 'center', borderRadius: '6px', cursor: 'pointer',
                border: '2px solid ' + (data.form.type === pay.label ? '#409eff' : '#e0e0e0'),
                background: data.form.type === pay.label ? '#f0f7ff' : '#fff'
              }">
              <div style="font-size: 24px;">{{ pay.icon }}</div>
              <div style="font-size: 13px; margin-top: 3px;">{{ pay.label }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="danger" @click="handleConfirmRecharge" :disabled="!data.form.money || !data.form.type" :loading="data.rechargeLoading">确认充值 {{ data.form.money }} 元</el-button>
      </template>
    </el-dialog>

    <!-- 扫码支付弹窗 -->
    <el-dialog title="扫码支付" width="360px" v-model="data.qrVisible" :close-on-click-modal="false" destroy-on-close>
      <div style="text-align: center; padding: 10px;">
        <div style="font-weight: bold; margin-bottom: 10px;">{{ data.form.type }}</div>
        <div style="background: #fff; width: 210px; margin: 0 auto 10px; padding: 5px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
          <img v-if="data.qrCodeUrl" :src="data.qrCodeUrl" style="width: 200px; height: 200px; display: block;" />
          <div v-else style="width: 200px; height: 200px; display: flex; align-items: center; justify-content: center; color: #999;">生成中...</div>
        </div>
        <div style="font-size: 20px; font-weight: bold; color: red; margin-bottom: 15px;">&yen;{{ data.form.money }}</div>
        <el-button type="primary" size="large" style="width: 100%;" @click="doRecharge" :loading="data.rechargeLoading">我已支付</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive, ref, nextTick} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
import QRCode from 'qrcode';

const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1, pageSize: 10, total: 0,
  formVisible: false, qrVisible: false, qrCodeUrl: '',
  form: { money: 10, type: '微信支付' },
  tableData: [], time: null,
  rechargeLoading: false,
  stats: { totalMoney: 0, count: 0 }
})

const loadAccount = () => {
  request.get('/user/selectById/' + data.user.id).then(res => { data.user.account = res.data.account })
}

const load = () => {
  request.get('/recharge/selectPage', {
    params: { pageNum: data.pageNum, pageSize: data.pageSize, userId: data.user.id, time: data.time }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
    let totalMoney = 0
    if (res.data?.list) { res.data.list.forEach(r => { totalMoney += Number(r.money || 0) }) }
    data.stats = { totalMoney, count: res.data?.total || 0 }
  })
}
load()

const handleAdd = () => {
  data.form = { money: 10, type: '微信支付' }
  data.formVisible = true
}

const handleConfirmRecharge = () => {
  data.formVisible = false
  const qrContent = window.location.origin + '/#/front/userRecharge?amount=' + data.form.money
  nextTick(() => {
    QRCode.toDataURL(qrContent, { width: 300, margin: 4 }).then(url => {
      data.qrCodeUrl = url; data.qrVisible = true; data.rechargeLoading = false
    }).catch(() => { data.qrVisible = true; data.qrCodeUrl = ''; data.rechargeLoading = false })
  })
}

const doRecharge = () => {
  data.rechargeLoading = true
  data.form.userId = data.user.id
  request.post('/recharge/add', data.form).then(res => {
    if (res.code === '200') { load(); loadAccount(); ElMessage.success('充值成功'); data.qrVisible = false }
    else { ElMessage.error(res.msg) }
  }).finally(() => { data.rechargeLoading = false })
}
</script>
