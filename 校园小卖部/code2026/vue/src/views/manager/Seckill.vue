<template>
  <div>
    <div class="card" style="margin-bottom: 10px; display: flex; align-items: center; gap: 15px; padding: 15px;">
      <span style="font-weight: bold;">秒杀活动</span>
      <el-tag v-if="data.activeCount > 0" type="danger">进行中: {{ data.activeCount }}</el-tag>
      <div style="flex:1;"></div>
      <el-button type="danger" @click="handleAdd">+ 新建秒杀</el-button>
    </div>

    <div class="card">
      <el-table :data="data.tableData" stripe>
        <el-table-column label="商品" width="280">
          <template #default="scope">
            <div style="display: flex; gap: 10px; align-items: center;">
              <img :src="scope.row.goodsImg" style="width: 40px; height: 40px; border-radius: 4px;" />
              <div>
                <div style="font-weight: bold;">{{ scope.row.goodsName }}</div>
                <div style="font-size: 12px; color: #999;">原价: ￥{{ scope.row.seckillPrice ? '查看商品' : '-' }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="秒杀价" width="100">
          <template #default="scope"><b style="color: red; font-size: 16px;">￥{{ scope.row.seckillPrice }}</b></template>
        </el-table-column>
        <el-table-column label="库存" prop="totalStock" width="80"></el-table-column>
        <el-table-column label="开始时间" prop="startTime" width="160"></el-table-column>
        <el-table-column label="结束时间" prop="endTime" width="160"></el-table-column>
        <el-table-column label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === '进行中' ? 'danger' : scope.row.status === '未开始' ? 'warning' : 'info'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100" align="center">
          <template #default="scope">
            <el-button @click="handleDelete(scope.row)" type="danger" size="small">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="新建秒杀" width="450px" v-model="data.formVisible" :close-on-click-modal="false" destroy-on-close>
      <el-form ref="formRef" :model="data.form" label-width="100px" style="padding-right: 20px;">
        <el-form-item label="秒杀商品" prop="goodsId" :rules="[{required:true,message:'请选择商品'}]">
          <el-select v-model="data.form.goodsId" filterable placeholder="搜索商品" style="width:100%;">
            <el-option v-for="g in data.goodsList" :key="g.id" :label="g.name + ' (￥' + g.price + ')'" :value="g.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价" prop="seckillPrice" :rules="[{required:true,message:'请输入秒杀价'}]">
          <el-input-number :min="0.01" :max="99999" :precision="2" v-model="data.form.seckillPrice" style="width:100%;" />
        </el-form-item>
        <el-form-item label="秒杀库存" prop="totalStock" :rules="[{required:true,message:'请输入库存'}]">
          <el-input-number :min="1" :max="999" v-model="data.form.totalStock" style="width:100%;" />
        </el-form-item>
        <el-form-item label="开始时间" prop="startTime" :rules="[{required:true,message:'请选择时间'}]">
          <div style="display: flex; gap: 8px; width: 100%;">
            <el-date-picker v-model="data.form.startDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="flex:1;" />
            <el-time-picker v-model="data.form.startTime" placeholder="选择时间" format="HH:mm:ss" value-format="HH:mm:ss" style="flex:1;" />
          </div>
        </el-form-item>
        <el-form-item label="结束时间" prop="endTime" :rules="[{required:true,message:'请选择时间'}]">
          <div style="display: flex; gap: 8px; width: 100%;">
            <el-date-picker v-model="data.form.endDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" style="flex:1;" />
            <el-time-picker v-model="data.form.endTime" placeholder="选择时间" format="HH:mm:ss" value-format="HH:mm:ss" style="flex:1;" />
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="data.formVisible = false">取消</el-button>
        <el-button type="danger" @click="save">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue"
import request from "@/utils/request"
import { ElMessage, ElMessageBox } from "element-plus"

const data = reactive({
  tableData: [], goodsList: [], activeCount: 0,
  formVisible: false, form: {}
})

const load = () => {
  request.get('/seckill/list').then(res => {
    if (res.data) { data.tableData = res.data; data.activeCount = res.data.filter(a => a.status === '进行中').length }
  })
}
const loadGoods = () => { request.get('/goods/selectAll').then(res => { if (res.data) data.goodsList = res.data.filter((g) => g.status === '上架') }) }

const handleAdd = () => {
  data.form = { seckillPrice: 1, totalStock: 10 }
  data.formVisible = true
}

const save = () => {
  data.form.startTime = (data.form.startDate || '') + ' ' + (data.form.startTime || '')
  data.form.endTime = (data.form.endDate || '') + ' ' + (data.form.endTime || '')
  // 校验至少持续一天
  const diff = new Date(data.form.endTime) - new Date(data.form.startTime)
  if (diff < 86400000) { ElMessage.warning('秒杀活动至少持续一天'); return }
  request.post('/seckill/add', data.form).then(res => {
    if (res.code === '200') { ElMessage.success('创建成功'); data.formVisible = false; load() }
    else { ElMessage.error(res.msg) }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm('确定删除该秒杀活动?').then(() => {
    request.delete('/seckill/delete/' + row.id).then(res => {
      if (res.code === '200') { ElMessage.success('已删除'); load() }
      else { ElMessage.error(res.msg) }
    })
  }).catch(() => {})
}

onMounted(() => { load(); loadGoods() })
</script>

