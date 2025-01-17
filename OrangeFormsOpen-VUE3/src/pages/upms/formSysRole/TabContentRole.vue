<template>
  <div class="tab-content-box" :style="'min-height:' + (mainContextHeight - 76) + 'px'">
    <el-form
      ref="form"
      :model="fragmentSysRole"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box :item-width="350" @search="refreshFragmentSysRole(true)" @reset="onResetRole">
        <el-form-item label="Role Name" prop="formFilter.sysRoleName" label-position="top">
          <el-input
            class="filter-item"
            v-model="fragmentSysRole.formFilter.sysRoleName"
            :clearable="true"
            placeholder="Role Name"
          />
        </el-form-item>
        <el-form-item label="User Type" prop="formFilter.sysUserType" label-position="top">
          <el-select
            class="filter-item"
            v-model="fragmentSysRole.formFilter.sysUserType"
            :clearable="true"
            placeholder="User Type"
          >
            <el-option
              v-for="item in userTypeList"
              :key="item.attr1"
              :label="item.attr1Name"
              :value="item.attr1"
            />
          </el-select>
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="fragmentSysRole.SysRole.impl.dataList"
      :hasExtend="false"
      @sort-change="fragmentSysRole.SysRole.impl.onSortChange"
      @refresh="refreshFragmentSysRole(true)"
      :seq-config="{
        startIndex:
          (fragmentSysRole.SysRole.impl.currentPage - 1) * fragmentSysRole.SysRole.impl.pageSize,
      }"
    >
      <template v-slot:operator>
        <el-button
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          :icon="Plus"
          :disabled="!checkPermCodeExist('formSysRole:fragmentSysRole:add')"
          @click="onAddSysRoleClick()"
        >
          New
        </el-button>
      </template>
      <vxe-column
        title="No."
        type="seq"
        width="50px"
        :index="fragmentSysRole.SysRole.impl.getTableIndex"
      />
      <vxe-column title="Role Name" field="roleName"> </vxe-column>
      <vxe-column title="User Type" field="userType"> </vxe-column>
      <vxe-column title="Operation" fixed="right" width="180px">
        <template v-slot="scope">
          <el-button
            @click="onEditSysRoleClick(scope.row)"
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysRole:fragmentSysRole:update')"
          >
            Edit
          </el-button>
          <el-button
            @click="onDeleteClick(scope.row)"
            link
            type="danger"
            :size="layoutStore.defaultFormItemSize"
            :disabled="!checkPermCodeExist('formSysRole:fragmentSysRole:delete')"
          >
            Delete
          </el-button>
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="fragmentSysRole.SysRole.impl.totalCount"
            :current-page="fragmentSysRole.SysRole.impl.currentPage"
            :page-size="fragmentSysRole.SysRole.impl.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="fragmentSysRole.SysRole.impl.onCurrentPageChange"
            @size-change="fragmentSysRole.SysRole.impl.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import { inject, reactive, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import axios from 'axios';
import { usePermissions } from '@/common/hooks/usePermission';
import { TableOptions } from '@/common/types/pagination';
import { useTable } from '@/common/hooks/useTable';
import { TableData } from '@/common/types/table';
import { SystemRoleController } from '@/api/system';
import { ANY_OBJECT } from '@/types/generic';
import { useDialog } from '@/components/Dialog/useDialog';
import { Role } from '@/types/upms/role';
import { useLayoutStore } from '@/store';
import { serverDefaultCfg } from '@/common/http/config';
import FormEditSysRole from '../formEditSysRole/index.vue';

const userTypeList = ref();
const Dialog = useDialog();
const layoutStore = useLayoutStore();
const mainContextHeight = inject('mainContextHeight', 200);
const { checkPermCodeExist } = usePermissions();

const form = ref();
/**
 * 用户角色数据获取函数，返回Primise
 */
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
const loadSysRoleData = (params: ANY_OBJECT): Promise<TableData<Role>> => {
  params.sysRoleDtoFilter = {
    userType: fragmentSysRole.formFilterCopy.sysUserType,
    roleName: fragmentSysRole.formFilterCopy.sysRoleName,
  };
  return new Promise((resolve, reject) => {
    SystemRoleController.getRoleList(params)
      .then(res => {
        resolve({
          dataList: res.data.dataList.map(item => {
            return { ...item };
          }),
          totalCount: res.data.totalCount,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * 用户角色数据获取检测函数，返回true正常获取数据，返回false停止获取数据
 */
const loadSysRoleVerify = () => {
  fragmentSysRole.formFilterCopy.sysUserType = fragmentSysRole.formFilter.sysUserType;
  fragmentSysRole.formFilterCopy.sysRoleName = fragmentSysRole.formFilter.sysRoleName;
  return true;
};

const tableOptions: TableOptions<Role> = {
  loadTableData: loadSysRoleData,
  verifyTableParameter: loadSysRoleVerify,
  paged: true,
  //orderFieldName: 'createTime',
  //ascending: true,
};

const fragmentSysRole = reactive({
  formFilter: {
    sysUserType: undefined,
    sysRoleName: undefined,
  },
  formFilterCopy: {
    sysUserType: undefined,
    sysRoleName: undefined,
  },
  SysRole: {
    impl: useTable(tableOptions),
  },
  isInit: false,
});

const onResetRole = () => {
  form.value.resetFields();
  refreshFragmentSysRole(true);
};

/**
 * 更新角色列表
 */
const refreshFragmentSysRole = (reloadData = false) => {
  // 重新获取数据组件的数据
  if (reloadData) {
    fragmentSysRole.SysRole.impl.refreshTable(true, 1);
  } else {
    fragmentSysRole.SysRole.impl.refreshTable();
  }
  fragmentSysRole.isInit = true;
};
/**
 * 新建
 */
const onAddSysRoleClick = () => {
  let params = {};

  Dialog.show(
    'Add Role',
    FormEditSysRole,
    {
      area: ['800px', 'auto'],
      offset: '100px',
    },
    params,
  )
    .then(() => {
      refreshFragmentSysRole();
    })
    .catch(e => {
      console.warn(e);
    });
};
const loadRowData = (row: Role): Promise<Role> => {
  return new Promise((resolve, reject) => {
    let params = {
      roleId: row.roleId,
    };
    SystemRoleController.getRole(params)
      .then(res => {
        if (typeof res.data.permsJsonData === 'string') {
          res.data.permsJsonData = JSON.parse(res.data.permsJsonData);
        }
        resolve(res.data);
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * 编辑
 */
const onEditSysRoleClick = (row: Role) => {
  loadRowData(row)
    .then(rowData => {
      return Dialog.show(
        'Edit Role',
        FormEditSysRole,
        {
          area: ['600px', 'auto'],
        },
        {
          rowData: rowData,
        },
      );
    })
    .then(() => {
      fragmentSysRole.SysRole.impl.refreshTable();
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * 删除
 */
const onDeleteClick = (row: Role) => {
  let params = {
    roleId: row.roleId,
  };

  ElMessageBox.confirm(`Delete the role【${row.roleName}】？`, '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      SystemRoleController.deleteRole(params)
        .then(() => {
          ElMessage.success('Delete Successful');
          fragmentSysRole.SysRole.impl.refreshTable();
        })
        .catch(e => {
          console.warn(e);
        });
    })
    .catch(() => {
      // do nothing
    });
};
onMounted(() => {
  getUserTypeList();
});

defineExpose({
  refreshFragmentSysRole,
});
// onMounted(() => {
//   refreshFragmentSysRole(true);
// });
</script>
