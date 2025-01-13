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
  const selectedItem = selectedItems.value.find(i => i.code === value);
  eventbus.emit(`bs:${pps.widget.variableName}`, selectedItem);
};

const getSelectList = async (isClear = false, data) => {
  const formInstance = form();
  if (!pps.depend) {
    console.error('depend argument is not config');
  }
  if (formInstance.isEdit) return;
  if (isClear) {
    // 向下传递
    emit('update:modelValue', '');
    selectedItems.value = [];
    eventbus.emit(`bs:${pps.widget.variableName}`, null);
  }
  console.log('bssr receive', data);
  if (!data?.value && pps.depend) return; // has depend but don't have value, do not request options
  const res = await axios.get(
    `${serverDefaultCfg.baseURL}order/saleRep${data?.value ? `?userDivision=${data.value}` : ''}`,
  );
  selectedItems.value = res?.data?.map(i => ({
    ...i,
    label: i.name,
    value: i.code,
  }));
};

onMounted(() => {
  if (pps.depend) {
    eventbus.on(`bs:${pps.depend}`, d => {
      getSelectList(true, d);
    });
  }

  nextTick(() => {
    getSelectList(false);
  });
});
onUnmounted(() => {
  eventbus.off(`bs:${pps.depend}`);
});
const getValue = () => {
  const selected = selectedItems.value.find(i => i.value === pps.modelValue) || {};
  return {
    ...selected,
    srName: selected?.label || '',
    value: pps.modelValue,
    valueHuman: selected?.label || '',
  };
};
const setValue = (val: string) => {
  emit('update:modelValue', val);
  emit('change', val);
};
defineExpose({ getValue, setValue });
</script>
