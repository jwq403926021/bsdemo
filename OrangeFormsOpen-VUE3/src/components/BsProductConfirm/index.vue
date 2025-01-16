<template>
  <div>
    <el-table
      v-if="selectedMode === 'pc'"
      ref="multipleTableRef"
      :data="tableData"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column label="Product Name" min-width="50%">
        <template #default="scope">
          {{ scope.row.productUpn }} - {{ scope.row.productName }}
        </template>
      </el-table-column>
      <el-table-column label="Inventory" min-width="25%">
        <template #default="scope">
          <div style="display: flex; align-items: center">
            <span
              :style="{
                display: 'inline-block',
                background: scope.row.recordStatus,
                width: '10px',
                height: '5px',
              }"
            ></span>
            <span style="margin: 0 10px">{{ scope.row.qty }}</span>
            <span>unit (s)</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column label="Quantity" property="selectedQty" min-width="25%" />
    </el-table>
    <bs-product-card
      v-else
      :widget="widget"
      :data="tableData"
      :multipleSelection="multipleSelection"
      :isConfirm=true
      @onCardClick=""
      @onQuantityUpdate=""
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, inject } from 'vue';
import { eventbus } from '@/common/utils/mitt';
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';
import BsProductCard from '@/components/BsProductCard/index.vue';

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
const widgetList = inject('widgetList');
const selectedMode = ref<string>('pc');
const tableData = ref([]); // 初始化为空数组
const multipleSelection = ref([]);

const handleSelectionChange = val => {
  console.log('val=============', val);
  multipleSelection.value = val;
};
const data = ref([]);
const step = inject('step');
watch(
  () => step.value,
  () => {
    const result = [];
    const form = formInject();
    console.log('form==========', form);
    form.widgetList.forEach(i => {
      if (step.value === 3 && i.widgetType === 409) {
        console.log('i.variableName==========', i.variableName, ':', widgetList[i.variableName]);
        if (widgetList[i.variableName].getValue) {
          tableData.value = widgetList[i.variableName].getValue().products;
        }
      }
    });
    console.log(result);
    data.value = result;
  },
);
onMounted(() => {
  eventbus.on('transferSelectedMode', d => {
    selectedMode.value = d as string;
  })
});
onUnmounted(() => {
  eventbus.off('transferSelectedMode');
});
</script>

<style scoped>
::v-deep(.el-table th .cell) {
  color: #1a457a !important;
}
</style>
