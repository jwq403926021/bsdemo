<template>
  <div class="panel-tab__content" style="padding-top: 0">
    <el-form :size="layoutStore.defaultFormItemSize" @submit.prevent label-position="top">
      <el-form-item label="Task Notification">
        <el-select
          v-model="sendMessageType"
          multiple
          clearable
          @change="updateSendMessageType()"
          placeholder=""
        >
          <el-option label="Email" value="email" />
        </el-select>
      </el-form-item>
    </el-form>
    <el-dialog
      title="Built-in Variables"
      v-model="showVariableDlg"
      width="800px"
      append-to-body
      destroy-on-close
      class="panel-dialog"
    >
      <div style="padding: 16px 20px">
        <vxe-table
          empty-text="No data"
          :data="variableList"
          :size="layoutStore.defaultFormItemSize"
          :row-config="{ isHover: true }"
        >
          <vxe-column title="Variable Name" field="name" width="180px" />
          <vxe-column title="Description" field="desc" />
          <template v-slot:empty>
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>No Data</span>
            </div>
          </template>
        </vxe-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue';
import { VxeTable, VxeColumn } from 'vxe-table';
import TaskUserSelect from '@/pages/workflow/components/TaskUserSelect.vue';
import { SysCommonBizController } from '@/api/system';
import { ANY_OBJECT } from '@/types/generic';
import { Dialog } from '@/components/Dialog';

const props = defineProps<{ businessObject: ANY_OBJECT; type: string }>();
console.log('ElementMultiInstance props', props);

import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const prefix = inject('prefix');

const win: ANY_OBJECT = window;
const isCountersign = ref(true);
const showVariableDlg = ref(false);
const loopCharacteristics = ref('ParallelMultiInstance');
const sendMessageType = ref<ANY_OBJECT[]>([]);
const timeoutHandleWay = ref<string>();
const timeoutHours = ref<number | string>();
const defaultAssignee = ref<string>();
const defaultAssigneeShowName = ref<string>();
const emptyUserHandleWay = ref('autoComplete');
const emptyUserToAssignee = ref<string>();
const emptyUserToAssigneeShowName = ref<string>();
// Default configuration to override original non-existent options to avoid errors
const defaultLoopInstanceForm = {
  completionCondition: '',
  loopCardinality: '',
  extensionElements: [],
  asyncAfter: false,
  asyncBefore: false,
  exclusive: false,
};
const loopInstanceForm = ref<ANY_OBJECT>({
  collection: 'assigneeList',
  elementVariable: 'assignee',
  completionCondition: '${nrOfInstances == nrOfCompletedInstances}',
});
const variableList = [
  {
    name: 'assigneeList',
    desc: 'Multi-instance user collection variable',
  },
  {
    name: 'nrOfInstances',
    desc: 'Total number of instances',
  },
  {
    name: 'nrOfActiveInstances',
    desc: 'Number of currently active (i.e., not completed) instances. This value is always 1 for sequential multi-instances',
  },
  {
    name: 'nrOfCompletedInstances',
    desc: 'Number of completed instances',
  },
  {
    name: 'multiNumOfInstances',
    desc: 'Total number of instances (used after multi-instance node for judgment)',
  },
  {
    name: 'multiAgreeCount',
    desc: 'Number of agreed instances',
  },
  {
    name: 'multiRefuseCount',
    desc: 'Number of refused instances',
  },
  {
    name: 'multiAbstainCount',
    desc: 'Number of abstained instances',
  },
];

let bpmnElement: ANY_OBJECT | null = {};
let multiLoopInstance: ANY_OBJECT | null = {};

const loadUserInfo = (val: string, callback: (res: string | undefined) => void | null) => {
  let params = {
    widgetType: 'upms_user',
    fieldName: 'loginName',
    fieldValues: val,
  };
  SysCommonBizController.viewByIds(params)
    .then(res => {
      console.log(res);
      if (Array.isArray(res.data) && res.data.length > 0) {
        if (callback) callback(res.data[0].showName || res.data[0].loginName);
      } else {
        if (callback) callback(undefined);
      }
    })
    .catch(e => {
      console.log(e);
    });
};
const onSelectAssignee = (callback: (res: ANY_OBJECT) => void) => {
  Dialog.show<ANY_OBJECT>(
    'Select User',
    TaskUserSelect,
    {
      area: ['1000px', '650px'],
    },
    {
      multiple: false,
      showAssignee: false,
      path: 'thirdTaskUserSelect',
    },
    {
      width: '1000px',
      height: '650px',
      pathName: '/thirdParty/thirdTaskUserSelect',
    },
  )
    .then(res => {
      console.log('onSelectAssignee >>>', res);
      callback(res);
    })
    .catch(e => {
      console.error(e);
    });
};
const getElementLoop = (businessObject: ANY_OBJECT) => {
  let value = (win.bpmnInstances.bpmnElement || {}).businessObject?.sendMessageType;
  sendMessageType.value = value != null && value !== '' ? value.split(',') : [];
  timeoutHandleWay.value = (win.bpmnInstances.bpmnElement || {}).businessObject?.timeoutHandleWay;
  timeoutHours.value = (win.bpmnInstances.bpmnElement || {}).businessObject?.timeoutHours;
  defaultAssignee.value = (win.bpmnInstances.bpmnElement || {}).businessObject?.defaultAssignee;
  emptyUserHandleWay.value =
    (win.bpmnInstances.bpmnElement || {}).businessObject.emptyUserHandleWay || 'autoComplete';
  emptyUserToAssignee.value = (
    win.bpmnInstances.bpmnElement || {}
  ).businessObject.emptyUserToAssignee;

  emit('isCountersignChange', !!businessObject.loopCharacteristics);

  if (!businessObject.loopCharacteristics) {
    loopCharacteristics.value = 'Null';
    loopInstanceForm.value = {};
    isCountersign.value = false;
    return;
  }
  if (businessObject.loopCharacteristics.$type === 'bpmn:StandardLoopCharacteristics') {
    loopCharacteristics.value = 'StandardLoop';
    loopInstanceForm.value = {};
    return;
  }
  if (businessObject.loopCharacteristics.isSequential) {
    loopCharacteristics.value = 'SequentialMultiInstance';
  } else {
    loopCharacteristics.value = 'ParallelMultiInstance';
  }
  // Merge configuration
  loopInstanceForm.value = {
    ...defaultLoopInstanceForm,
    ...businessObject.loopCharacteristics,
    completionCondition:
      businessObject.loopCharacteristics?.completionCondition?.body ??
      '${nrOfInstances == nrOfCompletedInstances}',
    loopCardinality: businessObject.loopCharacteristics?.loopCardinality?.body ?? '',
  };
  // Retain current element businessObject's loopCharacteristics instance
  multiLoopInstance = win.bpmnInstances.bpmnElement.businessObject.loopCharacteristics;
  // Update form
  if (
    businessObject.loopCharacteristics.extensionElements &&
    businessObject.loopCharacteristics.extensionElements.values &&
    businessObject.loopCharacteristics.extensionElements.values.length
  ) {
    const val = { ...loopInstanceForm.value };
    val.timeCycle = businessObject.loopCharacteristics.extensionElements.values[0].body;
    loopInstanceForm.value = val;
  }
};
const changeLoopCharacteristicsType = (type: string) => {
  if (type === 'SequentialMultiInstance' || loopInstanceForm.value.completionCondition === '') {
    loopInstanceForm.value.completionCondition = '${nrOfInstances == nrOfCompletedInstances}';
  }
  // Cancel multi-instance configuration
  if (type === 'Null') {
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      loopCharacteristics: null,
    });
    return;
  }
  // Configure loop
  if (type === 'StandardLoop') {
    const loopCharacteristicsObject = win.bpmnInstances.moddle.create(
      'bpmn:StandardLoopCharacteristics',
    );
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      loopCharacteristics: loopCharacteristicsObject,
    });
    multiLoopInstance = null;
    return;
  }
  // Sequential
  if (type === 'SequentialMultiInstance') {
    multiLoopInstance = win.bpmnInstances.moddle.create('bpmn:MultiInstanceLoopCharacteristics', {
      isSequential: true,
    });
  } else {
    multiLoopInstance = win.bpmnInstances.moddle.create('bpmn:MultiInstanceLoopCharacteristics');
  }

  win.bpmnInstances.modeling.updateProperties(bpmnElement, {
    loopCharacteristics: multiLoopInstance,
  });

  updateLoopBase();
  updateLoopCondition(loopInstanceForm.value.completionCondition);
};
// Loop cardinality

// Completion condition
const updateLoopCondition = (condition: string) => {
  let completionCondition = null;
  if (condition && condition.length) {
    completionCondition = win.bpmnInstances.moddle.create('bpmn:FormalExpression', {
      body: condition,
    });
  }
  win.bpmnInstances.modeling.updateModdleProperties(bpmnElement, multiLoopInstance, {
    completionCondition,
  });
};
// Retry cycle
const updateLoopTimeCycle = (timeCycle: string) => {
  const extensionElements = win.bpmnInstances.moddle.create('bpmn:ExtensionElements', {
    values: [
      win.bpmnInstances.moddle.create(`${prefix}:FailedJobRetryTimeCycle`, {
        body: timeCycle,
      }),
    ],
  });
  win.bpmnInstances.modeling.updateModdleProperties(bpmnElement, multiLoopInstance, {
    extensionElements,
  });
};
// Directly update base information
const updateLoopBase = () => {
  win.bpmnInstances.modeling.updateModdleProperties(bpmnElement, multiLoopInstance, {
    collection: loopInstanceForm.value.collection || null,
    elementVariable: loopInstanceForm.value.elementVariable || null,
  });
};
// Various asynchronous states

const isCountersignChange = (bol: string | number | boolean) => {
  if (!bol) {
    loopCharacteristics.value = 'Null';
    loopInstanceForm.value = {};
    changeLoopCharacteristicsType('Null');
    win.bpmnInstances.modeling.updateProperties(win.bpmnInstances.bpmnElement, {
      assignee: undefined,
    });
  } else {
    loopCharacteristics.value = 'ParallelMultiInstance';
    loopInstanceForm.value = {
      collection: 'assigneeList',
      elementVariable: 'assignee',
    };
    changeLoopCharacteristicsType('ParallelMultiInstance');
  }
};
const updateSendMessageType = () => {
  let taskAttr = Object.create(null);
  taskAttr.sendMessageType = sendMessageType.value.join(',');
  win.bpmnInstances.modeling.updateProperties(win.bpmnInstances.bpmnElement, taskAttr);
};
const onTimeoutChange = () => {
  let taskAttr = Object.create(null);
  if (!timeoutHandleWay.value) {
    timeoutHandleWay.value = undefined;
    timeoutHours.value = undefined;
  } else {
    if (timeoutHours.value == null) timeoutHours.value = 24;
  }
  // Only automatic approval can set the default assignee
  if (timeoutHandleWay.value !== 'autoComplete') {
    defaultAssignee.value = undefined;
  }
  taskAttr.timeoutHandleWay = timeoutHandleWay.value;
  taskAttr.timeoutHours = timeoutHours.value;
  taskAttr.defaultAssignee = defaultAssignee.value;
  // TODO Call this method causes the interface not to update, possibly because values passed are incorrect
  win.bpmnInstances.modeling.updateProperties(win.bpmnInstances.bpmnElement, taskAttr);
  console.log('ElementMultiInstance onTimeoutChange', win.bpmnInstances.bpmnElement, taskAttr);
};
const onTimeoutAssigneeChang = (res: ANY_OBJECT) => {
  if (res) {
    defaultAssignee.value = res.loginName;
    defaultAssigneeShowName.value = res.showName;
  }
  onTimeoutChange();
};
const onEmptyUserChange = (res: ANY_OBJECT) => {
  if (res) {
    emptyUserToAssignee.value = res.loginName;
    emptyUserToAssigneeShowName.value = res.showName;
  }
  let taskAttr = Object.create(null);
  if (emptyUserHandleWay.value !== 'toAssignee') {
    emptyUserToAssignee.value = undefined;
  }
  taskAttr.emptyUserHandleWay = emptyUserHandleWay.value;
  taskAttr.emptyUserToAssignee = emptyUserToAssignee.value;
  // TODO
  win.bpmnInstances.modeling.updateProperties(win.bpmnInstances.bpmnElement, taskAttr);
  console.log('ElementMultiInstance onEmptyUserChange', win.bpmnInstances.bpmnElement, taskAttr);
};

onBeforeUnmount(() => {
  multiLoopInstance = null;
  bpmnElement = null;
});

const emit = defineEmits<{ isCountersignChange: [boolean] }>();

watch(
  () => props.type,
  () => {
    if (props.businessObject) {
      getElementLoop(props.businessObject);
    }
  },
);
watch(
  () => props.businessObject,
  val => {
    bpmnElement = win.bpmnInstances.bpmnElement;
    getElementLoop(val);
  },
  {
    immediate: true,
  },
);
watch(
  isCountersign,
  val => {
    emit('isCountersignChange', val);
  },
  {
    immediate: true,
  },
);
watch(
  emptyUserToAssignee,
  val => {
    if (val == null || val === '') {
      emptyUserToAssigneeShowName.value = undefined;
    } else if (val === '${startUserName}') {
      emptyUserToAssigneeShowName.value = 'Process Initiator';
    } else if (val === '${appointedAssignee}') {
      emptyUserToAssigneeShowName.value = 'Designated Approver';
    } else {
      if (emptyUserToAssigneeShowName.value == null || emptyUserToAssigneeShowName.value === '') {
        loadUserInfo(val, showName => {
          emptyUserToAssigneeShowName.value = showName;
        });
      }
    }
  },
  {
    immediate: true,
  },
);
watch(
  defaultAssignee,
  val => {
    if (val == null || val === '') {
      defaultAssigneeShowName.value = undefined;
    } else if (val === '${startUserName}') {
      defaultAssigneeShowName.value = 'Process Initiator';
    } else {
      if (defaultAssigneeShowName.value == null || defaultAssigneeShowName.value === '') {
        loadUserInfo(val, showName => {
          defaultAssigneeShowName.value = showName;
        });
      }
    }
  },
  {
    immediate: true,
  },
);
</script>

<style lang="scss">
.panel-dialog .el-dialog__body {
  overflow: hidden;
  padding: 0 1px;
}
.variables-btn {
  width: 64px;
  height: 24px;
  padding: 0 !important;
  margin-left: 18px !important;
  font-size: 12px !important;
  color: $color-primary !important;

  /* float: right; */
  background-color: #fff !important;
  border-color: $color-primary !important;
  line-height: 24px !important;
}
.variables-btn:hover {
  color: $color-primary !important;
  opacity: 0.8;
}
</style>

<style scoped>
.input-with-select {
  width: 88%;
}
.input-with-select :deep(.el-input-group__prepend) {
  padding: 0;
  background-color: #fff;
  width: 120px;
}
.input-with-select :deep(.el-select .el-input__inner) {
  cursor: pointer !important;
}

.empty-user .el-radio {
  margin-bottom: 8px;
}
.el-input :deep(.el-input-group__prepend .el-input__inner) {
  color: #606266;
}
.el-input :deep(.el-input-group__prepend .el-input__inner:hover) {
  color: #606266;
}
</style>
