<template>
  <div>
    <table-box
      class="border-bottom-0 page-table"
      :data="formSysMenu.SysMenu.impl.dataList"
      :hasExtend="false"
      :tree-config="{ rowField: 'menuId', parentField: 'parentId' }"
      @refresh="refreshFormSysMenu"
    >
      <template v-slot:operator>
          <el-row type="flex" align="middle">
            <el-col :span="12" style="margin-bottom: 16px">
              <el-button
                type="primary"
                :icon="Plus"
                :size="layoutStore.defaultFormItemSize"
                :disabled="!checkPermCodeExist('formSysMenu:fragmentSysMenu:add')"
                @click="onCreateSysMenuClick()"
              >
                New
              </el-button>
            </el-col>
            <el-col :span="12">
              <filter-box
                :minMenuWidth=30
                :hasSearch="false"
                :hasReset="false"
                :hasRefresh="true"
                @search="refreshFormSysMenu"
              />
            </el-col>
          </el-row>
      </template>
      <vxe-column title="Menu Name" field="menuName" width="300px" tree-node> </vxe-column>
      <vxe-column title="Menu Icon" field="icon" width="100px">
        <template v-slot="scope">
          <orange-icon v-if="scope.row.icon" :icon="scope.row.icon" />
        </template>
      </vxe-column>
      <vxe-column title="Menu Order" field="showOrder" width="110px"> </vxe-column>
      <vxe-column title="Menu Type" field="menuType" width="110px">
        <template v-slot="scope">
          <el-tag :type="getMenuType(scope.row)" effect="light">{{
            SysMenuType.getValue(scope.row.menuType)
          }}</el-tag>
        </template>
      </vxe-column>
      <vxe-column title="Menu Route" field="formRouterName" min-width="250px"> </vxe-column>
      <vxe-column title="Operation" fixed="right" width="220px">
        <template v-slot="scope">
          <el-button
            @click="onEditSysMenuClick(scope.row)"
            type="primary"
            :size="layoutStore.defaultFormItemSize"
            link
            :disabled="
              !checkPermCodeExist('formSysMenu:fragmentSysMenu:update') ||
              (scope.row.onlineFormId != null && scope.row.menuType === SysMenuType.BUTTON)
            "
          >
            Edit
          </el-button>
          <el-button
            @click="onAddChildSysMenuClick(scope.row)"
            type="primary"
            :size="layoutStore.defaultFormItemSize"
            link
            :disabled="
              !checkPermCodeExist('formSysMenu:fragmentSysMenu:add') ||
              scope.row.menuType === SysMenuType.BUTTON
            "
          >
            Add
          </el-button>
          <el-button
            @click="onDeleteClick(scope.row)"
            type="danger"
            :size="layoutStore.defaultFormItemSize"
            link
            :disabled="
              !checkPermCodeExist('formSysMenu:fragmentSysMenu:delete') ||
              (scope.row.onlineFormId != null && scope.row.menuType === SysMenuType.BUTTON)
            "
          >
            Delete
          </el-button>
        </template>
      </vxe-column>
    </table-box>
  </div>
</template>

<script lang="ts">
export default {
  name: 'formSysMenu',
};
</script>

<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue';
import { ElMessageBox } from 'element-plus';
import { useDialog } from '@/components/Dialog/useDialog';
import { MenuItem } from '@/types/upms/menu';
import { SystemMenuController } from '@/api/system';
import TableBox from '@/components/TableBox/index.vue';
import { treeDataTranslate } from '@/common/utils';
import { SysMenuType } from '@/common/staticDict/index';
import OrangeIcon from '@/components/icons/index.vue';
import { usePermissions } from '@/common/hooks/usePermission';
import { T } from '@/types/generic';
import { TableOptions } from '@/common/types/pagination';
import { useTable } from '@/common/hooks/useTable';
import { TableData } from '@/common/types/table';
import { useLayoutStore } from '@/store';
import SysMenuEditForm from '../formEditSysMenu/index.vue';
import { useMenuTools } from './hooks';

let allMenuList: MenuItem[] = [];
const Dialog = useDialog();
const { checkPermCodeExist } = usePermissions();
const layoutStore = useLayoutStore();
const mainContextHeight = inject('mainContextHeight', document.documentElement.clientHeight);

const loadSysMenuData = (params: T): Promise<TableData<MenuItem>> => {
  return new Promise((resolve, reject) => {
    SystemMenuController.getMenuPermList(params)
      .then(res => {
        allMenuList = res.data;
        resolve({
          dataList: treeDataTranslate(res.data, 'menuId'),
          totalCount: res.data.length,
        });
      })
      .catch(e => {
        reject(e);
      });
  });
};

/**
 * Menu Data Load Verification Function, returns true to continue loading data, false to stop loading data
 */
const loadSysMenuVerify = () => {
  return true;
};

const tableOptions: TableOptions<MenuItem> = {
  loadTableData: loadSysMenuData,
  verifyTableParameter: loadSysMenuVerify,
  paged: false,
};

// Load Menu Data
const formSysMenu = reactive({
  formFilter: {},
  formFilterCopy: {},
  SysMenu: {
    impl: useTable(tableOptions),
  },
  isInit: false,
});

const refreshFormSysMenu = (reloadData = false) => {
  // Reload the data of the component
  if (reloadData) {
    formSysMenu.SysMenu.impl.refreshTable(true, 1);
  } else {
    formSysMenu.SysMenu.impl.refreshTable();
  }
  formSysMenu.isInit = true;
};

// Menu CRUD Operations
const onCreateSysMenuClick = () => {
  let params = {
    menuList: allMenuList,
  };
  Dialog.show<MenuItem>(
    'Create',
    SysMenuEditForm,
    {
      area: ['800px', '650px'],
    },
    params,
  )
    .then(res => {
      formSysMenu.SysMenu.impl.refreshTable();
    })
    .catch(e => {
      console.log('dialog reject', e);
    });
};

const onEditSysMenuClick = (row: MenuItem) => {
  SystemMenuController.viewMenu({ menuId: row.menuId })
    .then(res => {
      let params = {
        rowData: res.data,
        menuId: row.menuId,
        menuList: allMenuList,
      };

      Dialog.show(
        'Edit',
        SysMenuEditForm,
        {
          area: ['800px', '650px'],
        },
        params,
      )
        .then(res => {
          formSysMenu.SysMenu.impl.refreshTable();
        })
        .catch(e => {
          console.log('dialog reject', e);
        });
    })
    .catch(e => {
      console.error('viewMenu', e);
    });
};

const onAddChildSysMenuClick = (row: MenuItem) => {
  let params = {
    parentId: row.menuId,
    menuList: allMenuList,
  };

  Dialog.show(
    'Add Child Menu',
    SysMenuEditForm,
    {
      area: ['800px', '650px'],
    },
    params,
  )
    .then(() => {
      formSysMenu.SysMenu.impl.refreshTable();
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteClick = (row: MenuItem) => {
  let params = {
    menuId: row.menuId,
  };

  ElMessageBox.confirm(`Are you sure you want to delete the menu item【${row.menuName}】?`, '', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(res => {
      SystemMenuController.deleteMenu(params)
        .then(() => {
          formSysMenu.SysMenu.impl.refreshTable();
        })
        .catch(e => {
          console.log(e);
        });
    })
    .catch(e => {
      //console.log(e);
    });
};

const { getMenuType } = useMenuTools();

onMounted(() => {
  refreshFormSysMenu();
});
</script>
