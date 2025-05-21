<template>
  <div>
    <!-- 搜索栏 -->
    <div style="margin-bottom: 20px">
      <el-input
          style="width: 200px; margin-right: 5px"
          placeholder="按操作类型搜索"
          v-model="searchOperationType"
      ></el-input>
      <el-input
          style="width: 200px;margin-right: 5px"
          placeholder="按用户昵称搜索"
          v-model="searchNickname"
      ></el-input>
      <el-button type="primary" @click="load">搜索</el-button>
      <el-button @click="reset">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="tableData" stripe>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column label="用户昵称">
        <template v-slot="scope">
          {{ users.find(u => u.id === scope.row.userId)?.nickname || '未知' }}
        </template>
      </el-table-column>
      <el-table-column prop="operationType" label="操作类型"></el-table-column>
      <el-table-column prop="operationDetail" label="操作详情"></el-table-column>
      <el-table-column prop="ip" label="IP地址"></el-table-column>
      <el-table-column label="操作时间">
        <template v-slot="scope">
          {{ formatTime(scope.row.createTime) }}
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <el-pagination
        @current-change="handlePageChange"
        :current-page="pageNum"
        :page-size="pageSize"
        layout="total, prev, pager, next"
        :total="total"
    ></el-pagination>
  </div>
</template>

<script>
import dayjs from 'dayjs';

export default {
  name: 'Log',
  data() {
    return {
      tableData: [],
      users: [],
      pageNum: 1,
      pageSize: 10,
      total: 0,
      searchOperationType: '',
      searchNickname: ''
    }
  },
  created() {
    this.load();
    this.loadUsers();
  },
  methods: {
    load() {
      this.request.get('/sys-log/page', {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          operationType: this.searchOperationType,
          nickname: this.searchNickname
        }
      }).then(res => {
        this.tableData = res.data.records;
        this.total = res.data.total;
      });
    },
    loadUsers() {
      this.request.get('/user').then(res => {
        this.users = res.data;
      });
    },
    reset() {
      this.searchOperationType = '';
      this.searchNickname = '';
      this.load();
    },
    formatTime(time) {
      return dayjs(time).format('YYYY-MM-DD HH:mm');
    },
    handlePageChange(pageNum) {
      this.pageNum = pageNum;
      this.load();
    }
  }
}
</script>