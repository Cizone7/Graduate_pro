<template>
  <div class="library-container">
    <!-- 返回按钮 -->
    <el-button
        type="info"
        icon="el-icon-arrow-left"
        @click="$router.go(-1)"
        style="margin-bottom: 20px"
    >返回</el-button>

    <!-- 图库信息卡片 -->
    <el-card v-if="libraryInfo">
      <div slot="header" class="clearfix">
        <span style="font-size: 24px">{{ libraryInfo.name }}</span>
        <el-tag type="info" style="float: right; margin-left: 10px">
          作品数量: {{ libraryInfo.creationCount }}
        </el-tag>
        <el-tag style="float: right">
          创建时间: {{ formatDate(libraryInfo.time) }}
        </el-tag>
      </div>

      <!-- 图片网格布局，每排最多 9 个 -->
      <el-row :gutter="10" v-loading="loading" type="flex" class="image-grid">
        <template v-for="(item, index) in creations">
          <!-- 9个为一组，单独换行 -->
          <el-row v-if="index % 9 === 0" :key="'row-' + index" class="row-break" />

          <el-col
              :key="item.id"
              :span="2.66"
              class="grid-item"
          >
            <div class="creation-card" @click="$router.push(`/front/detail?id=${item.id}`)">
              <el-image
                  :src="item.img"
                  fit="cover"
                  class="creation-image"
                  :preview-src-list="[item.img]"
              >
                <div slot="error" class="image-error">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
              <div class="creation-name">{{ item.name }}</div>
            </div>
          </el-col>
        </template>
      </el-row>

      <!-- 无数据提示 -->
      <div v-if="!loading && creations.length === 0" class="empty-tip">
        暂无作品
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  data() {
    return {
      libraryInfo: null,
      creations: [],
      loading: true,
      error: false,
      errorMessage: ''
    }
  },
  created() {
    this.loadData()
  },
  methods: {
    async loadData() {
      try {
        const libraryId = this.$route.query.id

        // 获取图库基本信息
        const libRes = await this.request.get(`/library/${libraryId}`)
        if (libRes.code === '200') {
          this.libraryInfo = libRes.data
        }

        // 获取作品列表（分页）
        const creRes = await this.request.get(`/library/${libraryId}/creations`, {
          params: { page: 1, size: 99 } // 调整为较大数值，确保加载所有作品
        })
        if (creRes.code === '200') {
          this.creations = creRes.data?.records || []
        }

        this.loading = false
      } catch (error) {
        this.error = true
        this.loading = false
        this.errorMessage = `加载失败: ${error.message}`
      }
    },
    formatDate(timestamp) {
      return new Date(timestamp).toLocaleString()
    }
  }
}
</script>

<style scoped>
.library-container {
  max-width: 1200px;
  margin: 20px auto;
  padding: 0 30px;
}

/* 控制图片网格的排版 */
.image-grid {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-start; /* 不居中，改为从左到右排 */
}

.row-break {
  width: 100%;
  height: 10px; /* 添加一些间距 */
}

.grid-item {
  margin-bottom: 15px; /* 增加图片之间的间距 */
  display: flex;
  margin-right: 15px;
  margin-left: 15px;
}

.creation-card {
  width: 100%;
  max-width: 80px; /* 更小的卡片尺寸 */
  position: relative;
  cursor: pointer;
  transition: transform 0.3s;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  flex-direction: column;
  align-items: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.1);
  padding: 5px; /* 增加卡片内部的 padding */
}

.creation-card:hover {
  transform: translateY(-5px);
}

.creation-image {
  width: 100%;
  aspect-ratio: 1 / 1;
  object-fit: cover;
  background-color: #f5f7fa;
  margin: 0 auto;
}

.image-error {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px; /* 图标本体更小 */
  color: #909399;
}

.creation-name {
  padding: 5px;
  text-align: center;
  font-size: 12px;
  color: #333;
  background-color: white;
}

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: #999;
  font-size: 16px;
}

@media (max-width: 768px) {
  .creation-card {
    max-width: 70px; /* 移动端更小尺寸 */
  }
  .creation-name {
    font-size: 10px;
  }
}
</style>
