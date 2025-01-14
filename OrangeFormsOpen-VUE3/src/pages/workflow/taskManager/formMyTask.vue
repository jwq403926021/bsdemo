<template>
  <!-- To-Do Tasks -->
  <div style="position: relative" class="my-task">
    <el-form
      ref="form"
      :model="formMyTask"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row class="form-title">
        <el-col :span="24">
          My Task
        </el-col>
      </el-row>
      <filter-box
        :item-width="320"
        :hasFold="true"
        :hasRefresh="true"
        :hasDownload="true"
        @search="refreshMyTask(true)"
        @reset="onReset">
        <el-form-item label="Process Name" prop="formFilter.processDefinitionName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
          />
        </el-form-item>
        <el-form-item label="Process Key" prop="formFilter.processDefinitionKey" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.processDefinitionKey"
            :clearable="true"
            placeholder="Process Key"
          />
        </el-form-item>
        <el-form-item label="Task Name" prop="formFilter.taskName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.taskName"
            :clearable="true"
            placeholder="Task Name"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formMyTask.taskWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formMyTask.taskWidget.onSortChange"
      @refresh="refreshMyTask(true)"
      :seq-config="{
        startIndex: (formMyTask.taskWidget.currentPage - 1) * formMyTask.taskWidget.pageSize,
      }"
      :hasExtend="false"
    >
      <vxe-column
        title="No."
        type="seq"
        width="50px"
        :index="formMyTask.taskWidget.getTableIndex"
      />
      <vxe-column title="Process Name" field="processDefinitionName" />
      <vxe-column title="Current Task" field="taskName" />
      <vxe-column title="Initiator Login Name" field="processInstanceInitiator" />
      <vxe-column title="Initiator Show Name" field="showName" />
      <vxe-column title="Task Initiation Time" field="processInstanceStartTime" />
      <vxe-column title="Operation" width="100px">
        <template v-slot="scope">
          <general-button
            text="Handle"
            :size="layoutStore.defaultFormItemSize"
            @btnClick="onSubmit(scope.row)"
          />
        </template>
      </vxe-column>
      <template #pagination>
        <pagination
          :total="formMyTask.taskWidget.totalCount"
          :currentPage="formMyTask.taskWidget.currentPage"
          :pageSize="formMyTask.taskWidget.pageSize"
          size="default"
          @currentChange="formMyTask.taskWidget.onCurrentPageChange"
          @sizeChange="formMyTask.taskWidget.onPageSizeChange"
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

const router = useRouter();
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const form = ref();

const loadTaskData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    processDefinitionName: formMyTask.value.formFilterCopy.processDefinitionName,
    processDefinitionKey: formMyTask.value.formFilterCopy.processDefinitionKey,
    taskName: formMyTask.value.formFilterCopy.taskName,
    ...params,
  };
  return new Promise((resolve, reject) => {
    FlowOperationController.listRuntimeTask(params)
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
const loadTaskDataVerify = () => {
  formMyTask.value.formFilterCopy.processDefinitionName =
    formMyTask.value.formFilter.processDefinitionName;
  formMyTask.value.formFilterCopy.processDefinitionKey =
    formMyTask.value.formFilter.processDefinitionKey;
  formMyTask.value.formFilterCopy.taskName = formMyTask.value.formFilter.taskName;
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadTaskData,
  verifyTableParameter: loadTaskDataVerify,
  paged: true,
  orderFieldName: 'entryId',
  ascending: true,
};
const formMyTask = ref({
  formFilter: {
    processDefinitionName: undefined,
    processDefinitionKey: undefined,
    taskName: undefined,
  },
  formFilterCopy: {
    processDefinitionName: undefined,
    processDefinitionKey: undefined,
    taskName: undefined,
  },
  taskWidget: useTable(tableOptions),
  isInit: false,
});

const refreshMyTask = (reloadData = false) => {
  if (reloadData) {
    formMyTask.value.taskWidget.refreshTable(true, 1);
  } else {
    formMyTask.value.taskWidget.refreshTable();
  }
};
const onReset = () => {
  form.value.resetFields();
  refreshMyTask(true);
};
const onResume = () => {
  refreshMyTask();
};
const formInit = () => {
  refreshMyTask();
};

const onSubmit = (row: ANY_OBJECT) => {
  let params = {
    processInstanceId: row.processInstanceId,
    processDefinitionId: row.processDefinitionId,
    taskId: row.taskId,
  };

  FlowOperationController.viewRuntimeTaskInfo(params)
    .then(res => {
      console.log('......', res);
      if (res.data) {
        router.push({
          name: 'handlerFlowTask',
          query: {
            isRuntime: 'true',
            isDraft: row.isDraft,
            taskId: row.taskId,
            processDefinitionKey: row.processDefinitionKey,
            processInstanceId: row.processInstanceId,
            processDefinitionId: row.processDefinitionId,
            formId: res.data.formId,
            routerName: res.data.routerName,
            readOnly: res.data.readOnly,
            taskName: row.taskName,
            flowEntryName: row.processDefinitionName,
            processInstanceInitiator: row.showName,
            // Filter out co-signing and revoke operations, as these can only be performed in completed tasks
            operationList: JSON.stringify(
              (res.data.operationList || []).filter((item: ANY_OBJECT) => {
                return (
                  item.type !== SysFlowTaskOperationType.CO_SIGN &&
                  item.type !== SysFlowTaskOperationType.REVOKE &&
                  item.type !== SysFlowTaskOperationType.SIGN_REDUCTION
                );
              }),
            ),
            variableList: JSON.stringify(res.data.variableList),
          },
        });
      }
    })
    .catch(e => {
      console.warn(e);
    });
};

watch(
  () => route.name,
  () => {
    if (route.name === 'formMyTask') {
      formInit();
    }
  },
  { immediate: true },
);

defineExpose({ onResume });
</script>

<style scope>
.my-task{
  border-radius: 20px;
}
</style>
