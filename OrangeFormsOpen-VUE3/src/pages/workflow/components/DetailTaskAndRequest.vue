<template>
  <div class="flow-task" style="position: relative; padding: 16px 24px; background-color: white">
    <div class="task-title">
      <div>
        <span class="text">{{ flowInfo.flowEntryName }}</span>
        <el-tag
          v-if="flowInfo.taskName"
          effect="dark"
          :size="layoutStore.defaultFormItemSize"
          type="success"
          >{{ 'Current Node: ' + flowInfo.taskName }}</el-tag
        >
        <el-tag
          v-if="flowInfo.processInstanceInitiator"
          effect="dark"
          :size="layoutStore.defaultFormItemSize"
          type="info"
          >{{ 'Initiator: ' + flowInfo.processInstanceInitiator }}</el-tag
        >
      </div>
    </div>
    <el-row justify="space-between" style="margin-bottom: 24px; flex-wrap: nowrap">
      <el-radio-group size="large" v-model="currentPage" style="min-width: 400px">
        <el-radio-button value="formInfo">{{
          formType === 'task'
            ? requestType
            : formType === 'request'
            ? 'Order Detail'
            : 'CC Check Detail'
        }}</el-radio-button>
        <el-radio-button v-if="processInstanceId != null && !isDraft" value="flowProcess"
          >Flow Chart</el-radio-button
        >
      </el-radio-group>
    </el-row>
    <el-scrollbar class="custom-scroll" :style="{ height: mainContextHeight - 180 + 'px' }">
      <el-form
        ref="form"
        class="full-width-input"
        style="width: 100%"
        label-width="100px"
        :size="layoutStore.defaultFormItemSize"
        label-position="right"
        @submit.prevent
      >
        <!-- Form Information -->
        <div v-show="currentPage === 'formInfo'" :key="formKey">
          <el-row class="information-form-title">
            <el-col>{{
              formType === 'task'
                ? 'Basic Information'
                : formType === 'request'
                ? 'Order Detail'
                : 'Shipping Order'
            }}</el-col>
          </el-row>
          <div v-for="(value, key) in basicInfo" :key="key" class="basic-info">
            <div class="label">{{ FormInfo[key] }}</div>
            <div v-if="key === 'status'" class="value">
              <el-tag
                v-if="formType === 'task'"
                effect="plain"
                :size="layoutStore.defaultFormItemSize"
                :type="
                  MyTaskStatus.getValue(value) === 'Approved'
                    ? 'success'
                    : MyTaskStatus.getValue(value) === 'Rejected'
                    ? 'danger'
                    : MyTaskStatus.getValue(value) === 'Pending'
                    ? 'warning'
                    : 'primary'
                "
              >
                {{ MyTaskStatus.getValue(value) }}
              </el-tag>
              <el-tag
                v-else
                effect="plain"
                :size="layoutStore.defaultFormItemSize"
                :type="
                  ['Approved', 'Completed'].includes(MyRequestStatus.getValue(value))
                    ? 'success'
                    : ['Rejected'].includes(MyRequestStatus.getValue(value))
                    ? 'danger'
                    : ['Waiting CC Check', 'Pending'].includes(MyRequestStatus.getValue(value))
                    ? 'warning'
                    : 'primary'
                "
              >
                {{ MyRequestStatus.getValue(value) }}
              </el-tag>
            </div>
            <div v-else class="value">{{ value }}</div>
            <div v-if="key === 'ccOwner'" class="value">
              <el-select v-model="ccOwner" placeholder="Select CC Owner" style="width: 200px">
                <el-option
                  v-for="item in ccOwnerList"
                  :key="item.userId"
                  :label="item.loginName"
                  :value="item.loginName"
                />
              </el-select>
            </div>
          </div>
          <!-- <slot name="formInfo" /> -->
          <el-row class="information-form-title">
            <el-col>{{ formType === 'task' ? 'Order Detail' : 'Selected Products' }}</el-col>
          </el-row>
          <el-row :gutter="20" class="information-form-table">
            <el-col :span="24">
              <vxe-table
                empty-text="No Data"
                :data="orderDetailsList"
                header-cell-class-name="table-header-gray"
                :max-height="mainContextHeight - 150 + 'px'"
                min-height="92px"
                :row-config="{ isHover: true }"
              >
                <vxe-column title="UPN" field="productUpn" />
                <vxe-column title="Product Name" field="productName" />
                <vxe-column
                  align="center"
                  :title="formType === 'task' ? 'Order. Qty' : 'Quantity'"
                  :field="formType === 'task' ? 'qty' : 'quantity'"
                >
                  <template #default="{ row }">
                    <span style="color: #99b2c7">
                      {{ formType === 'task' ? row.qty : `x${row.quantity}` }}
                    </span>
                  </template>
                </vxe-column>
                <vxe-column
                  v-if="['request', 'ccCheck'].includes(formType)"
                  title="Rejected Reason"
                  field="rejectedReason"
                />
                <template v-slot:empty>
                  <div class="table-empty unified-font">
                    <img src="@/assets/img/empty.png" />
                    <span>No Data</span>
                  </div>
                </template>
              </vxe-table>
            </el-col>
          </el-row>
          <el-row v-if="formType === 'task'" class="information-form-title">
            <el-col>Approval Record</el-col>
          </el-row>
          <el-row v-if="formType === 'task'" :gutter="20" class="information-form-table">
            <el-col :span="24">
              <vxe-table
                empty-text="No Data"
                :data="flowTaskCommentList"
                header-cell-class-name="table-header-gray"
                :max-height="mainContextHeight - 150 + 'px'"
                min-height="92px"
                :row-config="{ isHover: true }"
              >
                <vxe-column title="No." type="seq" width="50" />
                <vxe-column title="Role Type" field="roleType" />
                <vxe-column title="Operator" field="createUsername" />
                <vxe-column title="Action" width="150px">
                  <template v-slot="scope">
                    <el-tag
                      :size="layoutStore.defaultFormItemSize"
                      :type="getOperationTagType(scope.row.approvalType)"
                      :style="{ minWidth: '68px' }"
                      effect="dark"
                      >{{ SysFlowTaskOperationResultType.getValue(scope.row.approvalType) }}</el-tag
                    >
                    <el-tag
                      v-if="scope.row.delegateAssignee != null"
                      :size="layoutStore.defaultFormItemSize"
                      type="success"
                      effect="plain"
                      style="margin-left: 10px"
                      >{{ scope.row.delegateAssignee }}</el-tag
                    >
                  </template>
                </vxe-column>
                <vxe-column title="Comment">
                  <template v-slot="scope">
                    <span>{{ scope.row.taskComment ? scope.row.taskComment : '' }}</span>
                  </template>
                </vxe-column>
                <vxe-column title="Create Time" field="createTime" />
                <template v-slot:empty>
                  <div class="table-empty unified-font">
                    <img src="@/assets/img/empty.png" />
                    <span>No Data</span>
                  </div>
                </template>
              </vxe-table>
            </el-col>
          </el-row>
          <el-row v-if="['request', 'ccCheck'].includes(formType)" class="information-form-title">
            <el-col>Shipping Condition</el-col>
          </el-row>
          <div v-if="['request', 'ccCheck'].includes(formType)" class="contact-info">
            <div div class="contact-info-title">Contact info for packing list</div>
            <div v-for="(value, key) in contactInfo" :key="key" class="basic-info">
              <div class="label">{{ FormInfo[key] }}</div>
              <div class="value">{{ value }}</div>
            </div>
            <div div class="contact-info-title">Remarks to Warehouse</div>
            <el-radio-group disabled v-model="remarksToWarehouse" class="radio-group">
              <el-radio label="Standard Delivery">Standard delivery</el-radio>
              <el-radio label="Urgent NSD">Urgent non-standard delivery</el-radio>
            </el-radio-group>
            <el-form
              v-if="remarksToWarehouse !== 'Standard Delivery'"
              class="urgent-form"
              ref="form"
              label-width="180px"
              label-position="top"
            >
              <el-row :gutter="48">
                <el-col :span="6">
                  <el-form-item span="12" label="Request Delivery Date" prop="requestDeliveryDate">
                    <el-date-picker
                      disabled
                      v-model="shippingInfo.deliveryDate"
                      type="date"
                      placeholder="Request Delivery Date"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="6">
                  <el-form-item label="Paying" prop="paying">
                    <el-select disabled v-model="paying" placeholder="Paying">
                      <el-option label="BSC paying" value="BSC paying"></el-option>
                      <el-option label="Customer paying" value="Customer paying"></el-option>
                    </el-select>
                  </el-form-item>
                </el-col>
              </el-row>
            </el-form>
            <!-- <div class="delivery-date-box">
              <div>
                <span>Request Delivery Date</span>
                <span class="delivery-date-box-required">*</span>
              </div>
              <el-date-picker v-model="shippingInfo.deliveryDate" type="date" disabled />
            </div> -->
          </div>
        </div>
        <!-- Flow Chart -->
        <el-row v-if="currentPage === 'flowProcess'">
          <ProcessViewer
            :style="{ height: mainContextHeight - 148 + 'px' }"
            :xml="processXml"
            :finishedInfo="finishedInfo"
            :allCommentList="flowTaskCommentList"
          />
        </el-row>
        <!-- Cc Settings -->
        <el-row v-show="currentPage === 'copyInfo'">
          <el-col :span="24" style="border-top: 1px solid #ebeef5">
            <CopyForSelect v-model:value="copyItemList" />
          </el-col>
        </el-row>
      </el-form>
    </el-scrollbar>
    <el-row class="task-operation" justify="end" style="flex-wrap: nowrap">
      <template v-if="$slots.operations">
        <slot name="operations" />
      </template>
      <template v-else>
        <el-button size="default" class="broder-radius-16" type="info" @click="emit('close')">
          Back
        </el-button>
        <el-button
          v-if="formType === 'ccCheck'"
          :disabled="basicInfo['status'] === 3"
          size="default"
          class="broder-radius-16"
          type="danger"
          @click="handlerOperation({ label: 'Reject', showOrder: '1', type: 'refuse' })"
        >
          Reject
        </el-button>
        <el-button
          v-if="formType === 'ccCheck'"
          :disabled="basicInfo['status'] === 3"
          size="default"
          class="broder-radius-16"
          type="primary"
          @click="submitCcOwner()"
        >
          Submit
        </el-button>
        <el-button
          v-for="(operation, index) in flowOperationList"
          :key="index"
          size="default"
          class="broder-radius-16"
          :type="getButtonType(operation.type) || 'primary'"
          :plain="operation.plain || false"
          @click="handlerOperation(operation)"
        >
          {{ operation.label }}
        </el-button>
      </template>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { VxeTable, VxeColumn } from 'vxe-table';
import { ElMessage } from 'element-plus';
import CopyForSelect from '@/pages/workflow/components/CopyForSelect/index.vue';
import ProcessViewer from '@/pages/workflow/components/ProcessViewer.vue';
import { FlowOperationController, TaskAndRequestController } from '@/api/flow';
import { ANY_OBJECT } from '@/types/generic';
import {
  SysFlowTaskOperationType,
  SysFlowTaskOperationResultType,
  MyTaskStatus,
  MyRequestStatus,
} from '@/common/staticDict/flow';
import { FormInfo, FormInfoSort } from '@/common/enum/useForm';
import { useLayoutStore } from '@/store';

const layoutStore = useLayoutStore();

const emit = defineEmits<{
  start: [ANY_OBJECT, ANY_OBJECT[], string | undefined];
  submit: [ANY_OBJECT, ANY_OBJECT[], string | undefined];
  close: [];
  draft: [];
}>();

const props = withDefaults(
  defineProps<{
    // 流程实例id
    processInstanceId?: string;
    // 流程定义id
    processDefinitionId?: string;
    isRuntime?: boolean | string;
    isDraft?: boolean | string;
    // 流程名称
    flowEntryName?: string;
    // 发起人
    processInstanceInitiator?: string;
    // 当前任务ID
    taskId?: string;
    // 当前任务节点名称
    taskName?: string;
    // 当前任务节点操作列表
    operationList?: Array<ANY_OBJECT> | string;
    // my task
    formType?: string;
    requestType?: string;
    requestor?: string;
    sr?: string;
    requestDate?: string;
    orderNumber?: string;
    status?: string | number;
    pendingBy?: string;
    soldTo?: string;
    shipTo?: string;
    stockLocation?: string;
    orderDetails?: Array<ANY_OBJECT> | string;
    // my request
    ccOwner?: string | undefined;
    requestData?: string;
    productList?: Array<ANY_OBJECT> | string;
    rejectReason?: string;
    productTotalCount?: string;
  }>(),
  {
    formType: 'task', // task -- myTask, request -- myRequest
    isDraft: false,
  },
);

const mainContextHeight = inject('mainContextHeight', 200);

const formKey = new Date().getTime();
const currentPage = ref('formInfo');
const processXml = ref<string>();
const finishedInfo = ref<ANY_OBJECT>();
const flowInfo = reactive({
  taskName: props.taskName,
  flowEntryName: props.flowEntryName,
  processInstanceInitiator: props.processInstanceInitiator,
});
const ccOwner = ref<string | undefined>('');
const ccOwnerList = ref<ANY_OBJECT>([]);
const flowTaskCommentList = ref<ANY_OBJECT[]>([]);
const copyItemList = ref<ANY_OBJECT[]>([]);
const assigneeList = ref<ANY_OBJECT[]>([]);
const basicInfo = ref<ANY_OBJECT>({});

const flowOperationList = computed<ANY_OBJECT[]>(() => {
  const operationList = Array.isArray(props.operationList)
    ? props.operationList
    : JSON.parse(props.operationList || '[]');
  if (Array.isArray(operationList)) {
    return operationList.map(item => {
      if (item.type === SysFlowTaskOperationType.MULTI_SIGN && item.multiSignAssignee != null) {
        let multiSignAssignee = {
          ...item.multiSignAssignee,
        };
        multiSignAssignee.assigneeList = item.multiSignAssignee.assigneeList
          ? multiSignAssignee.assigneeList.split(',')
          : undefined;
        return {
          ...item,
          multiSignAssignee,
        };
      } else {
        return {
          ...item,
        };
      }
    });
  } else {
    return [];
  }
});

const orderDetailsList = computed(() => {
  if (props.formType === 'task') {
    return Array.isArray(props.orderDetails)
      ? props.orderDetails
      : JSON.parse(props.orderDetails || '[]');
  } else if (['request', 'ccCheck'].includes(props.formType)) {
    const productList =
      typeof props.requestData === 'string' ? JSON.parse(props.requestData)?.productList : [];
    if (productList.length !== 0) {
      productList.push({
        rejectedReason: props.rejectReason,
        quantity: props.productTotalCount,
      });
    }
    return productList;
  }
  return [];
});

const shippingInfo = computed(() => {
  if (['request', 'ccCheck'].includes(props.formType)) {
    return props.requestData && typeof props.requestData === 'string'
      ? JSON.parse(props.requestData)?.shippingCondition
      : {};
  }
  return {};
});

const remarksToWarehouse = computed(() => {
  if (['request', 'ccCheck'].includes(props.formType)) {
    return props.requestData && typeof props.requestData === 'string'
      ? JSON.parse(props.requestData)?.remarksToWarehouse
      : '';
  }
  return '';
});

const paying = computed(() => {
  if (['request', 'ccCheck'].includes(props.formType)) {
    return props.requestData && typeof props.requestData === 'string'
      ? JSON.parse(props.requestData)?.paying
      : {};
  }
  return {};
});

const contactInfo = computed(() => {
  if (['request', 'ccCheck'].includes(props.formType)) {
    const shippingCondition =
      props.requestData && typeof props.requestData === 'string'
        ? JSON.parse(props.requestData)?.shippingCondition
        : {};
    const { deliveryDate, ...rest } = shippingCondition;
    return rest;
  }
  return {};
});

const canDraft = computed(() => {
  // 启动或者草稿状态
  return props.processInstanceId == null || props.isDraft || props.isDraft === 'true';
});

const getButtonType = (type: string) => {
  switch (type) {
    case SysFlowTaskOperationType.AGREE:
    case SysFlowTaskOperationType.TRANSFER:
    case SysFlowTaskOperationType.CO_SIGN:
    case SysFlowTaskOperationType.SIGN_REDUCTION:
    case SysFlowTaskOperationType.BFORE_CONSIGN:
    case SysFlowTaskOperationType.AFTER_CONSIGN:
    case SysFlowTaskOperationType.MULTI_AGREE:
    case SysFlowTaskOperationType.MULTI_SIGN:
    case SysFlowTaskOperationType.SET_ASSIGNEE:
      return 'primary';
    case SysFlowTaskOperationType.SAVE:
      return 'success';
    case SysFlowTaskOperationType.PARALLEL_REFUSE:
    case SysFlowTaskOperationType.MULTI_REFUSE:
      return 'default';
    case SysFlowTaskOperationType.REFUSE:
    case SysFlowTaskOperationType.REJECT:
    case SysFlowTaskOperationType.REJECT_TO_TASK:
    case SysFlowTaskOperationType.REVOKE:
      return 'danger';
    case SysFlowTaskOperationType.BACK:
      return 'info';
    case SysFlowTaskOperationType.REJECT_TO_START:
      return 'warning';
    default:
      return 'default';
  }
};
const getOperationTagType = (type: string) => {
  switch (type) {
    case SysFlowTaskOperationType.AGREE:
    case SysFlowTaskOperationType.MULTI_AGREE:
    case SysFlowTaskOperationType.SET_ASSIGNEE:
      return 'success';
    case SysFlowTaskOperationType.REFUSE:
    case SysFlowTaskOperationType.PARALLEL_REFUSE:
    case SysFlowTaskOperationType.MULTI_REFUSE:
    case SysFlowTaskOperationType.STOP:
    case SysFlowTaskOperationType.REJECT:
    case SysFlowTaskOperationType.REJECT_TO_START:
    case SysFlowTaskOperationType.REJECT_TO_TASK:
    case SysFlowTaskOperationType.REVOKE:
      return 'danger';
    case SysFlowTaskOperationType.SUBMIT:
      return 'primary';
    default:
      return '';
  }
};
// 保存草稿
const handlerDraft = () => {
  emit('draft');
};
const onClose = () => {
  emit('close');
};
const handlerOperation = (operation: ANY_OBJECT) => {
  if (props.processInstanceId == null && props.taskId == null) {
    emit('start', operation, copyItemList.value, processXml.value);
  } else {
    emit('submit', operation, copyItemList.value, processXml.value);
  }
};

const submitCcOwner = () => {
  if (!ccOwner.value) {
    handlerOperation({ label: 'Approve', showOrder: '0', type: 'agree' });
    return;
  }
  const params = {
    userName: ccOwner.value,
    processInstanceId: props.processInstanceId,
    orderId: basicInfo.value.orderNo,
  };
  FlowOperationController.submitCcOwner(params).then(res => {
    ElMessage.success('Submit Successful');
    onClose();
  });
};
const getTaskHighlightData = () => {
  if (props.processInstanceId == null || props.processInstanceId === '') {
    return;
  }
  let params = {
    processInstanceId: props.processInstanceId,
  };

  FlowOperationController.viewHighlightFlowData(params)
    .then(res => {
      // 已完成节点
      finishedInfo.value = res.data;
    })
    .catch(e => {
      console.warn(e);
    });
};
const getTaskProcessXml = () => {
  if (props.processDefinitionId == null || props.processDefinitionId === '') {
    return;
  }
  let params = {
    processDefinitionId: props.processDefinitionId,
  };
  FlowOperationController.viewProcessBpmn(params)
    .then(res => {
      // 当前流程实例xml
      processXml.value = res.data;
    })
    .catch(e => {
      console.warn(e);
    });
};
const loadProcessCommentList = () => {
  flowTaskCommentList.value = [];
  if (props.processInstanceId == null || props.processInstanceId === '') {
    return;
  }
  TaskAndRequestController.listFlowTaskComment({
    processInstanceId: props.processInstanceId,
  })
    .then(res => {
      if (res.data) {
        flowTaskCommentList.value = res.data.map(item => {
          return { ...item };
        });
      }
    })
    .catch(e => {
      console.warn(e);
    });
};
const loadAssigneeList = () => {
  if (props.taskId == null) return;
  let params = {
    processDefinitionId: props.processDefinitionId,
    processInstanceId: props.processInstanceId,
    taskId: props.taskId,
    historic: !(props.isRuntime || props.isRuntime === 'true'),
  };

  FlowOperationController.viewTaskUserInfo(params)
    .then(res => {
      assigneeList.value = (res.data || []).map(item => {
        return { ...item };
      });
    })
    .catch(e => {
      console.warn(e);
    });
};

const sortObject = (obj: ANY_OBJECT, sortOrder: ANY_OBJECT) => {
  const sortedKeys = Object.keys(obj).sort((a, b) => {
    return (sortOrder[a] || Infinity) - (sortOrder[b] || Infinity);
  });
  const sortedObj = {};
  sortedKeys.forEach(key => {
    sortedObj[key] = obj[key];
  });

  return sortedObj;
};

const formInit = () => {
  if (['request', 'ccCheck'].includes(props.formType)) {
    const shippingOrder =
      props.requestData && typeof props.requestData === 'string'
        ? JSON.parse(props.requestData)?.shippingOrder
        : {};
    basicInfo.value = sortObject({ ...shippingOrder }, FormInfoSort);
    if (props.formType === 'ccCheck') {
      basicInfo.value.ccOwner = '';
      ccOwner.value = props.ccOwner;
    }
  } else {
    const taskFields = [
      'requestType',
      'requestor',
      'sr',
      'requestDate',
      'orderNumber',
      'status',
      'pendingBy',
      'soldTo',
      'shipTo',
      'stockLocation',
    ];
    basicInfo.value = taskFields.reduce((acc, field) => {
      acc[field] = props[field] || '';
      return acc;
    }, {});
  }
};
const getApprovalUserList = () => {
  TaskAndRequestController.getApprovalUserList({}).then(res => {
    ccOwnerList.value = res.data.filter(item => item.loginName !== layoutStore.currentLoginName);
  });
};

onMounted(() => {
  getApprovalUserList();
  formInit();
  getTaskHighlightData();
  getTaskProcessXml();
  loadProcessCommentList();
  loadAssigneeList();
});
</script>

<style lang="scss" scoped>
.task-title {
  display: flex;
  justify-content: space-between;
  padding-bottom: 6px;
  margin-bottom: 16px;
  border-bottom: 2px solid #e8e8e8;
}
.task-title .text {
  height: 28px;
  line-height: 28px;
  font-weight: 600;
  font-size: 16px;
  color: #383838;
}
.task-title .el-tag {
  margin-left: 10px;
}
.third-party .flow-task {
  overflow: hidden;
}
.page-back-box {
  position: absolute;
  top: 12px;
  right: 24px;
  :deep(.el-button span) {
    display: flex;
    align-items: center;
    img {
      margin-right: 4px;
    }
  }
}
</style>

<style lang="scss">
@import url('../package/theme/index.scss');
.djs-palette {
  background: var(--palette-background-color);
  border: solid 1px var(--palette-border-color) !important;
  border-radius: 2px;
}

.my-process-designer {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  box-sizing: border-box;
  .my-process-designer__header {
    width: 100%;
    min-height: 36px;
    .el-button {
      text-align: center;
    }
    .el-button-group {
      margin: 4px;
    }
    .el-tooltip__popper {
      .el-button {
        width: 100%;
        text-align: left;
        padding-left: 8px;
        padding-right: 8px;
      }
      .el-button:hover {
        background: rgba(64, 158, 255, 0.8);
        color: #ffffff;
      }
    }
    .align {
      position: relative;
      i {
        &:after {
          content: '|';
          position: absolute;
          left: 15px;
          transform: rotate(90deg) translate(200%, 60%);
        }
      }
    }
    .align.align-left i {
      transform: rotate(90deg);
    }
    .align.align-right i {
      transform: rotate(-90deg);
    }
    .align.align-top i {
      transform: rotate(180deg);
    }
    .align.align-bottom i {
      transform: rotate(0deg);
    }
    .align.align-center i {
      transform: rotate(90deg);
      &:after {
        transform: rotate(90deg) translate(0, 60%);
      }
    }
    .align.align-middle i {
      transform: rotate(0deg);
      &:after {
        transform: rotate(90deg) translate(0, 60%);
      }
    }
  }
  .my-process-designer__container {
    display: inline-flex;
    width: 100%;
    flex: 1;
    background-color: #f6f7f9;
    .my-process-designer__canvas {
      flex: 1;
      height: 100%;
      position: relative;
      background: url('data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iNDAiIGhlaWdodD0iNDAiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+PGRlZnM+PHBhdHRlcm4gaWQ9ImEiIHdpZHRoPSI0MCIgaGVpZ2h0PSI0MCIgcGF0dGVyblVuaXRzPSJ1c2VyU3BhY2VPblVzZSI+PHBhdGggZD0iTTAgMTBoNDBNMTAgMHY0ME0wIDIwaDQwTTIwIDB2NDBNMCAzMGg0ME0zMCAwdjQwIiBmaWxsPSJub25lIiBzdHJva2U9IiNlMGUwZTAiIG9wYWNpdHk9Ii4yIi8+PHBhdGggZD0iTTQwIDBIMHY0MCIgZmlsbD0ibm9uZSIgc3Ryb2tlPSIjZTBlMGUwIi8+PC9wYXR0ZXJuPjwvZGVmcz48cmVjdCB3aWR0aD0iMTAwJSIgaGVpZ2h0PSIxMDAlIiBmaWxsPSJ1cmwoI2EpIi8+PC9zdmc+')
        repeat !important;
      div.toggle-mode {
        display: none;
      }
    }
    .my-process-designer__property-panel {
      height: 100%;
      overflow: scroll;
      overflow-y: auto;
      z-index: 10;
      * {
        box-sizing: border-box;
      }
    }
    svg {
      width: 100%;
      height: 100%;
      min-height: 100%;
      overflow: hidden;
    }
  }
}

.djs-palette.open {
  .djs-palette-entries {
    div[class^='bpmn-icon-']:before,
    div[class*='bpmn-icon-']:before {
      line-height: unset;
    }
    div.entry {
      position: relative;
    }
    div.entry:hover {
      &::after {
        width: max-content;
        content: attr(title);
        vertical-align: text-bottom;
        position: absolute;
        right: -10px;
        top: 0;
        bottom: 0;
        overflow: hidden;
        transform: translateX(100%);
        font-size: 0.5em;
        display: inline-block;
        text-decoration: inherit;
        font-variant: normal;
        text-transform: none;
        background: #fafafa;
        box-shadow: 0 0 6px #eeeeee;
        border: 1px solid #cccccc;
        box-sizing: border-box;
        padding: 0 16px;
        border-radius: 4px;
        z-index: 100;
      }
    }
  }
}
pre {
  margin: 0;
  height: 100%;
  overflow: hidden;
  max-height: calc(80vh - 32px);
  overflow-y: auto;
}
.hljs {
  word-break: break-word;
  white-space: pre-wrap;
}
.hljs * {
  font-family: Consolas, Monaco, monospace;
}

.djs-container {
  .djs-visual {
    rect,
    polygon,
    circle {
      stroke: #c1c2c4 !important;
      stroke-width: 1px !important;
    }
    circle[style*='stroke-width: 4px'] {
      stroke: #333333 !important;
    }
    path[style='fill: black; stroke-width: 1px; stroke: black;'] {
      fill: #333333 !important;
      stroke-width: 1px;
      stroke: #333333 !important;
    }
  }
  .djs-visual path {
    stroke: #333333 !important;
  }
  [id^='sequenceflow-end-white-black'] path {
    fill: #333333 !important;
    stroke: #333333 !important;
  }
  [id^='conditional-flow-marker-white-black'] path {
    stroke: #333333 !important;
  }
}
.information-form-title {
  font-size: 20px;
  color: $color-primary;
  padding: 18px 0px 12px 10px;
}
.information-form-table {
  padding: 0px 50px 10px 20px;
}
.task-operation {
  padding: 20px 50px 20px 0px;
}
.basic-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 36px;
  line-height: 36px;
  color: $color-text-secondary;
  font-weight: 400;
  margin: 0px 50px 10px 20px;
  border-bottom: 1px solid #e8eaec;
}
.radio-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start !important;
  border-bottom: 1px solid #e8eaec;
  margin: 0px 50px 5px 20px;
}
.contact-info {
  margin: 15px 50px 10px 10px;
  border-top: 1px solid #e8eaec;
  padding-top: 10px;
  color: $color-text-secondary;
}
.contact-info-title {
  margin-bottom: 15px;
}
.delivery-date-box {
  width: 220px;
  margin: 20px 0px 10px 20px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}
.delivery-date-box-required {
  color: rgb(255, 0, 0);
}
.urgent-form {
  position: relative;
  left: 40px;
}
</style>
