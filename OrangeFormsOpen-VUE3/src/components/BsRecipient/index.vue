<template>
  <div>
    <el-input
      v-model="inputValue"
      @input="emitInput"
      @change="emitChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, inject } from 'vue';
import { eventbus } from '@/common/utils/mitt';
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';

const emit = defineEmits<{
  'update:modelValue': [string | number | ANY_OBJECT[]];
  change: [string | number | ANY_OBJECT[], string | number | ANY_OBJECT[]];
}>();

const pps = withDefaults(
  defineProps<{
    modelValue: string | number | Array<ANY_OBJECT> | undefined;
    depend?: string;
    widget: WidgetProps;
  }>(),
  {},
);
const inputValue = ref('')
const form = inject('form');
const step = inject('step');
const emitInput = value => {
  inputValue.value = value
};
const emitChange = value => {
  emit('update:modelValue', inputValue.value);
  emit('change', inputValue.value);
};
onMounted(() => {
  if (pps.depend) {
    eventbus.on(`bs:${pps.depend}`, d => {
      console.log('bs recipient receive data', d);
      inputValue.value = d?.recipient || ''
    });
  }
});
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
// 获取当前值和对应的标签
const getValue = () => {
  return {
    recipientModify: inputValue.value,
    valueHuman: inputValue.value
  };
};
defineExpose({ getValue });
</script>

<style scoped></style>
