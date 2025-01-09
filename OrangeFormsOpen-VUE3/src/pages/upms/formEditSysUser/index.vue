<template>
  <el-form
    ref="form"
    :model="formData"
    :rules="rules"
    label-width="150px"
    :size="formItemSize"
    label-position="right"
    @submit.prevent
  >
    <el-row :gutter="20" class="full-width-input">
      <el-col :span="24">
        <el-form-item label="Login Name" prop="loginName">
          <el-input
            v-model="formData.loginName"
            placeholder="User Login Name"
            clearable
            :disabled="isEdit"
            maxlength="30"
            autocomplete="new-loginName"
          />
        </el-form-item>
        <el-form-item label="Login Password" v-if="!isEdit" prop="password">
          <el-input
            v-model="formData.password"
            type="password"
            placeholder="User Login Password"
            clearable
            maxlength="64"
            autocomplete="new-password"
          />
        </el-form-item>
        <el-form-item label="Confirm Password" v-if="!isEdit" prop="passwordRepeat">
          <el-input
            v-model="formData.passwordRepeat"
            type="password"
            placeholder="Re-enter User Password"
            clearable
            maxlength="64"
          />
        </el-form-item>
        <el-form-item label="Show Name" prop="showName">
          <el-input
            v-model="formData.showName"
            placeholder="User Show Name"
            clearable
            maxlength="30"
          />
        </el-form-item>
        <!-- <el-form-item label="Account Type" prop="userType">
          <el-select v-model="formData.userType">
            <el-option
              v-for="item in SysUserType.getList()"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item> -->
        <el-form-item label="User Status" prop="userStatus" v-if="isEdit">
          <el-radio-group v-model="formData.userStatus">
            <el-radio v-for="item in SysUserStatus.getList()" :key="item.id" :value="item.id">{{
              item.name
            }}</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Department" prop="deptId">
          <el-cascader
            class="input-item"
            v-model="deptIdPath"
            :clearable="true"
            placeholder="Belong Department"
            :loading="deptId.impl.loading"
            :props="{ value: 'deptId', label: 'deptName', checkStrictly: true }"
            @visible-change="onDeptIdVisibleChange"
            :options="deptId.impl.dropdownList"
            @change="onDeptIdValueChange"
          >
          </el-cascader>
        </el-form-item>
        <el-form-item label="User Position" prop="deptPostIdList">
          <el-select v-model="formData.deptPostIdList" multiple placeholder="User Position">
            <el-option
              v-for="deptPost in deptPostList"
              :key="deptPost.deptPostId"
              :label="deptPost.postShowName"
              :value="deptPost.deptPostId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="User Type" prop="userRole">
          <el-select v-model="formData.userRole" placeholder="User Type">
            <el-option
              v-for="item in userTypeList"
              :key="item.attr1"
              :label="item.attr1Name"
              :value="item.attr1"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Role" prop="roleIdList">
          <el-select v-model="formData.roleIdList" placeholder="Role">
            <el-option
              v-for="role in roleList"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Data Permission" prop="dataPermIdList">
          <el-select v-model="formData.dataPermIdList" multiple placeholder="Data Permission">
            <el-option
              v-for="dataPerm in dataPermList"
              :key="dataPerm.dataPermId"
              :label="dataPerm.dataPermName"
              :value="dataPerm.dataPermId"
            />
          </el-select>
        </el-form-item>
      </el-col>
    </el-row>
    <!-- Popup Button Bar, must set class dialog-btn-layer -->
    <el-row type="flex" justify="end" class="dialog-btn-layer">
      <el-button :plain="true" @click="onCancel">Cancel</el-button>
      <el-button
        type="primary"
        @click="onSubmit"
        :disabled="
          !(
            checkPermCodeExist('formSysUser:fragmentSysUser:update') ||
            checkPermCodeExist('formSysUser:fragmentSysUser:add')
          )
        "
      >
        Confirm
      </el-button>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { Ref, computed, inject, onMounted, reactive, ref } from 'vue';
import { CascaderValue, ElCascader, ElMessage } from 'element-plus';
import { SysUserStatus, SysUserType } from '@/common/staticDict/index';
import { findTreeNodePath } from '@/common/utils';
import { DialogProp } from '@/components/Dialog/types';
import { usePermissions } from '@/common/hooks/usePermission';
import {
  DictionaryController,
  SysDeptController,
  SystemUserController,
  SysDataPermController,
  SystemRoleController,
} from '@/api/system';
import { ANY_OBJECT } from '@/types/generic';
import { User } from '@/types/upms/user';
import { useDropdown } from '@/common/hooks/useDropdown';
import { DropdownOptions, ListData } from '@/common/types/list';
import { useLayoutStore } from '@/store';
import axios from 'axios';
import { serverDefaultCfg } from '@/common/http/config';
const layoutStore = useLayoutStore();

const props = defineProps<{
  rowData?: User;
  // When using Dialog.show to pop up the component, this prop must be defined for callback.
  dialog?: DialogProp<ANY_OBJECT>;
}>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize;
});
const { checkPermCodeExist } = usePermissions();

const form = ref();
const formData: Ref<User> = ref({
  loginName: '',
  password: '',
  userRole: '',
  userType: 2,
  userStatus: 0,
  dataPermIdList: [],
  deptPostIdList: [],
  roleIdList: '',
});
const validatePasswordRepeat = (
  rule: ANY_OBJECT,
  value: string,
  callback: (error?: Error) => void,
) => {
  //console.log(rule, value, callback);
  if (!value) {
    callback(new Error('Re-enter Password Cannot Be Empty'));
  } else if (value != formData.value.password) {
    callback(new Error('The Two Password Inputs Are Inconsistent, Please Re-enter'));
  } else {
    callback();
  }
};
const rules = ref({
  loginName: [{ required: true, message: 'User Name Cannot Be Empty', trigger: 'blur' }],
  password: [{ required: true, message: 'User Password Cannot Be Empty', trigger: 'blur' }],
  passwordRepeat: [
    {
      required: true,
      validator: validatePasswordRepeat,
      trigger: 'blur',
    },
  ],
  showName: [{ required: true, message: 'User Show Name Cannot Be Empty', trigger: 'blur' }],
  dataPermIdList: [
    { required: true, message: 'Data Permission Cannot Be Empty', trigger: 'change' },
  ],
  deptId: [{ required: true, message: 'Department Id Cannot Be Empty', trigger: 'change' }],
  deptPostIdList: [{ required: true, message: 'User Position Cannot Be Empty', trigger: 'change' }],
  roleIdList: [{ required: true, message: 'Role Cannot Be Empty', trigger: 'change' }],
});
const deptIdPath = ref<CascaderValue | undefined>([]);
const dataPermList = ref<ANY_OBJECT>([]);
const deptPostList = ref<ANY_OBJECT[]>();
const userTypeList = ref();
const roleList = ref<ANY_OBJECT>([]);

const isEdit = computed(() => {
  return formData.value.userId != null;
});
/**
 * Belonging Department Dropdown Data Fetch Function
 */
const loadDeptmentDropdownList = (): Promise<ListData<ANY_OBJECT>> => {
  return new Promise((resolve, reject) => {
    let params = {};
    SysDeptController.list(params)
      .then(res => {
        resolve({
          dataList: res.data.dataList,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};

const dropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadDeptmentDropdownList,
  idKey: 'deptId',
  isTree: true,
};

const deptId = reactive({
  impl: useDropdown(dropdownOptions),
  value: [],
});
/**
 * Belonging Department Dropdown Visibility
 */
const onDeptIdVisibleChange = (show: boolean) => {
  deptId.impl.onVisibleChange(show).catch(e => {
    console.warn(e);
  });
};
/**
 * Belonging Department Selected Value Changed
 */
const onDeptIdValueChange = (value: CascaderValue) => {
  formData.value.deptId = Array.isArray(value) ? value[value.length - 1].toString() : undefined;
  formData.value.deptPostIdList = undefined;
  loadDeptPost();
};
/**
 * Get Department Position List
 */
const loadDeptPost = () => {
  // if (formData.value.deptId == null || formData.value.deptId === '') {
  //   deptPostList.value = [];
  //   return;
  // }
  DictionaryController.dictDeptPost({
    deptId: formData.value.deptId,
  })
    .then(res => {
      console.log('dictDeptPost', res);
      deptPostList.value = res;
    })
    .catch(e => {
      console.warn(e);
    });
};
const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  }
};
const getUserTypeList = () => {
  axios
    .get(`${serverDefaultCfg.baseURL}sys/code/list`, {
      params: {
        groupCode: 'UserType',
      },
    })
    .then(res => {
      userTypeList.value = res.data;
    })
    .catch(e => {
      console.log(e);
    });
};
const onSubmit = () => {
  form.value.validate((valid: boolean) => {
    if (valid) {
      let params = {
        sysUserDto: {
          userId: formData.value.userId,
          loginName: formData.value.loginName,
          password: formData.value.password,
          showName: formData.value.showName,
          userRole: formData.value.userRole,
          userType: formData.value.userType,
          deptId: formData.value.deptId,
          userStatus: formData.value.userStatus,
        },
        dataPermIdListString: Array.isArray(formData.value.dataPermIdList)
          ? formData.value.dataPermIdList.join(',')
          : undefined,
        deptPostIdListString: Array.isArray(formData.value.deptPostIdList)
          ? formData.value.deptPostIdList.join(',')
          : undefined,
        roleIdListString: Array.isArray(formData.value.roleIdList)
          ? formData.value.roleIdList.join(',')
          : formData.value.roleIdList,
      };

      let operation: Promise<ANY_OBJECT>;
      if (formData.value.userId != null) {
        operation = SystemUserController.updateUser(params);
      } else {
        operation = SystemUserController.addUser(params);
      }

      operation
        .then(res => {
          ElMessage.success(formData.value.userId != null ? 'Edit Successful' : 'Add Successful');
          props.dialog?.submit(res);
        })
        .catch(e => {
          console.warn(e);
        });
    }
  });
};
const loadRole = () => {
  SystemRoleController.getRoleList({})
    .then(res => {
      roleList.value = res.data.dataList;
    })
    .catch(e => {
      console.warn(e);
    });
};
const loadDataPerm = () => {
  SysDataPermController.list({})
    .then(res => {
      dataPermList.value = res.data.dataList;
    })
    .catch(e => {
      console.warn(e);
    });
};
onMounted(() => {
  if (props.rowData != null) {
    formData.value = {
      ...props.rowData,
      dataPermIdList: [],
      deptPostIdList: [],
      roleIdList: '',
    };
    if (Array.isArray(formData.value.sysDataPermUserList)) {
      formData.value.dataPermIdList = formData.value.sysDataPermUserList.map(
        item => item.dataPermId,
      );
    }
    if (Array.isArray(formData.value.sysUserPostList)) {
      formData.value.deptPostIdList = formData.value.sysUserPostList.map(item => item.deptPostId);
    }
    if (Array.isArray(formData.value.sysUserRoleList)) {
      formData.value.roleIdList = formData.value.sysUserRoleList.map(item => item.roleId)[0];
    }
  }
  deptId.impl.onVisibleChange(true).then(() => {
    deptIdPath.value = formData.value.deptId
      ? findTreeNodePath(deptId.impl.dropdownList, formData.value.deptId, 'deptId')
      : [];
  });
  loadRole();
  loadDataPerm();
  loadDeptPost();
  getUserTypeList();
});
</script>
