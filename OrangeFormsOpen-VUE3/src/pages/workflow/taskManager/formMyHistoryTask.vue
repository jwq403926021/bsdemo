<template>
  <!-- Historical Tasks -->
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFilter"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box :item-width="350" @search="refreshFormMyHistoryTask(true)" @reset="onReset">
        <el-form-item label="Process Name" prop="processDefinitionName">
          <el-input
            class="filter-item"
            v-model="formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
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
      :data="formMyHistoryTaskWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formMyHistoryTaskWidget.onSortChange"
      @refresh="refreshFormMyHistoryTask(true)"
      :seq-config="{
        startIndex: (formMyHistoryTaskWidget.currentPage - 1) * formMyHistoryTaskWidget.pageSize,
      }"
      :hasExtend="false"
    >
      <vxe-column
        title="No."
        type="seq"
        width="70px"
        :index="formMyHistoryTaskWidget.getTableIndex"
      />
      <vxe-column title="Process Name" field="processDefinitionName" />
      <vxe-column title="Process Identifier" field="processDefinitionKey" />
      <vxe-column title="Initiator Login Name" field="startUserId" />
      <vxe-column title="Initiator Nickname" field="showName" />
      <vxe-column title="Task Initiation Time" field="startTime" />
      <vxe-column title="Task End Time" field="endTime" />
      <vxe-column title="Operation" width="90px">
        <template v-slot="scope">
          <el-button
            :size="layoutStore.defaultFormItemSize"
            link
            type="primary"
            @click="onFlowDetail(scope.row)"
            >Details</el-button
          >
        </template>
      </vxe-column>
      <template #pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formMyHistoryTaskWidget.totalCount"
            :current-page="formMyHistoryTaskWidget.currentPage"
            :page-size="formMyHistoryTaskWidget.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="formMyHistoryTaskWidget.onCurrentPageChange"
            @size-change="formMyHistoryTaskWidget.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
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
import { useRoute } from 'vue-router';
const route = useRoute();

const form = ref();
const router = useRouter();
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

/**
 * Get all process instances
 */
const loadMyHistoryTaskData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    processDefinitionName: formFilterCopy.processDefinitionName,
    beginDate: formFilterCopy.createDate[0],
    endDate: formFilterCopy.createDate[1],
  };

  return new Promise((resolve, reject) => {
    FlowOperationController.listHistoricProcessInstance(params)
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
const loadMyHistoryTaskVerify = () => {
  formFilterCopy.processDefinitionName = formFilter.value.processDefinitionName;
  formFilterCopy.createDate = Array.isArray(formFilter.value.createDate)
    ? [...formFilter.value.createDate]
    : [];
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadMyHistoryTaskData,
  verifyTableParameter: loadMyHistoryTaskVerify,
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
const formMyHistoryTaskWidget = reactive(useTable(tableOptions));
let isInit = false;

const refreshFormMyHistoryTask = (reloadData = false) => {
  if (reloadData) {
    formMyHistoryTaskWidget.refreshTable(true, 1);
  } else {
    formMyHistoryTaskWidget.refreshTable();
  }
  // eslint-disable-next-line no-empty
  if (!isInit) {
    // Initialize dropdown data
    // do nothing
  }
  isInit = true;
};
const onReset = () => {
  form.value.resetFields();
  refreshFormMyHistoryTask(true);
};

const onFlowDetail = (row: ANY_OBJECT) => {
  let params = {
    processInstanceId: row.processInstanceId,
  };

  FlowOperationController.viewInitialHistoricTaskInfo(params)
    .then(res => {
      if (res.data) {
        router.push({
          name: 'handlerFlowTask',
          query: {
            processDefinitionKey: row.processDefinitionKey,
            taskId: null,
            processInstanceId: row.processInstanceId,
            processDefinitionId: row.processDefinitionId,
            formId: res.data.formId,
            routerName: res.data.routerName,
            readOnly: 'true',
            flowEntryName: row.processDefinitionName,
            processInstanceInitiator: row.showName,
          },
        });
      }
    })
    .catch(e => {
      console.warn(e);
    });
};
const onResume = () => {
  refreshFormMyHistoryTask();
};
const formInit = () => {
  refreshFormMyHistoryTask();
};

watch(
  () => route.name,
  () => {
    if (route.name === 'formMyHistoryTask') {
      formInit();
    }
  },
  {immediate: true}
);

defineExpose({ onResume });
</script>
