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
import axios from 'axios';
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';
import { eventbus } from '@/common/utils/mitt';
import { serverDefaultCfg } from '@/common/http/config';

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
  const selectedItem = selectedItems.value.find(i => i.value === value);
  eventbus.emit(`bs:${pps.widget.variableName}`, selectedItem);
};

const getProductLevelName = async (isClear = false, data?) => {
  const formInstance = form();
  // if (!pps.depend) {
  //   console.error('depend argument is not config');
  // }
  if (formInstance.isEdit) return;
  // const dependWidget = formInstance.widgetList.find(i => i.variableName === pps.depend);
  // if (!dependWidget) return;
  // const dependValue = formInstance.getWidgetValue(dependWidget);
  if (isClear) {
    // 向下传递
    emit('update:modelValue', '');
    selectedItems.value = [];
    eventbus.emit(`bs:${pps.widget.variableName}`, null);
  }
  let productLevel = '';
  if (data) {
    productLevel = data.value;
  }
  const res = await axios.get(
    `${serverDefaultCfg.baseURL}order/product?productLevel=${productLevel}`,
  );
  selectedItems.value = res?.data
    ?.map(i => ({
      label: i.productLevelName,
      value: i.productLevelName,
    }))
    .filter(
      (value, index, self) =>
        index === self.findIndex(t => t.label === value.label && t.value === value.value),
    );
  // }
};

onMounted(() => {
  getProductLevelName(true);
  eventbus.on(`bs:${pps.depend}`, d => {
    getProductLevelName(true, d);
  });
});
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
const getValue = () => {
  return {
    value: pps.modelValue,
    valueHuman: selectedItems.value.find(i => i.value === pps.modelValue)?.label || '',
  };
};
const setValue = (val: string) => {
  emit('update:modelValue', val);
  emit('change', val);
};
defineExpose({ getValue, setValue });
</script>
