<template>
  <!-- Process Instance -->
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFilter"
      label-width="80px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box :item-width="350" @search="refreshFormAllInstance(true)" @reset="onReset">
        <el-form-item label="Process Name" prop="processDefinitionName">
          <el-input
            class="filter-item"
            v-model="formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
          />
        </el-form-item>
        <el-form-item label="Initiator" prop="startUser">
          <el-input
            class="filter-item"
            v-model="formFilter.startUser"
            :clearable="true"
            placeholder="Initiator"
          />
        </el-form-item>
        <el-form-item label="Create Date" prop="createDate">
          <date-range
            class="filter-item"
            v-model:value="formFilter.createDate"
            :clearable="true"
            :allowTypes="['day']"
            align="left"
            range-separator="-"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formAllInstanceWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formAllInstanceWidget.onSortChange"
      @refresh="refreshFormAllInstance(true)"
      :seq-config="{
        startIndex: (formAllInstanceWidget.currentPage - 1) * formAllInstanceWidget.pageSize,
      }"
      :hasExtend="false"
    >
      <vxe-column
        title="No."
        type="seq"
        width="50px"
        :index="formAllInstanceWidget.getTableIndex"
      />
      <vxe-column title="Process Name" field="processDefinitionName" />
      <vxe-column title="Process Id" field="processDefinitionKey" />
      <vxe-column title="Task Initiator" field="startUserId" />
      <vxe-column title="Task Start Time" field="startTime" />
      <vxe-column title="Task End Time" field="endTime" />
      <vxe-column title="Operation" width="220px">
        <template v-slot="scope">
          <el-button
            :size="layoutStore.defaultFormItemSize"
            type="primary"
            link
            @click="onShowProcessViewer(scope.row)"
            >Diagram</el-button
          >
          <el-button
            :size="layoutStore.defaultFormItemSize"
            type="danger"
            link
            :disabled="scope.row.endTime != null"
            @click="onStopTask(scope.row)"
          >
            Terminate
          </el-button>
          <el-button
            :size="layoutStore.defaultFormItemSize"
            type="danger"
            link
            :disabled="scope.row.endTime == null"
            @click="onDeleteTask(scope.row)"
          >
            Delete
          </el-button>
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formAllInstanceWidget.totalCount"
            :current-page="formAllInstanceWidget.currentPage"
            :page-size="formAllInstanceWidget.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="formAllInstanceWidget.onCurrentPageChange"
            @size-change="formAllInstanceWidget.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { Dialog } from '@/components/Dialog';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { FlowOperationController } from '@/api/flow';
import { FlowEntryType } from '@/common/staticDict/flow';
import { ANY_OBJECT } from '@/types/generic';
import { useLayoutStore } from '@/store';
import StopTask from './stopTask.vue';
import FormTaskProcessViewer from './formTaskProcessViewer.vue';

const layoutStore = useLayoutStore();

const form = ref();
const formFilter = reactive({
  processDefinitionName: undefined,
  startUser: undefined,
  createDate: [],
});
const formFilterCopy = reactive({
  processDefinitionName: undefined,
  startUser: undefined,
  createDate: [],
});

/**
 * Get all process instances
 */
const loadAllTaskData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    processDefinitionName: formFilterCopy.processDefinitionName,
    startUser: formFilterCopy.startUser,
    beginDate: formFilterCopy.createDate[0],
    endDate: formFilterCopy.createDate[1],
  };

  return new Promise((resolve, reject) => {
    FlowOperationController.listAllHistoricProcessInstance(params)
      .then(res => {
        res.data.dataList.forEach(item => {
          item.taskId =
            Array.isArray(item.runtimeTaskInfoList) && item.runtimeTaskInfoList.length > 0
              ? item.runtimeTaskInfoList[0].taskId
              : undefined;
        });
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
const loadAllTaskVerify = () => {
  formFilterCopy.processDefinitionName = formFilter.processDefinitionName;
  formFilterCopy.startUser = formFilter.startUser;
  formFilterCopy.createDate = Array.isArray(formFilter.createDate)
    ? [...formFilter.createDate]
    : [];
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadAllTaskData,
  verifyTableParameter: loadAllTaskVerify,
  paged: true,
  orderFieldName: 'showOrder',
  ascending: true,
};
const formAllInstanceWidget = reactive(useTable(tableOptions));

const refreshFormAllInstance = (reloadData = false) => {
  if (reloadData) {
    formAllInstanceWidget.refreshTable(true, 1);
  } else {
    formAllInstanceWidget.refreshTable();
  }
};
const onReset = () => {
  form.value.resetFields();
  refreshFormAllInstance(true);
};
const onShowProcessViewer = (row: ANY_OBJECT) => {
  Dialog.show(
    'Diagram',
    FormTaskProcessViewer,
    {
      area: ['1200px', '750px'],
    },
    {
      processDefinitionId: row.processDefinitionId,
      processInstanceId: row.processInstanceId,
    },
    {
      width: '1200px',
      height: '750px',
      pathName: '/thirdParty/thirdFormTaskProcessViewer',
    },
  ).catch(e => {
    console.warn(e);
  });
};
const onStopTask = (row: ANY_OBJECT) => {
  Dialog.show(
    'Terminate Task',
    StopTask,
    {
      area: '500px',
    },
    {
      processInstanceId: row.processInstanceId,
      taskId: row.taskId,
    },
    {
      width: '500px',
      height: '200px',
      pathName: '/thirdParty/thirdFormStopTaskInstance',
    },
  )
    .then(() => {
      formAllInstanceWidget.refreshTable();
    })
    .catch(e => {
      console.warn(e);
    });
};
const onDeleteTask = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to delete this process instance?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      return FlowOperationController.deleteProcessInstance({
        processInstanceId: row.processInstanceId,
      });
    })
    .then(() => {
      ElMessage.success('Deleted successfully');
      formAllInstanceWidget.refreshTable();
    })
    .catch(e => {
      console.warn(e);
    });
};
const formInit = () => {
  refreshFormAllInstance();
};
onMounted(() => {
  // Initialize page data
  formInit();
});

const onResume = () => {
  refreshFormAllInstance();
};
defineExpose({
  onResume,
});
</script>
