<template>
  <div>
    <div class="card" style="margin-bottom: 10px; padding: 15px; display: flex; align-items: center; gap: 15px;">
      <div style="flex: 1;">
        <el-tag type="danger" v-if="data.lowStockCount > 0" style="margin-right: 10px;">低库存预警: {{ data.lowStockCount }} 件</el-tag>
        <el-tag type="success" v-else style="margin-right: 10px;">库存正常</el-tag>
        <span style="color: #666;">共 {{ data.tableData.length }} 件商品</span>
      </div>
      <el-input v-model="data.search" placeholder="搜索商品名称" style="width: 250px;" prefix-icon="Search" clearable @clear="load" @keyup.enter="load" />
    </div>

    <div class="card">
      <el-table :data="data.tableData" stripe @sort-change="handleSort">
        <el-table-column label="商品图片" width="80">
          <template #default="scope"><img :src="scope.row.img" style="width: 40px; height: 40px; border-radius: 4px;" /></template>
        </el-table-column>
        <el-table-column label="商品名称" prop="name" sortable="custom"></el-table-column>
        <el-table-column label="类目" prop="categoryName"></el-table-column>
        <el-table-column label="当前库存" prop="store" sortable="custom" width="120">
          <template #default="scope">
            <span :style="{ color: scope.row.store < 10 ? 'red' : scope.row.store < 30 ? '#e6a23c' : '#67c23a', fontWeight: scope.row.store < 10 ? 'bold' : 'normal' }">
              {{ scope.row.store }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="累计销量" prop="saleCount" sortable="custom" width="100"></el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="scope">
            <el-tag :type="scope.row.store < 10 ? 'danger' : scope.row.store < 30 ? 'warning' : 'success'" size="small" effect="plain">
              {{ scope.row.store < 10 ? '预警' : scope.row.store < 30 ? '偏低' : '正常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button @click="showAdjust(scope.row, 'in')" type="success" size="small">入库</el-button>
            <el-button @click="showAdjust(scope.row, 'out')" type="warning" size="small">出库</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog title="调整库存" width="380px" v-model="data.dialogVisible" :close-on-click-modal="false" destroy-on-close>
      <div style="padding: 10px 0;">
        <div style="display: flex; gap: 15px; align-items: center; margin-bottom: 20px;">
          <img :src="data.currentGoods?.img" style="width: 60px; height: 60px; border-radius: 4px;" />
          <div>
            <div style="font-weight: bold;">{{ data.currentGoods?.name }}</div>
            <div style="color: #666; font-size: 13px;">当前库存: <b>{{ data.currentGoods?.store }}</b></div>
          </div>
        </div>
        <el-form label-width="80px">
          <el-form-item :label="data.adjustType === 'in' ? '入库数量' : '出库数量'">
            <el-input-number :min="1" :max="9999" v-model="data.adjustNum" style="width: 200px;" />
          </el-form-item>
        </el-form>
        <div v-if="data.adjustNum > 0" style="background: #f5f7fa; padding: 10px; border-radius: 6px; font-size: 13px;">
          调整后库存: <b :style="{ color: (data.adjustType === 'in' ? data.currentGoods?.store + data.adjustNum : data.currentGoods?.store - data.adjustNum) < 0 ? 'red' : '#333' }">
            {{ data.adjustType === 'in' ? data.currentGoods?.store + data.adjustNum : data.currentGoods?.store - data.adjustNum }}
          </b>
        </div>
      </div>
      <template #footer>
        <el-button @click="data.dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAdjust" :loading="data.loading">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue"
import request from "@/utils/request"
import { ElMessage } from "element-plus"

const data = reactive({
  tableData: [],
  search: '',
  lowStockCount: 0,
  dialogVisible: false,
  currentGoods: null,
  adjustType: 'in',
  adjustNum: 1,
  loading: false,
  sortField: null,
  sortOrder: null
})

const load = () => {
  request.get('/inventory/list').then(res => {
    if (res.data) {
      let list = res.data
      if (data.search) list = list.filter(g => g.name.includes(data.search))
      list = list.filter(g => g.status === '上架')
      data.lowStockCount = list.filter(g => g.store < 10).length
      data.tableData = list
    }
  })
}

const handleSort = ({ prop, order }) => {
  if (!order) { load(); return }
  data.tableData.sort((a, b) => {
    const va = a[prop] || 0, vb = b[prop] || 0
    return order === 'ascending' ? va - vb : vb - va
  })
}

const showAdjust = (goods, type) => {
  data.currentGoods = goods
  data.adjustType = type
  data.adjustNum = 1
  data.dialogVisible = true
}

const confirmAdjust = () => {
  if (data.adjustNum < 1) { ElMessage.warning('请输入有效数量'); return }
  const change = data.adjustType === 'in' ? data.adjustNum : -data.adjustNum
  const newStore = data.currentGoods.store + change
  if (newStore < 0) { ElMessage.warning('库存不能为负数'); return }

  data.loading = true
  request.put('/inventory/update', null, {
    params: { goodsId: data.currentGoods.id, store: newStore }
  }).then(res => {
    if (res.code === '200') {
      ElMessage.success(data.adjustType === 'in' ? '入库成功' : '出库成功')
      data.dialogVisible = false
      load()
    } else {
      ElMessage.error(res.msg)
    }
  }).finally(() => { data.loading = false })
}

onMounted(load)
</script>
