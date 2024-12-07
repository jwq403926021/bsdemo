<template>
  <div class="user-select">
    <el-select :model-value="modelValue" style="width: 100%" @change="emitChange">
      <el-option
        v-for="item in selectedItems"
        :key="item.value"
        :label="item.label"
        :value="item.value"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { eventbus } from '@/common/utils/mitt';
import { WidgetProps } from '@/online/components/types/widget';

const emit = defineEmits<{
  'update:modelValue': [string | number | ANY_OBJECT[]];
  change: [string | number | ANY_OBJECT[], string | number | ANY_OBJECT[]];
}>();

const pps = withDefaults(
  defineProps<{
    modelValue: string | number | Array<ANY_OBJECT> | undefined;
    widget: WidgetProps;
  }>(),
  {
  },
);
const formInject = inject('form');
const selectedItems = ref<ANY_OBJECT[]>([]);

const emitChange = value => {
  emit('update:modelValue', value);
  emit('change', value);
};

const getSelectUserList = () => {
  selectedItems.value = [
    {
      label: 'parent option 1',
      value: 'p1',
    },
    {
      label: 'parent option 2',
      value: 'p2',
    },
  ];
};
onMounted(() => {
  getSelectUserList();
});
const getValue = () => {
  return {
    value: pps.modelValue,
    valueHuman: selectedItems.value.find(i => i.value === pps.modelValue)?.label || '',
  };
};
defineExpose({ getValue });
</script>
