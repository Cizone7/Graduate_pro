<template>
  <div style="margin: 0 auto;width: 65%">
    <el-card style="margin-top: 10px">
      <el-button style="margin-bottom: 10px;float: right" type="info" @click="$router.go(-1)">返回</el-button>
      <el-button style="margin-bottom: 10px;margin-right:10px;float: right" type="success"  @click="$router.push('/front/person' )">修改信息</el-button>

      <div style="display: flex">
        <div style="width: 28%">
          <img :src=item.avatarUrl alt="" style="width: 70%; height: 160px">
        </div>
        <div style="flex: 1; margin-left: 10px">
          <el-form label-width="80px" style="font-size: 20px">
            <el-form-item label="用户昵称">{{ item.nickname }}</el-form-item>
            <el-form-item label="用户积分">{{ item.count }}</el-form-item>
            <el-form-item label="所处地区">{{ item.address }}</el-form-item>
            <el-form-item label="合作邮箱">{{ item.email }}</el-form-item>
            <el-form-item label="自我介绍">{{ item.info }}</el-form-item>
          </el-form>
        </div>
      </div>
    </el-card>

    <el-tabs  type="card" style="margin-top: 10px">
      <el-tab-pane label="我的发布">
        <!--我的发布-->
        <el-table :data="tableData" stripe style="border-radius: 10px;margin-top: 30px;line-height: 30px;text-align: center;font-size: 18px">
          <el-table-column prop="name" label="作品标题"></el-table-column>
          <el-table-column label="作品预览" ><template v-slot="scope"><el-image style="width: 80px; height: 80px" :src="scope.row.img" :preview-src-list="[scope.row.img]"></el-image></template></el-table-column>
          <el-table-column prop="typeId" label="作品分类">
            <template v-slot="scope">
              <span v-if="scope.row.typeId">{{ types.find(v => v.id === scope.row.typeId) ? types.find(v => v.id === scope.row.typeId).name : ''  }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="style" label="创作类型"></el-table-column>
          <el-table-column prop="time" label="发布时间"></el-table-column>

          <el-table-column label="操作" align="center">
            <template v-slot="scope">
              <div class="table-row-options-warp">
                <el-popconfirm
                    class="ml-5"
                    confirm-button-text='确定'
                    cancel-button-text='取消'
                    icon="el-icon-info"
                    icon-color="red"
                    title="您确定删除这条数据吗？"
                    @confirm="del(scope.row.id)"
                >
                  <el-button slot="reference" type="danger">删除</el-button>
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
      </el-tab-pane>
      <el-tab-pane label="我的收藏">
        <div class="collect-container">
          <el-row :gutter="10" class="collect-grid">
            <el-col
                v-for="item in tableData1"
                :key="item.id"
                :xs="8" :sm="6" :md="4"
            class="collect-item"
            >
            <div class="collect-card">
              <img
                  :src="item.img"
                  class="collect-image"
                  @click="$router.push('/front/detail?id=' + item.id)"
              >
<!--              <div class="collect-name">{{ item.name }}</div>-->
              <div class="collect-actions">
                <el-popconfirm
                    title="您确定要取消收藏吗？"
                    @confirm="cancel(item.id)"
                >
                  <el-button
                      slot="reference"
                      type="danger"
                      size="mini"
                      class="cancel-btn"
                      icon="el-icon-delete"
                  ></el-button>
                </el-popconfirm>
              </div>
            </div>
            </el-col>
          </el-row>

          <div v-if="tableData1.length === 0" class="empty-tip">
            暂无收藏内容
          </div>

          <div style="margin: 20px 0">
            <el-pagination
                background
                @size-change="handleSizeChange1"
                @current-change="handleCurrentChange1"
                :current-page="pageNum1"
                :page-sizes="[12, 24, 36]"
                :page-size="pageSize1"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total1"
            ></el-pagination>
          </div>
        </div>
      </el-tab-pane>
      <el-tab-pane label="我的图库">
        <el-table
            :data="libraryData"
            stripe
            style="margin-top: 30px; line-height: 30px; text-align: center; font-size: 18px"
        >
          <el-table-column prop="name" label="图库名称"></el-table-column>
          <el-table-column label="创建时间">
            <template v-slot="scope">
              {{ formatDate(scope.row.time) }}
            </template>
          </el-table-column>
          <el-table-column label="作品数量">
            <template v-slot="scope">
              {{scope.row.creationCount || 0 }}
            </template>
          </el-table-column>
          <el-table-column label="操作" align="center" width="200">
            <template v-slot="scope">
              <el-button type="primary" size="mini" @click="viewLibrary(scope.row.id)">查看</el-button>
              <el-popconfirm
                  title="确定要删除这个图库吗？"
                  @confirm="deleteLibrary(scope.row.id)"
              >
                <el-button slot="reference" type="danger" size="mini">删除</el-button>
              </el-popconfirm>
            </template>
          </el-table-column>
        </el-table>

        <div style="padding: 10px 0">
          <el-pagination
              @size-change="handleLibSizeChange"
              @current-change="handleLibCurrentChange"
              :current-page="libPageNum"
              :page-sizes="[5, 10, 20]"
              :page-size="libPageSize"
              layout="total, sizes, prev, pager, next, jumper"
              :total="libTotal"
          ></el-pagination>
        </div>
      </el-tab-pane>
    </el-tabs>
    <div style="height:60px;margin-bottom: 50px"></div>


  </div>
</template>

<script>
export default {
  name: 'Mine',
  data() {
    return {
      item: {},
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      types:[],
      users:[],
      total: 0,
      pageNum: 1,
      pageSize: 5,
      tableData: [],
      total1: 0,
      pageNum1: 1,
      pageSize1:12,
      tableData1: [],
      // 新增图库相关数据
      libraryData: [],
      libPageNum: 1,
      libPageSize: 5,
      libTotal: 0,
    }
  },
  created() {
    this.loadLibraries()
    this.load()
    this.request.get("/type").then(res=>{
      this.types=res.data
    })
    this.request.get("/user").then(res=>{
      this.users=res.data
    })
  },
  methods: {
    del(id) {
      this.request.delete(`/creation/${id}`).then(res => {
        if (res.code === '200') {
          this.$message.success("删除成功");
          this.load(); // 重新加载作品列表
        }
      }).catch(error => {
        this.$message.error("删除失败：" + error.message);
      });
    },
    // 新增图库加载方法
    loadLibraries() {
      this.request.get("/library/my/page", {
        params: {
          pageNum: this.libPageNum,
          pageSize: this.libPageSize,
          name: this.searchLibName // 如果需要搜索功能可以添加
        }
      }).then(res => {
        if(res.code === '200') {
          this.libraryData = res.data?.records || [];
          this.libTotal = res.data?.total || 0;
        }
      });
    },
    // 查看图库
    viewLibrary(id) {
      this.$router.push(`/front/library?id=${id}`);
    },
    // 删除图库
    deleteLibrary(id) {
      // 先查询图库中的作品数量
      this.request.get(`/library/${id}/creation-count`).then(res => {
        if (res.code === '200' && res.data > 0) {
          this.$message.warning("请先删除该图库内的所有作品");
          return; // 拦截删除操作
        }

        // 确认无作品后执行删除
        this.request.delete(`/library/${id}`).then(res => {
          if (res.code === '200') {
            this.$message.success("删除成功");
            this.loadLibraries(); // 刷新图库列表
          } else {
            this.$message.error(res.msg || "删除失败");
          }
        });
      }).catch(error => {
        this.$message.error("操作失败：" + error.message);
      });
    },
    // 分页处理
    handleLibSizeChange(val) {
      this.libPageSize = val;
      this.loadLibraries();
    },
    handleLibCurrentChange(val) {
      this.libPageNum = val;
      this.loadLibraries();
    },
    // 日期格式化
    formatDate(timestamp) {
      return new Date(timestamp).toLocaleString();
    },
    load() {
      this.request.get('/user/' + this.user.id).then(res => {
        this.item = res.data
      })
      //我发布的作品信息
      this.request.get("/creation/my/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
        }
      }).then(res => {
        this.tableData = res.data?.records
        this.total = res.data?.total
      })
      //我收藏的作品
      this.request.get("/creation/collect/page", {
        params: {
          pageNum: this.pageNum1,
          pageSize: this.pageSize1,
          name: this.name,
        }
      }).then(res => {
        if(res.code==='200'){
          this.tableData1=res.data?.records || []
          this.total1=res.data?.total || 0

          if(this.total1 === 0) {
            this.pageNum1 = 1
          }
        }
      })
    },
    cancel(id){
      this.request.delete('/collect/'+id).then(res=>{
        if (res.code==='200'){
          this.$message.success('取消收藏成功')
          this.load()
        }
      })
    },
    handleSizeChange(pageSize) {
      this.pageSize = pageSize
      this.load()
    },
    handleCurrentChange(pageNum) {
      this.pageNum = pageNum
      this.load()
    },
    handleSizeChange1(pageSize1) {
      this.pageSize1 = pageSize1
      this.load()
    },
    handleCurrentChange1(pageNum1) {
      this.pageNum1 = pageNum1
      this.load()
    }
  }
}
</script>

<style scoped>

.info{
  height: 15px;
  line-height: 1.2;
  word-break: break-all;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1; /* 这里是超出几行省略 */
  overflow: hidden;
}

.collect-container {
  padding: 20px;
}

/* 收藏项样式 */
.collect-grid {
  display: flex;
  flex-wrap: wrap;
  margin: -8px;
}

.collect-item {
  padding: 8px;
  flex: 0 0 auto;
  width: 16.666%; /* 6个/行 */
  max-width: 16.666%;
}

.collect-card {
  width: 100%;
  height: 100%;
  position: relative;
  overflow: hidden;
  border-radius: 8px;
  transition: all 0.3s;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 16px;
}


.collect-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 4px 16px 0 rgba(0,0,0,0.15);
}

.collect-image {
  width: 100%;
  height: 120px;
  object-fit: cover;
  cursor: pointer;
  border-bottom: 1px solid #ebeef5;
}

.collect-name {
  padding: 8px;
  font-size: 12px;
  color: #606266;
  text-align: center;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.collect-actions {
  padding: 8px;
  display: flex;
  justify-content: center;
  border-top: 1px solid #f5f7fa;
}

.cancel-btn {
  padding: 5px 8px;
  font-size: 12px;
  border-radius: 4px;
  transition: all 0.2s;
}

.cancel-btn:hover {
  transform: scale(1.05);
}

/* 移动端适配 */
@media (max-width: 1200px) {
  .collect-item {
    width: 20%; /* 5个/行 */
    max-width: 20%;
  }
}

@media (max-width: 992px) {
  .collect-item {
    width: 25%; /* 4个/行 */
    max-width: 25%;
  }
}

@media (max-width: 768px) {
  .collect-item {
    width: 33.333%; /* 3个/行 */
    max-width: 33.333%;
  }
}

@media (max-width: 480px) {
  .collect-item {
    width: 50%; /* 2个/行 */
    max-width: 50%;
  }
}

</style>