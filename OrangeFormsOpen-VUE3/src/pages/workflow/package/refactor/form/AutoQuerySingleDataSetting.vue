<template>
  <div class="autotask-query-single-settting">
    <el-form-item label="源表">
      <el-row :gutter="8" style="width: 100%">
        <el-col :span="12">
          <!-- 源表数据库链接选择 -->
          <el-select
            v-model="formData.srcDblinkId"
            clearable
            placeholder="请选择数据库链接"
            @change="onSrcDblinkChange"
          >
            <el-option
              v-for="item in flowDblinkList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-col>
        <el-col :span="12">
          <!-- 源表选择 -->
          <el-select
            v-model="formData.srcTableName"
            clearable
            filterable
            placeholder="请选择数据源表"
            @change="onSrcTableChange"
          >
            <el-option v-for="item in srcTableList" :key="item" :label="item" :value="item" />
          </el-select>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="源表过滤类型">
      <el-radio-group v-model="formData.srcFilterType" @change="onChange">
        <el-radio label="field">字段过滤</el-radio>
        <el-radio label="sql">自定义SQL</el-radio>
      </el-radio-group>
    </el-form-item>
    <MultiItemList
      v-if="formData.srcFilterType === 'field'"
      label="源表过滤条件"
      :data="formData.srcFilterList"
      addText="添加"
      :disabled="formData.srcTableName == null || formData.srcTableName === ''"
      @add="onEditSrcFilter()"
      @edit="onEditSrcFilter"
      @delete="onDeleteSrcFilter"
      :prop="{
        label: 'filterColumnName',
        value: 'filterColumnName',
      }"
    >
      <template v-slot="scope">
        <span>{{ (scope.data || {}).filterColumnName }}</span>
        <span style="margin: 0px 10px">{{
          CriteriaFilterType.getValue((scope.data || {}).filterType)
        }}</span>
        <span style="margin: 0px 10px 0px 0px">{{
          AutoTaskValueType.getValue(scope.data.valueType)
        }}</span>
        <span>
          {{ (scope.data || {}).filterValue }}
        </span>
      </template>
    </MultiItemList>
    <el-input
      v-else
      v-model="formData.srcFilterSql"
      type="textarea"
      rows="5"
      placeholder="重要！过滤值使用流程变量时需要手动输入，变量名的两边一定不要添加任何引号，如：name = ${variableName}"
      @change="onChange"
    />
    <el-form-item label="查询字段">
      <el-select
        v-model="formData.selectFieldList"
        clearable
        filterable
        multiple
        placeholder="请选择查询字段"
        @change="onChange"
      >
        <el-option
          v-for="item in srcColumnList"
          :key="item.columnName"
          :label="item.columnName"
          :value="item.columnName"
        />
      </el-select>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus';
import { defineProps, defineEmits } from 'vue';
import EditSrcTableFilter from './editSrcTableFilter.vue';
import { CriteriaFilterType } from '@/common/staticDict/index';
import { Dialog } from '@/components/Dialog';
import { AutoTaskActionType, AutoTaskValueType } from '@/common/staticDict/flow';
import { ANY_OBJECT } from '@/types/generic';
import MultiItemList from '@/components/MultiItemList/index.vue';
import { FlowDblinkController } from '@/api/flow';

const emit = defineEmits(['update:modelValue', 'change']);
const props = defineProps<{
  modelValue: string;
  flowDblinkList: ANY_OBJECT[];
}>();
const flowEntry = inject('flowEntry', () => {
  return {} as ANY_OBJECT;
});
const formList = inject('formList', () => {
  return {} as ANY_OBJECT;
});
const prefix = inject('prefix');
const getAllAutoVariableList = inject('getAllAutoVariableList', () => {
  return [];
});

type FormDataType = {
  actionType?: number;
  srcDblinkId?: string;
  srcDblinkType?: string | number;
  srcTableName: string | null;
  srcFilterType: string;
  srcFilterList: ANY_OBJECT[];
  srcFilterSql: string;
  selectFieldList: string[];
};
const formData = ref<FormDataType>({
  actionType: AutoTaskActionType.QUERY_SINGLE_DATA,
  srcDblinkId: undefined,
  srcDblinkType: undefined,
  srcTableName: null,
  srcFilterType: 'field',
  srcFilterList: [],
  srcFilterSql: '',
  selectFieldList: [],
});
const srcColumnList = ref<ANY_OBJECT[]>([]);
const srcTableList = ref<string[]>([]);

const onChange = () => {
  let tempData = {
    ...formData.value,
  };
  emit('update:modelValue', JSON.stringify(tempData));
  emit('change', JSON.stringify(tempData));
};

const loadDblinkTableList = async dblinkId => {
  let res = await FlowDblinkController.listDblinkTables({
    dblinkId,
  });

  return Array.isArray(res.data) ? res.data.map(item => item.tableName) : [];
};

const getColumnList = () => {
  if (
    formData.value.srcDblinkId == null ||
    formData.value.srcTableName == null ||
    formData.value.srcTableName === '' ||
    formData.value.srcDblinkId === ''
  ) {
    srcColumnList.value = [];
    return;
  }
  FlowDblinkController.listDblinkTableColumns({
    dblinkId: formData.value.srcDblinkId,
    tableName: formData.value.srcTableName,
  }).then(res => {
    srcColumnList.value = res.data;
  });
};

const onSrcDblinkChange = val => {
  formData.value.srcTableName = '';
  formData.value.srcDblinkType = (props.flowDblinkList.find(item => item.id === val) || {}).type;
  formData.value.srcFilterSql = '';
  formData.value.srcFilterList = [];
  onChange();
};

const onSrcTableChange = () => {
  formData.value.srcFilterSql = '';
  formData.value.srcFilterList = [];
  onChange();
};

const updateSrcTableFilter = (data, res) => {
  if (data == null) {
    // 新建
    formData.value.srcFilterList.push(res);
  } else {
    // 编辑
    formData.value.srcFilterList = formData.value.srcFilterList.map(item => {
      return item.srcTableName === res.srcTableName ? res : item;
    });
  }
  onChange();
};

const onEditSrcFilter = (data?: ANY_OBJECT) => {
  Dialog.show(
    '编辑过滤字段',
    EditSrcTableFilter,
    {
      area: ['500px', '400px'],
    },
    {
      entryId: flowEntry().entryId,
      data,
      dblinkId: formData.value.srcDblinkId,
      tableName: formData.value.srcTableName,
      flowVariableList: getAllAutoVariableList(),
      path: 'thirdEditQuerySingleFilter',
    },
    {
      width: '500px',
      height: '400px',
      pathName: '/thirdParty/thirdEditTableFilter',
    },
  )
    .then(res => {
      updateSrcTableFilter(data, res);
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteSrcFilter = data => {
  ElMessageBox.confirm('是否删除此过滤字段？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      formData.value.srcFilterList = formData.value.srcFilterList.filter(
        item => item.filterColumnName !== data.filterColumnName,
      );
      onChange();
    })
    .catch(e => {
      console.log(e);
    });
};

watch(
  () => props.modelValue,
  val => {
    let taskInfo = val && val !== '' ? JSON.parse(val) : {};
    formData.value = {
      actionType: AutoTaskActionType.QUERY_SINGLE_DATA,
      srcFilterType: taskInfo.srcFilterType || 'field',
      srcFilterSql: taskInfo.srcFilterSql || '',
      srcDblinkId: taskInfo.srcDblinkId,
      srcTableName: taskInfo.srcTableName || '',
      srcDblinkType: taskInfo.srcDblinkType,
      srcFilterList: taskInfo.srcFilterList || [],
      selectFieldList: taskInfo.selectFieldList || [],
    };
  },
  { immediate: true },
);

watch(
  () => formData.value.srcDblinkId,
  val => {
    if (val) {
      loadDblinkTableList(val).then(tableList => {
        srcTableList.value = tableList;
      });
    } else {
      srcTableList.value = [];
    }
  },
);

watch(
  () => formData.value.srcTableName,
  () => {
    getColumnList();
  },
  { immediate: true },
);
</script>

<style scoped></style>
