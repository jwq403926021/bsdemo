<template>
  <div>
    <el-form
      v-if="tabType === 'baseInfo'"
      ref="form"
      :size="layoutStore.defaultFormItemSize"
      :model="formData"
      :rules="rules"
      @submit.prevent
      label-position="top"
    >
      <el-form-item label="Form Route" prop="routerName">
        <el-select v-model="onlinePage" clearable placeholder="Form Route">
          <el-option
            v-for="item in onlinePageList"
            :key="item.pageName"
            :label="item.pageName"
            :value="item.pageCode"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="Online Form"
        prop="formId"
        v-if="flowEntryComputed.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM"
      >
        <el-select
          v-model="formData.formId"
          clearable
          placeholder=""
          @change="updateElementFormKey"
        >
          <el-option
            v-for="item in formList().filter((item:ANY_OBJECT) => item.formType === SysOnlineFormType.FLOW)"
            :key="item.formId"
            :label="item.formName"
            :value="item.formId"
          />
        </el-select>
      </el-form-item>
      <el-row>
        <el-col :span="12">
          <el-form-item label="Editable" style="margin-bottom: 4px">
            <template #label>
              <span>Editable</span>
              <el-switch
                v-model="formData.editable"
                @change="updateElementFormKey"
                style="margin-left: 18px"
              />
            </template>
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item
            v-if="flowEntryComputed.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM"
          >
            <template #label>
              <span>Form Permission Settings</span>
              <el-button
                type="primary"
                size="default"
                style="margin-left: 18px"
                @click="onSetOnlineFormAuth"
                >Set</el-button
              >
            </template>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row v-else-if="tabType === 'button'" style="padding-top: 3px">
      <!-- <el-col :span="24">
        <el-divider>Button Settings</el-divider>
      </el-col> -->
      <el-col :span="24">
        <vxe-table
          empty-text="No data"
          :data="formOperationList"
          :row-config="{ isHover: true }"
          header-cell-class-name="table-header-gray"
        >
          <vxe-column title="No." width="50px" type="seq" />
          <vxe-column title="Button Name" min-width="100px">
            <template v-slot="scope">
              <span>{{ scope.row.label }}</span>
            </template>
          </vxe-column>
          <vxe-column title="Button Type" min-width="100px">
            <template v-slot="scope">
              <el-tag :size="layoutStore.defaultFormItemSize" effect="dark">{{
                SysFlowTaskOperationType.getValue(scope.row.type)
              }}</el-tag>
            </template>
          </vxe-column>
          <!-- <vxe-column title="Display Order" min-width="100px" prop="showOrder"></vxe-column> -->
          <vxe-column title="Operation" width="110px">
            <template v-slot="scope">
              <el-button
                :size="layoutStore.defaultFormItemSize"
                link
                type="primary"
                @click="onEditOperation(scope.row)"
                style="padding: 0"
                >Edit</el-button
              >
              <el-button
                :size="layoutStore.defaultFormItemSize"
                link
                type="danger"
                @click="onDeleteOperation(scope.row)"
                style="padding: 0"
                >Delete</el-button
              >
            </template>
          </vxe-column>
          <template v-slot:empty>
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>No Data</span>
            </div>
          </template>
        </vxe-table>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { getUUID } from '@/common/utils/index';
import { ElMessageBox, ElMessage } from 'element-plus';
import { VxeTable, VxeColumn } from 'vxe-table';
import { ANY_OBJECT } from '@/types/generic';
import { Dialog } from '@/components/Dialog';
import { SysOnlineFormType } from '@/common/staticDict/index';
import { SysFlowEntryBindFormType, SysFlowTaskOperationType } from '@/common/staticDict/flow';
import EditOperation from './formEditOperation.vue';
import FormSetOnlineFormAuth from './formSetOnlineFormAuth.vue';
import { useLayoutStore } from '@/store';
import { OnlinePageController } from '@/api/online';

const layoutStore = useLayoutStore();

const props = defineProps<{ id: string; type: string; tabType: string }>();
const flowEntry = inject('flowEntry', () => {
  return {} as ANY_OBJECT;
});
const formList = inject('formList', () => {
  return {} as ANY_OBJECT;
});
const prefix = inject('prefix');

const win: ANY_OBJECT = window;

const form = ref();
const formData = ref<ANY_OBJECT>({
  formId: flowEntry().value.defaultFormId,
  routerName: flowEntry().value.defaultRouterName,
  formAuth: {},
  editable: false,
});
const operationList = ref<ANY_OBJECT>({});
const onlinePage = ref<string>('');
const onlinePageList = ref<ANY_OBJECT[]>([]);
const formOperationList = ref<ANY_OBJECT[]>([]);
const rules = {
  formId: [{ required: true, message: 'Please select an online form!', trigger: 'blur' }],
  // routerName: [{ required: true, message: 'Form route cannot be empty!', trigger: 'blur' }],
};

let bpmnElement: ANY_OBJECT = {};

const flowEntryComputed = computed(() => {
  console.log('flowEntryComputed', flowEntry().value);
  return flowEntry().value;
});
const getFlowEntryValidStatus = computed(() => {
  if (
    flowEntry().value.extensionData &&
    Array.isArray(flowEntry().value.extensionData.approvalStatusDict)
  ) {
    return flowEntry().value.extensionData.approvalStatusDict;
  } else {
    return [];
  }
});

const currentForm = computed(() => {
  return formList().find((item: ANY_OBJECT) => item.formId === formData.value.formId);
});

const formatOnlineFormInfo = formInfo => {
  if (formInfo == null) return null;
  let children;
  if (Array.isArray(formInfo.childWidgetList) && formInfo.childWidgetList.length > 0) {
    children = formInfo.childWidgetList.map(subWidget => formatOnlineFormInfo(subWidget));
  }
  return {
    formId: getUUID(),
    showName: formInfo.showName,
    variableName: formInfo.variableName,
    widgetType: formInfo.widgetType,
    children: children,
  };
};

const onSetOnlineFormAuth = () => {
  if (currentForm.value == null) {
    ElMessage.error('Please select a form first!');
    return;
  }
  let formWidgetConfig = JSON.parse(currentForm.value.widgetJson);
  let tempConfig = {
    pc: {
      widgetList: [],
    },
    mobile: {
      widgetList: [],
    },
  };
  if (formWidgetConfig != null) {
    if (
      formWidgetConfig.pc &&
      Array.isArray(formWidgetConfig.pc.widgetList) &&
      formWidgetConfig.pc.widgetList.length > 0
    ) {
      tempConfig.pc = {
        widgetList: formWidgetConfig.pc.widgetList.map(subWidget =>
          formatOnlineFormInfo(subWidget),
        ),
      };
    }
    if (
      formWidgetConfig.mobile &&
      Array.isArray(formWidgetConfig.mobile.widgetList) &&
      formWidgetConfig.mobile.widgetList.length > 0
    ) {
      tempConfig.mobile = {
        widgetList: formWidgetConfig.mobile.widgetList.map(subWidget =>
          formatOnlineFormInfo(subWidget),
        ),
      };
    }
  }
  console.log(tempConfig);
  Dialog.show<ANY_OBJECT>(
    'Set Form Permissions',
    FormSetOnlineFormAuth,
    {
      area: ['1000px', '700px'],
    },
    {
      formAuth: formData.value.formAuth || {},
      formWidgetConfig: tempConfig,
      path: 'thirdSetOnlineFormAuth',
    },
    {
      width: '1000px',
      height: '700px',
      pathName: '/thirdParty/formSetOnlineFormAuth',
    },
  )
    .then(res => {
      formData.value.formAuth = res;
      updateElementFormKey();
    })
    .catch(e => {
      console.warn(e);
    });
};
const getOnlinePageList = () => {
  OnlinePageController.list({
    onlinePageDtoFilter: {},
  }).then(res => {
    onlinePageList.value = res.data.dataList.filter(item => item.published);
  });
};

const resetFormList = () => {
  onlinePage.value = '';
  bpmnElement = win.bpmnInstances.bpmnElement;
  let formKey = bpmnElement.businessObject.formKey;
  let formObj = formKey ? JSON.parse(formKey) : undefined;
  if (formObj) {
    formData.value = {
      formId: formObj.formId,
      routerName: formObj.routerName,
      formAuth: formObj.formAuth || {},
      editable: !formObj.readOnly,
      groupType: formObj.groupType || 'ASSIGNEE',
    };
  }
  let elExtensionElements =
    bpmnElement.businessObject.get('extensionElements') ||
    win.bpmnInstances.moddle.create('bpmn:ExtensionElements', { values: [] });
  operationList.value =
    elExtensionElements.values.filter(
      (ex: ANY_OBJECT) => ex.$type === `${prefix}:OperationList`,
    )?.[0] || win.bpmnInstances.moddle.create(`${prefix}:OperationList`, { operationList: [] });
  formOperationList.value = JSON.parse(JSON.stringify(operationList.value.operationList || []));
  formOperationList.value.forEach(item => {
    item.showOrder = Number.parseInt(item.showOrder);
  });
  // Update element extension properties to avoid subsequent errors
  updateElementFormKey();
  updateElementExtensions();
  getOnlinePageList();
};
const updateElementFormKey = () => {
  console.log('updateElementFormKey');
  form.value?.validate((valid: boolean) => {
    if (!valid) return;
    let formKeyString = JSON.stringify({
      formId:
        flowEntry().value.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM
          ? formData.value.formId
          : undefined,
      routerName:
        flowEntry().value.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM
          ? undefined
          : formData.value.routerName,
      formAuth: formData.value.formAuth,
      readOnly: !formData.value.editable,
      groupType: formData.value.groupType || 'ASSIGNEE',
    });
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      formKey: formKeyString,
    });
  });
};
const updateElementExtensions = () => {
  // Update additional extensions
  let elExtensionElements =
    bpmnElement.businessObject.get('extensionElements') ||
    win.bpmnInstances.moddle.create('bpmn:ExtensionElements', { values: [] });
  let otherExtensions = elExtensionElements.values.filter(
    (ex: ANY_OBJECT) => ex.$type !== `${prefix}:OperationList`,
  );
  const newElExtensionElements = win.bpmnInstances.moddle.create(`bpmn:ExtensionElements`, {
    values: otherExtensions.concat(operationList.value),
  });
  operationList.value.operationList = formOperationList.value.map((item: ANY_OBJECT) => {
    return win.bpmnInstances.moddle.create(`${prefix}:FormOperation`, {
      id: item.id,
      label: item.label,
      type: item.type,
      showOrder: item.showOrder,
      latestApprovalStatus: item.latestApprovalStatus,
      multiSignAssignee: item.multiSignAssignee,
    });
  });

  // Update to the element
  win.bpmnInstances.modeling.updateProperties(bpmnElement, {
    extensionElements: newElExtensionElements,
  });
};
const onEditOperation = (operation: ANY_OBJECT) => {
  Dialog.show<ANY_OBJECT>(
    operation ? 'Edit Button' : 'Add Button',
    EditOperation,
    {
      area: '500px',
    },
    {
      rowData: operation,
      validStatusList: getFlowEntryValidStatus.value,
      path: 'thirdEditOperation',
    },
    {
      width: '500px',
      height: '450px',
      pathName: '/thirdParty/thirdEditOperation',
    },
  )
    .then(res => {
      updateEditOperation(res);
    })
    .catch(e => {
      console.warn(e);
    });
};
const updateEditOperation = (res: ANY_OBJECT) => {
  //console.log('>>> updateEditOperation', res);
  if (res.id == null) {
    res.id = new Date().getTime();
    formOperationList.value.push(res);
  } else {
    formOperationList.value.forEach(item => {
      if (item.id === res.id) {
        item.label = res.label;
        item.type = res.type;
        item.showOrder = res.showOrder;
        item.multiSignAssignee = res.multiSignAssignee;
        item.latestApprovalStatus = res.latestApprovalStatus;
      }
    });
  }
  formOperationList.value.sort((value1: ANY_OBJECT, value2: ANY_OBJECT) => {
    return value1.showOrder - value2.showOrder;
  });
  updateElementExtensions();
};
const onDeleteOperation = (operation: ANY_OBJECT) => {
  ElMessageBox.confirm('Are you sure you want to delete this button?', 'Prompt', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      formOperationList.value = formOperationList.value.filter(item => {
        return item.id !== operation.id;
      });
      updateElementExtensions();
    })
    .catch(e => {
      console.warn(e);
    });
};

watch(
  () => props.id,
  val => {
    val && val.length && nextTick(() => resetFormList());
  },
  {
    immediate: true,
  },
);

defineExpose({ onEditOperation });
</script>

<style lang="scss" scoped>
.full-line-btn {
  width: 100%;
  margin-top: 10px;
  border: 1px dashed #ebeef5;
}
.el-tag {
  color: #ffb800;
  background: #fff8e5;
  border-color: #fff8e5;
}
:deep(.table-btn) {
  color: #999;
}
</style>
