<template>
  <div>
    <el-form
      v-if="tabType === 'baseInfo'"
      ref="form"
      label-position="top"
      :size="layoutStore.defaultFormItemSize"
      :model="formData"
      @submit.prevent
    >
      <el-form-item label="Java委托类">
        <el-input
          v-model="formData.class"
          clearable
          placeholder="输入包含包名的完整委托类名"
          @change="updateServiceTaskClass"
        />
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { defineProps, ref, inject, watch, nextTick } from 'vue';
import { ANY_OBJECT } from '@/types/generic';
import { useLayoutStore } from '@/store';

const layoutStore = useLayoutStore();
const props = defineProps<{
  id: string;
  type: string;
  tabType: string;
}>();
const flowEntry = inject('flowEntry', () => {
  return {} as ANY_OBJECT;
});
const prefix = inject('prefix');
const win: ANY_OBJECT = window;

type FormDataType = {
  class: string;
};
const formData = ref<FormDataType>({
  class: '',
});
const taskInfoElement = ref();

let bpmnELement = win.bpmnInstances.bpmnElement;
const resetTaskInfo = () => {
  formData.value = {
    class: bpmnELement.businessObject.class,
  };
};
const updateServiceTaskClass = () => {
  win.bpmnInstances.modeling.updateProperties(bpmnELement, {
    class: formData.value.class,
  });
};

watch(
  () => props.id,
  val => {
    val && val.length && nextTick(() => resetTaskInfo());
  },
  {
    immediate: true,
  },
);
</script>

<style></style>
