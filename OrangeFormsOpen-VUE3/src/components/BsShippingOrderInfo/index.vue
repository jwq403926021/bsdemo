<template>
  <div>
    <h2 style="margin-bottom: 10px">Shipping Order Info</h2>
    <ul>
      <li style="line-height: 24px" v-for="item in data">
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
const formInject = inject('form');
const widgetList = inject('widgetList');
const data = ref([]);
const step = inject('step');
watch(
  () => step.value,
  () => {
    const result = [];
    const form = formInject();
    console.log('form:', form);
    form.widgetList.forEach(i => {
      const includes = [404, 405, 407, 408];
      if (includes.includes(i.widgetType)) {
        console.log(i.variableName, ':', widgetList[i.variableName]);
        let widgetValueObj = {};
        if (widgetList[i.variableName].getValue) {
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
  },
);
</script>
