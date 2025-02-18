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
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="数据表" prop="tableId">
            <el-select
              class="input-item"
              v-model="formData.tableId"
              :clearable="true"
              filterable
              placeholder="数据表"
            >
              <el-option
                v-for="item in dialogParams.tableList"
                :key="item.tableId"
                :value="item.tableId"
                :label="item.tableName"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="过滤字段" prop="columnId">
            <el-select
              class="input-item"
              v-model="formData.columnId"
              :clearable="true"
              filterable
              placeholder="过滤字段"
            >
              <el-option
                v-for="item in columnList"
                :key="item.columnId"
                :value="item.columnId"
                :label="item.columnName"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="过滤类型" prop="operatorType">
            <el-select
              class="input-item"
              v-model="formData.operatorType"
              :clearable="true"
              filterable
              placeholder="过滤类型"
              @change="onOperationTypeChange"
            >
              <el-option
                v-for="item in SysOnlineFilterOperationType.getList()"
                :key="item.id"
                :value="item.id"
                :label="item.name"
              />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item label="过滤参数值" prop="value">
            <el-select
              v-if="filterColumn != null && filterColumn.dictInfo != null"
              v-model="formData.value"
              :disabled="!valueEnabled"
              placeholder=""
            >
              <el-option
                v-for="item in dictValueList"
                :key="item.id"
                :value="item.id"
                :label="item.name"
              />
            </el-select>
            <template v-else>
              <el-switch
                v-if="filterColumn != null && filterColumn.objectFieldType === 'Boolean'"
                :disabled="!valueEnabled"
                v-model="formData.value"
              />
              <el-input-number
                v-if="
                  filterColumn != null &&
                  ['Integer', 'Long', 'BigDecimal', 'Double'].indexOf(
                    filterColumn.objectFieldType,
                  ) !== -1
                "
                :disabled="!valueEnabled"
                v-model="formData.value"
                :controls="false"
              />
              <el-input
                v-else
                :disabled="filterColumn == null || !valueEnabled"
                v-model="formData.value"
                clearable
              />
            </template>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
    <el-row class="menu-box">
      <el-col :span="24">
        <el-row class="no-scroll flex-box" type="flex" justify="end">
          <el-button :size="formItemSize" :plain="true" @click="onCancel"> 取消 </el-button>
          <el-button type="primary" :size="formItemSize" @click="onSubmit()"> 保存 </el-button>
        </el-row>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { OnlineColumnController } from '@/api/online';
import { SysOnlineFilterOperationType } from '@/common/staticDict/online';
import { findItemFromList } from '@/common/utils';
import { DialogProp } from '@/components/Dialog/types';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
import { useDict } from '../../hooks/useDict';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  rowData?: ANY_OBJECT;
  tableList: Array<ANY_OBJECT>;
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT>;
}
const props = defineProps<IProps>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize;
});

const { thirdParams, onCloseThirdDialog } = useThirdParty(props);

const form = ref();
const formData = ref<ANY_OBJECT>({
  tableId: undefined,
  columnId: undefined,
  operatorType: SysOnlineFilterOperationType.EQUAL,
  value: undefined as string & number,
  dictValueName: undefined,
});
const columnList = ref<ANY_OBJECT[]>([]);
const dictValueList = ref<ANY_OBJECT[]>([]);
const rules = {};

const valueEnabled = computed(() => {
  let temp = [SysOnlineFilterOperationType.NOT_NULL, SysOnlineFilterOperationType.IS_NULL];
  return formData.value.operatorType != null && temp.indexOf(formData.value.operatorType) === -1;
});
const dialogParams = computed(() => {
  return {
    tableList: props.tableList || thirdParams.value.tableList,
    rowData: props.rowData || thirdParams.value.rowData,
  };
});
const filterTable = computed(() => {
  return findItemFromList(dialogParams.value.tableList, formData.value.tableId, 'tableId');
});
const filterColumn = computed(() => {
  return findItemFromList(columnList.value, formData.value.columnId, 'columnId');
});

const { getDictDataList } = useDict();

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
    let temp = findItemFromList(dictValueList.value, formData.value.value, 'id');
    formData.value.dictValueName = temp ? temp.name : undefined;
    let data = {
      ...formData.value,
      table: filterTable.value,
      column: filterColumn.value,
    };
    if (props.dialog) {
      props.dialog.submit(data);
    } else {
      onCloseThirdDialog(true, dialogParams.value.rowData, formData.value);
    }
  });
};

const loadOnlineTableColumnList = (tableId: string) => {
  if (!tableId) return;

  let params = {
    onlineColumnDtoFilter: {
      tableId,
    },
  };

  OnlineColumnController.list(params)
    .then(res => {
      //console.log('OnlineColumnController.list', res);
      //console.log('OnlineColumnController.list columnId', formData.value.columnId);
      columnList.value = res.data.dataList;
      if (filterTable.value) {
        filterTable.value.columnList = res.data.dataList;
      }
    })
    .catch(e => {
      console.warn(e);
    });
};
const getDictValue = (dictInfo: ANY_OBJECT) => {
  if (dictInfo == null) return;
  getDictDataList(dictInfo, {})
    .then(res => {
      dictValueList.value = res;
    })
    .catch(e => {
      console.warn(e);
    });
};
const onOperationTypeChange = (value: number) => {
  let temp = [SysOnlineFilterOperationType.NOT_NULL, SysOnlineFilterOperationType.IS_NULL];
  if (value == null || temp.indexOf(value) !== -1) {
    formData.value.value = undefined;
  }
};

watch(
  () => filterTable.value,
  (table: ANY_OBJECT) => {
    columnList.value = [];
    if (table != null) {
      if (Array.isArray(table.columnList)) {
        columnList.value = table.columnList;
      } else {
        loadOnlineTableColumnList(table.tableId);
      }
    }
  },
  {
    deep: true,
  },
);
watch(
  () => filterColumn.value,
  (column: ANY_OBJECT) => {
    dictValueList.value = [];
    formData.value.dictValueName = undefined;
    if (column != null) {
      if (column.dictInfo != null) {
        getDictValue(column.dictInfo);
      }
    } else {
      // 该语句会导致“过滤字段”第一次不能选中对应的数据
      //formData.value.columnId = undefined;
    }
  },
  {
    deep: true,
  },
);

onMounted(() => {
  if (dialogParams.value.rowData) {
    formData.value = {
      ...dialogParams.value.rowData,
    };
  }
});
</script>
