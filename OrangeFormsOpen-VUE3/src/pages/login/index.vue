<template>
  <div class="login-form">
    <div class="logo-box logo-right">
      <div class="el-space" style="align-items: center; gap: 8px"></div>
    </div>
    <div class="login-box">
      <div class="img-box"></div>
      <div class="login-input">
        <!-- <img src="@/assets/img/orange.png" style="width: 62px; margin-bottom: 9px" alt="" /> -->
        <span class="title">Welcome to Log in</span>
        <span class="desc">Configurable Order Workflow Demo</span>
        <el-form
          :model="dataForm"
          :rules="dataRule"
          label-position="top"
          ref="loginForm"
          @keyup.enter.stop="() => {}"
        >
          <el-col :span="24">
            <el-form-item prop="mobilePhone" label="" style="margin-top: 48px; margin-bottom: 25px">
              <el-input
                v-model="dataForm.mobilePhone"
                style="width: 100%; height: 50px"
                placeholder="Please enter account number"
              />
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item prop="password" label="">
              <el-input
                v-model="dataForm.password"
                style="width: 100%; height: 50px"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Please enter password"
                @keyup.enter="dataFormSubmit()"
              >
                <template v-slot:suffix>
                  <i
                    style="
                      display: flex;
                      justify-content: center;
                      align-items: center;
                      cursor: pointer;
                      height: 100%;
                      margin-right: 10px;
                    "
                    ><img
                      :src="!showPassword ? eyeClose : eyeOpen"
                      style="width: 24px"
                      @click="showPassword = !showPassword"
                  /></i>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-button
            class="login-btn-submit"
            type="primary"
            size="large"
            @click="dataFormSubmit()"
            style="width: 100%; height: 50px; margin-top: 32px; font-size: 16px"
          >
            Login
          </el-button>
        </el-form>
      </div>
    </div>
    <div
      style="
        position: absolute;
        right: 0;
        bottom: 0;
        left: 0;
        height: 44px;
        font-size: 14px;
        color: #666;
        background-color: #f7fafe;
      "
    >
      <p style="padding: 0; margin: 0; text-align: center; line-height: 44px"></p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { FormInstance } from 'element-plus';
import { Router, useRouter } from 'vue-router';
import { encrypt, setToken, treeDataTranslate } from '@/common/utils/index';
import { loginParam } from '@/types/upms/login';
import { useLoginStore, useLayoutStore } from '@/store';
import LoginController from '@/api/system/LoginController';
import eyeOpen from '@/assets/img/eye_open.png';
import eyeClose from '@/assets/img/eye_close.png';
import { UserInfo } from '@/types/upms/user';

const bkImg = 'https://orangeforms-website.oss-cn-beijing.aliyuncs.com/login_icon2.png';

onMounted(() => {
  layoutStore.setMenuList([]);
  loginStore.setUserInfo({} as UserInfo);
  setToken(null);
});

const loginForm = ref<FormInstance>();

const loginStore = useLoginStore();
const layoutStore = useLayoutStore();
const router: Router = useRouter();

const showPassword = ref(false);

const dataForm = reactive({
  mobilePhone: 'admin',
  password: '123456',
});

const dataRule = {
  mobilePhone: [{ required: true, message: 'Account cannot be empty', trigger: 'blur' }],
  password: [{ required: true, message: 'Password cannot be empty', trigger: 'blur' }],
};

const dataFormSubmit = () => {
  loginForm.value?.validate((valid: boolean) => {
    if (valid) {
      login();
    }
  });
};

const modifyMenuList = menuList => {
  const rootMenuIndex = menuList.findIndex(item => item.menuName === 'Root Menu');
  if (rootMenuIndex === -1) {
    console.log("Don't have Root Menu");
    return menuList;
  }
  const rootMenuId = menuList[rootMenuIndex].menuId;
  menuList.splice(rootMenuIndex, 1);
  menuList.forEach(item => {
    if (item.parentId === rootMenuId) {
      delete item.parentId;
    }
  });

  return menuList;
};

const login = function (verifyParams: { captchaVerification: string } | null = null) {
  let params: loginParam = {
    loginName: dataForm.mobilePhone,
    password: encrypt(dataForm.password),
    captchaVerification: verifyParams?.captchaVerification,
  };
  setToken(null);
  LoginController.login(params)
    .then(data => {
      //console.log('login >>>', data);
      if (data.data.menuList) {
        const newMenuList = modifyMenuList(data.data.menuList);
        layoutStore.setMenuList(treeDataTranslate(newMenuList, 'menuId', 'parentId'));
        delete data.data.menuList;
      }
      setToken(data.data.tokenData);
      layoutStore.setCurrentMenu(null);
      layoutStore.clearAllTags();
      layoutStore.setLoginName(dataForm.mobilePhone);
      if (data.data.tokenData) {
        delete data.data.tokenData;
      }
      loginStore.setUserInfo(data.data);
      // 登录成功跳转页面
      router.replace({ name: 'main' });
    })
    .catch(e => {
      console.error(e);
    });
};
</script>

<style lang="scss">
.login-form {
  position: relative;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: hidden;
  width: 100vw;
  height: 100vh;
  background: url('@/assets/img/login_bg2.png') center center;
  background-size: cover;
  .logo-box {
    position: absolute;
    top: 38px;
    right: 48px;
    left: 48px;
    display: flex;
    justify-content: space-between;
    align-items: center;
    font-size: 28px;
    color: #333 !important;
    font-weight: bold;
    img {
      margin-right: 11px;
    }
  }
  .login-box {
    display: flex;
    align-items: center;
    height: 499px;
    border-radius: 3px;
    .login-input {
      display: flex;
      width: 464px;
      padding: 54px 39px;
      background-color: #fff;
      border-radius: 12px;
      box-shadow: 0 2px 20px 1px rgb(79 79 79 / 10%);
      flex-direction: column;
      .title {
        padding-bottom: 12px;
        font-size: 40px;
        text-align: center;
        color: #333 !important;
        font-weight: bold;
      }
      .desc {
        font-size: 18px;
        text-align: center;
        color: #666 !important;
      }
    }
    .img-box {
      position: relative;
      height: 100%;
      margin-right: 109px;
      .img-title {
        position: absolute;
        top: -80px;
        right: 0;
        left: 0;
        display: none;
        width: 375px;
        margin: 0 auto;
      }
    }
  }
  .el-form-item__label {
    color: #333 !important;
  }
}

.login-form .el-input__inner {
  height: 50px !important;
}

.logo-right {
  a,
  span {
    font-size: 16px;
    font-weight: normal;
    color: #666 !important;
  }
}

.el-space {
  display: inline-flex;
  vertical-align: top;
}
.el-text {
  padding: 0;
  margin: 0;
  font-size: var(--el-text-font-size);
  color: var(--el-text-color);
  align-self: center;
  overflow-wrap: break-word;
}
.mx-1 {
  margin-right: 0.25rem;
  margin-left: 0.25rem;
}

.login-form input {
  font-size: 14px !important;
}
</style>
