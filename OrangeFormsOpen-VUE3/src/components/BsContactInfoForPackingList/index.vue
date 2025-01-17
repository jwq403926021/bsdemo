<template>
  <div class="contact-info">
    <div class="contact-info-title">Contact Info for Packing List</div>
    <!-- <ul>
      <li v-if="has412" style="line-height: 24px">Shipment: {{ Shipment }}</li>
    </ul> -->
  </div>
</template>

<script setup lang="ts">
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
const step = inject('step');
const formInject = inject('form');
const widgetList = inject('widgetList');
const Shipment = ref('');

const has412 = ref(false);
watch(
  () => step.value,
  () => {
    const form = formInject();
    const shipToWidget = form.widgetList.find(i => i.widgetType === 412);
    has412.value = !!shipToWidget;
    if (shipToWidget && widgetList[shipToWidget.variableName].getValue) {
      const shipToInfo = widgetList[shipToWidget.variableName].getValue();
      Shipment.value = shipToInfo.valueHuman;
    }
  },
);
</script>

<style scoped lang="scss">
.contact-info{
  margin-top: 10px;
}
.contact-info-title {
  font-size: 18px;
  color: $color-primary;
  margin-bottom: 10px
}
.contact-info-item {
  font-size: 14px;
  line-height: 36px;
  color: $color-primary;
  margin-bottom: 8px;
}
</style>