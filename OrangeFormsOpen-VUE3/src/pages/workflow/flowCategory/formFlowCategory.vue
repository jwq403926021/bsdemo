<template>
  <div style="position: relative">
    <el-form
      ref="form"
      :model="formFlowCategory"
      label-width="120px"
      :size="layoutStore.defaultFormItemSize"
      label-position="right"
      @submit.prevent
    >
      <filter-box
        :hasFold="true"
        :hasRefresh="true"
        :hasDownload="true"
        :item-width="350"
        @search="refreshFormFlowCategory(true)"
        @reset="onReset"
      >
        <el-form-item label="Category Name" prop="formFilter.name" label-position="top">
          <el-input
            class="filter-item"
            v-model="formFlowCategory.formFilter.name"
            :clearable="true"
            placeholder="Process Category Name"
          />
        </el-form-item>
        <el-form-item label="Category Code" prop="formFilter.code" label-position="top">
          <el-input
            class="filter-item"
            v-model="formFlowCategory.formFilter.code"
            :clearable="true"
            placeholder="Category Code"
          />
        </el-form-item>
      </filter-box>
    </el-form>
    <table-box
      class="page-table"
      ref="flowCategory"
      :data="flowCategoryWidget.dataList"
      :hasExtend="false"
      :size="layoutStore.defaultFormItemSize"
      @sort-change="flowCategoryWidget.onSortChange"
      @refresh="refreshFormFlowCategory(true)"
      :seq-config="{
        startIndex: (flowCategoryWidget.currentPage - 1) * flowCategoryWidget.pageSize,
      }"
    >
      <template v-slot:operator>
        <el-button
          type="primary"
          :icon="ElIconPlus"
          :size="layoutStore.defaultFormItemSize"
          @click="onAddFlowCategoryClick()"
          >New</el-button
        >
      </template>
      <vxe-column title="No." type="seq" width="50px" :index="flowCategoryWidget.getTableIndex" />
      <vxe-column title="Process Category Name" field="name"> </vxe-column>
      <vxe-column title="Category Code" field="code"> </vxe-column>
      <vxe-column title="Display Order" field="showOrder" sortable> </vxe-column>
      <vxe-column title="Creation Time" field="createTime" sortable>
        <template v-slot="scope">
          <span>{{ formatDateByStatsType(scope.row.createTime, 'day') }}</span>
        </template>
      </vxe-column>
      <vxe-column title="Operation" fixed="right" width="180px">
        <template v-slot="scope">
          <general-button
            text="Edit"
            :size="layoutStore.defaultFormItemSize"
            @btnClick="onEditFlowCategoryClick(scope.row)"
          />
          <general-button
            text="Delete"
            type="danger"
            :size="layoutStore.defaultFormItemSize"
            @btnClick="onDeleteFlowCategoryClick(scope.row)"
          />
        </template>
      </vxe-column>
      <template v-slot:pagination>
        <pagination
          :total="flowCategoryWidget.totalCount"
          :currentPage="flowCategoryWidget.currentPage"
          :pageSize="flowCategoryWidget.pageSize"
          size="default"
          @currentChange="flowCategoryWidget.onCurrentPageChange"
          @sizeChange="flowCategoryWidget.onPageSizeChange"
        />
      </template>
    </table-box>
  </div>
</template>

<script setup lang="ts">
import { Plus as ElIconPlus } from '@element-plus/icons-vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Dialog } from '@/components/Dialog';
import { useTable } from '@/common/hooks/useTable';
import { TableOptions } from '@/common/types/pagination';
import { ANY_OBJECT } from '@/types/generic';
import FlowCategoryController from '@/api/flow/FlowCategoryController';
import { useDate } from '@/common/hooks/useDate';
import { useLayoutStore } from '@/store';
import formEditFlowCategory from './formEditFlowCategory.vue';

const layoutStore = useLayoutStore();
const { formatDateByStatsType } = useDate();

const form = ref();
const formFlowCategory = reactive<ANY_OBJECT>({
  formFilter: {
    name: undefined,
    code: undefined,
  },
  formFilterCopy: {
    name: undefined,
    code: undefined,
  },
  isInit: false,
});

/**
 * FlowCategory Data Fetching Function, Returns Promise
 */
const loadFlowCategoryWidgetData = (params: ANY_OBJECT) => {
  if (params == null) params = {};
  params = {
    ...params,
    flowCategoryDtoFilter: {
      name: formFlowCategory.formFilterCopy.name,
      code: formFlowCategory.formFilterCopy.code,
    },
  };
  return new Promise((resolve, reject) => {
    FlowCategoryController.list(params)
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
 * FlowCategory Data Fetching Verification Function, Returns true to Continue Fetching, Returns false to Stop Fetching
 */
const loadFlowCategoryVerify = () => {
  formFlowCategory.formFilterCopy.name = formFlowCategory.formFilter.name;
  formFlowCategory.formFilterCopy.code = formFlowCategory.formFilter.code;
  return true;
};
const tableOptions: TableOptions<ANY_OBJECT> = {
  loadTableData: loadFlowCategoryWidgetData,
  verifyTableParameter: loadFlowCategoryVerify,
  paged: true,
  orderFieldName: 'showOrder',
  ascending: true,
};

const flowCategoryWidget = reactive(useTable(tableOptions));

/**
 * Update Flow Category Management
 */
const refreshFormFlowCategory = (reloadData = false) => {
  if (reloadData) {
    flowCategoryWidget.refreshTable(true, 1);
  } else {
    flowCategoryWidget.refreshTable();
  }
  if (!formFlowCategory.isInit) {
    // Initialize Dropdown Data
  }
  formFlowCategory.isInit = true;
};
const onReset = () => {
  form.value.resetFields();
  refreshFormFlowCategory(true);
};

/**
 * New
 */
const onAddFlowCategoryClick = () => {
  Dialog.show(
    'New',
    formEditFlowCategory,
    {
      area: '500px',
    },
    {
      //path: 'thirdAddFormFlowCategory',
    },
    {
      width: '500px',
      height: '300px',
      pathName: '/thirdParty/thirdAddFormFlowCategory',
    },
  )
    .then(() => {
      refreshFormFlowCategory();
    })
    .catch(e => {
      console.log(e);
    });
};
/**
 * Edit
 */
const onEditFlowCategoryClick = (row: ANY_OBJECT) => {
  Dialog.show(
    'Edit',
    formEditFlowCategory,
    {
      area: '500px',
    },
    {
      categoryId: row.categoryId,
      //path: 'thirdAddFormFlowCategory',
    },
    {
      width: '500px',
      height: '280px',
      pathName: '/thirdParty/thirdAddFormFlowCategory',
    },
  )
    .then(() => {
      flowCategoryWidget.refreshTable();
    })
    .catch(e => {
      console.log(e);
    });
};
/**
 * Delete
 */
const onDeleteFlowCategoryClick = (row: ANY_OBJECT) => {
  if (row.categoryId == null) {
    ElMessage.error('Request Failed, Required Parameter Found Empty!');
    return;
  }
  let params = {
    categoryId: row.categoryId,
  };

  ElMessageBox.confirm('Do you want to delete this process category?', '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      FlowCategoryController.delete(params)
        .then(() => {
          ElMessage.success('Deleted Successful');
          flowCategoryWidget.refreshTable();
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
  refreshFormFlowCategory();
};
const formInit = () => {
  refreshFormFlowCategory();
};

onMounted(() => {
  // Initialize Page Data
  formInit();
});

defineExpose({
  onResume,
});
</script>
