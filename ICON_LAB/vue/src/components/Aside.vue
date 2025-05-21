<template>
  <el-menu
    style="min-height: 100%; overflow-x: hidden"
    :default-active="$route.path"
    :collapse-transition="false"
    :collapse="isCollapse"
    router
    :class="`${themes[themeStatus]}`"
  >
    <div :class="`logo-warp theme ${themes[themeStatus]}`" style="height: 70px; line-height: 60px; text-align: center">

      <div class="logo-inner">
        <div @click="openDrawer" style="display: inline-block">
          <img src="../../public/logo.svg" alt="" style="width: 30px; position: relative; top: 8px" />
        </div>
        <span style="margin-left: 5px; font-size: 15px; font-weight: 500;color: white" v-show="logoTextShow">后台管理系统</span>
      </div>

    </div>

    <!--后台菜单-->
    <el-menu-item index="/home">
      <i class="el-icon-house"></i>
      <span slot="title">系统主页</span>
    </el-menu-item>

    <el-menu-item index="/type">
      <i class="el-icon-collection"></i>
      <span slot="title">分类管理</span>
    </el-menu-item>

    <el-menu-item index="/rate">
      <i class="el-icon-s-finance"></i>
      <span slot="title">充值管理</span>
    </el-menu-item>

    <el-menu-item index="/creation">
      <i class="el-icon-suitcase"></i>
      <span slot="title">作品管理</span>
    </el-menu-item>

    <el-menu-item index="/echarts">
      <i class="el-icon-data-analysis"></i>
      <span slot="title">数据统计</span>
    </el-menu-item>

    <el-submenu index="系统管理">

      <template slot="title">
        <i class="el-icon-s-grid"></i>
        <span slot="title">系统管理</span>
      </template>

      <el-menu-item index="/user">
        <i class="el-icon-user"></i>
        <span slot="title">用户管理</span>
      </el-menu-item>

      <el-menu-item index="/record">
        <i class="el-icon-tickets"></i>
        <span slot="title">流水记录</span>
      </el-menu-item>

      <el-menu-item index="/log">
        <i class="el-icon-notebook-2"></i>
        <span slot="title">系统日志</span>
      </el-menu-item>

    </el-submenu>

    <!--后台菜单-->
  </el-menu>
</template>

<script>

import {projectName} from "../../config/config.default";

export default {
  name: 'Aside',
  props: {
    isCollapse: Boolean,
    logoTextShow: Boolean,
    themeStatus: Number
  },
  data() {
    return {
      projectName:projectName,
      themes: ['theme1', 'theme2', 'theme3', 'theme4', 'theme5', 'theme6', 'theme7', 'theme8'],
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
    }
  },
  methods: {
    openDrawer() {
      this.$emit('openDrawer')
    }
  }
}
</script>

<style scoped>

/*/
  menu菜单 整体样式
 */
.el-menu{
  background-color: white;
  border: none;
}

/*/
  menu菜单 鼠标指针放到具体的某一项上时候的样式
 */
.el-menu-item:hover {
  color: var(--font-color-primary);
  background-color: var(--back-color-primary);
}

/*/
  menu菜单 当前被选择项的样式
 */
.el-menu-item.is-active {
  background-color: var(--back-color-primary);
  color: var(--font-color-primary);
  border-right-style: solid;
  border-right-width: 3px;
  border-right-color: var(--font-color-primary);
}

/*/
  menu菜单 标题 展开时候的样式
 */
.el-submenu >>> .el-submenu__title {
  background-color: white;
}

/*/
  menu菜单 标题 鼠标放到展开时候的样式
 */
.el-submenu >>> .el-submenu__title :hover {
  color: var(--font-color-primary);
}

/*/
  menu菜单 标题 被展开时候，这个展开标题的样式
 */
.el-submenu.is-opened >>> .el-submenu__title {
  color: var(--font-color-primary);
}

/*
 解决收缩菜单文字不消失问题
*/
.el-menu--collapse span {
  visibility: hidden;
}

.logo-warp {
  background-color: #f9f9f9;
  padding-bottom: 10px;
  height: 100%;
}

.logo-inner {
  background-color: var(--font-color-primary);
}

</style>
