<template>
  <!-- To-Do Tasks -->
  <div style="position: relative" class="broder-radius-20">
    <el-form
      ref="form"
      :model="formMyTask"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row class="form-title">
        <el-col :span="24"> My Task </el-col>
      </el-row>
      <filter-box
        :item-width="320"
        :hasFold="true"
        :hasRefresh="true"
        :hasDownload="true"
        @search="refreshMyTask(true)"
        @reset="onReset"
      >
        <el-form-item label="MyOrder No." prop="formFilter.orderNumber" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.orderNumber"
            :clearable="true"
            placeholder="MyOrder No."
          />
        </el-form-item>
        <el-form-item label="Sold To" prop="formFilter.soldTo" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.soldTo"
            :clearable="true"
            placeholder="sold to"
          />
        </el-form-item>
        <el-form-item label="Status" prop="formFilter.status" label-position="top">
          <el-select
            class="filter-item"
            v-model="formMyTask.formFilter.status"
            :clearable="true"
            filterable
            placeholder="All"
          >
            <el-option
              v-for="item in MyTaskStatus.getList()"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Request" prop="formFilter.requestType" label-position="top">
          <el-select
            class="filter-item"
            v-model="formMyTask.formFilter.requestType"
            :clearable="true"
            filterable
            placeholder="All"
          >
            <el-option
              v-for="item in requestTypeList as Array<{ orderType: string }>"
              :key="item.orderType"
              :value="item.orderType"
              :label="item.orderType"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Submitted Date" prop="formFilter.submittedDate" label-position="top">
          <date-range
            class="filter-item"
            v-model:value="formMyTask.formFilter.submittedDate"
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
        <el-form-item label="Requestor" prop="formFilter.requestor" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyTask.formFilter.requestor"
            :clearable="true"
            placeholder="requestor"
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
        startIndex: ((formMyTask.taskWidget.currentPage as number) - 1) * (formMyTask.taskWidget.pageSize as number),
      }"
      :hasExtend="false"
    >
      <vxe-column title="MyOrder No.">
        <template v-slot="scope">
          <el-button color="#005FFF" plain link @click="onDetail(scope.row)">
            {{ scope.row.orderNumber }}
          </el-button>
        </template>
      </vxe-column>
      <vxe-column title="Sold To" field="soldTo" />
      <vxe-column title="Request" field="requestType" />
      <vxe-column title="Pending Approval By" field="pendingBy" />
      <vxe-column title="Requestor" field="requestor" />
      <vxe-column title="Submitted Date" field="submitttedTime" />
      <vxe-column title="Status">
        <template v-slot="scope">
          <el-tag
            effect="plain"
            :size="layoutStore.defaultFormItemSize"
            :type="
              MyTaskStatus.getValue(scope.row.status) === 'Approved'
                ? 'success'
                : MyTaskStatus.getValue(scope.row.status) === 'Rejected'
                ? 'danger'
                : MyTaskStatus.getValue(scope.row.status) === 'Pending'
                ? 'warning'
                : 'primary'
            "
          >
            {{ MyTaskStatus.getValue(scope.row.status) || '' }}
          </el-tag>
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
import { useRouter, useRoute } from 'vue-router';
import { FlowOperationController, TaskAndRequestController } from '@/api/flow';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { ANY_OBJECT } from '@/types/generic';
import { SysFlowTaskOperationType, MyTaskStatus } from '@/common/staticDict/flow';
import { setEndOfDay } from '@/common/utils';
const route = useRoute();

const router = useRouter();
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const form = ref();
const requestTypeList = ref({});

const loadTaskData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    orderNumber: formMyTask.value.formFilterCopy.orderNumber,
    soldTo: formMyTask.value.formFilterCopy.soldTo,
    orderType: formMyTask.value.formFilterCopy.requestType,
    pendingBy: formMyTask.value.formFilterCopy.pendingBy,
    requestor: formMyTask.value.formFilterCopy.requestor,
    beginDate: formMyTask.value.formFilterCopy.submittedDate[0],
    endDate: formMyTask.value.formFilterCopy.submittedDate.length
      ? setEndOfDay(formMyTask.value.formFilterCopy.submittedDate[1])
      : undefined,
    status: formMyTask.value.formFilterCopy.status,
    ...params,
  };
  return new Promise((resolve, reject) => {
    TaskAndRequestController.listMyTask(params)
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
  formMyTask.value.formFilterCopy.orderNumber = formMyTask.value.formFilter.orderNumber;
  formMyTask.value.formFilterCopy.soldTo = formMyTask.value.formFilter.soldTo;
  formMyTask.value.formFilterCopy.requestType = formMyTask.value.formFilter.requestType;
  formMyTask.value.formFilterCopy.pendingBy = formMyTask.value.formFilter.pendingBy;
  formMyTask.value.formFilterCopy.requestor = formMyTask.value.formFilter.requestor;
  formMyTask.value.formFilterCopy.submittedDate = formMyTask.value.formFilter.submittedDate;
  formMyTask.value.formFilterCopy.status = formMyTask.value.formFilter.status;
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
    orderNumber: undefined,
    status: undefined,
    soldTo: undefined,
    submittedDate: [],
    requestor: undefined,
    requestType: undefined,
    pendingBy: undefined,
  },
  formFilterCopy: {
    orderNumber: undefined,
    status: undefined,
    soldTo: undefined,
    submittedDate: [],
    requestor: undefined,
    requestType: undefined,
    pendingBy: undefined,
  },
  taskWidget: useTable(tableOptions),
  isInit: false,
});
const getRequestTypeList = () => {
  return new Promise((resolve, reject) => {
    TaskAndRequestController.getRequestTypeList({})
      .then(res => {
        requestTypeList.value = res.data;
      })
      .catch(e => {
        reject(e);
      });
  });
};
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
  getRequestTypeList();
};

const onDetail = (row: ANY_OBJECT) => {
  if (row.status === '1') {
    // pending
    let params = {
      processInstanceId: row.processInstanceId,
      processDefinitionId: row.processDefinitionId,
      taskId: row.taskId,
    };

    FlowOperationController.viewRuntimeTaskInfo(params)
      .then(res => {
        if (res.data) {
          goToDetail(row, res);
        }
      })
      .catch(e => {
        console.warn(e);
      });
  } else {
    goToDetail(row);
  }
};

const goToDetail = (row: ANY_OBJECT, res?: ANY_OBJECT) => {
  router.push({
    name: 'detailTaskAndRequest',
    query: {
      isRuntime: 'true',
      isDraft: row.isDraft,
      taskId: row.taskId,
      processDefinitionKey: row.processDefinitionKey,
      processInstanceId: row.processInstanceId,
      processDefinitionId: row.processDefinitionId,
      formId: res?.data.formId,
      routerName: res?.data.routerName,
      readOnly: res?.data.readOnly,
      taskName: row.taskName,
      flowEntryName: row.processDefinitionName,
      processInstanceInitiator: row.showName,
      // Filter out co-signing and revoke operations, as these can only be performed in completed tasks
      operationList: JSON.stringify(
        (res?.data.operationList || []).filter((item: ANY_OBJECT) => {
          return (
            item.type !== SysFlowTaskOperationType.CO_SIGN &&
            item.type !== SysFlowTaskOperationType.REVOKE &&
            item.type !== SysFlowTaskOperationType.SIGN_REDUCTION
          );
        }),
      ),
      variableList: JSON.stringify(res?.data.variableList),
      requestType: row.requestType,
      requestor: row.requestor,
      sr: row.sr,
      requestDate: row.requestDate,
      orderNumber: row.orderNumber,
      status: row.status,
      pendingBy: row.pendingBy,
      soldTo: row.soldTo,
      shipTo: row.shipTo,
      stockLocation: row.stockLocation,
      orderDetails: JSON.stringify(row.orderDetails),
    },
  });
};

watch(
  () => route.name,
  () => {
    if (route.name === 'myTask') {
      formInit();
    }
  },
  { immediate: true },
);

defineExpose({ onResume });
</script>
