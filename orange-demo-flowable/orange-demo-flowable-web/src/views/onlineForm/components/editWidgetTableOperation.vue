<template>
  <div class="form-single-fragment" style="position: relative;">
    <el-form ref="form" :model="formData" class="full-width-input" :rules="rules" style="width: 100%;"
      label-width="100px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
      <el-row>
        <el-col :span="24">
          <el-form-item label="操作名称" prop="name">
            <el-input class="input-item" v-model="formData.name"
              :clearable="true" placeholder="操作按钮名称" />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="!formData.rowOperation">
          <el-form-item label="按钮类型" prop="btnType">
            <el-select v-model="formData.btnType">
              <el-option label="primary" value="primary" />
              <el-option label="success" value="success" />
              <el-option label="warning" value="warning" />
              <el-option label="danger" value="danger" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="!formData.rowOperation">
          <el-form-item label="镂空模式" prop="btnType">
            <el-switch v-model="formData.plain" />
          </el-form-item>
        </el-col>
        <el-col :span="24" v-if="formData.rowOperation">
          <el-form-item label="操作按钮类名">
            <el-input class="input-item" v-model="formData.btnClass"
              :clearable="true" placeholder="操作按钮类名" />
          </el-form-item>
        </el-col>
        <el-col :span="24"
          v-if="formData.type === SysCustomWidgetOperationType.ADD ||
            formData.type === SysCustomWidgetOperationType.EDIT ||
            formData.type === SysCustomWidgetOperationType.CUSTOM"
        >
          <el-form-item label="操作表单">
            <el-select v-model="formData.formId" placeholder="选择操作按钮触发表单" clearable>
              <el-option v-for="form in getFormList" :key="form.formId"
                :label="form.formName" :value="form.formId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24" v-show="formData.type === SysCustomWidgetOperationType.EXPORT" style="margin-bottom: 20px;">
          <el-table ref="expoerColumnTable" :data="tableColumnTree" row-key="id" header-cell-class-name="table-header-gray" height="400px"
            :default-expand-all="true" @selection-change="onExportColumnChange"
          >
            <el-table-column type="selection" width="55px" :selectable="(row) => row.isColumn">
              <template slot="header">
                <span>AAA</span>
              </template>
            </el-table-column>
            <el-table-column type="index" width="55px" />
            <el-table-column label="导出字段" prop="variableName">
              <template slot-scope="scope">
                <span>{{scope.row.variableName}}</span>
                <el-tag style="margin-left: 10px;" size="mini" type="danger" v-if="scope.row.aggregationColumnId">聚合字段</el-tag>
                <el-tag style="margin-left: 10px;" size="mini" type="success" v-if="scope.row.isTable && scope.row.relationType != null">一对一关联</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="显示名称" prop="showName" width="250px">
              <template slot-scope="scope">
                <el-input v-if="scope.row.isColumn" size="mini" :disabled="!exportColumnIsSelected(scope.row)" v-model="scope.row.showName" />
              </template>
            </el-table-column>
            <el-table-column label="显示顺序" width="150px">
              <template slot-scope="scope">
                <el-input-number v-if="scope.row.isColumn" size="mini" :disabled="!exportColumnIsSelected(scope.row)" v-model="scope.row.showOrder" controls-position="right" />
              </template>
            </el-table-column>
          </el-table>
        </el-col>
        <el-col :span="24">
          <el-row class="no-scroll flex-box" type="flex" justify="end">
            <el-button type="primary" :size="defaultFormItemSize" :plain="true"
              @click="onCancel(false)">
              取消
            </el-button>
            <el-button type="primary" :size="defaultFormItemSize"
              @click="onSubmit()">
              保存
            </el-button>
          </el-row>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { findItemFromList } from '@/utils';
export default {
  props: {
    rowData: {
      type: Object
    },
    formList: {
      type: Array,
      required: true
    },
    tableList: {
      type: Array
    }
  },
  data () {
    return {
      formData: {
        type: this.SysCustomWidgetOperationType.CUSTOM,
        name: undefined,
        btnType: undefined,
        plain: false,
        enabled: true,
        builtin: false,
        rowOperation: true,
        btnClass: 'table-btn primary',
        formId: undefined,
        exportColumnList: []
      },
      tableColumnTree: [],
      exportColumn: [],
      rules: {
        name: [
          { required: true, message: '操作按钮名称不能为空', trigger: 'blur' }
        ]
      }
    }
  },
  methods: {
    onCancel (isSuccess) {
      if (this.observer != null) {
        if (this.formData.type === this.SysCustomWidgetOperationType.EXPORT) {
          this.formData.exportColumnList = (this.exportColumn || []).map(column => {
            return column ? {
              tableId: column.table.tableId,
              columnId: column.columnId,
              virtualColumnId: column.aggregationColumnId,
              showName: column.showName,
              showOrder: column.showOrder
            } : undefined;
          });
        } else {
          this.formData.exportColumnList = [];
        }
        this.observer.cancel(isSuccess, this.formData);
      }
    },
    onSubmit () {
      this.$refs.form.validate(valid => {
        if (!valid) return;
        this.onCancel(true);
      });
    },
    onExportColumnChange (value) {
      this.exportColumn = value;
    },
    exportColumnIsSelected (row) {
      if (Array.isArray(this.exportColumn)) {
        return this.exportColumn.indexOf(row) !== -1;
      } else {
        return false;
      }
    },
    getTableColumnTree () {
      this.tableColumnTree = [];
      let selectedRows = [];
      if (Array.isArray(this.tableList)) {
        this.tableColumnTree = this.tableList.map(item => {
          return {
            variableName: item.tableName,
            id: item.tableId,
            isTable: true,
            relationType: item.relationType,
            children: (item.columnList || []).map(column => {
              let columnInfo = findItemFromList(this.formData.exportColumnList, column.aggregationColumnId, 'virtualColumnId');
              if (columnInfo == null) columnInfo = findItemFromList(this.formData.exportColumnList, column.columnId, 'columnId');
              let temp = {
                ...column,
                id: column.aggregationColumnId || column.columnId,
                table: item,
                variableName: column.columnName,
                showName: (columnInfo || {}).showName || column.columnComment,
                showOrder: (columnInfo || {}).showOrder || 0,
                isColumn: true
              };
              if (columnInfo != null) selectedRows.push(temp);
              return temp;
            })
          }
        });
      }
      return selectedRows;
    }
  },
  computed: {
    getFormList () {
      return this.formList.filter(item => {
        if (this.formData.type === this.SysCustomWidgetOperationType.CUSTOM) {
          return item.formType === this.SysOnlineFormType.QUERY;
        } else {
          return item.formType === this.SysOnlineFormType.FORM;
        }
      });
    }
  },
  mounted () {
    this.exportColumn = [];
    if (this.rowData != null) {
      this.formData = {
        ...this.rowData
      }
      if (this.formData.type === this.SysCustomWidgetOperationType.EXPORT) {
        let selectedRows = this.getTableColumnTree();
        this.$nextTick(() => {
          selectedRows.forEach(row => {
            this.$refs.expoerColumnTable.toggleRowSelection(row);
          });
        });
      }
    }
  }
}
</script>

<style>
</style>
