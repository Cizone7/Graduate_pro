<template>
  <div style="color: #666;width: 60%;margin: 0 auto">

    <div style="display: flex; margin-top: 20px; padding: 15px; background-color: #f9f9f9; border-radius: 8px; box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);">
      <div style="flex: 3; padding: 10px;">
        <el-button type="info" plain @click="$router.go(-1)" size="mini" style="margin-bottom: 10px;">返回</el-button>
        <div style="text-align: center; margin-bottom: 15px;">
          <h1 style="font-size: 20px; color: #333; margin: 0;">{{item.name}}</h1>
        </div>

        <div style="margin: 15px 0; text-align: center;">
          <img :src="item.img" alt="Content Image" style="max-width: 80%; height: 180px; object-fit: contain; border-radius: 6px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);"  />
        </div>

        <div style="font-size: 14px; margin-top: 10px; text-align: center;">
          <div style="color: #555; margin-bottom: 5px;">
            <i class="el-icon-coin"></i> 下载积分: {{item.count}}
          </div>
          <div style="color: #777; font-size: 12px;">
            <i class="el-icon-time"></i> 发布时间: {{item.time}}
          </div>
        </div>
        <div style="display: flex; justify-content: center; margin-top: 15px; gap: 8px;">
          <el-button type="warning" size="small" @click="collect" icon="el-icon-star-off">收藏</el-button>
          <el-button type="primary" size="small" @click="download(item)" icon="el-icon-download">下载</el-button>
        </div>
      </div>
      <div style="flex: 1; padding: 10px; min-width: 180px;">
        <el-card class="author-card" style="border-radius: 6px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1); padding: 12px;">
          <div style="font-size: 14px; color: #666; margin-bottom: 12px;">作者名片</div>
          <div style="display: flex; flex-direction: column; align-items: center;">
            <el-image
                style="width: 80px; height: 80px; border-radius: 50%; border: 2px solid #189a97;"
                :src="users.avatarUrl"
                fit="cover"
            ></el-image>
            <div style="text-align: center; margin-top: 8px;">
              <div style="font-size: 16px; font-weight: 500;">{{ users.nickname }}</div>
<!--              <div style="font-size: 12px; color: #999; margin-top: 4px;">-->
<!--                积分：{{ users.count }}-->
<!--              </div>-->
            </div>
            <el-link
                type="primary"
                :underline="false"
                @click="$router.push('/front/personal?id=' + users.id)"
                style="margin-top: 8px; font-size: 12px;"
            >
              查看主页 →
            </el-link>
          </div>
        </el-card>
      </div>
    </div>



    <div style="margin: 30px 0">
      <div style="margin: 10px 0">
        <div style="border-bottom: 2px solid #ccc; padding: 10px 0; font-size: 20px; margin-bottom:10px;" >评论({{comments.length}})</div>
        <div class="comment-send-warp">
          <el-input class="comment-send-input" size="small" type="textarea" rows="4" v-model="commentForm.content" placeholder="善语结善缘，恶语伤人心"></el-input>
          <el-button :disabled="!commentForm.content" class="btn-sent-comment" type="primary" size="medium" @click="save" >评论</el-button>
        </div>
      </div>

      <div class="comment-warp">
        <div v-for="item in comments" :key="item.id">
          <div class="comment-item">
            <div class="comment-avatar">
              <el-image :src="item.avatarUrl"></el-image>
            </div> <!--  头像-->
            <div class="comment-info">
              <div class="comment-name">{{ item.nickname }}</div>
              <div class="comment-content">{{ item.content }}</div>

              <div class="comment-other">
                <div class="comment-time">
                  <i class="el-icon-time"></i><span>{{ item.time }}</span>
                </div>
                <div>
                  <el-button type="text" @click="handleReply(item.id)">回复</el-button>
                  <el-button type="text" @click="del(item.id)" v-if="user.id === item.userId || user.role==='ROLE_ADMIN'">删除</el-button>
                </div>
              </div>
            </div>
          </div>
          <!--回复-->
          <template v-if="item.children.length">
            <div v-for="subItem in item.children" class="comment-item comment-item-reply">
              <div class="comment-avatar">
                <el-image :src="subItem.avatarUrl"/>
              </div> <!--  头像-->
              <div class="comment-info">
                <div class="comment-name">{{ subItem.nickname }} 回复 @ {{subItem.pnickname}}</div>
                <div class="comment-content">{{ subItem.content }}</div>

                <div class="comment-other">
                  <div class="comment-time">
                    <i class="el-icon-time"></i><span>{{ subItem.time }}</span>
                  </div>
                  <div>
                    <el-button type="text" @click="handleReply(subItem.id)">回复</el-button>
                    <el-button type="text" @click="del(subItem.id)" v-if="user.id === subItem.userId || user.role==='ROLE_ADMIN'">删除</el-button>
                  </div>
                </div>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>
    <div style="height:60px;margin-bottom: 50px"></div>

    <el-dialog title="回复" :visible.sync="dialogFormVisible2" width="50%" >
      <el-form label-width="80px" size="small">
        <el-form-item label="回复内容">
          <el-input type="textarea" v-model="commentForm.contentReply" autocomplete="off"></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible2 = false" size="small">取 消</el-button>
        <el-button type="primary" @click="save"  size="small">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { MessageBox } from 'element-ui'
export default {
  name: "",
  data() {
    return {
      item: {},
      user: localStorage.getItem("user") ? JSON.parse(localStorage.getItem("user")) : {},
      id: this.$route.query.id,
      commentItemId: this.$route.query.id,
      dialogFormVisible2: false,
      comments: [],
      commentForm: {},
      users:[],
    }
  },
  created() {
    this.load()
    this.loadComment()
  },
  methods: {
    load() {
      this.request.get("/creation/" + this.id).then(res => {
        this.item = res.data
        this.fetchUserInfo(this.item.userId);
      })
    },
    fetchUserInfo(userId) {
      this.request.get("/user/" + userId).then(res => {
        this.users = res.data;
      });
    },
    startDownload(url) {
      const iframe = document.createElement('iframe')
      iframe.style.display = 'none'
      iframe.src = url
      document.body.appendChild(iframe)

      setTimeout(() => {
        document.body.removeChild(iframe)
      }, 5000)
    },
    async download(item) {
      if (this.user.id === item.userId) {
        this.$message.error("您无须下载自己的作品")
        return;
      }
      if (!this.user?.id) {
        this.$message.warning("请先登录")
        return
      }

      try {
        // 添加下载确认弹窗
        await MessageBox.confirm(
            `确定要下载《${item.name}》并消耗 ${item.count} 积分吗？`,
            '下载确认',
            {
              confirmButtonText: '确定',
              cancelButtonText: '取消',
              type: 'warning'
            }
        )

        // 用户确认后执行原有下载逻辑
        const res = await this.request.get(`/creation/userAddCount/${item.userId}/creat/${item.count}/item/${item.id}`)
        if (res.code === '200') {
          if (res.data && res.data.includes('已下载过此作品')) {
            this.$message.success(res.data) // 提示不扣积分
            this.startDownload(item.img)   // 直接触发下载
            this.load()
          } else {
            this.$message.success("下载开始，请稍候...")
            this.startDownload(item.img)   // 正常下载逻辑
            this.load()                    // 刷新页面数据
          }
        } else {
          this.$message.error(res.msg || '操作失败')
        }
      } catch (error) {
        // 过滤取消操作
        if (error !== 'cancel') {
          this.$message.error(error.response?.data?.msg || '下载失败')
        }
      }
    },

    collect(){
      if (this.user.id === this.users.id) {
        this.$message.error("您无须收藏自己的作品")
        return;
      }
      const data ={
        itemId : this.id
      }
      this.request.post('/collect',data).then(res=>{
        if (res.code==='200'){
          this.$message.success('收藏成功')
        }else {
          this.$message.error(res.msg)
        }
      })
    },
    loadComment() {
      this.request.get("/comment/tree/" + this.commentItemId).then(res => {
        this.comments = res.data
      })
    },

    save() {
      if (!this.user.id) {
        this.$message.warning("请登录后操作")
        return
      }
      this.commentForm.itemId = this.commentItemId
      if (this.commentForm.contentReply) {
        this.commentForm.content = this.commentForm.contentReply
      }
      this.request.post("/comment", this.commentForm).then(res => {
        if (res.code === '200') {
          this.$message.success("评论成功")
          this.commentForm = {}  // 初始化评论对象内容
          this.loadComment()
          this.dialogFormVisible2 = false
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    del(id) {
      this.request.delete("/comment/" + id).then(res => {
        if (res.code === '200') {
          this.$message.success("删除成功")
          this.loadComment()
        } else {
          this.$message.error("删除失败")
        }
      })
    },
    handleReply(pid) {
      this.commentForm = { pid: pid }
      this.dialogFormVisible2 = true
    },
  }
}
</script>

<style scoped>
.comment-send-warp {
  position: relative;
}
.comment-send-warp .btn-sent-comment {
  position: absolute;
  bottom: 35px;
  right: 30px
}
.comment-send-input{
  padding: 20px 10px;
  font-size: 16px;

}
.el-textarea__inner {
  padding: 15px ;
  font-family: '微软雅黑';
}
.comment-warp {
  padding: 0 10px;
}
.comment-item {
  display: flex;
  width: 100%;
  border-bottom: 1px solid #eee;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.comment-item-reply {
  margin-left: 22px;
  margin-right: -30px;
  transform: scale(0.9);
  padding-left: 10px;
  border-radius: 4px;
}
.comment-item  .comment-avatar  {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  overflow: hidden;
  margin-right: 20px;
}

.comment-item  .comment-avatar img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.comment-info {
  flex: 1;
}
.comment-info .comment-name {
  color: #8c939d;
  font-size: 14px;
}
.comment-info .comment-content {
  color: #8c939d;
  font-size: 14px;
  margin-top: 4px;
  font-size: 16px;
  color: #666;
  text-align: justify;
}
.comment-info .comment-other {
  display: flex;
  /*justify-content: space-between;*/
  align-items: center;
  font-size: 14px;
  margin-top: 2px;
  color: #9499A0;
}
.comment-info .comment-other  .comment-time {
  margin-right: 20px;
  min-width: 170px;
}
.comment-info .comment-other  .comment-time i {
  margin-right: 4px;
}
.comment-info .comment-other .el-button {
  color: #9499A0;
}
/* 添加全局响应式适配 */
@media (max-width: 768px) {
  .container {
    width: 90% !important;
  }
  .author-card {
    display: none; /* 移动端隐藏作者卡片 */
  }
}
</style>
