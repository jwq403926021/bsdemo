<template>
  <el-container class="edit-online-page">
    <el-header
      class="step-header"
      style="overflow: hidden; height: 72px; background: white; border-bottom: 1px solid #ebeef5"
    >
      <el-row type="flex" justify="space-between" align="middle" style="height: 100%">
        <div
          class="title"
          style="display: flex; width: 200px; height: 40px; line-height: 40px"
        ></div>
        <!-- v-model:value can be used for two-way binding, use :value when two-way binding is not needed -->
        <StepBar class="step" :value="activeStep">
          <StepBarItem icon="online-icon icon-basic-info" :name="SysOnlinePageSettingStep.BASIC"
            >Basic Info</StepBarItem
          >
          <!-- <StepBarItem icon="online-icon icon-data" :name="SysOnlinePageSettingStep.DATASOURCE"
            >Data Model</StepBarItem
          > -->
          <StepBarItem
            icon="online-icon icon-form-design"
            :name="SysOnlinePageSettingStep.FORM_DESIGN"
            >Form Design</StepBarItem
          >
        </StepBar>
        <el-row class="operation" type="flex" justify="end" style="width: 200px">
          <el-button
            v-if="!showSaveButton"
            :size="formItemSize"
            :disabled="activeStep == SysOnlinePageSettingStep.BASIC"
            @click="onPrevClick"
          >
            Previous
          </el-button>
          <el-button
            v-if="!showSaveButton"
            :disabled="activeStep == SysOnlinePageSettingStep.FORM_DESIGN"
            :size="formItemSize"
            type="primary"
            @click="onNextClick"
          >
            Next
          </el-button>
          <el-button v-if="showSaveButton" :size="formItemSize" type="primary" @click="onSaveClick">
            Save
          </el-button>
          <el-button v-if="showSaveButton" @click="onReturn" :size="formItemSize">Return</el-button>
          <el-button @click="onClose" :size="formItemSize">Quit</el-button>
        </el-row>
      </el-row>
    </el-header>
    <el-main
      :style="{
        padding: activeStep == SysOnlinePageSettingStep.FORM_DESIGN ? '0px' : '15px',
      }"
    >
      <el-row type="flex" justify="center" style="height: 100%">
        <!-- Basic Information -->
        <BasicForm
          ref="basicForm"
          :status="status"
          v-if="activeStep == SysOnlinePageSettingStep.BASIC"
          class="main-box"
          style="width: 600px"
          :dataSource="getPageDatasource"
          :dblinkInfo="dblinkInfo"
          v-model="formPageData"
        />
        <!-- Online Form Data Model Configuration -->
        <!-- <DataModel
          ref="modelForm"
          :data="getPageDatasourceTableList"
          :defaultFormItemSize="formItemSize"
          :pageId="formPageData.pageId"
          :dataSource="getPageDatasource"
          :dblinkInfo="dblinkInfo"
          :dictList="dictList"
          :clientHeight="clientHeight || thirdParams.clientHeight"
          @change="initPageDatasourceInfo"
          @un-saved="onUnSaved"
          v-if="activeStep == SysOnlinePageSettingStep.DATASOURCE && formPageData.pageId"
        /> -->
        <!-- Form Design -->
        <FormDesign
          v-if="activeStep == SysOnlinePageSettingStep.FORM_DESIGN"
          ref="designForm"
          :pageType="formPageData.pageType!"
          :tableList="getPageDatasourceTableList"
          :allFormList="formList"
          :dictList="dictList"
          :height="clientHeight || thirdParams.clientHeight"
          class="design-box"
          style="width: 100%; height: 100%"
          @createForm="onCreateNewForm"
          @updateForm="updateFormInfo"
          @cloneForm="onCloneForm"
          @deleteForm="onDeleteForm"
        />
      </el-row>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Dialog } from '@/components/Dialog';
import {
  SysOnlineFormKind,
  SysOnlinePageSettingStep,
  SysOnlinePageStatus,
  SysOnlinePageType,
} from '@/common/staticDict/online';
import { DialogProp } from '@/components/Dialog/types';
import { ANY_OBJECT } from '@/types/generic';
import StepBar from '@/components/StepBar/index.vue';
import StepBarItem from '@/components/StepBar/stepItem.vue';
import { FormPage } from '@/types/online/page';
import {
  OnlineColumnController,
  OnlineDatasourceRelationController,
  OnlineDblinkController,
  OnlineDictController,
  OnlineFormController,
  OnlinePageController,
} from '@/api/online';
import { Dict } from '@/types/online/dict';
import { DBLink } from '@/types/online/dblink';
import { ResponseDataType } from '@/common/http/types';
import { TableData } from '@/common/types/table';
import { useFormConfig } from '@/pages/online/hooks/useFormConfig';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
import { SysOnlineFormType } from '@/common/staticDict';
import BasicForm from './basic/index.vue';
import DataModel from './dataModel/index.vue';
import FormDesign from './formDesign/index.vue';
import EditOnlineForm from './editOnlineForm.vue';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  pageId?: string;
  status?: number;
  clientHeight: ANY_OBJECT;
  // When using Dialog.show to pop up the component, this prop must be defined for callback
  dialog?: DialogProp<ANY_OBJECT>;
}

const props = defineProps<IProps>();
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});

const { thirdParams, onCloseThirdDialog } = useThirdParty(props);
provide('clientHeight', props.clientHeight || thirdParams.value.clientHeight);

const basicForm = ref();
const modelForm = ref();
const designForm = ref();
const activeStep = ref(SysOnlinePageSettingStep.BASIC);
const formPageData = ref({ published: false } as FormPage);
const dictList = ref<Dict[]>([]);
const formList = ref<ANY_OBJECT[]>([]);
const dblinkInfo = ref();
// TODO: Add condition checks
const hasUnSaved = ref(false);
const showSaveButton = computed(() => {
  return activeStep.value == SysOnlinePageSettingStep.FORM_DESIGN || hasUnSaved.value;
});
const onUnSaved = (val: boolean) => {
  hasUnSaved.value = val;
};

const pageDatasourceList = ref<ANY_OBJECT[]>([]);

const onPrevClick = () => {
  switch (activeStep.value) {
    case SysOnlinePageSettingStep.DATASOURCE:
      activeStep.value = SysOnlinePageSettingStep.BASIC;
      break;
    case SysOnlinePageSettingStep.FORM_DESIGN:
      //dirty = false;
      activeStep.value = SysOnlinePageSettingStep.DATASOURCE;
      break;
  }
};
const onNextClick = () => {
  switch (activeStep.value) {
    case SysOnlinePageSettingStep.BASIC:
      // Step 2 Enter Data Model Page
      basicForm.value
        .save()
        .then((res: FormPage) => {
          if (res) {
            formPageData.value = { ...res };
          }
          if (formPageData.value.pageId) {
            initPageDatasourceInfo(formPageData.value.pageId).then(res => {
              onSavePageDatasourceInfo(false)
                .then(() => {
                  // Step 1 Get the field list of all tables used in the data source
                  let httpCalls: ANY_OBJECT[] = [];
                  console.log(
                    'Get the field list of all tables used in the data source',
                    getPageDatasourceTableList.value.length,
                  );
                  getPageDatasourceTableList.value.forEach(item => {
                    httpCalls.push(loadOnlineTableColumns(item.tableId));
                  });
                  return Promise.all(httpCalls);
                })
                .then(res => {
                  res.forEach((item, index) => {
                    getPageDatasourceTableList.value[index].columnList = item;
                    return getPageDatasourceTableList.value[index];
                  });
                  // Step 2 Get the form list
                  return initPageFormList(formPageData.value.pageId);
                })
                .then(() => {
                  // Step 3 Enter Form Design Page
                  activeStep.value = SysOnlinePageSettingStep.FORM_DESIGN;
                })
                .catch(e => {
                  console.warn(e);
                });
            });
          }
          return res;
        })
        .catch((e: Error) => {
          console.warn(e);
        });

      break;
  }
};
const loadOnlineTableColumns = (tableId: string) => {
  if (tableId == null || tableId === '') return Promise.reject();

  return new Promise((resolve, reject) => {
    let params = {
      onlineColumnDtoFilter: {
        tableId,
      },
    };

    OnlineColumnController.list(params)
      .then(res => {
        resolve(res.data.dataList);
      })
      .catch(e => {
        reject(e);
      });
  });
};

const { getFormConfig, mergeWidget, mergeArray } = useFormConfig();

/**
 * Get the list of sub-forms on the page
 */
const initPageFormList = (pageId: string | undefined) => {
  if (!pageId) {
    return Promise.resolve();
  }
  return new Promise((resolve, reject) => {
    OnlineFormController.list({
      onlineFormDtoFilter: {
        pageId: pageId,
      },
      orderParam: [
        {
          fieldName: 'createTime',
          asc: true,
        },
      ],
    })
      .then(res => {
        console.log('editOnlinePage form list', res.data.dataList);
        formList.value = (res.data.dataList || []).map(item => {
          let config = item.widgetJson ? JSON.parse(item.widgetJson) : {};
          let paramList = item.paramsJson ? JSON.parse(item.paramsJson) : [];
          let formConfig = getFormConfig(item.formType, formPageData.value.pageType);
          let allowMode = ['pc'];
          allowMode.forEach(mode => {
            config[mode] = {
              ...formConfig[mode],
              ...config[mode],
            };

            config[mode].formId = item.formId;
            config[mode].formCode = item.formCode;
            config[mode].formName = item.formName;
            config[mode].formKind = item.formKind;
            config[mode].formType = item.formType;
            config[mode].formTypeDictMap = item.formTypeDictMap;
            config[mode].masterTableId = item.masterTableId;
            // Merge form operations
            config[mode].operationList = mergeArray(
              config[mode].operationList,
              formConfig[mode].operationList,
              'id',
            );
            // Merge component operations
            if (Array.isArray(config[mode].widgetList)) {
              config[mode].widgetList.forEach((widget: ANY_OBJECT) => {
                mergeWidget(widget);
              });
            }
            mergeWidget(config[mode].tableWidget);
            mergeWidget(config[mode].leftWidget);
          });
          return {
            ...item,
            ...config,
            paramList,
            processId:
              item.formType === SysOnlineFormType.FORM && item.processId ? item.processId : '',
            step1Name: config?.pc?.step1Name || '',
            step2Name: config?.pc?.step2Name || '',
            step3Name: config?.pc?.step3Name || '',
          };
        });
        resolve(res);
      })
      .catch(e => {
        reject(e);
      });
  });
};

const onSaveClick = () => {
  if (activeStep.value == SysOnlinePageSettingStep.FORM_DESIGN) {
    designForm.value.saveForm();
  } else if (activeStep.value == SysOnlinePageSettingStep.DATASOURCE) {
    modelForm.value.save();
  }
};
const onReturn = () => {
  if (showSaveButton.value) {
    activeStep.value = SysOnlinePageSettingStep.BASIC;
  }
};
const onClose = () => {
  if (props.dialog) {
    props.dialog.cancel();
  } else {
    // Close third-party popup
    onCloseThirdDialog(true);
  }
};

/**
 * Change form status
 *
 * @param status Status
 */
const savePageInfo = (status: number) => {
  let params = {
    onlinePageDto: {
      ...formPageData.value,
      status: status,
      published: false,
    },
  };
  return OnlinePageController.update(params);
};

/**
 * Save page data model information
 */
const onSavePageDatasourceInfo = (isPrev = false) => {
  // If it is the previous step, return directly
  if (isPrev) return Promise.resolve();
  // Next step needs to check if a data source has been added
  if (getPageDatasource.value == null) {
    ElMessage.error('Please set the page data model!');
    if (formPageData.value.status !== SysOnlinePageStatus.DATASOURCE) {
      savePageInfo(SysOnlinePageStatus.DATASOURCE).catch(e => {
        console.warn(e);
      });
    }
    return Promise.reject();
  } else {
    if (formPageData.value.status === SysOnlinePageStatus.DESIGNING) {
      return Promise.resolve();
    } else {
      return savePageInfo(SysOnlinePageStatus.DESIGNING);
    }
  }
};

/**
 * Get all online form dictionary information
 */
const loadOnlineDictList = () => {
  OnlineDictController.list({})
    .then(res => {
      res.data.dataList.forEach((item: Dict) => {
        item.id = item.dictId;
        item.name = item.dictName;
        item.dictData = item.dictDataJson ? JSON.parse(item.dictDataJson) : undefined;
      });
      dictList.value = res.data.dataList;
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Get database information
 */
const loadDblinkList = () => {
  return new Promise<ANY_OBJECT>((resolve, reject) => {
    dblinkInfo.value = {};
    OnlineDblinkController.list({})
      .then(res => {
        res.data.dataList.forEach((item: DBLink) => {
          if (item.dblinkId) {
            dblinkInfo.value[item.dblinkId] = {
              ...item,
              tableList: undefined,
            };
          }
        });
        resolve(res);
      })
      .catch(e => {
        reject(e);
      });
  });
};

const initPageInfo = () => {
  formPageData.value = {
    pageId: undefined,
    pageCode: undefined,
    pageName: undefined,
    published: false,
    pageType: SysOnlinePageType.BIZ,
    status: SysOnlinePageStatus.BASIC,
    extraJson: {
      extendClass: undefined,
    },
  };
  activeStep.value = SysOnlinePageSettingStep.BASIC;
  let pageId = props.pageId || thirdParams.value.pageId;
  if (pageId != null || formPageData.value.pageId != null) {
    let params = {
      pageId: pageId || formPageData.value.pageId,
    };
    return OnlinePageController.view(params);
  } else {
    return Promise.reject();
  }
};

const getPageDatasource = computed(() => {
  return pageDatasourceList.value[0];
});
const getPageDatasourceTableList = computed<ANY_OBJECT[]>(() => {
  if (getPageDatasource.value == null) return [];

  let tableList = [];
  // Add main table information
  tableList.push({
    id: getPageDatasource.value.datasourceId,
    tableName: getPageDatasource.value.masterTableIdDictMap.name,
    tableId: getPageDatasource.value.masterTableId,
    relationType: undefined,
    masterColumnName: undefined,
    slaveColumnName: undefined,
    cascadeDelete: false,
    leftJoin: false,
    showName: getPageDatasource.value.datasourceName,
    tag: getPageDatasource.value,
  });
  // Add associated slave table information
  if (Array.isArray(getPageDatasource.value.relationList)) {
    getPageDatasource.value.relationList.forEach(relation => {
      tableList.push({
        id: relation.relationId,
        tableName: relation.slaveTable.tableName,
        tableId: relation.slaveTableId,
        relationType: relation.relationType,
        masterColumnName: (relation.masterColumn || {}).columnName || 'Unknown Field',
        slaveColumnName: (relation.slaveColumn || {}).columnName || 'Unknown Field',
        cascadeDelete: relation.cascadeDelete,
        leftJoin: relation.leftJoin,
        showName: relation.relationName,
        tag: relation,
      });
    });
  }
  return tableList;
});
/**
 * Initialize page data model information
 */
const initPageDatasourceInfo = (pageId: string) => {
  return new Promise((resolve, reject) => {
    OnlinePageController.listOnlinePageDatasource({
      pageId: pageId,
    })
      .then(res => {
        pageDatasourceList.value = res.data.dataList;
        return loadDatasourceRelation();
      })
      .then(res => {
        pageDatasourceList.value = pageDatasourceList.value.map(item => {
          if (item.datasourceId === getPageDatasource.value.datasourceId) {
            item.relationList = res.data.dataList || [];
          }
          return item;
        });
        resolve(res);
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Get data model association information
 */
const loadDatasourceRelation = () => {
  if (getPageDatasource.value == null)
    return Promise.resolve({
      data: { dataList: [], totalCount: 0 },
      success: false,
      errorCode: null,
      errorMessage: null,
    } as ResponseDataType<TableData<ANY_OBJECT>>);

  return OnlineDatasourceRelationController.list({
    onlineDatasourceRelationDtoFilter: {
      datasourceId: getPageDatasource.value.datasourceId,
    },
  });
};

/**
 * Create new form
 */
const onCreateNewForm = () => {
  Dialog.show(
    'Create Form',
    EditOnlineForm,
    {
      area: ['600px'],
    },
    {
      pageId: formPageData.value.pageId,
      pageType: formPageData.value.pageType,
      datasourceId: getPageDatasource.value.datasourceId,
      datasourceTableList: getPageDatasourceTableList.value,
      defaultFormItemSize: formItemSize.value,
      //path: 'thirdEditOnlineForm',
    },
    {
      width: '600px',
      height: '400px',
      pathName: '/thirdParty/thirdEditOnlineForm',
    },
  )
    .then(() => {
      initPageFormList(formPageData.value.pageId);
    })
    .catch((e: Error) => {
      console.warn(e);
    });
};
const buildSaveWidget = (widget: ANY_OBJECT) => {
  if (widget == null) return widget;
  let oldWidgetImpl = widget.widgetImpl;
  let childWidgetList = widget.childWidgetList;
  let oldParent = widget.parent;
  widget.widgetImpl = undefined;
  widget.parent = undefined;
  widget.childWidgetList = [];
  let actions = widget.props.actions;
  widget.props.actions = {};
  let temp = JSON.parse(JSON.stringify(widget));
  widget.widgetImpl = oldWidgetImpl;
  widget.parent = oldParent;
  widget.childWidgetList = childWidgetList;
  widget.props.actions = actions;
  temp.column = undefined;
  temp.datasource = undefined;
  temp.relation = undefined;
  temp.bindData.column = undefined;
  temp.bindData.table = undefined;
  temp.widgetImpl = undefined;
  if (temp.props && temp.props.dictInfo) temp.props.dictInfo.dict = undefined;
  if (temp.props && Array.isArray(temp.props.tableColumnList)) {
    temp.props.tableColumnList.forEach((tableColumn: ANY_OBJECT) => {
      tableColumn.table = undefined;
      tableColumn.column = undefined;
    });
  }
  if (Array.isArray(widget.childWidgetList)) {
    temp.childWidgetList = widget.childWidgetList.map(item => {
      return buildSaveWidget(item);
    });
  }
  return temp;
};
const updateFormInfo = (currentForm: ANY_OBJECT | undefined | null) => {
  if (!currentForm) {
    console.warn('currentForm is null or undefined');
    return;
  }
  let allowMode = ['pc'];
  let formConfig: ANY_OBJECT = allowMode.reduce((formConfig: ANY_OBJECT, mode: string) => {
    console.log('reduce', formConfig, mode);
    let tempFormConfig = currentForm[mode];
    formConfig[mode] = {
      gutter: tempFormConfig.gutter,
      filterItemWidth: tempFormConfig.filterItemWidth,
      labelWidth: tempFormConfig.labelWidth,
      labelPosition: tempFormConfig.labelPosition,
      tableWidget: buildSaveWidget(tempFormConfig.tableWidget),
      leftWidget: buildSaveWidget(tempFormConfig.leftWidget),
      operationList: tempFormConfig.operationList,
      customFieldList: tempFormConfig.customFieldList,
      widgetList: tempFormConfig.widgetList.map((widget: ANY_OBJECT) => {
        return buildSaveWidget(widget);
      }),
      formEventList: tempFormConfig.formEventList,
      maskFieldList: tempFormConfig.maskFieldList,
      width: tempFormConfig.width,
      height: tempFormConfig.height,
      fullscreen: tempFormConfig.fullscreen,
      processId: tempFormConfig.processId,
      step1Name: tempFormConfig.step1Name,
      step2Name: tempFormConfig.step2Name,
      step3Name: tempFormConfig.step3Name,
      advanceQuery: tempFormConfig.advanceQuery,
    };
    return formConfig;
  }, {});

  let params = {
    onlineFormDto: {
      pageId: currentForm.pageId,
      formId: currentForm.formId,
      formCode: currentForm.pc.formCode,
      formName: currentForm.pc.formName,
      step1Name: currentForm.pc.step1Name,
      step2Name: currentForm.pc.step2Name,
      step3Name: currentForm.pc.step3Name,
      processId: currentForm.pc.processId,
      formKind: SysOnlineFormKind.PAGE,
      formType: currentForm.formType,
      masterTableId: currentForm.masterTableId,
      widgetJson: JSON.stringify(formConfig),
      datasourceIdList: [getPageDatasource.value.datasourceId],
    },
  };
  let httpCall = OnlineFormController.update(params);
  httpCall
    .then(() => {
      ElMessage.success('Save success');
      initPageFormList(formPageData.value.pageId).catch(e => {
        console.warn(e);
      });
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Copy form
 */
const onCloneForm = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to copy this form?', '', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      let params = {
        formId: row.formId,
      };

      return OnlineFormController.clone(params);
    })
    .then(() => {
      ElMessage.success('Copy successful!');
      initPageFormList(formPageData.value.pageId).catch(e => {
        console.warn(e);
      });
    })
    .catch(e => {
      console.warn(e);
    });
};
const onDeleteForm = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to delete this form?', '', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      let params = {
        formId: row.formId,
      };

      return OnlineFormController.delete(params);
    })
    .then(() => {
      ElMessage.success('Deletion successful!');
      initPageFormList(formPageData.value.pageId).catch(e => {
        console.warn(e);
      });
    })
    .catch(e => {
      console.warn(e);
    });
};

onMounted(() => {
  loadOnlineDictList();
  loadDblinkList()
    .then(() => {
      console.log('dblinkInfo', dblinkInfo.value);
      return initPageInfo();
    })
    .then(res => {
      let extraJson = JSON.parse((res.data.extraJson as string) || '{}');
      formPageData.value = {
        ...res.data,
        extraJson: {
          extendClass: extraJson.extendClass,
        },
      };
    })
    .catch(e => {
      console.warn(e);
    });
});
</script>

<style scoped>
.edit-online-page {
  position: fixed;
  top: 0;
  left: 0;
  z-index: 100;
  width: 100vw;
  height: 100vh;
  background: #f6f6f6 !important;
}
.main-box {
  height: 100%;
  padding: 20px;
  background: white;
}
.design-box {
  height: 100%;
  padding: 0;
  border-radius: 3px;
}
</style>
