<template>
  <div class="container">
    <div class="main-layout">
      <!-- 左侧图标库列表 -->
      <div class="sidebar">
        <h2>图标库列表</h2>
        <ul>
          <li
              v-for="collection in collections"
              :key="collection.prefix"
              @click="loadIcons(collection.prefix)"
              :class="{ active: currentCollection?.prefix === collection.prefix }"
          >
            {{ collection.name }}
            <!--            ({{ collection.total }})-->
          </li>
        </ul>
      </div>

      <!-- 中间图标列表 -->
      <div class="icon-panel">
        <!-- 搜索框 -->
        <div class="search-box">
          <input
              type="text"
              v-model="searchQuery"
              placeholder="搜索图标名称..."
              @input="handleSearch"
          />
        </div>
        <div class="icon-container" ref="iconContainer" @scroll="handleScroll">
          <div
              v-for="(icon, index) in filteredIcons"
              :key="`${icon}-${index}`"
              class="icon-item"
              @click="selectIcon(icon)"
              :class="{ selected: selectedIcon === `${currentCollection.prefix}:${icon}` }"
          >
            <img
                :data-src="iconUrl(icon)"
                :alt="icon"
                class="icon-image lazy"
                @load="handleImageLoad"
                @error="handleImageError"
            />
            <div class="icon-name">{{ icon }}</div>
          </div>
          <div v-if="showLoadMore" class="loading-more">加载更多...</div>
        </div>
      </div>

      <!-- 右侧预览区 -->
      <div class="preview-panel">
        <div v-if="selectedIcon" class="preview-container">
          <!--          <h2>图标预览</h2>-->
          <div class="preview-layout">
            <div class="preview-wrapper">
              <Icon
                  :icon="selectedIcon"
                  :width="iconSize"
                  :height="iconSize"
                  :style="{
                  color: iconColor,
                  stroke: iconColor,
                  strokeWidth: effectiveStrokeWidth
                }"
              />
            </div>
            <div class="control-area">
              <div class="controls">
                <div class="control-group">
                  <label>颜色设置</label>
                  <input type="color" v-model="iconColor" />
                </div>
                <div class="control-group">
                  <label>尺寸设置 {{ iconSize }}px</label>
                  <input
                      type="range"
                      min="16"
                      max="256"
                      v-model="iconSize"
                  />
                </div>
                <div class="control-group">
                  <label>线条粗细 {{ iconStrokeWidth }}px</label>
                  <input
                      type="range"
                      min="1"
                      max="10"
                      v-model="iconStrokeWidth"
                  />
                </div>
              </div>
              <div class="action-buttons">
                <button @click="copySvgCode">复制SVG代码</button>
                <button @click="downloadPng">下载PNG</button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { Icon } from '@iconify/vue2';
import { Canvg } from 'canvg';
import { getIcon } from '@iconify/iconify';

export default {
  components: { Icon },
  data() {
    return {
      collections: [],
      currentCollection: null,
      allIcons: [],
      visibleIcons: [],
      currentPage: 1,
      pageSize: 60,
      loadingMore: false,
      observer: null,
      iconsLoaded: false,

      selectedIcon: null,
      iconColor: '#000000',
      iconSize: 128,
      iconStrokeWidth: 1,
      iconViewBox: { width: 24, height: 24 },
      searchQuery: '',
    };
  },
  computed: {
    // 过滤后的图标列表
    filteredIcons() {
      const icons = this.searchQuery
          ? this.allIcons.filter(icon =>
              icon.toLowerCase().includes(this.searchQuery.toLowerCase())
          )
          : this.allIcons;

      return icons.slice(0, this.currentPage * this.pageSize);
    },
    // 是否显示加载更多
    showLoadMore() {
      return !this.searchQuery && this.loadingMore;
    },
    paginatedIcons() {
      return this.allIcons?.slice(0, this.currentPage * this.pageSize) || [];
    },
    scaleRatio() {
      // 使用viewBox宽度作为基准，保持与SVG实际渲染逻辑一致
      const baseSize = this.iconViewBox.width || 24;
      return this.iconSize / baseSize;
    },
    effectiveStrokeWidth() {
      return this.iconStrokeWidth / (this.scaleRatio || 1);
    }
  },

  async mounted() {
    // 新增的兜底检测逻辑
    const timeoutHandler = setTimeout(() => {
      if (!this.collections.length) {
        this.$message.error('未能加载图标库');
      }
    }, 2000);
    try {
      const response = await fetch('http://localhost:3000/collections');
      const data = await response.json();
      this.collections = Object.keys(data).map(prefix => ({
        prefix,
        ...data[prefix]
      }));
      clearTimeout(timeoutHandler);
      if (this.collections.length > 0) {
        await this.loadIcons(this.collections[0].prefix);
        // 初始化第一个图标的viewBox
        if (this.visibleIcons.length > 0) {
          this.selectIcon(this.visibleIcons[0]);
        }
      }
    } catch (error) {
      console.error('加载图标库失败:', error);
      clearTimeout(timeoutHandler);
    }
  },

  methods: {
    filterIcons() {
      this.visibleIcons = this.filteredIcons.slice(0, this.currentPage * this.pageSize);
    },
    async loadIcons(prefix) {
      this.iconsLoaded = false;
      // this.$message.loading('图标加载中...');
      this.allIcons = [];  // 清空当前图标数据
      this.visibleIcons = [];  // 清空已显示的图标
      try {
        const response = await fetch(
            `http://localhost:3000/collection?prefix=${prefix}`
        );
        const data = await response.json();
        const hidden = data.hidden || [];
        this.currentCollection = this.collections.find(c => c.prefix === prefix);
        if (!this.currentCollection) {
          throw new Error(`找不到前缀为 ${prefix} 的图标库`);
        }

        const categories = data.categories ? Object.values(data.categories).flat() : [];
        const uncategorized = data.uncategorized || [];
        this.searchQuery = '';
        this.$set(this, 'allIcons', [...categories, ...uncategorized].filter(icon => !hidden.includes(icon)));
        this.currentPage = 1;
        this.visibleIcons = this.paginatedIcons;
        this.iconsLoaded = true;
        console.log('图标加载完成');
        // this.$message.destroy();
        this.$nextTick(() => {
          this.initLazyLoad();
          this.checkNeedMore();
        });
      } catch (error) {
        console.error('loadIcons失败:', error);
        this.$message.error(`图标库加载失败: ${error.message}`);
        this.iconsLoaded = true;
      }
    },
    handleScroll(event) {
      const { scrollTop, scrollHeight, clientHeight } = event.target;
      if (scrollHeight - (scrollTop + clientHeight) < 100) this.loadMore();
    },
    checkNeedMore() {
      this.$nextTick(() => {
        const container = this.$refs.iconContainer;
        if (container && container.clientHeight < window.innerHeight) {
          this.loadMore();
        }
        // 主动触发懒加载一次，避免图标太少 observer 不生效
        this.initLazyLoad();
      });
    },
    loadMore() {
      const icons = this.searchQuery
          ? this.allIcons.filter(icon =>
              icon.toLowerCase().includes(this.searchQuery.toLowerCase())
          )
          : this.allIcons;

      if (this.loadingMore || (this.currentPage * this.pageSize >= icons.length)) return;

      this.loadingMore = true;
      setTimeout(() => {
        this.currentPage++;
        this.loadingMore = false;
        this.$nextTick(() => this.initLazyLoad());
      }, 500);
    },
    initLazyLoad() {
      if (this.observer) this.observer.disconnect();
      this.observer = new IntersectionObserver(entries => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            const img = entry.target;
            img.src = img.dataset.src;
            this.observer.unobserve(img);
          }
        });
      }, { rootMargin: '100px 0px' });
      document.querySelectorAll('.lazy').forEach(img => this.observer.observe(img));
    },
    iconUrl(iconName) {
      return `http://localhost:3000/${this.currentCollection.prefix}/${iconName}.svg`;
    },
    handleImageLoad(event) {
      event.target.style.opacity = 1;
    },
    handleImageError(event) {
      console.error('图片加载失败:', event.target.src);
      event.target.style.display = 'none';
    },
    handleSearch() {
      // 搜索时重置分页
      this.currentPage = 1;
      this.filterIcons();
      this.checkNeedMore();
    },
    selectIcon(icon) {
      this.selectedIcon = `${this.currentCollection.prefix}:${icon}`;
      // 获取实际viewBox尺寸
      const iconData = getIcon(this.selectedIcon);
      if (iconData) {
        this.iconViewBox = {
          width: iconData.width,
          height: iconData.height
        };
      }
    },
    getSvgCode() {
      try {
        console.log('[DEBUG] selectedIcon:', this.selectedIcon); // 调试点1：确认选中的图标名称

        const iconData = getIcon(this.selectedIcon);
        if (!iconData) {
          console.error('[ERROR] iconData为空，请检查图标名称:', this.selectedIcon);
          return '';
        }

        console.log('[DEBUG] iconData结构:', {
          body: iconData.body?.slice(0, 100) + '...', // 预览前100字符
          width: iconData.width,
          height: iconData.height
        }); // 调试点2：确认iconData有效性

        // 计算当前实际的缩放比例
        const baseSize = this.iconViewBox.width || iconData.width || 24;
        const currentScaleRatio = this.iconSize / baseSize;

        const absoluteStrokeWidth = this.iconStrokeWidth / currentScaleRatio;

        // 核心清理逻辑
        let svgBody = iconData.body
            .replace(/<animate[\s\S]*?<\/animate>/gi, '')
            .replace(/<link[\s\S]*?(<\/link>|>)/gi, '')
            .replace(/<style[\s\S]*?<\/style>/gi, '')
            .replace(/<script[\s\S]*?<\/script>/gi, '')
            // 处理颜色
            .replace(/(<path[^>]*)(fill|stroke)="[^"]*"/gi, (_, p1, p2) => `${p1}${p2}="${this.iconColor}"`)
            // 强制设置stroke颜色和添加stroke属性
            .replace(/(<path)([^>]*>)/gi, (_, p1, p2) =>
                `${p1} stroke="${this.iconColor}" ${p2}`)
            // 处理stroke-width
            .replace(/(<path)((?![^>]*stroke-width)[^>]*>)/gi, (_, p1, p2) =>
                `${p1} stroke-width="${absoluteStrokeWidth}" ${p2}`)
            .replace(/(<path[^>]*)stroke-width="[^"]*"/gi, (_, p1) =>
                `${p1}stroke-width="${absoluteStrokeWidth}"`);

        const finalSVG = `<svg xmlns="http://www.w3.org/2000/svg"
      viewBox="0 0 ${iconData.width} ${iconData.height}"
      width="${this.iconSize}"
      height="${this.iconSize}"
      style="color:${this.iconColor}">
      ${svgBody}
    </svg>`.trim();

        console.log('[DEBUG] 生成的SVG代码:', finalSVG.slice(0, 200) + '...'); // 调试点3：检查SVG片段
        return finalSVG;

      } catch (error) {
        console.error('[ERROR] getSvgCode发生异常:', error);
        return '';
      }
    },

    copySvgCode() {
      try {
        const svg = this.getSvgCode(); // 直接获取同步返回值
        navigator.clipboard.writeText(svg)
            .then(() => alert('SVG代码已复制'))
            .catch(err => {
              console.error('复制失败:', err);
              this.fallbackCopy(svg);
            });
      } catch (err) {
        alert('生成SVG失败: ' + err);
      }
    },
    fallbackCopy(text) {
      const textArea = document.createElement('textarea');
      textArea.value = text;
      document.body.appendChild(textArea);
      textArea.select();
      try {
        document.execCommand('copy');
        alert('已使用备用方法复制');
      } catch (err) {
        alert('复制失败，请手动复制');
      }
      document.body.removeChild(textArea);
    },
    async downloadPng() {
      try {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        canvas.width = this.iconSize;
        canvas.height = this.iconSize;
        ctx.fillStyle = '#ffffff';
        ctx.fillRect(0, 0, canvas.width, canvas.height);
        const v = await Canvg.from(ctx, this.getSvgCode(), {
          ignoreAnimation: true,
          ignoreMouse: true,
          ignoreDimensions: false, // 改为false以考虑原始尺寸
          scaleWidth: this.iconSize,
          scaleHeight: this.iconSize,
          useCSSStyles: true
        });
        await v.render();
        const blob = await new Promise(resolve => canvas.toBlob(resolve, 'image/png', 1));
        const link = document.createElement('a');
        link.href = URL.createObjectURL(blob);
        link.download = `icon_${Date.now()}.png`;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        setTimeout(() => URL.revokeObjectURL(link.href), 100);
      } catch (error) {
        alert(`下载错误: ${error.message}`);
      }
    }
  },
  watch: {
    searchQuery() {
      // 确保分页重置
      this.currentPage = 1;
      this.filterIcons();
      this.checkNeedMore();
    }
  },
};
</script>

<style scoped>
.search-box {
  padding: 0 15px 15px;
  margin-bottom: 10px;
  border-bottom: 1px solid #eee;
}

.search-box input {
  width: 100%;
  padding: 10px 15px;
  border: 2px solid #dee2e6;
  border-radius: 25px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.search-box input:focus {
  outline: none;
  border-color: #2196f3;
  box-shadow: 0 0 8px rgba(33, 150, 243, 0.2);
}
.container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.main-layout {
  display: flex;
  gap: 20px;
  height: calc(100vh - 40px); /* 根据实际布局调整 */
  min-height: 500px; /* 添加最小高度保证可见性 */
}

/* 左侧边栏 */
.sidebar {
  width: 250px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar h2 {
  padding: 0 15px;
}

.sidebar ul {
  flex: 1;
  overflow-y: auto;
  padding: 0 15px;
}

.sidebar li {
  padding: 12px;
  margin: 4px 0;
  border-radius: 6px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.sidebar li:hover { background: #e9ecef; }
.sidebar li.active { background: #dee2e6; }

/* 中间图标面板 */
.icon-panel {
  flex: 1;
  min-width: 0;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.icon-container {
  flex: 1;
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  overflow-y: auto;
  padding: 10px;
}

.icon-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 15px;
  height: 100%;
  box-sizing: border-box;
  transition: transform 0.2s ease;
}

.icon-item:hover {
  transform: scale(1.05);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}

.icon-item.selected {
  border-color: #2196f3;
  background: #e3f2fd;
}

.icon-image {
  width: 48px;
  height: 48px;
  margin: 0 auto 8px;
  display: block;
}

.icon-name {
  font-size: 12px;
  color: #666;
  word-break: break-word;
  text-align: center;
}

.loading-more {
  text-align: center;
  padding: 20px;
  color: #666;
}

/* 右侧预览面板 */
.preview-panel {
  width: 400px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  padding: 20px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.preview-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.preview-container h2 {
  font-size: 1.4rem;
  color: #2c3e50;
  margin-bottom: 12px;
  border-bottom: 2px solid rgba(44, 62, 80, 0.1);
  padding-bottom: 8px;
}

/* 修复居中问题 */
.preview-wrapper {
  min-height: 300px;
  background: rgba(255, 255, 255, 0.6);
  border: 2px solid rgba(33, 150, 243, 0.15);
  border-radius: 12px;
  margin: 0;
  padding: 24px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  display: flex;
  align-items: center;
  justify-content: center;
}

.control-area {
  background: rgba(247, 249, 252, 0.8);
  border-radius: 10px;
  padding: 20px;
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.control-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 18px;
}

.control-group label {
  font-size: 0.9rem;
  color: #5a6d88;
  font-weight: 500;
}

input[type="color"] {
  width: 100%;
  height: 40px;
  border: 2px solid rgba(33, 150, 243, 0.2);
  border-radius: 8px;
  padding: 4px;
  background: white;
  cursor: pointer;
}

input[type="range"] {
  width: 100%;
  height: 6px;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 4px;
  outline: none;
  -webkit-appearance: none;
}

input[type="range"]::-webkit-slider-thumb {
  -webkit-appearance: none;
  width: 18px;
  height: 18px;
  background: #2196f3;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-top: 24px;
}

button {
  flex: 1;
  padding: 12px;
  background: linear-gradient(135deg, #2196f3, #1976d2);
  color: white;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-weight: 500;
  transition: all 0.2s ease;
  box-shadow: 0 2px 4px rgba(33, 150, 243, 0.2);
}

button:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(33, 150, 243, 0.3);
  opacity: 0.9;
}

button:active {
  transform: translateY(0);
  opacity: 1;
}

.lazy { opacity: 0; }
</style>