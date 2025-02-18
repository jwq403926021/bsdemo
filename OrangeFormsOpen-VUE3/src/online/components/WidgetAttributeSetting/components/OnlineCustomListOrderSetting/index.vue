<template>
  <MultiItemBox
    :data="value"
    addText="添加排序字段"
    :supportSort="true"
    @add="onEditTableColumn()"
    @edit="onEditTableColumn"
    @delete="onRemoveTableColumn"
    :prop="{
      label: 'showName',
      value: 'id',
    }"
  />
</template>
<script setup lang="ts">
import { ElMessageBox } from 'element-plus';
import MultiItemBox from '@/components/MultiItemBox/index.vue';
import { ANY_OBJECT } from '@/types/generic';
import { Dialog } from '@/components/Dialog';
import { SysOnlineFormType } from '@/common/staticDict';
import EditCustomListOrder from './editCustomListOrder.vue';

const emit = defineEmits<{ 'update:value': [ANY_OBJECT[]] }>();
const props = withDefaults(defineProps<{ value: ANY_OBJECT[] }>(), {});
const formConfig = inject('formConfig', () => {
  console.error('OnlineCustomListOrderSetting: formConfig not injected');
  return {} as ANY_OBJECT;
});

const handlerEditOperate = (row: ANY_OBJECT | null, res: ANY_OBJECT) => {
  let tempList: ANY_OBJECT[] = [];
  if (row == null) {
    tempList = props.value || [];
    tempList.push(res);
  } else {
    tempList = (props.value || []).map(item => {
      return item.columnId === res.id ? res : item;
    });
  }
  emit('update:value', tempList);
};
const onEditTableColumn = (row: ANY_OBJECT | null = null) => {
  let tableList = formConfig().getTableWidgetTableList;
  // 非查询页面只能从组件绑定的表中选择
  if (formConfig().form.formType !== SysOnlineFormType.QUERY) {
    tableList = (tableList || []).filter((table: ANY_OBJECT) => {
      return table.tableId === formConfig().currentWidget.bindData.tableId;
    });
  }
  Dialog.show<ANY_OBJECT>(
    row ? '编辑排序字段' : '添加排序字段',
    EditCustomListOrder,
    {
      area: '500px',
    },
    {
      rowData: row,
      tableList: tableList,
      usedColumnList: props.value.map(item => item.columnId),
      path: 'thirdEditOnlineListOrder',
    },
    {
      width: '500px',
      height: '500px',
      pathName: '/thirdParty/thirdEditOnlineListOrder',
    },
  )
    .then(res => {
      handlerEditOperate(row, res);
    })
    .catch(e => {
      console.warn(e);
    });
};
const onRemoveTableColumn = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('是否删除此排序字段？', '', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      emit(
        'update:value',
        (props.value || []).filter(item => item !== row),
      );
    })
    .catch(e => {
      console.warn(e);
    });
};
</script>
