<template>
  <div style="color: #666; font-size: 14px">

    <el-row :gutter="24">
      <el-col :span="14">
        <el-carousel height="450px" :interval="5000">
          <el-carousel-item v-for="item in imgs" :key="item">
            <img :src="item" style="width: 100%; height: 100%" />
          </el-carousel-item>
        </el-carousel>
      </el-col>

      <el-col :span="10">
        <el-card class="box-card">
          <div slot="header" class="clearfix">
            <span>系统技术选型</span>
          </div>
          <div class="text item">
            操作系统: Windows11
          </div>
          <div class="text item">
            开发工具: Idea
          </div>
          <div class="text item">
            系统架构: 前后端分离B/S架构
          </div>
          <div class="text item">
            后端: SpringBoot+MybatisPlus
          </div>
          <div class="text item">
            前端: Vue+ElementUI+Axios
          </div>
          <div class="text item">
            数据库: Mysql8.0
          </div>
        </el-card>
      </el-col>
    </el-row>


    <el-row :gutter="24" style="margin-top: 10px">
      <el-col :span="14">
        <el-card style="width: 100%">
          <p>
            <strong style="color: orangered">欢迎使用本系统</strong>
          </p>
          <p style="margin-top: 5px">
          </p>
          <el-divider />
        </el-card>
      </el-col>

    </el-row>

  </div>
</template>

<script>
export default {
  name: "Home",
  data() {
    return {
      imgs: [
        "https://image.baidu.com/search/down?thumburl=https://baidu.com&url=https://tvax2.sinaimg.cn/large/7e8b4ac8ly1htxt4v6o91j21si1cc7fl.jpg",
        "https://image.baidu.com/search/down?thumburl=https://baidu.com&url=https://tvax2.sinaimg.cn/large/7e8b4ac8ly1htxsg6u1ctj21b80ragpc.jpg",
      ],
    };
  },
};
</script>

<style scoped>
.text {
  font-size: 14px;
}

.item {
  margin-bottom: 18px;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}
.clearfix:after {
  clear: both
}

.box-card {
  width: 480px;
}
</style>

