<template>
  <!-- Process Instance -->
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFilter"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box
        hasFold="true"
        hasRefresh="true"
        hasDownload="true"
        :item-width="350"
        @search="refreshFormAllInstance(true)"
        @reset="onReset">
        <el-form-item label="Process Name" prop="processDefinitionName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
          />
        </el-form-item>
        <el-form-item label="Initiator" prop="startUser" label-position="top">
          <el-input
            class="filter-item"
            v-model="formFilter.startUser"
            :clearable="true"
            placeholder="Initiator"
          />
        </el-form-item>
        <el-form-item label="Create Date" prop="createDate" label-position="top">
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
      <vxe-column title="Process Name" min-width="100px" field="processDefinitionName" />
      <vxe-column title="Process Identifier" min-width="120px" field="processDefinitionKey" />
      <vxe-column title="Initiator" width="90px" field="startUserId" />
      <vxe-column title="Task Initiation Time" min-width="170px" field="startTime" />
      <vxe-column title="Task End Time" min-width="170px" field="endTime" />
      <vxe-column title="Operation" min-width="245px">
        <template v-slot="scope">
          <general-button
            text="Diagram"
            :style="{padding: '4px 10px'}"
            :size="layoutStore.defaultFormItemSize"
            @btnClick="onShowProcessViewer(scope.row)"
          />
          <general-button
            text="Stop"
            type="danger"
            :style="{padding: '4px 10px'}"
            :size="layoutStore.defaultFormItemSize"
            :disabled="scope.row.endTime != null"
            @btnClick="onStopTask(scope.row)"
          />
          <general-button
            text="Delete"
            type="danger"
            :style="{padding: '4px 10px'}"
            :size="layoutStore.defaultFormItemSize"
            :disabled="scope.row.endTime == null"
            @click="onDeleteTask(scope.row)"
          />
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <pagination
          :total="formAllInstanceWidget.totalCount"
          :currentPage="formAllInstanceWidget.currentPage"
          :pageSize="formAllInstanceWidget.pageSize"
          size="default"
          @currentChange="formAllInstanceWidget.onCurrentPageChange"
          @sizeChange="formAllInstanceWidget.onPageSizeChange"
        />
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
