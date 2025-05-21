<template>
  <div class="register-container">
    <div class="register-bg-warp"></div>
    <div class="register-form-warp">
      <div class="register-form">
        <div class="welcome-title">Hi，注册一个账号开始使用吧</div>
        <el-form :model="user" :rules="rules" ref="userFormInst">
          <el-form-item prop="username">
            <el-input placeholder="请输入用户名" size="medium" prefix-icon="el-icon-user" v-model="user.username"></el-input>
          </el-form-item>
          <el-form-item prop="password">
            <el-input size="medium" prefix-icon="el-icon-lock" show-password placeholder="请输入密码" v-model="user.password"></el-input>
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input size="medium" prefix-icon="el-icon-lock" show-password placeholder="请再次输入密码" v-model="user.confirmPassword"></el-input>
          </el-form-item>
          <el-form-item>
            <el-checkbox v-model="isAllow" class="allow-warp">我已阅读同意{{ projectName }}《隐私政策》和《服务条款》</el-checkbox>
          </el-form-item>
          <el-form-item>
            <el-button class="btn-register" type="primary" size="medium" autocomplete="off" :disabled="!isAllow" @click="register">立即注册</el-button>
          </el-form-item>
          <el-form-item>
            <div class="other-warp">
              <div @click="$router.push('/login')">我有账号，去登录</div>
            </div>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script>
import { projectName } from '../../config/config.default'

export default {
  name: 'register',
  data() {
    return {
      projectName: projectName,
      user: {},
      isAllow: true,
      rules: {
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
          { min: 3, max: 10, message: '长度在 3 到 10 个字符', trigger: 'blur' }
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    register() {
      this.$refs['userFormInst'].validate((valid) => {
        if (valid) {
          // 表单校验合法
          if (this.user.password !== this.user.confirmPassword) {
            this.$message.error('两次输入的密码不一致')
            return false
          }
          this.request.post('/web/register', this.user).then((res) => {
            if (res.code === '200') {
              this.$message.success('注册成功')
            } else {
              this.$message.error(res.msg)
            }
          })
        }
      })
    }
  }
}
</script>

<style scoped lang="scss">
.register-container {
  width: 100%;
  height: 100vh;
  background-color: #f5f5f5;
  display: flex;
  .register-bg-warp {
    width: 40%;
    background: url('../assets/register.png') no-repeat;
    background-size: cover;
  }
  .register-form-warp {
    flex: 1;
    display: flex;
    justify-content: center;
    align-items: center;
    .register-form {
      width: 380px;
      .welcome-title {
        font-size: 24px;
        margin-bottom: 30px;
      }
      .verify-code-form-item {
        display: flex;
        align-items: center;
      }
      .allow-warp {
        color: #4084d9;
        display: flex;
        align-items: center;
        font-size: 12px;
      }
      .btn-register {
        width: 100%;
      }
      .other-warp {
        width: 100%;
        display: flex;
        justify-content: space-between;
        color: #4084d9;
        cursor: pointer;
      }
    }
  }
}
</style>
