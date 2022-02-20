<template>
  <div class="form-single-fragment">
    <el-form ref="form" :model="formData" class="full-width-input" :rules="rules"
      :label-width="formConfig.labelWidth + 'px'" :size="defaultFormItemSize"
      :label-position="formConfig.labelPosition"
      @submit.native.prevent>
      <el-row :gutter="formConfig.gutter">
        <template v-for="widget in formConfig.formWidgetList">
          <CustomTable :ref="widget.variableName"
            v-if="widget.widgetType === SysCustomWidgetType.Table" :key="widget.id"
            :widgetConfig="widget"
            :formType="formConfig.formType"
            :primaryColumnName="widget.primaryColumnName"
            :flowData="flowData"
            :isNew="true"
            :getTableQueryParams="getTableQueryParams"
            @operationClick="onTableOperationClick"
          />
          <CustomUpload :ref="widget.variableName" :key="widget.id"
            v-else-if="widget.widgetType === SysCustomWidgetType.Upload"
            :widgetConfig="widget"
            :flowData="flowData"
            :getDataId="getWidgetPrimaryColumnId"
            v-model="formData[getWidgetFieldName(widget)]" />
          <CustomWidget
            v-else
            :ref="widget.variableName"
            :key="widget.id"
            :widgetConfig="widget"
            :formConfig="formConfig"
            :getDropdownParams="getDropdownParams"
            v-model="formData[getWidgetFieldName(widget)]"
          >
            <template v-for="subWidget in widget.childWidgetList">
              <CustomTable :ref="subWidget.variableName"
                v-if="subWidget.widgetType === SysCustomWidgetType.Table" :key="subWidget.id"
                :widgetConfig="subWidget"
                :formType="formConfig.formType"
                :isNew="true"
                :getTableQueryParams="getTableQueryParams"
                @operationClick="onTableOperationClick"
              />
              <CustomUpload :ref="subWidget.variableName" :key="subWidget.id"
                v-else-if="subWidget.widgetType === SysCustomWidgetType.Upload"
                :widgetConfig="subWidget"
                :flowData="flowData"
                :getDataId="getWidgetPrimaryColumnId"
                v-model="formData[getWidgetFieldName(subWidget)]" />
              <CustomWidget
                v-else
                :ref="subWidget.variableName"
                :key="subWidget.id"
                :widgetConfig="subWidget"
                :formConfig="formConfig"
                :getDropdownParams="getDropdownParams"
                v-model="formData[getWidgetFieldName(subWidget)]"
              />
            </template>
          </CustomWidget>
        </template>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { OnlineFormMixins } from './onlineFormMixins.js';
import CustomUpload from '@/views/onlineForm/components/customUpload.vue';
import CustomWidget from '@/views/onlineForm/components/customWidget.vue';
import CustomTable from '@/views/onlineForm/components/customTable.vue';

export default {
  name: 'workflowForm',
  props: {
    readOnly: {
      type: [String, Boolean]
    }
  },
  inject: ['preview'],
  mixins: [OnlineFormMixins],
  components: {
    CustomWidget,
    CustomTable,
    CustomUpload
  },
  data () {
    return {
      flowData: {}
    }
  },
  methods: {
    getWidgetFieldName (widget) {
      if (widget && widget.relation == null) {
        return (widget.column || {}).columnName;
      } else {
        return widget.relation.variableName + '__' + (widget.column || {}).columnName
      }
    },
    getDropdownParams (widget) {
      if (Array.isArray(widget.dictParamList)) {
        let params = {};
        for (let i = 0; i < widget.dictParamList.length; i++) {
          let dictParam = widget.dictParamList[i];
          if (dictParam.dictValue == null || dictParam.dictValueType == null) continue;
          params = this.getParamValueObj(dictParam.dictParamName, dictParam.dictValueType, dictParam.dictValue, params);
          if (params == null) return null;
        }

        return params;
      } else {
        return {};
      }
    },
    getTableQueryParams (widget) {
      return {};
    },
    onTableOperationClick (operation, row, widget) {
      this.handlerOperation(operation, row, widget);
    },
    setFormData (formData, oneToManyData, flowData) {
      this.flowData = flowData;
      this.formData = {
        ...this.formData,
        ...formData
      }
      this.setOneToManyRelationData(oneToManyData);
    },
    setOneToManyRelationData (oneToManyRelationList) {
      if (oneToManyRelationList != null) {
        Object.keys(oneToManyRelationList).forEach(relationVariableName => {
          if (Array.isArray(this.tableWidgetList)) {
            for (let i = 0; i < this.tableWidgetList.length; i++) {
              let tableWidget = this.tableWidgetList[i];
              if (tableWidget.relation.variableName === relationVariableName) {
                let temp = this.$refs[tableWidget.variableName];
                let tableImpl = Array.isArray(temp) ? temp[0] : temp;
                if (tableImpl) {
                  tableImpl.getTableWidget().dataList = oneToManyRelationList[relationVariableName];
                }
                break;
              }
            }
          }
        });
      }
    },
    getVariableData (variableList) {
      let variableData;
      if (Array.isArray(variableList) && variableList.length > 0) {
        variableList.forEach(variable => {
          if (!variable.builtin) {
            let column = this.columnMap.get(variable.bindColumnId);
            let relation = this.relationMap.get(variable.bindRelationId);
            if (column != null) {
              if (variableData == null) variableData = {};
              let key = relation == null ? '' : relation.variableName + '__';
              key += column.columnName;
              variableData[variable.variableName] = this.formData[key];
            }
          }
        });
      }
      return variableData;
    },
    getFormData () {
      return new Promise((resolve, reject) => {
        this.$refs.form.validate(valid => {
          if (!valid) return reject();
          let tempObj = {};
          let that = this;
          let oneToOneRelationObj = {};
          let hasMasterData = false;
          function getFlowWidgetData (widget) {
            if (widget == null || widget.readOnly || widget.disabled) return;
            if (widget.relation != null) {
              if (tempObj.slaveData == null) tempObj.slaveData = {};
              if (widget.widgetType === that.SysCustomWidgetType.Table) {
                let tableData = that.getRelationTableData(widget);
                if (tableData != null) {
                  tempObj.slaveData[widget.relation.relationId] = tableData;
                }
              } else {
                oneToOneRelationObj[widget.relation.relationId] = widget.relation;
              }
            } else {
              hasMasterData = true;
            }

            if (Array.isArray(widget.childWidgetList)) {
              widget.childWidgetList.forEach(subWidget => {
                getFlowWidgetData(subWidget);
              });
            }
          }
          // 一对多关联数据
          if (Array.isArray(this.formConfig.formWidgetList)) {
            this.formConfig.formWidgetList.forEach(widget => {
              getFlowWidgetData(widget);
            });
          }
          // 主表数据
          if (hasMasterData && this.masterTable) {
            this.masterTable.columnList.forEach(column => {
              if (tempObj.masterData == null) tempObj.masterData = {};
              tempObj.masterData[column.columnName] = this.formData[column.columnName];
            });
          }
          // 一对一关联数据
          Object.keys(oneToOneRelationObj).forEach(relationId => {
            let relation = oneToOneRelationObj[relationId];
            if (relation && relation.slaveTable && Array.isArray(relation.slaveTable.columnList)) {
              relation.slaveTable.columnList.forEach(column => {
                let value = that.formData[relation.variableName + '__' + (column || {}).columnName];
                if (tempObj.slaveData[relationId] == null) tempObj.slaveData[relationId] = {};
                tempObj.slaveData[relationId][column.columnName] = value;
              });
            }
          });

          resolve(tempObj);
        });
      });
    }
  },
  computed: {
    formReadOnly () {
      return this.readOnly === 'true' || this.readOnly;
    }
  }
}

</script>

<style scoped>
</style>
