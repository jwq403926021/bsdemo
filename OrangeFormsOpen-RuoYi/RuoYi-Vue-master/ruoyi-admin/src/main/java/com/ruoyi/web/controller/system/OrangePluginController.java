package com.ruoyi.web.controller.system;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.page.PageMethod;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.entity.SysDept;
import com.ruoyi.common.core.domain.entity.SysMenu;
import com.ruoyi.common.core.domain.entity.SysRole;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.system.domain.SysPost;
import com.ruoyi.system.domain.SysRoleDept;
import com.ruoyi.system.mapper.SysDeptMapper;
import com.ruoyi.system.mapper.SysRoleDeptMapper;
import com.ruoyi.system.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 集成在若依中的橙单插件接口。
 *
 * @author 橙单团队
 */
@RestController
@RequestMapping("/orangePlugin")
public class OrangePluginController extends BaseController {

    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private ISysRoleService roleService;
    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private ISysUserService userService;
    @Autowired
    private ISysPostService postService;

    /**
     * sysDeptMapper和sysRoleMapper本来不应该在controller中直接使用的。
     * 可以自己分别修改若依的SysDeptService和SysRoleService方法封装一下这两个mapper的接口。
     * 我们这里之所以直接使用，主要还是为了简化文档的说明，以及尽量减少对若依基础框架代码的修改。
     */
    @Autowired
    private SysDeptMapper sysDeptMapper;
    @Autowired
    private SysRoleDeptMapper roleDeptMapper;

    /**
     * 在线表单数据库链接管理管理菜单关联的接口权限集合。
     */
    private static Set<String> onlineDblinkPerms;
    /**
     * 在线表单页面管理菜单关联的接口权限集合。
     */
    private static Set<String> onlinePagePerms;
    /**
     * 在线表单字典管理菜单关联的接口权限集合。
     */
    private static Set<String> onlineDictPerms;
    /**
     * 在线表单业务页面需要访问的运行时接口。第三方接入目前尚不支持"读写"权限的区分。既只要具有该菜单的权限，
     * 那么就具备读写权限。
     */
    private static Set<String> onlineOperationPerms;
    /**
     * 在线表单的白名单接口集合。
     */
    private static Set<String> onlineWhitelistPerms;
    /**
     * 工作流分类接口集合。
     */
    private static Set<String> flowCategoryPerms;
    /**
     * 工作流设计菜单的接口集合。
     */
    private static Set<String> flowEntryPerms;
    /**
     * 工作流流程实例菜单的接口集合。
     */
    private static Set<String> flowInstancePerms;
    /**
     * 工作流待办任务菜单的接口集合。
     */
    private static Set<String> flowRuntimeTaskPerms;
    /**
     * 工作流历史任务菜单的接口集合。
     */
    private static Set<String> flowHistoricTaskPerms;
    /**
     * 工作流已办任务菜单的接口集合。
     */
    private static Set<String> flowFinishTaskPerms;
    /**
     * 工作流工单中与流程定义标识相关的接口集合。
     */
    private static Set<String> flowWorkOrderOperationPerms;
    /**
     * 工作流工单中的通用接口集合。
     */
    private static Set<String> flowWorkOrderCommonPerms;
    /**
     * 工作流所需的白名单接口集合。
     */
    private static Set<String> flowWhitelistPerms;
    /**
     * 报表数据库链接管理菜单关联的接口权限集合。
     */
    private static Set<String> reportDblinkPerms;
    /**
     * 报表数据集管理菜单关联的接口权限集合。
     */
    private static Set<String> reportDatasetPerms;
    /**
     * 报表字典管理菜单关联的接口权限集合。
     */
    private static Set<String> reportDictPerms;
    /**
     * 报表页面管理菜单关联的接口权限集合。
     */
    private static Set<String> reportPagePerms;
    /**
     * 报表打印管理菜单关联的接口权限集合。
     */
    private static Set<String> reportPrintPerms;
    /**
     * 报表打印所需的白名单接口集合。
     */
    private static Set<String> reportWhitelistPerms;
    /**
     * 报表业务页面需要访问的运行时接口。
     */
    private static Set<String> reportOperationPerms;

    static {
        onlinePagePerms = CollUtil.newHashSet(
                "/admin/online/onlineDblink/list",
                "/admin/online/onlineDblink/listDblinkTableColumns",
                "/admin/online/onlineDblink/listDblinkTables",
                "/admin/online/onlineForm/clone",
                "/admin/online/onlineForm/delete",
                "/admin/online/onlineForm/update",
                "/admin/online/onlineForm/add",
                "/admin/online/onlineForm/list",
                "/admin/online/onlineDatasourceRelation/delete",
                "/admin/online/onlineDatasourceRelation/update",
                "/admin/online/onlineDatasourceRelation/add",
                "/admin/online/onlineDatasourceRelation/view",
                "/admin/online/onlineDatasourceRelation/list",
                "/admin/online/onlineDatasource/delete",
                "/admin/online/onlineDatasource/update",
                "/admin/online/onlineDatasource/add",
                "/admin/online/onlineDatasource/view",
                "/admin/online/onlineDatasource/list",
                "/admin/online/onlinePage/viewOnlinePageDatasource",
                "/admin/online/onlinePage/updateOnlinePageDatasource",
                "/admin/online/onlinePage/deleteOnlinePageDatasource",
                "/admin/online/onlinePage/addOnlinePageDatasource",
                "/admin/online/onlinePage/listNotInOnlinePageDatasource",
                "/admin/online/onlinePage/listOnlinePageDatasource",
                "/admin/online/onlinePage/delete",
                "/admin/online/onlinePage/updatePublished",
                "/admin/online/onlinePage/update",
                "/admin/online/onlinePage/add",
                "/admin/online/onlinePage/view",
                "/admin/online/onlinePage/listAllPageAndForm",
                "/admin/online/onlinePage/list",
                "/admin/online/onlineRule/delete",
                "/admin/online/onlineRule/update",
                "/admin/online/onlineRule/add",
                "/admin/online/onlineRule/view",
                "/admin/online/onlineRule/list",
                "/admin/online/onlineColumn/viewOnlineColumnRule",
                "/admin/online/onlineColumn/updateOnlineColumnRule",
                "/admin/online/onlineColumn/deleteOnlineColumnRule",
                "/admin/online/onlineColumn/addOnlineColumnRule",
                "/admin/online/onlineColumn/listNotInOnlineColumnRule",
                "/admin/online/onlineColumn/listOnlineColumnRule",
                "/admin/online/onlineColumn/refresh",
                "/admin/online/onlineColumn/delete",
                "/admin/online/onlineColumn/update",
                "/admin/online/onlineColumn/add",
                "/admin/online/onlineColumn/view",
                "/admin/online/onlineColumn/list",
                "/admin/online/onlineDblink/testConnection",
                "/admin/online/onlineVirtualColumn/delete",
                "/admin/online/onlineVirtualColumn/update",
                "/admin/online/onlineVirtualColumn/add",
                "/admin/online/onlineVirtualColumn/view",
                "/admin/online/onlineVirtualColumn/list");

        onlineDblinkPerms = CollUtil.newHashSet(
                "/admin/online/onlineDblink/add",
                "/admin/online/onlineDblink/update",
                "/admin/online/onlineDblink/delete",
                "/admin/online/onlineDblink/list",
                "/admin/online/onlineDblink/view");

        onlineDictPerms = CollUtil.newHashSet(
                "/admin/online/onlineDict/delete",
                "/admin/online/onlineDict/update",
                "/admin/online/onlineDict/add",
                "/admin/online/onlineDict/view",
                "/admin/online/onlineDict/list");

        onlineOperationPerms = CollUtil.newHashSet(
                "/admin/online/onlineOperation/viewByDatasourceId/",
                "/admin/online/onlineOperation/viewByOneToManyRelationId/",
                "/admin/online/onlineOperation/listByDatasourceId/",
                "/admin/online/onlineOperation/listByOneToManyRelationId/",
                "/admin/online/onlineOperation/exportByDatasourceId/",
                "/admin/online/onlineOperation/exportByOneToManyRelationId/",
                "/admin/online/onlineOperation/downloadDatasource/",
                "/admin/online/onlineOperation/downloadOneToManyRelation/",
                "/admin/online/onlineOperation/addDatasource/",
                "/admin/online/onlineOperation/addOneToManyRelation/",
                "/admin/online/onlineOperation/updateDatasource/",
                "/admin/online/onlineOperation/updateOneToManyRelation/",
                "/admin/online/onlineOperation/deleteDatasource/",
                "/admin/online/onlineOperation/deleteOneToManyRelation/",
                "/admin/online/onlineOperation/deleteBatchDatasource/",
                "/admin/online/onlineOperation/deleteBatchOneToManyRelation/",
                "/admin/online/onlineOperation/uploadDatasource/",
                "/admin/online/onlineOperation/uploadOneToManyRelation/");

        onlineWhitelistPerms = CollUtil.newHashSet(
                "/admin/online/onlineForm/render",
                "/admin/online/onlineForm/view",
                "/admin/online/onlineOperation/listDict",
                "/admin/commonext/bizwidget/list",
                "/admin/commonext/bizwidget/view");

        flowCategoryPerms = CollUtil.newHashSet(
                "/admin/flow/flowCategory/list",
                "/admin/flow/flowCategory/add",
                "/admin/flow/flowCategory/update",
                "/admin/flow/flowCategory/delete",
                "/admin/flow/flowCategory/view"
        );

        flowEntryPerms = CollUtil.newHashSet(
                "/admin/flow/flowEntry/activateFlowEntryPublish",
                "/admin/flow/flowEntry/add",
                "/admin/flow/flowEntry/delete",
                "/admin/flow/flowEntry/list",
                "/admin/flow/flowEntry/listFlowEntryPublish",
                "/admin/flow/flowEntry/publish",
                "/admin/flow/flowEntry/suspendFlowEntryPublish",
                "/admin/flow/flowEntry/update",
                "/admin/flow/flowEntry/updateMainVersion",
                "/admin/flow/flowEntry/view",
                "/admin/flow/flowEntryVariable/add",
                "/admin/flow/flowEntryVariable/delete",
                "/admin/flow/flowEntryVariable/list",
                "/admin/flow/flowEntryVariable/update",
                "/admin/flow/flowEntryVariable/view",
                "/admin/flow/flowOnlineOperation/download",
                "/admin/flow/flowOnlineOperation/startPreview",
                "/admin/flow/flowOnlineOperation/upload",
                "/admin/flow/flowOperation/startOnly",
                "/admin/flow/flowOperation/viewInitialTaskInfo",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/online/onlineColumn/list",
                "/admin/online/onlineDatasourceRelation/list",
                "/admin/online/onlineForm/list",
                "/admin/online/onlineForm/render",
                "/admin/online/onlinePage/list",
                "/admin/online/onlinePage/listOnlinePageDatasource",
                "/admin/online/onlineVirtualColumn/list",
                "/admin/upms/sysUser/list"
        );

        flowInstancePerms = CollUtil.newHashSet(
                "/admin/flow/flowOnlineOperation/viewHistoricProcessInstance",
                "/admin/flow/flowOperation/deleteProcessInstance",
                "/admin/flow/flowOperation/listAllHistoricProcessInstance",
                "/admin/flow/flowOperation/stopProcessInstance",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/viewInitialHistoricTaskInfo",
                "/admin/flow/flowOperation/viewProcessBpmn"
        );

        flowRuntimeTaskPerms = CollUtil.newHashSet(
                "/admin/flow/flowOnlineOperation/download",
                "/admin/flow/flowOnlineOperation/submitUserTask",
                "/admin/flow/flowOnlineOperation/upload",
                "/admin/flow/flowOnlineOperation/viewUserTask",
                "/admin/flow/flowOperation/listFlowTaskComment",
                "/admin/flow/flowOperation/listRuntimeTask",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/flow/flowOperation/viewRuntimeTaskInfo",
                "/admin/online/onlineForm/render"
        );

        flowHistoricTaskPerms = CollUtil.newHashSet(
                "/admin/flow/flowOnlineOperation/download",
                "/admin/flow/flowOnlineOperation/viewHistoricProcessInstance",
                "/admin/flow/flowOperation/listFlowTaskComment",
                "/admin/flow/flowOperation/listHistoricProcessInstance",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/viewInitialHistoricTaskInfo",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/online/onlineForm/render"
        );

        flowFinishTaskPerms = CollUtil.newHashSet(
                "/admin/flow/flowOnlineOperation/viewHistoricProcessInstance",
                "/admin/flow/flowOperation/listFlowTaskComment",
                "/admin/flow/flowOperation/listHistoricTask",
                "/admin/flow/flowOperation/submitConsign",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/viewHistoricTaskInfo",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/online/onlineForm/render",
                "/admin/online/onlineForm/view"
        );

        flowWorkOrderOperationPerms = CollUtil.newHashSet(
                "admin/online/flowOnlineOperation/startAndTakeUserTask/",
                "admin/online/flowOnlineOperation/startAndSaveDraft/",
                "admin/online/flowOnlineOperation/listWorkOrder/",
                "admin/online/flowOnlineOperation/printWorkOrder/"
        );

        flowWorkOrderCommonPerms = CollUtil.newHashSet(
                "/admin/online/onlineForm/view",
                "/admin/online/onlineForm/render",
                "/admin/flow/flowOperation/viewInitialHistoricTaskInfo",
                "/admin/flow/flowOperation/startOnly",
                "/admin/flow/flowOperation/viewInitialTaskInfo",
                "/admin/flow/flowOperation/viewRuntimeTaskInfo",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/listFlowTaskComment",
                "/admin/flow/flowOperation/cancelWorkOrder",
                "/admin/flow/flowOnlineOperation/viewUserTask",
                "/admin/flow/flowOnlineOperation/viewHistoricProcessInstance",
                "/admin/flow/flowOnlineOperation/submitUserTask",
                "/admin/flow/flowOnlineOperation/upload",
                "/admin/flow/flowOnlineOperation/download",
                "/admin/flow/flowOperation/submitConsign"
        );

        flowWhitelistPerms = CollUtil.newHashSet(
                "/admin/flow/flowCategory/listDict",
                "/admin/flow/flowEntry/listDict",
                "/admin/flow/flowEntry/viewDict",
                "/admin/flow/flowOnlineOperation/listFlowEntryForm",
                "/admin/flow/flowOnlineOperation/viewCopyBusinessData",
                "/admin/flow/flowOnlineOperation/viewDraftData",
                "/admin/flow/flowOperation/viewDraftData",
                "/admin/flow/flowOperation/countRuntimeTask",
                "/admin/flow/flowOperation/viewInitialTaskInfo",
                "/admin/flow/flowOperation/viewRuntimeTaskInfo",
                "/admin/flow/flowOperation/viewHistoricTaskInfo",
                "/admin/flow/flowOperation/viewInitialHistoricTaskInfo",
                "/admin/flow/flowOperation/viewTaskUserInfo",
                "/admin/flow/flowOperation/submitConsign",
                "/admin/flow/flowOperation/listMultiSignAssignees",
                "/admin/flow/flowOperation/listFlowTaskComment",
                "/admin/flow/flowOperation/viewProcessBpmn",
                "/admin/flow/flowOperation/viewHighlightFlowData",
                "/admin/flow/flowOperation/cancelWorkOrder",
                "/admin/flow/flowOperation/remindRuntimeTask",
                "/admin/flow/flowOperation/listRejectCandidateUserTask",
                "/admin/flow/flowOperation/rejectToStartUserTask",
                "/admin/flow/flowOperation/rejectRuntimeTask",
                "/admin/flow/flowOperation/revokeHistoricTask",
                "/admin/flow/flowOperation/freeJumpTo",
                "/admin/flow/flowOperation/viewCopyBusinessData",
                "/admin/flow/flowMessage/getMessageCount",
                "/admin/flow/flowMessage/listRemindingTask",
                "/admin/flow/flowMessage/listCopyMessage"
        );

        reportDictPerms = CollUtil.newHashSet(
                "/admin/report/reportDict/add",
                "/admin/report/reportDict/update",
                "/admin/report/reportDict/delete",
                "/admin/report/reportDict/list",
                "/admin/report/reportDict/view");

        reportPagePerms = CollUtil.newHashSet(
                "/admin/report/reportPageGroup/add",
                "/admin/report/reportPageGroup/update",
                "/admin/report/reportPageGroup/delete",
                "/admin/report/reportPageGroup/list",
                "/admin/report/reportPageGroup/view",
                "/admin/report/reportPage/add",
                "/admin/report/reportPage/update",
                "/admin/report/reportPage/delete",
                "/admin/report/reportPage/list");

        reportPrintPerms = CollUtil.newHashSet(
                "/admin/report/reportPrintGroup/add",
                "/admin/report/reportPrintGroup/update",
                "/admin/report/reportPrintGroup/delete",
                "/admin/report/reportPrintGroup/list",
                "/admin/report/reportPrintGroup/view",
                "/admin/report/reportPrint/add",
                "/admin/report/reportPrint/update",
                "/admin/report/reportPrint/delete",
                "/admin/report/reportPrint/list",
                "/admin/report/reportPrint/view");

        reportDblinkPerms = CollUtil.newHashSet(
                "/admin/report/reportDblink/add",
                "/admin/report/reportDblink/update",
                "/admin/report/reportDblink/delete",
                "/admin/report/reportDblink/list",
                "/admin/report/reportDblink/view",
                "/admin/report/reportDblink/listAllTables",
                "/admin/report/reportDblink/listTableColumn");

        reportDatasetPerms = CollUtil.newHashSet(
                "/admin/report/reportDatasetGroup/add",
                "/admin/report/reportDatasetGroup/update",
                "/admin/report/reportDatasetGroup/delete",
                "/admin/report/reportDatasetGroup/list",
                "/admin/report/reportDatasetGroup/view",
                "/admin/report/reportDataset/add",
                "/admin/report/reportDataset/update",
                "/admin/report/reportDataset/delete",
                "/admin/report/reportDataset/list",
                "/admin/report/reportDataset/view",
                "/admin/report/reportDataset/sync",
                "/admin/report/reportDataset/previewDataset",
                "/admin/report/reportDataset/listDataWithColumn",
                "/admin/report/reportDatasetColumn/update",
                "/admin/report/reportDatasetColumn/view",
                "/admin/report/reportDatasetRelation/add",
                "/admin/report/reportDatasetRelation/update",
                "/admin/report/reportDatasetRelation/delete",
                "/admin/report/reportDatasetRelation/list",
                "/admin/report/reportDatasetRelation/view",
                "/admin/report/reportOperation/previewData");

        reportWhitelistPerms = CollUtil.newHashSet(
                "/admin/report/reportDblink/testConnection",
                "/admin/report/reportDblink/listDict",
                "/admin/report/reportDict/listAllGlobalDict",
                "/admin/report/reportDict/listDict",
                "/admin/report/reportDict/listDictData",
                "/admin/report/reportPrint/preview",
                "/admin/report/reportPrint/listAll",
                "/admin/report/reportDataset/listByIds",
                "/admin/report/reportDataset/listDictByIds",
                "/admin/report/reportDataset/listDict",
                "/admin/report/reportPageGroup/listAll",
                "/admin/report/reportDatasetGroup/listAll",
                "/admin/report/reportPage/view");

        reportOperationPerms = CollUtil.newHashSet(
                "/admin/report/reportOperation/listData/");
    }

    /**
     * 验证Token并构造TokenData对象数据的访问接口。
     * GET请求，同时getTokenData的路径后缀，以及参数形式和参数名，必须与本示例保持一致。
     * 因为橙单的代码中会使用该值。
     *
     * @param token 会话token。
     * @return 橙单指定的TokenData格式。
     */
    @GetMapping("/getTokenData")
    public JSONObject getTokenData(@RequestParam String token) {
        LoginUser loginUser = super.getLoginUser();
        if (loginUser == null) {
            return makeResultData(false, "当前会话已过期或不存在！", null);
        }
        SysUser sysUser = loginUser.getUser();
        JSONObject tokenData = this.userConverter(sysUser);
        tokenData.put("sessionId", loginUser.getToken());
        tokenData.put("isAdmin", sysUser.isAdmin());
        tokenData.put("token", token);
        // makeResultData返回的对象，是橙单指定的格式，必须保持一致。
        return this.makeResultData(true, null, tokenData);
    }

    /**
     * 获取用户操作权限和数据权限数据的接口。
     * GET请求，同时getPermData的路径后缀，以及参数形式和参数名，必须与本示例保持一致。
     * 因为橙单的代码中会使用该值。
     *
     * @param token 会话token。
     * @return 橙单指定的权限数据格式。
     */
    @GetMapping("/getPermData")
    public JSONObject getPermData(@RequestParam String token) {
        LoginUser loginUser = super.getLoginUser();
        // 若依中获取当前登录用户菜单权限的查询操作。
        List<SysMenu> menuList = menuService.selectMenuList(loginUser.getUserId());
        menuList = menuList.stream().filter(m -> StrUtil.isNotBlank(m.getQuery())).collect(Collectors.toList());
        boolean hasOnlineDblinkPerms = false;
        boolean hasOnlinePagePerms = false;
        boolean hasOnlineDictPerms = false;

        boolean hasFlowCategoryPerms = false;
        boolean hasFlowEntryPerms = false;
        boolean hasFlowInstancePerms = false;
        boolean hasFlowRuntimeTaskPerms = false;
        boolean hasFlowHistoricTaskPerms = false;
        boolean hasFlowFinishTaskPerms = false;
        boolean hasFlowWorkOrderOperationPerms = false;

        boolean hasReportDblinkPerms = false;
        boolean hasReportDictPerms = false;
        boolean hasReportDatasetPerms = false;
        boolean hasReportPagePerms = false;
        boolean hasReportPrintPerms = false;

        // 操作权限列表，收集后会返回给橙单。
        List<String> urlPerms = new LinkedList<>();
        // 菜单的"路由参数"中，只要包含了指定的字符串，就被视为拥有"菜单管理"的权限了。
        // 下面的字符串部分，如"/#/thirdParty/thirdOnlineDblink"，必须和菜单中的配置保持一致。具体配置见下面的截图。
        for (SysMenu m : menuList) {
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdOnlineDblink")) {
                hasOnlineDblinkPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdOnlinePage")) {
                hasOnlinePagePerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormOnlineDict")) {
                hasOnlineDictPerms = true;
            }

            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormFlowCategory")) {
                hasFlowCategoryPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormFlowEntry")) {
                hasFlowEntryPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormAllInstance")) {
                hasFlowInstancePerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormMyTask")) {
                hasFlowRuntimeTaskPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormMyHistoryTask")) {
                hasFlowHistoricTaskPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdFormMyApprovedTask")) {
                hasFlowFinishTaskPerms = true;
            }

            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdReportDblink")) {
                hasReportDblinkPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdReportDict")) {
                hasReportDictPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdPrint")) {
                hasReportPrintPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdReportDataset")) {
                hasReportDatasetPerms = true;
            }
            if (StrUtil.contains(m.getQuery(), "/#/thirdParty/thirdReportPage")) {
                hasReportPagePerms = true;
            }
            JSONObject data = JSON.parseObject(m.getQuery());
            // 菜单的"路由参数"中，如果包含datasourceVariableName键，则被视为在线表单的业务页面菜单。
            String datasourceVariableName = data.getString("datasourceVariableName");
            // 根据数据源的标识值，构建该业务页面的接口权限数据。
            if (StrUtil.isNotBlank(datasourceVariableName)) {
                onlineOperationPerms.forEach(perm -> urlPerms.add(perm + datasourceVariableName));
            }
            String processDefinitionKey = data.getString("processDefinitionKey");
            if (StrUtil.isNotBlank(processDefinitionKey)) {
                hasFlowWorkOrderOperationPerms = true;
                flowWorkOrderOperationPerms.forEach(perm -> urlPerms.add(perm + processDefinitionKey));
            }
            // 菜单的"路由参数"中，如果包含pageCode键，则被视为报表的业务页面菜单。
            String pageCode = data.getString("pageCode");
            // 根据数据源的标识值，构建该业务页面的接口权限数据。
            if (StrUtil.isNotBlank(pageCode)) {
                reportOperationPerms.forEach(perm -> urlPerms.add(perm + pageCode));
            }
        }
        // 上面循环扫描的结果，可以判断当前用户被分配了哪些橙单集成页面的菜单。
        if (hasOnlineDblinkPerms) {
            urlPerms.addAll(onlineDblinkPerms);
        }
        if (hasOnlinePagePerms) {
            urlPerms.addAll(onlinePagePerms);
        }
        if (hasOnlineDictPerms) {
            urlPerms.addAll(onlineDictPerms);
        }
        if (hasFlowCategoryPerms) {
            urlPerms.addAll(flowCategoryPerms);
        }
        if (hasFlowEntryPerms) {
            urlPerms.addAll(flowEntryPerms);
        }
        if (hasFlowInstancePerms) {
            urlPerms.addAll(flowInstancePerms);
        }
        if (hasFlowRuntimeTaskPerms) {
            urlPerms.addAll(flowRuntimeTaskPerms);
        }
        if (hasFlowHistoricTaskPerms) {
            urlPerms.addAll(flowHistoricTaskPerms);
        }
        if (hasFlowFinishTaskPerms) {
            urlPerms.addAll(flowFinishTaskPerms);
        }
        if (hasFlowWorkOrderOperationPerms) {
            urlPerms.addAll(flowWorkOrderCommonPerms);
            urlPerms.addAll(flowWorkOrderCommonPerms);
        }
        if (hasReportDblinkPerms) {
            urlPerms.addAll(reportDblinkPerms);
        }
        if (hasReportDatasetPerms) {
            urlPerms.addAll(reportDatasetPerms);
        }
        if (hasReportDictPerms) {
            urlPerms.addAll(reportDictPerms);
        }
        if (hasReportPagePerms) {
            urlPerms.addAll(reportPagePerms);
        }
        if (hasReportPrintPerms) {
            urlPerms.addAll(reportPrintPerms);
        }
        urlPerms.addAll(onlineWhitelistPerms);
        urlPerms.addAll(flowWhitelistPerms);
        urlPerms.addAll(reportWhitelistPerms);

        // OrangePermData是后台权限数据的保存格式，即便接入其他第三方框架时，也要保持一致，如确有修改，
        // 需要同时修改橙单中的代码。具体位置为，单体工程的 AuthenticationInterceptor，微服务的AuthenticationPreFilter。
        OrangePermData permData = new OrangePermData();
        permData.setUrlPerms(urlPerms);
        // 下面的查询是获取当前用户的角色列表，同时获取角色中绑定的数据权限。
        // 这里仅仅是若依获取数据权限的方式，其他第三方框架可自行修改。
        List<SysRole> roleList = roleService.selectRolesByUserId(loginUser.getUserId());
        List<SysRole> dataPermRoles = roleList.stream()
                .filter(r -> StrUtil.isNotBlank(r.getDataScope())).collect(Collectors.toList());
        // 如果当前用户没有数据权限配置，就直接返回操作权限数据列表了。
        if (CollUtil.isEmpty(dataPermRoles)) {
            return this.makeResultData(true, null, permData);
        }
        List<OrangeDataPermData> dataPermDataList = new LinkedList<>();
        for (SysRole role : dataPermRoles) {
            OrangeDataPermData dataPermData = new OrangeDataPermData();
            // mapDataPermType将若依中的数据权限过滤策略值，映射为橙单中的策略值。
            dataPermData.setRuleType(this.mapDataPermType(role.getDataScope()));
            // "4" 在若依中表示为本部门及子部门。
            // "2" 在若依中表示自定义部门。
            // 对于 "为本部门及子部门" 和 "自定义部门" 两个策略，需要在这里计算出具体的部门Id列表，
            // 并返回给橙单，橙单中会直接使用。下面是两种不同过滤策略，在若依中获取部门Id列表的逻辑。
            // 不同的第三方框架，或者内部系统，仅需参考此处的逻辑和数据格式即可。
            if (StrUtil.equals(role.getDataScope(), "2")) {
                List<SysRoleDept> roleDepts = roleDeptMapper.selectRoleDeptListByRoleId(role.getRoleId());
                Set<Long> roleDeptIds = roleDepts.stream().map(SysRoleDept::getDeptId).collect(Collectors.toSet());
                dataPermData.setDeptIds(CollUtil.join(roleDeptIds, ","));
            } else if (StrUtil.equals(role.getDataScope(), "4")) {
                List<SysDept> childrenDeptList = sysDeptMapper.selectChildrenDeptById(loginUser.getDeptId());
                Set<Long> deptIds = childrenDeptList.stream().map(SysDept::getDeptId).collect(Collectors.toSet());
                deptIds.add(loginUser.getDeptId());
                dataPermData.setDeptIds(CollUtil.join(deptIds, ","));
            }
            dataPermDataList.add(dataPermData);
        }
        permData.setDataPerms(dataPermDataList);
        // makeResultData返回的对象，是橙单指定的格式，必须保持一致。
        return this.makeResultData(true, null, permData);
    }

    /**
     * 在线表单高级组件中，查询用户和部门数据列表的接口地址，今后可以扩展组件时，添加更多的type即可。
     *
     * @param token 会话token。
     * @param args  查询参数。
     * @return 在若依中的查询结果列表，并返回橙单指定的数据格式。
     */
    @PostMapping("/listBizWidgetData")
    public JSONObject listBizWidgetData(@RequestParam String token, @RequestBody JSONObject args) {
        // 目前仅仅支持upms_user和upms_dept两种类型。这两个值，可以自行定义，
        // 但是需要和橙单中common-ext.apps.types的配置值保持一直即可。推荐使用默认生成的值。
        String type = args.getString("type");
        // 分页对象。
        JSONObject pageParam = args.getJSONObject("pageParam");
        if (pageParam != null) {
            PageMethod.startPage(pageParam.getInteger("pageNum"), pageParam.getInteger("pageSize"));
        }
        // args参数中，还可能存在filter和orderParam两个键，分别代表过滤和排序对象。
        // 当前的示例中没有个给出，可根据实际需求添加。
        // 根据不同的类型，获取不同类型的业务数据列表。给橙单业务组件返回的数据格式，参考userConverter
        // 和deptConverter方法，今后自己增加新的业务组件，可以自行定义数据格式，只要两边保持一致即可。
        if (StrUtil.equals(type, "upms_user")) {
            List<SysUser> userList = userService.selectUserList(new SysUser());
            JSONObject pageData = this.makePageData(userList, this::userConverter);
            return this.makeResultData(true, null, pageData);
        } else if (StrUtil.equals(type, "upms_dept")) {
            List<SysDept> deptList = deptService.selectDeptList(new SysDept());
            JSONObject pageData = this.makePageData(deptList, this::deptConverter);
            return this.makeResultData(true, null, pageData);
        } else if (StrUtil.equals(type, "upms_role")) {
            List<SysRole> roleList = roleService.selectRoleList(new SysRole());
            JSONObject pageData = this.makePageData(roleList, this::roleConverter);
            return this.makeResultData(true, null, pageData);
        } else if (StrUtil.equals(type, "upms_post")) {
            List<SysPost> postList = postService.selectPostList(new SysPost());
            JSONObject pageData = this.makePageData(postList, this::postConverter);
            return this.makeResultData(true, null, pageData);
        } else if (StrUtil.equals(type, "upms_dept_post")) {
            // 这里因为若依没有支持部门和岗位之间的多对多关系，因为我们使用了和upms_post一样的逻辑
            // 作为样例代码，如需要可自行修改并扩展若依，以支持部门和岗位的多对多关系。
            List<SysPost> postList = postService.selectPostList(new SysPost());
            JSONObject pageData = this.makePageData(postList, this::postConverter);
            return this.makeResultData(true, null, pageData);
        }
        // 注意这里一定要返回具体的错误信息。
        return this.makeResultData(false, "尚不支持的组件类型！！！", null);
    }

    /**
     * 在线表单高级组件中，查询用户和部门数据详情的接口地址，今后可以扩展组件时，添加更多的type即可。
     * 为了提高效率，这里可以通过指定多个主键ID，一次性返回多个主键的详情数据。
     *
     * @param token 会话token。
     * @param args  查询参数。
     * @return 在若依中的查询结果详情列表，并返回橙单指定的数据格式。
     */
    @PostMapping("/viewBizWidgetData")
    public JSONObject viewBizWidgetData(@RequestParam String token, @RequestBody JSONObject args) {
        // 这个方法的具体逻辑，和上面的listBizWidgetData基本一致。
        String type = args.getString("type");
        String ids = args.getString("fieldValues");
        if (StrUtil.equals(type, "upms_user")) {
            List<JSONObject> userList = new LinkedList<>();
            List<String> idList = StrUtil.split(ids, ",");
            for (String id : idList) {
                // 这里需要hardcode判断一下，获取用户是基于userId还是loginName。
                String fieldName = args.getString("fieldName");
                SysUser user;
                if (StrUtil.equals(fieldName, "loginName")) {
                    user = userService.selectUserByUserName(id);
                } else {
                    user = userService.selectUserById(Long.valueOf(id));
                }
                if (user != null) {
                    userList.add(this.userConverter(user));
                }
            }
            return this.makeResultData(true, null, userList);
        } else if (StrUtil.equals(type, "upms_dept")) {
            List<JSONObject> deptList = new LinkedList<>();
            List<String> idList = StrUtil.split(ids, ",");
            for (String id : idList) {
                SysDept dept = deptService.selectDeptById(Long.valueOf(id));
                if (dept != null) {
                    deptList.add(this.deptConverter(dept));
                }
            }
            return this.makeResultData(true, null, deptList);
        } else if (StrUtil.equals(type, "upms_role")) {
            List<JSONObject> roleList = new LinkedList<>();
            List<String> idList = StrUtil.split(ids, ",");
            for (String id : idList) {
                SysRole role = roleService.selectRoleById(Long.valueOf(id));
                if (role != null) {
                    roleList.add(this.roleConverter(role));
                }
            }
            return this.makeResultData(true, null, roleList);
        } else if (StrUtil.equals(type, "upms_post")) {
            List<JSONObject> postList = new LinkedList<>();
            List<String> idList = StrUtil.split(ids, ",");
            for (String id : idList) {
                SysPost post = postService.selectPostById(Long.valueOf(id));
                if (post != null) {
                    postList.add(this.postConverter(post));
                }
            }
            return this.makeResultData(true, null, postList);
        } else if (StrUtil.equals(type, "upms_dept_post")) {
            // 这里因为若依没有支持部门和岗位之间的多对多关系，因为我们使用了和upms_post一样的逻辑
            // 作为样例代码，如需要可自行修改并扩展若依，以支持部门和岗位的多对多关系。
            List<JSONObject> postList = new LinkedList<>();
            List<String> idList = StrUtil.split(ids, ",");
            for (String id : idList) {
                SysPost post = postService.selectPostById(Long.valueOf(id));
                if (post != null) {
                    postList.add(this.postConverter(post));
                }
            }
            return this.makeResultData(true, null, postList);
        }
        // 注意这里一定要返回具体的错误信息。
        return this.makeResultData(false, "尚不支持的组件类型！！！", null);
    }

    private JSONObject userConverter(SysUser user) {
        JSONObject result = new JSONObject();
        result.put("userId", user.getUserId());
        result.put("loginName", user.getUserName());
        result.put("showName", user.getNickName());
        result.put("deptId", user.getDeptId());
        if (user.getDept() != null) {
            Map<String, Object> deptIdDictMap = new HashMap<>(2);
            deptIdDictMap.put("id", user.getDept().getDeptId());
            deptIdDictMap.put("name", user.getDept().getDeptName());
            result.put("deptIdDictMap", deptIdDictMap);
        }
        return result;
    }

    private JSONObject deptConverter(SysDept dept) {
        JSONObject result = new JSONObject();
        result.put("deptId", dept.getDeptId());
        result.put("deptName", dept.getDeptName());
        return result;
    }

    private JSONObject roleConverter(SysRole role) {
        JSONObject result = new JSONObject();
        result.put("roleId", role.getRoleId());
        result.put("roleName", role.getRoleName());
        return result;
    }

    private JSONObject postConverter(SysPost post) {
        JSONObject result = new JSONObject();
        result.put("postId", post.getPostId());
        result.put("postName", post.getPostName());
        // 若依没有支持岗位级别，所以我们暂时使用了若依中岗位的显示顺序来代替一下。
        // 如果要完全兼容橙单，需要扩展若依的岗位支持岗位级别。
        result.put("postLevel", post.getPostSort());
        // 若依中没有领导岗位的标记，所以我们缺省使用位置为0的是领导岗位了，也可自行
        // 修改若依代码，以便准确的支持该功能。
        result.put("leaderPost", StrUtil.equals("0", post.getPostSort()));
        return result;
    }

    private <T, R> JSONObject makePageData(List<T> dataList, Function<T, R> converter) {
        long totalCount = 0L;
        if (dataList instanceof Page) {
            totalCount = ((Page<SysUser>) dataList).getTotal();
        }
        List<R> resultList = new LinkedList<>();
        for (T data : dataList) {
            resultList.add(converter.apply(data));
        }
        JSONObject pageData = new JSONObject();
        pageData.put("dataList", resultList);
        pageData.put("totalCount", totalCount);
        return pageData;
    }

    private JSONObject makeResultData(boolean success, String errorMsg, Object data) {
        JSONObject result = new JSONObject();
        result.put("success", success);
        result.put("errorMessage", errorMsg);
        result.put("data", data);
        return result;
    }

    private Integer mapDataPermType(String localDataPerm) {
        // 该变量为橙单中的数据权限过滤值，具体值可参考橙单中的DataPermRuleType常量类。
        int orangeDataPermRuleType = 0;
        // 数据范围（1：所有数据权限；2：自定义数据权限；3：本部门数据权限；4：本部门及以下数据权限；5：仅本人数据权限）
        switch (localDataPerm) {
            case "1":
                orangeDataPermRuleType = 0;
                break;
            case "2":
                orangeDataPermRuleType = 5;
                break;
            case "3":
                orangeDataPermRuleType = 2;
                break;
            case "4":
                orangeDataPermRuleType = 3;
                break;
            case "5":
                orangeDataPermRuleType = 1;
                break;
            default:
                break;
        }
        return orangeDataPermRuleType;
    }

    public static class OrangePermData {
        /**
         * 当前用户会话可访问的url接口地址列表。
         */
        private List<String> urlPerms;
        /**
         * 当前用户会话的数据权限列表。
         */
        private List<OrangeDataPermData> dataPerms;

        public List<String> getUrlPerms() {
            return urlPerms;
        }

        public void setUrlPerms(List<String> urlPerms) {
            this.urlPerms = urlPerms;
        }

        public List<OrangeDataPermData> getDataPerms() {
            return dataPerms;
        }

        public void setDataPerms(List<OrangeDataPermData> dataPerms) {
            this.dataPerms = dataPerms;
        }
    }

    public static class OrangeDataPermData {
        /**
         * 数据权限的规则类型。需要按照橙单的约定返回。
         * 0. 查看全部
         * 1. 仅看当前用户
         * 2. 仅看本部门
         * 3. 本部门及子部门
         * 4. 多部门及子部门
         * 5. 自定义部门
         */
        private Integer ruleType;
        /**
         * 部门Id集合，多个部门Id之间逗号分隔。
         * 注意：仅当ruleType为3、4、5时需要包含该字段值。
         */
        private String deptIds;

        public String getDeptIds() {
            return deptIds;
        }

        public void setDeptIds(String deptIds) {
            this.deptIds = deptIds;
        }

        public Integer getRuleType() {
            return ruleType;
        }

        public void setRuleType(Integer ruleType) {
            this.ruleType = ruleType;
        }
    }
}
