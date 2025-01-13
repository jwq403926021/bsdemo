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
import { removeDuplicates } from '@/common/utils';

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
const selectedContactInfo = ref<ANY_OBJECT | undefined | null>({});
const form = inject('form');
const step = inject('step');
const emitChange = value => {
  emit('update:modelValue', value);
  emit('change', value);
  const selectedItem = selectedItems.value.find(i => i.value === value);
  selectedContactInfo.value = selectedItem;
  eventbus.emit(`bs:${pps.widget.variableName}`, selectedItem);
};

const getSelectList = async (isClear = false, data) => {
  const formInstance = form();
  if (!pps.depend) {
    console.error('depend argument is not config');
  }
  if (formInstance.isEdit) return;
  if (isClear) {
    emit('update:modelValue', '');
    selectedItems.value = [];
    selectedContactInfo.value = null;
    eventbus.emit(`bs:${pps.widget.variableName}`, null);
  }
  console.log('bscontactinfo receive', data);
  if (!data?.value && pps.depend) return; // has depend but don't have value, do not request options
  const res = await axios.get(
    `${serverDefaultCfg.baseURL}order/customer${data?.value ? `?code=${data.value}` : ''}`,
  );
  const result = removeDuplicates(res?.data || [], ['recipient', 'telNo']);
  selectedItems.value = result.map(i => ({
    ...i,
    label: i.recipient + ' | ' + i.telNo,
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
  return {
    contactInfo: selectedContactInfo.value?.recipient && selectedContactInfo.value?.telNo ? selectedContactInfo.value?.recipient + ' | ' + selectedContactInfo.value?.telNo : '',
    recipient: selectedContactInfo.value?.recipient,
    phone: selectedContactInfo.value?.telNo,
    ...selectedContactInfo.value,
  };
};
const setValue = (val: string) => {
  emit('update:modelValue', val);
  emit('change', val);
};
defineExpose({ getValue, setValue });
</script>
