<template>
  <div class="front-container" style="max-width: 600px; margin: 0 auto;">
    <div class="card" style="padding: 30px;" v-if="data.order.id">
      <div style="font-size: 22px; font-weight: bold; text-align: center; margin-bottom: 30px;">
        确认支付
      </div>

      <!-- 订单信息 -->
      <div style="background: #f8f9fa; padding: 15px; border-radius: 8px; margin-bottom: 25px;">
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: #666;">订单编号</span>
          <span style="font-weight: bold;">{{ data.order.orderNo }}</span>
        </div>
        <div style="display: flex; justify-content: space-between; margin-bottom: 8px;">
          <span style="color: #666;">下单时间</span>
          <span>{{ data.order.time }}</span>
        </div>
        <div style="display: flex; justify-content: space-between; border-top: 1px dashed #ddd; padding-top: 10px;">
          <span style="font-size: 16px;">应付金额</span>
          <span style="color: red; font-size: 28px; font-weight: bold;">￥{{ data.order.total }}</span>
        </div>
      </div>

      <!-- 支付方式 -->
      <div style="margin-bottom: 25px;">
        <div style="font-size: 16px; font-weight: bold; margin-bottom: 12px;">选择支付方式</div>
        <div style="display: flex; grid-gap: 12px;">
          <div
            @click="data.selectedPay = '余额支付'"
            :style="{
              flex: 1, padding: '15px', border: '2px solid ' + (data.selectedPay === '余额支付' ? '#409eff' : '#e0e0e0'),
              borderRadius: '8px', cursor: 'pointer', textAlign: 'center',
              background: data.selectedPay === '余额支付' ? '#f0f7ff' : '#fff'
            }"
          >
            <div style="font-size: 24px; margin-bottom: 5px;">🏦</div>
            <div style="font-weight: bold;">余额支付</div>
            <div style="font-size: 12px; color: #999; margin-top: 3px;">账户余额支付</div>
          </div>
          <div
            @click="data.selectedPay = '支付宝'"
            :style="{
              flex: 1, padding: '15px', border: '2px solid ' + (data.selectedPay === '支付宝' ? '#1677ff' : '#e0e0e0'),
              borderRadius: '8px', cursor: 'pointer', textAlign: 'center',
              background: data.selectedPay === '支付宝' ? '#e6f4ff' : '#fff'
            }"
          >
            <div style="font-size: 24px; margin-bottom: 5px;">💳</div>
            <div style="font-weight: bold; color: #1677ff;">支付宝</div>
            <div style="font-size: 12px; color: #999; margin-top: 3px;">模拟扫码支付</div>
          </div>
          <div
            @click="data.selectedPay = '微信支付（模拟）'"
            :style="{
              flex: 1, padding: '15px', border: '2px solid ' + (data.selectedPay === '微信支付（模拟）' ? '#07c160' : '#e0e0e0'),
              borderRadius: '8px', cursor: 'pointer', textAlign: 'center',
              background: data.selectedPay === '微信支付（模拟）' ? '#e6fff0' : '#fff'
            }"
          >
            <div style="font-size: 24px; margin-bottom: 5px;">📱</div>
            <div style="font-weight: bold; color: #07c160;">微信支付</div>
            <div style="font-size: 12px; color: #999; margin-top: 3px;">模拟扫码支付</div>
          </div>
        </div>
      </div>

     <!-- 支付宝/微信支付（模拟）弹窗 -->
      <el-dialog title="扫码支付" width="380px" v-model="data.simulateVisible" :close-on-click-modal="false" destroy-on-close>
        <div style="text-align: center; padding: 10px;">
          <div style="font-size: 18px; font-weight: bold; margin-bottom: 5px;">
            {{ data.selectedPay === '支付宝' ? '支付宝' : '微信' }}扫码支付
          </div>
          <div style="color: #999; font-size: 12px; margin-bottom: 12px;">请使用{{ data.selectedPay === '支付宝' ? '支付宝' : '微信' }}扫码完成支付</div>
          <!-- 二维码区域 -->
          <div style="background: #fff; border: 8px solid #fff; width: 220px; margin: 0 auto 12px; border-radius: 4px; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">
            <div style="position: relative;">
              <img v-if="data.qrCodeUrl" :src="data.qrCodeUrl" style="width: 220px; height: 220px; display: block;" alt="支付二维码" />
              <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 32px; height: 32px; background: #fff; border-radius: 4px; display: flex; align-items: center; justify-content: center; font-size: 18px;">
                {{ data.selectedPay === '支付宝' ? '💳' : '📱' }}
              </div>
              <div v-if="!data.qrCodeUrl" style="width: 220px; height: 220px; display: flex; align-items: center; justify-content: center; background: #f5f5f5; color: #999; font-size: 14px;">
                二维码加载中...
              </div>
            </div>
          </div>
          <div style="margin-bottom: 12px;">
            <div style="font-size: 12px; color: #999;">订单金额</div>
            <div style="font-size: 24px; font-weight: bold; color: red;">￥{{ data.order.total }}</div>
            <div style="font-size: 11px; color: #999; margin-top: 3px;">订单号: {{ data.order.orderNo }}</div>
          </div>
          <div style="font-size: 12px; color: #999; margin-top: 8px;" v-if="data.selectedPay === '支付宝' && data.polling">\n          <el-icon style="margin-right: 3px;"><Loading /></el-icon>等待支付结果中...\n        </div>\n        <el-button type="primary" size="large" style="width: 100%;" @click="confirmPay" :loading="data.paying">
            我已支付
          </el-button>
        </div>
      </el-dialog>

      <!-- 确认支付按钮 -->
      <el-button
        type="danger"
        size="large"
        style="width: 100%; height: 50px; font-size: 18px;"
        @click="handlePay"
        :loading="data.paying"
      >
        确认支付 ￥{{ data.order.total }}
      </el-button>

      <div style="text-align: center; margin-top: 15px;">
        <el-button @click="router.push('/front/userOrders')" link>返回订单列表</el-button>
      </div>
    </div>

    <div v-else style="text-align: center; padding: 50px; color: #999;">
      加载订单信息中...
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted, onUnmounted } from "vue";
import { useRouter, useRoute } from "vue-router";
import request from "@/utils/request";
import { ElMessage } from "element-plus";
import QRCode from 'qrcode';

const router = useRouter();
const route = useRoute();

const data = reactive({
  order: {},
  selectedPay: '余额支付',
  paying: false,
  simulateVisible: false,
  qrCodeUrl: ''
});

const loadOrder = () => {
  const orderId = route.query.orderId;
  if (!orderId) {
    ElMessage.error("订单参数缺失");
    router.push('/front/userOrders');
    return;
  }
  request.get('/orders/selectById/' + orderId).then(res => {
    if (res.data) {
      data.order = res.data;
      // 加载订单后生成二维码
      generateQRCode(res.data);
    } else {
      ElMessage.error("订单不存在");
      router.push('/front/userOrders');
    }
  }).catch(() => {
    ElMessage.error("加载订单失败");
    router.push('/front/userOrders');
  });
};

  const generateQRCode = (order) => {
    // 二维码内容：本机地址的支付链接，扫码可识别为网址
    const qrContent = window.location.origin + '/#/front/payment?orderNo=' + order.orderNo;
    QRCode.toDataURL(qrContent, {
      width: 300,
      margin: 4,
      color: { dark: '#000000', light: '#ffffff' }
  }).then(url => {
    data.qrCodeUrl = url;
  }).catch(err => {
    console.error('二维码生成失败:', err);
  });
};

const handlePay = () => {
  if (data.selectedPay === '余额支付') {
    // 余额支付直接确认
    confirmPay();
  } else if (data.selectedPay === '支付宝') {
    // 真实支付宝扫码支付
    handleAlipayPay();
  } else {
    // 微信支付（模拟）显示弹窗
    data.simulateVisible = true;
  }
};

const startPolling = (orderId) => {
  data.polling = true;
  const timer = setInterval(() => {
    request.get('/payment/queryStatus', {
      params: { orderId: orderId }
    }).then(res => {
      if (res.data?.status === 'SUCCESS') {
        clearInterval(timer);
        data.polling = false;
        data.simulateVisible = false;
        ElMessage.success('支付成功！');
        router.push('/front/userOrders');
      }
    }).catch(() => {});
  }, 3000);
  window.__alipayPollingTimer = timer;
};

const handleAlipayPay = () => {
  data.paying = true;
  request.get('/payment/alipayPay', {
    params: { orderId: data.order.id }
  }).then(res => {
    if (res.code === '200' && res.data?.qrCode) {
      QRCode.toDataURL(res.data.qrCode, {
        width: 300,
        margin: 4,
        color: { dark: '#1677ff', light: '#ffffff' }
      }).then(url => {
        data.qrCodeUrl = url;
        data.simulateVisible = true;
        startPolling(data.order.id);
      });
    } else {
      ElMessage.error('获取支付宝支付二维码失败，请重试');
    }
  }).catch(() => {
    ElMessage.error('支付服务异常，请稍后再试');
  }).finally(() => {
    data.paying = false;
  });
};

const confirmPay = () => {
  if (data.paying) return;
  data.paying = true;

  request.post('/payment/pay', {
    orderId: data.order.id,
    payType: data.selectedPay
    }).then(res => {
        if (res.code === '200') {
            data.simulateVisible = false;
            ElMessage.success('支付成功！');
            router.push('/front/userOrders');
        } else {
      ElMessage.error(res.msg);
    }
    }).catch(err => {
        ElMessage.error(err?.response?.data?.msg || '请求失败，请检查后端是否已重启');
        console.error('支付错误:', err);
    }).finally(() => {
    data.paying = false;
  });
};

onMounted(() => {
  loadOrder();
});
</script>
