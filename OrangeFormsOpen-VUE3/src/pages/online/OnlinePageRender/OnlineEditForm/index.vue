<template>
  <div class="online-edit-form" style="position: relative; height: 100%; min-height: 200px">
    <template v-if="dialogParams.fullscreen && !dialogParams.isEdit">
      <el-container>
        <el-main style="width: 100%; background: #f9f9f9">
          <el-row
            type="flex"
            justify="center"
            :style="{
              height:
                'calc(100vh - ' +
                (dialogParams.readOnly && (form.operationList || []).length <= 0 ? 122 : 192) +
                'px)',
            }"
          >
            <div
              style="width: 8000px; max-width: 90vw; height: 100%; padding: 15px; background: white"
            >
              <el-scrollbar style="height: 100%" class="custom-scroll">
                <el-form
                  ref="formRef"
                  :model="formData"
                  class="full-width-input"
                  :rules="rules"
                  style="width: 100%"
                  :label-width="(form.labelWidth || 200) + 'px'"
                  :label-position="form.labelPosition || 'right'"
                  :size="layoutStore.defaultFormItemSize"
                  @submit.prevent
                >
                  <OnlineCustomBlock
                    v-show="isReady"
                    ref="root"
                    v-model:value="form.widgetList"
                    height="100%"
                    :isEdit="dialogParams.isEdit"
                    :showBorder="false"
                    @widgetClick="onWidgetClick"
                  />
                </el-form>
              </el-scrollbar>
            </div>
          </el-row>
        </el-main>
        <el-footer
          v-if="!dialogParams.readOnly || (form.operationList || []).length > 0"
          style="background: white"
        >
          <el-row type="flex" justify="center" align="middle" style="height: 60px">
            <el-button
              v-for="operation in form.operationList"
              :key="operation.id"
              size="default"
              style="margin-right: 16px"
              :type="operation.btnType"
              :plain="operation.plain"
              @click="onOperationClick(operation)"
            >
              {{ operation.name || SysCustomWidgetOperationType.getValue(operation.type) }}
            </el-button>
            <el-button :disabled="active === 1" @click="previous">Previous</el-button>
            <el-button :disabled="active === 3" @click="next">Next</el-button>
            <el-button
              v-if="!dialogParams.readOnly"
              type="primary"
              :disabled="active != 3"
              size="default"
              style="margin-right: 16px"
              @click="onSubmit"
            >
              Submit
            </el-button>
            <el-button v-if="props?.dialog?.index" size="default" :plain="true" @click="onCancel">
              Back
            </el-button>
          </el-row>
        </el-footer>
      </el-container>
    </template>
    <template v-else>
      <div class="form-box" :style="{ 'min-height': dialogParams.isEdit ? height : '0px' }">
        <el-scrollbar style="height: 100%" class="custom-scroll">
          <component
            :is="mode === 'pc' ? ElForm : VanForm"
            ref="formRef"
            :model="formData"
            class="full-width-input"
            :rules="rules"
            style="width: 100%"
            :label-width="(form.labelWidth || 200) + 'px'"
            :label-position="form.labelPosition || 'right'"
            :size="layoutStore.defaultFormItemSize"
            @submit.prevent
          >
            <OnlineCustomBlock
              v-show="isReady"
              ref="root"
              v-model:value="form.widgetList"
              :height="height"
              :isEdit="dialogParams.isEdit"
              :showBorder="false"
              @widgetClick="onWidgetClick"
            />
          </component>
        </el-scrollbar>
      </div>
      <el-row v-if="!dialogParams.isEdit" class="menu-box" type="flex" justify="end">
        <el-button
          v-for="operation in form.operationList"
          :key="operation.id"
          :size="layoutStore.defaultFormItemSize"
          :type="operation.btnType"
          :plain="operation.plain"
          @click="onOperationClick(operation)"
        >
          {{ operation.name || SysCustomWidgetOperationType.getValue(operation.type) }}
        </el-button>
        <el-button
          v-if="!dialogParams.readOnly"
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          :plain="true"
          @click="onCancel"
        >
          取消
        </el-button>
        <el-button
          v-if="!dialogParams.readOnly"
          type="primary"
          :size="layoutStore.defaultFormItemSize"
          @click="onSubmit"
        >
          保存
        </el-button>
      </el-row>
    </template>
  </div>
</template>

<script setup lang="ts">
import { Close } from '@element-plus/icons-vue';
import { ElForm, ElMessage } from 'element-plus';
import { Form as VanForm } from 'vant';
import axios from 'axios';
import { ANY_OBJECT } from '@/types/generic';
import { OnlineFormEventType, SysCustomWidgetOperationType } from '@/common/staticDict';
import {
  SysOnlineColumnFilterType,
  SysOnlineFieldKind,
  SysOnlineRelationType,
} from '@/common/staticDict/online';
import { post, get } from '@/common/http/request';
import { TableData } from '@/common/http/types';
import OnlineCustomBlock from '@/online/components/OnlineCustomBlock.vue';
import { DialogProp } from '@/components/Dialog/types';
import { API_CONTEXT } from '@/api/config';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import widgetData from '@/online/config/index';
import { useLayoutStore, useLoginStore } from '@/store';
import { serverDefaultCfg } from '@/common/http/config';
import { eventbus } from '@/common/utils/mitt';
import { FlowEntryController } from '@/api/flow';
import { useDict } from '../../hooks/useDict';
import { useForm } from '../hooks/useForm';
import { useFormExpose } from '../hooks/useFormExpose';
import { useRoute } from 'vue-router';
const route = useRoute();

const loginStore = useLoginStore();

interface IProps extends ThirdProps {
  formConfig: ANY_OBJECT;
  height?: string;
  // 主表数据
  masterTableData?: ANY_OBJECT;
  // 是否表单编辑模式
  isEdit?: boolean;
  // 是否复制
  isCopy?: boolean;
  readOnly?: boolean;
  // 当前选中组件
  currentWidget?: ANY_OBJECT | null;
  // 需要编辑数据，如果是null则是新增
  rowData?: ANY_OBJECT;
  // 是否全屏弹窗
  fullscreen?: boolean;
  // 是否保存到数据库
  saveData?: boolean;
  mode?: string;
  // 当使用Dialog.show弹出组件时，须定义该prop属性，以便对dialog进行回调
  dialog?: DialogProp<ANY_OBJECT>;
}

const emit = defineEmits<{
  widgetClick: [ANY_OBJECT | null];
}>();

const props = withDefaults(defineProps<IProps>(), {
  isEdit: false,
  readOnly: false,
  fullscreen: false,
  saveData: true,
  mode: 'pc',
});

const layoutStore = useLayoutStore();
const { onCloseThirdDialog } = useThirdParty(props);
const active = ref(1);
const formRef = ref();
const previous = () => {
  if (active.value > 0) {
    active.value -= 1;
  }
};
const next = () => {
  if (active.value < 3) {
    active.value += 1;
  }
};

const { getDictDataList } = useDict();
const {
  isReady,
  rules,
  dialogParams,
  form,
  formData,
  masterTable,
  isRelation,
  tableWidgetList,
  richEditWidgetList,
  getWidgetValue,
  getWidgetVisible,
  onValueChange,
  onWidgetValueChange,
  getPrimaryData,
  getDropdownParams,
  checkOperationPermCode,
  checkOperationDisabled,
  checkOperationVisible,
  cloneWidget,
  handlerOperation,
  loadOnlineFormConfig,
  getTableData,
  setTableData,
  initPage,
  initFormWidgetList,
  initWidgetRule,
  initWidgetLinkage,
  onPrint,
} = useForm(props);
const bsWidgetList = {};
provide('widgetList', bsWidgetList);
provide('step', active);
provide('form', () => {
  return {
    ...form.value,
    mode: props.mode || 'pc',
    isEdit: dialogParams.value.isEdit,
    readOnly: dialogParams.value.readOnly,
    formData: formData,
    getWidgetValue: getWidgetValue,
    getWidgetVisible: getWidgetVisible,
    onValueChange: onValueChange,
    onWidgetValueChange: onWidgetValueChange,
    getTableData: getTableData,
    setTableData: setTableData,
    getPrimaryData: getPrimaryData,
    getDropdownParams: getDropdownParams,
    checkOperationPermCode: checkOperationPermCode,
    checkOperationDisabled: checkOperationDisabled,
    checkOperationVisible: checkOperationVisible,
    cloneWidget: cloneWidget,
    handlerOperation: handlerOperation,
    getDictDataList: getDictDataList,
    loadOnlineFormConfig: loadOnlineFormConfig,
    isActive: (widget: ANY_OBJECT) => {
      return props.currentWidget === widget;
    },
    getWidgetObject: widgetData.getWidgetObject,
    instanceData: () => useFormExpose(formData),
  };
});

const onInput = (widgetList: ANY_OBJECT[]) => {
  form.value.widgetList = widgetList;
};

const onWidgetClick = (widget: ANY_OBJECT | null) => {
  emit('widgetClick', widget);
};
const onOperationClick = (operation: ANY_OBJECT) => {
  let keyName;
  if (masterTable.value.relation == null) {
    keyName = masterTable.value.datasource.variableName;
  } else {
    keyName = masterTable.value.relation.variableName;
  }
  onPrint(operation, formData[keyName], null, form.value.formName + '.pdf');
};

const onCancel = () => {
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    onCloseThirdDialog(false);
  }
};
const getQueryParam = paramName => {
  const str = window.location.hash.substring(13) + '';
  const query = str; // 去掉开头的 ?
  const params = query.split('&');
  for (const param of params) {
    const [key, value] = param.split('=');
    if (key === paramName) {
      return decodeURIComponent(value || ''); // 解码参数值
    }
  }
  return null; // 参数不存在时返回 null
};
// 提交表单数据
const onSaveFormData = async () => {
  console.log("route==================", route)
  let params: ANY_OBJECT = {};
  for (const key in bsWidgetList) {
    if (bsWidgetList[key]?.getValue && typeof bsWidgetList[key].getValue === 'function') {
      const value = bsWidgetList[key]?.getValue() || {};
      console.log(`${key}::!@#!@#!@#!@#::`, value);
      params = {
        ...params,
        ...value,
      };
    }
  }
  console.log('all field::!@#!@#!@#!@#', params);
  params = {
    orderType: props.formConfig.formName,
    divisionsName: params.divisionsName,
    srName: params.srName,
    shipTo: params.shipTo,
    stockLocName: params.stockLocName,
    contactInfo: params.contactInfo,
    productUpn: params.products?.[0].productUpn,
    productName: params.products?.[0].productName,
    qty: params.products?.[0].selectedQty,
    recipient: params?.recipientModify ?? params.recipient,
    phone: params?.phoneModify ?? params.phone,
    deliveryDate: params.requestDeliveryDate,
    processDefinitionKey: dialogParams.value.formConfig.processId || '',
    orderDataType: getQueryParam('formId'),
  };
  console.log('real params::::', params);
  const res: ANY_OBJECT = await FlowEntryController.orderPlacement(params);
  console.log(res);
  if (res.status === 200) {
    onCancel();
    eventbus.emit('refreshTable');
  }
};
// 提交
const onSubmit = () => {
  if (dialogParams.value.isEdit) return;
  if (Array.isArray(richEditWidgetList)) {
    richEditWidgetList.forEach(richWidget => {
      if (richWidget && richWidget.widgetImpl) {
        onValueChange(richWidget, richWidget.widgetImpl.getHtml());
      }
    });
  }
  formRef.value.validate((valid: boolean) => {
    if (!valid) return;
    if (dialogParams.value.saveData) {
      // 非级联保存数据
      onSaveFormData().then(res => {
        ElMessage.success('Save success');
      });
    } else {
      if (props.dialog) {
        props.dialog.submit(formData);
      } else {
        onCloseThirdDialog(true, dialogParams.value.rowData, formData);
      }
    }
  });
};

const clearFormData = (data: ANY_OBJECT | null, columnList: string[]) => {
  if (data == null) return;
  columnList.forEach(columnName => {
    data[columnName] = undefined;
  });
};
const loadRelationTableData = (widget: ANY_OBJECT) => {
  if (widget == null || widget.datasource == null || widget.relation == null)
    return Promise.reject();
  return new Promise<void>((resolve, reject) => {
    let params: ANY_OBJECT = {
      datasourceId: widget.datasource.datasourceId,
      relationId: widget.relation.relationId,
      filterDtoList: [
        {
          tableName: widget.table.tableName,
          columnName: widget.relation.slaveColumn.columnName,
          filterType: SysOnlineColumnFilterType.EQUAL_FILTER,
          columnValue: (formData[widget.datasource.variableName] || {})[
            widget.relation.masterColumn.columnName
          ],
        },
      ],
    };
    if (params == null) {
      reject();
      return;
    }
    let httpCall = post<TableData<ANY_OBJECT>>(
      API_CONTEXT +
        '/online/onlineOperation/listByOneToManyRelationId/' +
        widget.datasource.variableName,
      params,
    );
    httpCall
      .then(res => {
        // 复制数据，清空主键id以及自动编码字段
        if (dialogParams.value.isCopy) {
          let temp = new Date().getTime();
          let autoCodeColumnName: string[] = [];
          res.data.dataList.forEach(item => {
            if (widget.primaryColumnName) item[widget.primaryColumnName] = undefined;
            item.__cascade_add_id__ = temp++;
            autoCodeColumnName.forEach(columnName => {
              item[columnName] = undefined;
            });
          });
        }
        formData[widget.relation.variableName] = res.data.dataList;
        resolve();
      })
      .catch(e => {
        reject(e);
      });
  });
};
const initFormData = () => {
  if (dialogParams.value.rowData != null) {
    return new Promise<void>((resolve, reject) => {
      // 如果是复制，清空主键以及自动编码字段
      let clearColumnList: string[] = [];
      if (isRelation.value) {
        Object.keys(dialogParams.value.rowData).forEach(key => {
          formData[masterTable.value.relation.variableName][key] = dialogParams.value.rowData[key];
        });
        clearFormData(formData[masterTable.value.relation.variableName], clearColumnList);
        resolve();
      } else {
        // 初始化主表以及一对一字段数据
        let relationNameList: string[] = [];
        let datasourceName = '';
        form.value.tableMap.forEach((table: ANY_OBJECT) => {
          if (table.relation && table.relation.relationType === SysOnlineRelationType.ONE_TO_ONE) {
            relationNameList.push(table.relation.variableName);
            table.columnList.forEach(column => {
              formData[table.relation.variableName][column.columnName] = undefined;
            });
          } else if (table.relation == null) {
            datasourceName = table.datasource.variableName;
          }
        });
        Object.keys(dialogParams.value.rowData).forEach(key => {
          if (relationNameList.indexOf(key) === -1) {
            // 主表字段
            formData[datasourceName][key] = dialogParams.value.rowData[key];
          } else {
            // 从表字段
            if (dialogParams.value.rowData[key]) {
              Object.keys(dialogParams.value.rowData[key]).forEach(subKey => {
                formData[key][subKey] = dialogParams.value.rowData[key][subKey];
              });
            }
          }
        });
        // 初始化一对多数据
        if (Array.isArray(tableWidgetList) && tableWidgetList.length > 0) {
          try {
            let httpCallList = tableWidgetList
              .map(widget => {
                if (
                  widget.relation &&
                  widget.relation.relationType === SysOnlineRelationType.ONE_TO_MANY
                ) {
                  return loadRelationTableData(widget);
                }
                return null;
              })
              .filter(item => item != null);
            Promise.all(httpCallList)
              .then(() => {
                clearFormData(formData[datasourceName], clearColumnList);
                resolve();
              })
              .catch(e => {
                console.log(e);
                clearFormData(formData[datasourceName], clearColumnList);
                reject(e);
              });
          } catch (e) {
            console.log(e);
          }
        } else {
          clearFormData(formData[datasourceName], clearColumnList);
          resolve();
        }
      }
    });
  } else {
    return Promise.resolve();
  }
};

const formExpose = useFormExpose(formData);

onMounted(() => {
  isReady.value = false;
  if (!dialogParams.value.isEdit) {
    initWidgetRule();
    initFormData()
      .then(() => {
        initWidgetLinkage();
        setTimeout(() => {
          formRef.value.clearValidate();
        });
      })
      .catch((e: Error) => {
        console.error(e);
      });
  }
  isReady.value = true;
});
</script>

<style scoped>
.header-logo {
  display: inline-block;
  width: 40px;
  height: 40px;
  margin-right: 8px;
  text-align: center;
  color: #1a457b;
  background: rgb(26 69 123 / 10%);
  border-radius: 8px;
  line-height: 40px;
}

.el-divider--vertical {
  height: 26px;
  margin: 7px 15px;
}

.online-edit-form {
  display: flex;
  flex-direction: column;
}

.online-edit-form .info {
  position: absolute;
  top: 30%;
  width: 100%;
  text-align: center;
  vertical-align: middle;
}

.form-box {
  flex-grow: 1;
  flex-shrink: 1;
  height: 300px;
}

.menu-box {
  flex-grow: 0;
  flex-shrink: 0;
}
</style>
