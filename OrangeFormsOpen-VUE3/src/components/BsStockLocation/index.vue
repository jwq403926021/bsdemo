<template>
  <div class="user-select">
    <el-input :model-value="stockLocName" disabled />
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
const stockLocName = ref('');
const form = inject('form');
const step = inject('step');
const selected = ref({})
const emitChange = value => {};

onMounted(() => {
  eventbus.on(`bs:${pps.depend}`, d => {
    console.log('bs stocklocation receive:', d);
    if (d) {
      emit('update:modelValue', d.stockLocId);
      emit('change', d.stockLocId);
      stockLocName.value = d.stockLocName;
      selected.value = d
    } else {
      emit('update:modelValue', '');
      emit('change', '');
      stockLocName.value = '';
    }
  });
});
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
const getValue = () => {
  return {
    ...selected.value,
    value: pps.modelValue,
    valueHuman: stockLocName.value,
  };
};
defineExpose({ getValue });
</script>
