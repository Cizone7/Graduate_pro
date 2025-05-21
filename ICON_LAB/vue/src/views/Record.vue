<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin-bottom: 20px">
      <el-input
          style="width: 200px;margin-right: 5px"
          placeholder="按金额搜索"
          suffix-icon="el-icon-search"
          v-model="searchAmount"
      ></el-input>
      <el-input
          style="width: 200px;"
          placeholder="按用户昵称搜索"
          suffix-icon="el-icon-search"
          v-model="searchNickname"
      ></el-input>
      <el-button class="ml-5" plain type="primary" @click="load">搜索</el-button>
      <el-button type="info" plain @click="reset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-card>
      <el-table :data="tableData" stripe>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="用户昵称" align="center">
          <template v-slot="scope">
            <span v-if="scope.row.userId">
              {{ users.find(v => v.id === scope.row.userId)?.nickname || '未知用户' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="充值金额" align="center">
          <template v-slot="scope">
            ￥{{ scope.row.amount.toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column prop="points" label="获得积分" align="center"></el-table-column>
        <el-table-column prop="status" label="状态" align="center">
          <template v-slot="scope">
            <el-tag :type="scope.row.status === 'SUCCESS' ? 'success' : 'danger'">
              {{ scope.row.status }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="充值时间" align="center">
          <template v-slot="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div style="padding: 10px 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[5, 10, 20, 50]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
        ></el-pagination>
      </div>
    </el-card>
  </div>
</template>

<script>
import dayjs from 'dayjs'

export default {
  name: "Record",
  data() {
    return {
      tableData: [],
      users: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      searchAmount: "", // 搜索条件
      searchNickname: "",
    }
  },
  created() {
    this.load()
    this.request.get('/user').then(res => {
      this.users = res.data
    })
  },
  methods: {
    formatTime(time) {
      return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : ''
    },
    // 加载数据
    load() {
      this.request.get("/record/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          amount: this.searchAmount,
          nickname: this.searchNickname,
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      })
    },
    // 重置搜索
    reset() {
      this.searchAmount = ""
      this.searchNickname = ""
      this.load()
    },
    // 分页方法
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    }
  }
}
</script>

<style scoped>
.ml-5 {
  margin-left: 5px;
}
</style>