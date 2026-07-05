<template>
  <div class="front-container" style="width: 90%;">

    <div  style="margin-bottom: 10px;">
      <el-input clearable @clear="load" v-model="data.orderNo" style="width: 400px;height: 40px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
      <el-input clearable @clear="load" v-model="data.goodsName" style="width: 400px;height: 40px; margin-right: 10px" placeholder="请输入商品名称查询"></el-input>

      <el-button style="height: 40px;" type="primary" @click="load">查 询</el-button>
    </div>

    <div class="card" >
      <el-table :data="data.tableData" stripe :cell-style="{background:'#f5f7fa',color:'#606266'}" default-expand-all>
        <el-table-column type="expand">
          <template #default="props">
            <div style="padding: 10px;">
              <el-table :data="props.row.orderDetailList" border >
              <el-table-column label="商品图片" prop="goodsImg" width="100px">
                  <template #default="scope">
                    <img :src="scope.row.goodsImg" style="width: 50px;height: 50px; cursor: pointer;" @click="router.push('/front/goodsDetail?id=' + scope.row.goodsId)">
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" show-overflow-tooltip>
                  <template #default="scope">
                    <span style="cursor: pointer; color: #409eff;" @click="router.push('/front/goodsDetail?id=' + scope.row.goodsId)">{{ scope.row.goodsName }}</span>
                  </template>
                </el-table-column>
                <el-table-column label="商品单价" prop="goodsPrice" width="100px"></el-table-column>
                <el-table-column label="数量" prop="num">
                  <template #default="scope">
                    X {{scope.row.num}}
                  </template>
                </el-table-column>
                <el-table-column label="小计" width="150px">
                  <template #default="scope">
                    <b style="color: red;">{{(scope.row.goodsPrice * scope.row.num).toFixed(2)}} 元</b>
                  </template>
                </el-table-column>
                <el-table-column label="操作" width="120">
                  <template #default="scope">
                    <el-button @click="handleAddComment(scope.row)" v-if="props.row.status === '已完成'" type="success">评 价</el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="订单编号" prop="orderNo" width="240">
          <template #default="scope">
            <b style="color:#333;">{{scope.row.orderNo}}</b>
          </template>
        </el-table-column>
        <el-table-column label="总价格" prop="total">
          <template #default="scope">
            <b style="color: red">{{scope.row.total}} 元</b>
          </template>
        </el-table-column>
        <el-table-column label="配送类型" prop="deliverType"></el-table-column>
        <el-table-column label="支付方式" prop="payType"></el-table-column>
        <el-table-column label="状态" prop="status">
          <template #default="scope">
            <el-tag type="warning" v-if="scope.row.status === '待支付'">待支付</el-tag>
            <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
             <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
             <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
             <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
             <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
           </template>
         </el-table-column>
         <el-table-column label="下单时间" prop="time"></el-table-column>
        <el-table-column label="地址" prop="address" width="150"></el-table-column>
        <el-table-column label="配送信息" prop="deliver" width="150"></el-table-column>
       <el-table-column label="订单操作" align="center" width="120">
         <template #default="scope">
            <el-button @click="goPay(scope.row)" v-if="scope.row.status === '待支付'" type="warning" size="small">去支付</el-button>
            <el-button @click="cancel(scope.row)" v-if="scope.row.status === '待接单'" type="danger"> 取 消</el-button>

            
            <el-button @click="handleApplyRefund(scope.row)" v-if="scope.row.status === '待接单'|| scope.row.status === '已出货'|| scope.row.status === '已配送'|| scope.row.status === '已完成'" type="danger" size="small">申请售后</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px">
        <el-pagination @current-change="load"  layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>

        <!-- 售后申请弹窗 -->
    <el-dialog title="申请售后" width="45%" v-model="data.refundVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="refundFormRef" :model="data.refundForm" :rules="data.refundRules" label-width="100px" style="padding-right: 30px;padding-top: 20px">
        <el-form-item label="售后类型" prop="type">
          <el-radio-group v-model="data.refundForm.type">
            <el-radio value="仅退款">仅退款（未发货/未收到货）</el-radio>
            <el-radio value="退货退款">退货退款</el-radio>
            <el-radio value="换货">换货</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="退款金额" prop="amount">
          <el-input-number v-model="data.refundForm.amount" :precision="2" :min="0.01" :max="data.refundForm.maxAmount" :step="1" style="width: 200px"></el-input-number>
          <span style="margin-left: 10px; color: #999">订单总额：¥{{ data.refundForm.maxAmount }}</span>
        </el-form-item>
        <el-form-item label="退款原因" prop="reason">
          <el-select v-model="data.refundForm.reason" placeholder="请选择退款原因" style="width: 100%">
            <el-option label="商品质量问题" value="商品质量问题"></el-option>
            <el-option label="商品与描述不符" value="商品与描述不符"></el-option>
            <el-option label="收到商品破损" value="收到商品破损"></el-option>
            <el-option label="发错货/少发货" value="发错货/少发货"></el-option>
            <el-option label="不想要了" value="不想要了"></el-option>
            <el-option label="其他原因" value="其他原因"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述">
          <el-input type="textarea" :rows="4" v-model="data.refundForm.description" placeholder="请详细描述您的问题，有助于更快处理" />
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.refundVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitRefund">提 交</el-button>
      </span>
      </template>
    </el-dialog>
<el-dialog title="评价信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px;padding-top: 20px">
        <el-form-item label="评分" prop="score">
          <el-rate show-score allow-half v-model="data.form.score"></el-rate>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input type="textarea" :rows="3" v-model="data.form.content" autocomplete="off" placeholder="请输入评论" />
        </el-form-item>

      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="data.formVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">保 存</el-button>
      </span>
      </template>
    </el-dialog>


  </div>
</template>

<script setup>
import request from "@/utils/request";
import {useRouter} from "vue-router";
import {reactive,ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";
const router = useRouter();

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 3,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  orderNo: null,
  goodsName:null,
      refundVisible: false,
      refundForm: {},
      refundRules:{
        type:[
          {required:true,message:'请选择售后类型',trigger:'change'}
        ],
        amount:[
          {required:true,message:'请输入退款金额',trigger:'blur'}
        ],
        reason:[
          {required:true,message:'请选择退款原因',trigger:'change'}
        ]
      },
  rules:{
    content:[
      {required:true,message:'请输入内容',trigger:'blur'}
    ],
    score:[
      {required:true,message:'请选择评分',trigger:'change'}
    ]
  }
})

// 分页查询
const load = () => {
  request.get('/orders/selectPage', {
    params: {
      pageNum: data.pageNum,
      pageSize: data.pageSize,
      orderNo: data.orderNo,
      goodsName:data.goodsName,
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()

const cancel = (row) => {
  ElMessageBox.confirm('您确认取消订单吗?', '二次确认', { type: 'warning' }).then(res => {
    data.form =row
    data.form.status = '已取消'
    updateOrder()
  }).catch(err => {})
}

const done = (row) => {
  ElMessageBox.confirm('您确认已收到订单货物了吗?', '二次确认', { type: 'warning' }).then(res => {
    data.form =row
    data.form.status = '已完成'
    updateOrder()
}).catch(err => {})
}

const goPay = (row) => {
  router.push('/front/payment?orderId=' + row.id)
}

// 编辑保存
const updateOrder = () => {
  request.put('/orders/update', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 新增
const handleAddComment = (row) => {
    request.get('/comment/selectAll', {
      params: {
        orderId: row.orderId,
        goodsId: row.goodsId,

      }
    }).then(res => {
      data.form = res.data?.length > 0 ? res.data[0] : {
        orderId: row.orderId,
        goodsId: row.goodsId,
        userId: data.user.id,
      }
      data.formVisible = true
    })
}


// 新增保存
const addComment = () => {
  request.post('/comment/add', data.form).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const updateComment = () => {
  request.put('/comment/update', data.form).then(res => {
    if (res.code === '200') {

      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 弹窗保存
const save = () => {
  formRef.value.validate(valid => {
    if (valid) {
      data.form.id ? updateComment() : addComment()
    }
  })
}

// 删除
const handleDelete = (id) => {
  ElMessageBox.confirm('删除后数据无法恢复，您确定删除吗?', '删除确认', { type: 'warning' }).then(res => {
    request.delete('/orders/delete/' + id).then(res => {
      if (res.code === '200') {
        load()
        ElMessage.success('操作成功')
      } else {
        ElMessage.error(res.msg)
      }
    })
  }).catch(err => {})
}

// 重置
const reset = () => {
  data.orderNo = null
  load()
}


const refundFormRef = ref()

const handleApplyRefund = (row) => {
  data.refundForm = {
    orderId: row.id,
    maxAmount: row.total,
    amount: row.total,
    type: '仅退款',
    reason: '',
    description: ''
  }
  data.refundVisible = true
}

const submitRefund = () => {
  refundFormRef.value.validate(valid => {
    if (valid) {
      request.post('/refundOrders/add', data.refundForm).then(res => {
        if (res.code === '200') {
          ElMessage.success('售后申请已提交，请等待审核')
          data.refundVisible = false
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}
</script>

<style scoped>
.el-tag{
  font-weight: bold;
}
.el-tag--warning{
  color: orange;

  background-color: rgba(255, 165, 0, 0.1);
}
</style>
