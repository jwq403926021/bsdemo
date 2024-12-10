<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      style="width: 100%"
      label-width="100px"
      :size="formItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row>
        <el-col :span="24">
          <el-form-item label="Field Name" prop="fieldName">
            <el-input
              class="input-item"
              v-model="formData.fieldName"
              :clearable="true"
              placeholder="Form custom field name"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row class="no-scroll flex-box menu-box" type="flex" justify="end">
      <el-button :size="formItemSize" :plain="true" @click="onCancel"> Cancel </el-button>
      <el-button type="primary" :size="formItemSize" @click="onSubmit"> Save </el-button>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { DialogProp } from '@/components/Dialog/types';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  rowData: ANY_OBJECT | null;
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT>;
}
const props = defineProps<IProps>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);

const form = ref();
const formData = ref<ANY_OBJECT>({
  fieldName: undefined,
});
const rules = {
  fieldName: [
    {
      required: true,
      message: 'Field name cannot be empty',
      trigger: 'blur',
    },
  ],
};

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    onCloseThirdDialog(false);
  }
};

const onSubmit = () => {
  form.value.validate((valid: boolean) => {
    if (!valid) return;
    if (props.dialog) {
      props.dialog.submit(formData.value.fieldName);
    } else {
      onCloseThirdDialog(true, dialogParams.value.rowData, formData.value.fieldName);
    }
  });
};

const dialogParams = computed(() => {
  return {
    rowData: props.rowData || thirdParams.value.rowData,
  };
});

onMounted(() => {
  if (dialogParams.value.rowData) {
    formData.value = { ...dialogParams.value.rowData };
  }
});
</script>
