<template>
  <div class="recharge-container">
    <el-card class="recharge-card">
      <div slot="header" class="header">
        <span>积分充值</span>
        <span class="current-points">当前积分：{{ currentUser.count }}</span>
      </div>

      <!-- 充值金额选择 -->
      <div class="rate-list">
        <div
            v-for="rate in rates"
            :key="rate.id"
            class="rate-item"
            :class="{ active: selectedRate?.id === rate.id }"
            @click="selectRate(rate)"
        >
          <div class="amount">￥{{ rate.amount.toFixed(2) }}</div>
          <div class="points">+ {{ rate.points }} 积分</div>
        </div>
      </div>

      <!-- 充值按钮 -->
      <el-button
          type="primary"
          class="recharge-btn"
          :disabled="!selectedRate"
          @click="handleRecharge"
          :loading="loadingRecharge"
      >
        立即充值
      </el-button>
    </el-card>

    <el-card class="history-card" style="margin-top: 20px">
      <div slot="header">
        <span>充值记录</span>
      </div>

      <el-table :data="rechargeRecords" stripe v-loading="loading">
        <el-table-column prop="orderNo" label="订单号" align="center" width="200"></el-table-column>
        <el-table-column label="充值金额" align="center">
          <template v-slot="scope">
            ￥{{ scope.row.amount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="获得积分" align="center"></el-table-column>
        <el-table-column label="状态" align="center" width="120">
          <template v-slot="scope">
            <el-tag :type="statusType(scope.row.status)">
              {{ statusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" align="center" width="180">
          <template v-slot="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="支付时间" align="center" width="180">
          <template v-slot="scope">
            {{ scope.row.payTime ? formatTime(scope.row.payTime) : '-' }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
          style="margin-top: 20px"
          @current-change="handlePageChange"
          :current-page="pageNum"
          :page-size="pageSize"
          layout="total, prev, pager, next"
          :total="total"
      ></el-pagination>
    </el-card>
    <!-- 支付宝支付窗口 -->
    <div v-if="showPayForm" v-html="payForm" class="pay-form-container"></div>
  </div>
</template>

<script>
import dayjs from 'dayjs'
import { MessageBox } from 'element-ui'

export default {
  name: "Recharge",
  data() {
    return {
      currentUser: {}, // 当前登录用户信息
      rates: [],      // 充值比例数据
      selectedRate: null, // 选中的充值项
      rechargeRecords: [], // 充值记录数据
      loading: false,
      loadingRecharge: false,
      pageNum: 1,
      pageSize: 10,
      total: 0,
      payForm: '',
      showPayForm: false,
      pollInterval: null
    }
  },
  mounted() {
    this.loadCurrentUser()
    this.loadRates()
    this.loadRechargeRecords()
  },
  methods: {
    statusType(status) {
      const map = {
        'WAIT': 'warning',
        'SUCCESS': 'success',
        'FAILED': 'danger'
      }
      return map[status] || 'info'
    },
    statusText(status) {
      const map = {
        'WAIT': '待支付',
        'SUCCESS': '成功',
        'FAILED': '失败'
      }
      return map[status] || '未知'
    },
    async loadCurrentUser() {
      const localUser = JSON.parse(localStorage.getItem('user') || '{}')
      if (!localUser.id) {
        this.$message.warning('请先登录')
        this.$router.push('/login')
        return
      }

      try {

        const res = await this.request.get(`/user/${localUser.id}`)
        if (res.code === '200') {
          this.currentUser = res.data
        }
      } catch (error) {
        this.$message.error('用户信息加载失败')
      }
    },
    // 加载充值比例
    async loadRates() {
      try {
        const res = await this.request.get('/rate')
        if (res.code === '200') {
          this.rates = res.data
        }
      } catch (error) {
        this.$message.error('加载充值比例失败')
      }
    },

    async loadRechargeRecords() {
      this.loading = true
      const loadUser = JSON.parse(localStorage.getItem('user') || '{}')

      try {
        const res = await this.request.get(`/record/records/${loadUser.id}`, {
          params: {
            pageNum: this.pageNum,
            pageSize: this.pageSize
          }
        })
        this.rechargeRecords = res.data.records|| []
        console.log(res)
        this.total = res.data.total || 0
      } catch (error) {
        this.$message.error('加载记录失败')
      } finally {
        this.loading = false
      }
    },

    handlePageChange(pageNum) {
      this.pageNum = pageNum
      this.loadRechargeRecords()
    },

    // 选择充值项
    selectRate(rate) {
      this.selectedRate = rate
    },

    // 执行充值
    async handleRecharge() {
      if (!this.currentUser?.id) return

      try {
        await MessageBox.confirm(
            `确定要充值 ￥${this.selectedRate.amount.toFixed(2)}，获得 ${this.selectedRate.points} 积分吗？`,
            '充值确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        this.loadingRecharge = true
        // 创建订单
        const createRes = await this.request.post('/record/recharge', {
          userId: this.currentUser.id,
          amount: Number(this.selectedRate.amount)
        })

        if (createRes.code === '200') {
          const localUser = JSON.parse(localStorage.getItem('user') || '{}');
          // 使用新窗口打开支付页面
          const payUrl = `${this.request.defaults.baseURL}/alipay/pay?orderNo=${createRes.data}&token=${localUser.token}`;
          window.open(payUrl);

          // 启动轮询检查支付状态
          this.startPolling(createRes.data)
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error("完整错误对象:", error);
          console.error("请求URL:", error.config?.url);
          console.error("响应状态码:", error.response?.status);
          console.error("响应数据:", error.response?.data);
          this.$message.error(error.response?.data?.message || '操作失败');
        }
      } finally {
        this.loadingRecharge = false
      }
    },startPolling(orderNo) {
      this.pollInterval = setInterval(async () => {
        try {
          const res = await this.request.get(`/record/${orderNo}`)
          if (res.data.status === 'SUCCESS') {
            clearInterval(this.pollInterval)
            this.$message.success('充值成功')
            await this.loadCurrentUser()
            this.loadRechargeRecords()
            this.showPayForm = false
          } else if (res.data.status === 'FAILED') {
            clearInterval(this.pollInterval)
            this.$message.error('支付失败')
            this.showPayForm = false
          }
        } catch (error) {
          console.error('轮询支付状态失败', error)
        }
      }, 3000) // 每3秒检查一次
    },
    formatTime(time) {
      return dayjs(time).format('YYYY-MM-DD HH:mm')
    },
  }
}
</script>

<style scoped>
.pay-form-container {
  position: fixed;
  top: -1000px;
  left: -1000px;
}
.history-card {
  margin-top: 20px;
}

.recharge-container {
  max-width: 800px;
  margin: 20px auto;
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.current-points {
  color: #409EFF;
  font-weight: bold;
}

.rate-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 20px;
  margin: 30px 0;
}

.rate-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 20px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.rate-item:hover {
  border-color: #409EFF;
  transform: translateY(-5px);
}

.rate-item.active {
  border-color: #409EFF;
  background: #f5f7fa;
}

.amount {
  font-size: 24px;
  color: #303133;
  font-weight: bold;
  margin-bottom: 10px;
}

.points {
  color: #67C23A;
  font-size: 16px;
}

.recharge-btn {
  width: 100%;
  height: 50px;
  font-size: 18px;
}
</style>