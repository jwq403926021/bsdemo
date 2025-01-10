<template>
  <el-config-provider :locale="en">
    <div class="edit-online-form">
      <el-container style="height: 100vh">
        <el-header class="step-header" style="height: 72px">
          <el-row type="flex" justify="space-between">
            <el-col class="title header" :span="6" style="height: auto; line-height: auto">
              <i
                style="font-size: 40px; color: #1a457b"
              />
            </el-col>
            <div>
              <StepBar class="step" :value="activeStep" style="margin-top: 14px">
                <StepBarItem icon="online-icon icon-basic-info" :name="SysFlowEntryStep.BASIC"
                  >Basic Information</StepBarItem
                >
                <StepBarItem
                  icon="online-icon icon-flow-design"
                  :name="SysFlowEntryStep.PROCESS_DESIGN"
                  >Process Design</StepBarItem
                >
              </StepBar>
            </div>
            <el-col :span="6">
              <el-row type="flex" justify="end" align="middle" style="height: 72px">
                <!-- <el-button
                  v-if="activeStep !== SysFlowEntryStep.PROCESS_DESIGN"
                  size="default"
                  @click="onPrevClick"
                  :disabled="activeStep === SysFlowEntryStep.BASIC"
                >
                  Previous
                </el-button> -->
                <el-button
                  type="primary"
                  v-if="activeStep !== SysFlowEntryStep.PROCESS_DESIGN"
                  size="default"
                  @click="onNextClick"
                  :disabled="activeStep === SysFlowEntryStep.PROCESS_DESIGN"
                  >Next</el-button
                >
                <el-button
                  v-if="activeStep === SysFlowEntryStep.PROCESS_DESIGN"
                  size="default"
                  type="primary"
                  @click="onSave"
                  >Save</el-button
                >
                <el-button
                  v-if="activeStep === SysFlowEntryStep.PROCESS_DESIGN"
                  size="default"
                  @click="onPrevClick"
                  >Return</el-button
                >
                <el-button
                  size="default"
                  @click="onClose"
                  >Exit</el-button
                >
              </el-row>
            </el-col>
          </el-row>
        </el-header>
        <el-main
          style="padding: 10px; background: #ebeef5"
          :style="{ height: clientHeight - 72 + 'px' }"
        >
          <el-row type="flex" justify="center" style="height: 100%">
            <!-- Process Basic Information Setting -->
            <el-col
              v-if="activeStep === SysFlowEntryStep.BASIC"
              class="main-box"
              style="min-width: 650px"
              :span="9"
            >
              <el-form
                ref="entryBasicInfo"
                class="full-width-input"
                size="default"
                :model="formFlowEntryData"
                :rules="formRules"
                label-position="right"
                label-width="150px"
                @submit.prevent
              >
                <el-row :gutter="16">
                  <el-col :span="24">
                    <el-form-item label="Process Name" prop="processDefinitionName">
                      <el-input v-model="formFlowEntryData.processDefinitionName" />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="Process Key" prop="processDefinitionKey">
                      <el-input
                        v-model="formFlowEntryData.processDefinitionKey"
                        :disabled="isEdit"
                      />
                    </el-form-item>
                  </el-col>
                  <el-col :span="24">
                    <el-form-item label="Process Category" prop="categoryId">
                      <el-select
                        v-model="formFlowEntryData.categoryId"
                        placeholder=""
                        :loading="categoryIdWidget.loading"
                        @visible-change="categoryIdWidget.onVisibleChange"
                      >
                        <el-option
                          v-for="item in categoryIdWidget.dropdownList"
                          :key="item.id"
                          :value="item.id"
                          :label="item.name"
                        />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </el-form>
            </el-col>
            <!-- Process Variable Setting -->
            <el-col
              v-if="activeStep === SysFlowEntryStep.PROCESS_VARIABLE"
              class="main-box"
              :span="16"
            >
              <vxe-table
                empty-text="No data"
                :data="processVariableList"
                header-cell-class-name="table-header-gray"
                key="processVariableList"
                :size="formItemSize"
                :row-config="{ isHover: true }"
              >
                <vxe-column title="Index" type="seq" width="70px" />
                <vxe-column title="Show Name" field="showName" />
                <vxe-column title="Variable Name" field="variableName" />
                <vxe-column title="Variable Type">
                  <template v-slot="scope">
                    <el-tag
                      :size="formItemSize"
                      effect="dark"
                      :type="
                        scope.row.variableType === SysFlowVariableType.TASK ? 'primary' : 'success'
                      "
                    >
                      {{ SysFlowVariableType.getValue(scope.row.variableType) }}
                    </el-tag>
                  </template>
                </vxe-column>
                <vxe-column title="Built-in Variable">
                  <template v-slot="scope">
                    <el-tag
                      :size="formItemSize"
                      effect="dark"
                      :type="scope.row.builtin ? 'success' : 'danger'"
                    >
                      {{ scope.row.builtin ? 'Yes' : 'No' }}
                    </el-tag>
                  </template>
                </vxe-column>
                <vxe-column title="Operation" width="120px">
                  <template v-slot="scope">
                    <el-button
                      :size="formItemSize"
                      type="primary"
                      link
                      :disabled="scope.row.builtin"
                      @click="editEntryVariable(scope.row)"
                      style="padding: 0"
                      >Edit</el-button
                    >
                    <el-button
                      :size="formItemSize"
                      type="danger"
                      link
                      :disabled="scope.row.builtin"
                      @click="deleteEntryVariable(scope.row)"
                      style="padding: 0"
                      >Delete</el-button
                    >
                  </template>
                </vxe-column>
                <template v-slot:empty>
                  <div class="table-empty unified-font">
                    <img src="@/assets/img/empty.png" />
                    <span>No Data</span>
                  </div>
                </template>
              </vxe-table>
              <el-button class="btn-add" :icon="Plus" @click="addEntryVariable" :size="formItemSize"
                >Add Variable</el-button
              >
            </el-col>
            <!-- Process Status Setting -->
            <el-col
              v-if="activeStep === SysFlowEntryStep.PROCESS_STATUS"
              class="main-box"
              :span="16"
            >
              <vxe-table
                empty-text="No data"
                :data="formFlowEntryData.extensionData.approvalStatusDict"
                :row-config="{ isHover: true }"
                header-cell-class-name="table-header-gray"
                key="processStatusList"
                :size="formItemSize"
              >
                <vxe-column title="Index" type="seq" width="70px" />
                <vxe-column title="Status Name" field="name" />
                <vxe-column title="Status Id" field="id" />
                <vxe-column title="Operation" width="120px">
                  <template v-slot="scope">
                    <el-button
                      :size="formItemSize"
                      type="primary"
                      link
                      :disabled="scope.row.builtin"
                      @click="editEntryStatus(scope.row)"
                      style="padding: 0"
                      >Edit</el-button
                    >
                    <el-button
                      :size="formItemSize"
                      type="danger"
                      link
                      :disabled="scope.row.builtin"
                      @click="deleteEntryStatus(scope.row)"
                      style="padding: 0"
                      >Delete</el-button
                    >
                  </template>
                </vxe-column>
                <template v-slot:empty>
                  <div class="table-empty unified-font">
                    <img src="@/assets/img/empty.png" />
                    <span>No Data</span>
                  </div>
                </template>
              </vxe-table>
              <el-button
                class="btn-add"
                :icon="Plus"
                :size="formItemSize"
                @click="editEntryStatus(null)"
                >New</el-button
              >
            </el-col>
            <!-- Process Design -->
            <el-col
              v-if="activeStep === SysFlowEntryStep.PROCESS_DESIGN"
              class="main-box"
              :span="24"
              style="min-width: 1100px; padding: 0"
            >
              <ProcessDesigner
                ref="designer"
                :flowEntryInfo="formFlowEntryData"
                @save="onSaveFlowEntry"
              />
            </el-col>
          </el-row>
        </el-main>
      </el-container>
    </div>
  </el-config-provider>
</template>

<script setup lang="ts">
import { VxeTable, VxeColumn } from 'vxe-table';
import { ElMessage, ElMessageBox, FormItemRule } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import Clipboard from 'clipboard';
import { Arrayable } from 'element-plus/es/utils/typescript';
import StepBar from '@/components/StepBar/index.vue';
import StepBarItem from '@/components/StepBar/stepItem.vue';
import {
  FlowEntryController,
  FlowEntryVariableController,
  FlowDictionaryController,
} from '@/api/flow';
import {
  OnlinePageController,
  OnlineFormController,
  OnlineColumnController,
  OnlineDatasourceRelationController,
  OnlineVirtualColumnController,
} from '@/api/online';
import {
  DiagramType,
  SysAutoCodeType,
  SysFlowEntryBindFormType,
  SysFlowEntryStep,
  SysFlowVariableType,
  FlowEntryType,
} from '@/common/staticDict/flow';
import { SysOnlineFormType } from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';
import { DialogProp } from '@/components/Dialog/types';
import { useDropdown } from '@/common/hooks/useDropdown';
import { DropdownOptions, ListData } from '@/common/types/list';
import { SysOnlinePageType, SysOnlineRelationType } from '@/common/staticDict/online';
import { TableData } from '@/common/types/table';
import { ResponseDataType } from '@/common/http/types';
import { Dialog } from '@/components/Dialog';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
import ProcessDesigner from '../components/ProcessDesigner.vue';
import EditFlowEntryStatus from './formEditFlowEntryStatus.vue';
import EditFlowEntryVariable from './formEditFlowEntryVariable.vue';
const layoutStore = useLayoutStore();
const defaultApprovalStatus = [
  { id: 1, name: 'Agree' },
  { id: 2, name: 'Reject' },
  { id: 3, name: 'Return' },
  { id: 4, name: 'Co-sign Agree' },
  { id: 5, name: 'Co-sign Reject' },
];
interface IProps extends ThirdProps {
  flowEntry?: ANY_OBJECT;
  // When using Dialog.show to pop up components, this prop must be defined for dialog callback
  dialog?: DialogProp<ANY_OBJECT>;
}
const props = defineProps<IProps>();
const { thirdParams, onCloseThirdDialog } = useThirdParty(props);
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});

const clientHeight = inject('documentClientHeight', 200);

const entryBasicInfo = ref();
const designer = ref();

const activeStep = ref(SysFlowEntryStep.BASIC);
const formFlowEntryData = ref<ANY_OBJECT>({
  entryId: undefined,
  processDefinitionName: undefined,
  processDefinitionKey: undefined,
  flowType: FlowEntryType.AUTO_TASK,
  categoryId: undefined,
  bindFormType: SysFlowEntryBindFormType.ROUTER_FORM,
  pageId: undefined,
  defaultFormId: undefined,
  defaultRouterName: undefined,
  bpmnXml: undefined,
  diagramType: DiagramType.ORDINARY,
  encodedRule: {
    calculateWhenView: false,
    prefix: undefined,
    precisionTo: SysAutoCodeType.DAYS,
    middle: undefined,
    idWidth: undefined,
  },
  extensionData: {},
  notifyTypes: undefined,
  cascadeDeleteBusinessData: false,
  supportRevive: false,
  keptReviveDays: undefined,
});
const formRules: Partial<Record<string, Arrayable<FormItemRule>>> = {
  processDefinitionKey: [
    { required: true, message: 'Process Key Cannot Be Empty!', trigger: 'blur' },
    {
      type: 'string',
      pattern: /^[A-Za-z0-9]+$/,
      message: 'Process Key Only Allows Letters and Numbers',
      trigger: 'blur',
    },
    {
      type: 'string',
      pattern: /^[A-Za-z][A-Za-z0-9]+$/,
      message: 'Process Key Cannot Start With a Number',
      trigger: 'blur',
    },
  ],
  processDefinitionName: [
    { required: true, message: 'Process Name Cannot Be Empty!', trigger: 'blur' },
  ],
  categoryId: [{ required: true, message: 'Process Category Cannot Be Empty!', trigger: 'blur' }],
  // flowType: [{ required: true, message: 'Process Type Cannot Be Empty!', trigger: 'blur' }],
  pageId: [{ required: true, message: 'Process Page Cannot Be Empty!', trigger: 'blur' }],
  defaultFormId: [
    {
      required: true,
      message: 'Default Online Form Cannot Be Empty!',
      trigger: 'blur',
    },
  ],
  defaultRouterName: [
    {
      required: true,
      message: 'Default Router Form Cannot Be Empty!',
      trigger: 'blur',
    },
  ],
};
const processVariableList = ref<ANY_OBJECT[]>([]);
const defaultOnlineFormList = ref<ANY_OBJECT[]>([]);
let entryDatasource: ANY_OBJECT | undefined | null = undefined;
// Get Process Category
const loadCategoryIdDropdownList = (): Promise<ListData<ANY_OBJECT>> => {
  return new Promise((resolve, reject) => {
    FlowDictionaryController.dictFlowCategory({})
      .then(res => {
        resolve({ dataList: res.getList() });
      })
      .catch(e => {
        reject(e);
      });
  });
};
const categoryIdDropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadCategoryIdDropdownList,
};
const categoryIdWidget = reactive(useDropdown(categoryIdDropdownOptions));
// Get Online Form List
const loadPageIdDropdownList = (): Promise<ListData<ANY_OBJECT>> => {
  return new Promise((resolve, reject) => {
    OnlinePageController.list({
      onlinePageDtoFilter: {
        pageType: SysOnlinePageType.FLOW,
      },
    })
      .then(res => {
        resolve({ dataList: res.data.dataList.filter(item => item.published) });
      })
      .catch(e => {
        reject(e);
      });
  });
};
const pageIdDropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadPageIdDropdownList,
};
const pageIdWidget = reactive(useDropdown(pageIdDropdownOptions));
// Get Default Form Page List
const loadDefaultFormIdDropdownList = (): Promise<ListData<ANY_OBJECT>> => {
  return new Promise((resolve, reject) => {
    if (formFlowEntryData.value.pageId == null || formFlowEntryData.value.pageId === '') {
      resolve({ dataList: [] });
      return;
    }
    OnlineFormController.list({
      onlineFormDtoFilter: {
        pageId: formFlowEntryData.value.pageId,
      },
      orderParam: [
        {
          fieldName: 'createTime',
          asc: true,
        },
      ],
    })
      .then(res => {
        defaultOnlineFormList.value = res.data.dataList || [];
        resolve({
          dataList: (res.data.dataList || []).filter(
            item => item.formType === SysOnlineFormType.FLOW,
          ),
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
const defaultFormIdDropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadDefaultFormIdDropdownList,
};
const defaultFormIdWidget = reactive(useDropdown(defaultFormIdDropdownOptions));
const dialogParams = computed(() => {
  return {
    flowEntry: props.flowEntry || thirdParams.value.flowEntry,
  };
});
const isEdit = computed(() => {
  return dialogParams.value.flowEntry != null || formFlowEntryData.value.entryId != null;
});
const shareFormList = computed(() => {
  return (defaultOnlineFormList.value || []).filter(
    item => item.formType === SysOnlineFormType.WORK_ORDER,
  );
});

const onPrevClick = () => {
  switch (activeStep.value) {
    case SysFlowEntryStep.PROCESS_VARIABLE:
      activeStep.value = SysFlowEntryStep.BASIC;
      break;
    case SysFlowEntryStep.PROCESS_STATUS:
      activeStep.value = SysFlowEntryStep.PROCESS_VARIABLE;
      break;
    case SysFlowEntryStep.PROCESS_DESIGN:
      if (formFlowEntryData.value.flowType === FlowEntryType.AUTO_TASK) {
        activeStep.value = SysFlowEntryStep.BASIC;
      } else {
        activeStep.value = SysFlowEntryStep.PROCESS_STATUS;
      }
      break;
  }
};
// Save Process Basic Information
const saveFlowEntryInfo = () => {
  if (formFlowEntryData.value.extensionData == null) {
    formFlowEntryData.value.extensionData = {};
  }
  if (!Array.isArray(formFlowEntryData.value.extensionData.approvalStatusDict)) {
    formFlowEntryData.value.extensionData['approvalStatusDict'] = [...defaultApprovalStatus];
  }
  formFlowEntryData.value.extensionData['notifyTypes'] = formFlowEntryData.value.notifyTypes;
  formFlowEntryData.value.extensionData['cascadeDeleteBusinessData'] =
    formFlowEntryData.value.cascadeDeleteBusinessData;
  formFlowEntryData.value.extensionData['supportRevive'] = formFlowEntryData.value.supportRevive;
  formFlowEntryData.value.extensionData['keptReviveDays'] = formFlowEntryData.value.keptReviveDays;
  let params = {
    flowEntryDto: {
      ...formFlowEntryData.value,
      diagramType:
        designer.value?.processDesigner?.diagramType || formFlowEntryData.value.diagramType,
      extensionData: JSON.stringify(formFlowEntryData.value.extensionData),
    },
  };
  console.log(
    'formEditFlowEntry.saveFlowEntryInfo params',
    params,
    'designer.value',
    designer.value,
  );

  return isEdit.value ? FlowEntryController.update(params) : FlowEntryController.add(params);
};
/**
 * Get Data Model Association Information
 */
const loadDatasourceRelation = (res: ResponseDataType<TableData<ANY_OBJECT>>) => {
  if (entryDatasource == null) return Promise.resolve(res);
  return OnlineDatasourceRelationController.list({
    onlineDatasourceRelationDtoFilter: {
      datasourceId: entryDatasource.datasourceId,
    },
  });
};
// Get Online Form Data Table Column List
const loadOnlineTableColumns = (
  tableId: string | null,
  owner: ANY_OBJECT,
): Promise<ResponseDataType<TableData<ANY_OBJECT>>> => {
  if (tableId == null || tableId === '') return Promise.reject();

  return new Promise((resolve, reject) => {
    let params = {
      onlineColumnDtoFilter: {
        tableId,
      },
    };

    OnlineColumnController.list(params)
      .then(res => {
        owner.columnList = res.data.dataList;
        resolve(res);
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * Get All Table Fields Under Data Source
 */
const loadDatasourceAllColumnList = (res: ResponseDataType<TableData<ANY_OBJECT>>) => {
  if (entryDatasource == null) return Promise.resolve(res);
  let allHttpCalls = [loadOnlineTableColumns(entryDatasource.masterTableId, entryDatasource)];
  entryDatasource.relationList.forEach((relation: ANY_OBJECT) => {
    if (relation.relationType === SysOnlineRelationType.ONE_TO_ONE) {
      allHttpCalls.push(loadOnlineTableColumns(relation.slaveTableId, relation));
    }
  });

  Promise.all(allHttpCalls).then(res => {
    return Promise.resolve(res);
  });
};
// Get Process Bound Page Data Source Information
const initFlowDatasourceInfo = () => {
  return new Promise((resolve, reject) => {
    if (entryDatasource != null || formFlowEntryData.value.flowType === FlowEntryType.AUTO_TASK) {
      return resolve(null);
    }
    if (formFlowEntryData.value.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM) {
      OnlinePageController.listOnlinePageDatasource({
        pageId: formFlowEntryData.value.pageId,
      })
        .then(res => {
          entryDatasource = res.data.dataList[0];
          return loadDatasourceRelation(res);
        })
        .then(res => {
          if (entryDatasource) {
            entryDatasource.relationList = res.data.dataList || [];
          } else {
            console.error('formEditFlowEntry.entryDatasource is null');
          }
          return loadDatasourceAllColumnList(res);
        })
        .then(() => {
          return OnlineVirtualColumnController.list({
            onlineVirtualColumnDtoFilter: {
              datasourceId: entryDatasource?.datasourceId,
            },
          });
        })
        .then(res => {
          let virtualColumnList = res.data.dataList;
          if (Array.isArray(virtualColumnList) && entryDatasource) {
            if (!Array.isArray(entryDatasource.columnList)) entryDatasource.columnList = [];
            entryDatasource?.columnList.push(
              ...virtualColumnList.map(item => {
                return {
                  ...item,
                  columnId: item.virtualColumnId,
                  columnName: item.objectFieldName,
                  columnComment: item.columnPrompt,
                };
              }),
            );
          }
          resolve(res);
        })
        .catch(e => {
          reject(e);
        });
    } else {
      entryDatasource = null;
      resolve(null);
    }
  });
};
const onSaveFlowEntryBasicInfo = () => {
  return saveFlowEntryInfo();
};
// Get Process Variable List
const loadEntryVariableList = () => {
  return new Promise((resolve, reject) => {
    let params = {
      flowEntryVariableDtoFilter: {
        entryId: formFlowEntryData.value.entryId || dialogParams.value.flowEntry?.entryId,
      },
    };

    FlowEntryVariableController.list(params)
      .then(res => {
        // VxeTable requires objects to have hasOwnerProperty method, need to reassemble objects
        processVariableList.value = res.data.dataList.map(item => {
          return { ...item };
        });
        resolve(res);
      })
      .catch(e => {
        reject(e);
      });
  });
};
const onNextClick = () => {
  switch (activeStep.value) {
    case SysFlowEntryStep.BASIC:
      entryBasicInfo.value.validate((valid: boolean) => {
        if (!valid) return;
        if (formFlowEntryData.value.encodedRule.calculateWhenView) {
          if (
            formFlowEntryData.value.encodedRule.precisionTo == null ||
            formFlowEntryData.value.encodedRule.precisionTo === ''
          ) {
            ElMessage.error('Work Order Encoding Precision Type!');
            return;
          }
          if (
            formFlowEntryData.value.encodedRule.idWidth == null ||
            formFlowEntryData.value.encodedRule.idWidth <= 0
          ) {
            ElMessage.error('Work Order Encoding ID Length Cannot Be Less Than 1!');
            return;
          }
        }
        // Save Basic Process Information
        onSaveFlowEntryBasicInfo()
          .then(res => {
            if (!isEdit.value) formFlowEntryData.value.entryId = res.data;
            ElMessage.success('Save Success');
            // Automation Task Directly Jump to Process Design Page
            if (formFlowEntryData.value.flowType === FlowEntryType.AUTO_TASK) {
              activeStep.value = SysFlowEntryStep.PROCESS_DESIGN;
              return;
            }
            // Get Process Page Data Source Information
            initFlowDatasourceInfo()
              .then(() => {
                // Get Process Variables
                return loadEntryVariableList();
              })
              .then(() => {
                activeStep.value = SysFlowEntryStep.PROCESS_VARIABLE;
              })
              .catch(e => {
                console.log(e);
              });
          })
          .catch(e => {
            console.log(e);
          });
      });
      break;
    case SysFlowEntryStep.PROCESS_VARIABLE:
      activeStep.value = SysFlowEntryStep.PROCESS_STATUS;
      break;
    case SysFlowEntryStep.PROCESS_STATUS:
      // Save Status Information
      onSaveFlowEntryBasicInfo()
        .then(() => {
          ElMessage.success('Save Success');
          activeStep.value = SysFlowEntryStep.PROCESS_DESIGN;
        })
        .catch(e => {
          console.log(e);
        });
      break;
  }
};

const onClose = () => {
  // this.$emit('close');
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    onCloseThirdDialog(true);
  }
};
const onSave = () => {
  designer.value.processDesigner.onSave();
};
// Process Binding Form Type Change
const onBindFormTypeChange = () => {
  formFlowEntryData.value.pageId = undefined;
  formFlowEntryData.value.defaultFormId = undefined;
  formFlowEntryData.value.defaultRouterName = undefined;
  entryDatasource = undefined;
};
// Process Binding Form Page Change
const onEntryPageChange = () => {
  defaultFormIdWidget.setDirty(true);
  formFlowEntryData.value.defaultFormId = undefined;
  entryDatasource = null;
};
const getFormShareInfo = (form: ANY_OBJECT) => {
  let shareInfo = {
    url:
      window.location.origin +
      '/#/thirdParty/thirdOnlineForm?formId=' +
      form.formId +
      '&entryId=' +
      formFlowEntryData.value.entryId,
    processDefinitionKey: formFlowEntryData.value.processDefinitionKey,
  };

  return JSON.stringify(shareInfo);
};

const handleShare = (formId: string) => {
  try {
    let clipboard = new Clipboard('.' + formId);
    clipboard.on('success', e => {
      ElMessage.success('Access Information Copied Successfully!');
      clipboard.destroy();
    });
    clipboard.on('error', e => {
      // TODO Not Found shareUrl Definition
      //ElMessage.error('Browser Does Not Support Copying, Please Manually Copy Access Information: ' + this.shareUrl);
      ElMessage.error('Browser Does Not Support Copying, Please Manually Copy Access Information');
      clipboard.destroy();
    });
  } catch (e) {
    console.log(e);
  }
};
const editEntryVariable = (row: ANY_OBJECT) => {
  Dialog.show(
    'Edit Variable',
    EditFlowEntryVariable,
    {
      area: '500px',
    },
    {
      entryId: formFlowEntryData.value.entryId || dialogParams.value.flowEntry.entryId,
      datasource: entryDatasource,
      rowData: row,
      path: 'thirdFormEditFlowEntryVariable',
    },
    {
      width: '500px',
      height: '260px',
      pathName: '/thirdParty/thirdFormEditFlowEntryVariable',
    },
  )
    .then(() => {
      loadEntryVariableList();
    })
    .catch((e: Error) => {
      console.warn(e);
    });
};
const deleteEntryVariable = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do You Want to Delete This Process Variable?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      let params = {
        variableId: row.variableId,
      };

      return FlowEntryVariableController.delete(params);
    })
    .then(() => {
      ElMessage.success('Delete Success!');
      loadEntryVariableList();
    })
    .catch(e => {
      console.warn(e);
    });
};
const addEntryVariable = () => {
  Dialog.show(
    'Add Variable',
    EditFlowEntryVariable,
    {
      area: '500px',
    },
    {
      entryId: formFlowEntryData.value.entryId || dialogParams.value.flowEntry.entryId,
      datasource: entryDatasource,
      path: 'thirdFormEditFlowEntryVariable',
    },
    {
      width: '500px',
      height: '260px',
      pathName: '/thirdParty/thirdFormEditFlowEntryVariable',
    },
  )
    .then(() => {
      loadEntryVariableList();
    })
    .catch((e: Error) => {
      console.warn(e);
    });
};
const editEntryStatus = (row: ANY_OBJECT | null) => {
  Dialog.show<ANY_OBJECT[]>(
    row ? 'Edit' : 'Add',
    EditFlowEntryStatus,
    {
      area: '500px',
    },
    {
      rowData: row,
      flowEntry: formFlowEntryData.value,
      path: 'thirdFormEditFlowEntryStatus',
    },
    {
      width: '500px',
      height: '220px',
      pathName: '/thirdParty/thirdFormEditFlowEntryStatus',
    },
  )
    .then(approvalStatusDict => {
      formFlowEntryData.value.extensionData.approvalStatusDict = [...approvalStatusDict];
    })
    .catch(e => {
      console.warn(e);
    });
};
const deleteEntryStatus = (row: ANY_OBJECT) => {
  console.log('formEditFlowEntry.deleteEntryStatus row', row);
  ElMessageBox.confirm('Do You Want to Delete This Process Status?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(res => {
      console.log(
        'FormEditFlowEntry.DeleteEntryStatus FormFlowEntryData.ExtensionData.ApprovalStatusDict',
        formFlowEntryData.value.extensionData.approvalStatusDict,
      );
      let approvalStatusDict = formFlowEntryData.value.extensionData.approvalStatusDict.filter(
        (item: ANY_OBJECT) => {
          return item.id !== row.id;
        },
      );
      console.log('FormEditFlowEntry.DeleteEntryStatus ApprovalStatusDict', approvalStatusDict);
      let params = {
        flowEntryDto: {
          ...formFlowEntryData.value,
          extensionData: JSON.stringify({
            approvalStatusDict,
          }),
        },
      };
      FlowEntryController.update(params)
        .then(res => {
          ElMessage.success('Delete Success!');
          formFlowEntryData.value.extensionData.approvalStatusDict = approvalStatusDict;
        })
        .catch(e => {
          console.warn(e);
        });
    })
    .catch(e => {
      console.warn(e);
    });
};
const onSaveFlowEntry = (xml: string) => {
  formFlowEntryData.value.bpmnXml = xml;
  onSaveFlowEntryBasicInfo()
    .then(() => {
      ElMessage.success('Save Success');
    })
    .catch(e => {
      console.warn(e);
    });
};

// Initialize Flow Basic Information
const initFlowEntryInfo = () => {
  formFlowEntryData.value = {
    processDefinitionName: undefined,
    processDefinitionKey: undefined,
    categoryId: undefined,
    flowType: FlowEntryType.NORMAL,
    bindFormType: SysFlowEntryBindFormType.ONLINE_FORM,
    pageId: undefined,
    defaultFormId: undefined,
    defaultRouterName: undefined,
    bpmnXml: undefined,
    diagramType: DiagramType.ORDINARY,
    encodedRule: {
      calculateWhenView: false,
      prefix: undefined,
      precisionTo: SysAutoCodeType.DAYS,
      middle: undefined,
      idWidth: undefined,
    },
    extensionData: {},
    notifyTypes: undefined,
    cascadeDeleteBusinessData: false,
    supportRevive: false,
    keptReviveDays: undefined,
  };
  activeStep.value = SysFlowEntryStep.BASIC;

  FlowEntryController.view({
    entryId: dialogParams.value.flowEntry?.entryId,
  })
    .then(res => {
      formFlowEntryData.value = {
        ...formFlowEntryData.value,
        ...res.data,
        encodedRule:
          res.data.encodedRule != null
            ? JSON.parse(res.data.encodedRule)
            : {
                calculateWhenView: false,
                prefix: undefined,
                precisionTo: SysAutoCodeType.DAYS,
                middle: undefined,
                idWidth: undefined,
              },
        extensionData: res.data.extensionData != null ? JSON.parse(res.data.extensionData) : {},
      };
      formFlowEntryData.value.notifyTypes = formFlowEntryData.value.extensionData.notifyTypes || [];
      formFlowEntryData.value.cascadeDeleteBusinessData =
        formFlowEntryData.value.extensionData.cascadeDeleteBusinessData || false;
      formFlowEntryData.value.supportRevive =
        formFlowEntryData.value.extensionData.supportRevive || false;
      formFlowEntryData.value.keptReviveDays = formFlowEntryData.value.extensionData.keptReviveDays;
      if (formFlowEntryData.value.bindFormType === SysFlowEntryBindFormType.ONLINE_FORM) {
        defaultFormIdWidget.onVisibleChange(true);
      }
      if (designer.value?.processDesigner) {
        designer.value.processDesigner.diagramType = res.data.diagramType;
      }
    })
    .catch(e => {
      console.warn(e);
    });
};
onMounted(() => {
  // Initialize Page Data
  categoryIdWidget.onVisibleChange(true);
  pageIdWidget.onVisibleChange(true);
  if (isEdit.value) {
    initFlowEntryInfo();
  }
});

provide('flowEntry', () => formFlowEntryData);
provide('formList', () => (defaultFormIdWidget ? defaultFormIdWidget.dropdownList : []));
provide('allVariableList', () => processVariableList);
</script>

<style scoped>
.edit-online-form {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  width: 100vw;
  height: 100vh;
  background: white;
}
.edit-online-form :deep(.el-steps--simple) {
  width: 800px;
  padding: 13px 0;
  background: white !important;
}
.edit-online-form :deep(.vxe th),
.edit-online-form :deep(.vxe td) {
  padding: 6px 0;
}
.edit-online-form :deep(.el-scrollbar__bar) {
  display: none;
}
.edit-online-form .header {
  height: 72px;
  line-height: 72px;
}
.edit-online-form .title {
  font-size: 24px;
  flex-grow: 0 !important;
}
.edit-online-form .title > i {
  margin-right: 10px;
  color: #1a457b;
}
.edit-online-form .main-box {
  height: 100%;
  padding: 20px;
  background: white;
}
.edit-online-form .btn-add {
  width: 100%;
  margin-top: 10px;
  border: 1px dashed #ebeef5;
}
.edit-online-form :deep(.vxe td) {
  font-size: 12px;
  color: #666;
}
.append-add {
  background: #f5f7fa;
  border: none;
  border-radius: 0;
  border-left: 1px solid #dcdfe6;
}
</style>
