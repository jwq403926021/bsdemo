<template>
  <div class="user-select">
    <el-date-picker
      v-model="requestDeliveryDate"
      type="date"
      style="width: 100%"
      @change="emitChange"
      placeholder="Select a date"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
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
  {},
);
const formInject = inject('form');

// 用于存储选中的日期
const selectedDate = ref<string | null>(null);
const requestDeliveryDate = ref('');

// 在日期变化时触发的方法
const emitChange = value => {
  emit('update:modelValue', value);
  emit('change', value);

  // 使用选中的日期值来做其他事情
  console.log(selectedDate.value, '?');
  const formattedDate = value ? new Date(value).toLocaleDateString() : '';
  eventbus.emit(`bs:${pps.widget.variableName}`, formattedDate);
};

// 模拟获取数据的函数（可以根据需要移除或修改）
const getSelectUserList = async () => {
  // 如果这是一个固定的日期选择组件，则不需要API请求
  // const res = await axios.get(`${serverDefaultCfg.baseURL}order/divisions`);
  // selectedItems.value = res?.data?.map(i => ({
  //   ...i,
  //   label: i.name,
  //   value: i.code,
  // }));
};

onMounted(() => {
  getSelectUserList(); // 可以选择去掉这部分，如果不需要
});

// 获取当前值和对应的标签
const getValue = () => {
  return {
    requestDeliveryDate: requestDeliveryDate.value,
  };
};
defineExpose({ getValue });
</script>

<style scoped>
.user-select {
}
</style>
