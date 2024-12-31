<template>
  <!-- Terminate Process -->
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      style="width: 100%"
      label-width="80px"
      :size="itemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="Terminate Reason" prop="reason">
            <el-input
              class="input-item"
              v-model="formData.reason"
              type="textarea"
              :clearable="true"
              placeholder="Terminate Reason"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row class="no-scroll flex-box menu-box" type="flex" justify="end">
      <el-button :size="itemSize" :plain="true" @click="onCancel"> Cancel </el-button>
      <el-button type="primary" :size="itemSize" @click="onSubmit"> Save </el-button>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { FlowOperationController } from '@/api/flow';
import { DialogProp } from '@/components/Dialog/types';
import { ANY_OBJECT } from '@/types/generic';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  processInstanceId?: string;
  taskId?: string;
  // When using Dialog.show to pop up the component, this prop must be defined for callback of dialog
  dialog?: DialogProp<boolean>;
}
const props = defineProps<IProps>();
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);
const itemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});

const form = ref();
const formData = ref<ANY_OBJECT>({
  reason: undefined,
});
const rules = {
  reason: [
    {
      required: true,
      message: 'Please enter the termination reason',
      trigger: 'blur',
    },
  ],
};

const dialogParams = computed(() => {
  return {
    processInstanceId: props.processInstanceId || thirdParams.value.processInstanceId,
    taskId: props.taskId || thirdParams.value.taskId,
  };
});

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
    FlowOperationController.stopProcessInstance({
      processInstanceId: dialogParams.value.processInstanceId,
      taskId: dialogParams.value.taskId,
      stopReason: formData.value.reason,
    })
      .then(() => {
        if (props.dialog) {
          props.dialog.submit(true);
        } else {
          onCloseThirdDialog(true);
        }
      })
      .catch(e => {
        console.warn(e);
      });
  });
};
</script>
