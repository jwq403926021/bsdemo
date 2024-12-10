import { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router';
import Layout from '@/components/layout/index.vue';
import Welcome from '@/pages/welcome/index.vue';
import Login from '@/pages/login/index.vue';
import { useLayoutStore } from '@/store';

function getProps(route: RouteLocationNormalizedLoaded) {
  return route.query;
}

// 系统生成路由
export const routers: Array<RouteRecordRaw> = [
  {
    path: '/login',
    component: Login,
    name: 'login',
    meta: {
      title: '登录',
    },
  },
  {
    path: '/',
    component: Layout,
    name: 'main',
    props: getProps,
    redirect: {
      name: 'welcome',
    },
    meta: {
      title: 'Home',
      showOnly: true,
    },
    children: [
      {
        path: 'welcome',
        component: Welcome,
        name: 'welcome'
      },
      {
        path: 'formSysMenu',
        component: () =>
          useLayoutStore().supportColumn
            ? import('@/pages/upms/formSysMenu/formSysColumnMenu.vue')
            : import('@/pages/upms/formSysMenu/index.vue'),
        name: 'formSysMenu'
      },
      {
        path: 'formSysUser',
        component: () => import('@/pages/upms/formSysUser/index.vue'),
        name: 'formSysUser'
      },
      {
        path: 'formSysDept',
        component: () => import('@/pages/upms/formSysDept/index.vue'),
        name: 'formSysDept'
      },
      {
        path: 'formSysRole',
        component: () => import('@/pages/upms/formSysRole/index.vue'),
        name: 'formSysRole'
      },
      {
        path: 'formSysDataPerm',
        component: () => import('@/pages/upms/formSysDataPerm/index.vue'),
        name: 'formSysDataPerm'
      },
      {
        path: 'formSysPermCode',
        component: () => import('@/pages/upms/formSysPermCode/index.vue'),
        name: 'formSysPermCode'
      },
      {
        path: 'formSysPerm',
        component: () => import('@/pages/upms/formSysPerm/index.vue'),
        name: 'formSysPerm'
      },
      {
        path: 'formSysLoginUser',
        component: () => import('@/pages/upms/formSysLoginUser/index.vue'),
        name: 'formSysLoginUser'
      },
      // 岗位模块路由配置
      {
        path: 'formSysPost',
        component: () => import('@/pages/upms/formSysPost/index.vue'),
        name: 'formSysPost'
      },
      {
        path: 'formSysDeptPost',
        component: () => import('@/pages/upms/formSysDeptPost/index.vue'),
        name: 'formSysDeptPost',
        props: getProps,
      },
      {
        path: 'formSysDict',
        component: () => import('@/pages/upms/formSysDict/index.vue'),
        name: 'formSysDict',
      },
      {
        path: 'formSysOperationLog',
        component: () => import('@/pages/upms/formSysOperationLog/index.vue'),
        name: 'formSysOperationLog',
      },
      // 404
      {
        path: '/:pathMatch(.*)*',
        component: () => import('@/pages/error/404.vue'),
        name: 'NotFound',
        meta: {
          title: '404',
        },
      },
      // 在线表单
      {
        path: 'formOnlineDblink',
        component: () => import('@/pages/online/formOnlineDblink/index.vue'),
        name: 'formOnlineDblink',
        props: getProps,
      },
      {
        path: 'formOnlineDict',
        component: () => import('@/pages/online/formOnlineDict/index.vue'),
        name: 'formOnlineDict',
        props: getProps,
      },
      {
        path: 'formOnlinePage',
        component: () => import('@/pages/online/formOnlinePage/index.vue'),
        name: 'formOnlinePage',
        props: getProps,
      },
      {
        path: 'onlineForm',
        component: () => import('@/pages/online/OnlinePageRender/index.vue'),
        name: 'onlineForm',
        props: getProps,
      },
      // 工作流模块
      {
        path: 'formMessage',
        component: () => import('@/pages/workflow/formMessage/index.vue'),
        name: 'formMessage',
        props: getProps,
      },
      {
        path: 'formFlowCategory',
        component: () => import('@/pages/workflow/flowCategory/formFlowCategory.vue'),
        name: 'formFlowCategory',
        props: getProps,
      },
      {
        path: 'formFlowEntry',
        component: () => import('@/pages/workflow/flowEntry/formFlowEntry.vue'),
        name: 'formFlowEntry',
        props: getProps,
      },
      {
        path: 'formFlowDblink',
        component: () => import('@/pages/workflow/formFlowDblink/index.vue'),
        name: 'formFlowDblink',
        props: getProps,
      },
      {
        path: 'formAllInstance',
        component: () => import('@/pages/workflow/taskManager/formAllInstance.vue'),
        name: 'formAllInstance',
        props: getProps,
      },
      {
        path: 'handlerFlowTask',
        component: () => import('@/pages/workflow/handlerFlowTask/index.vue'),
        name: 'handlerFlowTask',
        props: getProps,
        children: [
          // 静态表单路由设置
        ],
      },
      {
        path: 'formMyTask',
        component: () => import('@/pages/workflow/taskManager/formMyTask.vue'),
        name: 'formMyTask',
        props: getProps,
      },
      {
        path: 'formMyApprovedTask',
        component: () => import('@/pages/workflow/taskManager/formMyApprovedTask.vue'),
        name: 'formMyApprovedTask',
        props: getProps,
      },
      {
        path: 'formMyHistoryTask',
        component: () => import('@/pages/workflow/taskManager/formMyHistoryTask.vue'),
        name: 'formMyHistoryTask',
        props: getProps,
      },
    ],
  },
  // 第三方接入路由
  {
    path: '/thirdParty',
    component: () => import('@/components/thirdParty/index.vue'),
    name: 'thirdParty',
    props: getProps,
    children: [
      // 流程分类列表
      {
        path: 'thirdFormFlowCategory',
        name: 'thirdFormFlowCategory',
        props: getProps,
        component: () => import('@/pages/workflow/flowCategory/formFlowCategory.vue'),
      },
      // 流程分类 新增、编辑
      {
        path: 'thirdAddFormFlowCategory',
        name: 'thirdAddFormFlowCategory',
        props: getProps,
        component: () => import('@/pages/workflow/flowCategory/formEditFlowCategory.vue'),
      },
      // 流程实例列表
      {
        path: 'thirdFormAllInstance',
        name: 'thirdFormAllInstance',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/formAllInstance.vue'),
      },
      // 流程图
      {
        path: 'thirdFormTaskProcessViewer',
        name: 'thirdFormTaskProcessViewer',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/formTaskProcessViewer.vue'),
      },
      // 流程终止
      {
        path: 'thirdFormStopTaskInstance',
        name: 'thirdFormStopTaskInstance',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/stopTask.vue'),
      },
      // 选择用户-处理用户
      {
        path: 'thirdTaskUserSelect',
        name: 'thirdTaskUserSelect',
        props: getProps,
        component: () => import('@/pages/workflow/components/TaskUserSelect.vue'),
      },
      // 选择用户-跳转节点
      {
        path: 'thirdSelectUserTask',
        name: 'thirdSelectUserTask',
        props: getProps,
        component: () => import('@/pages/workflow/components/UserTaskSelect/userTaskSelectDlg.vue'),
      },
      // 流程设计
      {
        path: 'thirdEditFlowDblink',
        name: 'thirdEditFlowDblink',
        props: getProps,
        component: () => import('@/pages/workflow/formFlowDblink/editFlowDblink.vue'),
      },
      {
        path: 'thirdFormFlowEntry',
        name: 'thirdFormFlowEntry',
        props: getProps,
        component: () => import('@/pages/workflow/flowEntry/formFlowEntry.vue'),
      },
      {
        path: 'thirdFormEditFlowEntry',
        name: 'thirdFormEditFlowEntry',
        props: getProps,
        component: () => import('@/pages/workflow/flowEntry/formEditFlowEntry.vue'),
      },
      {
        path: 'thirdFormPublishedFlowEntry',
        name: 'thirdFormPublishedFlowEntry',
        props: getProps,
        component: () => import('@/pages/workflow/flowEntry/formPublishedFlowEntry.vue'),
      },
      {
        path: 'thirdHandlerFlowTask',
        name: 'thirdHandlerFlowTask',
        props: getProps,
        component: () => import('@/pages/workflow/handlerFlowTask/index.vue'),
      },
      // 流程设计-候选用户组
      {
        path: 'thirdTaskGroupSelect',
        name: 'thirdTaskGroupSelect',
        props: getProps,
        component: () => import('@/pages/workflow/components/TaskGroupSelect.vue'),
      },
      // 流程设计-选择岗位
      {
        path: 'thirdTaskPostSelect',
        name: 'thirdTaskPostSelect',
        props: getProps,
        component: () => import('@/pages/workflow/components/TaskPostSelect.vue'),
      },
      // 流程设计-抄送
      {
        path: 'thirdAddCopyForItem',
        name: 'thirdAddCopyForItem',
        props: getProps,
        component: () => import('@/pages/workflow/components/CopyForSelect/addCopyForItem.vue'),
      },
      // 流程设计-任务操作按钮
      {
        path: 'thirdEditOperation',
        name: 'thirdEditOperation',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/formEditOperation.vue'),
      },
      // 流程设计-设置表单权限
      {
        path: 'thirdSetOnlineFormAuth',
        name: 'thirdSetOnlineFormAuth',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/formSetOnlineFormAuth.vue'),
      },
      // 流程设计-添加变量
      {
        path: 'thirdFormEditFlowEntryVariable',
        name: 'thirdFormEditFlowEntryVariable',
        props: getProps,
        component: () => import('@/pages/workflow/flowEntry/formEditFlowEntryVariable.vue'),
      },
      // 流程设计- 新建状态
      {
        path: 'thirdFormEditFlowEntryStatus',
        name: 'thirdFormEditFlowEntryStatus',
        props: getProps,
        component: () => import('@/pages/workflow/flowEntry/formEditFlowEntryStatus.vue'),
      },
      // 流程设计-新建状态
      {
        path: 'thirdTaskCommit',
        name: 'thirdTaskCommit',
        props: getProps,
        component: () => import('@/pages/workflow/components/TaskCommit.vue'),
      },
      // 自动化任务 - 编辑过滤条件
      {
        path: 'thirdEditTableFilter',
        name: 'thirdEditTableFilter',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/editSrcTableFilter.vue'),
      },
      // 自动化任务 - 编辑更新字段
      {
        path: 'thirdEditFieldUpdateData',
        name: 'thirdEditFieldUpdateData',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/editInsertData.vue'),
      },
      // 自动化任务 - 编辑响应字段
      {
        path: 'thirdEditResponseData',
        name: 'thirdEditResponseData',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/editResponseData.vue'),
      },
      // 自动化任务 - 编辑HTTP请求参数
      {
        path: 'thirdEditHttpRequestParam',
        name: 'thirdEditHttpRequestParam',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/editHttpParameter.vue'),
      },
      // 自动化任务 - 编辑HTTP请求头
      {
        path: 'thirdEditHttpHeader',
        name: 'thirdEditHttpHeader',
        props: getProps,
        component: () => import('@/pages/workflow/package/refactor/form/editHttpHeader.vue'),
      },
      // 待办任务
      {
        path: 'thirdFormMyTask',
        name: 'thirdFormMyTask',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/formMyTask.vue'),
      },
      // 历史任务
      {
        path: 'thirdFormMyHistoryTask',
        name: 'thirdFormMyHistoryTask',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/formMyHistoryTask.vue'),
      },
      // 已办任务
      {
        path: 'thirdFormMyApprovedTask',
        name: 'thirdFormMyApprovedTask',
        props: getProps,
        component: () => import('@/pages/workflow/taskManager/formMyApprovedTask.vue'),
      },
      // 在线表单部分
      {
        path: 'thirdOnlineForm',
        name: 'thirdOnlineForm',
        props: getProps,
        component: () => import('@/pages/online/OnlinePageRender/index.vue'),
      },
      {
        path: 'thirdOnlineEditForm',
        name: 'thirdOnlineEditForm',
        props: getProps,
        component: () => import('@/pages/online/OnlinePageRender/OnlineEditForm/index.vue'),
      },
      {
        path: 'thirdFormOnlineDict',
        name: 'thirdFormOnlineDict',
        props: getProps,
        component: () => import('@/pages/online/formOnlineDict/index.vue'),
      },
      {
        path: 'thirdEditOnlineDict',
        name: 'thirdEditOnlineDict',
        props: getProps,
        component: () => import('@/pages/online/formOnlineDict/EditOnlineDict.vue'),
      },
      {
        path: 'thirdOnlinePage',
        name: 'thirdOnlinePage',
        props: getProps,
        component: () => import('@/pages/online/formOnlinePage/index.vue'),
      },
      {
        path: 'thirdEditOnlinePage',
        name: 'thirdEditOnlinePage',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/index.vue'),
      },
      {
        path: 'thirdEditOnlineForm',
        name: 'thirdEditOnlineForm',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/editOnlineForm.vue'),
      },
      {
        path: 'thirdEditPageDatasource',
        name: 'thirdEditPageDatasource',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/editOnlinePageDatasource.vue'),
      },
      {
        path: 'thirdEditPageRelation',
        name: 'thirdEditPageRelation',
        props: getProps,
        component: () =>
          import('@/pages/online/editOnlinePage/editOnlinePageDatasourceRelation.vue'),
      },
      {
        path: 'thirdSetOnlineTableColumnRule',
        name: 'thirdSetOnlineTableColumnRule',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/setOnlineTableColumnRule.vue'),
      },
      {
        path: 'thirdEditVirtualColumnFilter',
        name: 'thirdEditVirtualColumnFilter',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/editVirtualColumnFilter.vue'),
      },
      {
        path: 'thirdEditTableColumn',
        name: 'thirdEditTableColumn',
        props: getProps,
        component: () => import('@/pages/online/editOnlinePage/formDesign/editTableColumn.vue'),
      },
      {
        path: 'thirdEditCustomFormOperate',
        name: 'thirdEditCustomFormOperate',
        props: getProps,
        component: () =>
          import('@/pages/online/editOnlinePage/formDesign/components/EditCustomFormOperate.vue'),
      },
      {
        path: 'thirdEditFormField',
        name: 'thirdEditFormField',
        props: getProps,
        component: () =>
          import('@/pages/online/editOnlinePage/formDesign/components/EditFormField.vue'),
      },
      {
        path: 'thirdEditDictParamValue',
        name: 'thirdEditDictParamValue',
        props: getProps,
        component: () =>
          import(
            '@/pages/online/editOnlinePage/formDesign/components/CustomWidgetDictSetting/EditDictParamValue.vue'
          ),
      },
      {
        path: 'thirdEditOnlineTableColumn',
        name: 'thirdEditOnlineTableColumn',
        props: getProps,
        component: () =>
          import(
            '@/pages/online/editOnlinePage/formDesign/components/OnlineTableColumnSetting/editOnlineTableColumn.vue'
          ),
      },
      {
        path: 'thirdEditOnlineTabPanel',
        name: 'thirdEditOnlineTabPanel',
        props: getProps,
        component: () =>
          import(
            '@/pages/online/editOnlinePage/formDesign/components/OnlineTabPanelSetting/editOnlineTabPanel.vue'
          ),
      },
      {
        path: 'thirdOnlineDblink',
        name: 'thirdOnlineDblink',
        props: getProps,
        component: () => import('@/pages/online/formOnlineDblink/index.vue'),
      },
      {
        path: 'thirdEditOnlineDblink',
        name: 'thirdEditOnlineDblink',
        props: getProps,
        component: () => import('@/pages/online/formOnlineDblink/EditOnlineDblink.vue'),
      },
      // 通用
      {
        path: 'thirdEditDictParamValue2',
        name: 'thirdEditDictParamValue2',
        props: getProps,
        component: () =>
          import(
            '@/online/components/WidgetAttributeSetting/components/CustomWidgetDictSetting/editDictParamValue.vue'
          ),
      },
      {
        path: 'thirdEditOnlineTableColumn2',
        name: 'thirdEditOnlineTableColumn2',
        props: getProps,
        component: () =>
          import(
            '@/online/components/WidgetAttributeSetting/components/OnlineTableColumnSetting/editOnlineTableColumn.vue'
          ),
      },
      {
        path: 'thirdEditOnlineTabPanel2',
        name: 'thirdEditOnlineTabPanel2',
        props: getProps,
        component: () =>
          import(
            '@/online/components/WidgetAttributeSetting/components/OnlineTabPanelSetting/editOnlineTabPanel.vue'
          ),
      },
      {
        path: 'thirdSelectDept',
        name: 'thirdSelectDept',
        props: getProps,
        component: () => import('@/components/DeptSelect/DeptSelectDlg.vue'),
      },
      {
        path: 'thirdSelectUser',
        name: 'thirdSelectUser',
        props: getProps,
        component: () => import('@/components/UserSelect/UserSelectDlg.vue'),
      },
    ],
  },
];
