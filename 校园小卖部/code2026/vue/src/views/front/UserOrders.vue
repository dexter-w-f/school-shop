<template>
  <div class="front-container">

    <div  style="margin-bottom: 20px;">
      <el-input clearable @clear="load" v-model="data.orderNo" style="width: 400px;height: 40px; margin-right: 10px" placeholder="请输入订单编号查询"></el-input>
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
                    <img :src="scope.row.goodsImg" style="width: 50px;height: 50px">
                  </template>
                </el-table-column>
                <el-table-column label="商品名称" prop="goodsName" show-overflow-tooltip></el-table-column>
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
         <el-table-column label="状态" prop="status">
           <template #default="scope">
             <el-tag type="danger" v-if="scope.row.status === '已取消'">已取消</el-tag>
             <el-tag type="warning" v-if="scope.row.status === '待接单'">待接单</el-tag>
             <el-tag type="primary" v-if="scope.row.status === '已配送'">已配送</el-tag>
             <el-tag type="primary" v-if="scope.row.status === '已出货'">已出货</el-tag>
             <el-tag type="success" v-if="scope.row.status === '已完成'">已完成</el-tag>
           </template>
         </el-table-column>
         <el-table-column label="下单时间" prop="time"></el-table-column>

        <el-table-column label="订单操作" align="center" width="120">
          <template #default="scope">
            <el-button v-if="scope.row.status === '待接单'" type="danger"> 取 消</el-button>
            <el-button v-if="scope.row.status === '待收货'" type="primary">确认收货</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 20px">
        <el-pagination @current-change="load"  layout="total, prev, pager, next" v-model:page-size="data.pageSize" v-model:current-page="data.pageNum" :total="data.total"/>
      </div>
    </div>



    <el-dialog title="订单信息" width="30%" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" :rules="data.rules" label-width="80px" style="padding-right: 30px;padding-top: 20px">

        <el-form-item label="名称" prop="name">
          <el-input v-model="data.form.name" autocomplete="off" />
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
import {reactive,ref} from "vue";
import {ElMessageBox, ElMessage} from "element-plus";

const formRef = ref()
const data = reactive({
  user: JSON.parse(localStorage.getItem('system-user') || '{}'),
  pageNum: 1,
  pageSize: 3,
  total: 0,
  formVisible: false,
  form: {},
  tableData: [],
  name: null,
  rules:{
    name:[
      {required:true,message:'请输入名称',trigger:'blur'}
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
      userId: data.user.id
    }
  }).then(res => {
    data.tableData = res.data?.list
    data.total = res.data?.total
  })
}
load()
// 新增
const handleAdd = () => {
  data.form = {}
  data.formVisible = true
}

// 编辑
const handleEdit = (row) => {
  data.form = JSON.parse(JSON.stringify(row))
  data.formVisible = true
}

// 新增保存
const add = () => {
  request.post('/orders/add', data.form).then(res => {
    if (res.code === '200') {
      load()
      ElMessage.success('操作成功')
      data.formVisible = false
    } else {
      ElMessage.error(res.msg)
    }
  })
}

// 编辑保存
const update = () => {
  request.put('/orders/update', data.form).then(res => {
    if (res.code === '200') {
      load()
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
      data.form.id ? update() : add()
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