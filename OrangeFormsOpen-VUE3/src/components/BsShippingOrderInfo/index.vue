<template>
  <div class="shipping-order-info">
    <div class="shipping-order-info-title">Shipping Order Info</div>
    <ul>
      <li class="shipping-order-info-item" :key="item.valueHuman" v-for="item in data">
        {{ item.label }}: {{ item.valueHuman }}
      </li>
    </ul>
  </div>
</template>

<script setup lang="ts">
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
const formInject = inject<ANY_OBJECT>('form');
const widgetList = inject<ANY_OBJECT>('widgetList');
const data: ANY_OBJECT = ref([]);
const step = inject<ANY_OBJECT>('step');
watch(
  () => step.value,
  () => {
    getInfo();
  },
);
const getInfo = () => {
  const result: ANY_OBJECT[] = [];
  const form = formInject();
  console.log('form:', form);
  form.widgetList.forEach(i => {
    const includes = [404, 405, 406, 412, 408];
    if (includes.includes(i.widgetType)) {
      console.log(i.variableName, ':', widgetList[i.variableName]);
      let widgetValueObj: ANY_OBJECT = {};
      if (widgetList[i.variableName]?.getValue) {
        widgetValueObj = widgetList[i.variableName].getValue();
      }
      result.push({
        label: i.showName,
        value: widgetValueObj?.value || '',
        valueHuman: widgetValueObj?.valueHuman || '',
      });
    }
  });
  console.log(result);
  data.value = result;
};
onMounted(() => {
  getInfo();
});
</script>

<style scoped lang="scss">
.shipping-order-info {
  margin-bottom: 50px;
}
.shipping-order-info-title {
  font-size: 18px;
  color: $color-primary;
  margin-bottom: 10px;
}
.shipping-order-info-item {
  font-size: 14px;
  line-height: 36px;
  color: $color-text-secondary;
  border-bottom: 1px solid #e8eaec;
  margin-bottom: 10px;
}
</style>
