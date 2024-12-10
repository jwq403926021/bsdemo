<template>
  <el-form
    label-position="top"
    :size="layoutStore.defaultFormItemSize"
    @submit.prevent
    style="width: 100%"
  >
    <el-form-item label="Step 1 Label">
      <el-input v-model="formConfig().form.step1Name" placeholder="" clearable filterable />
    </el-form-item>
    <el-form-item label="Step 2 Label">
      <el-input v-model="formConfig().form.step2Name" placeholder="" clearable filterable />
    </el-form-item>
    <el-form-item label="Step 3 Label">
      <el-input v-model="formConfig().form.step3Name" placeholder="" clearable filterable />
    </el-form-item>
    <el-form-item label="Form ID">
      <el-row justify="space-between" align="middle" style="flex-wrap: nowrap; width: 100%">
        <el-input readonly :model-value="formConfig().form.formId" />
        <el-button
          class="formShareBtn"
          link
          :icon="Share"
          style="margin-left: 8px"
          :title="
            formConfig().form.formType !== SysOnlineFormType.QUERY &&
            formConfig().form.formType !== SysOnlineFormType.ADVANCE_QUERY
              ? 'Only Query Forms Are Allowed'
              : 'Copy Access Information'
          "
          :disabled="
            formConfig().form.formType !== SysOnlineFormType.QUERY &&
            formConfig().form.formType !== SysOnlineFormType.ADVANCE_QUERY
          "
          :data-clipboard-text="shareInfo"
          @click="onShareForm"
        />
      </el-row>
    </el-form-item>
    <el-form-item label="Form Type">
      <el-input v-model="(formConfig().form.formTypeDictMap || {}).name" placeholder="" disabled />
    </el-form-item>
    <el-form-item label="Form Name">
      <el-input v-model="formConfig().form.formName" placeholder="" clearable filterable />
    </el-form-item>
    <el-form-item label="Form Code">
      <el-input v-model="formConfig().form.formCode" placeholder="" clearable filterable />
    </el-form-item>
    <el-form-item label="Label Position">
      <el-row type="flex" align="middle" style="width: 100%; height: 40px">
        <el-radio-group v-model="formConfig().form.labelPosition" size="default">
          <el-radio-button value="left">Left</el-radio-button>
          <el-radio-button value="right">Right</el-radio-button>
          <el-radio-button value="top">Top</el-radio-button>
        </el-radio-group>
      </el-row>
    </el-form-item>
    <el-form-item label="Label Width">
      <el-row type="flex" style="width: 100%">
        <el-input-number
          v-model="formConfig().form.labelWidth"
          style="width: 100%"
          controls-position="right"
        />
      </el-row>
    </el-form-item>
    <el-form-item label="Gutter">
      <el-row type="flex" style="width: 100%">
        <el-input-number
          v-model="formConfig().form.gutter"
          style="width: 100%"
          controls-position="right"
        />
      </el-row>
    </el-form-item>
    <el-form-item
      label="Filter Component Width"
      v-if="
        formConfig().form.formType === SysOnlineFormType.QUERY ||
        formConfig().form.formType === SysOnlineFormType.ADVANCE_QUERY
      "
    >
      <el-row type="flex" style="width: 100%">
        <el-input-number
          v-model="formConfig().form.filterItemWidth"
          style="width: 100%"
          controls-position="right"
        />
      </el-row>
    </el-form-item>
    <el-form-item label="Full Screen" v-if="supportDialog">
      <el-row type="flex" align="middle" style="width: 100%; height: 40px">
        <el-radio-group v-model="formConfig().form.fullscreen" size="default">
          <el-radio :value="true">Full Screen</el-radio>
          <el-radio :value="false">Pop Up</el-radio>
        </el-radio-group>
      </el-row>
    </el-form-item>
    <el-form-item v-if="supportDialog && !formConfig().form.fullscreen" label="Popup Width">
      <el-row type="flex" style="width: 100%">
        <el-input-number
          v-model="formConfig().form.width"
          style="width: 100%"
          controls-position="right"
        />
      </el-row>
    </el-form-item>
    <el-form-item v-if="supportDialog && !formConfig().form.fullscreen" label="Popup Height">
      <el-row type="flex" style="width: 100%">
        <el-input-number
          v-model="formConfig().form.height"
          style="width: 100%"
          controls-position="right"
        />
      </el-row>
    </el-form-item>
    <MultiItemList
      v-model:data="formConfig().form.customFieldList"
      label="Custom Fields"
      :supportSort="true"
      @add="onEditFormField(null)"
      @edit="onEditFormField"
      @delete="onRemoveFormField"
      :prop="{
        label: 'fieldName',
        value: 'fieldName',
      }"
    >
    </MultiItemList>
  </el-form>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { Share } from '@element-plus/icons-vue';
import Clipboard from 'clipboard';
import { computed } from 'vue';
import { ANY_OBJECT } from '@/types/generic';
import MultiItemList from '@/components/MultiItemList/index.vue';
import { SysOnlineFormType } from '@/common/staticDict/index';
import { SysOnlineFieldKind } from '@/common/staticDict/online';
import { Dialog } from '@/components/Dialog';
import { findItemFromList } from '@/common/utils';
import { useLayoutStore } from '@/store';
import EditFormField from './EditFormField.vue';

const formConfig = inject('formConfig', () => {
  console.error('CustomFormSetting: formConfig not injected');
  return {} as ANY_OBJECT;
});

const layoutStore = useLayoutStore();

// TODO Save Changes
const handlerEditOperate = (row: ANY_OBJECT | null, fieldName: string) => {
  if (Array.isArray(formConfig().form.customFieldList)) {
    if (row == null) {
      let temp = findItemFromList(formConfig().form.customFieldList, fieldName, 'fieldName');
      if (temp != null) {
        ElMessage.error('This Field Already Exists!');
      } else {
        formConfig().form.customFieldList.push({
          fieldName: fieldName,
        });
      }
    } else {
      if (row.fieldName !== fieldName) {
        // Modified Field Name
        let temp = findItemFromList(formConfig().form.customFieldList, fieldName, 'fieldName');
        if (temp != null) {
          ElMessage.error('This Field Already Exists!');
        } else {
          formConfig().form.customFieldList = formConfig().form.customFieldList.map(
            (item: ANY_OBJECT) => {
              if (item.fieldName === row.fieldName) {
                return {
                  fieldName: fieldName,
                };
              } else {
                return {
                  ...item,
                };
              }
            },
          );
        }
      }
    }
  } else {
    formConfig().form.customFieldList = [
      {
        fieldName: fieldName,
      },
    ];
  }
};
const onEditFormField = (row: ANY_OBJECT | null) => {
  Dialog.show<string>(
    row ? 'Edit Field' : 'Add Field',
    EditFormField,
    {
      area: '600px',
    },
    {
      rowData: row,
      //path: 'thirdEditFormField',
    },
    {
      height: '200px',
      width: '600px',
      pathName: '/thirdParty/thirdEditFormField',
    },
  )
    .then(fieldName => {
      handlerEditOperate(row, fieldName);
    })
    .catch(e => {
      console.warn(e);
    });
};
const onRemoveFormField = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Are You Sure You Want to Remove This Field?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      formConfig().form.customFieldList = formConfig().form.customFieldList.filter(
        (item: ANY_OBJECT) => {
          return item !== row;
        },
      );
    })
    .catch(e => {
      console.warn(e);
    });
};
const shareInfo = computed(() => {
  let shareUrl = {
    url:
      window.location.origin + '/#/thirdParty/thirdOnlineForm?formId=' + formConfig().form.formId,
    datasourceVariableName:
      formConfig().getMasterTable == null
        ? undefined
        : formConfig().getMasterTable.tag.variableName,
  };

  return JSON.stringify(shareUrl);
});
const onShareForm = () => {
  let clipboard = new Clipboard('.formShareBtn');
  clipboard.on('success', () => {
    ElMessage.success('Access Information Copied Successfully!');
    clipboard.destroy();
  });
  clipboard.on('error', () => {
    ElMessage.error('Browser Does Not Support Copying, Please Manually Copy Access Information: ' + shareInfo.value);
    clipboard.destroy();
  });
};

const supportDialog = computed(() => {
  return (
    // Editing Forms Supports Popups
    formConfig().form.formType === SysOnlineFormType.FORM ||
    // Subtable Query Pages Support Popups
    (formConfig().form.formType === SysOnlineFormType.QUERY &&
      formConfig().getMasterTable &&
      formConfig().getMasterTable.relationType != null)
  );
});
const getMaskTableList = computed(() => {
  if (formConfig().form.formType === SysOnlineFormType.FORM) {
    return formConfig().getAllTableList.filter((table: ANY_OBJECT) => {
      if (formConfig().getMasterTable.relationType == null) {
        // Main Table Editing
        return true;
      } else {
        // Subtable Editing
        return table.tableId === formConfig().getMasterTable.tableId;
      }
    });
  } else {
    return formConfig().getValidTableList;
  }
});
const getAllTableMaskColumnTree = computed(() => {
  console.log('getMaskTableList', getMaskTableList);
  return getMaskTableList.value
    .map((table: ANY_OBJECT) => {
      let maskColumnList = table.columnList
        .filter((column: ANY_OBJECT) => {
          return column.fieldKind === SysOnlineFieldKind.FIELD_MASK;
        })
        .map((column: ANY_OBJECT) => {
          return {
            id: column.columnId,
            name: column.columnName,
          };
        });
      if (maskColumnList.length > 0) {
        return {
          id: table.tableId,
          name: table.tableName,
          children: maskColumnList,
        };
      }
      return null;
    })
    .filter((item: ANY_OBJECT) => item != null);
});
</script>
