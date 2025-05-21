<template>
  <div class="search-results">
    <!-- 搜索框 -->
    <el-card class="search-card">
      <div class="search-container">
        <el-input
            v-model="searchKeyword"
            placeholder="搜索作品..."
            class="modern-search"
            @keyup.enter.native="handleSearch"
        >
          <template #append>
            <el-button
                type="primary"
                class="search-action-btn"
                @click="handleSearch"
            >
              <i class="el-icon-search"></i>
              搜索
            </el-button>
          </template>
        </el-input>
      </div>

      <!-- 新增样式筛选 -->
      <div class="filter-container">
        <el-select
            v-model="selectedStyle"
            placeholder="选择类型"
            class="style-select"
            @change="handleStyleChange"
        >
          <el-option
              v-for="item in styleOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
          />
        </el-select>

      <!-- 分类筛选 -->
        <div class="type-filter">
          <div
              v-for="type in types"
              :key="type.id"
              class="type-item"
              :class="{ active: selectedTypeId === type.id }"
              @click="handleTypeClick(type.id)"
          >
            {{ type.name }}
          </div>
        </div>
      </div>

      <!-- 统计信息 -->
      <div class="stats-counter" v-if="items.length > 0">
        <span class="count-text">共找到</span>
        <span class="count-number">{{ items.length }}</span>
        <span class="count-text">个作品</span>
      </div>
    </el-card>

    <!-- 瀑布流布局 -->
    <div class="result-container">
      <waterfall
          :col="colCount"
          :width="itemWidth"
          :data="items"
          :gutter="20"
      >
        <div
            class="result-item"
            v-for="(item, index) in items"
            :key="index"
            @click="$router.push(`/front/detail?id=${item.id}`)"
        >
          <div class="image-wrapper">
            <img
                :src="item.img"
                class="preview-image"
                :style="{height: imageHeight + 'px'}"
                @error="handleImageError"
            >
            <div class="image-meta">
              <el-tag effect="dark" type="warning" v-if="item.style">{{ item.style }}</el-tag>
            </div>
          </div>

          <div class="item-footer">
            <div class="author-info">
              <img
                  :src="item.avatarUrl || require('@/assets/login.png')"
                  class="author-avatar"
              >
              <span class="author-name">{{ item.nickname || '未知作者' }}</span>
            </div>
            <div class="item-name">{{ item.name }}</div>
          </div>
        </div>
      </waterfall>

      <!-- 空状态 -->
      <el-empty
          v-if="items.length === 0"
          description="没有找到相关作品"
          class="empty-tip"
      >
        <img
            src="@/assets/svg/empty.svg"
            style="width: 200px; margin-top: 20px;"
        >
      </el-empty>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      selectedStyle: '',
      styleOptions: [
        { value: '', label: '全部类型' },
        { value: '原创', label: '原创' },
        { value: '临摹', label: '临摹' },
        { value: 'AI辅助', label: 'AI辅助' }
      ],
      types: [
        { id: 0, name: "全部" },
        { id: 1, name: "官方图标" },
        { id: 2, name: "多彩图标" },
        { id: 3, name: "单色图标" }
      ],
      selectedTypeId: 0,
      searchKeyword: '',
      items: [],
      // 响应式布局参数
      colCount: 9,
      itemWidth: 60, // 正方形宽度
      imageHeight:60// 保持与宽度一致
    }
  },
  created() {
    this.calculateLayout();
    window.addEventListener('resize', this.calculateLayout);
    if (this.$route.query.keyword) {
      this.searchKeyword = this.$route.query.keyword;
      this.loadResults();
    }
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.calculateLayout)
  },
  methods: {
    handleStyleChange() {
      this.loadResults();
    },
    calculateLayout() {
      const width = window.innerWidth
      this.colCount = width > 1920 ? 9 : width > 1440 ? 7 : width > 768 ? 3 : 2
      this.itemWidth = Math.floor(window.innerWidth / this.colCount - 12)
      this.imageHeight = this.itemWidth // 保持正方形
    },
    handleImageError(e) {
      e.target.src = require('@/assets/login.png')
    },
    handleSearch() {
      if (!this.searchKeyword.trim()) return
      this.$router.replace({
        query: { keyword: this.searchKeyword.trim() }
      })
      this.loadResults()
    },
    async loadResults() {
      try {
        const params = {
          pageNum: 1,
          pageSize: 20,
          name: this.searchKeyword.trim(),
          typeId: this.selectedTypeId || undefined,// 传递分类参数
          style: this.selectedStyle || undefined,// 新增样式参数
        };

        // 过滤空值参数
        Object.keys(params).forEach(key => {
          if (!params[key] && params[key] !== 0) delete params[key]
        });

        const res = await this.request.get("/creation/front/page", { params });
        if (res.code === '200') {
          this.items = res.data?.records || [];
        }
      } catch (error) {
        console.error('搜索失败:', error);
        this.items = [];
      }
    },
    handleTypeClick(typeId) {
      this.selectedTypeId = typeId;
      this.loadResults();
    },
  }
}
</script>

<style lang="scss" scoped>
@mixin text-ellipsis($line: 1) {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: $line;
  overflow: hidden;
  text-overflow: ellipsis;
}

.filter-container {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 20px;
  margin-top: 15px;
  padding: 0 20px;
  flex-wrap: wrap;
}

.style-select {
  flex: 0 0 auto;
  width: 180px;
  v-deep .el-input__inner {
    height: 36px;
    border-radius: 18px;
    border: 1px solid #DCDFE6;
    &:focus {
      border-color: #6B8DD6;
    }
  }
}

.type-filter {
  flex: 0 0 auto;
  display: flex;
  gap: 15px;
  flex-wrap: wrap;
  justify-content: center;

  .type-item {
    padding: 8px 15px;
    border-radius: 20px;
    background: #f5f5f5;
    color: #666;
    cursor: pointer;
    transition: all 0.3s;
    font-size: 14px;

    &.active {
      background: #6B8DD6;
      color: white;
      font-weight: 500;
    }

    &:hover:not(.active) {
      background: #e0e0e0;
    }
  }
}

/* 显眼的统计信息 */
.stats-counter {
  padding: 12px 20px 0;
  font-size: 18px;
  font-weight: 700;
  text-align: left;

  .count-number {
    color: #F56C6C;
    margin: 0 4px;
  }
  .count-text {
    color: #333;
  }
}

.search-card {
  margin-bottom: 20px;
  border: none;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);


  v-deep .el-card__body {
    padding: 20px;
  }
}

.search-container {
  max-width: 600px;
  margin: 0 auto;
}

.modern-search {
  v-deep .el-input__inner {
    height:44px;
    border-radius: 6px;
    border: 2px solid #6B8DD6;
    font-size: 16px;
    padding: 0 20px;
    transition: all 0.3s;

    &:focus {
      border-color: #8E37D7;
      box-shadow: 0 0 12px rgba(142, 55, 215, 0.2);
    }
  }
}

.search-action-btn {
  height: 44px !important;
  border-radius: 8px !important;
  background: linear-gradient(135deg, #6B8DD6 0%, #8E37D7 100%);
  border: none !important;
  padding: 0 25px !important;
  font-size: 16px;

  i {
    margin-right: 8px;
  }

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(107, 141, 214, 0.4);
  }
}

.result-item {
  margin-bottom: 10px;
  background: #fff;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: transform 0.3s;
  cursor: pointer;

  &:hover {
    transform: translateY(-5px);
  }
}

.image-wrapper {
  position: relative;
  overflow: hidden;

  .preview-image {
    width: 100%;
    height: 100%;
    object-fit: cover;
    border-radius: 8px 8px 0 0;
    transition: transform 0.3s;

    &:hover {
      transform: scale(1.05);
    }
  }

  .image-meta {
    position: absolute;
    bottom: 10px;
    left: 10px;
  }
}

.item-footer {
  padding: 15px;

  .author-info {
    display: flex;
    align-items: center;
    margin-bottom: 10px;

    .author-avatar {
      width: 32px;
      height: 32px;
      border-radius: 50%;
      margin-right: 8px;
      border: 2px solid #fff;
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    }

    .author-name {
      font-size: 14px;
      color: #666;
    }
  }

  .item-name {
    font-size: 16px;
    font-weight: 500;
    color: #333;
    @include text-ellipsis(2);
  }
}

.empty-tip {
  padding: 50px 0;

  v-deep .el-empty__description {
    margin-top: 20px;
    font-size: 18px;
    color: #999;
  }
}

@media (max-width: 768px) {
  .preview-image {
    height: 80px !important;
  }

  .author-avatar {
    width: 28px !important;
    height: 28px !important;
  }

  .item-name {
    font-size: 14px !important;
  }
}

@media (max-width: 480px) {
  .preview-image {
    height: 60px !important;
  }

  .result-item {
    margin-bottom: 6px;
    border-radius: 6px;
  }

  .item-footer {
    padding: 10px;
  }
}
</style>