<template>
  <div class="dept-select">
    <el-select
      :model-value="value"
      style="width: 100%"
      :multiple="multiple"
      :disabled="disabled"
      :size="size"
      :clearable="clearable"
      :collapse-tags="collapseTags"
      :placeholder="placeholder"
      :teleported="false"
      popper-class="dept-select-popper"
      @visible-change="onVisibleChange"
      @remove-tag="onRemoveTag"
      @clear="onClear"
    >
      <el-option
        v-for="item in selectedItems"
        :key="item[pps.value]"
        :label="item[pps.label]"
        :value="item[pps.value]"
      />
    </el-select>
  </div>
</template>

<script setup lang="ts">
import { getUUID } from '@/common/utils';
import { ANY_OBJECT } from '@/types/generic';
import { SysCommonBizController } from '@/api/system';
import { Dialog } from '@/components/Dialog';
import DeptSelectDlg from './DeptSelectDlg.vue';

const emit = defineEmits<{ input: [ANY_OBJECT]; change: [ANY_OBJECT] }>();

const props = withDefaults(
  defineProps<{
    value: string | number | Array<ANY_OBJECT>;
    size?: '' | 'default' | 'small' | 'large';
    placeholder?: string;
    props?: ANY_OBJECT;
    multiple?: boolean;
    disabled?: boolean;
    clearable?: boolean;
    collapseTags?: boolean;
  }>(),
  {
    props: () => {
      return {
        label: 'deptName',
        value: 'deptId',
      };
    },
    multiple: false,
    disabled: false,
    clearable: false,
    collapseTags: true,
  },
);

const widgetId = ref(getUUID());
const selectedItems = ref<ANY_OBJECT[]>([]);
const pps = computed(() => props.props);

const refreshData = (data: ANY_OBJECT) => {
  if (data.path === 'thirdSelectDept/' + widgetId.value && data.isSuccess) {
    handlerEditOperate(data.data);
  }
};
const handlerEditOperate = (items: Ref<ANY_OBJECT>) => {
  console.log('DeptSelect > handlerEditOperate', items);
  selectedItems.value = [];
  if (props.multiple) {
    if (Array.isArray(items.value)) selectedItems.value = items.value;
  } else {
    if (items.value != null) selectedItems.value.push(items.value);
  }
  if (!checkSelectChange()) return;
  emitChange();
};
const onVisibleChange = (visible: boolean) => {
  if (visible) {
    Dialog.show<Ref<ANY_OBJECT>>(
      '部门选择',
      DeptSelectDlg,
      {
        area: ['900px', '650px'],
        offset: '100px',
      },
      {
        value: selectedItems.value,
        props: props.props,
        path: 'thirdSelectDept/' + widgetId.value,
        multiple: props.multiple,
      },
      {
        width: '900px',
        height: '650px',
        pathName: '/thirdParty/thirdSelectDept',
      },
    ).then(res => {
      handlerEditOperate(res);
    });
  }
};
const onRemoveTag = (val: ANY_OBJECT) => {
  selectedItems.value = selectedItems.value.filter(item => {
    return item[props.props.value] !== val;
  });
  emitChange();
};
const onClear = () => {
  selectedItems.value = [];
  emitChange();
};
const emitChange = () => {
  let tempValue;
  if (props.multiple) {
    tempValue = selectedItems.value.map(item => {
      return item[props.props.value];
    });
  } else {
    tempValue = (selectedItems.value[0] || {})[props.props.value];
  }
  emit('input', tempValue);
  emit('change', tempValue);
};
const checkSelectChange = () => {
  let valueIdString =
    props.multiple && Array.isArray(props.value)
      ? (props.value || [])
          .sort((val1: ANY_OBJECT, val2: ANY_OBJECT) => {
            if (val1 === val2) return 0;
            return val1 < val2 ? -1 : 1;
          })
          .join(',')
      : props.value || '';
  let selectedItemsString = selectedItems.value
    .sort((item1, item2) => {
      if (item1[props.props.value] === item2[props.props.value]) return 0;
      return item1[props.props.value] < item2[props.props.value] ? -1 : 1;
    })
    .map(item => item[props.props.value])
    .join(',');
  return valueIdString !== selectedItemsString;
};
const getSelectDeptList = () => {
  let params: ANY_OBJECT = {
    widgetType: 'upms_dept',
  };
  if (props.value == null || props.value === '' || props.value.length <= 0)
    selectedItems.value = [];
  if (props.multiple) {
    params.fieldValues = Array.isArray(props.value) ? props.value : [];
  } else {
    params.fieldValues = Array.isArray(props.value) ? props.value[0] : props.value;
    params.fieldValues = params.fieldValues == null ? [] : [params.fieldValues];
  }
  if (Array.isArray(params.fieldValues) && params.fieldValues.length > 0) {
    params.fieldValues = params.fieldValues.join(',');
    SysCommonBizController.viewByIds(params, {
      showMask: false,
    })
      .then(res => {
        if (Array.isArray(res.data)) {
          selectedItems.value = res.data;
        }
      })
      .catch(e => {
        console.warn(e);
      });
  }
};
watch(
  () => props.value,
  () => {
    if (props.value) getSelectDeptList();
  },
  {
    immediate: true,
  },
);

defineExpose({
  refreshData,
});
</script>

<style scoped>
.dept-select :deep(.dept-select-popper) {
  display: none;
}

.dept-select :deep(.el-dialog__header) {
  height: 42px;
  line-height: 42px;
  padding: 0 20px;
  background-color: #f8f8f8;
}
.dept-select :deep(.el-dialog__title) {
  font-size: 14px;
  color: #333;
}
.dept-select :deep(.el-dialog__headerbtn) {
  top: 12px;
}
.dept-select :deep(.el-dialog__body) {
  padding: 25px;
}
</style>
