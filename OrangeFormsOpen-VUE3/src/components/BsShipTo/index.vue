<template>
  <div class="user-select">
    <el-input :model-value="soldToName" disabled />
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';
import { eventbus } from '@/common/utils/mitt';

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
  {
    depend: '',
  },
);
const soldToName = ref('');
const form = inject('form');
const step = inject('step');
const selected = ref({});
const emitChange = value => {
  return null;
};

onMounted(() => {
  eventbus.on(`bs:${pps.depend}`, d => {
    console.log('bsshipto receive:', d);
    if (d) {
      emit('update:modelValue', d.soldToNum);
      emit('change', d.soldToNum);
      soldToName.value = d.soldToName;
      selected.value = d;
    } else {
      emit('update:modelValue', '');
      emit('change', '');
      soldToName.value = '';
    }
  });
});
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
const getValue = () => {
  return {
    shipToName: soldToName.value,
    value: pps.modelValue,
    valueHuman: soldToName.value,
  };
};
const setValue = (val: string) => {
  emit('update:modelValue', val);
  emit('change', val);
};
defineExpose({ getValue, setValue });
</script>
