<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative; height: 100%">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      style="width: 100%"
      label-width="100px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-form-item label="Key" prop="key">
        <el-input v-model="formData.key" clearable :disabled="dialogParams.data != null" />
      </el-form-item>
      <el-form-item label="绑定值类型" prop="type">
        <el-select v-model="formData.type" clearable placeholder="请选择绑定值类型">
          <el-option
            :label="AutoTaskValueType.getValue(AutoTaskValueType.FIXED)"
            :value="AutoTaskValueType.FIXED"
          />
          <el-option
            :label="AutoTaskValueType.getValue(AutoTaskValueType.VARIABLE)"
            :value="AutoTaskValueType.VARIABLE"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="绑定值" prop="srcValue">
        <!-- 固定值 -->
        <el-input
          v-if="formData.type === AutoTaskValueType.FIXED"
          v-model="formData.srcValue"
          clearable
          placeholder="请输入绑定值"
        />
        <!-- 流程变量 -->
        <el-input
          v-else-if="formData.type === AutoTaskValueType.VARIABLE"
          v-model="formData.srcValue"
          clearable
          placeholder="请输入流程变量"
        />
      </el-form-item>
    </el-form>
    <el-row class="no-scroll flex-box menu-box" type="flex" justify="end" style="padding-top: 10px">
      <el-button :size="layoutStore.defaultFormItemSize" :plain="true" @click="onCancel()">
        取消
      </el-button>
      <el-button type="primary" :size="layoutStore.defaultFormItemSize" @click="onSubmit()">
        保存
      </el-button>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { ANY_OBJECT } from '@/types/generic';
import { AutoTaskValueType } from '@/common/staticDict/flow';
import { findTreeNodePath } from '@/common/utils';
import { DialogProp } from '@/components/Dialog/types';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';

const layoutStore = useLayoutStore();
interface IProps extends ThirdProps {
  data?: ANY_OBJECT;
  flowVariableList?: ANY_OBJECT[];
  usedKeyList?: string[];
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT | ANY_OBJECT[] | undefined>;
}
const props = withDefaults(defineProps<IProps>(), {
  data: undefined,
  flowVariableList: undefined,
  usedKeyList: undefined,
});
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);
const form = ref();

type FormDataType = {
  key: string;
  type: number | string;
  srcValue: string | string[];
};
const formData = ref<FormDataType>({
  key: '',
  type: AutoTaskValueType.FIXED,
  srcValue: '',
});
const rules = {
  key: [{ required: true, message: '请输入Key', trigger: 'blur' }],
  srcValue: [{ required: true, message: '请输入Value', trigger: 'blur' }],
};
const dialogParams = computed(() => {
  return {
    data: props.data || thirdParams.value.data,
    flowVariableList: props.flowVariableList || thirdParams.value.flowVariableList,
    usedKeyList: props.usedKeyList || thirdParams.value.usedKeyList,
  };
});
const finalUsedKeyList = computed(() => {
  return dialogParams.value.data
    ? dialogParams.value.usedKeyList.filter(item => item !== dialogParams.value.data.key)
    : dialogParams.value.usedKeyList;
});

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    onCloseThirdDialog(false);
  }
};

const onSubmit = () => {
  if ((dialogParams.value.usedKeyList || []).includes(formData.value.key)) {
    ElMessage.error('Key已存在');
    return;
  }
  form.value.validate(valid => {
    if (valid) {
      let srcValue: string | string[] | undefined;
      srcValue = formData.value.srcValue;
      let tempData = {
        ...formData.value,
        srcValue,
      };
      if (props.dialog) {
        props.dialog.submit(tempData);
      } else {
        onCloseThirdDialog(true, dialogParams.value.data, tempData);
      }
    }
  });
};

onMounted(() => {
  if (dialogParams.value.data) {
    let srcValue: string | undefined;
    srcValue = dialogParams.value.data.srcValue;
    formData.value = {
      ...formData.value,
      ...dialogParams.value.data,
      srcValue: srcValue,
    };
  }
});
</script>

<style></style>
