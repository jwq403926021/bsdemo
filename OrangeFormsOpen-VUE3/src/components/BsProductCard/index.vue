<template>
  <div>
    <div class="mb-display-box">
      <el-card v-if="isConfirm" style="margin: 10px 0px; box-shadow: 0 0 0;">
        <div class="confirm-card" v-for="(prod, _) in data" :key="prod.productUpn">
          <el-row>
            <el-col :span="18">
              <div>
                <div class="product-info">{{ prod.productDescription }}</div>
                <div class="product-info">{{ prod.productUpn }}</div>
                <div class="product-info">
                  <img class="product-info-icon" src="@/assets/img/ic_batch.png" alt="icon">
                  <span>{{ prod.id }}</span>
                </div>
              </div>
            </el-col>
            <el-col :span="6" class="quantity-section">
              {{ 'x ' + prod.selectedQty }}
            </el-col>
          </el-row>
        </div>
        <el-row justify="end">
          {{ totalCount + ' Itme(s)' }}
        </el-row>
      </el-card>
      <el-card class="box-card" v-else v-for="(prod, _) in data" :key="prod.id" @click="emit('onCardClick', prod)"
        :class="{ selected: prod.isCardSelected }">
        <el-row>
          <el-col :span="18">
            <div>
              <div class="product-info">{{ prod.productDescription }}</div>
              <div class="product-info">{{ prod.productUpn }}</div>
              <div class="product-info">
                <img class="product-info-icon" src="@/assets/img/ic_batch.png" alt="icon">
                <span>{{ prod.id }}</span>
              </div>
            </div>
          </el-col>
          <el-col :span="6" class="quantity-section">
            {{ prod.qty + ' unit(s)' }}
          </el-col>
        </el-row>
        <div class="input-num-comp" @click.stop>
          <el-input-number v-model="prod.selectedQty" class="input-box" :min="1" :max="prod.qty" size="small"
            @change="emit('onQuantityUpdate', prod)" />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { WidgetProps } from '@/online/components/types/widget';
import { Product } from '../BsProduct/type';

const emit = defineEmits(['onCardClick', 'onQuantityUpdate']);

const props = withDefaults(
  defineProps<{
    data: Product[]
    multipleSelection: Product[]
    widget: WidgetProps
    isConfirm?: boolean
  }>(),
  { isConfirm: false },
);

const totalCount = computed(() => {
  return props.data.reduce((sum, item) => sum + (item.selectedQty || 0), 0);
});
</script>

<style scoped lang="scss">
.mb-display-box {
  background-color: $color-white !important;
}

.confirm-card {
  font-size: 12px;
  font-weight: 400;
  color: #000000;
  border-bottom: 1px solid #DCE6EF;
  padding-bottom: 3px;
  margin-bottom: 12px;
}

.box-card {
  margin: 0px 0px 10px 0px;
  font-size: 12px;
  font-weight: 400;
  color: #436180;
  box-shadow: 0 0 0;
}

.selected {
  background-color: rgb(237, 237, 250);
}

.product-info {
  margin-bottom: 5px;
}

.product-info-icon {
  width: 20px;
  height: 20px;
}

.quantity-section {
  display: flex;
  justify-content: end;
  align-items: center;
}

.input-num-comp {
  display: flex;
  justify-content: end;
}

.input-box {
  width: 100px !important;
  height: 30px;
}
</style>
