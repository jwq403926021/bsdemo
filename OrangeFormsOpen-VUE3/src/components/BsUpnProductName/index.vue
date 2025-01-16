<template>
  <el-form :inline="true">
    <el-form-item>
      <el-input
        v-model="productName"
        style="width: form().mode === 'pc' ? '240px' : '200px'"
        placeholder="Enter UPN/Name"
        :suffix-icon="Search"
      />
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="emitSearch">Search</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { Search } from '@element-plus/icons-vue';
import { ref, inject } from 'vue';
import { eventbus } from '@/common/utils/mitt';
import { ANY_OBJECT } from '@/types/generic';
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

const emitSearch = value => {
  eventbus.emit(`bs:${pps.widget.variableName}`, productName);
};

const requestDeliveryDate = ref('');
const productName = ref('');

// 获取当前值和对应的标签
const getValue = () => {
  return {
    value: requestDeliveryDate,
  };
};
const setValue = (val: string) => {
  productName.value = val;
  emitSearch('')
};
defineExpose({ getValue, setValue });
</script>

<style scoped></style>
