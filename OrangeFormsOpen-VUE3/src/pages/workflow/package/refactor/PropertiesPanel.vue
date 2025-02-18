<template>
  <el-tabs class="properties-panel" v-model="activeName" :style="{ width: `430px` }">
    <el-tab-pane label="Basic Settings" name="baseInfo">
      <template #label>
        <div class="panel-label">Basic Settings</div>
      </template>
      <div class="process-panel__container">
        <div>
          <element-base-info
            :id-edit-disabled="idEditDisabled"
            :business-object="elementBusinessObject"
            :type="elementType"
          />

          <ElementForm
            :id="elementId"
            :type="elementType"
            :tabType="activeName"
            v-if="formVisible && activeName === 'baseInfo'"
            key="form"
          />

          <form-variable :id="elementId" :type="elementType" v-if="formVisible" key="variable" />

          <element-multi-instance
            @isCountersignChange="isCountersignChange"
            :business-object="elementBusinessObject"
            :type="elementType"
            :key="elementBusinessObject.id"
            v-if="
              formVisible &&
              elementType &&
              (elementType.indexOf('Task') !== -1 || elementType === 'SubProcess')
            "
          />
          <service-task
            :id="elementId"
            :type="elementType"
            :tabType="activeName"
            v-if="
              (elementType === 'ServiceTask' || elementType === 'ReceiveTask') &&
              activeName === 'baseInfo'
            "
          />
        </div>

        <div v-if="conditionFormVisible" key="condition">
          <flow-condition :business-object="elementBusinessObject" :type="elementType" />
        </div>

        <div v-if="conditionFormVisible" key="status">
          <div class="panel-title">Work Order Status</div>
          <SetApproveStatus :id="elementId" :type="elementType" />
        </div>
      </div>
    </el-tab-pane>
    <el-tab-pane label="Button Settings" name="button" v-if="formVisible">
      <template #label>
        <div class="panel-label">Button Settings</div>
      </template>
      <div class="process-panel__container">
        <div v-if="formVisible" key="form">
          <div class="panel-title">
            Buttons
            <right-add-btn @click.prevent.stop="elementForm.onEditOperation()">Add</right-add-btn>
          </div>
          <ElementForm
            v-if="activeName === 'button'"
            :id="elementId"
            :type="elementType"
            :tabType="activeName"
            ref="elementForm"
          />
        </div>

        <!-- <div v-if="formVisible">
          <AutoAgree :id="elementId" :isCountersign="isCountersign" />
        </div> -->
      </div>
    </el-tab-pane>
    <el-tab-pane label="Task Handlers" name="taskHandler" v-if="formVisible">
      <template #label>
        <div class="panel-label">Task Handlers</div>
      </template>
      <div class="process-panel__container">
        <div
          v-if="elementType && (elementType.indexOf('Task') !== -1 || elementType === 'SubProcess')"
          key="task"
        >
          <element-task :id="elementId" :type="elementType" :isCountersign="isCountersign" />
        </div>
      </div>
    </el-tab-pane>
    <el-tab-pane label="Other Settings" name="other">
      <template #label>
        <div class="panel-label">Other Settings</div>
      </template>
      <div class="process-panel__container">
        <div key="message" v-if="elementType === 'Process'">
          <signal-and-massage />
        </div>

        <div key="listeners" v-if="elementType" style="margin-bottom: 10px">
          <div class="panel-title">
            Execution Listeners
            <right-add-btn @click.prevent.stop="listeners.openListenerForm()">Add</right-add-btn>
          </div>
          <element-listeners ref="listeners" :id="elementId" :type="elementType" />
        </div>

        <div key="taskListeners" v-if="elementType === 'UserTask'" style="margin-bottom: 10px">
          <div class="panel-title">
            Task Listeners
            <right-add-btn @click.prevent.stop="useTaskListeners.openListenerForm()"
              >Add</right-add-btn
            >
          </div>
          <user-task-listeners :id="elementId" :type="elementType" ref="useTaskListeners" />
        </div>

        <div key="extensions" v-if="elementType && !conditionFormVisible">
          <div class="panel-title">
            Extended Attributes
            <right-add-btn @click.prevent.stop="properties.openAttributesForm(null, -1)"
              >Add</right-add-btn
            >
          </div>
          <element-properties :id="elementId" :type="elementType" ref="properties" />
        </div>
      </div>
    </el-tab-pane>
  </el-tabs>
</template>

<script setup lang="ts">
/**
 * Sidebar
 * @Author MiyueFE
 * @Home https://github.com/miyuesc
 * @Date March 31, 2021
 */
import { ANY_OBJECT } from '@/types/generic';
import RightAddBtn from '@/components/Btns/RightAddBtn.vue';
import ElementBaseInfo from './base/ElementBaseInfo.vue';
import ElementTask from './task/ElementTask.vue';
import ElementMultiInstance from './multi-instance/ElementMultiInstance.vue';
import FlowCondition from './flow-condition/FlowCondition.vue';
import SignalAndMassage from './signal-message/SignalAndMessage.vue';
import ElementListeners from './listeners/ElementListeners.vue';
import ElementProperties from './properties/ElementProperties.vue';
import SetApproveStatus from './properties/SetApproveStatus.vue';
import ElementForm from './form/flowFormConfig.vue';
import ServiceTask from './form/ServiceTask';
import FormVariable from './form-variable/index.vue';
import CopyForSelect from './copy-for/index.vue';
import UserTaskListeners from './listeners/UserTaskListeners.vue';
import AutoAgree from './autoAgree/index.vue';
const props = withDefaults(
  defineProps<{
    bpmnModeler: ANY_OBJECT;
    prefix?: string;
    width?: number;
    idEditDisabled?: boolean;
    autoTaskVariableList: ANY_OBJECT[];
    flowVariableList: ANY_OBJECT[];
  }>(),
  {
    prefix: 'camunda',
    width: 480,
    idEditDisabled: false,
    autoTaskVariableList: () => [],
    flowVariableList: () => [],
  },
);

provide('prefix', props.prefix);
provide('width', props.width);
provide('flowVariableList', () => props.flowVariableList);
provide('autoTaskVariableList', () => props.autoTaskVariableList);

// refs
const elementForm = ref();
const listeners = ref();
const useTaskListeners = ref();
const properties = ref();

const activeName = ref('baseInfo');
const elementId = ref('');
const elementType = ref('');
// Element businessObject mirror, provided for components that need to make judgments
const elementBusinessObject = ref<ANY_OBJECT>({});
// Transition condition settings
const conditionFormVisible = ref(false);
// Form configuration
const formVisible = ref(false);
const isCountersign = ref(false);

watch(
  formVisible,
  val => {
    if (!val && ['baseInfo', 'other'].indexOf(activeName.value) === -1) {
      activeName.value = 'baseInfo';
    }
  },
  {
    deep: true,
  },
);

const win: ANY_OBJECT = window;
let timer: number | null = null;
const initModels = () => {
  // Initialize modeler and other moddle
  if (!props.bpmnModeler) {
    // Avoid loading when the process diagram has not been loaded completely
    timer = setTimeout(() => initModels(), 10);
    return;
  }
  if (timer) clearTimeout(timer);
  win.bpmnInstances = {
    modeler: props.bpmnModeler,
    modeling: props.bpmnModeler.get('modeling'),
    moddle: props.bpmnModeler.get('moddle'),
    eventBus: props.bpmnModeler.get('eventBus'),
    bpmnFactory: props.bpmnModeler.get('bpmnFactory'),
    elementRegistry: props.bpmnModeler.get('elementRegistry'),
    replace: props.bpmnModeler.get('replace'),
    selection: props.bpmnModeler.get('selection'),
  };
  getActiveElement();
};
const getActiveElement = () => {
  // Initial first selected element bpmn:Process
  initFormOnChanged(null);
  props.bpmnModeler.on('import.done', () => {
    initFormOnChanged(null);
  });
  // Listen for selection events to modify the currently activated element and form
  props.bpmnModeler.on('selection.changed', ({ newSelection }: { newSelection: ANY_OBJECT[] }) => {
    initFormOnChanged(newSelection[0] || null);
  });
  props.bpmnModeler.on('element.changed', ({ element }: { element: ANY_OBJECT }) => {
    // Ensure that when modifying multiple elements like "default transition path",
    // the updated element's form is consistent with the original selected element.
    if (element && element.id === elementId.value) {
      initFormOnChanged(element);
    }
  });
};
// Initialize data
const initFormOnChanged = (element: ANY_OBJECT | null) => {
  let activatedElement = element;
  if (!activatedElement) {
    activatedElement =
      win.bpmnInstances.elementRegistry.find((el: ANY_OBJECT) => el.type === 'bpmn:Process') ??
      win.bpmnInstances.elementRegistry.find((el: ANY_OBJECT) => el.type === 'bpmn:Collaboration');
  }
  if (!activatedElement) return;

  win.bpmnInstances.bpmnElement = activatedElement;
  elementId.value = activatedElement.id;
  elementType.value = activatedElement.type.split(':')[1];
  elementBusinessObject.value = JSON.parse(JSON.stringify(activatedElement.businessObject));
  conditionFormVisible.value = !!(
    elementType.value === 'SequenceFlow' &&
    activatedElement.source &&
    activatedElement.source.type.indexOf('StartEvent') === -1
  );
  formVisible.value = elementType.value === 'UserTask';
};
const isCountersignChange = (bol: boolean) => {
  isCountersign.value = bol;
};

onMounted(() => {
  initModels();
});
onBeforeUnmount(() => {
  win.bpmnInstances = null;
});
</script>

<style lang="scss">
.properties-panel.el-tabs {
  border-left: 1px solid #e8e8e8;
  .el-tabs__nav-wrap::after {
    height: 1px !important;
  }
  .el-tabs__item {
    height: 50px;
    padding: 0 18px !important;
    text-align: center;
    color: #333;
    line-height: 50px;
    &.is-top:nth-child(2) {
      padding-left: 16px !important;
    }
  }
  .el-tabs__header {
    margin-bottom: 15px !important;
  }
  .el-tabs__content {
    overflow: auto;
    width: 430px;
    height: calc(100% - 60px);
  }
  .el-tab-pane,
  .process-panel__container {
    width: 100%;
    min-height: 100%;
    box-shadow: none;
    box-sizing: border-box;
    border-left: 0;
    .el-form-item {
      margin-bottom: 10px;
    }
  }
  .process-panel__container {
    padding: 0 18px;
  }
  .panel-tab__content {
    border-top: 0;
    padding: 10px 0 0;
  }
  .is-active {
    font-weight: bold;
    color: $color-primary;
  }

  .el-form-item__label {
    font-size: 14px;
    color: #333;
    width: 100%;
    text-align: left;
  }

  .el-button {
    font-weight: normal;
  }
}
.panel-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #333;
  line-height: 32px;
}
.panel-label {
  padding: 0 2px;
  font-size: 14px;
}
.process-panel__container div:first-child .panel-title {
  margin-top: 0;
}
.el-table th {
  font-size: 14px;
  color: #666 !important;
  background-color: #f6f7f9 !important;
  font-weight: normal;
}
</style>
