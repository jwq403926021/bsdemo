package com.orangeforms.common.flow.online.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.annotation.MultiDatabaseWriteMethod;
import com.orangeforms.common.core.annotation.MyDataSource;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.object.CallResult;
import com.orangeforms.common.flow.config.FlowProperties;
import com.orangeforms.common.flow.constant.FlowApprovalType;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.constant.FlowTaskStatus;
import com.orangeforms.common.flow.exception.FlowOperationException;
import com.orangeforms.common.flow.model.FlowEntry;
import com.orangeforms.common.flow.model.FlowTaskComment;
import com.orangeforms.common.flow.model.FlowWorkOrder;
import com.orangeforms.common.flow.online.service.FlowOnlineOperationService;
import com.orangeforms.common.flow.service.FlowApiService;
import com.orangeforms.common.flow.service.FlowEntryService;
import com.orangeforms.common.flow.service.FlowWorkOrderService;
import com.orangeforms.common.online.config.OnlineProperties;
import com.orangeforms.common.online.model.OnlineDatasource;
import com.orangeforms.common.online.model.OnlineDatasourceRelation;
import com.orangeforms.common.online.model.OnlineTable;
import com.orangeforms.common.online.service.OnlineDatasourceService;
import com.orangeforms.common.online.service.OnlineOperationService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@MyDataSource(ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowOnlineOperationService")
public class FlowOnlineOperationServiceImpl implements FlowOnlineOperationService {

    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private OnlineOperationService onlineOperationService;
    @Autowired
    private OnlineDatasourceService onlineDatasourceService;
    @Autowired
    private OnlineProperties onlineProperties;
    @Autowired
    private FlowProperties flowProperties;

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndStartProcess(
            String processDefinitionId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable table,
            JSONObject data) {
        this.saveNewAndStartProcess(processDefinitionId, flowTaskComment, taskVariableData, table, data, null);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndStartProcess(
            String processDefinitionId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Object dataId = onlineOperationService.saveNewWithRelation(masterTable, masterData, slaveDataListMap);
        Assert.notNull(dataId);
        if (taskVariableData == null) {
            taskVariableData = new JSONObject();
        }
        taskVariableData.put(FlowConstant.MASTER_DATA_KEY, masterData);
        taskVariableData.put(FlowConstant.SLAVE_DATA_KEY, this.normailizeSlaveDataListMap(slaveDataListMap));
        taskVariableData.put(FlowConstant.MASTER_TABLE_KEY, masterTable);
        ProcessInstance instance = flowApiService.start(processDefinitionId, dataId);
        flowWorkOrderService.saveNew(instance, dataId, masterTable.getTableId(), null);
        flowApiService.takeFirstTask(instance.getProcessInstanceId(), flowTaskComment, taskVariableData);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public FlowWorkOrder saveNewDraftAndStartProcess(
            String processDefinitionId, Long tableId, JSONObject masterData, JSONObject slaveData) {
        ProcessInstance instance = flowApiService.start(processDefinitionId, null);
        return flowWorkOrderService.saveNewWithDraft(
                instance, tableId, null, JSON.toJSONString(masterData), JSON.toJSONString(slaveData));
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndTakeTask(
            String processInstanceId,
            String taskId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable table,
            JSONObject data) {
        this.saveNewAndTakeTask(
                processInstanceId, taskId, flowTaskComment, taskVariableData, table, data, null);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveNewAndTakeTask(
            String processInstanceId,
            String taskId,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineTable masterTable,
            JSONObject masterData,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        Object dataId = onlineOperationService.saveNewWithRelation(masterTable, masterData, slaveDataListMap);
        Assert.notNull(dataId);
        Task task = flowApiService.getProcessInstanceActiveTask(processInstanceId, taskId);
        flowApiService.setBusinessKeyForProcessInstance(processInstanceId, dataId);
        Map<String, Object> variables =
                flowApiService.initAndGetProcessInstanceVariables(task.getProcessDefinitionId());
        if (taskVariableData == null) {
            taskVariableData = new JSONObject();
        }
        taskVariableData.putAll(variables);
        taskVariableData.put(FlowConstant.MASTER_DATA_KEY, masterData);
        taskVariableData.put(FlowConstant.SLAVE_DATA_KEY, this.normailizeSlaveDataListMap(slaveDataListMap));
        taskVariableData.put(FlowConstant.MASTER_TABLE_KEY, masterTable);
        flowApiService.completeTask(task, flowTaskComment, taskVariableData);
        ProcessInstance instance = flowApiService.getProcessInstance(processInstanceId);
        FlowWorkOrder flowWorkOrder =
                flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(instance.getProcessInstanceId());
        if (flowWorkOrder == null) {
            flowWorkOrderService.saveNew(instance, dataId, masterTable.getTableId(), null);
        } else {
            flowWorkOrder.setBusinessKey(dataId.toString());
            flowWorkOrder.setUpdateTime(new Date());
            flowWorkOrder.setFlowStatus(FlowTaskStatus.SUBMITTED);
            flowWorkOrderService.updateById(flowWorkOrder);
        }
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateAndTakeTask(
            Task task,
            FlowTaskComment flowTaskComment,
            JSONObject taskVariableData,
            OnlineDatasource datasource,
            JSONObject masterData,
            String masterDataId,
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        int flowStatus = FlowTaskStatus.APPROVING;
        if (flowTaskComment.getApprovalType().equals(FlowApprovalType.REFUSE)) {
            flowStatus = FlowTaskStatus.REFUSED;
        } else if (flowTaskComment.getApprovalType().equals(FlowApprovalType.STOP)) {
            flowStatus = FlowTaskStatus.FINISHED;
        }
        OnlineTable masterTable = datasource.getMasterTable();
        Long datasourceId = datasource.getDatasourceId();
        flowWorkOrderService.updateFlowStatusByProcessInstanceId(task.getProcessInstanceId(), flowStatus);
        this.updateMasterData(masterTable, masterData, masterDataId);
        if (slaveDataListMap != null) {
            for (Map.Entry<OnlineDatasourceRelation, List<JSONObject>> relationEntry : slaveDataListMap.entrySet()) {
                Long relationId = relationEntry.getKey().getRelationId();
                onlineOperationService.updateRelationData(
                        masterTable, masterData, masterDataId, datasourceId, relationId, relationEntry.getValue());
            }
        }
        if (flowTaskComment.getApprovalType().equals(FlowApprovalType.STOP)) {
            Integer s = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
            flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), s);
            CallResult stopResult = flowApiService.stopProcessInstance(
                    task.getProcessInstanceId(), flowTaskComment.getTaskComment(), flowStatus);
            if (!stopResult.isSuccess()) {
                throw new FlowOperationException(stopResult.getErrorMessage());
            }
        } else {
            if (taskVariableData == null) {
                taskVariableData = new JSONObject();
            }
            taskVariableData.put(FlowConstant.MASTER_DATA_KEY, masterData);
            taskVariableData.put(FlowConstant.SLAVE_DATA_KEY, this.normailizeSlaveDataListMap(slaveDataListMap));
            taskVariableData.put(FlowConstant.MASTER_TABLE_KEY, masterTable);
            flowApiService.completeTask(task, flowTaskComment, taskVariableData);
        }
    }

    @Override
    public List<Map<String, Object>> calculatePermData(Set<Long> onlineFormEntryIds) {
        if (CollUtil.isEmpty(onlineFormEntryIds)) {
            return new LinkedList<>();
        }
        List<Map<String, Object>> permDataList = new LinkedList<>();
        List<FlowEntry> flowEntries = flowEntryService.getInList(onlineFormEntryIds);
        Set<Long> pageIds = flowEntries.stream().map(FlowEntry::getPageId).collect(Collectors.toSet());
        Map<Long, String> pageAndVariableNameMap =
                onlineDatasourceService.getPageIdAndVariableNameMapByPageIds(pageIds);
        for (FlowEntry flowEntry : flowEntries) {
            JSONObject permData = new JSONObject();
            permData.put("entryId", flowEntry.getEntryId());
            String key = StrUtil.upperFirst(flowEntry.getProcessDefinitionKey());
            List<String> permCodeList = new LinkedList<>();
            String formPermCode = "form" + key;
            permCodeList.add(formPermCode);
            permCodeList.add(formPermCode + ":fragment" + key);
            permData.put("permCodeList", permCodeList);
            String flowUrlPrefix = flowProperties.getUrlPrefix();
            String onlineUrlPrefix = onlineProperties.getUrlPrefix();
            List<String> permList = CollUtil.newLinkedList(
                    onlineUrlPrefix + "/onlineForm/view",
                    onlineUrlPrefix + "/onlineForm/render",
                    onlineUrlPrefix + "/onlineOperation/listByOneToManyRelationId/" + pageAndVariableNameMap.get(flowEntry.getPageId()),
                    onlineUrlPrefix + "/onlineOperation/uploadByOneToManyRelationId/" + pageAndVariableNameMap.get(flowEntry.getPageId()),
                    onlineUrlPrefix + "/onlineOperation/dowloadByOneToManyRelationId/" + pageAndVariableNameMap.get(flowEntry.getPageId()),
                    flowUrlPrefix + "/flowOperation/viewInitialHistoricTaskInfo",
                    flowUrlPrefix + "/flowOperation/startOnly",
                    flowUrlPrefix + "/flowOperation/viewInitialTaskInfo",
                    flowUrlPrefix + "/flowOperation/viewRuntimeTaskInfo",
                    flowUrlPrefix + "/flowOperation/viewProcessBpmn",
                    flowUrlPrefix + "/flowOperation/viewHighlightFlowData",
                    flowUrlPrefix + "/flowOperation/listFlowTaskComment",
                    flowUrlPrefix + "/flowOperation/cancelWorkOrder",
                    flowUrlPrefix + "/flowOperation/listRuntimeTask",
                    flowUrlPrefix + "/flowOperation/listHistoricProcessInstance",
                    flowUrlPrefix + "/flowOperation/listHistoricTask",
                    flowUrlPrefix + "/flowOperation/freeJumpTo",
                    flowUrlPrefix + "/flowOnlineOperation/startPreview",
                    flowUrlPrefix + "/flowOnlineOperation/viewUserTask",
                    flowUrlPrefix + "/flowOnlineOperation/viewHistoricProcessInstance",
                    flowUrlPrefix + "/flowOnlineOperation/submitUserTask",
                    flowUrlPrefix + "/flowOnlineOperation/upload",
                    flowUrlPrefix + "/flowOnlineOperation/download",
                    flowUrlPrefix + "/flowOperation/submitConsign",
                    flowUrlPrefix + "/flowOnlineOperation/startAndTakeUserTask/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/startAndSaveDraft/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/listWorkOrder/" + flowEntry.getProcessDefinitionKey(),
                    flowUrlPrefix + "/flowOnlineOperation/printWorkOrder/" + flowEntry.getProcessDefinitionKey()
            );
            permData.put("permList", permList);
            permDataList.add(permData);
        }
        return permDataList;
    }

    private void updateMasterData(OnlineTable masterTable, JSONObject masterData, String dataId) {
        if (masterData == null) {
            return;
        }
        // 如果存在主表数据，就执行主表数据的更新。
        Map<String, Object> originalMasterData =
                onlineOperationService.getMasterData(masterTable, null, null, dataId);
        for (Map.Entry<String, Object> entry : originalMasterData.entrySet()) {
            masterData.putIfAbsent(entry.getKey(), entry.getValue());
        }
        if (!onlineOperationService.update(masterTable, masterData)) {
            throw new FlowOperationException("主表数据不存在！");
        }
    }

    private Map<String, List<JSONObject>> normailizeSlaveDataListMap(
            Map<OnlineDatasourceRelation, List<JSONObject>> slaveDataListMap) {
        if (slaveDataListMap == null || slaveDataListMap.isEmpty()) {
            return null;
        }
        Map<String, List<JSONObject>> resultMap = new HashMap<>(slaveDataListMap.size());
        for (Map.Entry<OnlineDatasourceRelation, List<JSONObject>> entry : slaveDataListMap.entrySet()) {
            resultMap.put(entry.getKey().getSlaveTable().getTableName(), entry.getValue());
        }
        return resultMap;
    }
}
