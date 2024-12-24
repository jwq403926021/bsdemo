<template>
  <!-- Workflow Version Management -->
  <div class="form-single-fragment" style="position: relative">
    <el-row>
      <el-col :span="24" v-if="entryXml == null">
        <vxe-table
          empty-text="No data"
          ref="flowEntry"
          :data="publishedFlowEntryWidget.dataList"
          :size="formItemSize"
          height="655px"
          :row-config="{ isHover: true }"
          @sort-change="publishedFlowEntryWidget.onSortChange"
          header-cell-class-name="table-header-gray"
        >
          <vxe-column
            title="No."
            type="seq"
            width="70px"
            :index="publishedFlowEntryWidget.getTableIndex"
          />
          <vxe-column title="Process Name">
            <template v-slot>
              <span>{{ dialogParams.flowEntry.processDefinitionName }}</span>
            </template>
          </vxe-column>
          <vxe-column title="Process Category">
            <template v-slot>
              <span>{{ dialogParams.flowEntry.flowCategory.name }}</span>
            </template>
          </vxe-column>
          <vxe-column title="Publish Version" field="publishVersion">
            <template v-slot="scope">
              <el-tag :size="formItemSize" type="primary" effect="dark">{{
                'V:' + scope.row.publishVersion
              }}</el-tag>
            </template>
          </vxe-column>
          <vxe-column title="Active Status" field="activeStatus">
            <template v-slot="scope">
              <el-tag
                :size="formItemSize"
                effect="dark"
                :type="scope.row.activeStatus ? 'success' : 'danger'"
              >
                {{ scope.row.activeStatus ? 'Activated' : 'DeActivated' }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column title="Main Version" field="mainVersion">
            <template v-slot="scope">
              <el-tag
                :size="formItemSize"
                effect="dark"
                :type="scope.row.mainVersion ? 'success' : 'danger'"
              >
                {{ scope.row.mainVersion ? 'Yes' : 'No' }}
              </el-tag>
            </template>
          </vxe-column>
          <vxe-column title="Publish Time" field="publishTime" />
          <vxe-column title="Actions" fixed="right" min-width="110px">
            <template v-slot="scope">
              <el-button
                @click.stop="onSetActiveStatus(scope.row)"
                link
                type="danger"
                :size="formItemSize"
              >
                {{ scope.row.activeStatus ? 'Suspend' : 'Activate' }}
              </el-button>
              <el-button
                type="primary"
                link
                :size="formItemSize"
                @click="getTaskProcessXml(scope.row)"
              >
                Diagram
              </el-button>
              <el-button
                type="primary"
                :disabled="scope.row.mainVersion"
                @click.stop="onSetMainVersion(scope.row)"
                link
                :size="formItemSize"
              >
                Set As Main
              </el-button>
            </template>
          </vxe-column>
          <template v-slot:empty>
            <div class="table-empty unified-font">
              <img src="@/assets/img/empty.png" />
              <span>No Data</span>
            </div>
          </template>
        </vxe-table>
      </el-col>
      <el-col :span="24" v-show="entryXml != null">
        <ProcessViewer :xml="entryXml" style="height: 653px">
          <el-button
            type="default"
            size="default"
            :icon="CircleClose"
            @click.stop="entryXml = undefined"
          />
        </ProcessViewer>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { VxeTable, VxeColumn } from 'vxe-table';
import { ElMessage, ElMessageBox } from 'element-plus';
import { CircleClose as CircleClose } from '@element-plus/icons-vue';
import { FlowEntryController, FlowOperationController } from '@/api/flow';
import ProcessViewer from '@/pages/workflow/components/ProcessViewer.vue';
import { DialogProp } from '@/components/Dialog/types';
import { ANY_OBJECT } from '@/types/generic';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { ThirdProps } from '@/components/thirdParty/types';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

interface IProps extends ThirdProps {
  flowEntry: ANY_OBJECT;
  // When using Dialog.show to pop up components, this prop must be defined for dialog callback
  dialog?: DialogProp<ANY_OBJECT>;
}
const props = withDefaults(defineProps<IProps>(), {
  flowEntry: undefined,
});
const { thirdParams } = useThirdParty(props);
const formItemSize = computed(() => {
  return layoutStore.defaultFormItemSize || thirdParams.value.defaultFormItemSize?.value;
});
const entryXml = ref<string>();
const dialogParams = computed(() => {
  return {
    flowEntry: props.flowEntry || thirdParams.value.flowEntry || {},
  };
});
/**
 * Workflow published version data retrieval function, returns Promise
 */
const loadFlowEntryWidgetData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    entryId: dialogParams.value.flowEntry.entryId,
  };
  return new Promise((resolve, reject) => {
    FlowEntryController.listFlowEntryPublish(params)
      .then(res => {
        resolve({
          dataList: res.data,
          totalCount: Array.isArray(res.data) ? res.data.length : 0,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};
/**
 * Workflow published version data retrieval verification function, returns true to continue data retrieval, returns false to stop
 */
const loadFlowEntryVerify = () => {
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadFlowEntryWidgetData,
  verifyTableParameter: loadFlowEntryVerify,
};
const publishedFlowEntryWidget = reactive(useTable(tableOptions));

const refreshFormFlowEntry = (reloadData = false) => {
  if (reloadData) {
    publishedFlowEntryWidget.refreshTable(true, 1);
  } else {
    publishedFlowEntryWidget.refreshTable();
  }
};
/**
 * Activate / Suspend
 */
const onSetActiveStatus = (row: ANY_OBJECT) => {
  let params = {
    entryPublishId: row.entryPublishId,
  };

  let httpCall = row.activeStatus
    ? FlowEntryController.suspendFlowEntryPublish(params)
    : FlowEntryController.activateFlowEntryPublish(params);
  httpCall
    .then(() => {
      ElMessage.success(`${row.activeStatus ? 'Suspension Successful!' : 'Activation Successful!'}`);
      refreshFormFlowEntry();
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Get process diagram XML
 */
const getTaskProcessXml = (row: ANY_OBJECT) => {
  if (row.processDefinitionId == null || row.processDefinitionId === '') {
    entryXml.value = undefined;
    return;
  }
  let params = {
    processDefinitionId: row.processDefinitionId,
  };
  FlowOperationController.viewProcessBpmn(params)
    .then(res => {
      // Current process instance XML
      entryXml.value = res.data;
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Set main version
 */
const onSetMainVersion = (row: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to set the current version as the main version?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      let params = {
        entryId: dialogParams.value.flowEntry.entryId,
        newEntryPublishId: row.entryPublishId,
      };
      return FlowEntryController.updateMainVersion(params);
    })
    .then(() => {
      ElMessage.success('Setting Successful!');
      refreshFormFlowEntry();
    })
    .catch(e => {
      console.warn(e);
    });
};

onMounted(() => {
  // Initialize page data
  refreshFormFlowEntry();
});
</script>
