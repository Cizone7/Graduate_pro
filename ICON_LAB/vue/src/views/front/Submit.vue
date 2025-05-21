<template>
  <div class="detail-container">
    <div class="main-content">

      <!-- 上传区域 -->
      <div class="upload-wrapper" :class="{ 'collapsed': fileList.length }">
        <el-card>
          <el-upload
              ref="upload"
              class="upload-area"
              action="http://localhost:9090/web/upload"
              :multiple="true"
              :file-list="fileList"
              :before-upload="beforeUpload"
              :on-success="handleSuccess"
              drag
              :show-file-list="false"
          >
          <div class="upload-content">
            <i class="el-icon-upload"></i>
            <div class="upload-text">
              将图片拖到此处，或
              <el-button
                  type="primary"
                  size="small"
                  class="upload-btn"
              >
              点击上传
              </el-button>
            </div>
            <div class="upload-tips">支持JPG/PNG格式，大小不超过5MB</div>
          </div>
          </el-upload>
        </el-card>
      </div>

      <!-- 主内容区域 -->
      <div v-if="fileList.length" class="home-container">
        <el-card class="main-card">
          <div class="main-panels">
            <!-- 左侧预览面板 (3/5宽度) -->
            <div class="preview-panel">
              <el-card class="image-list-card">
                <div class="image-list-title">已上传作品</div>
                <div class="image-list">
                  <div
                      v-for="(file, index) in fileList"
                      :key="index"
                      class="image-item"
                      :class="{ active: selectedImage?.url === file.url }"
                      @click="selectImage(file)"
                  >
                    <img :src="file.url" class="image-preview" />
                    <el-button
                        type="danger"
                        icon="el-icon-delete"
                        circle
                        class="delete-btn"
                        @click.stop="handleRemove(file)"
                    ></el-button>
                  </div>
                </div>
              </el-card>
            </div>

            <!-- 右侧编辑面板 (2/5宽度) -->
            <div class="edit-panel">
              <el-card class="edit-card compact">
                <el-form
                    label-width="100px"
                    size="medium"
                    :model="selectedImage.form"
                    :rules="rules"
                    ref="ruleForm"
                >
                  <el-form-item label="作品标题" prop="name">
                    <el-input
                        v-model="selectedImage.form.name"
                        placeholder="请输入作品名称"
                        clearable
                    ></el-input>
                  </el-form-item>

                  <el-form-item label="作品积分" prop="count">
                    <el-input-number
                        v-model="selectedImage.form.count"
                        :min="0"
                        :max="100"
                        label="积分值"
                    ></el-input-number>
                  </el-form-item>

                  <el-form-item label="作品分类" prop="typeId">
                    <el-select
                        v-model="selectedImage.form.typeId"
                        placeholder="请选择分类"
                        clearable
                    >
                      <el-option
                          v-for="item in types"
                          :key="item.id"
                          :label="item.name"
                          :value="item.id"
                      ></el-option>
                    </el-select>
                  </el-form-item>

                  <el-form-item label="创作类型" prop="style">
                    <el-radio-group v-model="selectedImage.form.style">
                      <el-radio label="原创"></el-radio>
                      <el-radio label="临摹"></el-radio>
                      <el-radio label="AI辅助"></el-radio>
                    </el-radio-group>
                  </el-form-item>

                  <el-form-item>
                    <el-button
                        type="success"
                        size="medium"
                        @click="save"
                        style="width: 100%"
                    >
                      提交作品
                    </el-button>
                  </el-form-item>
                </el-form>
              </el-card>
            </div>
          </div>
        </el-card>

        <!-- 新增图库选择部分 -->
        <el-card class="library-select-card">
          <el-form label-width="100px">
            <el-form-item label="选择图库">
              <el-select v-model="selectedLibrary" placeholder="请选择图库" clearable>
                <el-option
                    v-for="library in libraries"
                    :key="library.id"
                    :label="library.name"
                    :value="library.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item v-if="createNewLibrary">
              <el-input v-model="newLibraryName" placeholder="请输入新图库名称" />
            </el-form-item>

            <el-form-item>
              <el-checkbox v-model="createNewLibrary">新建图库</el-checkbox>
            </el-form-item>
          </el-form>
        </el-card>

      </div>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      fileList: [],
      selectedImage: null,
      hoverIndex: -1,
      types: [
        { id: 1, name: "官方图标" },
        { id: 2, name: "多彩图标" },
        { id: 3, name: "单色图标" },
      ],
      rules: {
        name: [
          { required: true, message: "请输入作品名称", trigger: "blur" },
          { min: 1, max: 20, message: "长度在1到20个字符", trigger: "blur" },
        ],
        typeId: [
          { required: true, message: "请选择作品分类", trigger: "change" },
        ],
        style: [
          { required: true, message: "请选择创作类型", trigger: "change" },
        ],
        count: [
          { type: "number", required: true, message: "请输入积分值", trigger: "blur" },
        ],
      },
      // 选中的图库ID
      selectedLibrary: null,
      // 是否选择新建图库
      createNewLibrary: false,
      // 新图库名称
      newLibraryName: '',
      // 用户的所有图库
      libraries: [],
    };
  },
  mounted() {
    this.fetchLibraries();
  },
  methods: {
    // 新增triggerUpload方法
    triggerUpload() {
      this.$refs.upload.$el.querySelector('input[type="file"]').click();
    },

    async fetchLibraries() {
      // 从localStorage获取用户信息
      const userStr = localStorage.getItem("user");
      const user = userStr ? JSON.parse(userStr) : {};

      // 添加空值检查
      if (!user || !user.id) {
        this.$message.error("用户未登录或信息无效");
        return;
      }
      const currentUserId = user.id;

      this.request.get(`/library/user/${currentUserId}`).then(res => {
        if (res.code === '200') {
          this.libraries = res.data;
        }
      }).catch(error => {
        this.$message.error("获取图库失败：" + error.message);
      });
    },
    beforeUpload(file) {
      const isImage = file.type.startsWith("image/");
      const isLt5M = file.size / 1024 / 1024 < 5;

      if (!isImage) {
        this.$message.error("只能上传图片文件！");
        return false;
      }
      if (!isLt5M) {
        this.$message.error("图片大小不能超过5MB！");
        return false;
      }
      return true;
    },

    handleSuccess(response, file) {
      const serverUrl = response;
      const newFile = {
        url: serverUrl,
        name: file.name,
        form: {
          name: file.name.split(".")[0],
          count: 10,
          typeId: "",
          style: "",
        },
      };
      this.fileList.unshift(newFile);
      this.selectImage(newFile);
    },

    handleRemove(file) {
      this.fileList = this.fileList.filter((item) => item !== file);
      if (this.selectedImage === file) {
        this.selectedImage = null;
      }
    },

    selectImage(file) {
      this.selectedImage = file;
    },

    async save() {
      try{
        if (!this.fileList.length) {
          this.$message.warning("请先上传图片");
          return;
        }

        let allValid = true;
        this.fileList.forEach(file => {
          this.$refs.ruleForm.validate(valid => {
            if (!valid) allValid = false;
          });
        });

        if (!allValid) {
          this.$message.error("请完善所有图片的表单信息");
          return;
        }

        // 提交之前，检查图库选择
        let libraryId = this.selectedLibrary;
        if (this.createNewLibrary && this.newLibraryName) {
          const newLibrary = await this.createLibrary(this.newLibraryName);
          libraryId = newLibrary.id;
        }
        // 提交作品
        await this.submitImages(libraryId);

        // 重置表单
        this.newLibraryName = '';
        this.createNewLibrary = false;
      }catch (error) {
        this.$message.error(`提交失败: ${error.message}`);
        console.error("错误详情:", error);
      }

    },

// 创建新图库
    createLibrary(libraryName) {
      return new Promise((resolve, reject) => {
        this.request.post("/library", { name: libraryName })
          .then(res => {
            if (res.code === '200') {
              // 将新图库添加到列表首位
              this.libraries.unshift(res.data);
              // 自动选中新建的图库
              this.selectedLibrary = res.data.id;
              resolve(res.data);
            } else {
              reject(new Error(res.msg || "创建失败"));
            }
          })
          .catch(reject);
      });
    },

// 提交图片并关联图库ID
    submitImages(libraryId) {
      const submitDataList = this.fileList.map(file => ({
        ...file.form,
        img: file.url,
        libraryId: libraryId, // 关联图库ID
      }));

      this.request.post("/creation/batch", submitDataList).then(res => {
        if (res.code === '200') {
          this.$message.success(`成功提交 ${submitDataList.length} 张作品`);
          this.fileList = [];
          this.selectedImage = null;
        } else {
          this.$message.error(res.msg);
        }
      }).catch(error => {
        this.$message.error("提交失败：" + error.message);
      });
    },
  }
};
</script>

<style lang="scss" scoped>
.detail-container {
  padding: 20px;
  background: #f5f7fa;
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;

  .main-content {
    width: 100%;
    max-width: 1400px;
    margin: 0 auto;

    .title {
      font-size: 24px;
      font-weight: 600;
      color: #303133;
      text-align: center;
      margin-bottom: 30px;
      position: relative;
      &::after {
        content: "";
        display: block;
        width: 60px;
        height: 3px;
        background: #409eff;
        margin: 10px auto 0;
        border-radius: 2px;
      }
    }

    .upload-wrapper {
      width: 100%;
      max-width: 800px;
      margin: 20px auto;
      transition: all 0.3s;

      .el-card {
        border: 2px dashed #dcdfe6;
        background: rgba(255, 255, 255, 0.98);
        border-radius: 12px;
        overflow: hidden;

        ::v-deep .el-upload {
          width: 100%;
          height: 100%;
          display: block;
        }

        ::v-deep .el-upload-dragger {
          width: 100%;
          height: 100%;
          border: none;
          display: flex;
          align-items: center;
          justify-content: center;
          background: transparent;
        }
      }

      .upload-content {
        width: 100%;
        height: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        padding: 40px;

        .el-icon-upload {
          font-size: 64px;
          color: #c0c4cc;
          margin-bottom: 20px;
        }

        .upload-text {
          font-size: 16px;
          color: #606266;
          display: flex;
          flex-direction: column;
          align-items: center;
          gap: 10px;
          margin-top: 20px;
        }

        .upload-tips {
          font-size: 12px;
          color: #909399;
          position: absolute;
          bottom: 30px;
          left: 50%;
          transform: translateX(-50%);
        }

        .upload-btn {
          padding: 8px 24px;
          border-radius: 20px;
          transition: all 0.3s;
          &:hover {
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
          }
        }
      }

      &.collapsed {
        max-width: 600px;
        .el-card {
          height: 120px;
          border-color: #409eff;
          .upload-content {
            flex-direction: row;
            padding: 0 30px;
            .el-icon-upload {
              font-size: 36px;
              margin-right: 20px;
              color: #409eff;
            }
            .upload-text {
              flex-direction: row;
              font-size: 14px;
            }
            .upload-tips {
              display: none;
            }
          }
        }
      }
    }

    .main-panels {
      display: flex;
      gap: 30px;
      margin-top: 20px;
      height: calc(100vh - 300px);

      .preview-panel {
        flex: 3;
        .image-list-card {
          height: 100%;
          border-radius: 12px;
          overflow: hidden;
          .image-list {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(90px, 1fr));
            gap: 12px;
            padding: 20px;
            .image-item {
              width: 90px;
              height: 90px;
              border-radius: 6px;
              overflow: hidden;
              position: relative;
              .image-preview {
                width: 100%;
                height: 100%;
                object-fit: cover;
              }
            }
          }
        }
      }

      .image-item {
        position: relative;
        transition: all 0.3s;

        /* 新增悬浮效果 */
        &:hover {
          transform: translateY(-3px);
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);

          .delete-btn {
            opacity: 1;
            transform: scale(1);
          }
        }

        .delete-btn {
          position: absolute;
          top: 5px;
          right: 5px;
          padding: 6px !important;
          opacity: 0;
          transform: scale(0.8);
          transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
          z-index: 2;

          /* 按钮悬停增强效果 */
          &:hover {
            transform: scale(1.1);
            box-shadow: 0 2px 8px rgba(255, 72, 72, 0.4);
          }
        }
      }

      .edit-panel {
        flex: 2;
        min-width: 320px;
        .edit-card {
          height: 100%;
          padding: 20px;
          overflow-y: auto;
        }
      }
    }
  }
}

/* 滚动条样式 */
::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}
::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 4px;
}
::-webkit-scrollbar-track {
  background: #f1f1f1;
}

@media (max-width: 768px) {
  .main-panels {
    flex-direction: column;
    height: auto !important;
    .preview-panel,
    .edit-panel {
      width: 100% !important;
    }
  }
}
</style>