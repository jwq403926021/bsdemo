<template>
  <el-form
    label-position="top"
    size="default"
    v-if="pagerSetting"
    style="padding: 10px 16px 0"
    @submit.prevent
  >
    <el-form-item class="view-attribute-item" label="Show">
      <el-switch v-model="pagerSetting.show" />
    </el-form-item>
    <el-form-item class="view-attribute-item" label="Size">
      <el-select v-model="pagerSetting.size" placeholder="">
        <el-option label="Default" value="default" />
        <el-option label="Small" value="small" />
      </el-select>
    </el-form-item>
    <el-form-item class="view-attribute-item" label="Background">
      <el-select v-model="pagerSetting.background" placeholder="">
        <el-option label="Button" value="1" />
        <el-option label="Text" value="0" />
      </el-select>
    </el-form-item>
    <el-form-item class="view-attribute-item" label="Align">
      <el-radio-group v-model="pagerSetting.align">
        <el-radio-button value="left">Left</el-radio-button>
        <el-radio-button value="center">Center</el-radio-button>
        <el-radio-button value="right">Right</el-radio-button>
      </el-radio-group>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';

const props = withDefaults(defineProps<{ fieldName?: string }>(), { fieldName: 'pagerSetting' });
const formConfig = inject('formConfig', () => {
  console.error('PivotTableTotalSetting: formConfig not injected');
  return {} as ANY_OBJECT;
});

const widget = computed(() => {
  return formConfig().currentWidget;
});
const pagerSetting = computed(() => {
  let pps = (widget.value || {}).props;
  if (pps) {
    return pps[props.fieldName];
  } else {
    return {};
  }
});
</script>
