<template>
  <div class="front-container">
    <div class="card" style="padding: 20px;">
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px;">
        <div style="font-size: 22px; font-weight: bold;">商品对比</div>
        <div>
          <el-button @click="clearAll" size="small" type="danger" plain>清空对比</el-button>
          <el-button @click="$router.push('/front/goods')" size="small">继续添加</el-button>
        </div>
      </div>

      <div v-if="data.goodsList.length < 2" style="text-align: center; padding: 80px 0; color: #999;">
        <div style="font-size: 48px; margin-bottom: 10px;">📋</div>
        <div style="font-size: 16px;">请至少选择2个商品进行对比</div>
        <el-button @click="$router.push('/front/goods')" style="margin-top: 15px;" type="primary">去商品列表选择</el-button>
      </div>

      <div v-else>
        <!-- 商品头部卡片 -->
        <div style="display: flex; gap: 15px; margin-bottom: 20px;">
          <div style="width: 120px; flex-shrink: 0;"></div>
          <div v-for="g in data.goodsList" :key="g.id" style="flex: 1; text-align: center; background: #fafafa; border-radius: 8px; padding: 15px; border: 1px solid #eee;">
            <img :src="g.img" style="width: 120px; height: 120px; object-fit: cover; border-radius: 5px; cursor: pointer;" @click="$router.push('/front/goodsDetail?id=' + g.id)" />
            <div style="font-weight: bold; margin: 8px 0; cursor: pointer; color: #409eff;" @click="$router.push('/front/goodsDetail?id=' + g.id)">{{ g.name }}</div>
            <div style="color: red; font-size: 18px; font-weight: bold;">￥{{ g.price }}</div>
            <el-button @click="remove(g.id)" size="small" type="danger" link style="margin-top: 5px;">移除</el-button>
          </div>
        </div>

        <!-- 对比属性表 -->
        <el-table :data="data.rows" border style="width: 100%;">
          <el-table-column label="属性" prop="label" width="120" align="center"></el-table-column>
          <el-table-column v-for="(g, idx) in data.goodsList" :label="'商品' + (idx + 1)" :key="g.id" align="center">
            <template #default="scope">
              <template v-if="scope.row.key === 'description'">
                <div style="text-align: left; line-height: 1.6; max-height: 120px; overflow-y: auto;">{{ g.description || '无' }}</div>
              </template>
              <template v-else-if="scope.row.key === 'store'">
                <span :style="{ color: g.store < 10 ? 'red' : '#333', fontWeight: g.store < 10 ? 'bold' : 'normal' }">{{ g.store }}</span>
              </template>
              <template v-else>
                {{ g[scope.row.key] ?? '无' }}
              </template>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from "vue"
import { useRouter } from "vue-router"
import request from "@/utils/request"
import { getCompareList, removeFromCompare, clearCompare } from "@/utils/compare"
import { ElMessage, ElMessageBox } from "element-plus"

const router = useRouter()
const data = reactive({
  goodsList: [],
  rows: [
    { label: '商品信息', key: 'description' },
    { label: '库存', key: 'store' },
    { label: '销量', key: 'saleCount' },
    { label: '分类', key: 'categoryName' },
    { label: '上架时间', key: 'time' },
  ]
})

const loadData = () => {
  const ids = getCompareList()
  if (ids.length === 0) {
    data.goodsList = []
    return
  }
  request.get('/goods/selectAll').then(res => {
    if (res.data) {
      data.goodsList = res.data.filter(g => ids.includes(g.id))
    }
  })
}

const remove = (id) => {
  removeFromCompare(id)
  ElMessage.success('已移除')
  loadData()
}

const clearAll = () => {
  ElMessageBox.confirm('确定清空对比列表?').then(() => {
    clearCompare()
    ElMessage.success('已清空')
    data.goodsList = []
  }).catch(() => {})
}

onMounted(loadData)
</script>
