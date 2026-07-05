<template>
  <div>
    <div class="card" style="margin-bottom: 5px;">
      <el-select v-model="data.statusFilter" clearable placeholder="筛选状态" style="width: 150px; margin-right: 10px" @change="load">
        <el-option label="待审核" value="待审核"></el-option>
        <el-option label="已通过" value="已通过"></el-option>
        <el-option label="已退款" value="已退款"></el-option>
        <el-option label="已拒绝" value="已拒绝"></el-option>
      </el-select>
      <el-button type="primary" @click="load">查询</el-button>
      <el-button type="info" style="margin: 0 10px" @click="reset">重置</el-button>
    </div>

    <div class="card">
      <el-table :data="data.tableData" stripe :cell-style="{background:'#f5f7fa',color:'#606266'}">
        <el-table-column label="售后编号" prop="id" width="80"></el-table-column>
        <el-table-column label="订单编号" prop="orderNo" width="220"></el-table-column>
        <el-table-column label="用户名" prop="userName"></el-table-column>
        <el-table-column label="售后类型" prop="type" width="100"></el-table-column>
        <el-table-column label="退款金额" prop="amount" width="100">
          <template #default="scope">
            <b style="color: red;">¥{{ scope.row.amount }}</b>
          </template>
        </el-table-column>
        <el-table-column label="退款原因" prop="reason" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column label="状态" prop="status" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === '已退款' ? 'success' : scope.row.status === '已拒绝' ? 'danger' : scope.row.status === '已通过' ? 'primary' : 'warning'">{{ scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" prop="time" width="170"></el-table-column>
        <el-table-column label="管理员回复" prop="reply" width="150" show-overflow-tooltip></el-table-column>
        <el-table-column label="操作" align="center" width="230" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === '待审核'" type="success" size="small" @click="handleApprove(scope.row)">通过</el-button>
            <el-button v-if="scope.row.status === '待审核'" type="danger" size="small" @click="handleReject(scope.row)">拒绝</el-button>
            <el-button v-if="scope.row.status === '已通过'" type="primary" size="small" @click="handleRefund(scope.row)">确认退款</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)" v-if="scope.row.status === '已拒绝' || scope.row.status === '已退款'">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px">
        <el-pagination @current-change="load" background layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

    <!-- 审核回复弹窗 -->
    <el-dialog title="审核售后" width="40%" v-model="data.dialogVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" label-width="80px" style="padding-right: 30px;padding-top: 20px">
        <div style="margin-bottom: 15px; padding: 10px; background: #f5f7fa; border-radius: 5px;">
          <div><b>售后类型：</b>{{ data.form.type }}</div>
          <div><b>退款金额：</b>¥{{ data.form.amount }}</div>
          <div><b>退款原因：</b>{{ data.form.reason }}</div>
          <div v-if="data.form.description"><b>问题描述：</b>{{ data.form.description }}</div>
        </div>
        <el-form-item label="审核操作" prop="action" v-if="data.actionType === 'approve'">
          <span style="color: green; font-weight: bold;">确认通过该售后申请？</span>
        </el-form-item>
        <el-form-item label="审核操作" prop="action" v-if="data.actionType === 'reject'">
          <span style="color: red; font-weight: bold;">确认拒绝该售后申请？</span>
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input type="textarea" :rows="4" v-model="data.form.reply" :placeholder="data.actionType === 'approve' ? '审核通过，可填写回复说明（选填）' : '请填写拒绝原因'" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="confirmAction">{{ data.actionType === 'approve' ? '通过' : '拒绝' }}</el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import request from "@/utils/request";
import {reactive} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const data = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0,
  tableData: [],
  statusFilter: null,
  dialogVisible: false,
  actionType: 'approve',
  form: {}
})

const load = () => {
  request.get('/refundOrders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      status: data.statusFilter
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const reset = () => {
  data.statusFilter = null
  load()
}

const handleApprove = (row) => {
  data.actionType = 'approve'
  data.form = { ...row, reply: '' }
  data.dialogVisible = true
}

const handleReject = (row) => {
  data.actionType = 'reject'
  data.form = { ...row, reply: '' }
  data.dialogVisible = true
}

const confirmAction = () => {
  if (data.actionType === 'approve') {
    request.put('/refundOrders/approve', null, {
      params: { id: data.form.id, reply: data.form.reply || '' }
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已通过该售后申请')
        data.dialogVisible = false
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  } else {
    request.put('/refundOrders/reject', null, {
      params: { id: data.form.id, reply: data.form.reply || '' }
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('已拒绝该售后申请')
        data.dialogVisible = false
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }
}

const handleRefund = (row) => {
  ElMessageBox.confirm('确认将 ¥' + row.amount + ' 退回到用户余额吗？', '退款确认', { type: 'warning' }).then(res => {
    request.put('/refundOrders/refund', null, {
      params: { id: row.id }
    }).then(res => {
      if (res.code === '200') {
        ElMessage.success('退款成功，金额已退回用户余额')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗？', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/refundOrders/delete/' + id).then(res => {
      if (res.code === '200') {
        ElMessage.success('操作成功')
        load()
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}
</script>
