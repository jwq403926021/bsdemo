<template>
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFlowEntry"
      label-width="140px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box :item-width="350" @search="refreshFormFlowEntry(true)" @reset="onReset">
        <el-form-item label="Process Category" prop="formFilter.categoryId">
          <el-select
            class="filter-item"
            v-model="formFlowEntry.formFilter.categoryId"
            :clearable="true"
            filterable
            placeholder="Process Category"
            :loading="formFlowEntry.categoryIdWidget.loading"
            @visible-change="formFlowEntry.categoryIdWidget.onVisibleChange"
          >
            <el-option
              v-for="item in formFlowEntry.categoryIdWidget.dropdownList"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="Process Name" prop="formFilter.processDefinitionName">
          <el-input
            class="filter-item"
            v-model="formFlowEntry.formFilter.processDefinitionName"
            :clearable="true"
            placeholder="Process Name"
          />
        </el-form-item>
        <el-form-item label="Publish Status" prop="formFilter.status">
          <el-select
            class="filter-item"
            v-model="formFlowEntry.formFilter.status"
            :clearable="true"
            filterable
            placeholder="Publish Status"
          >
            <el-option
              v-for="item in SysFlowEntryPublishedStatus.getList()"
              :key="item.id"
              :value="item.id"
              :label="item.name"
            />
          </el-select>
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      ref="flowEntry"
      :data="formFlowEntry.flowEntryWidget.dataList"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="formFlowEntry.flowEntryWidget.onSortChange"
      @refresh="refreshFormFlowEntry(true)"
      :seq-config="{
        startIndex:
          (formFlowEntry.flowEntryWidget.currentPage - 1) * formFlowEntry.flowEntryWidget.pageSize,
      }"
    >
      <template v-slot:operator>
        <el-button
          type="primary"
          :icon="Plus"
          :size="layoutStore.defaultFormItemSize"
          @click="onAddFlowEntryClick()"
          >Create</el-button
        >
      </template>
      <vxe-column
        title="No."
        type="seq"
        width="50px"
        :index="formFlowEntry.flowEntryWidget.getTableIndex"
      />
      <vxe-column title="Process Name" field="processDefinitionName"> </vxe-column>
      <vxe-column title="Process Key" field="processDefinitionKey"> </vxe-column>
      <vxe-column title="Process Category" field="flowCategory.name"> </vxe-column>
      <vxe-column title="Process Type">
        <template v-slot="scope">
          <el-tag
            :size="layoutStore.defaultFormItemSize"
            :type="scope.row.flowType === FlowEntryType.FLOW ? 'success' : 'primary'"
          >
            {{ FlowEntryType.getValue(scope.row.flowType || FlowEntryType.NORMAL) }}
          </el-tag>
        </template>
      </vxe-column>
      <vxe-column title="Diagram Type">
        <template v-slot="scope">
          <el-tag
            :size="layoutStore.defaultFormItemSize"
            :type="scope.row.diagramType === DiagramType.ORDINARY ? 'success' : 'primary'"
          >
            {{ DiagramType.getValue(scope.row.diagramType) }}
          </el-tag>
        </template>
      </vxe-column>
      <vxe-column title="Publish Status" field="status">
        <template v-slot="scope">
          <el-tag
            :size="layoutStore.defaultFormItemSize"
            :type="
              scope.row.status === SysFlowEntryPublishedStatus.PUBLISHED ? 'success' : 'warning'
            "
          >
            {{ SysFlowEntryPublishedStatus.getValue(scope.row.status) }}
          </el-tag>
        </template>
      </vxe-column>
      <vxe-column title="Main Version" field="mainFlowEntryPublish" width="200px">
        <template v-slot="scope">
          <el-tag
            v-if="scope.row.mainFlowEntryPublish"
            :size="layoutStore.defaultFormItemSize"
            type="primary"
            effect="dark"
            style="min-width: 60px"
          >
            {{ 'V:' + scope.row.mainFlowEntryPublish.publishVersion }}
          </el-tag>
          <el-tag
            v-if="scope.row.mainFlowEntryPublish"
            :size="layoutStore.defaultFormItemSize"
            effect="dark"
            style="margin-left: 10px"
            :type="scope.row.mainFlowEntryPublish.activeStatus ? 'success' : 'danger'"
          >
            {{ scope.row.mainFlowEntryPublish.activeStatus ? 'Activated' : 'DeActivated' }}
          </el-tag>
        </template>
      </vxe-column>
      <vxe-column title="Last Publish Time" field="latestPublishTime" sortable />
      <vxe-column title="Operation" fixed="right" width="290px">
        <template v-slot="scope">
          <el-button
            @click.stop="onStartFlowEntryClick(scope.row)"
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
            :disabled="
              !(scope.row.mainFlowEntryPublish && scope.row.mainFlowEntryPublish.activeStatus)
            "
          >
            Start
          </el-button>
          <el-button
            @click.stop="onEditFlowEntryClick(scope.row)"
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
          >
            Edit
          </el-button>
          <el-button
            @click.stop="onPublishedClick(scope.row)"
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
          >
            Publish
          </el-button>
          <el-button
            @click.stop="onPublishedEntryListClick(scope.row)"
            type="primary"
            link
            :size="layoutStore.defaultFormItemSize"
          >
            Version
          </el-button>
          <el-button
            @click.stop="onDeleteFlowEntryClick(scope.row)"
            type="danger"
            link
            :size="layoutStore.defaultFormItemSize"
          >
            Delete
          </el-button>
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <el-row type="flex" justify="end" style="margin-top: 16px">
          <el-pagination
            :total="formFlowEntry.flowEntryWidget.totalCount"
            :current-page="formFlowEntry.flowEntryWidget.currentPage"
            :page-size="formFlowEntry.flowEntryWidget.pageSize"
            :page-sizes="[10, 20, 50, 100]"
            layout="total, prev, pager, next, sizes"
            @current-change="formFlowEntry.flowEntryWidget.onCurrentPageChange"
            @size-change="formFlowEntry.flowEntryWidget.onPageSizeChange"
          >
          </el-pagination>
        </el-row>
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { useCommon } from '@/common/hooks/useCommon';
import { FlowEntryController, FlowDictionaryController, FlowOperationController } from '@/api/flow';
import { DropdownOptions, ListData } from '@/common/types/list';
import { TableOptions } from '@/common/types/pagination';
import { ANY_OBJECT } from '@/types/generic';
import { DictData } from '@/common/staticDict/types';
import {
  //SysFlowTaskOperationType,
  SysFlowTaskType,
  DiagramType,
  SysFlowEntryPublishedStatus,
  SysFlowTaskOperationType,
  FlowEntryType,
} from '@/common/staticDict/flow';
import { useLayoutStore } from '@/store';
import FormPublishedFlowEntry from './formPublishedFlowEntry.vue';
import FormEditFlowEntry from './formEditFlowEntry.vue';

const layoutStore = useLayoutStore();
const form = ref();
const { Plus, useDropdown, useTable, Dialog } = useCommon();

/**
 * Process Category Dropdown Data Fetch Function
 */
const loadCategoryIdDropdownList = (): Promise<ListData<DictData>> => {
  return new Promise((resolve, reject) => {
    let params = {};
    FlowDictionaryController.dictFlowCategory(params)
      .then(res => {
        resolve({ dataList: res.getList() });
      })
      .catch(e => {
        reject(e);
      });
  });
};
const dropdownOptions: DropdownOptions<ANY_OBJECT> = {
  loadData: loadCategoryIdDropdownList,
};

/**
 * FlowEntry Data Fetch Function, Returns Promise
 */
const loadFlowEntryWidgetData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    flowEntryDtoFilter: {
      categoryId: formFlowEntry.formFilterCopy.categoryId,
      processDefinitionName: formFlowEntry.formFilterCopy.processDefinitionName,
      processDefinitionKey: formFlowEntry.formFilterCopy.processDefinitionKey,
      flowType: formFlowEntry.formFilterCopy.flowType,
      status: formFlowEntry.formFilterCopy.status,
    },
  };
  return new Promise((resolve, reject) => {
    FlowEntryController.list(params)
      .then(res => {
        resolve({
          dataList: res.data.dataList,
          totalCount: res.data.totalCount,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * FlowEntry Data Verification Function, Returns true if data fetch is normal, returns false to stop fetching data
 */
const loadFlowEntryVerify = () => {
  formFlowEntry.formFilterCopy.categoryId = formFlowEntry.formFilter.categoryId;
  formFlowEntry.formFilterCopy.flowType = formFlowEntry.formFilter.flowType;
  formFlowEntry.formFilterCopy.processDefinitionName =
    formFlowEntry.formFilter.processDefinitionName;
  formFlowEntry.formFilterCopy.processDefinitionKey = formFlowEntry.formFilter.processDefinitionKey;
  formFlowEntry.formFilterCopy.status = formFlowEntry.formFilter.status;
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadFlowEntryWidgetData,
  verifyTableParameter: loadFlowEntryVerify,
  paged: true,
  orderFieldName: 'entryId',
  ascending: true,
};
const formFlowEntry = reactive({
  formFilter: {
    status: undefined,
    categoryId: undefined,
    processDefinitionName: undefined,
    processDefinitionKey: undefined,
    flowType: undefined,
  },
  formFilterCopy: {
    status: undefined,
    categoryId: undefined,
    processDefinitionName: undefined,
    processDefinitionKey: undefined,
    flowType: undefined,
  },
  categoryIdWidget: reactive(useDropdown(dropdownOptions)),
  flowEntryWidget: reactive(useTable(tableOptions)),
  isInit: false,
});

const router = useRouter();
/**
 * Update Process Design
 */
const refreshFormFlowEntry = (reloadData = false) => {
  if (reloadData) {
    formFlowEntry.flowEntryWidget.refreshTable(true, 1);
  } else {
    formFlowEntry.flowEntryWidget.refreshTable();
  }
  if (!formFlowEntry.isInit) {
    // Initialize Dropdown Data
    console.log('');
  }
  formFlowEntry.isInit = true;
};

const onReset = () => {
  form.value.resetFields();
  refreshFormFlowEntry(true);
};
/**
 * Start
 */
const onStartFlowEntryClick = (row: ANY_OBJECT) => {
  let params = {
    processDefinitionKey: row.processDefinitionKey,
  };
  let httpCall =
    row.flowType === FlowEntryType.AUTO_TASK
      ? FlowOperationController.startAutoTask(params)
      : FlowOperationController.viewInitialTaskInfo(params);
  httpCall
    .then(res => {
      if (row.flowType === FlowEntryType.AUTO_TASK) {
        ElMessage.success('Started Successfully!');
        return;
      }
      if (res.data && res.data.taskType === SysFlowTaskType.USER_TASK && res.data.assignedMe) {
        let params = {
          processDefinitionKey: row.processDefinitionKey || '',
          formId: res.data.formId || '',
          routerName: res.data.routerName,
          readOnly: res.data.readOnly,
          taskName: 'Start Process',
          flowEntryName: row.processDefinitionName || '',
          // TypeError: Cannot convert object to primitive value
          operationList: JSON.stringify(
            (res.data.operationList || []).filter((item: ANY_OBJECT) => {
              return (
                item.type !== SysFlowTaskOperationType.CO_SIGN &&
                item.type !== SysFlowTaskOperationType.REVOKE &&
                item.type !== SysFlowTaskOperationType.SIGN_REDUCTION
              );
            }),
          ),
          isDraft: 'true',
          isPreview: 'true',
          // TypeError: can't convert text to primitive type
          variableList: JSON.stringify(res.data.variableList),
        };
        console.log('viewInitialTaskInfo', res.data);
        console.log('params', params);
        router.push({
          name: res.data.routerName || 'handlerFlowTask',
          query: params,
        });
      } else {
        FlowOperationController.startOnly({
          processDefinitionKey: row.processDefinitionKey,
        })
          .then(() => {
            ElMessage.success('Started Successfully!');
          })
          .catch(e => {
            console.warn(e);
          });
      }
    })
    .catch(e => {
      console.warn(e);
    });
};

/**
 * Create
 */
const onAddFlowEntryClick = () => {
  Dialog.show(
    'Create Page',
    FormEditFlowEntry,
    {
      area: ['100vw', '100vh'],
      skin: 'fullscreen-dialog',
    },
    {},
    {
      fullscreen: true,
      pathName: '/thirdParty/thirdFormEditFlowEntry',
    },
  )
    .then(() => {
      refreshFormFlowEntry();
    })
    .catch(e => {
      console.log(e);
      refreshFormFlowEntry();
    });
};
/**
 * Edit
 */
const onEditFlowEntryClick = (row: ANY_OBJECT) => {
  Dialog.show(
    'Edit Page',
    FormEditFlowEntry,
    {
      area: ['100vw', '100vh'],
      skin: 'fullscreen-dialog',
    },
    {
      flowEntry: row,
    },
    {
      fullscreen: true,
      pathName: '/thirdParty/thirdFormEditFlowEntry',
    },
  )
    .then(() => {
      refreshFormFlowEntry();
    })
    .catch(e => {
      console.warn(e);
      refreshFormFlowEntry();
    });
};
/**
 * Publish
 */
const onPublishedClick = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to publish the current workflow design?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      if (row.entryId == null) {
        ElMessage.error('Request failed, required parameters are empty!');
        return;
      }
      let params = {
        entryId: row.entryId,
      };

      FlowEntryController.publish(params)
        .then(() => {
          ElMessage.success('Published Successfully');
          refreshFormFlowEntry();
        })
        .catch(e => {
          console.warn(e);
        });
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Version Management
 */
const onPublishedEntryListClick = (row: ANY_OBJECT) => {
  Dialog.show(
    'Version Management',
    FormPublishedFlowEntry,
    {
      area: ['1200px', '750px'],
    },
    {
      flowEntry: row,
    },
    {
      width: '1200px',
      height: '780px',
      pathName: '/thirdParty/thirdFormPublishedFlowEntry',
    },
  )
    .then(() => {
      refreshFormFlowEntry();
    })
    .catch(() => {
      refreshFormFlowEntry();
    });
};
/**
 * Delete
 */
const onDeleteFlowEntryClick = (row: ANY_OBJECT) => {
  if (row.entryId == null) {
    ElMessage.error('Request failed, required parameters are empty!');
    return;
  }
  let params = {
    entryId: row.entryId,
  };

  ElMessageBox.confirm('Do you want to delete this process?', '', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      FlowEntryController.delete(params)
        .then(() => {
          ElMessage.success('Deleted Successfully');
          formFlowEntry.flowEntryWidget.refreshTable();
        })
        .catch(e => {
          console.warn(e);
        });
    })
    .catch(e => {
      console.warn(e);
    });
};

const onResume = () => {
  refreshFormFlowEntry();
};
const formInit = () => {
  refreshFormFlowEntry();
};
onMounted(() => {
  // Initialize Page Data
  formInit();
});
defineExpose({
  onResume,
});
</script>
