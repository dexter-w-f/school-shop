<template>
  <div>
    <div style="display: flex;grid-gap: 10px">
      <div style="flex: 1;display: flex;padding: 20px" class="card">
        <div style="flex: 1;font-size: 20px">销售总额</div>
        <div style="flex: 1;font-weight: bold;font-size: 20px;color: red">￥{{ data.count.total }}</div>
      </div>
      <div style="flex: 1;display: flex;padding: 20px" class="card">
        <div style="flex: 1;font-size: 20px">今日销售额</div>
        <div style="flex: 1;font-weight: bold;font-size: 20px;color: #30065e">￥{{ data.count.today }}</div>
      </div>
      <div style="flex: 1;display: flex;padding: 20px" class="card">
        <div style="flex: 1;font-size: 20px">商品总数</div>
        <div style="flex: 1;font-weight: bold;font-size: 20px;color: #5993b6">{{ data.count.goods }}</div>
      </div>
      <div style="flex: 1;display: flex;padding: 20px" class="card">
        <div style="flex: 1;font-size: 20px">注册用户</div>
        <div style="flex: 1;font-weight: bold;font-size: 20px;color: #b35b1c">{{ data.count.user }}</div>
      </div>
    </div>

    <div style="margin-top: 10px;display: flex;grid-gap: 10px"></div>
    <div id="line" style="flex: 1;padding: 20px;height: 500px" class="card"></div>
    <div id="pie" style="flex: 1;padding: 20px;height: 500px" class="card"></div>

  </div>
</template>

<script setup>
import {reactive, onMounted} from "vue";
import request from "@/utils/request";
import * as echarts from 'echarts';
const data = reactive({
  count: {
    total: 0,
    today: 0,
    goods: 0,
    user: 0
  }
})

onMounted(() => {
  request.get('/count').then(res => {
    if (res.data) {
      data.count = res.data
    }
  }).catch(err => {
    console.error('获取统计数据失败:', err)
  })
})

const lineOption = {
  title: {
    text: '近一周销售统计',
    left: 'center'
  },
  tooltip: {
    trigger: 'axis'
  },
  legend: {
    left: 'left',
  },
  xAxis: {
    name: '日期',
    type: 'category',
    data: []
  },
  yAxis: {
    name: '销售额（元）',
    type: 'value'
  },
  grid: {
    top: '20%',
    bottom: '10%',
  },
  series: [
    {
      name: '销售量',
      type: 'line',
      data: [],
      smooth: true,
      areaStyle: {
        opacity: 0.8,
        color: 'rgb(185,190,255)'
      },
      markPoint: {
        data: [
          { type: 'max', name: 'Max' },
          { type: 'min', name:'Min'}
        ]
      },
      markLine: {
        data: [
          { type: 'average', name: 'Average' }
        ]
      }
    },
  ]
}

const pieOption = {
  title: {
    text: '商品销售占比',
    subtext: '比例图',
    left: 'center'
  },
  tooltip: {
    formatter: '{a} <br/>{b}: {c}元 ({d}%)',
    trigger: 'item'
  },
  legend: {
    orient: 'vertical',
    left: 'left',
  },
  series: [
    {
      name: '销售额',
      type: 'pie',
      center: ['50%', '50%'],
      radius: '50%',
      data: [],
      label: {
        show: true,
        formatter(params){
          return params.name + ': ' + params.percent + '%'
        }
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
        }
      }
    }
  ]
}

onMounted(() => {
  //折线图
  let lineDom = document.getElementById('line')
  let lineChart = echarts.init(lineDom)
  request.get('/selectLine').then(res => {
    lineOption.xAxis.data = res.data.date
    lineOption.series[0].data = res.data.count
    lineChart.setOption(lineOption)
  })
  //饼图
  let pieDom = document.getElementById('pie')
  let pieChart = echarts.init(pieDom)
  request.get('/selectPie').then(res => {
    pieOption.series[0].data = res.data
    pieChart.setOption(pieOption)

  })
})
</script>