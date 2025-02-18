<template>
  <div>
    <el-form :size="layoutStore.defaultFormItemSize" @submit.prevent label-position="top">
      <el-form-item label="Process Name">
        <el-input :value="flowEntry().value.processDefinitionName" :disabled="true" />
      </el-form-item>
      <el-form-item label="Task ID">
        <el-input
          v-model="elementBaseInfo.id"
          :disabled="idEditDisabled || elementBaseInfo.$type === 'bpmn:Process'"
          clearable
          @input="updateBaseInfo('id')"
        />
      </el-form-item>
      <el-form-item label="Task Name">
        <el-input
          v-model="elementBaseInfo.name"
          clearable
          :disabled="idEditDisabled || elementBaseInfo.$type === 'bpmn:Process'"
          @input="updateBaseInfo('name')"
        />
      </el-form-item>
      <!-- Basic properties of the process -->
      <template v-if="elementBaseInfo.$type === 'bpmn:Process'">
        <el-form-item label="Executable">
          <el-switch
            v-model="elementBaseInfo.isExecutable"
            @change="updateBaseInfo('isExecutable')"
            style="margin-left: 18px"
          />
        </el-form-item>
      </template>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { ref, onBeforeUnmount, watch, nextTick, inject } from 'vue';
import { ANY_OBJECT } from '@/types/generic';
import { useLayoutStore } from '@/store';

const layoutStore = useLayoutStore();
const props = withDefaults(
  defineProps<{ businessObject: ANY_OBJECT; type?: string; idEditDisabled?: boolean }>(),
  { idEditDisabled: true },
);

const flowEntry = inject('flowEntry', () => {
  return {} as ANY_OBJECT;
});

const elementBaseInfo = ref<ANY_OBJECT>({});
let bpmnElement: ANY_OBJECT | null = null;
const win: ANY_OBJECT = window;

const resetBaseInfo = () => {
  bpmnElement = win?.bpmnInstances?.bpmnElement;
  elementBaseInfo.value = JSON.parse(JSON.stringify(bpmnElement?.businessObject));
};
const updateBaseInfo = (key: string) => {
  const attrObj = Object.create(null);
  attrObj[key] = elementBaseInfo.value[key];
  if (key === 'id') {
    win.bpmnInstances.modeling.updateProperties(bpmnElement, {
      id: elementBaseInfo.value[key],
      di: { id: `${elementBaseInfo.value[key]}_di` },
    });
  } else {
    win.bpmnInstances.modeling.updateProperties(bpmnElement, attrObj);
  }
};

onBeforeUnmount(() => {
  bpmnElement = null;
});

watch(
  () => props.businessObject,
  val => {
    if (val) {
      nextTick(() => resetBaseInfo());
    }
  },
);
</script>
