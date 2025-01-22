<template>
  <el-form
    ref="form"
    :model="formData"
    :rules="rules"
    :size="formItemSize"
    label-position="left"
    label-width="160px"
    @submit.prevent
  >
    <el-row :gutter="20">
      <el-col :span="24">
        <el-form-item label="Role Name" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="Role Name" clearable maxlength="30" />
        </el-form-item>
        <el-form-item label="Business Model Type" prop="userType">
          <el-select v-model="formData.userType" placeholder="Business Model Type">
            <el-option
              v-for="item in userTypeList"
              :key="item.attr1"
              :label="item.attr1Name"
              :value="item.attr1"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="false" label="Role Type" :required="true">
          <el-radio-group v-model="formData.adminRole">
            <el-radio :value="1">Admin</el-radio>
            <el-radio :value="0">Other</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="Work Flow">
          <el-select v-model="formData.workFlow" multiple placeholder="Select Process" clearable>
            <el-option
              v-for="item in processList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-card shadow="never">
          <template v-slot:header>
            <div class="card-header">
              <span>Menu Permissions</span>
              <el-input
                :size="formItemSize"
                v-model="menuNameFilter"
                placeholder="Please input menu name"
                style="width: 250px"
                clearable
                :suffix-icon="Search"
              />
            </div>
          </template>
          <el-scrollbar style="height: 230px" wrap-class="scrollbar_dropdown__wrap">
            <el-tree
              ref="authTree"
              :data="authData"
              :props="treeProps"
              :check-strictly="false"
              show-checkbox
              node-key="menuId"
              :filter-node-method="
                (value, data) => {
                  return filterMenuNode(value, data as MenuItem);
                }
              "
            >
            </el-tree>
          </el-scrollbar>
        </el-card>
      </el-col>
    </el-row>
    <!-- 弹窗下发按钮栏，必须设置class为dialog-btn-layer -->
    <el-row type="flex" justify="end" class="dialog-btn-layer" style="margin-top: 20px">
      <el-button :plain="true" @click="onCancel">Cancel</el-button>
      <el-button
        type="primary"
        @click="onSubmit"
        :disabled="
          !(
            checkPermCodeExist('formSysRole:fragmentSysRole:update') ||
            checkPermCodeExist('formSysRole:fragmentSysRole:add')
          )
        "
      >
        Submit
      </el-button>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue';
import { ElTree, ElMessage } from 'element-plus';
import axios from 'axios';
import { DialogProp } from '@/components/Dialog/types';
import { usePermissions } from '@/common/hooks/usePermission';
import { ANY_OBJECT } from '@/types/generic';
import { SystemRoleController, SystemMenuController } from '@/api/system';
import { findTreeNode, treeDataTranslate } from '@/common/utils';
import { MenuItem } from '@/types/upms/menu';
import { Role } from '@/types/upms/role';
import { useLayoutStore } from '@/store';
import { serverDefaultCfg } from '@/common/http/config';
import { FlowEntryController } from '@/api/flow';

const layoutStore = useLayoutStore();
const processList = ref<ANY_OBJECT[]>([]);
const props = defineProps<{
  rowData?: Role;
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT>;
}>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize;
});
const { checkPermCodeExist } = usePermissions();
const treeProps = {
  label: 'menuName',
};
const form = ref();
const authTree = ref<InstanceType<typeof ElTree>>();
const formData = ref({
  roleId: '',
  roleName: '',
  workFlow: [] as string[],
  userType: '',
  adminRole: false,
  menuIdListString: '',
  menuIdList: [] as string[],
  sysRoleMenuList: [] as MenuItem[],
});
const rules = {
  userType: [{ required: true, message: 'User type cannot be empty', trigger: 'blur' }],
  roleName: [{ required: true, message: 'Role name cannot be empty', trigger: 'blur' }],
};
const menuNameFilter = ref();
const userTypeList = ref();
const authData: Ref<MenuItem[]> = ref([]);
let allowParentList: string[] = [];
const isEdit = computed(() => {
  return !!props.rowData?.roleId;
});

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  }
};
// eslint-disable-next-line @typescript-eslint/no-unused-vars
const filterMenuNode = (value: string, data: MenuItem) => {
  if (!value) return true;
  if (data.menuName.indexOf(value) !== -1) {
    allowParentList.push(data.menuId);
    return true;
  } else if (data.parentId) {
    return allowParentList.indexOf(data.parentId) !== -1;
  }
  return false;
};
const onSubmit = () => {
  form.value.validate((valid: boolean) => {
    if (valid) {
      let selectMenu = authTree.value?.getHalfCheckedKeys() || [];
      selectMenu = selectMenu.concat(authTree.value?.getCheckedKeys() || []);

      if (selectMenu.length <= 0) {
        ElMessage.error({
          message: 'Please select the menu permissions for the role',
          showClose: true,
        });
        return;
      }
      let params = {
        sysRoleDto: { ...formData.value, workFlow: String(formData.value.workFlow) },
        menuIdListString: '',
      };
      params.menuIdListString = selectMenu.join(',');
      if (isEdit.value) {
        SystemRoleController.updateRole(params)
          .then(res => {
            ElMessage.success('Edit Successful');
            if (props.dialog) {
              props.dialog.submit(res);
            }
          })
          .catch(e => {
            console.warn(e);
          });
      } else {
        SystemRoleController.addRole(params)
          .then(res => {
            ElMessage.success('Add Successful');
            if (props.dialog) {
              props.dialog.submit(res);
            }
          })
          .catch(e => {
            console.warn(e);
          });
      }
    }
  });
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
const loadAuthData = () => {
  SystemMenuController.getMenuPermList({})
    .then(res => {
      console.log(res);
      authData.value = treeDataTranslate(res.data, 'menuId', 'parentId');
      if (Array.isArray(formData.value.menuIdList)) {
        let tempList: string[] = [];
        formData.value.menuIdList.forEach(menuId => {
          let tempMenu = findTreeNode(authData.value, menuId, 'menuId');
          // 判断是否为叶子节点
          if (tempMenu != null && (!tempMenu.children || tempMenu.children.length <= 0)) {
            tempList.push(menuId);
          }
        });
        console.log(tempList);
        authTree.value?.setCheckedKeys(tempList, true);
      }
    })
    .catch(e => {
      console.warn(e);
    });
};

watch(menuNameFilter, val => {
  allowParentList = [];
  authTree.value?.filter(val);
});

const getProcessList = () => {
  const params = {
    orderParam: [
      {
        fieldName: 'entryId',
        asc: true,
      },
    ],
    flowEntryDtoFilter: {
      flowType: 1,
      status: 1,
    },
  };
  FlowEntryController.listNoPage(params).then((res: ANY_OBJECT) => {
    processList.value = res.data.map(item => {
      return { label: item.processDefinitionName, value: item.processDefinitionKey };
    });
  });
};

onMounted(() => {
  if (props.rowData) {
    // if (props.rowData.workFlow) {
    //   props.rowData.workFlow = (props.rowData.workFlow as unknown as string).split(',');
    // }
    formData.value = { ...formData.value, ...props.rowData };
    if (formData.value.workFlow) {
      formData.value.workFlow = (formData.value.workFlow as unknown as string).split(',');
    }
    if (formData.value.sysRoleMenuList) {
      formData.value.menuIdList = formData.value.sysRoleMenuList.map(item => item.menuId);
    }
  }
  getUserTypeList();
  loadAuthData();
  getProcessList();
});
</script>
