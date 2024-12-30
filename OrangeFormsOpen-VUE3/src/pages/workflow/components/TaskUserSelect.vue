<template>
  <div class="form-single-fragment" style="position: relative">
    <el-form
      label-width="100px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row type="flex">
        <el-form-item label="Login Name">
          <el-input
            class="filter-item"
            v-model="formSysUser.formFilter.sysUserLoginName"
            @change="refreshFormSysUser(true)"
            :clearable="true"
            placeholder="Login Name"
          />
        </el-form-item>
        <el-form-item label="Show Name">
          <el-input
            class="filter-item"
            v-model="formSysUser.formFilter.showName"
            @change="refreshFormSysUser(true)"
            :clearable="true"
            placeholder="Show Name"
          />
        </el-form-item>
        <el-input
          :disabled="dialogParams.multiple"
          :size="layoutStore.defaultFormItemSize"
          v-model="assignee"
          placeholder="Custom User"
          style="width: 200px; margin-bottom: 16px; margin-left: 20px; flex-grow: 1"
        />
      </el-row>
      <el-row type="flex" justify="end" style="margin-bottom: 15px">
        <el-button
          v-if="dialogParams.showStartUser"
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          @click="setStartUser"
        >
          Process Initiator
        </el-button>
        <el-button
          v-if="dialogParams.showAssignee"
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          @click="useAppointedAssignee"
        >
          Use Appointed Approver
        </el-button>
        <el-button
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          @click="onSubmit"
          :disabled="!canCommit"
        >
          Add User
        </el-button>
      </el-row>
    </el-form>
    <el-row>
      <el-col :span="24">
        <el-radio-group class="radio-table" v-model="selectUserId" style="width: 100%">
          <el-table
            :data="formSysUser.sysUserWidget.dataList"
            :size="layoutStore.defaultFormItemSize"
            height="410px"
            header-cell-class-name="table-header-gray"
            row-key="userId"
            @sort-change="formSysUser.sysUserWidget.onSortChange"
            @selection-change="handleSelectionChange"
          >
            <el-table-column
              v-if="dialogParams.multiple"
              header-align="center"
              :reserve-selection="dialogParams.multiple"
              align="center"
              type="selection"
              width="50px"
              :selectable="canSelect"
            />
            <el-table-column v-else label="No." header-align="center" align="center" width="50px">
              <template v-slot="scope">
                <el-radio :value="scope.row.userId"></el-radio>
              </template>
            </el-table-column>
            <el-table-column label="Login Name" prop="loginName" />
            <el-table-column label="Show Name" prop="showName" />
            <el-table-column label="Department" prop="deptIdDictMap.name" />
            <el-table-column label="Account Type" prop="userTypeDictMap.name" />
            <el-table-column label="Creation Time">
              <template v-slot="scope">
                <span>{{ formatDateByStatsType(scope.row.createTime, 'day') }}</span>
              </template>
            </el-table-column>
            <template v-slot:empty>
              <div class="table-empty unified-font">
                <img src="@/assets/img/empty.png" />
                <span>No Data</span>
              </div>
            </template>
          </el-table>
        </el-radio-group>
      </el-col>
      <el-col :span="24">
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formSysUser.sysUserWidget.totalCount"
            :current-page="formSysUser.sysUserWidget.currentPage"
            :page-size="formSysUser.sysUserWidget.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="formSysUser.sysUserWidget.onCurrentPageChange"
            @size-change="formSysUser.sysUserWidget.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { SysCommonBizController } from '@/api/system';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { DialogProp } from '@/components/Dialog/types';
import { findItemFromList } from '@/common/utils';
import { useDate } from '@/common/hooks/useDate';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';

interface IProps extends ThirdProps {
  // Show appointed approver
  showAssignee?: boolean;
  // Show process initiator
  showStartUser?: boolean;
  // Multi-select option
  multiple?: boolean;
  // List of already used users
  usedUserIdList?: Array<ANY_OBJECT>;
  // Additional filter conditions
  filterObject?: ANY_OBJECT;
  // When using Dialog.show to pop up the component, this prop must be defined for dialog callback
  dialog?: DialogProp<ANY_OBJECT | ANY_OBJECT[] | null | undefined>;
}
// TODO Default values of showAssignee and showStartUser as true may affect the correct display of third-party buttons
const props = withDefaults(defineProps<IProps>(), {
  showAssignee: true,
  showStartUser: true,
  multiple: false,
  usedUserIdList: () => [],
});
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);

import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const { formatDateByStatsType } = useDate();

const assignee = ref<string>();
// Selected user in single selection
const selectUserId = ref<string>();
// Selected user list in multi-selection
const multiSelectUser = ref<ANY_OBJECT[] | ANY_OBJECT>([]);

const dialogParams = computed(() => {
  console.log('dialogParams', props);
  return {
    showAssignee: props.showAssignee || thirdParams.value.showAssignee,
    showStartUser: props.showStartUser || thirdParams.value.showStartUser,
    multiple: props.multiple || thirdParams.value.multiple,
    usedUserIdList: props.usedUserIdList || thirdParams.value.usedUserIdList,
    filterObject: props.filterObject || thirdParams.value.filterObject,
  };
});
const canCommit = computed(() => {
  return dialogParams.value.multiple
    ? multiSelectUser.value.length > 0
    : (selectUserId.value != null && selectUserId.value !== '') || assignee.value != null;
});

/**
 * User management data retrieval function, returns Promise
 */
const loadSysUserData = (params: ANY_OBJECT) => {
  params.widgetType = 'upms_user';
  params.filter = {
    ...dialogParams.value.filterObject,
    loginName: formSysUser.formFilterCopy.sysUserLoginName,
    showName: formSysUser.formFilterCopy.showName,
  };

  return new Promise((resolve, reject) => {
    SysCommonBizController.list(params)
      .then(res => {
        resolve({
          dataList: res.data.dataList,
          totalCount: res.data.totalCount,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * User management data acquisition check function, returns true if data can be retrieved normally, returns false to stop data acquisition
 */
const loadSysUserVerify = () => {
  formSysUser.formFilterCopy.sysUserLoginName = formSysUser.formFilter.sysUserLoginName;
  formSysUser.formFilterCopy.showName = formSysUser.formFilter.showName;
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadSysUserData,
  verifyTableParameter: loadSysUserVerify,
  paged: true,
};
const formSysUser = reactive({
  formFilter: {
    sysUserLoginName: undefined,
    showName: undefined,
  },
  formFilterCopy: {
    sysUserLoginName: undefined,
    showName: undefined,
  },
  sysUserWidget: useTable(tableOptions),
  isInit: false,
});

const onSubmit = () => {
  let selectUser: ANY_OBJECT | ANY_OBJECT[] | null = multiSelectUser.value;
  if (!dialogParams.value.multiple) {
    if (assignee.value != null && assignee.value !== '') {
      const data = {
        loginName: assignee.value,
      };
      if (props.dialog) {
        props.dialog.submit(data);
      } else {
        onCloseThirdDialog(true, undefined, data);
      }
      return;
    }
    selectUser = findItemFromList(formSysUser.sysUserWidget.dataList, selectUserId.value, 'userId');
  }
  if (props.dialog) {
    props.dialog.submit(selectUser);
  } else {
    onCloseThirdDialog(true, undefined, selectUser);
  }
};
const onClear = () => {
  if (props.dialog) {
    props.dialog.submit(null);
  }
};
const setStartUser = () => {
  const data = {
    loginName: '${startUserName}',
  };
  if (props.dialog) {
    props.dialog.submit(data);
  } else {
    onCloseThirdDialog(true, undefined, data);
  }
};
const useAppointedAssignee = () => {
  const data = {
    loginName: '${appointedAssignee}',
  };
  if (props.dialog) {
    props.dialog.submit(data);
  } else {
    onCloseThirdDialog(true, undefined, data);
  }
};
const canSelect = (row: ANY_OBJECT) => {
  if (
    Array.isArray(dialogParams.value.usedUserIdList) &&
    dialogParams.value.usedUserIdList.length > 0
  ) {
    return dialogParams.value.usedUserIdList.indexOf(row.loginName) === -1;
  } else {
    return true;
  }
};
const handleSelectionChange = (values: ANY_OBJECT[] | ANY_OBJECT) => {
  multiSelectUser.value = values;
};
/**
 * Update user management
 */
const refreshFormSysUser = (reloadData = false) => {
  // Reload user management data
  if (reloadData) {
    formSysUser.sysUserWidget.refreshTable(true, 1);
  } else {
    formSysUser.sysUserWidget.refreshTable();
  }
  formSysUser.isInit = true;
};

onMounted(() => {
  refreshFormSysUser();
});

defineExpose({ onClear });
</script>

<style scoped>
.radio-table :deep(.el-radio__label) {
  display: none;
}
</style>
