<template>
  <div class="container">

    <div class="main-layout">
      <!-- 三栏布局 -->
      <div class="upload-box">
        <h2>内容图标</h2>
        <div class="preview-container" @click="triggerContentInput">
          <div class="image-wrapper">
            <img v-if="contentPreview" :src="contentPreview" class="preview-img">
            <div v-else class="upload-label">
              <i>📷</i>
              <span>点击上传</span>
            </div>
          </div>
          <input
              type="file"
              ref="contentInput"
              accept="image/*"
              @change="handleContentUpload"
              hidden
          >
        </div>
      </div>

      <div class="divider"></div>

      <div class="upload-box">
        <h2>风格图标</h2>
        <div class="preview-container" @click="triggerStyleInput">
          <div class="image-wrapper">
            <img v-if="stylePreview" :src="stylePreview" class="preview-img">
            <div v-else class="upload-label">
              <i>🎨</i>
              <span>点击上传</span>
            </div>
          </div>
          <input
              type="file"
              ref="styleInput"
              accept="image/*"
              @change="handleStyleUpload"
              hidden
          >
        </div>
      </div>

      <div class="divider" v-if="resultUrl"></div>

      <div class="result-box" v-if="resultUrl">
        <h2>生成结果</h2>
        <div class="preview-container">
          <img class="preview-img" :src="resultUrl">
        </div>
        <button @click="downloadImage" class="download-btn">⬇️ 下载图标</button>
      </div>

      <!-- 底部按钮 -->
      <div class="action-footer">
        <button @click="processImages" :disabled="processing">
          <span v-if="!processing">✨ 开始转换</span>
          <span v-else>🔄 处理中...</span>
        </button>
      </div>
    </div>

    <!-- 加载提示和错误弹窗保持原样 -->
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      contentFile: null,
      styleFile: null,
      contentPreview: null,
      stylePreview: null,
      processing: false,
      error: null,
      resultUrl: null
    };
  },
  watch: {
    error(newVal) {
      if (newVal) {
        this.$nextTick(() => {
          this.$refs.errorModal?.focus();
        });
      }
    }
  },
  methods: {
    triggerContentInput() {
      this.$refs.contentInput.click();
    },
    triggerStyleInput() {
      this.$refs.styleInput.click();
    },
    handleContentUpload(e) {
      this.handleFileUpload(e, "content");
    },
    handleStyleUpload(e) {
      this.handleFileUpload(e, "style");
    },
    handleFileUpload(e, type) {
      const file = e.target.files[0];
      if (!file) return;

      if (!file.type.startsWith("image/")) {
        this.error = "仅支持图片文件格式";
        return;
      }

      this[`${type}File`] = file;
      this.previewImage(file, type);
    },
    previewImage(file, type) {
      const reader = new FileReader();
      reader.onload = (e) => {
        this[`${type}Preview`] = e.target.result;
      };
      reader.readAsDataURL(file);
    },
    async processImages() {
      this.error = null;
      this.resultUrl = null;

      if (!this.contentFile || !this.styleFile) {
        this.error = "请先上传内容和风格图片";
        return;
      }

      this.processing = true;

      try {
        const formData = new FormData();
        formData.append("content", this.contentFile);
        formData.append("style", this.styleFile);

        const response = await axios.post(
            "http://localhost:5000/stylize",
            formData,
            {
              headers: { "Content-Type": "multipart/form-data" },
              timeout: 30000  // 延长超时时间
            }
        );

        if (response.data.status === "success") {
          // 直接从响应中提取文件名，确保路径正确
          const outputFilename = response.data.output_url.split('/').pop();
          this.resultUrl = `http://localhost:5000/output/${outputFilename}`;
        } else {
          throw new Error(response.data.error || "处理失败");
        }
      } catch (err) {
        this.error = err.response?.data?.error || err.message || "服务器连接失败";
        console.error("请求错误:", err);
      } finally {
        this.processing = false;
      }
    },
    downloadImage() {
      if (!this.resultUrl) return;
      // 使用正确的URL拼接方式，避免重复添加路径
      const downloadUrl = `${this.resultUrl}?download=true`;
      const link = document.createElement('a');
      link.href = downloadUrl;
      link.setAttribute('download', ''); // 允许浏览器自动获取文件名
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
    },
  }
};
</script>

<style scoped>
.download-btn {
  margin-top: 1rem;
  padding: 0.5rem 1.5rem;
  background: #3498db;
  color: white;
  border-radius: 20px;
  cursor: pointer;
  transition: background 0.3s;
  display: inline-block; /* 确保按钮可居中 */
}

.download-btn:hover {
  background: #2980b9;
}
.container {
  max-width: 1200px;
  padding: 2rem;
  margin: 0 auto;
  min-height: 100vh;
}

h1 {
  text-align: center;
  margin-bottom: 2rem;
}

.main-layout {
  display: flex;
  justify-content: center;
  gap: 2rem;
  flex-wrap: wrap;
  position: relative;
  padding-bottom: 80px; /* 给底部按钮留空间 */
}

.upload-box {
  background: #fff;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 280px;
}
.result-box {
  text-align: center;
  background: #fff;
  padding: 1.5rem;
  border-radius: 16px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  width: 280px;
}

.divider {
  width: 1px;
  background: repeating-linear-gradient(
      180deg,
      transparent,
      transparent 10px,
      #bdc3c7 10px,
      #bdc3c7 20px
  );
  margin: 0 1rem;
}

.preview-container {
  width: 250px;
  height: 250px;
  border: 2px dashed #bdc3c7;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.result-box .preview-container {
  border-style: solid;
  border-color: #eee;
}

.preview-img {
  max-width: 90%;
  max-height: 90%;
  object-fit: contain;
  border-radius: 8px;
}

.upload-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  color: #7f8c8d;
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
}

/* 底部按钮样式 */
.action-footer {
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  text-align: center;
  padding: 1rem 0;
}

.action-footer button {
  background: linear-gradient(135deg, #3498db, #2ecc71);
  color: white;
  padding: 1rem 3rem;
  border-radius: 30px;
  font-size: 1.1rem;
  transition: transform 0.2s;
}

@media (max-width: 768px) {
  .main-layout {
    flex-direction: column;
    align-items: center;
    padding-bottom: 120px;
  }

  .divider {
    width: 100%;
    height: 1px;
    margin: 1rem 0;
    background: repeating-linear-gradient(
        90deg,
        transparent,
        transparent 10px,
        #bdc3c7 10px,
        #bdc3c7 20px
    );
  }

  .action-footer {
    position: fixed;
    bottom: 20px;
    background: rgba(255, 255, 255, 0.9);
    padding: 10px;
    box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  }
}
</style>