<template>
  <div
    :class="{
      advance: formConfig && formConfig.formType === SysOnlineFormType.ADVANCE_QUERY,
    }"
    style="position: relative; width: 100; padding: 0 !important"
  >
    <OnlineQueryForm
      v-if="formConfig && formConfig.formType === SysOnlineFormType.QUERY"
      :height="mainContextHeight - 32 + 'px'"
      :formConfig="formConfig"
    />
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { SysOnlineFormType } from '@/common/staticDict';
import OnlineQueryForm from './OnlineQueryForm/index.vue';

const mainContextHeight = inject('mainContextHeight', 200);
const workflow = ref();
const formConfig = ref();

const getFormData = (isDraft: boolean, variableList: ANY_OBJECT[]) => {
  return workflow.value ? workflow.value.getFormData(isDraft, variableList) : Promise.reject();
};

defineExpose({
  getFormData,
});
onMounted(() => {
  formConfig.value = {
    formType: SysOnlineFormType.QUERY,
  };
});
</script>

<style scoped>
.advance {
  padding: 0 !important;
}
</style>
