<template>
  <div>
    <el-table
      v-if="selectedMode === 'pc'"
      ref="multipleTableRef"
      :data="tableData"
      style="width: 100%"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" :selectable="selectable" width="55" />
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
      <el-table-column label="Quantity" min-width="25%">
        <template #default="scope">
          <el-input-number
            v-model="scope.row.selectedQty"
            :min="1"
            :max="100"
            @change="updateSelectedQuantity(scope.row)"
            style="width: 105px"
          />
        </template>
      </el-table-column>
    </el-table>
    <bs-product-card
      v-else
      :widget="widget"
      :data="tableData"
      :multipleSelection="multipleSelection"
      @onCardClick="handleCardClick"
      @onQuantityUpdate="updateSelectedQuantity"
    />
  </div>
</template>

<script setup lang="ts">
import axios from 'axios';
import { ref, onMounted } from 'vue';
import { eventbus } from '@/common/utils/mitt';
import { serverDefaultCfg } from '@/common/http/config';
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';
import BsProductCard from '@/components/BsProductCard/index.vue';
import { Product } from './type';

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
const productName = ref('');
const productLevel = ref('');
const productLevelName = ref('');
const selectedMode = ref<string>('pc');
const tableData = ref<Product[]>([]); // 初始化为空数组
const multipleSelection = ref<Product[]>([]);
const selectable = (row, index) => {
  // 返回 true 表示所有行都可以选择
  return true;
};

const handleSelectionChange = val => {
  console.log('val=============', val);
  multipleSelection.value = val;
};

  const isCardSelected = (prod: Product) => {
    return multipleSelection.value.some((selectedItem: Product) => selectedItem.id === prod.id)
  }

  const handleCardClick = (prod: Product) => {
    if (isCardSelected(prod)) {
      multipleSelection.value = multipleSelection.value.filter((selectedItem) => {
        if (selectedItem.id !== prod.id) {
          return true
        } else {
          prod.isCardSelected = false
          return false
        }
      })
    } else {
      prod.isCardSelected = true
      multipleSelection.value.push(prod)
    }
  }

// 从API获取数据
const fetchData = async () => {
  try {
    // const formInstance = formInject();
    // console.log(formInstance.mode);
    // if (formInstance.isEdit) {
    //   return;
    // }
    const response = await axios.get(`${serverDefaultCfg.baseURL}order/product`, {
      params: {
        productName: productName.value,
        productLevel: productLevel.value,
        productLevelName: productLevelName.value,
      },
    });
    // 添加 selectedQty 字段以保存用户选择的数量
    tableData.value = response.data.map((item: Product) => ({ ...item, selectedQty: 1, isCardSelected: false }));
  } catch (error) {
    console.error('Error fetching data:', error);
  }
};

// 更新选定行的数量
const updateSelectedQuantity = row => {
  console.log(`Updated quantity for ${row.productName}: ${row.selectedQty}`);
};

const getValue = () => {
  return {
    products: multipleSelection.value,
  };
};
const setValue = () => {
  return null;
};
defineExpose({ getValue, setValue });

onMounted(() => {
  fetchData();
  eventbus.on(`bs:upnProductName`, d => {
    productName.value = d.value;
    fetchData();
  });
  eventbus.on(`bs:productLevel`, d => {
    productLevel.value = d.value;
    fetchData();
  });
  eventbus.on(`bs:productLevelName`, d => {
    if (!d) {
      productLevelName.value = '';
    } else {
      productLevelName.value = d.value;
      fetchData();
    }
  });
  eventbus.on('transferSelectedMode', d => {
    selectedMode.value = d as string;
  })
});
onUnmounted(() => {
  eventbus.off('bs:upnProductName');
  eventbus.off('bs:productLevel');
  eventbus.off('bs:productLevelName');
  eventbus.off('transferSelectedMode');
});
</script>

<style scoped lang="scss">
::v-deep(.el-table th .cell) {
  color: #1a457a !important;
}
</style>
