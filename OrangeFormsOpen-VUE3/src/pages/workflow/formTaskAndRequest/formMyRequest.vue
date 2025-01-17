<template>
  <!-- My Request -->
  <div style="position: relative" class="broder-radius-20">
    <el-form
      ref="form"
      :model="formMyRequest"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row class="form-title">
        <el-col :span="24"> My Request </el-col>
      </el-row>
      <filter-box
        :item-width="320"
        :hasFold="true"
        :hasRefresh="true"
        :hasDownload="true"
        @search="refreshMyRequest(true)"
        @reset="onReset"
      >
        <el-form-item label="MyOrder No." prop="formFilter.myOrderNo" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.myOrderNo"
            :clearable="true"
            placeholder="MyOrder No."
          />
        </el-form-item>
        <el-form-item label="Order Type" prop="formFilter.orderType" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.orderType"
            :clearable="true"
            placeholder="Order Type"
          />
        </el-form-item>
        <el-form-item label="Account Name" prop="formFilter.accountName" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.accountName"
            :clearable="true"
            placeholder="Account Name"
          />
        </el-form-item>
        <el-form-item label="Ship to" prop="formFilter.shipTo" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.shipTo"
            :clearable="true"
            placeholder="Ship to"
          />
        </el-form-item>
        <el-form-item label="Stock Location" prop="formFilter.stockLocation" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.stockLocation"
            :clearable="true"
            placeholder="Stock Location"
          />
        </el-form-item>
        <el-form-item label="Division" prop="formFilter.division" label-position="top">
          <el-select
            class="filter-item"
            v-model="formMyRequest.formFilter.division"
            :clearable="true"
            filterable
            placeholder="All"
          >
            <el-option
              v-for="item in divisionsList"
              :key="item.code"
              :value="item.name"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Status" prop="formFilter.status" label-position="top">
          <el-select
            class="filter-item"
            v-model="formMyRequest.formFilter.status"
            :clearable="true"
            filterable
            placeholder="All"
          >
            <el-option
              v-for="item in MyRequestStatus.getList()"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="UPN" prop="formFilter.upn" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.upn"
            :clearable="true"
            placeholder="UPN"
          />
        </el-form-item>
        <el-form-item label="Submitted Date" prop="formFilter.submittedDate" label-position="top">
          <date-range
            class="filter-item"
            v-model:value="formMyRequest.formFilter.submittedDate"
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
        <el-form-item label="Submitted By" prop="formFilter.submittedBy" label-position="top">
          <el-input
            class="filter-item"
            v-model="formMyRequest.formFilter.submittedBy"
            :clearable="true"
            placeholder="Submitted By"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      :data="formMyRequest.taskWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formMyRequest.taskWidget.onSortChange"
      @refresh="refreshMyRequest(true)"
      :seq-config="{
        startIndex: ((formMyRequest.taskWidget.currentPage as number) - 1) * (formMyRequest.taskWidget.pageSize as number),
      }"
      :hasExtend="false"
    >
      <vxe-column title="MyOrder No." field="myOrderNo">
        <template v-slot="scope">
          <el-button color="#005FFF" plain link @click="onDetail(scope.row)">
            {{ scope.row.myOrderNo }}
          </el-button>
        </template>
      </vxe-column>
      <vxe-column title="Order Type" field="orderType" />
      <vxe-column title="Customer" field="customer" />
      <vxe-column title="Ship To" field="shipTo" />
      <vxe-column title="Pending Approval By" field="pendingApprovalBy" />
      <vxe-column title="Submitted By" field="submittedBy" />
      <vxe-column title="Submitted Date" field="submittedDate" />
      <vxe-column title="Status">
        <template v-slot="scope">
          <el-tag
            effect="plain"
            :size="layoutStore.defaultFormItemSize"
            :type="
              MyRequestStatus.getValue(scope.row.status) === 'Approved'
                ? 'success'
                : MyRequestStatus.getValue(scope.row.status) === 'Rejected'
                ? 'danger'
                : MyRequestStatus.getValue(scope.row.status) === 'Pending'
                ? 'warning'
                : 'primary'
            "
          >
            {{ MyRequestStatus.getValue(scope.row.status) || '' }}
          </el-tag>
        </template>
      </vxe-column>
      <template #pagination>
        <pagination
          :total="formMyRequest.taskWidget.totalCount"
          :currentPage="formMyRequest.taskWidget.currentPage"
          :pageSize="formMyRequest.taskWidget.pageSize"
          size="default"
          @currentChange="formMyRequest.taskWidget.onCurrentPageChange"
          @sizeChange="formMyRequest.taskWidget.onPageSizeChange"
        />
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import axios from 'axios';
import { useRouter, useRoute } from 'vue-router';
import { TaskAndRequestController } from '@/api/flow';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { ANY_OBJECT } from '@/types/generic';
import {
  SysFlowTaskOperationType,
  MyRequestStatus,
  MyRequestDivisions,
} from '@/common/staticDict/flow';
import { setEndOfDay } from '@/common/utils';
import { serverDefaultCfg } from '@/common/http/config';
const route = useRoute();

const router = useRouter();
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const form = ref();
const divisionsList = ref();

const loadRequestData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    myOrderNo: formMyRequest.value.formFilterCopy.myOrderNo,
    orderType: formMyRequest.value.formFilterCopy.orderType,
    accountName: formMyRequest.value.formFilterCopy.accountName,
    shipTo: formMyRequest.value.formFilterCopy.shipTo,
    stockLocation: formMyRequest.value.formFilterCopy.stockLocation,
    division: formMyRequest.value.formFilterCopy.division,
    status: formMyRequest.value.formFilterCopy.status,
    upn: formMyRequest.value.formFilterCopy.upn,
    submittedStartDate: formMyRequest.value.formFilterCopy.submittedDate[0],
    submittedEndDate: formMyRequest.value.formFilterCopy.submittedDate.length
      ? setEndOfDay(formMyRequest.value.formFilterCopy.submittedDate[1])
      : undefined,
    submittedBy: formMyRequest.value.formFilterCopy.submittedBy,
    ...params,
  };
  return new Promise((resolve, reject) => {
    TaskAndRequestController.listMyRequest(params)
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
const loadRequestDataVerify = () => {
  formMyRequest.value.formFilterCopy.myOrderNo = formMyRequest.value.formFilter.myOrderNo;
  formMyRequest.value.formFilterCopy.orderType = formMyRequest.value.formFilter.orderType;
  formMyRequest.value.formFilterCopy.accountName = formMyRequest.value.formFilter.accountName;
  formMyRequest.value.formFilterCopy.shipTo = formMyRequest.value.formFilter.shipTo;
  formMyRequest.value.formFilterCopy.stockLocation = formMyRequest.value.formFilter.stockLocation;
  formMyRequest.value.formFilterCopy.submittedDate = formMyRequest.value.formFilter.submittedDate;
  formMyRequest.value.formFilterCopy.status = formMyRequest.value.formFilter.status;
  formMyRequest.value.formFilterCopy.division = formMyRequest.value.formFilter.division;
  formMyRequest.value.formFilterCopy.upn = formMyRequest.value.formFilter.upn;
  formMyRequest.value.formFilterCopy.submittedBy = formMyRequest.value.formFilter.submittedBy;
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadRequestData,
  verifyTableParameter: loadRequestDataVerify,
  paged: true,
  orderFieldName: 'entryId',
  ascending: true,
};
const formMyRequest = ref({
  formFilter: {
    myOrderNo: undefined,
    orderType: undefined,
    shipTo: undefined,
    status: undefined,
    submittedBy: undefined,
    submittedDate: [],
    accountName: undefined,
    stockLocation: undefined,
    division: undefined,
    upn: undefined,
  },
  formFilterCopy: {
    myOrderNo: undefined,
    orderType: undefined,
    shipTo: undefined,
    status: undefined,
    submittedBy: undefined,
    submittedDate: [],
    accountName: undefined,
    stockLocation: undefined,
    division: undefined,
    upn: undefined,
  },
  taskWidget: useTable(tableOptions),
  isInit: false,
});

const refreshMyRequest = (reloadData = false) => {
  if (reloadData) {
    formMyRequest.value.taskWidget.refreshTable(true, 1);
  } else {
    formMyRequest.value.taskWidget.refreshTable();
  }
};
const getDivisions = async () => {
  const res = await axios.get(`${serverDefaultCfg.baseURL}order/divisions`);
  divisionsList.value = res.data;
};
const onReset = () => {
  form.value.resetFields();
  refreshMyRequest(true);
};
const onResume = () => {
  refreshMyRequest();
};
const formInit = () => {
  getDivisions();
  refreshMyRequest();
};

const onDetail = (row: ANY_OBJECT) => {
  let params = {
    orderNo: row.myOrderNo,
  };

  TaskAndRequestController.getMyRequestOrderDetail(params)
    .then(res => {
      if (res.data) {
        router.push({
          name: 'detailTaskAndRequest',
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
            // my request
            formType: 'request',
            productTotalCount: res.data.productTotalCount,
            rejectReason: res.data.rejectReason ?? '',
            requestData: JSON.stringify(res.data),
            shippingOrder: JSON.stringify(res.data.shippingOrder),
            productList: JSON.stringify(res.data.productList),
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
    if (route.name === 'myRequest') {
      formInit();
    }
  },
  { immediate: true },
);

defineExpose({ onResume });
</script>
