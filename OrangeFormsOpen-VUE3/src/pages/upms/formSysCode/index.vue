<template>
  <div>
    <el-form
      :model="formSysCode"
      ref="form"
      label-width="75px"
      label-position="right"
      :size="layoutStore.defaultFormItemSize"
      @submit.prevent
    >
      <filter-box
        :item-width="350"
        :hasFold="true"
        @search="refreshFormSysCode(true)"
        @reset="onReset"
      >
        <el-form-item label="Account Type" prop="formFilter.attr1" label-position="top">
          <el-input
            class="filter-item"
            v-model="formSysCode.formFilter.attr1"
            :clearable="true"
            placeholder="Account Type"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formSysCode.SysUser.impl.dataList"
      :hasExtend="false"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formSysCode.SysUser.impl.onSortChange"
      @refresh="refreshFormSysCode(true)"
      :seq-config="{
        startIndex: (formSysCode.SysUser.impl.currentPage - 1) * formSysCode.SysUser.impl.pageSize,
      }"
    >
      <template v-slot:operator>
        <el-button
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          :icon="Plus"
          :disabled="!checkPermCodeExist('formSysCode:fragmentSysCode:add')"
          @click="onAddRow()"
          >New</el-button
        >
      </template>
      <vxe-column title="No." type="seq" width="50px" />
      <vxe-column title="Country Code" field="countryCode" />
      <vxe-column title="Group Code" field="groupCode" />
      <vxe-column title="Group Name" field="groupName" />
      <vxe-column title="Code Number" field="codeNumber" />
      <vxe-column title="English" field="english" />
      <vxe-column title="Korean" field="korean" />
      <vxe-column title="Chinese" field="chinese" />
      <vxe-column title="Japanese" field="japanese" />
      <vxe-column title="Status" field="useStatus">
        <template v-slot="scope">
          <el-tag
            :type="getUserStatusType(scope.row.useStatus)"
            :size="layoutStore.defaultFormItemSize"
            >{{ SysCodeStatus.getValue(scope.row.useStatus) }}</el-tag
          >
        </template>
      </vxe-column>
      <vxe-column title="Sort" field="sort" />
      <vxe-column title="Code 1" field="attr1" />
      <vxe-column title="Code Name 1" field="attr1Name" />
      <vxe-column title="Operation" fixed="right" width="150px">
        <template v-slot="scope">
          <el-button
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
            @click="onEditRow(scope.row)"
            :disabled="
              isAdmin(scope.row) || !checkPermCodeExist('formSysCode:fragmentSysCode:update')
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
              isAdmin(scope.row) || !checkPermCodeExist('formSysCode:fragmentSysCode:delete')
            "
          >
            Delete
          </el-button>
        </template>
      </vxe-column>
      <!-- <template v-slot:pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formSysCode.SysUser.impl.totalCount"
            :current-page="formSysCode.SysUser.impl.currentPage"
            :page-size="formSysCode.SysUser.impl.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="pageNum => formSysCode.SysUser.impl.onCurrentPageChange(pageNum)"
            @size-change="pageSize => formSysCode.SysUser.impl.onPageSizeChange(pageSize)"
          >
          </el-pagination>
        </el-row>
      </template> -->
    </table-box>
  </div>
</template>

<script lang="ts">
export default {
  name: 'formSysCode',
};
</script>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import SystemUserController from '@/api/system/UserController';
import TableBox from '@/components/TableBox/index.vue';
import { treeDataTranslate } from '@/common/utils';
import { SysCodeStatus, SysUserType } from '@/common/staticDict/index';
import { Code, User, UserInfo } from '@/types/upms/user';
import { usePermissions } from '@/common/hooks/usePermission';
import { ANY_OBJECT } from '@/types/generic';
import { useDialog } from '@/components/Dialog/useDialog';
import { TableOptions } from '@/common/types/pagination';
import { useTable } from '@/common/hooks/useTable';
import { TableData } from '@/common/types/table';
import { useLayoutStore } from '@/store';
import EditCodeForm from '../formEditSysCode/index.vue';
import axios from 'axios';
import { serverDefaultCfg } from '@/common/http/config';
const layoutStore = useLayoutStore();

const Dialog = useDialog();
const form = ref();
const deptIdPath = ref<Array<string | number>>([]);

const onReset = () => {
  form.value.resetFields();
  deptIdPath.value = [];
  refreshFormSysCode(true);
};

const isAdmin = (row: UserInfo) => {
  return row.userType === SysUserType.ADMIN;
};

const loadSysUserData = (params: ANY_OBJECT): Promise<TableData<User>> => {
  return new Promise((resolve, reject) => {
    params = formSysCode.formFilter.attr1 ? { attr1: formSysCode.formFilter.attr1 } : {};
    axios
      .get(`${serverDefaultCfg.baseURL}sys/code/list`, { params })
      .then(res => {
        resolve({
          dataList: treeDataTranslate<User>(res.data, 'id'),
          totalCount: res.data.totalCount,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * User Code Verification Function, returns true to continue fetching data, false to stop fetching data
 */
const loadSysUserVerify = () => {
  return true;
};

const tableOptions: TableOptions<User> = {
  loadTableData: loadSysUserData,
  verifyTableParameter: loadSysUserVerify,
  // paged: true,
  // orderFieldName: 'createTime',
  ascending: true,
};

// Load Code Data
const formSysCode = reactive({
  formFilter: {
    attr1: '',
  },
  formFilterCopy: {
    attr1: '',
  },
  SysUser: {
    impl: useTable(tableOptions),
  },
  isInit: false,
});

const { checkPermCodeExist } = usePermissions();

const refreshFormSysCode = (reloadData = false) => {
  // Refresh the Data Component
  if (reloadData) {
    formSysCode.SysUser.impl.refreshTable(true, 1);
  } else {
    formSysCode.SysUser.impl.refreshTable();
  }
  formSysCode.isInit = true;
};

const getUserStatusType = (status: number) => {
  if (status === 1) {
    return 'success';
  } else {
    return 'danger';
  }
};

const onAddRow = () => {
  Dialog.show('Create Code', EditCodeForm, {
    area: '600px',
  })
    .then(() => {
      refreshFormSysCode();
    })
    .catch(e => {
      console.warn(e);
    });
};

const onEditRow = (row: Code) => {
  Dialog.show(
    'Edit Code',
    EditCodeForm,
    {
      area: '600px',
    },
    {
      rowData: row,
    },
  )
    .then(() => {
      refreshFormSysCode();
    })
    .catch(e => {
      console.warn(e);
    });
};

const onDeleteRow = (row: Code) => {
  let params = {
    id: row.id,
  };
  ElMessageBox.confirm(`Are you sure you want to delete code【${row.id}】?`, '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      SystemUserController.deleteCode(params)
        .then(() => {
          ElMessage.success('Delete Success!');
          refreshFormSysCode();
        })
        .catch(e => {
          console.log(e);
        });
    })
    .catch(e => {
      console.log(e);
    });
};

onMounted(() => {
  refreshFormSysCode();
});
</script>
