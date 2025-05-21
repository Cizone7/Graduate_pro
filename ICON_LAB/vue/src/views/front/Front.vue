<template>
  <div class="front-container">
    <div class="header-nav">
      <div class="header-left-warp">
        <div class="logo-warp">
          <div class="logo">
            <img src="../../../public/logo.svg" alt="" />
          </div>
          <div class="logo-text">{{ projectName }}</div>
        </div>
        <div class="header-navs">
          <el-menu
            router
            :default-active="$route.path"
            class="el-menu-demo"
            mode="horizontal"
            @select="handleSelect"
          >
            <!--前台路由-->
            <el-menu-item index="/front/home">首页</el-menu-item>
            <el-menu-item index="/front/submit">发布</el-menu-item>
            <el-menu-item index="/front/stylize">风格迁移</el-menu-item>
            <el-menu-item index="/front/openicon">开源图标</el-menu-item>
            <el-menu-item index="/front/recharge">充值</el-menu-item>
            <el-menu-item index="/front/mine">我的</el-menu-item>
            <!--前台路由-->
          </el-menu>
        </div>
      </div>
      <div class="user-warp">
        <!-- 消息通知 -->
        <el-badge
            :value="unreadCount"
            :max="99"
            class="notification-badge"
            :hidden="unreadCount === 0"
        >
          <el-button
              type="text"
              @click="openNotifications"
              style="margin-right: 15px"
          >
            <i class="el-icon-bell" style="font-size: 20px"></i>
          </el-button>
        </el-badge>

        <!-- 消息弹窗 -->
        <el-dialog
            title="评论通知"
            :visible.sync="notificationVisible"
            width="40%"
            :modal="false"
            :modal-append-to-body="false"
            custom-class="custom-notification-dialog"
        >
          <div v-for="item in notifications" :key="item.id" class="notification-item">
            <el-avatar :src="item.avatarUrl" size="small"></el-avatar>
            <div class="notification-content">
              <div>
                <span style="font-weight:500">{{ item.nickname }}</span>
                评论了你的作品
                <el-link type="primary" @click="goDetail(item.itemId)">{{ item.itemName }}</el-link>
              </div>
              <div style="color:#666;margin-top:5px">{{ item.content }}</div>
              <div style="color:#999;font-size:12px">{{ item.time }}</div>
            </div>
          </div>
          <span slot="footer">
        <el-button @click="markAsRead" type="primary">全部标记已读</el-button>
      </span>
        </el-dialog>

        <div v-if="!user.id" class="btn-login">
          <el-button @click="$router.push('/login')">登录</el-button>
        </div>
        <div v-if="!user.id" class="btn-login" style="margin-left: 10px">
          <el-button @click="$router.push('/register')">注册</el-button>
        </div>
        <el-dropdown v-else>
          <div class="user-avatar">
            <img :src="user.avatarUrl" />
          </div>
          <el-dropdown-menu size="medium" slot="dropdown">
            <el-dropdown-item>{{ user.nickname }}</el-dropdown-item>
            <el-dropdown-item>
              <router-link to="/front/person"><div>个人信息</div></router-link>
            </el-dropdown-item>
            <el-dropdown-item>
              <router-link to="/front/password"
                ><div>修改密码</div></router-link
              >
            </el-dropdown-item>
            <el-dropdown-item
              ><div @click="logout">退出登录</div></el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
    </div>
    <div class="main-content">
      <router-view @refreshUser="getUser"></router-view>
    </div>
    <div style="width: 100%; background-color: white; position: fixed; bottom: 0;  padding: 5px 0; color: #666; font-size: 10px;text-align: center">
      声明：本站部分内容来源于网络，图片版权属于原作者，本站内容由第三方用户转载仅供大家交流学习，切勿用于任何商业用途；本站不承担用户因使用这些资源，对自己和他人造成任何形式的损害或伤害；如果侵犯了您的合法权益，请您及时与我们联系，我们会在第一时间删除相关内容。
    </div>

  </div>
</template>

<script>
import { projectName } from "../../../config/config.default";
export default {
  name: "",
  data() {
    return {
      projectName: projectName,
      user: localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user"))
        : {},
      notifications: [],
      unreadCount: 0,
      notificationVisible: false,
      pollTimer: null,
    };
  },
  mounted() {
    this.loadNotifications();
    this.pollTimer = setInterval(this.loadNotifications, 10000);
  },
  beforeDestroy() {
    clearInterval(this.pollTimer);
  },
  methods: {
    async loadNotifications() {
      if (!this.user?.id) return;
      try {
        const res = await this.request.get("/comment/notifications");
        this.notifications = res.data || [];
        this.unreadCount = this.notifications.filter(item => !item.isRead).length;
      } catch (e) {
        console.error("获取通知失败", e.response?.data || e.message);
        this.$message.error("通知加载失败，请稍后重试");
      }
    },
    openNotifications() {
      this.notificationVisible = true;
      this.markAsRead();
    },
    async markAsRead() {
      const unreadIds = this.notifications
          .filter(item => !item.isRead)
          .map(item => item.id);

      if (unreadIds.length > 0) {
        await this.request.post("/comment/read", unreadIds);
        this.loadNotifications();
      }
    },
    goDetail(itemId) {
      this.$router.push(`/front/detail?id=${itemId}`);
      this.notificationVisible = false;
    },
    handleSelect(key, keyPath) {},
    logout() {
      this.$store.commit("logout");
      this.$message.success("退出成功");
    },
    getUser() {
      let username = localStorage.getItem("user")
        ? JSON.parse(localStorage.getItem("user")).username
        : "";
      if (username) {
        // 从后台获取User数据
        this.request.get("/user/username/" + username).then((res) => {
          // 重新赋值后台的最新User数据
          this.user = res.data;
        });
      }
    },
  },
};
</script>

<style lang="scss" scoped>
/* 去除模态背景 */
v-deep .v-modal {
  display: none !important;
}

/* 可选：防止点击外部关闭弹窗 */
v-deep .el-dialog__wrapper {
  pointer-events: none;

  .el-dialog {
    pointer-events: all;
  }
}

/* 可选：优化弹窗阴影 */
.custom-notification-dialog {
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1) !important;
  border-radius: 8px !important;
}
.notification-badge {
  v-deep .el-badge__content {
    box-shadow: none;
  }
}

.el-button--text {
  background: transparent !important;
  &:hover, &:focus {
    background: transparent !important;
  }
}

.el-dialog__wrapper {
  background-color: rgba(0,0,0,0.5) !important;

  .el-dialog {
    border-radius: 8px;

    .el-dialog__header {
      border-bottom: 1px solid #eee;
    }

    .el-dialog__body {
      padding: 15px 20px;
    }
  }
}
.notification-badge {
  margin-right: 20px;
}

.notification-item {
  display: flex;
  align-items: start;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.notification-content {
  margin-left: 10px;
  flex: 1;
}
.front-container {
  background-color: var(--color-background-2);
  min-height: 100vh;
  .header-nav {
    z-index: 1800;
    position: sticky;
    top: 0;
    height: 70px;
    background-color: var(--color-background-2);
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 20px;
    box-shadow: 0 2px 10px 0 rgb(0 0 0 / 10%);
    .header-left-warp {
      margin-left: 20px;
      display: flex;
      align-items: center;
      .logo-warp {
        display: flex;
        align-items: center;
        .logo {
          width: 30px;
          height: 30px;
          margin-right: 5px;
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
        .logo-text {
          font-size: 22px;
          font-weight: 500;
          color: var(--color-success);
        }
      }
      .header-navs {
        margin-left: 100px;
        .el-menu {
          background: transparent;
        }
        .el-menu--horizontal {
          border: none;
        }
        .el-submenu__title {
          padding: 0;
          font-size: 17px !important;
          height: 35px;
          margin: 0 10px;
          display: flex;
          align-items: center;
        }
        .is-active {
          border-bottom: 2px solid var(--color-success);
          color: var(--color-success);
        }
      }
    }
    .user-warp {
      margin-right: 10px;
      margin-top: 5px;
      display: flex;
      .user-avatar {
        width: 40px;
        height: 40px;

        border-radius: 50%;
        overflow: hidden;
        border: 1px solid var(--color-primary);
        padding: 2px;
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
          border-radius: 50%;
        }
      }
    }
  }
  .main-content {

  }
}
</style>
