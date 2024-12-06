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
const selectedItems = ref<ANY_OBJECT[]>([]);
const form = inject('form');
const step = inject('step');
const emitChange = value => {
  emit('update:modelValue', value);
  emit('change', value);
};

const getSelectList = (isClear = false) => {
  const formInstance = form();
  const dependWidget = formInstance.widgetList.find(i => i.variableName === pps.depend);
  const dependValue = formInstance.getWidgetValue(dependWidget);
  console.log(formInstance, '11');
  console.log(dependWidget, '22');
  console.log(dependValue, '33');
  if (isClear) {
    emit('update:modelValue', '');
  }
  selectedItems.value = [
    {
      label: `child option 1 base on '${dependValue}'`,
      value: 'c1',
    },
    {
      label: `child option 2 base on '${dependValue}'`,
      value: 'c2',
    },
  ];
};

onMounted(() => {
  eventbus.on(`bs:${pps.depend}`, d => {
    getSelectList(true);
  });
  nextTick(() => {
    getSelectList(false);
  });
});
watch(
  () => step.value,
  v => {
    console.log('watch step:', v);
  },
  {
    immediate: true,
  },
);
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
</script>
