<template>
  <div>
    <el-form
      :model="formSysUser"
      ref="form"
      label-width="75px"
      label-position="right"
      :size="layoutStore.defaultFormItemSize"
      @submit.prevent
    >
      <filter-box
        :item-width="350"
        :hasFold="true"
        @search="refreshFormSysUser(true)"
        @reset="onReset"
      >
        <el-form-item label="User" prop="formFilter.sysUserLoginName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formSysUser.formFilter.sysUserLoginName"
            :clearable="true"
            placeholder="Enter Full Name/Username"
          />
        </el-form-item>
        <el-form-item label="Role" prop="formFilter.roleId" label-position="top">
          <el-select
            class="filter-item"
            v-model="formSysUser.formFilter.roleId"
            :clearable="true"
            placeholder="Role"
          >
            <el-option
              v-for="role in roleList"
              :key="role.roleId"
              :label="role.roleName"
              :value="role.roleId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Division" prop="formFilter.deptId" label-position="top">
          <el-cascader
            class="filter-item"
            v-model="deptIdPath"
            :clearable="true"
            placeholder="Select Division"
            :loading="formSysUser.deptId.impl.loading"
            :props="deptOptions"
            @visible-change="onDeptIdVisibleChange"
            :options="formSysUser.deptId.impl.dropdownList"
            @change="onDeptIdValueChange"
          >
          </el-cascader>
        </el-form-item>
        <!-- <el-form-item label="Business Model Type" prop="formFilter.userRole" label-position="top">
          <el-input
            class="filter-item"
            v-model="formSysUser.formFilter.userRole"
            :clearable="true"
            placeholder="Business Model Type"
          />
        </el-form-item> -->
        <!-- <el-form-item label="Show Name" prop="formFilter.showName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formSysUser.formFilter.showName"
            :clearable="true"
            placeholder="Show Name"
          />
        </el-form-item> -->
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formSysUser.SysUser.impl.dataList"
      :hasExtend="false"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formSysUser.SysUser.impl.onSortChange"
      @refresh="refreshFormSysUser(true)"
      :seq-config="{
        startIndex: (formSysUser.SysUser.impl.currentPage - 1) * formSysUser.SysUser.impl.pageSize,
      }"
    >
      <template v-slot:operator>
        <el-button
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          :icon="Plus"
          :disabled="!checkPermCodeExist('formSysUser:fragmentSysUser:add')"
          @click="onAddRow()"
          >Create</el-button
        >
      </template>
      <vxe-column title="No." type="seq" width="50px" />
      <vxe-column title="Username" field="loginName"> </vxe-column>
      <vxe-column title="Full Name" field="showName"> </vxe-column>
      <vxe-column title="Role" field="roleName"> </vxe-column>
      <vxe-column title="Business Model Type" field="userRole" />
      <vxe-column title="Division" field="deptIdDictMap.name" />
      <vxe-column title="Status">
        <template v-slot="scope">
          <el-tag
            :type="getUserStatusType(scope.row.userStatus)"
            :size="layoutStore.defaultFormItemSize"
            >{{ SysUserStatus.getValue(scope.row.userStatus) }}</el-tag
          >
        </template>
      </vxe-column>
      <vxe-column title="Create Time">
        <template v-slot="scope">
          <span class="vxe-cell--label">{{
            formatDateByStatsType(scope.row.createTime, 'day')
          }}</span>
        </template>
      </vxe-column>
      <vxe-column title="Operation" fixed="right" width="250px">
        <template v-slot="scope">
          <el-button
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
            @click="onEditRow(scope.row)"
            :disabled="
              isAdmin(scope.row) || !checkPermCodeExist('formSysUser:fragmentSysUser:update')
            "
          >
            Edit
          </el-button>
          <el-button
            link
            type="danger"
            :size="layoutStore.defaultFormItemSize"
            @click="onDeleteRow(scope.row)"
            :disabled="
              isAdmin(scope.row) || !checkPermCodeExist('formSysUser:fragmentSysUser:delete')
            "
          >
            Delete
          </el-button>
          <el-button
            link
            type="primary"
            :size="layoutStore.defaultFormItemSize"
            @click="onResetPassword(scope.row)"
            :disabled="!checkPermCodeExist('formSysUser:fragmentSysUser:resetPassword')"
          >
            Reset Password
          </el-button>
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formSysUser.SysUser.impl.totalCount"
            :current-page="formSysUser.SysUser.impl.currentPage"
            :page-size="formSysUser.SysUser.impl.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="pageNum => formSysUser.SysUser.impl.onCurrentPageChange(pageNum)"
            @size-change="pageSize => formSysUser.SysUser.impl.onPageSizeChange(pageSize)"
          >
          </el-pagination>
        </el-row>
      </template>
    </table-box>
  </div>
</template>

<script lang="ts">
export default {
  name: 'formSysUser',
};
</script>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue';
import {
  CascaderNodePathValue,
  CascaderNodeValue,
  CascaderValue,
  ElMessage,
  ElMessageBox,
} from 'element-plus';
import SystemUserController from '@/api/system/UserController';
import TableBox from '@/components/TableBox/index.vue';
import { treeDataTranslate } from '@/common/utils';
import { SysUserStatus, SysUserType } from '@/common/staticDict/index';
import { User, UserInfo } from '@/types/upms/user';
import { usePermissions } from '@/common/hooks/usePermission';
import { ANY_OBJECT } from '@/types/generic';
import { DictionaryController, SystemRoleController } from '@/api/system';
import { useDialog } from '@/components/Dialog/useDialog';
import { TableOptions } from '@/common/types/pagination';
import { useTable } from '@/common/hooks/useTable';
import { TableData } from '@/common/types/table';
import { useDropdown } from '@/common/hooks/useDropdown';
import { DropdownOptions, ListData } from '@/common/types/list';
import { useDate } from '@/common/hooks/useDate';
import { useLayoutStore } from '@/store';
import EditUserForm from '../formEditSysUser/index.vue';
const layoutStore = useLayoutStore();
const roleList = ref<ANY_OBJECT>([]);

const Dialog = useDialog();
const mainContextHeight = inject('mainContextHeight', 200);
const form = ref();
const deptIdPath = ref<Array<string | number>>([]);

const deptOptions = { value: 'id', label: 'name', checkStrictly: true };

const onReset = () => {
  form.value.resetFields();
  deptIdPath.value = [];
  refreshFormSysUser(true);
};

const isAdmin = (row: UserInfo) => {
  return row.userType === SysUserType.ADMIN;
};

const loadSysUserData = (params: ANY_OBJECT): Promise<TableData<User>> => {
  params.sysUserDtoFilter = {
    deptId: formSysUser.formFilterCopy.deptId,
    roleId: formSysUser.formFilterCopy.roleId,
    loginName: formSysUser.formFilterCopy.sysUserLoginName,
    showName: formSysUser.formFilterCopy.showName,
    userRole: formSysUser.formFilterCopy.userRole,
    userStatus: formSysUser.formFilterCopy.sysUserStatus,
  };
  return new Promise((resolve, reject) => {
    SystemUserController.getUserList(params)
      .then(res => {
        resolve({
          dataList: treeDataTranslate<User>(res.data.dataList, 'userId'),
          totalCount: res.data.totalCount,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * User data acquisition verification function, returns true to normally acquire data, returns false to stop acquiring data
 */
const loadSysUserVerify = () => {
  formSysUser.formFilterCopy.deptId = formSysUser.formFilter.deptId;
  formSysUser.formFilterCopy.roleId = formSysUser.formFilter.roleId;
  formSysUser.formFilterCopy.sysUserLoginName = formSysUser.formFilter.sysUserLoginName;
  formSysUser.formFilterCopy.showName = formSysUser.formFilter.showName;
  formSysUser.formFilterCopy.userRole = formSysUser.formFilter.userRole;
  formSysUser.formFilterCopy.sysUserStatus = formSysUser.formFilter.sysUserStatus;
  return true;
};

const tableOptions: TableOptions<User> = {
  loadTableData: loadSysUserData,
  verifyTableParameter: loadSysUserVerify,
  paged: true,
  orderFieldName: 'createTime',
  ascending: true,
};

const loadDeptIdDropdownList = (): Promise<ListData<ANY_OBJECT>> => {
  return new Promise((resolve, reject) => {
    let params = {};
    DictionaryController.dictSysDept(params)
      .then(res => {
        resolve({
          dataList: treeDataTranslate(res.getList(), 'id'),
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
const dropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadDeptIdDropdownList,
  idKey: 'deptId',
};

// Load user data
const formSysUser = reactive({
  formFilter: {
    roleId: undefined,
    userRole: undefined,
    deptId: undefined as CascaderNodeValue | CascaderNodePathValue | undefined,
    sysUserStatus: undefined,
    sysUserLoginName: undefined,
    showName: undefined,
  },
  formFilterCopy: {
    roleId: undefined,
    userRole: undefined,
    deptId: undefined as CascaderNodeValue | CascaderNodePathValue | undefined,
    sysUserStatus: undefined,
    sysUserLoginName: undefined,
    showName: undefined,
  },
  deptId: {
    impl: useDropdown(dropdownOptions),
  },
  SysUser: {
    impl: useTable(tableOptions),
  },
  isInit: false,
});

const { checkPermCodeExist } = usePermissions();
const { formatDateByStatsType } = useDate();

const refreshFormSysUser = (reloadData = false) => {
  // Retrieve data component data again
  if (reloadData) {
    formSysUser.SysUser.impl.refreshTable(true, 1);
  } else {
    formSysUser.SysUser.impl.refreshTable();
  }
  formSysUser.isInit = true;
};

const getUserStatusType = (status: number) => {
  if (status === SysUserStatus.NORMAL) {
    return 'success';
  } else if (status === SysUserStatus.LOCKED) {
    return 'danger';
  } else {
    return 'info';
  }
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
const onAddRow = () => {
  Dialog.show('Create User', EditUserForm, {
    area: '600px',
  })
    .then(() => {
      refreshFormSysUser();
    })
    .catch(e => {
      console.warn(e);
    });
};

const onEditRow = (row: User) => {
  var params = {
    userId: row.userId,
  };

  SystemUserController.getUser(params)
    .then(res => {
      Dialog.show(
        'Edit User',
        EditUserForm,
        {
          area: '600px',
        },
        {
          rowData: res.data,
        },
      )
        .then(() => {
          refreshFormSysUser();
        })
        .catch(e => {
          console.warn(e);
        });
    })
    .catch(e => {
      console.warn(e);
    });
};

const onDeleteRow = (row: User) => {
  let params = {
    userId: row.userId,
  };
  ElMessageBox.confirm(`Are you sure to delete user 【${row.showName || row.loginName}】?`, '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      SystemUserController.deleteUser(params)
        .then(() => {
          ElMessage.success('Delete Success!');
          refreshFormSysUser();
        })
        .catch(e => {
          console.log(e);
        });
    })
    .catch(e => {
      console.log(e);
    });
};

const onResetPassword = (row: User) => {
  ElMessageBox.confirm('Are you sure to reset user password?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      return SystemUserController.resetUserPassword({ userId: row.userId });
    })
    .then(() => {
      ElMessage.success('Password reset successful');
    })
    .catch(e => {
      console.warn(e);
    });
};

const onDeptIdVisibleChange = (show: boolean) => {
  formSysUser.deptId.impl.onVisibleChange(show).catch(e => {
    console.warn(e);
  });
};

const onDeptIdValueChange = (value: CascaderValue) => {
  formSysUser.formFilter.deptId = Array.isArray(value) ? value[value.length - 1] : undefined;
};

onMounted(() => {
  loadRole();
  refreshFormSysUser();
});
</script>
