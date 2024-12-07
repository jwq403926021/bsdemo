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
import axios from "axios";
import { serverDefaultCfg } from "@/common/http/config";

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
  const selectedItem = selectedItems.value.find(i => i.value === value)
  eventbus.emit(`bs:${pps.widget.variableName}`, selectedItem);
};

const getSelectList = async (isClear = false, data) => {
  const formInstance = form();
  if (!pps.depend) {
    console.error('depend argument is not config');
  }
  if (formInstance.isEdit) return;
  const dependWidget = formInstance.widgetList.find(i => i.variableName === pps.depend);
  if (!dependWidget) return;
  const dependValue = formInstance.getWidgetValue(dependWidget);
  if (isClear) {
    emit('update:modelValue', '');
    selectedItems.value = []
    eventbus.emit(`bs:${pps.widget.variableName}`, null);
  }
  console.log('bsaccountname receive', data);
  if (data) {
    const res = await axios.get(`${serverDefaultCfg.baseURL}order/orderSalesHierarchy?salesRepNum=${data.code}`)
    selectedItems.value = res?.data?.map(i => ({
      ...i,
      label: i.fullName + ' - ' + i.stockLocName,
      value: i.stockLocId
    }));
  }
};

onMounted(() => {
  eventbus.on(`bs:${pps.depend}`, d => {
    getSelectList(true, d);
  });
  nextTick(() => {
    getSelectList(false);
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
defineExpose({ getValue });
</script>
