<template>
  <div>
    <div style="">
      <el-input style="width: 200px" placeholder="请输入金额" suffix-icon="el-icon-search" v-model="amount"></el-input>
      <el-button class="ml-5" plain type="primary" @click="load">搜索</el-button>
      <el-button type="info" plain @click="reset">重置</el-button>
    </div>

    <div style="margin: 10px 0">
      <el-button type="primary" plain @click="handleAdd">新增 <i class="el-icon-circle-plus-outline"></i></el-button>
      <el-popconfirm
          class="ml-5"
          confirm-button-text='确定'
          cancel-button-text='我再想想'
          icon="el-icon-info"
          icon-color="red"
          title="您确定批量删除这些数据吗？"
          @confirm="delBatch"
      >
        <el-button type="danger" plain slot="reference">批量删除 <i class="el-icon-remove-outline"></i></el-button>
      </el-popconfirm>
    </div>

    <el-card>
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="60"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="amount" label="金额（元）" align="center"></el-table-column>
        <el-table-column prop="points" label="积分" align="center"></el-table-column>
        <el-table-column prop="createTime" label="创建时间" align="center">
          <template v-slot="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" align="center">
          <template v-slot="scope">
            {{ formatTime(scope.row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" align="center">
          <template v-slot="scope">
            <div class="table-row-options-warp">
              <span @click="handleEdit(scope.row)">编辑 </span>
              <el-popconfirm
                  class="ml-5"
                  confirm-button-text='确定'
                  cancel-button-text='取消'
                  icon="el-icon-info"
                  icon-color="red"
                  title="您确定删除这条数据吗？"
                  @confirm="del(scope.row.id)"
              >
                <span slot="reference">删除</span>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div style="padding: 10px 0">
        <el-pagination
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pageNum"
            :page-sizes="[2, 5, 10, 20]"
            :page-size="pageSize"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total">
        </el-pagination>
      </div>
    </el-card>

    <el-dialog title="充值比例" :visible.sync="dialogFormVisible" width="30%">
      <el-form label-width="80px" size="small" :model="form" :rules="rules" ref="form">
        <el-form-item label="金额" prop="amount">
          <el-input-number
              v-model="form.amount"
              :precision="2"
              :min="0.01"
              :step="1"
              controls-position="right"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>
        <el-form-item label="积分" prop="points">
          <el-input-number
              v-model="form.points"
              :min="1"
              :step="10"
              controls-position="right"
              style="width: 100%"
          ></el-input-number>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button type="primary" @click="save">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import {serverHost} from '../../config/config.default'
import dayjs from 'dayjs'
export default {
  name: "Rate",
  data() {
    return {
      serverHost : serverHost,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      amount: "",
      form: {},
      dialogFormVisible: false,
      multipleSelection: [],
      rules: {
        amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
        points: [{ required: true, message: '请输入积分', trigger: 'blur' }]
      }
    }
  },
  created() {
    this.load()
  },
  methods: {
    formatTime(time) {
      return time ? dayjs(time).format('YYYY-MM-DD HH:mm') : '';
    },
    load() {
      this.request.get("/rate/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          amount: this.amount,
        }
      }).then(res => {
        this.tableData = res.data.records
        this.total = res.data.total
      })
    },
    save() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          this.request.post("/rate", this.form).then(res => {
            if (res.code === '200') {
              this.$message.success("保存成功")
              this.dialogFormVisible = false
              this.load()
            } else {
              this.$message.error("保存失败")
            }
          })
        }
      })
    },
    handleAdd() {
      this.dialogFormVisible = true
      this.form = {}
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row))
      this.dialogFormVisible = true
    },
    del(id) {
      this.request.delete("/rate/" + id).then(res => {
        if (res.code === '200') {
          this.$message.success("删除成功")
          this.load()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    delBatch() {
      const ids = this.multipleSelection.map(v => v.id)
      this.request.post("/rate/del/batch", ids).then(res => {
        if (res.code === '200') {
          this.$message.success("批量删除成功")
          this.load()
        } else {
          this.$message.error("批量删除失败")
        }
      })
    },
    reset() {
      this.amount = ""
      this.load()
    },
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
.table-row-options-warp span {
  cursor: pointer;
  color: #409EFF;
  margin: 0 5px;
}
</style>