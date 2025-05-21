<template>
  <div style="margin: 0 auto; width: 70%">
    <!-- 作者信息卡片 -->
    <el-card style="margin-top: 20px; border-radius: 12px">
      <el-button
          style="margin-bottom: 10px; float: right"
          type="info"
          @click="$router.go(-1)"
      >返回</el-button>

      <div class="author-profile">
        <img :src="item.avatarUrl" class="author-avatar">
        <div class="author-info">
          <h1 class="author-name">{{ item.nickname }}</h1>
          <p class="author-bio">{{ item.info || '这位作者暂时还没有留下简介~' }}</p>
        </div>
      </div>
    </el-card>

    <!-- 图库展示 -->
    <div class="section-title">
      <i class="el-icon-picture-outline"></i>
      <span>创作图库</span>
    </div>

    <el-row :gutter="20" style="margin-top: 15px">
      <el-col
          :span="6"
          v-for="library in libraries"
          :key="library.id"
          style="margin-bottom: 10px;"
      >
        <div @click="goLibrary(library.id)">
          <el-card class="library-card" shadow="hover">
            <div class="library-header" >
              <h3 class="library-title">{{ library.name }}</h3>
              <span class="creation-count">共 {{ library.creationCount }} 件作品</span>
            </div>

            <div class="thumbnail-canvas" :ref="'canvasContainer' + library.id">
              <canvas :ref="'canvas' + library.id"></canvas>
              <div v-if="!library.previews?.length" class="empty-preview">
                暂无预览
              </div>
            </div>
          </el-card>
        </div>
      </el-col>
    </el-row>

    <!-- 底部声明 -->
<!--    <el-divider class="footer-divider">-->
<!--      <span style="color: #8c939d; font-size: 12px">-->
<!--        本站部分内容来源于网络，图片版权属于原作者，内容仅供交流学习，禁止商业用途-->
<!--      </span>-->
<!--    </el-divider>-->
  </div>
</template>

<script>
export default {
  name: 'personal',
  data() {
    return {
      id: this.$route.query.id,
      item: {},
      libraries: [], // 存储图库数据
    }
  },
  async created() {
    await this.loadUser()
    await this.loadLibraries()
  },
  methods: {
    // 获取用户基本信息
    async loadUser() {
      try {
        const res = await this.request.get(`/user/${this.id}`)
        this.item = res.data
      } catch (e) {
        console.error('用户信息获取失败:', e)
      }
    },
    async load() {
      // 加载用户信息
      const userRes = await this.request.get('/user/' + this.id)
      this.item = userRes.data

      // 加载用户图库
      const librariesRes = await this.request.get(`/library/user/${this.id}`)
      this.libraries = librariesRes.data

      // 为每个图库加载作品缩略图
      this.libraries = await Promise.all(this.libraries.map(async library => {
        const creationsRes = await this.request.get(`/library/${library.id}/creations/simple`)
        return { ...library, creations: creationsRes.data }
      }))
    },
    // 获取用户图库数据
    async loadLibraries() {
      try {
        // 关键修改：使用与Home.vue相同的分页接口
        const res = await this.request.get("/library/front/page", {
          params: {
            pageNum: 1,
            pageSize: 100, // 获取全部数据
            userId: this.id // 添加用户ID过滤参数
          }
        })

        // 处理数据获取
        this.libraries = await Promise.all(
            (res.data?.records || []).map(async lib => {
              // 获取每个图库的预览作品
              const creRes = await this.request.get(`/library/${lib.id}/creations`, {
                params: { page: 1, size: 6}
              })
              lib.previews = creRes.data?.records || []
              return lib
            })
        )

        // 渲染Canvas
        this.$nextTick(() => {
          this.libraries.forEach(lib => {
            if(lib.previews.length > 0) {
              this.drawCanvas(lib.id, lib.previews)
            }
          })
        })
      } catch (e) {
        console.error('图库数据获取失败:', e)
      }
    },
    goLibrary(id) {
      this.$router.push({
        path: '/front/library',
        query: { id: id } // 更清晰的参数传递
      })
    },

    // Canvas绘制方法（从Home.vue复制）
    async drawCanvas(libId, previews) {
      const canvas = this.$refs[`canvas${libId}`][0]
      const container = this.$refs[`canvasContainer${libId}`][0]

      const size = 200 // 根据布局调整尺寸
      canvas.width = size
      canvas.height = size

      const ctx = canvas.getContext('2d')
      ctx.clearRect(0, 0, size, size)

      // 添加内边距参数
      const padding = size * 0.05 // 5%的内边距
      const spacing = size * 0.05 // 5%的间距
      const drawArea = size - padding * 2

      // 新的布局逻辑（带间距）
      const layouts = {
        1: [[0, 0, 1, 1]],
        2: [
          [0, 0, 0.5, 1],
          [0.5 + spacing/drawArea, 0, 0.5, 1]
        ],
        3: [
          [0, 0, 0.5, 0.5],
          [0.5 + spacing/drawArea, 0, 0.5, 0.5],
          [0, 0.5 + spacing/drawArea, 1, 0.5]
        ],
        4: [
          [0, 0, 0.5, 0.5],
          [0.5 + spacing/drawArea, 0, 0.5, 0.5],
          [0, 0.5 + spacing/drawArea, 0.5, 0.5],
          [0.5 + spacing/drawArea, 0.5 + spacing/drawArea, 0.5, 0.5]
        ],
        5: [
          [0, 0, 0.333, 0.5],
          [0.333 + spacing/drawArea, 0, 0.333, 0.5],
          [0.666 + spacing*2/drawArea, 0, 0.333, 0.5],
          [0, 0.5 + spacing/drawArea, 0.5, 0.5],
          [0.5 + spacing/drawArea, 0.5 + spacing/drawArea, 0.5, 0.5]
        ],
        6: [
          [0, 0, 0.333, 0.5],
          [0.333 + spacing/drawArea, 0, 0.333, 0.5],
          [0.666 + spacing*2/drawArea, 0, 0.333, 0.5],
          [0, 0.5 + spacing/drawArea, 0.333, 0.5],
          [0.333 + spacing/drawArea, 0.5 + spacing/drawArea, 0.333, 0.5],
          [0.666 + spacing*2/drawArea, 0.5 + spacing/drawArea, 0.333, 0.5]
        ]
      }
      const count = Math.min(previews.length, 6)
      const layout = layouts[count] || layouts[6]

      await Promise.all(previews.slice(0,6).map((preview, i) => {
        return new Promise((resolve) => {
          if (!layout[i]) return resolve()

          const img = new Image()
          img.onload = () => {
            const [rx, ry, rw, rh] = layout[i]
            const x = padding + rx * (drawArea - spacing*2)
            const y = padding + ry * (drawArea - spacing*2)
            const width = rw * (drawArea - spacing*3)
            const height = rh * (drawArea - spacing*3)

            // 保持1:1比例的核心修改
            const targetSize = Math.min(width, height)
            const offsetX = (width - targetSize) / 2
            const offsetY = (height - targetSize) / 2

            ctx.save()
            ctx.beginPath()
            ctx.rect(x, y, width, height)
            ctx.clip()

            // 居中裁剪绘制
            const imgRatio = img.width / img.height
            if (imgRatio > 1) { // 宽图
              const srcHeight = img.height
              const srcWidth = srcHeight * 1 // 强制1:1
              ctx.drawImage(
                  img,
                  (img.width - srcWidth)/2, 0, // 源图x,y
                  srcWidth, srcHeight,        // 源图宽高
                  x + offsetX, y + offsetY,   // 目标x,y
                  targetSize, targetSize      // 目标宽高
              )
            } else { // 高图
              const srcWidth = img.width
              const srcHeight = srcWidth * 1 // 强制1:1
              ctx.drawImage(
                  img,
                  0, (img.height - srcHeight)/2, // 源图x,y
                  srcWidth, srcHeight,           // 源图宽高
                  x + offsetX, y + offsetY,      // 目标x,y
                  targetSize, targetSize         // 目标宽高
              )
            }

            ctx.restore()
            resolve()
          }
          img.src = preview.img
        })
      }))

      // 绘制数量提示
      if(previews.length > 6) {
        ctx.fillStyle = 'rgba(0,0,0,0.6)'
        ctx.fillRect(size - 45, size - 25, 45, 25)
        ctx.fillStyle = 'white'
        ctx.font = '12px Arial'
        ctx.textAlign = 'right'
        ctx.fillText(`+${previews.length - 6}`, size - 8, size - 8)
      }
    }
  }
}
</script>

<style scoped>

/* 新增Canvas样式 */
.thumbnail-canvas {
  width: 200px;
  height:180px;
  background: #f8f9fa;
  border-radius: 8px;
  border: 1px solid #eee; /* 增加边框 */
  overflow: hidden;
}
.library-card:hover .thumbnail-canvas {
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empty-preview {
  width: 200px;
  height:180px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}
/* 作者信息样式 */
.author-profile {
  display: flex;
  align-items: center;
  padding: 20px 0;
}

.author-avatar {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  margin-right: 30px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
}

.author-info {
  flex: 1;
}

.author-name {
  font-size: 28px;
  margin: 0 0 10px 0;
  color: #2c3e50;
}

.author-bio {
  font-size: 14px;
  color: #7f8c8d;
  margin: 0;
  line-height: 1.6;
}

/* 模块标题 */
.section-title {
  font-size: 20px;
  color: #1abc9c;
  margin: 30px 0 15px;
  display: flex;
  align-items: center;
}

.section-title i {
  margin-right: 8px;
}

/* 图库卡片样式 */
.library-card {
  height: 260px;
  width: 240px;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s;

}

.library-card:hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.15);
  transform: translateY(-3px);
}

.library-header {
  margin-bottom: 12px;
}

.library-title {
  font-size: 16px;
  margin: 0;
  color: #34495e;
}

.creation-count {
  font-size: 12px;
  color: #95a5a6;
}

/* 底部声明 */
.footer-divider {
  margin: 40px 0 20px;
}
</style>