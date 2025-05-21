<template>
  <div class="home-container">
    <div style="margin: 10px 0">
      <el-carousel height="300px" :interval="10000">
        <el-carousel-item v-for="item in imgs" :key="item">
          <img :src="item" style="width: 100%;height: 100%">
        </el-carousel-item>
      </el-carousel>
    </div>
    <div style="font-size: 32px; margin-bottom: 15px; margin-top: 15px; display: inline-block">
      <span style="margin-left: 1px; color: #1abc9c">热门作者榜</span>
    </div>
    <div>

      <el-row :gutter="10">
        <el-col :span="4" v-for="item in rank" :key="item.id" style="margin-bottom: 10px">
          <div
              style="border: 1px solid #ccc; display: flex;align-items: center;justify-content: space-around;padding-bottom: 10px; border-radius: 10px; overflow: hidden; cursor: pointer"
              @click="$router.push('/front/personal?id=' + item.id)">
            <img :src="item.avatarUrl"
                 style="margin-top:5px;width: 60px;height: 60px;border-radius: 50%;overflow: hidden;padding: 2px;">
            <div style="color: #000; margin: 10px" class="info"><i class="el-icon-user"></i> {{ item.nickname }}</div>
          </div>
        </el-col>
      </el-row>
    </div>

    <el-card class="search-card">
      <div class="search-container">
        <el-input
            v-model="name"
            placeholder="搜索作品..."
            class="elegant-search"
            @keyup.enter.native="handleSearch"
        >
          <template #append>
            <el-button
                type="primary"
                class="search-btn"
                @click="handleSearch"
            >
              立即搜索
            </el-button>
          </template>
        </el-input>
      </div>
    </el-card>

    <div style="margin-top: 20px;margin-left: 25px">
      <waterfall :col="6" :width="170" :data="items">

        <div class="cell-item" v-for="(item, index) in items" :key="index"
             @click="$router.push(`/front/library?id=${item.id}`)">
          <div class="library-preview" :ref="'canvasContainer'+index">
            <canvas :ref="'canvas'+index"></canvas>
            <div v-if="!item.previews?.length" class="empty-preview">
              暂无预览
            </div>
          </div>
          <div class="item-body">
            <div class="item-desc">
              <span style="margin-right: 10px">{{ item.name }}</span>
            </div>
            <div class="item-footer">
              <div class="footer-left">
                <img class="item-img" :src="item.avatarUrl || require('@/assets/login.png')" alt="User Avatar"/>
                <div class="name">{{ item.nickname || '未知用户' }}</div>
              </div>
            </div>
          </div>
        </div>
      </waterfall>

    </div>

    <div style="height:60px;margin-bottom: 50px"></div>

  </div>
</template>

<script>

export default {
  name: "",
  data() {
    return {
      items: [],
      types: [],
      typeId: 0,
      name: '',
      pageNum: -1,
      pageSize: -1,
      total: 0,
      users: [],
      rank: [],
      imgs: [
        "https://img2.huashi6.com/images/resource/2020/11/20/85h793202p0.jpg?imageMogr2/quality/100/interlace/1/thumbnail/2000x%3E",
          "https://haowallpaper.com/link/common/file/previewFileImg/16445126326406528",
          "https://haowallpaper.com/link/common/file/previewFileImg/16552651290955136",
      ],
    };
  },
  created() {
    this.load()
    this.loadType()
    this.request.get("/user/front/count").then(res => {
      this.rank = res.data
    })

  },
  methods: {
    handleSearch() {
      const keyword = this.name.trim()
      if (keyword) {
        this.$router.push({
          path: '/front/search',
          query: { keyword: keyword }
        })
      }
    },
    load() {
      this.request.get("/library/front/page", {
        params: {
          pageNum: this.pageNum,
          pageSize: this.pageSize,
          name: this.name,
          typeId: this.typeId
        }
      }).then(async res => {
        if (res.code === '200') {
          this.items = await Promise.all(
              (res.data?.records || []).map(async (item, index) => {
                // 获取图库的前4张作品
                const creRes = await this.request.get(`/library/${item.id}/creations`, {
                  params: { page: 1, size: 6 }
                });
                item.previews = creRes.data?.records?.slice(0,6) || [];
                return item;
              })
          );

          // 等待DOM更新后绘制Canvas
          this.$nextTick(() => {
            this.items.forEach((item, index) => {
              if(item.previews.length > 0) {
                this.drawCanvas(index, item.previews);
              }
            });
          });
        }
      })
    },
    // 新增Canvas绘制方法
    async drawCanvas(index, previews) {
      const canvas = this.$refs['canvas'+index][0];
      const container = this.$refs['canvasContainer'+index][0];

      // 设置Canvas尺寸
      const size = 170; // 保持与瀑布流宽度一致
      canvas.width = size;
      canvas.height = size;

      const ctx = canvas.getContext('2d');
      ctx.clearRect(0, 0, size, size);

      // 根据图片数量选择布局
      const layouts = {
        1: [[0,0,1,1]],
        2: [[0,0,0.5,1], [0.5,0,0.5,1]],
        3: [[0,0,0.5,0.5], [0.5,0,0.5,0.5], [0,0.5,1,0.5]],
        4: [[0,0,0.5,0.5], [0.5,0,0.5,0.5],
          [0,0.5,0.5,0.5], [0.5,0.5,0.5,0.5]],
        5: [[0,0,0.333,0.5], [0.333,0,0.333,0.5], [0.666,0,0.333,0.5],
          [0,0.5,0.5,0.5], [0.5,0.5,0.5,0.5]],
        6: [[0,0,0.333,0.5], [0.333,0,0.333,0.5], [0.666,0,0.333,0.5],
          [0,0.5,0.333,0.5], [0.333,0.5,0.333,0.5], [0.666,0.5,0.333,0.5]]
      };
      const layout = layouts[Math.min(previews.length,6)] || layouts[6];

      // 添加内边距和间距
      const padding = size * 0.05;
      const spacing = size * 0.03;
      const drawArea = size - padding*2;

      // 异步加载所有图片
      await Promise.all(previews.slice(0,6).map((preview, i) => {
        return new Promise((resolve) => {
          if(i >= layout.length) return resolve();

          const img = new Image();
          img.onload = () => {
            const [rx, ry, rw, rh] = layout[i];
            const x = padding + rx * (drawArea - spacing);
            const y = padding + ry * (drawArea - spacing);
            const width = rw * (drawArea - spacing*2);
            const height = rh * (drawArea - spacing*2);

            // 保持1:1比例绘制
            const targetSize = Math.min(width, height);
            const offsetX = (width - targetSize)/2;
            const offsetY = (height - targetSize)/2;

            ctx.save();
            ctx.beginPath();
            ctx.rect(x, y, width, height);
            ctx.clip();

            // 居中裁剪逻辑
            const imgRatio = img.width / img.height;
            if(imgRatio > 1) {
              ctx.drawImage(
                  img,
                  (img.width - img.height)/2, 0,
                  img.height, img.height,
                  x + offsetX, y + offsetY,
                  targetSize, targetSize
              );
            } else {
              ctx.drawImage(
                  img,
                  0, (img.height - img.width)/2,
                  img.width, img.width,
                  x + offsetX, y + offsetY,
                  targetSize, targetSize
              );
            }

            ctx.restore();
            resolve();
          };
          img.src = preview.img;
        });
      }));
      if(previews.length > 6) {
        ctx.fillStyle = 'rgba(0,0,0,0.6)';
        ctx.fillRect(size-40, size-20, 40, 20);
        ctx.fillStyle = 'white';
        ctx.font = '12px Arial';
        ctx.fillText(`+${previews.length-6}`, size-30, size-5);
      }
    },
    loadType() {
      this.request.get("/type").then(res => {
        this.types = res.data
      })
    },
  }
};
</script>

<style lang="scss" scoped>

.waterfall {
  column-count: 6; /* 6列布局 */
  column-gap: 10px;
}

.search-card {
  margin-top: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: none;

  v-deep .el-card__body {
    padding: 20px;
  }
}

.elegant-search {
  v-deep .el-input__inner {
    padding-left: 15px !important;
  }
  v-deep .el-input-group__prepend {
    background: linear-gradient(135deg, #6B8DD6 0%, #8E37D7 100%);
    border: none;
    padding: 0 15px;

    .search-icon {
      color: white;
      font-size: 18px;
    }
  }

  v-deep .el-input__inner {
    height: 48px;
    border-radius: 8px 0 0 8px;
    border: 2px solid #6B8DD6;
    border-right: none;
    font-size: 16px;
    padding-left: 45px;
    transition: all 0.3s;

    &:focus {
      border-color: #8E37D7;
      box-shadow: 0 0 8px rgba(142, 55, 215, 0.2);
    }
  }
}

.search-btn {
  height: 48px !important;
  border-radius: 0 8px 8px 0 !important;
  background: linear-gradient(135deg, #6B8DD6 0%, #8E37D7 100%);
  border: none !important;
  padding: 0 30px !important;
  font-weight: 500;
  letter-spacing: 1px;
  transition: all 0.3s;

  &:hover {
    opacity: 0.9;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(107, 141, 214, 0.4);
  }
}

.library-preview {
  width: 150px;
  height: 150px;
  border-radius: 8px;
  overflow: hidden;
  position: relative;
  background: #f8f9fa;
  border: 1px solid #eee;

  canvas {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.info {
  height: 15px;
  line-height: 1.2;
  word-break: break-all;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 1; /* 这里是超出几行省略 */
  overflow: hidden;
}

.home-container {
  width: 75%;
  margin: 0 auto;

  .type-container {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .type-left {
      flex: 1;
      display: flex;
      align-items: center;
    }

    .type-item {
      flex: 1;
      text-align: center;
    }

    .type-search {
      display: flex;
    }
  }

  .cell-item {
    width: 100%;
    height: auto;
    background: #ffffff;
    border-radius: 6px;
    overflow: hidden;
    box-sizing: border-box;
    cursor: pointer;
    break-inside: avoid;
    margin-bottom: 15px;

    img {
      display: none;
    }

    .item-body {
      margin: 9px;

      .item-desc {
        font-size: 14px;
        margin-bottom: 8px;
      }

      .item-footer {
        display: flex;
        justify-content: space-between;

        .footer-left {
          display: flex;
          align-items: center;
          font-family: SF Pro Display;
          font-style: normal;
          font-weight: normal;
          font-size: 12px;
          line-height: 14px;
          margin-top: 7px;
          color: rgba(0, 0, 0, 0.6);

          img {
            border-radius: 50%;
            width: 22px;
            height: 22px;
            margin-right: 4px;
          }

          .item-img {
            height: 35px;
            width: 35px;
            object-fit: contain;
            align-self: flex-start;
          }
        }
      }
    }

  }

}
</style>