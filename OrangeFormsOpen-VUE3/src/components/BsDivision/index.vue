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
import { eventbus } from '@/common/utils/mitt';
import { WidgetProps } from '@/online/components/types/widget';
import { get } from '@/common/http/request';
import { serverDefaultCfg } from '@/common/http/config';
const emit = defineEmits<{
  'update:modelValue': [string | number | ANY_OBJECT[]];
  change: [string | number | ANY_OBJECT[], string | number | ANY_OBJECT[]];
}>();

const pps = withDefaults(
  defineProps<{
    modelValue: string | number | Array<ANY_OBJECT> | undefined;
    widget: WidgetProps;
  }>(),
  {},
);
const formInject = inject('form');
const selectedItems = ref<ANY_OBJECT[]>([]);

const emitChange = value => {
  emit('update:modelValue', value);
  emit('change', value);
  console.log(selectedItems.value, '?');
  const selectedItem = selectedItems.value.find(i => i.code === value);
  eventbus.emit(`bs:${pps.widget.variableName}`, selectedItem);
};

const getSelectUserList = async () => {
  const res = await axios.get(`${serverDefaultCfg.baseURL}order/divisions`);
  selectedItems.value = res?.data?.map(i => ({
    ...i,
    label: i.name,
    value: i.name,
  }));
};
onMounted(() => {
  getSelectUserList();
});
const getValue = () => {
  const selected = selectedItems.value.find(i => i.value === pps.modelValue) || {};
  return {
    ...selected,
    divisionsName: selected?.name || '',
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
