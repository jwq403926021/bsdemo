<template>
  <el-form
    ref="form"
    :model="formData"
    :rules="rules"
    label-width="120px"
    :size="formItemSize"
    label-position="right"
    @submit.prevent
  >
    <el-row :gutter="20" class="full-width-input">
      <el-col :span="24">
        <el-form-item label="Country Code" prop="countryCode">
          <el-input v-model="formData.countryCode" placeholder="Country Code" />
        </el-form-item>
        <el-form-item label="Group Code" prop="groupCode">
          <el-select v-model="formData.groupCode" placeholder="Group Code">
            <el-option
              v-for="item in groupCodeList"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Group Name" prop="groupName">
          <el-input v-model="formData.groupName" placeholder="Group Name" />
        </el-form-item>
        <el-form-item label="Code Number" prop="codeNumber">
          <el-input v-model="formData.codeNumber" placeholder="Code Number" />
        </el-form-item>
        <el-form-item label="English" prop="english">
          <el-input v-model="formData.english" placeholder="English" />
        </el-form-item>
        <el-form-item label="Korean" prop="korean">
          <el-input v-model="formData.korean" placeholder="Korean" />
        </el-form-item>
        <el-form-item label="Chinese" prop="chinese">
          <el-input v-model="formData.chinese" placeholder="Chinese" />
        </el-form-item>
        <el-form-item label="Japanese" prop="japanese">
          <el-input v-model="formData.japanese" placeholder="Japanese" />
        </el-form-item>
        <el-form-item label="Sort" prop="sort">
          <el-input v-model="formData.sort" type="number" placeholder="Sort" />
        </el-form-item>
        <el-form-item label="Code 1" prop="attr1">
          <el-input v-model="formData.attr1" placeholder="Code 1" />
        </el-form-item>
        <el-form-item label="Code 1 Name" prop="attr1Name">
          <el-input v-model="formData.attr1Name" placeholder="Code 1 Name" />
        </el-form-item>
      </el-col>
    </el-row>
    <!-- 弹窗下发按钮栏，必须设置class为dialog-btn-layer -->
    <el-row type="flex" justify="end" class="dialog-btn-layer">
      <el-button :plain="true" @click="onCancel">Cancel</el-button>
      <el-button
        type="primary"
        @click="onSubmit"
        :disabled="
          !(
            checkPermCodeExist('formSysUser:fragmentSysUser:update') ||
            checkPermCodeExist('formSysUser:fragmentSysUser:add')
          )
        "
      >
        Confirm
      </el-button>
    </el-row>
  </el-form>
</template>

<script setup lang="ts">
import { Ref, computed, onMounted, reactive, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { DialogProp } from '@/components/Dialog/types';
import { usePermissions } from '@/common/hooks/usePermission';
import { SystemUserController } from '@/api/system';
import { ANY_OBJECT } from '@/types/generic';
import { Code } from '@/types/upms/user';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

const props = defineProps<{
  rowData?: Code;
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT>;
}>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize;
});
const { checkPermCodeExist } = usePermissions();

const form = ref();
const formData: Ref<Code> = ref({
  id: '',
  countryCode: '',
  groupCode: '',
  groupName: '',
  codeNumber: '',
  english: '',
  korean: '',
  chinese: '',
  japanese: '',
  useStatus: 1,
  sort: null,
  attr1: '',
  attr1Name: '',
});
const countryCodeList: ANY_OBJECT = ref([{ label: 'CN', value: 'CN' }]);
const groupCodeList: ANY_OBJECT = ref([{ label: 'UserType', value: 'UserType' }]);
const rules = ref({});

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  }
};
const onSubmit = () => {
  form.value.validate((valid: boolean) => {
    if (valid) {
      let params = {
        id: formData.value.id,
        countryCode: formData.value.countryCode,
        groupCode: formData.value.groupCode,
        groupName: formData.value.groupName,
        codeNumber: formData.value.codeNumber,
        english: formData.value.english,
        korean: formData.value.korean,
        chinese: formData.value.chinese,
        japanese: formData.value.japanese,
        useStatus: 1,
        sort: formData.value.sort,
        attr1: formData.value.attr1,
        attr1Name: formData.value.attr1Name,
      };
      let content = formData.value.id
        ? SystemUserController.editCode(params)
        : SystemUserController.addCode(params);
      content
        .then(res => {
          const message = formData.value.id ? 'Edit Success' : 'Add Success';
          ElMessage.success(message);
          props.dialog?.submit(res);
        })
        .catch(e => {
          console.warn(e);
        });
    }
  });
};
onMounted(() => {
  if (props.rowData != null) {
    formData.value = {
      ...props.rowData,
    };
  }
});
</script>
