<template>
  <div>
    <h2 style="margin-bottom: 10px;">Contact Info for Packing List</h2>
    <ul>
      <li v-if="has407" style="line-height: 24px;">Recipient: {{ Recipient }}</li>
      <li v-if="has407" style="line-height: 24px;">Phone: {{ Phone }}</li>
      <li v-if="has412" style="line-height: 24px;">Shipment: {{ Shipment }}</li>
    </ul>
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
const Phone = ref('');
const Recipient = ref('');
const Shipment = ref('');

const has407 = ref(false)
const has412 = ref(false)
watch(
  () => step.value,
  () => {
    const form = formInject();
    const contactInfoWidget = form.widgetList.find(i => i.widgetType === 407);
    has407.value = !!contactInfoWidget
    if (contactInfoWidget && widgetList[contactInfoWidget.variableName].getValue) {
      const contactInfo = widgetList[contactInfoWidget.variableName].getValue();
      Phone.value = contactInfo.telNo;
      Recipient.value = contactInfo.recipient;
    }
    const shipToWidget = form.widgetList.find(i => i.widgetType === 412);
    has412.value = !!shipToWidget
    if (shipToWidget && widgetList[shipToWidget.variableName].getValue) {
      const shipToInfo = widgetList[shipToWidget.variableName].getValue();
      Shipment.value = shipToInfo.valueHuman;
    }
  },
);
</script>
