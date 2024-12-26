<template>
  <div class="form-single-fragment third-party-dlg" style="position: relative">
    <el-form
      ref="form"
      :model="formData"
      class="full-width-input form-box"
      :rules="rules"
      style="width: 100%"
      label-width="120px"
      :size="formItemSize"
      label-position="right"
      @submit.prevent
    >
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="Category Name" prop="FlowCategory.name">
            <el-input
              class="input-item"
              v-model="formData.FlowCategory.name"
              :clearable="true"
              placeholder="Process Category Name"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="Category Code" prop="FlowCategory.code">
            <el-input
              class="input-item"
              v-model="formData.FlowCategory.code"
              :disabled="isEdit"
              :clearable="true"
              placeholder="Category Code"
            />
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="Display Order" prop="FlowCategory.showOrder">
            <el-input-number
              class="input-item"
              v-model="formData.FlowCategory.showOrder"
              :clearable="true"
              controls-position="right"
              placeholder="Display Order"
            />
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row class="no-scroll flex-box menu-box" type="flex" justify="end">
      <el-button :size="formItemSize" :plain="true" @click="onCancel"> Cancel </el-button>
      <el-button type="primary" :size="formItemSize" @click="onSubmit()"> Save </el-button>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { FormItemRule, ElMessage } from 'element-plus';
import { Arrayable } from 'element-plus/es/utils/typescript';
import FlowCategoryController from '@/api/flow/FlowCategoryController';
import { DialogProp } from '@/components/Dialog/types';
import { ANY_OBJECT } from '@/types/generic';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  categoryId?: string;
  // When using Dialog.show to pop up the component, this prop is required for callback on dialog
  dialog?: DialogProp<ANY_OBJECT>;
}
const props = defineProps<IProps>();
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});

const form = ref();

const formData = reactive<ANY_OBJECT>({
  FlowCategory: {
    categoryId: undefined,
    name: undefined,
    code: undefined,
    showOrder: undefined,
    isDatasourceInit: false,
  },
});

const formEditFlowCategory = reactive<ANY_OBJECT>({
  formFilter: {},
  formFilterCopy: {},
  menuBlock: {
    isInit: false,
  },
  isInit: false,
});

const rules: Partial<Record<string, Arrayable<FormItemRule>>> = {
  'FlowCategory.code': [
    {
      required: true,
      message: 'Please enter the category code',
      trigger: 'blur',
    },
    {
      type: 'string',
      pattern: /^[A-Za-z0-9]+$/,
      message: 'Category code can only contain letters and numbers',
      trigger: 'blur',
    },
  ],
  'FlowCategory.showOrder': [
    {
      required: true,
      message: 'Please enter the display order',
      trigger: 'blur',
    },
    {
      type: 'integer',
      message: 'Display order only allows integers',
      trigger: 'blur',
      transform: (value: string) => Number(value),
    },
    {
      type: 'number',
      min: 0,
      message: 'Display order must be greater than 0',
      trigger: 'blur',
      transform: (value: string) => Number(value),
    },
  ],
  'FlowCategory.name': [
    {
      required: true,
      message: 'Please enter the process category name',
      trigger: 'blur',
    },
  ],
};

const dialogParams = computed(() => {
  return {
    categoryId: props.categoryId || thirdParams.value.categoryId,
  };
});
const isEdit = computed(() => {
  return dialogParams.value.categoryId != null && dialogParams.value.categoryId !== '';
});

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    onCloseThirdDialog(false);
  }
};

/**
 * Save
 */
const onSubmit = () => {
  form.value.validate((valid: boolean) => {
    if (!valid) return;
    if (
      formData.FlowCategory.name == null ||
      formData.FlowCategory.code == null ||
      formData.FlowCategory.showOrder == null
    ) {
      ElMessage.error('Request failed, found required parameters empty!');
      return;
    }
    let params = {
      flowCategoryDto: {
        categoryId: isEdit.value ? dialogParams.value.categoryId : undefined,
        name: formData.FlowCategory.name,
        code: formData.FlowCategory.code,
        showOrder: formData.FlowCategory.showOrder,
      },
    };

    let httpCall = isEdit.value
      ? FlowCategoryController.update(params)
      : FlowCategoryController.add(params);
    httpCall
      .then(res => {
        ElMessage.success('Save success');
        if (props.dialog) {
          props.dialog.submit(res);
        } else {
          onCloseThirdDialog(true);
        }
      })
      .catch(e => {
        console.warn(e);
      });
  });
};

/**
 * Reset form data
 */
const resetFormData = () => {
  form.value.resetFields();
};

/**
 * Get FlowCategory detailed information
 */
const loadFlowCategoryData = () => {
  return new Promise((resolve, reject) => {
    if (!formData.FlowCategory.isDatasourceInit && isEdit.value) {
      if (dialogParams.value.categoryId == null) {
        resetFormData();
        reject();
        return;
      }
      let params = { categoryId: dialogParams.value.categoryId };
      FlowCategoryController.view(params)
        .then(res => {
          formData.FlowCategory = {
            ...res.data,
            isDatasourceInit: true,
          };
          resolve(res);
        })
        .catch(e => {
          reject(e);
        });
    } else {
      resolve(null);
    }
  });
};

/**
 * Update edit flow category
 */
const refreshFormEditFlowCategory = () => {
  loadFlowCategoryData()
    .then(() => {
      if (!formEditFlowCategory.isInit) {
        // Initialize dropdown data
        console.log('');
      }
      formEditFlowCategory.isInit = true;
    })
    .catch(e => {
      console.warn(e);
    });
};

const formInit = () => {
  refreshFormEditFlowCategory();
};

onMounted(() => {
  // Initialize page data
  formInit();
});
</script>
