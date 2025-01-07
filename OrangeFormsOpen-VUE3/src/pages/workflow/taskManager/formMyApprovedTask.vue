<template>
  <!-- Completed Tasks -->
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFilter"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box
        hasFold="true"
        hasRefresh="true"
        hasDownload="true"
        :item-width="350"
        @search="refreshFormMyApprovedTask(true)"
        @reset="onReset">
        <el-form-item label="Process Name" prop="processDefinitionName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
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
      :data="handlerTaskWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="handlerTaskWidget.onSortChange"
      @refresh="refreshFormMyApprovedTask(true)"
      :seq-config="{ startIndex: (handlerTaskWidget.currentPage - 1) * handlerTaskWidget.pageSize }"
      :hasExtend="false"
    >
      <vxe-column title="No." type="seq" width="50px" :index="handlerTaskWidget.getTableIndex" />
      <vxe-column title="Process Name" field="processDefinitionName" />
      <vxe-column title="Process Key" field="processDefinitionKey" />
      <vxe-column title="Task Name" field="name" />
      <vxe-column title="Execution Operation">
        <template v-slot="scope">
          <span class="vxe-cell--label">{{
            SysFlowTaskOperationType.getValue(scope.row.approvalType)
          }}</span>
        </template>
      </vxe-column>
      <vxe-column title="Initiator Login Name" field="startUser" />
      <vxe-column title="Initiator Show Name" field="showName" />
      <vxe-column title="Task Initiation Time" width="170px" field="createTime" />
      <vxe-column title="Operation" width="100px">
        <template v-slot="scope">
          <general-button
            text="Details"
            :size="layoutStore.defaultFormItemSize"
            @btnClick="onTaskDetail(scope.row)"
          />
        </template>
      </vxe-column>
      <template #pagination>
        <pagination
          :total="handlerTaskWidget.totalCount"
          :currentPage="handlerTaskWidget.currentPage"
          :pageSize="handlerTaskWidget.pageSize"
          size="default"
          @currentChange="handlerTaskWidget.onCurrentPageChange"
          @sizeChange="handlerTaskWidget.onPageSizeChange"
        />
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { FlowOperationController } from '@/api/flow';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { ANY_OBJECT } from '@/types/generic';
import { SysFlowTaskOperationType } from '@/common/staticDict/flow';
import { useRoute } from 'vue-router';
const route = useRoute();

const form = ref();
const router = useRouter();
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

/**
 * Get Completed Task List
 */
const loadHandlerTaskData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    processDefinitionName: formFilterCopy.processDefinitionName,
    beginDate: formFilterCopy.createDate[0],
    endDate: formFilterCopy.createDate[1],
  };
  return new Promise((resolve, reject) => {
    FlowOperationController.listHistoricTask(params)
      .then(res => {
        resolve({
          dataList: res.data.dataList.map(item => {
            let formInfo = JSON.parse(item.formKey);
            return {
              ...item,
              formInfo,
            };
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
 * Validate Completed Task Filter Parameters
 */
const loadHandlerTaskVerify = () => {
  formFilterCopy.processDefinitionName = formFilter.value.processDefinitionName;
  formFilterCopy.createDate = Array.isArray(formFilter.value.createDate)
    ? [...formFilter.value.createDate]
    : [];
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadHandlerTaskData,
  verifyTableParameter: loadHandlerTaskVerify,
  paged: true,
};
const formFilter = ref<ANY_OBJECT>({
  processDefinitionName: undefined,
  createDate: [],
});
const formFilterCopy: ANY_OBJECT = {
  processDefinitionName: undefined,
  createDate: [],
};
const handlerTaskWidget = reactive(useTable(tableOptions));

/**
 * Refresh Completed Task List
 */
const refreshFormMyApprovedTask = (reloadData = false) => {
  if (reloadData) {
    handlerTaskWidget.refreshTable(true, 1);
  } else {
    handlerTaskWidget.refreshTable();
  }
};
const onReset = () => {
  form.value.resetFields();
  refreshFormMyApprovedTask(true);
};
/**
 * Details
 */
const onTaskDetail = (row: ANY_OBJECT) => {
  let params = {
    taskId: row.id,
    processDefinitionId: row.processDefinitionId,
    processInstanceId: row.processInstanceId,
  };

  FlowOperationController.viewHistoricTaskInfo(params).then(res => {
    router.push({
      name: 'handlerFlowTask',
      query: {
        processDefinitionKey: row.processDefinitionKey,
        taskId: row.id,
        processInstanceId: row.processInstanceId,
        processDefinitionId: row.processDefinitionId,
        formId: (row.formInfo || {}).formId,
        routerName: row.routerName,
        readOnly: 'true',
        flowEntryName: row.processDefinitionName,
        processInstanceInitiator: row.showName,
        // Only show co-sign, revoke and sign-reduction operations in completed tasks
        operationList: JSON.stringify(
          (res.data.operationList || []).filter((item: ANY_OBJECT) => {
            return (
              item.type === SysFlowTaskOperationType.CO_SIGN ||
              item.type === SysFlowTaskOperationType.REVOKE ||
              item.type === SysFlowTaskOperationType.SIGN_REDUCTION
            );
          }),
        ),
      },
    });
  });
};
const onResume = () => {
  refreshFormMyApprovedTask();
};
const formInit = () => {
  refreshFormMyApprovedTask();
};

watch(
  () => route.name,
  () => {
    if (route.name === 'formMyApprovedTask') {
      formInit();
    }
  },
  { immediate: true },
);

defineExpose({ onResume });
</script>
