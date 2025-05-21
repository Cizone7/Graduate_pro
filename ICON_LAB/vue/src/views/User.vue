<template>
  <div>
    <div style="">
      <el-select v-model="role" placeholder="请选择角色" class="mr-5">
        <el-option
            v-for="item in roles"
            :key="item.flag"
            :label="item.label"
            :value="item.flag">
        </el-option>
      </el-select>
      <el-input style="width: 200px" placeholder="请输入昵称" suffix-icon="el-icon-search" v-model="nickname"></el-input>
      <el-input style="width: 200px" placeholder="请输入邮箱" suffix-icon="el-icon-message" class="ml-5" v-model="email"></el-input>
      <el-input style="width: 200px" placeholder="请输入地址" suffix-icon="el-icon-position" class="ml-5" v-model="address"></el-input>

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
      <el-table :data="tableData" stripe @selection-change="handleSelectionChange" :header-cell-style="{'padding-left':'4px','padding-right':'4px'}">
        <el-table-column type="selection" width="60"></el-table-column>
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="username" label="用户名" width="140"></el-table-column>
        <el-table-column label="角色">
          <template v-slot="scope">
            <span v-if="scope.row.role">{{ roles.find(v => v.flag === scope.row.role) ? roles.find(v => v.flag === scope.row.role).label : ''  }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" width="120"></el-table-column>
        <el-table-column prop="email" label="邮箱"></el-table-column>
        <el-table-column prop="phone" label="电话"></el-table-column>
        <el-table-column prop="address" label="地址"></el-table-column>
        <el-table-column label="头像"><template v-slot="scope"><el-image style="width: 50px; height: 50px;border-radius: 50px"  :src="scope.row.avatarUrl" :preview-src-list="[scope.row.avatarUrl]"></el-image></template></el-table-column>
        <el-table-column label="操作"  width="200" align="center">
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
              <!-- 在操作列的模板中添加重置密码按钮 -->
              <el-popconfirm
                  class="ml-5"
                  confirm-button-text='确定'
                  cancel-button-text='取消'
                  icon="el-icon-info"
                  icon-color="red"
                  title="您确定重置该用户的密码吗？"
                  @confirm="resetPassword(scope.row.id)"
                  @after-leave="handlePopoverClose"
              >
                <span slot="reference" style="color: #E6A23C; margin-left: 10px">重置密码</span>
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

    <el-dialog title="用户信息" :visible.sync="dialogFormVisible" width="30%" >
      <el-form label-width="80px" size="small">
        <el-form-item prop="testImg" label="头像">
          <el-upload :action="serverHost+'/web/upload'" ref="img" :on-success="handleImgUploadSuccess">
            <el-button size="small" type="primary">点击上传</el-button>
          </el-upload>
        </el-form-item>
        <el-form-item label="用户名">
          <el-input v-model="form.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="角色">
          <el-select clearable v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option v-for="item in roles" :key="item.flag" :label="item.label" :value="item.flag"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="地址">
          <el-input v-model="form.address" autocomplete="off"></el-input>
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

export default {
  name: "User",
  data() {
    return {
      serverHost : serverHost,
      tableData: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      role: "",
      nickname: "",
      email: "",
      address: "",
      form: {},
      dialogFormVisible: false,
      multipleSelection: [],
      roles: [
        {label:'管理员',flag:'ROLE_ADMIN'},
        {label:'用户',flag:'ROLE_USER'},
      ],
    }
  },
  created() {
    this.load()
  },
  methods: {
    load() {
      this.request.get("/user/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          role: this.role,
          nickname: this.nickname,
          email: this.email,
          address: this.address,
        }
      }).then(res => {

        this.tableData = res.data.records
        this.total = res.data.total

      })
    },
    save() {
      this.request.post("/user", this.form).then(res => {
        if (res.code === '200') {
          this.$message.success("保存成功")
          this.dialogFormVisible = false
          this.load()
        } else {
          this.$message.error("保存失败")
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
      this.request.delete("/user/" + id).then(res => {
        if (res.code === '200') {
          this.$message.success("删除成功")
          this.load()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
    resetPassword(id) {
      this.request.put("/user/password-reset/" + id).then(res => {
        if (res.code === '200') {
          this.$message.success("密码重置成功");
          this.load(); // 刷新数据
        } else {
          this.$message.error("密码重置失败");
        }
      }).catch(error => {
        this.$message.error("请求异常：" + error.message);
      });
    },
    handlePopoverClose() {
      // 强制将焦点移出弹出框
      document.activeElement.blur();
    },
    handleSelectionChange(val) {
      this.multipleSelection = val
    },
    delBatch() {
      let ids = this.multipleSelection.map(v => v.id)  // [{}, {}, {}] => [1,2,3]
      this.request.post("/user/del/batch", ids).then(res => {
        if (res.code === '200') {
          this.$message.success("批量删除成功")
          this.load()
        } else {
          this.$message.error("批量删除失败")
        }
      })
    },
    reset() {
      this.role = ""
      this.nickname = ""
      this.email = ""
      this.address = ""
      this.load()
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleImgUploadSuccess(res) {
      this.form.avatarUrl = res
    },
  }
}
</script>

<style scoped>
.el-popover[aria-hidden="true"] {
  display: none !important;
}
</style>
