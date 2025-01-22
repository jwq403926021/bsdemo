<template>
  <div class="panel-tab__content" style="padding-top: 6px">
    <el-form
      :model="flowConditionForm"
      :size="layoutStore.defaultFormItemSize"
      @submit.prevent
      label-position="top"
    >
      <el-form-item label="Transfer Type">
        <el-select v-model="flowConditionForm.type" @change="updateFlowType">
          <el-option label="Normal Transfer Path" value="normal" />
          <el-option label="Default Transfer Path" value="default" />
          <el-option label="Built-in Button" value="operation" />
          <el-option label="Conditional Transfer Path" value="condition" />
        </el-select>
      </el-form-item>
      <el-form-item
        label="Conditional Format"
        v-if="flowConditionForm.type === 'condition'"
        key="condition"
      >
        <el-select v-model="flowConditionForm.conditionType">
          <el-option label="Expression" value="expression" />
          <el-option label="Script" value="script" />
        </el-select>
      </el-form-item>
      <el-form-item
        label="Button Type"
        v-if="flowConditionForm.type === 'operation'"
        key="operation"
      >
        <el-select v-model="flowConditionForm.operationType" @change="onOperationTypeChange">
          <el-option
            v-for="item in getValidOperationList"
            :key="item.id"
            :value="item.id"
            :label="item.name"
          />
        </el-select>
      </el-form-item>
      <el-form-item
        label="Expression"
        v-if="flowConditionForm.conditionType && flowConditionForm.conditionType === 'expression'"
        key="express"
      >
        <el-input
          :model-value="flowConditionForm.body"
          @update:modelValue="onExpressionBodyChange"
          clearable
          type="textarea"
          :disabled="flowConditionForm.type === 'operation'"
        />
      </el-form-item>
      <template
        v-if="flowConditionForm.conditionType && flowConditionForm.conditionType === 'script'"
      >
        <el-form-item label="Script Language" key="language">
          <el-input v-model="flowConditionForm.language" clearable @change="updateFlowCondition" />
        </el-form-item>
        <el-form-item label="Script Type" key="scriptType">
          <el-select v-model="flowConditionForm.scriptType">
            <el-option label="Inline Script" value="inlineScript" />
            <el-option label="External Script" value="externalScript" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="Script"
          v-if="flowConditionForm.scriptType === 'inlineScript'"
          key="body"
        >
          <el-input
            :value="flowConditionForm.body"
            @input="onExpressionBodyChange"
            clearable
            type="textarea"
            :disabled="flowConditionForm.type === 'operation'"
          />
        </el-form-item>
        <el-form-item
          label="Resource URL"
          v-if="flowConditionForm.scriptType === 'externalScript'"
          key="resource"
        >
          <el-input v-model="flowConditionForm.resource" clearable @change="updateFlowCondition" />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { SysFlowTaskOperationType } from '@/common/staticDict/flow';
import { ANY_OBJECT } from '@/types/generic';

const props = defineProps<{ businessObject: ANY_OBJECT; type: string }>();

import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();
const prefix = inject('prefix');

const flowConditionForm = ref<ANY_OBJECT>({});
let bpmnElement: ANY_OBJECT | null = {};
let bpmnElementSource: ANY_OBJECT | null = {};
let bpmnElementSourceRef: ANY_OBJECT | null = {};
const win: ANY_OBJECT = window;

const getValidOperationList = computed(() => {
  return SysFlowTaskOperationType.getList().filter(item => {
    return (
      [
        SysFlowTaskOperationType.INTERVENE,
        SysFlowTaskOperationType.REVIVE,
        SysFlowTaskOperationType.TIMEOUT_AUTO_COMPLETE,
        SysFlowTaskOperationType.EMPTY_USER_AUTO_COMPLETE,
        SysFlowTaskOperationType.EMPTY_USER_AUTO_REJECT,
      ].indexOf(item.id) === -1
    );
  });
});

const updateFlowCondition = () => {
  let { conditionType, scriptType, body, resource, language } = flowConditionForm.value;
  let condition;
  let otherExtensionList =
    bpmnElement?.businessObject?.extensionElements?.values?.filter((ex: ANY_OBJECT) => {
      return ex.$type === `${prefix}:Properties`;
    }) ?? [];
  if (conditionType === 'expression') {
    condition = win.bpmnInstances.moddle.create('bpmn:FormalExpression', {
      body,
    });
    // 内置按钮
    if (flowConditionForm.value.type === 'operation') {
      let customCondition = win.bpmnInstances.moddle.create(`${prefix}:CustomCondition`, {
        type: flowConditionForm.value.type,
        operationType:
          flowConditionForm.value.operationType === SysFlowTaskOperationType.PARALLEL_REFUSE
            ? undefined
            : flowConditionForm.value.operationType,
        parallelRefuse:
          flowConditionForm.value.operationType === SysFlowTaskOperationType.PARALLEL_REFUSE,
      });
      otherExtensionList.push(customCondition);
    }
  } else {
    const val = { ...flowConditionForm.value };
    if (scriptType === 'inlineScript') {
      condition = win.bpmnInstances.moddle.create('bpmn:FormalExpression', {
        body,
        language,
      });
      val.resource = '';
      flowConditionForm.value = val;
    } else {
      val.body = '';
      flowConditionForm.value = val;
      condition = win.bpmnInstances.moddle.create('bpmn:FormalExpression', {
        resource,
        language,
      });
    }
  }
  const extensions = win.bpmnInstances.moddle.create('bpmn:ExtensionElements', {
    values: otherExtensionList,
  });
  win.bpmnInstances.modeling.updateProperties(bpmnElement, {
    conditionExpression: condition,
    extensionElements: extensions,
  });
};
const onExpressionBodyChange = (val: string) => {
  flowConditionForm.value.body = val;
  updateFlowCondition();
};
const resetFlowCondition = () => {
  bpmnElement = win.bpmnInstances.bpmnElement;
  bpmnElementSource = bpmnElement?.source;
  bpmnElementSourceRef = bpmnElement?.businessObject.sourceRef;
  if (
    bpmnElementSourceRef &&
    bpmnElementSourceRef.default &&
    bpmnElementSourceRef.default.id === bpmnElement?.id
  ) {
    // 默认
    flowConditionForm.value = { type: 'default' };
  } else if (!bpmnElement?.businessObject.conditionExpression) {
    // 普通
    flowConditionForm.value = { type: 'normal' };
  } else {
    // 带条件
    const conditionExpression = bpmnElement.businessObject.conditionExpression;
    let extensionElements =
      bpmnElement.businessObject.get('extensionElements') ||
      win.bpmnInstances.moddle.create('bpmn:ExtensionElements', { values: [] });
    let customCondition = (extensionElements.values || []).filter(
      (ex: ANY_OBJECT) => ex.$type === `${prefix}:CustomCondition`,
    )[0];
    if (customCondition && customCondition.type === 'operation') {
      flowConditionForm.value = { ...conditionExpression, type: customCondition.type };
      flowConditionForm.value.operationType = customCondition.parallelRefuse
        ? SysFlowTaskOperationType.PARALLEL_REFUSE
        : customCondition.operationType;
    } else {
      flowConditionForm.value = { ...conditionExpression, type: 'condition' };
    }
    // resource 可直接标识 是否是外部资源脚本
    const val = { ...flowConditionForm.value };
    if (flowConditionForm.value.resource) {
      val.conditionType = 'script';
      val.scriptType = 'externalScript';
      flowConditionForm.value = val;
      return;
    }
    if (conditionExpression.language) {
      val.conditionType = 'script';
      val.scriptType = 'inlineScript';
      flowConditionForm.value = val;
      return;
    }
    val.conditionType = 'expression';
    flowConditionForm.value = val;
  }
};
const onOperationTypeChange = (value: string) => {
  if (value == null || value === '') {
    flowConditionForm.value.body = undefined;
  } else if (value === SysFlowTaskOperationType.PARALLEL_REFUSE) {
    // 并行网关拒绝
    flowConditionForm.value.body = '${parallelRefuse == true}';
  } else {
    flowConditionForm.value.body = "${operationType == '" + value + "'}";
  }
  updateFlowCondition();
};
const updateFlowType = (flowType: string) => {
  // 正常条件类
  if (flowType === 'condition' || flowType === 'operation') {
    // 内置按钮
    let extensionElements = null;
    if (flowType === 'operation') {
      let otherExtensionList =
        bpmnElement?.businessObject?.extensionElements?.values?.filter((ex: ANY_OBJECT) => {
          return ex.$type === `${prefix}:Properties`;
        }) ?? [];
      extensionElements = win.bpmnInstances.moddle.create('bpmn:ExtensionElements', {
        values: [
          ...otherExtensionList,
          win.bpmnInstances.moddle.create(`${prefix}:CustomCondition`, {
            type: flowType,
          }),
        ],
      });
    }
    const flowConditionRef = win.bpmnInstances.moddle.create('bpmn:FormalExpression');
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      conditionExpression: flowConditionRef,
      extensionElements: extensionElements,
    });
    return;
  }
  // 默认路径
  if (flowType === 'default') {
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      conditionExpression: null,
    });
    win.bpmnInstances.modeling.updateProperties(bpmnElementSource, {
      default: bpmnElement,
    });
    return;
  }
  // 正常路径，如果来源节点的默认路径是当前连线时，清除父元素的默认路径配置
  if (bpmnElementSourceRef?.default && bpmnElementSourceRef.default.id === bpmnElement?.id) {
    win.bpmnInstances.modeling.updateProperties(bpmnElementSource, {
      default: null,
    });
  }
  win.bpmnInstances.modeling.updateProperties(bpmnElement, {
    conditionExpression: null,
  });
};

watch(
  () => props.businessObject,
  () => {
    nextTick(() => resetFlowCondition());
  },
  { immediate: true },
);

onBeforeUnmount(() => {
  bpmnElement = null;
  bpmnElementSource = null;
  bpmnElementSourceRef = null;
});
</script>
