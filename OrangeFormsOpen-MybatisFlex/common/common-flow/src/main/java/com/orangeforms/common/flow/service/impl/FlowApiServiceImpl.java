package com.orangeforms.common.flow.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.*;
import cn.hutool.core.convert.Convert;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.annotation.MultiDatabaseWriteMethod;
import com.orangeforms.common.core.annotation.MyDataSourceResolver;
import com.orangeforms.common.core.constant.ApplicationConstant;
import com.orangeforms.common.core.constant.UserFilterGroup;
import com.orangeforms.common.core.exception.MyRuntimeException;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.MyDateUtil;
import com.orangeforms.common.core.util.MyCommonUtil;
import com.orangeforms.common.core.util.DefaultDataSourceResolver;
import com.orangeforms.common.flow.cmd.AddSequenceMultiInstanceCmd;
import com.orangeforms.common.flow.exception.FlowOperationException;
import com.orangeforms.common.flow.object.*;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.constant.FlowApprovalType;
import com.orangeforms.common.flow.constant.FlowTaskStatus;
import com.orangeforms.common.flow.model.*;
import com.orangeforms.common.flow.service.*;
import com.orangeforms.common.flow.util.BaseFlowIdentityExtHelper;
import com.orangeforms.common.flow.util.CustomChangeActivityStateBuilderImpl;
import com.orangeforms.common.flow.util.FlowCustomExtFactory;
import com.orangeforms.common.flow.vo.FlowTaskVo;
import com.orangeforms.common.flow.vo.FlowUserInfoVo;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.de.odysseus.el.ExpressionFactoryImpl;
import org.flowable.common.engine.impl.de.odysseus.el.util.SimpleContext;
import org.flowable.common.engine.impl.identity.Authentication;
import org.flowable.common.engine.impl.javax.el.ExpressionFactory;
import org.flowable.common.engine.impl.javax.el.ValueExpression;
import org.flowable.engine.*;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.history.*;
import org.flowable.engine.impl.RuntimeServiceImpl;
import org.flowable.engine.impl.bpmn.behavior.ParallelMultiInstanceBehavior;
import org.flowable.engine.impl.bpmn.behavior.SequentialMultiInstanceBehavior;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ChangeActivityStateBuilder;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceBuilder;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@MyDataSourceResolver(
        resolver = DefaultDataSourceResolver.class,
        intArg = ApplicationConstant.COMMON_FLOW_AND_ONLINE_DATASOURCE_TYPE)
@Service("flowApiService")
public class FlowApiServiceImpl implements FlowApiService {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private ManagementService managementService;
    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private FlowTaskCommentService flowTaskCommentService;
    @Autowired
    private FlowTaskExtService flowTaskExtService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private FlowMessageService flowMessageService;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private FlowMultiInstanceTransService flowMultiInstanceTransService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProcessInstance start(String processDefinitionId, Object dataId) {
        TokenData tokenData = TokenData.takeFromRequest();
        Map<String, Object> variableMap = this.initAndGetProcessInstanceVariables(processDefinitionId);
        Authentication.setAuthenticatedUserId(tokenData.getLoginName());
        String businessKey = dataId == null ? null : dataId.toString();
        ProcessInstanceBuilder builder = runtimeService.createProcessInstanceBuilder()
                .processDefinitionId(processDefinitionId).businessKey(businessKey).variables(variableMap);
        if (tokenData.getTenantId() != null) {
            builder.tenantId(tokenData.getTenantId().toString());
        } else {
            if (tokenData.getAppCode() != null) {
                builder.tenantId(tokenData.getAppCode());
            }
        }
        return builder.start();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Task takeFirstTask(String processInstanceId, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        // 获取流程启动后的第一个任务。
        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        if (StrUtil.equalsAny(task.getAssignee(), loginName, FlowConstant.START_USER_NAME_VAR)) {
            // 按照规则，调用该方法的用户，就是第一个任务的assignee，因此默认会自动执行complete。
            flowTaskComment.fillWith(task);
            this.completeTask(task, flowTaskComment, taskVariableData);
        }
        return task;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ProcessInstance startAndTakeFirst(
            String processDefinitionId, Object dataId, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        ProcessInstance instance = this.start(processDefinitionId, dataId);
        this.takeFirstTask(instance.getProcessInstanceId(), flowTaskComment, taskVariableData);
        return instance;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitConsign(
            HistoricTaskInstance startTaskInstance, Task multiInstanceActiveTask, String newAssignees, boolean isAdd) {
        JSONArray assigneeArray = JSON.parseArray(newAssignees);
        String multiInstanceExecId = this.getExecutionVariableStringWithSafe(
                multiInstanceActiveTask.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
        FlowMultiInstanceTrans trans =
                flowMultiInstanceTransService.getWithAssigneeListByMultiInstanceExecId(multiInstanceExecId);
        Set<String> assigneeSet = new HashSet<>(StrUtil.split(trans.getAssigneeList(), ","));
        Task runtimeTask = null;
        for (int i = 0; i < assigneeArray.size(); i++) {
            String assignee = assigneeArray.getString(i);
            if (isAdd) {
                assigneeSet.add(assignee);
            } else {
                assigneeSet.remove(assignee);
            }
            if (isAdd) {
                Map<String, Object> variables = new HashMap<>(2);
                variables.put("assignee", assigneeArray.getString(i));
                variables.put(FlowConstant.MULTI_SIGN_START_TASK_VAR, startTaskInstance.getId());
                runtimeService.addMultiInstanceExecution(
                        multiInstanceActiveTask.getTaskDefinitionKey(), multiInstanceActiveTask.getProcessInstanceId(), variables);
            } else {
                TaskQuery query = taskService.createTaskQuery().active();
                query.processInstanceId(multiInstanceActiveTask.getProcessInstanceId());
                query.taskDefinitionKey(multiInstanceActiveTask.getTaskDefinitionKey());
                query.taskAssignee(assignee);
                runtimeTask = query.singleResult();
                if (runtimeTask == null) {
                    throw new FlowOperationException("审批人 [" + assignee + "] 已经提交审批，不能执行减签操作！");
                }
                runtimeService.deleteMultiInstanceExecution(runtimeTask.getExecutionId(), false);
            }
        }
        if (!isAdd && runtimeTask != null) {
            this.doChangeTask(runtimeTask);
        }
        trans.setAssigneeList(StrUtil.join(",", assigneeSet));
        flowMultiInstanceTransService.updateById(trans);
        FlowTaskComment flowTaskComment = new FlowTaskComment();
        flowTaskComment.fillWith(startTaskInstance);
        flowTaskComment.setApprovalType(isAdd ? FlowApprovalType.MULTI_CONSIGN : FlowApprovalType.MULTI_MINUS_SIGN);
        String showName = TokenData.takeFromRequest().getLoginName();
        String comment = String.format("用户 [%s] [%s] [%s]。", isAdd ? "加签" : "减签", showName, newAssignees);
        flowTaskComment.setTaskComment(comment);
        flowTaskCommentService.saveNew(flowTaskComment);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void submitSequenceConsign(Task multiInstanceActiveTask, String newAssignees, boolean before) {
        JSONArray assigneeArray = JSON.parseArray(newAssignees);
        List<String> newAssigneeList = new LinkedList<>();
        for (int i = 0; i < assigneeArray.size(); i++) {
            newAssigneeList.add(assigneeArray.getString(i));
        }
        String multiInstanceExecId = this.getExecutionVariableStringWithSafe(
                multiInstanceActiveTask.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
        FlowMultiInstanceTrans trans =
                flowMultiInstanceTransService.getWithAssigneeListByMultiInstanceExecId(multiInstanceExecId);
        List<String> updatedAssignees = managementService.executeCommand(
                new AddSequenceMultiInstanceCmd(trans.getAssigneeList(), multiInstanceActiveTask.getId(), newAssigneeList, before));
        trans.setAssigneeList(StrUtil.join(",", updatedAssignees));
        flowMultiInstanceTransService.updateById(trans);
        FlowTaskComment flowTaskComment = new FlowTaskComment();
        flowTaskComment.fillWith(multiInstanceActiveTask);
        flowTaskComment.setApprovalType(before ? FlowApprovalType.MULTI_BEFORE_CONSIGN : FlowApprovalType.MULTI_AFTER_CONSIGN);
        String showName = TokenData.takeFromRequest().getLoginName();
        String comment = String.format("用户 [%s] [%s] [%s]。", before ? "前加签" : "后加签", showName, newAssignees);
        flowTaskComment.setTaskComment(comment);
        flowTaskCommentService.saveNew(flowTaskComment);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void completeTask(Task task, FlowTaskComment flowTaskComment, JSONObject taskVariableData) {
        if (taskVariableData == null) {
            taskVariableData = new JSONObject();
        }
        JSONObject passCopyData = (JSONObject) taskVariableData.remove(FlowConstant.COPY_DATA_KEY);
        // 判断当前完成执行的任务，是否存在抄送设置。
        Object copyData = runtimeService.getVariable(
                task.getProcessInstanceId(), FlowConstant.COPY_DATA_MAP_PREFIX + task.getTaskDefinitionKey());
        if (copyData != null || passCopyData != null) {
            JSONObject copyDataJson = this.mergeCopyData(copyData, passCopyData);
            flowMessageService.saveNewCopyMessage(task, copyDataJson);
        }
        if (flowTaskComment != null) {
            // 这里处理多实例会签逻辑。
            if (flowTaskComment.getApprovalType().equals(FlowApprovalType.MULTI_SIGN)) {
                String loginName = TokenData.takeFromRequest().getLoginName();
                String assigneeList = this.getMultiInstanceAssigneeList(task, taskVariableData);
                Assert.isTrue(StrUtil.isNotBlank(assigneeList));
                taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, 0);
                taskVariableData.put(FlowConstant.MULTI_SIGN_START_TASK_VAR, task.getId());
                String multiInstanceExecId = MyCommonUtil.generateUuid();
                taskVariableData.put(FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR, multiInstanceExecId);
                String comment = String.format("用户 [%s] 会签 [%s]。", loginName, assigneeList);
                FlowMultiInstanceTrans multiInstanceTrans = new FlowMultiInstanceTrans(task);
                multiInstanceTrans.setMultiInstanceExecId(multiInstanceExecId);
                multiInstanceTrans.setAssigneeList(assigneeList);
                flowMultiInstanceTransService.saveNew(multiInstanceTrans);
                flowTaskComment.setTaskComment(comment);
            }
            // 处理转办。
            if (FlowApprovalType.TRANSFER.equals(flowTaskComment.getApprovalType())) {
                this.transferTo(task, flowTaskComment);
                return;
            }
            this.handleMultiInstanceApprovalType(
                    task.getExecutionId(), flowTaskComment.getApprovalType(), taskVariableData);
            taskVariableData.put(FlowConstant.OPERATION_TYPE_VAR, flowTaskComment.getApprovalType());
            this.setSubmitUserVar(taskVariableData, flowTaskComment);
            flowTaskComment.fillWith(task);
            if (this.isMultiInstanceTask(task.getProcessDefinitionId(), task.getTaskDefinitionKey())) {
                String multiInstanceExecId = getExecutionVariableStringWithSafe(
                        task.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
                FlowMultiInstanceTrans multiInstanceTrans = new FlowMultiInstanceTrans(task);
                multiInstanceTrans.setMultiInstanceExecId(multiInstanceExecId);
                flowMultiInstanceTransService.saveNew(multiInstanceTrans);
                flowTaskComment.setMultiInstanceExecId(multiInstanceExecId);
            }
            flowTaskCommentService.saveNew(flowTaskComment);
        }
        taskVariableData.remove(FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR);
        Integer approvalStatus = MapUtil.getInt(taskVariableData, FlowConstant.LATEST_APPROVAL_STATUS_KEY);
        flowWorkOrderService.updateLatestApprovalStatusByProcessInstanceId(task.getProcessInstanceId(), approvalStatus);
        taskService.complete(task.getId(), taskVariableData, this.makeTransientVariableMap(taskVariableData));
        flowMessageService.updateFinishedStatusByTaskId(task.getId());
    }

    private void setSubmitUserVar(JSONObject taskVariableData, FlowTaskComment comment) {
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData != null) {
            taskVariableData.put(FlowConstant.SUBMIT_USER_VAR, tokenData.getLoginName());
        } else {
            if (StrUtil.isNotBlank(comment.getCreateLoginName())) {
                taskVariableData.put(FlowConstant.SUBMIT_USER_VAR, comment.getCreateLoginName());
            }
        }
    }

    private JSONObject makeTransientVariableMap(JSONObject taskVariableData) {
        JSONObject result = new JSONObject();
        if (taskVariableData == null) {
            return result;
        }
        Object masterData = taskVariableData.get(FlowConstant.MASTER_DATA_KEY);
        if (masterData != null) {
            result.put(FlowConstant.MASTER_DATA_KEY, masterData);
        }
        Object slaveData = taskVariableData.get(FlowConstant.SLAVE_DATA_KEY);
        if (slaveData != null) {
            result.put(FlowConstant.SLAVE_DATA_KEY, slaveData);
        }
        Object masterTable = taskVariableData.get(FlowConstant.MASTER_TABLE_KEY);
        if (masterTable != null) {
            result.put(FlowConstant.MASTER_TABLE_KEY, masterTable);
        }
        taskVariableData.remove(FlowConstant.MASTER_DATA_KEY);
        taskVariableData.remove(FlowConstant.SLAVE_DATA_KEY);
        taskVariableData.remove(FlowConstant.MASTER_TABLE_KEY);
        return result;
    }

    private String getMultiInstanceAssigneeList(Task task, JSONObject taskVariableData) {
        JSONArray assigneeArray = taskVariableData.getJSONArray(FlowConstant.MULTI_ASSIGNEE_LIST_VAR);
        String assigneeList;
        if (CollUtil.isEmpty(assigneeArray)) {
            FlowTaskExt flowTaskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(
                    task.getProcessDefinitionId(), task.getTaskDefinitionKey());
            assigneeList = this.buildMutiSignAssigneeList(flowTaskExt.getOperationListJson());
            if (assigneeList != null) {
                taskVariableData.put(FlowConstant.MULTI_ASSIGNEE_LIST_VAR, StrUtil.split(assigneeList, ','));
            }
        } else {
            assigneeList = CollUtil.join(assigneeArray, ",");
        }
        return assigneeList;
    }

    private JSONObject mergeCopyData(Object copyData, JSONObject passCopyData) {
        // passCopyData是传阅数据，copyData是抄送数据。
        JSONObject resultCopyDataJson = passCopyData;
        if (resultCopyDataJson == null) {
            resultCopyDataJson = JSON.parseObject(copyData.toString());
        } else if (copyData != null) {
            JSONObject copyDataJson = JSON.parseObject(copyData.toString());
            for (Map.Entry<String, Object> entry : copyDataJson.entrySet()) {
                String value = resultCopyDataJson.getString(entry.getKey());
                if (value == null) {
                    resultCopyDataJson.put(entry.getKey(), entry.getValue());
                } else {
                    List<String> list1 = StrUtil.split(value, ",");
                    List<String> list2 = StrUtil.split(entry.getValue().toString(), ",");
                    Set<String> valueSet = new HashSet<>(list1);
                    valueSet.addAll(list2);
                    resultCopyDataJson.put(entry.getKey(), StrUtil.join(",", valueSet));
                }
            }
        }
        this.processMergeCopyData(resultCopyDataJson);
        return resultCopyDataJson;
    }

    private void processMergeCopyData(JSONObject resultCopyDataJson) {
        TokenData tokenData = TokenData.takeFromRequest();
        BaseFlowIdentityExtHelper flowIdentityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        for (Map.Entry<String, Object> entry : resultCopyDataJson.entrySet()) {
            String type = entry.getKey();
            switch (type) {
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR:
                    Object upLeaderDeptPostId =
                            flowIdentityExtHelper.getUpLeaderDeptPostId(tokenData.getDeptId());
                    entry.setValue(upLeaderDeptPostId);
                    break;
                case FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR:
                    Object leaderDeptPostId =
                            flowIdentityExtHelper.getLeaderDeptPostId(tokenData.getDeptId());
                    entry.setValue(leaderDeptPostId);
                    break;
                case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                    Set<String> selfPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> deptPostIdMap =
                            flowIdentityExtHelper.getDeptPostIdMap(tokenData.getDeptId(), selfPostIdSet);
                    String deptPostIdValues = "";
                    if (deptPostIdMap != null) {
                        deptPostIdValues = StrUtil.join(",", deptPostIdMap.values());
                    }
                    entry.setValue(deptPostIdValues);
                    break;
                case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                    Set<String> siblingPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> siblingDeptPostIdMap =
                            flowIdentityExtHelper.getSiblingDeptPostIdMap(tokenData.getDeptId(), siblingPostIdSet);
                    String siblingDeptPostIdValues = "";
                    if (siblingDeptPostIdMap != null) {
                        siblingDeptPostIdValues = StrUtil.join(",", siblingDeptPostIdMap.values());
                    }
                    entry.setValue(siblingDeptPostIdValues);
                    break;
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                    Set<String> upPostIdSet = new HashSet<>(StrUtil.split(entry.getValue().toString(), ","));
                    Map<String, String> upDeptPostIdMap =
                            flowIdentityExtHelper.getUpDeptPostIdMap(tokenData.getDeptId(), upPostIdSet);
                    String upDeptPostIdValues = "";
                    if (upDeptPostIdMap != null) {
                        upDeptPostIdValues = StrUtil.join(",", upDeptPostIdMap.values());
                    }
                    entry.setValue(upDeptPostIdValues);
                    break;
                default:
                    break;
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult verifyAssigneeOrCandidateAndClaim(Task task) {
        String errorMessage;
        String loginName = TokenData.takeFromRequest().getLoginName();
        // 这里必须先执行拾取操作，如果当前用户是候选人，特别是对于分布式场景，更是要先完成候选人的拾取。
        if (task.getAssignee() == null) {
            // 没有指派人
            if (!this.isAssigneeOrCandidate(task)) {
                errorMessage = "数据验证失败，当前用户不是该待办任务的候选人，请刷新后重试！";
                return CallResult.error(errorMessage);
            }
            // 作为候选人主动拾取任务。
            taskService.claim(task.getId(), loginName);
        } else {
            if (!task.getAssignee().equals(loginName)) {
                errorMessage = "数据验证失败，当前用户不是该待办任务的指派人，请刷新后重试！";
                return CallResult.error(errorMessage);
            }
        }
        return CallResult.ok();
    }

    @Override
    public Map<String, Object> initAndGetProcessInstanceVariables(String processDefinitionId) {
        TokenData tokenData = TokenData.takeFromRequest();
        String loginName = tokenData.getLoginName();
        // 设置流程变量。
        Map<String, Object> variableMap = new HashMap<>(4);
        variableMap.put(FlowConstant.PROC_INSTANCE_INITIATOR_VAR, loginName);
        variableMap.put(FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR, loginName);
        List<FlowTaskExt> flowTaskExtList = flowTaskExtService.getByProcessDefinitionId(processDefinitionId);
        boolean hasDeptPostLeader = false;
        boolean hasUpDeptPostLeader = false;
        boolean hasPostCandidateGroup = false;
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
                hasUpDeptPostLeader = true;
            } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
                hasDeptPostLeader = true;
            } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_POST)) {
                hasPostCandidateGroup = true;
            }
        }
        // 如果流程图的配置中包含用户身份相关的变量(如：部门领导和上级领导审批)，flowIdentityExtHelper就不能为null。
        // 这个需要子类去实现 BaseFlowIdentityExtHelper 接口，并注册到FlowCustomExtFactory的工厂中。
        BaseFlowIdentityExtHelper flowIdentityExtHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        if (hasUpDeptPostLeader) {
            Assert.notNull(flowIdentityExtHelper);
            Object upLeaderDeptPostId = flowIdentityExtHelper.getUpLeaderDeptPostId(tokenData.getDeptId());
            if (upLeaderDeptPostId == null) {
                variableMap.put(FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, null);
            } else {
                variableMap.put(FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, upLeaderDeptPostId.toString());
            }
        }
        if (hasDeptPostLeader) {
            Assert.notNull(flowIdentityExtHelper);
            Object leaderDeptPostId = flowIdentityExtHelper.getLeaderDeptPostId(tokenData.getDeptId());
            if (leaderDeptPostId == null) {
                variableMap.put(FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, null);
            } else {
                variableMap.put(FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, leaderDeptPostId.toString());
            }
        }
        if (hasPostCandidateGroup) {
            Assert.notNull(flowIdentityExtHelper);
            Map<String, Object> postGroupDataMap =
                    this.buildPostCandidateGroupData(flowIdentityExtHelper, flowTaskExtList);
            variableMap.putAll(postGroupDataMap);
        }
        this.buildCopyData(flowTaskExtList, variableMap);
        return variableMap;
    }

    private void buildCopyData(List<FlowTaskExt> flowTaskExtList, Map<String, Object> variableMap) {
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (StrUtil.isBlank(flowTaskExt.getCopyListJson())) {
                continue;
            }
            List<JSONObject> copyDataList = JSON.parseArray(flowTaskExt.getCopyListJson(), JSONObject.class);
            Map<String, Object> copyDataMap = new HashMap<>(copyDataList.size());
            for (JSONObject copyData : copyDataList) {
                String type = copyData.getString("type");
                String id = copyData.getString("id");
                copyDataMap.put(type, id == null ? "" : id);
            }
            variableMap.put(FlowConstant.COPY_DATA_MAP_PREFIX + flowTaskExt.getTaskId(), JSON.toJSONString(copyDataMap));
        }
    }

    private Map<String, Object> buildPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, List<FlowTaskExt> flowTaskExtList) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        Set<String> selfPostIdSet = new HashSet<>();
        Set<String> siblingPostIdSet = new HashSet<>();
        Set<String> upPostIdSet = new HashSet<>();
        for (FlowTaskExt flowTaskExt : flowTaskExtList) {
            if (flowTaskExt.getGroupType().equals(FlowConstant.GROUP_TYPE_POST)) {
                Assert.notNull(flowTaskExt.getDeptPostListJson());
                List<FlowTaskPostCandidateGroup> groupDataList =
                        JSONArray.parseArray(flowTaskExt.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
                for (FlowTaskPostCandidateGroup groupData : groupDataList) {
                    switch (groupData.getType()) {
                        case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR -> selfPostIdSet.add(groupData.getPostId());
                        case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR -> siblingPostIdSet.add(groupData.getPostId());
                        case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR -> upPostIdSet.add(groupData.getPostId());
                        default -> {
                        }
                    }
                }
            }
        }
        postVariableMap.putAll(this.buildSelfPostCandidateGroupData(flowIdentityExtHelper, selfPostIdSet));
        postVariableMap.putAll(this.buildSiblingPostCandidateGroupData(flowIdentityExtHelper, siblingPostIdSet));
        postVariableMap.putAll(this.buildUpPostCandidateGroupData(flowIdentityExtHelper, upPostIdSet));
        return postVariableMap;
    }

    private Map<String, Object> buildSelfPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> selfPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(selfPostIdSet)) {
            Map<String, String> deptPostIdMap =
                    flowIdentityExtHelper.getDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), selfPostIdSet);
            for (String postId : selfPostIdSet) {
                if (MapUtil.isNotEmpty(deptPostIdMap) && deptPostIdMap.containsKey(postId)) {
                    String deptPostId = deptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.SELF_DEPT_POST_PREFIX + postId, deptPostId);
                } else {
                    postVariableMap.put(FlowConstant.SELF_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    private Map<String, Object> buildSiblingPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> siblingPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(siblingPostIdSet)) {
            Map<String, String> siblingDeptPostIdMap =
                    flowIdentityExtHelper.getSiblingDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), siblingPostIdSet);
            for (String postId : siblingPostIdSet) {
                if (MapUtil.isNotEmpty(siblingDeptPostIdMap) && siblingDeptPostIdMap.containsKey(postId)) {
                    String siblingDeptPostId = siblingDeptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.SIBLING_DEPT_POST_PREFIX + postId, siblingDeptPostId);
                } else {
                    postVariableMap.put(FlowConstant.SIBLING_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    private Map<String, Object> buildUpPostCandidateGroupData(
            BaseFlowIdentityExtHelper flowIdentityExtHelper, Set<String> upPostIdSet) {
        Map<String, Object> postVariableMap = MapUtil.newHashMap();
        if (CollUtil.isNotEmpty(upPostIdSet)) {
            Map<String, String> upDeptPostIdMap =
                    flowIdentityExtHelper.getUpDeptPostIdMap(TokenData.takeFromRequest().getDeptId(), upPostIdSet);
            for (String postId : upPostIdSet) {
                if (MapUtil.isNotEmpty(upDeptPostIdMap) && upDeptPostIdMap.containsKey(postId)) {
                    String upDeptPostId = upDeptPostIdMap.get(postId);
                    postVariableMap.put(FlowConstant.UP_DEPT_POST_PREFIX + postId, upDeptPostId);
                } else {
                    postVariableMap.put(FlowConstant.UP_DEPT_POST_PREFIX + postId, "");
                }
            }
        }
        return postVariableMap;
    }

    @Override
    public boolean isAssigneeOrCandidate(TaskInfo task) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        if (StrUtil.isNotBlank(task.getAssignee())) {
            return StrUtil.equals(loginName, task.getAssignee());
        }
        TaskQuery query = taskService.createTaskQuery();
        this.buildCandidateCondition(query, loginName);
        query.taskId(task.getId());
        return query.active().count() != 0;
    }

    @Override
    public Collection<FlowElement> getProcessAllElements(String processDefinitionId) {
        Process process = repositoryService.getBpmnModel(processDefinitionId).getProcesses().get(0);
        return this.getAllElements(process.getFlowElements(), null);
    }

    @Override
    public boolean isProcessInstanceStarter(String processInstanceId) {
        String loginName = TokenData.takeFromRequest().getLoginName();
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId).startedBy(loginName).count() != 0;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void setBusinessKeyForProcessInstance(String processInstanceId, Object dataId) {
        runtimeService.updateBusinessKey(processInstanceId, dataId.toString());
    }

    @Override
    public boolean existActiveProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId).active().count() != 0;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public ProcessInstance getProcessInstanceByBusinessKey(String processDefinitionId, String businessKey) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionId(processDefinitionId).processInstanceBusinessKey(businessKey).singleResult();
    }

    @Override
    public Task getProcessInstanceActiveTask(String processInstanceId, String taskId) {
        TaskQuery query = taskService.createTaskQuery().processInstanceId(processInstanceId);
        if (StrUtil.isNotBlank(taskId)) {
            query.taskId(taskId);
        }
        return query.active().singleResult();
    }

    @Override
    public List<Task> getProcessInstanceActiveTaskList(String processInstanceId) {
        return taskService.createTaskQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public List<FlowTaskVo> getProcessInstanceActiveTaskListAndConvert(String processInstanceId) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        return this.convertToFlowTaskList(taskList);
    }

    @Override
    public Task getTaskById(String taskId) {
        return taskService.createTaskQuery().taskId(taskId).singleResult();
    }

    @Override
    public MyPageData<Task> getTaskListByUserName(
            String username, String definitionKey, String definitionName, String taskName, MyPageParam pageParam) {
        TaskQuery query = this.createQuery();
        if (StrUtil.isNotBlank(definitionKey)) {
            query.processDefinitionKey(definitionKey);
        }
        if (StrUtil.isNotBlank(definitionName)) {
            query.processDefinitionNameLike("%" + definitionName + "%");
        }
        if (StrUtil.isNotBlank(taskName)) {
            query.taskNameLike("%" + taskName + "%");
        }
        this.buildCandidateCondition(query, username);
        long totalCount = query.count();
        query.orderByTaskCreateTime().desc();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<Task> taskList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(taskList, totalCount);
    }

    @Override
    public long getTaskCountByUserName(String username) {
        TaskQuery query = this.createQuery();
        this.buildCandidateCondition(query, username);
        return query.count();
    }

    @Override
    public List<Task> getTaskListByProcessInstanceIds(List<String> processInstanceIdSet) {
        return taskService.createTaskQuery().processInstanceIdIn(processInstanceIdSet).active().list();
    }

    @Override
    public List<ProcessInstance> getProcessInstanceList(Set<String> processInstanceIdSet) {
        return runtimeService.createProcessInstanceQuery().processInstanceIds(processInstanceIdSet).list();
    }

    @Override
    public ProcessDefinition getProcessDefinitionById(String processDefinitionId) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefinitionId).singleResult();
    }

    @Override
    public List<ProcessDefinition> getProcessDefinitionList(Set<String> processDefinitionIdSet) {
        return repositoryService.createProcessDefinitionQuery().processDefinitionIds(processDefinitionIdSet).list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void suspendProcessDefinition(String processDefinitionId) {
        repositoryService.suspendProcessDefinitionById(processDefinitionId);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void activateProcessDefinition(String processDefinitionId) {
        repositoryService.activateProcessDefinitionById(processDefinitionId);
    }

    @Override
    public BpmnModel getBpmnModelByDefinitionId(String processDefinitionId) {
        return repositoryService.getBpmnModel(processDefinitionId);
    }

    @Override
    public boolean isMultiInstanceTask(String processDefinitionId, String taskKey) {
        BpmnModel model = this.getBpmnModelByDefinitionId(processDefinitionId);
        FlowElement flowElement = model.getFlowElement(taskKey);
        if (!(flowElement instanceof UserTask userTask)) {
            return false;
        }
        return userTask.hasMultiInstanceLoopCharacteristics();
    }

    @Override
    public ProcessDefinition getProcessDefinitionByDeployId(String deployId) {
        return repositoryService.createProcessDefinitionQuery().deploymentId(deployId).singleResult();
    }

    @Override
    public void setProcessInstanceVariables(String processInstanceId, Map<String, Object> variableMap) {
        runtimeService.setVariables(processInstanceId, variableMap);
    }

    @Override
    public Object getProcessInstanceVariable(String processInstanceId, String variableName) {
        return runtimeService.getVariable(processInstanceId, variableName);
    }

    @Override
    public List<FlowTaskVo> convertToFlowTaskList(List<Task> taskList) {
        List<FlowTaskVo> flowTaskVoList = new LinkedList<>();
        if (CollUtil.isEmpty(taskList)) {
            return flowTaskVoList;
        }
        Set<String> processDefinitionIdSet = taskList.stream()
                .map(Task::getProcessDefinitionId).collect(Collectors.toSet());
        Set<String> procInstanceIdSet = taskList.stream()
                .map(Task::getProcessInstanceId).collect(Collectors.toSet());
        List<FlowEntryPublish> flowEntryPublishList =
                flowEntryService.getFlowEntryPublishList(processDefinitionIdSet);
        Map<String, FlowEntryPublish> flowEntryPublishMap =
                flowEntryPublishList.stream().collect(Collectors.toMap(FlowEntryPublish::getProcessDefinitionId, c -> c));
        List<ProcessInstance> instanceList = this.getProcessInstanceList(procInstanceIdSet);
        Map<String, ProcessInstance> instanceMap =
                instanceList.stream().collect(Collectors.toMap(ProcessInstance::getId, c -> c));
        List<ProcessDefinition> definitionList = this.getProcessDefinitionList(processDefinitionIdSet);
        Map<String, ProcessDefinition> definitionMap =
                definitionList.stream().collect(Collectors.toMap(ProcessDefinition::getId, c -> c));
        List<FlowWorkOrder> workOrderList =
                flowWorkOrderService.getInList("processInstanceId", procInstanceIdSet);
        Map<String, FlowWorkOrder> workOrderMap =
                workOrderList.stream().collect(Collectors.toMap(FlowWorkOrder::getProcessInstanceId, c -> c));
        for (Task task : taskList) {
            FlowTaskVo flowTaskVo = new FlowTaskVo();
            flowTaskVo.setTaskId(task.getId());
            flowTaskVo.setTaskName(task.getName());
            flowTaskVo.setTaskKey(task.getTaskDefinitionKey());
            flowTaskVo.setTaskFormKey(task.getFormKey());
            flowTaskVo.setTaskStartTime(task.getCreateTime());
            flowTaskVo.setEntryId(flowEntryPublishMap.get(task.getProcessDefinitionId()).getEntryId());
            ProcessDefinition processDefinition = definitionMap.get(task.getProcessDefinitionId());
            flowTaskVo.setProcessDefinitionId(processDefinition.getId());
            flowTaskVo.setProcessDefinitionName(processDefinition.getName());
            flowTaskVo.setProcessDefinitionKey(processDefinition.getKey());
            flowTaskVo.setProcessDefinitionVersion(processDefinition.getVersion());
            ProcessInstance processInstance = instanceMap.get(task.getProcessInstanceId());
            flowTaskVo.setProcessInstanceId(processInstance.getId());
            Object initiator = this.getProcessInstanceVariable(
                    processInstance.getId(), FlowConstant.PROC_INSTANCE_INITIATOR_VAR);
            flowTaskVo.setProcessInstanceInitiator(initiator.toString());
            flowTaskVo.setProcessInstanceStartTime(processInstance.getStartTime());
            flowTaskVo.setBusinessKey(processInstance.getBusinessKey());
            FlowWorkOrder flowWorkOrder = workOrderMap.get(task.getProcessInstanceId());
            if (flowWorkOrder != null) {
                flowTaskVo.setIsDraft(flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.DRAFT));
                flowTaskVo.setWorkOrderCode(flowWorkOrder.getWorkOrderCode());
            }
            flowTaskVoList.add(flowTaskVo);
        }
        Set<String> loginNameSet = flowTaskVoList.stream()
                .map(FlowTaskVo::getProcessInstanceInitiator).collect(Collectors.toSet());
        List<FlowUserInfoVo> flowUserInfos = flowCustomExtFactory
                .getFlowIdentityExtHelper().getUserInfoListByUsernameSet(loginNameSet);
        Map<String, FlowUserInfoVo> userInfoMap =
                flowUserInfos.stream().collect(Collectors.toMap(FlowUserInfoVo::getLoginName, c -> c));
        for (FlowTaskVo flowTaskVo : flowTaskVoList) {
            FlowUserInfoVo userInfo = userInfoMap.get(flowTaskVo.getProcessInstanceInitiator());
            flowTaskVo.setShowName(userInfo.getShowName());
            flowTaskVo.setHeadImageUrl(userInfo.getHeadImageUrl());
        }
        return flowTaskVoList;
    }

    @Override
    public void addProcessInstanceEndListener(BpmnModel bpmnModel, Class<? extends ExecutionListener> listenerClazz) {
        Assert.notNull(listenerClazz);
        Process process = bpmnModel.getMainProcess();
        FlowableListener listener = this.createListener("end", listenerClazz.getName());
        process.getExecutionListeners().add(listener);
    }

    @Override
    public void addExecutionListener(
            FlowElement flowElement,
            Class<? extends ExecutionListener> listenerClazz,
            String event,
            List<FieldExtension> fieldExtensions) {
        Assert.notNull(listenerClazz);
        FlowableListener listener = this.createListener(event, listenerClazz.getName());
        if (fieldExtensions != null) {
            listener.setFieldExtensions(fieldExtensions);
        }
        flowElement.getExecutionListeners().add(listener);
    }

    @Override
    public void addTaskCreateListener(UserTask userTask, Class<? extends TaskListener> listenerClazz) {
        Assert.notNull(listenerClazz);
        FlowableListener listener = this.createListener("create", listenerClazz.getName());
        userTask.getTaskListeners().add(listener);
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
    }

    @Override
    public List<HistoricProcessInstance> getHistoricProcessInstanceList(Set<String> processInstanceIdSet) {
        return historyService.createHistoricProcessInstanceQuery().processInstanceIds(processInstanceIdSet).list();
    }

    @Override
    public MyPageData<HistoricProcessInstance> getHistoricProcessInstanceList(
            String processDefinitionKey,
            String processDefinitionName,
            String startUser,
            String beginDate,
            String endDate,
            MyPageParam pageParam,
            boolean finishedOnly) throws ParseException {
        HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery();
        if (StrUtil.isNotBlank(processDefinitionKey)) {
            query.processDefinitionKey(processDefinitionKey);
        }
        if (StrUtil.isNotBlank(processDefinitionName)) {
            query.processDefinitionName(processDefinitionName);
        }
        if (StrUtil.isNotBlank(startUser)) {
            query.startedBy(startUser);
        }
        if (StrUtil.isNotBlank(beginDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.startedAfter(sdf.parse(beginDate));
        }
        if (StrUtil.isNotBlank(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.startedBefore(sdf.parse(endDate));
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.processInstanceTenantId(tokenData.getTenantId().toString());
        } else {
            if (tokenData.getAppCode() == null) {
                query.processInstanceWithoutTenantId();
            } else {
                query.processInstanceTenantId(tokenData.getAppCode());
            }
        }
        if (finishedOnly) {
            query.finished();
        }
        query.orderByProcessInstanceStartTime().desc();
        long totalCount = query.count();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<HistoricProcessInstance> instanceList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(instanceList, totalCount);
    }

    @Override
    public MyPageData<HistoricTaskInstance> getHistoricTaskInstanceFinishedList(
            String processDefinitionName,
            String beginDate,
            String endDate,
            MyPageParam pageParam) throws ParseException {
        String loginName = TokenData.takeFromRequest().getLoginName();
        HistoricTaskInstanceQuery query = historyService.createHistoricTaskInstanceQuery()
                .taskAssignee(loginName)
                .finished();
        if (StrUtil.isNotBlank(processDefinitionName)) {
            query.processDefinitionName(processDefinitionName);
        }
        if (StrUtil.isNotBlank(beginDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.taskCompletedAfter(sdf.parse(beginDate));
        }
        if (StrUtil.isNotBlank(endDate)) {
            SimpleDateFormat sdf = new SimpleDateFormat(MyDateUtil.COMMON_SHORT_DATETIME_FORMAT);
            query.taskCompletedBefore(sdf.parse(endDate));
        }
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.taskTenantId(tokenData.getTenantId().toString());
        } else {
            if (StrUtil.isBlank(tokenData.getAppCode())) {
                query.taskWithoutTenantId();
            } else {
                query.taskTenantId(tokenData.getAppCode());
            }
        }
        query.orderByHistoricTaskInstanceEndTime().desc();
        long totalCount = query.count();
        int firstResult = (pageParam.getPageNum() - 1) * pageParam.getPageSize();
        List<HistoricTaskInstance> instanceList = query.listPage(firstResult, pageParam.getPageSize());
        return new MyPageData<>(instanceList, totalCount);
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityInstanceList(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).list();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityInstanceListOrderByStartTime(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();
    }

    @Override
    public HistoricTaskInstance getHistoricTaskInstance(String processInstanceId, String taskId) {
        return historyService.createHistoricTaskInstanceQuery()
                .processInstanceId(processInstanceId).taskId(taskId).singleResult();
    }

    @Override
    public List<HistoricActivityInstance> getHistoricUnfinishedInstanceList(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId).unfinished().list();
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult stopProcessInstance(String processInstanceId, String stopReason, boolean forCancel) {
        //需要先更新状态，以便FlowFinishedListener监听器可以正常的判断流程结束的状态。
        int status = FlowTaskStatus.STOPPED;
        if (forCancel) {
            status = FlowTaskStatus.CANCELLED;
        }
        return this.stopProcessInstance(processInstanceId, stopReason, status);
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public CallResult stopProcessInstance(String processInstanceId, String stopReason, int status) {
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processInstanceId).active().list();
        if (CollUtil.isEmpty(taskList)) {
            return CallResult.error("数据验证失败，当前流程尚未开始或已经结束！");
        }
        BpmnModel bpmnModel = repositoryService.getBpmnModel(taskList.get(0).getProcessDefinitionId());
        EndEvent endEvent = bpmnModel.getMainProcess()
                .findFlowElementsOfType(EndEvent.class, false).get(0);
        List<String> currentActivitiIds = new LinkedList<>();
        flowWorkOrderService.updateFlowStatusByProcessInstanceId(processInstanceId, status);
        for (Task task : taskList) {
            String currActivityId = task.getTaskDefinitionKey();
            currentActivitiIds.add(currActivityId);
            FlowNode currFlow = (FlowNode) bpmnModel.getMainProcess().getFlowElement(currActivityId);
            if (currFlow == null) {
                List<SubProcess> subProcessList =
                        bpmnModel.getMainProcess().findFlowElementsOfType(SubProcess.class);
                for (SubProcess subProcess : subProcessList) {
                    FlowElement flowElement = subProcess.getFlowElement(currActivityId);
                    if (flowElement != null) {
                        currFlow = (FlowNode) flowElement;
                        break;
                    }
                }
            }
            org.springframework.util.Assert.notNull(currFlow, "currFlow can't be NULL");
            if (!(currFlow.getParentContainer().equals(endEvent.getParentContainer()))) {
                throw new FlowOperationException("数据验证失败，不能从子流程直接中止！");
            }
            FlowTaskComment taskComment = new FlowTaskComment(task);
            taskComment.setApprovalType(FlowApprovalType.STOP);
            taskComment.setTaskComment(stopReason);
            flowTaskCommentService.saveNew(taskComment);
        }
        this.doChangeState(processInstanceId, currentActivitiIds, CollUtil.newArrayList(endEvent.getId()));
        flowMessageService.updateFinishedStatusByProcessInstanceId(processInstanceId);
        return CallResult.ok();
    }

    @MultiDatabaseWriteMethod
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProcessInstance(String processInstanceId) {
        historyService.deleteHistoricProcessInstance(processInstanceId);
        flowMessageService.removeByProcessInstanceId(processInstanceId);
        FlowWorkOrder workOrder = flowWorkOrderService.getFlowWorkOrderByProcessInstanceId(processInstanceId);
        if (workOrder == null) {
            return;
        }
        FlowEntry flowEntry = flowEntryService.getFlowEntryFromCache(workOrder.getProcessDefinitionKey());
        if (StrUtil.isNotBlank(flowEntry.getExtensionData())) {
            FlowEntryExtensionData extData = JSON.parseObject(flowEntry.getExtensionData(), FlowEntryExtensionData.class);
            if (BooleanUtil.isTrue(extData.getCascadeDeleteBusinessData())) {
                // 级联删除在线表单工作流的业务数据。
                flowCustomExtFactory.getOnlineBusinessDataExtHelper().deleteBusinessData(workOrder);
            }
        }
        flowWorkOrderService.removeByProcessInstanceId(processInstanceId);
    }

    @Override
    public Object getTaskVariable(String taskId, String variableName) {
        return taskService.getVariable(taskId, variableName);
    }

    @Override
    public String getTaskVariableStringWithSafe(String taskId, String variableName) {
        try {
            Object v = taskService.getVariable(taskId, variableName);
            if (v == null) {
                return null;
            }
            return v.toString();
        } catch (Exception e) {
            String errorMessage =
                    String.format("Failed to getTaskVariable taskId [%s], variableName [%s]", taskId, variableName);
            log.error(errorMessage, e);
            return null;
        }
    }

    @Override
    public Object getExecutionVariable(String executionId, String variableName) {
        return runtimeService.getVariable(executionId, variableName);
    }

    @Override
    public String getExecutionVariableStringWithSafe(String executionId, String variableName) {
        try {
            Object v = runtimeService.getVariable(executionId, variableName);
            if (v == null) {
                return null;
            }
            return v.toString();
        } catch (Exception e) {
            String errorMessage = String.format(
                    "Failed to getExecutionVariableStringWithSafe executionId [%s], variableName [%s]", executionId, variableName);
            log.error(errorMessage, e);
            return null;
        }
    }

    @Override
    public Object getHistoricProcessInstanceVariable(String processInstanceId, String variableName) {
        HistoricVariableInstance hv = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId).variableName(variableName).singleResult();
        return hv == null ? null : hv.getValue();
    }

    @Override
    public BpmnModel convertToBpmnModel(String bpmnXml) throws XMLStreamException {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        InputStream in = new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8));
        @Cleanup XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(in);
        return converter.convertToBpmnModel(reader);
    }

    @Transactional
    @Override
    public CallResult backToRuntimeTask(Task task, String targetKey, boolean forReject, String reason) {
        String errorMessage;
        ProcessDefinition processDefinition = this.getProcessDefinitionById(task.getProcessDefinitionId());
        Collection<FlowElement> allElements = this.getProcessAllElements(processDefinition.getId());
        FlowElement source = null;
        // 获取跳转的节点元素
        FlowElement target = null;
        for (FlowElement flowElement : allElements) {
            if (flowElement.getId().equals(task.getTaskDefinitionKey())) {
                source = flowElement;
                if (StrUtil.isBlank(targetKey)) {
                    break;
                }
            }
            if (StrUtil.isNotBlank(targetKey)) {
                if (flowElement.getId().equals(targetKey)) {
                    target = flowElement;
                }
            }
        }
        if (targetKey != null && target == null) {
            errorMessage = "数据验证失败，被驳回的指定目标节点不存在！";
            return CallResult.error(errorMessage);
        }
        UserTask oneUserTask = null;
        List<String> targetIds = null;
        if (target == null) {
            List<UserTask> parentUserTaskList = this.getParentUserTaskList(source, null, null);
            if (CollUtil.isEmpty(parentUserTaskList)) {
                errorMessage = "数据验证失败，当前节点为初始任务节点，不能驳回！";
                return CallResult.error(errorMessage);
            }
            // 获取活动ID, 即节点Key
            Set<String> parentUserTaskKeySet = new HashSet<>();
            parentUserTaskList.forEach(item -> parentUserTaskKeySet.add(item.getId()));
            List<HistoricActivityInstance> historicActivityIdList =
                    this.getHistoricActivityInstanceListOrderByStartTime(task.getProcessInstanceId());
            // 数据清洗，将回滚导致的脏数据清洗掉
            List<String> lastHistoricTaskInstanceList =
                    this.cleanHistoricTaskInstance(allElements, historicActivityIdList);
            // 此时历史任务实例为倒序，获取最后走的节点
            targetIds = new ArrayList<>();
            // 循环结束标识，遇到当前目标节点的次数
            int number = 0;
            StringBuilder parentHistoricTaskKey = new StringBuilder();
            for (String historicTaskInstanceKey : lastHistoricTaskInstanceList) {
                // 当会签时候会出现特殊的，连续都是同一个节点历史数据的情况，这种时候跳过
                if (parentHistoricTaskKey.toString().equals(historicTaskInstanceKey)) {
                    continue;
                }
                parentHistoricTaskKey = new StringBuilder(historicTaskInstanceKey);
                if (historicTaskInstanceKey.equals(task.getTaskDefinitionKey())) {
                    number++;
                }
                if (number == 2) {
                    break;
                }
                // 如果当前历史节点，属于父级的节点，说明最后一次经过了这个点，需要退回这个点
                if (parentUserTaskKeySet.contains(historicTaskInstanceKey)) {
                    targetIds.add(historicTaskInstanceKey);
                }
            }
            // 目的获取所有需要被跳转的节点 currentIds
            // 取其中一个父级任务，因为后续要么存在公共网关，要么就是串行公共线路
            oneUserTask = parentUserTaskList.get(0);
        }
        // 获取所有正常进行的执行任务的活动节点ID，这些任务不能直接使用，需要找出其中需要撤回的任务
        List<Execution> runExecutionList =
                runtimeService.createExecutionQuery().processInstanceId(task.getProcessInstanceId()).list();
        List<String> runActivityIdList = runExecutionList.stream()
                .map(Execution::getActivityId)
                .filter(StrUtil::isNotBlank).collect(Collectors.toList());
        // 需驳回任务列表
        List<String> currentIds = new ArrayList<>();
        // 通过父级网关的出口连线，结合 runExecutionList 比对，获取需要撤回的任务
        List<FlowElement> currentFlowElementList = this.getChildUserTaskList(
                target != null ? target : oneUserTask, runActivityIdList, null, null);
        currentFlowElementList.forEach(item -> currentIds.add(item.getId()));
        if (target == null) {
            // 规定：并行网关之前节点必须需存在唯一用户任务节点，如果出现多个任务节点，则并行网关节点默认为结束节点，原因为不考虑多对多情况
            if (targetIds.size() > 1 && currentIds.size() > 1) {
                errorMessage = "数据验证失败，任务出现多对多情况，无法撤回！";
                return CallResult.error(errorMessage);
            }
        }
        AtomicReference<List<HistoricActivityInstance>> tmp = new AtomicReference<>();
        // 用于下面新增网关删除信息时使用
        String targetTmp = targetKey != null ? targetKey : String.join(",", targetIds);
        // currentIds 为活动ID列表
        // currentExecutionIds 为执行任务ID列表
        // 需要通过执行任务ID来设置驳回信息，活动ID不行
        currentIds.forEach(currentId -> runExecutionList.forEach(runExecution -> {
            if (StrUtil.isNotBlank(runExecution.getActivityId()) && currentId.equals(runExecution.getActivityId())) {
                // 查询当前节点的执行任务的历史数据
                tmp.set(historyService.createHistoricActivityInstanceQuery()
                        .processInstanceId(task.getProcessInstanceId())
                        .executionId(runExecution.getId())
                        .activityId(runExecution.getActivityId()).list());
                // 如果这个列表的数据只有 1 条数据
                // 网关肯定只有一条，且为包容网关或并行网关
                // 这里的操作目的是为了给网关在扭转前提前加上删除信息，结构与普通节点的删除信息一样，目的是为了知道这个网关也是有经过跳转的
                if (tmp.get() != null && tmp.get().size() == 1 && StrUtil.isNotBlank(tmp.get().get(0).getActivityType())
                        && ("parallelGateway".equals(tmp.get().get(0).getActivityType()) || "inclusiveGateway".equals(tmp.get().get(0).getActivityType()))) {
                    // singleResult 能够执行更新操作
                    // 利用 流程实例ID + 执行任务ID + 活动节点ID 来指定唯一数据，保证数据正确
                    historyService.createNativeHistoricActivityInstanceQuery().sql(
                            "UPDATE ACT_HI_ACTINST SET DELETE_REASON_ = 'Change activity to " + targetTmp + "'  WHERE PROC_INST_ID_='" + task.getProcessInstanceId() + "' AND EXECUTION_ID_='" + runExecution.getId() + "' AND ACT_ID_='" + runExecution.getActivityId() + "'").singleResult();
                }
            }
        }));
        try {
            if (StrUtil.isNotBlank(targetKey)) {
                runtimeService.createChangeActivityStateBuilder()
                        .processInstanceId(task.getProcessInstanceId())
                        .moveActivityIdsToSingleActivityId(currentIds, targetKey).changeState();
            } else {
                // 如果父级任务多于 1 个，说明当前节点不是并行节点，原因为不考虑多对多情况
                if (targetIds.size() > 1) {
                    // 1 对 多任务跳转，currentIds 当前节点(1)，targetIds 跳转到的节点(多)
                    ChangeActivityStateBuilder builder = runtimeService.createChangeActivityStateBuilder()
                            .processInstanceId(task.getProcessInstanceId())
                            .moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds);
                    for (String targetId : targetIds) {
                        FlowTaskComment taskComment =
                                flowTaskCommentService.getLatestFlowTaskComment(task.getProcessInstanceId(), targetId);
                        // 如果驳回后的目标任务包含指定人，则直接通过变量回抄，如果没有则自动忽略该变量，不会给流程带来任何影响。
                        String submitLoginName = taskComment.getCreateLoginName();
                        if (StrUtil.isNotBlank(submitLoginName)) {
                            builder.localVariable(targetId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR, submitLoginName);
                        }
                    }
                    builder.changeState();
                }
                // 如果父级任务只有一个，因此当前任务可能为网关中的任务
                if (targetIds.size() == 1) {
                    // 1 对 1 或 多 对 1 情况，currentIds 当前要跳转的节点列表(1或多)，targetIds.get(0) 跳转到的节点(1)
                    // 如果驳回后的目标任务包含指定人，则直接通过变量回抄，如果没有则自动忽略该变量，不会给流程带来任何影响。
                    ChangeActivityStateBuilder builder = runtimeService.createChangeActivityStateBuilder()
                            .processInstanceId(task.getProcessInstanceId())
                            .moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0));
                    FlowTaskComment taskComment =
                            flowTaskCommentService.getLatestFlowTaskComment(task.getProcessInstanceId(), targetIds.get(0));
                    String submitLoginName = taskComment.getCreateLoginName();
                    if (StrUtil.isNotBlank(submitLoginName)) {
                        builder.localVariable(targetIds.get(0), FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR, submitLoginName);
                    }
                    builder.changeState();
                }
            }
            FlowTaskComment comment = new FlowTaskComment();
            comment.setTaskId(task.getId());
            comment.setTaskKey(task.getTaskDefinitionKey());
            comment.setTaskName(task.getName());
            comment.setApprovalType(forReject ? FlowApprovalType.REJECT : FlowApprovalType.REVOKE);
            comment.setProcessInstanceId(task.getProcessInstanceId());
            comment.setTaskComment(reason);
            flowTaskCommentService.saveNew(comment);
        } catch (Exception e) {
            log.error("Failed to execute moveSingleActivityIdToActivityIds", e);
            return CallResult.error(e.getMessage());
        }
        return CallResult.ok();
    }

    private List<UserTask> getParentUserTaskList(
            FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            userTaskList = getParentUserTaskList(source.getSubProcess(), hasSequenceFlow, userTaskList);
        }
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 类型为用户节点，则新增父级节点
                if (sequenceFlow.getSourceFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getSourceFlowElement());
                    continue;
                }
                // 类型为子流程，则添加子流程开始节点出口处相连的节点
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    // 获取子流程用户任务节点
                    List<UserTask> childUserTaskList = findChildProcessUserTasks(
                            (StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && !childUserTaskList.isEmpty()) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 网关场景的继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                userTaskList = getParentUserTaskList(
                        sequenceFlow.getSourceFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
            }
        }
        return userTaskList;
    }

    private List<FlowElement> getChildUserTaskList(
            FlowElement source, List<String> runActiveIdList, Set<String> hasSequenceFlow, List<FlowElement> flowElementList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        flowElementList = flowElementList == null ? new ArrayList<>() : flowElementList;
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof EndEvent && source.getSubProcess() != null) {
            flowElementList = getChildUserTaskList(
                    source.getSubProcess(), runActiveIdList, hasSequenceFlow, flowElementList);
        }
        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，或者为网关
                // 活动节点ID 在运行的任务中存在，添加
                FlowElement targetElement = sequenceFlow.getTargetFlowElement();
                if ((targetElement instanceof UserTask || targetElement instanceof Gateway)
                        && runActiveIdList.contains(targetElement.getId())) {
                    flowElementList.add(sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<FlowElement> childUserTaskList = getChildUserTaskList(
                            (FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), runActiveIdList, hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && !childUserTaskList.isEmpty()) {
                        flowElementList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                flowElementList = getChildUserTaskList(
                        sequenceFlow.getTargetFlowElement(), runActiveIdList, new HashSet<>(hasSequenceFlow), flowElementList);
            }
        }
        return flowElementList;
    }

    private List<String> cleanHistoricTaskInstance(
            Collection<FlowElement> allElements, List<HistoricActivityInstance> historicActivityList) {
        // 会签节点收集
        List<String> multiTask = new ArrayList<>();
        allElements.forEach(flowElement -> {
            if (flowElement instanceof UserTask) {
                // 如果该节点的行为为会签行为，说明该节点为会签节点
                if (((UserTask) flowElement).getBehavior() instanceof ParallelMultiInstanceBehavior
                        || ((UserTask) flowElement).getBehavior() instanceof SequentialMultiInstanceBehavior) {
                    multiTask.add(flowElement.getId());
                }
            }
        });
        // 循环放入栈，栈 LIFO：后进先出
        Stack<HistoricActivityInstance> stack = new Stack<>();
        historicActivityList.forEach(stack::push);
        // 清洗后的历史任务实例
        List<String> lastHistoricTaskInstanceList = new ArrayList<>();
        // 网关存在可能只走了部分分支情况，且还存在跳转废弃数据以及其他分支数据的干扰，因此需要对历史节点数据进行清洗
        // 临时用户任务 key
        StringBuilder userTaskKey = null;
        // 临时被删掉的任务 key，存在并行情况
        List<String> deleteKeyList = new ArrayList<>();
        // 临时脏数据线路
        List<Set<String>> dirtyDataLineList = new ArrayList<>();
        // 由某个点跳到会签点,此时出现多个会签实例对应 1 个跳转情况，需要把这些连续脏数据都找到
        // 会签特殊处理下标
        int multiIndex = -1;
        // 会签特殊处理 key
        StringBuilder multiKey = null;
        // 会签特殊处理操作标识
        boolean multiOpera = false;
        while (!stack.empty()) {
            // 从这里开始 userTaskKey 都还是上个栈的 key
            // 是否是脏数据线路上的点
            final boolean[] isDirtyData = {false};
            for (Set<String> oldDirtyDataLine : dirtyDataLineList) {
                if (oldDirtyDataLine.contains(stack.peek().getActivityId())) {
                    isDirtyData[0] = true;
                }
            }
            // 删除原因不为空，说明从这条数据开始回跳或者回退的
            // MI_END：会签完成后，其他未签到节点的删除原因，不在处理范围内
            if (stack.peek().getDeleteReason() != null && !"MI_END".equals(stack.peek().getDeleteReason())) {
                // 可以理解为脏线路起点
                String dirtyPoint = "";
                if (stack.peek().getDeleteReason().contains("Change activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change activity to ", "");
                }
                // 会签回退删除原因有点不同
                if (stack.peek().getDeleteReason().contains("Change parent activity to ")) {
                    dirtyPoint = stack.peek().getDeleteReason().replace("Change parent activity to ", "");
                }
                FlowElement dirtyTask = null;
                // 获取变更节点的对应的入口处连线
                // 如果是网关并行回退情况，会变成两条脏数据路线，效果一样
                for (FlowElement flowElement : allElements) {
                    if (flowElement.getId().equals(stack.peek().getActivityId())) {
                        dirtyTask = flowElement;
                    }
                }
                // 获取脏数据线路
                Set<String> dirtyDataLine =
                        findDirtyRoads(dirtyTask, null, null, StrUtil.split(dirtyPoint, ','), null);
                // 自己本身也是脏线路上的点，加进去
                dirtyDataLine.add(stack.peek().getActivityId());
                log.info(stack.peek().getActivityId() + "点脏路线集合：" + dirtyDataLine);
                // 是全新的需要添加的脏线路
                boolean isNewDirtyData = true;
                for (Set<String> strings : dirtyDataLineList) {
                    // 如果发现他的上个节点在脏线路内，说明这个点可能是并行的节点，或者连续驳回
                    // 这时，都以之前的脏线路节点为标准，只需合并脏线路即可，也就是路线补全
                    if (strings.contains(userTaskKey.toString())) {
                        isNewDirtyData = false;
                        strings.addAll(dirtyDataLine);
                    }
                }
                // 已确定时全新的脏线路
                if (isNewDirtyData) {
                    // deleteKey 单一路线驳回到并行，这种同时生成多个新实例记录情况，这时 deleteKey 其实是由多个值组成
                    // 按照逻辑，回退后立刻生成的实例记录就是回退的记录
                    // 至于驳回所生成的 Key，直接从删除原因中获取，因为存在驳回到并行的情况
                    deleteKeyList.add(dirtyPoint + ",");
                    dirtyDataLineList.add(dirtyDataLine);
                }
                // 添加后，现在这个点变成脏线路上的点了
                isDirtyData[0] = true;
            }
            // 如果不是脏线路上的点，说明是有效数据，添加历史实例 Key
            if (!isDirtyData[0]) {
                lastHistoricTaskInstanceList.add(stack.peek().getActivityId());
            }
            // 校验脏线路是否结束
            for (int i = 0; i < deleteKeyList.size(); i ++) {
                // 如果发现脏数据属于会签，记录下下标与对应 Key，以备后续比对，会签脏数据范畴开始
                if (multiKey == null && multiTask.contains(stack.peek().getActivityId())
                        && deleteKeyList.get(i).contains(stack.peek().getActivityId())) {
                    multiIndex = i;
                    multiKey = new StringBuilder(stack.peek().getActivityId());
                }
                // 会签脏数据处理，节点退回会签清空
                // 如果在会签脏数据范畴中发现 Key改变，说明会签脏数据在上个节点就结束了，可以把会签脏数据删掉
                if (multiKey != null && !multiKey.toString().equals(stack.peek().getActivityId())) {
                    deleteKeyList.set(multiIndex , deleteKeyList.get(multiIndex).replace(stack.peek().getActivityId() + ",", ""));
                    multiKey = null;
                    // 结束进行下校验删除
                    multiOpera = true;
                }
                // 其他脏数据处理
                // 发现该路线最后一条脏数据，说明这条脏数据线路处理完了，删除脏数据信息
                // 脏数据产生的新实例中是否包含这条数据
                if (multiKey == null && deleteKeyList.get(i).contains(stack.peek().getActivityId())) {
                    // 删除匹配到的部分
                    deleteKeyList.set(i , deleteKeyList.get(i).replace(stack.peek().getActivityId() + ",", ""));
                }
                // 如果每组中的元素都以匹配过，说明脏数据结束
                if ("".equals(deleteKeyList.get(i))) {
                    // 同时删除脏数据
                    deleteKeyList.remove(i);
                    dirtyDataLineList.remove(i);
                    break;
                }
            }
            // 会签数据处理需要在循环外处理，否则可能导致溢出
            // 会签的数据肯定是之前放进去的所以理论上不会溢出，但还是校验下
            if (multiOpera && deleteKeyList.size() > multiIndex && "".equals(deleteKeyList.get(multiIndex))) {
                // 同时删除脏数据
                deleteKeyList.remove(multiIndex);
                dirtyDataLineList.remove(multiIndex);
                multiIndex = -1;
                multiOpera = false;
            }
            // pop() 方法与 peek() 方法不同，在返回值的同时，会把值从栈中移除
            // 保存新的 userTaskKey 在下个循环中使用
            userTaskKey = new StringBuilder(stack.pop().getActivityId());
        }
        log.info("清洗后的历史节点数据：" + lastHistoricTaskInstanceList);
        return lastHistoricTaskInstanceList;
    }

    private List<UserTask> findChildProcessUserTasks(FlowElement source, Set<String> hasSequenceFlow, List<UserTask> userTaskList) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        userTaskList = userTaskList == null ? new ArrayList<>() : userTaskList;
        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow : sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果为用户任务类型，且任务节点的 Key 正在运行的任务中存在，添加
                if (sequenceFlow.getTargetFlowElement() instanceof UserTask) {
                    userTaskList.add((UserTask) sequenceFlow.getTargetFlowElement());
                    continue;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    List<UserTask> childUserTaskList = findChildProcessUserTasks((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, null);
                    // 如果找到节点，则说明该线路找到节点，不继续向下找，反之继续
                    if (childUserTaskList != null && !childUserTaskList.isEmpty()) {
                        userTaskList.addAll(childUserTaskList);
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                userTaskList = findChildProcessUserTasks(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), userTaskList);
            }
        }
        return userTaskList;
    }

    private Set<String> findDirtyRoads(
            FlowElement source, List<String> passRoads, Set<String> hasSequenceFlow, List<String> targets, Set<String> dirtyRoads) {
        passRoads = passRoads == null ? new ArrayList<>() : passRoads;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
        if (source instanceof StartEvent && source.getSubProcess() != null) {
            dirtyRoads = findDirtyRoads(source.getSubProcess(), passRoads, hasSequenceFlow, targets, dirtyRoads);
        }
        // 根据类型，获取入口连线
        List<SequenceFlow> sequenceFlows = getElementIncomingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 新增经过的路线
                passRoads.add(sequenceFlow.getSourceFlowElement().getId());
                // 如果此点为目标点，确定经过的路线为脏线路，添加点到脏线路中，然后找下个连线
                if (targets.contains(sequenceFlow.getSourceFlowElement().getId())) {
                    dirtyRoads.addAll(passRoads);
                    continue;
                }
                // 如果该节点为开始节点，且存在上级子节点，则顺着上级子节点继续迭代
                if (sequenceFlow.getSourceFlowElement() instanceof SubProcess) {
                    dirtyRoads = findChildProcessAllDirtyRoad(
                            (StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, dirtyRoads);
                    // 是否存在子流程上，true 是，false 否
                    Boolean isInChildProcess = dirtyTargetInChildProcess(
                            (StartEvent) ((SubProcess) sequenceFlow.getSourceFlowElement()).getFlowElements().toArray()[0], null, targets, null);
                    if (isInChildProcess) {
                        // 已在子流程上找到，该路线结束
                        continue;
                    }
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                dirtyRoads = findDirtyRoads(sequenceFlow.getSourceFlowElement(),
                        new ArrayList<>(passRoads), new HashSet<>(hasSequenceFlow), targets, dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    private Set<String> findChildProcessAllDirtyRoad(
            FlowElement source, Set<String> hasSequenceFlow, Set<String> dirtyRoads) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        dirtyRoads = dirtyRoads == null ? new HashSet<>() : dirtyRoads;
        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows != null) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 添加脏路线
                dirtyRoads.add(sequenceFlow.getTargetFlowElement().getId());
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    dirtyRoads = findChildProcessAllDirtyRoad(
                            (FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, dirtyRoads);
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                dirtyRoads = findChildProcessAllDirtyRoad(
                        sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), dirtyRoads);
            }
        }
        return dirtyRoads;
    }

    private Boolean dirtyTargetInChildProcess(
            FlowElement source, Set<String> hasSequenceFlow, List<String> targets, Boolean inChildProcess) {
        hasSequenceFlow = hasSequenceFlow == null ? new HashSet<>() : hasSequenceFlow;
        inChildProcess = inChildProcess != null && inChildProcess;
        // 根据类型，获取出口连线
        List<SequenceFlow> sequenceFlows = getElementOutgoingFlows(source);
        if (sequenceFlows != null && !inChildProcess) {
            // 循环找到目标元素
            for (SequenceFlow sequenceFlow: sequenceFlows) {
                // 如果发现连线重复，说明循环了，跳过这个循环
                if (hasSequenceFlow.contains(sequenceFlow.getId())) {
                    continue;
                }
                // 添加已经走过的连线
                hasSequenceFlow.add(sequenceFlow.getId());
                // 如果发现目标点在子流程上存在，说明只到子流程为止
                if (targets.contains(sequenceFlow.getTargetFlowElement().getId())) {
                    inChildProcess = true;
                    break;
                }
                // 如果节点为子流程节点情况，则从节点中的第一个节点开始获取
                if (sequenceFlow.getTargetFlowElement() instanceof SubProcess) {
                    inChildProcess = dirtyTargetInChildProcess((FlowElement) (((SubProcess) sequenceFlow.getTargetFlowElement()).getFlowElements().toArray()[0]), hasSequenceFlow, targets, inChildProcess);
                }
                // 继续迭代
                // 注意：已经经过的节点与连线都应该用浅拷贝出来的对象
                // 比如分支：a->b->c与a->d->c，走完a->b->c后走另一个路线是，已经经过的节点应该不包含a->b->c路线的数据
                inChildProcess = dirtyTargetInChildProcess(sequenceFlow.getTargetFlowElement(), new HashSet<>(hasSequenceFlow), targets, inChildProcess);
            }
        }
        return inChildProcess;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void transferTo(Task task, FlowTaskComment flowTaskComment) {
        List<String> transferUserList = StrUtil.split(flowTaskComment.getDelegateAssignee(), ",");
        for (String transferUser : transferUserList) {
            if (transferUser.equals(FlowConstant.START_USER_NAME_VAR)) {
                String startUser = this.getProcessInstanceVariable(
                        task.getProcessInstanceId(), FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR).toString();
                String newDelegateAssignee = StrUtil.replace(
                        flowTaskComment.getDelegateAssignee(), FlowConstant.START_USER_NAME_VAR, startUser);
                flowTaskComment.setDelegateAssignee(newDelegateAssignee);
                transferUserList = StrUtil.split(flowTaskComment.getDelegateAssignee(), ",");
                break;
            }
        }
        taskService.unclaim(task.getId());
        FlowTaskExt taskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(
                task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (StrUtil.isNotBlank(taskExt.getCandidateUsernames())) {
            List<String> candidateUsernames = this.getCandidateUsernames(taskExt, task.getId());
            if (CollUtil.isNotEmpty(candidateUsernames)) {
                for (String username : candidateUsernames) {
                    taskService.deleteCandidateUser(task.getId(), username);
                }
            }
        } else if (StrUtil.equals(taskExt.getGroupType(), FlowConstant.GROUP_TYPE_ASSIGNEE)) {
            List<IdentityLink> links = taskService.getIdentityLinksForTask(task.getId());
            for (IdentityLink link : links) {
                taskService.deleteUserIdentityLink(task.getId(), link.getUserId(), link.getType());
            }
        } else {
            this.removeCandidateGroup(taskExt, task);
        }
        transferUserList.forEach(u -> taskService.addCandidateUser(task.getId(), u));
        flowTaskComment.fillWith(task);
        flowTaskCommentService.saveNew(flowTaskComment);
    }

    @Override
    public List<String> getCandidateUsernames(FlowTaskExt flowTaskExt, String taskId) {
        if (StrUtil.isBlank(flowTaskExt.getCandidateUsernames())) {
            return Collections.emptyList();
        }
        if (!StrUtil.equals(flowTaskExt.getCandidateUsernames(), "${" + FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR + "}")) {
            return StrUtil.split(flowTaskExt.getCandidateUsernames(), ",");
        }
        Object candidateUsernames = getTaskVariableStringWithSafe(taskId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR);
        return candidateUsernames == null ? null : StrUtil.split(candidateUsernames.toString(), ",");
    }

    @Override
    public Tuple2<Set<String>, Set<String>> getDeptPostIdAndPostIds(
            FlowTaskExt flowTaskExt, String processInstanceId, boolean historic) {
        Set<String> postIdSet = new LinkedHashSet<>();
        Set<String> deptPostIdSet = new LinkedHashSet<>();
        if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER)) {
            Object v = this.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_UP_DEPT_POST_LEADER_VAR, historic);
            if (ObjectUtil.isNotEmpty(v)) {
                deptPostIdSet.add(v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_DEPT_POST_LEADER)) {
            Object v = this.getProcessInstanceVariable(
                    processInstanceId, FlowConstant.GROUP_TYPE_DEPT_POST_LEADER_VAR, historic);
            if (ObjectUtil.isNotEmpty(v)) {
                deptPostIdSet.add(v.toString());
            }
        } else if (StrUtil.equals(flowTaskExt.getGroupType(), FlowConstant.GROUP_TYPE_POST)
                && StrUtil.isNotBlank(flowTaskExt.getDeptPostListJson())) {
            this.buildDeptPostIdAndPostIdsForPost(flowTaskExt, processInstanceId, historic, postIdSet, deptPostIdSet);
        }
        return new Tuple2<>(deptPostIdSet, postIdSet);
    }

    @Override
    public Map<String, UserTask> getAllUserTaskMap(String processDefinitionId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        Process process = bpmnModel.getProcesses().get(0);
        return process.findFlowElementsOfType(UserTask.class)
                .stream().collect(Collectors.toMap(UserTask::getId, a -> a, (k1, k2) -> k1));
    }

    @Override
    public UserTask getUserTask(String processDefinitionId, String taskKey) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        for (Process process : bpmnModel.getProcesses()) {
            UserTask userTask = process.findFlowElementsOfType(UserTask.class)
                    .stream().filter(t -> t.getId().equals(taskKey)).findFirst().orElse(null);
            if (userTask != null) {
                return userTask;
            }
        }
        return null;
    }

    private void doChangeState(String processInstanceId, List<String> currentIds, List<String> targetIds) {
        if (ObjectUtil.hasEmpty(currentIds, targetIds)) {
            throw new MyRuntimeException("跳转的源节点和任务节点数量均不能为空！");
        }
        ChangeActivityStateBuilder builder =
                this.createChangeActivityStateBuilder(currentIds, targetIds, processInstanceId);
        targetIds.forEach(targetId -> {
            FlowTaskComment comment = flowTaskCommentService.getLatestFlowTaskComment(processInstanceId, targetId);
            if (comment != null && StrUtil.isNotBlank(comment.getCreateLoginName())) {
                builder.localVariable(targetId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR, comment.getCreateLoginName());
            }
        });
        builder.changeState();
    }

    private ChangeActivityStateBuilder createChangeActivityStateBuilder(
            List<String> currentIds, List<String> targetIds, String processInstanceId) {
        ChangeActivityStateBuilder builder;
        if (currentIds.size() > 1 && targetIds.size() > 1) {
            builder = new CustomChangeActivityStateBuilderImpl((RuntimeServiceImpl) runtimeService);
            ((CustomChangeActivityStateBuilderImpl) builder)
                    .moveActivityIdsToActivityIds(currentIds, targetIds)
                    .processInstanceId(processInstanceId);
        } else {
            builder = runtimeService.createChangeActivityStateBuilder().processInstanceId(processInstanceId);
            if (targetIds.size() == 1) {
                if (currentIds.size() == 1) {
                    builder.moveActivityIdTo(currentIds.get(0), targetIds.get(0));
                } else {
                    builder.moveActivityIdsToSingleActivityId(currentIds, targetIds.get(0));
                }
            } else {
                builder.moveSingleActivityIdToActivityIds(currentIds.get(0), targetIds);
            }
        }
        return builder;
    }

    private void removeCandidateGroup(FlowTaskExt taskExt, Task task) {
        if (StrUtil.isNotBlank(taskExt.getDeptIds())) {
            for (String deptId : StrUtil.split(taskExt.getDeptIds(), ",")) {
                taskService.deleteCandidateGroup(task.getId(), deptId);
            }
        }
        if (StrUtil.isNotBlank(taskExt.getRoleIds())) {
            for (String roleId : StrUtil.split(taskExt.getRoleIds(), ",")) {
                taskService.deleteCandidateGroup(task.getId(), roleId);
            }
        }
        Tuple2<Set<String>, Set<String>> tuple2 =
                getDeptPostIdAndPostIds(taskExt, task.getProcessInstanceId(), false);
        if (CollUtil.isNotEmpty(tuple2.getFirst())) {
            for (String deptPostId : tuple2.getFirst()) {
                taskService.deleteCandidateGroup(task.getId(), deptPostId);
            }
        }
        if (CollUtil.isNotEmpty(tuple2.getSecond())) {
            for (String postId : tuple2.getSecond()) {
                taskService.deleteCandidateGroup(task.getId(), postId);
            }
        }
    }

    private void buildDeptPostIdAndPostIdsForPost(
            FlowTaskExt flowTaskExt,
            String processInstanceId,
            boolean historic,
            Set<String> postIdSet,
            Set<String> deptPostIdSet) {
        List<FlowTaskPostCandidateGroup> groupDataList =
                JSON.parseArray(flowTaskExt.getDeptPostListJson(), FlowTaskPostCandidateGroup.class);
        for (FlowTaskPostCandidateGroup groupData : groupDataList) {
            switch (groupData.getType()) {
                case FlowConstant.GROUP_TYPE_ALL_DEPT_POST_VAR:
                    postIdSet.add(groupData.getPostId());
                    break;
                case FlowConstant.GROUP_TYPE_DEPT_POST_VAR:
                    deptPostIdSet.add(groupData.getDeptPostId());
                    break;
                case FlowConstant.GROUP_TYPE_SELF_DEPT_POST_VAR:
                    Object v = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.SELF_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v)) {
                        deptPostIdSet.add(v.toString());
                    }
                    break;
                case FlowConstant.GROUP_TYPE_UP_DEPT_POST_VAR:
                    Object v2 = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.UP_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v2)) {
                        deptPostIdSet.add(v2.toString());
                    }
                    break;
                case FlowConstant.GROUP_TYPE_SIBLING_DEPT_POST_VAR:
                    Object v3 = this.getProcessInstanceVariable(
                            processInstanceId, FlowConstant.SIBLING_DEPT_POST_PREFIX + groupData.getPostId(), historic);
                    if (ObjectUtil.isNotEmpty(v3)) {
                        deptPostIdSet.addAll(StrUtil.split(v3.toString(), ",")
                                .stream().filter(StrUtil::isNotBlank).toList());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private Object getProcessInstanceVariable(String processInstanceId, String variableName, boolean historic) {
        if (historic) {
            return getHistoricProcessInstanceVariable(processInstanceId, variableName);
        }
        return getProcessInstanceVariable(processInstanceId, variableName);
    }

    private void handleMultiInstanceApprovalType(String executionId, String approvalType, JSONObject taskVariableData) {
        if (StrUtil.isBlank(approvalType)) {
            return;
        }
        if (StrUtil.equalsAny(approvalType,
                FlowApprovalType.MULTI_AGREE,
                FlowApprovalType.MULTI_REFUSE,
                FlowApprovalType.MULTI_ABSTAIN)) {
            Map<String, Object> variables = runtimeService.getVariables(executionId);
            Integer agreeCount = (Integer) variables.get(FlowConstant.MULTI_AGREE_COUNT_VAR);
            Integer refuseCount = (Integer) variables.get(FlowConstant.MULTI_REFUSE_COUNT_VAR);
            Integer abstainCount = (Integer) variables.get(FlowConstant.MULTI_ABSTAIN_COUNT_VAR);
            Integer nrOfInstances = (Integer) variables.get(FlowConstant.NUMBER_OF_INSTANCES_VAR);
            taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, agreeCount);
            taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, refuseCount);
            taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, abstainCount);
            taskVariableData.put(FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, nrOfInstances);
            switch (approvalType) {
                case FlowApprovalType.MULTI_AGREE:
                    if (agreeCount == null) {
                        agreeCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_AGREE_COUNT_VAR, agreeCount + 1);
                    break;
                case FlowApprovalType.MULTI_REFUSE:
                    if (refuseCount == null) {
                        refuseCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_REFUSE_COUNT_VAR, refuseCount + 1);
                    break;
                case FlowApprovalType.MULTI_ABSTAIN:
                    if (abstainCount == null) {
                        abstainCount = 0;
                    }
                    taskVariableData.put(FlowConstant.MULTI_ABSTAIN_COUNT_VAR, abstainCount + 1);
                    break;
                default:
                    break;
            }
        }
    }

    private TaskQuery createQuery() {
        TaskQuery query = taskService.createTaskQuery().active();
        TokenData tokenData = TokenData.takeFromRequest();
        if (tokenData.getTenantId() != null) {
            query.taskTenantId(tokenData.getTenantId().toString());
        } else {
            if (StrUtil.isBlank(tokenData.getAppCode())) {
                query.taskWithoutTenantId();
            } else {
                query.taskTenantId(tokenData.getAppCode());
            }
        }
        return query;
    }

    private void buildCandidateCondition(TaskQuery query, String loginName) {
        Set<String> groupIdSet = new HashSet<>();
        // NOTE: 需要注意的是，部门Id、部门岗位Id，或者其他类型的分组Id，他们之间一定不能重复。
        TokenData tokenData = TokenData.takeFromRequest();
        Object deptId = tokenData.getDeptId();
        if (deptId != null) {
            groupIdSet.add(deptId.toString());
        }
        String roleIds = tokenData.getRoleIds();
        if (StrUtil.isNotBlank(tokenData.getRoleIds())) {
            groupIdSet.addAll(StrUtil.split(roleIds, ","));
        }
        String postIds = tokenData.getPostIds();
        if (StrUtil.isNotBlank(tokenData.getPostIds())) {
            groupIdSet.addAll(StrUtil.split(postIds, ","));
        }
        String deptPostIds = tokenData.getDeptPostIds();
        if (StrUtil.isNotBlank(deptPostIds)) {
            groupIdSet.addAll(StrUtil.split(deptPostIds, ","));
        }
        if (CollUtil.isNotEmpty(groupIdSet)) {
            query.or().taskCandidateGroupIn(groupIdSet).taskCandidateOrAssigned(loginName).endOr();
        } else {
            query.taskCandidateOrAssigned(loginName);
        }
    }

    private String buildMutiSignAssigneeList(String operationListJson) {
        FlowTaskMultiSignAssign multiSignAssignee = null;
        List<FlowTaskOperation> taskOperationList = JSONArray.parseArray(operationListJson, FlowTaskOperation.class);
        for (FlowTaskOperation taskOperation : taskOperationList) {
            if (FlowApprovalType.MULTI_SIGN.equals(taskOperation.getType())) {
                multiSignAssignee = taskOperation.getMultiSignAssignee();
                break;
            }
        }
        org.springframework.util.Assert.notNull(multiSignAssignee, "multiSignAssignee can't be NULL");
        if (UserFilterGroup.USER.equals(multiSignAssignee.getAssigneeType())) {
            return multiSignAssignee.getAssigneeList();
        }
        Set<String> usernameSet = null;
        BaseFlowIdentityExtHelper extHelper = flowCustomExtFactory.getFlowIdentityExtHelper();
        Set<String> idSet = CollUtil.newHashSet(StrUtil.split(multiSignAssignee.getAssigneeList(), ","));
        switch (multiSignAssignee.getAssigneeType()) {
            case UserFilterGroup.ROLE -> usernameSet = extHelper.getUsernameListByRoleIds(idSet);
            case UserFilterGroup.DEPT -> usernameSet = extHelper.getUsernameListByDeptIds(idSet);
            case UserFilterGroup.POST -> usernameSet = extHelper.getUsernameListByPostIds(idSet);
            case UserFilterGroup.DEPT_POST -> usernameSet = extHelper.getUsernameListByDeptPostIds(idSet);
            default -> {
            }
        }
        return CollUtil.isEmpty(usernameSet) ? null : CollUtil.join(usernameSet, ",");
    }

    private Collection<FlowElement> getAllElements(Collection<FlowElement> flowElements, Collection<FlowElement> allElements) {
        allElements = allElements == null ? new ArrayList<>() : allElements;
        for (FlowElement flowElement : flowElements) {
            allElements.add(flowElement);
            if (flowElement instanceof SubProcess) {
                allElements = getAllElements(((SubProcess) flowElement).getFlowElements(), allElements);
            }
        }
        return allElements;
    }

    private void doChangeTask(Task runtimeTask) {
        Map<String, UserTask> allUserTaskMap =
                this.getAllUserTaskMap(runtimeTask.getProcessDefinitionId());
        UserTask userTaskModel = allUserTaskMap.get(runtimeTask.getTaskDefinitionKey());
        String completeCondition = userTaskModel.getLoopCharacteristics().getCompletionCondition();
        Execution parentExecution = this.getMultiInstanceRootExecution(runtimeTask);
        Object nrOfCompletedInstances = runtimeService.getVariable(
                parentExecution.getId(), FlowConstant.NUMBER_OF_COMPLETED_INSTANCES_VAR);
        Object nrOfInstances = runtimeService.getVariable(
                parentExecution.getId(), FlowConstant.NUMBER_OF_INSTANCES_VAR);
        ExpressionFactory factory = new ExpressionFactoryImpl();
        SimpleContext context = new SimpleContext();
        context.setVariable("nrOfCompletedInstances",
                factory.createValueExpression(nrOfCompletedInstances, Integer.class));
        context.setVariable("nrOfInstances",
                factory.createValueExpression(nrOfInstances, Integer.class));
        ValueExpression e = factory.createValueExpression(context, completeCondition, Boolean.class);
        Boolean ok = Convert.convert(Boolean.class, e.getValue(context));
        if (BooleanUtil.isTrue(ok)) {
            FlowElement targetKey = userTaskModel.getOutgoingFlows().get(0).getTargetFlowElement();
            ChangeActivityStateBuilder builder = runtimeService.createChangeActivityStateBuilder()
                    .processInstanceId(runtimeTask.getProcessInstanceId())
                    .moveActivityIdTo(userTaskModel.getId(), targetKey.getId());
            builder.localVariable(targetKey.getId(), FlowConstant.MULTI_SIGN_NUM_OF_INSTANCES_VAR, nrOfInstances);
            builder.changeState();
        }
    }

    private Execution getMultiInstanceRootExecution(Task runtimeTask) {
        List<Execution> executionList = runtimeService.createExecutionQuery()
                .processInstanceId(runtimeTask.getProcessInstanceId())
                .activityId(runtimeTask.getTaskDefinitionKey()).list();
        for (Execution e : executionList) {
            ExecutionEntityImpl ee = (ExecutionEntityImpl) e;
            if (ee.isMultiInstanceRoot()) {
                return e;
            }
        }
        Execution execution = executionList.get(0);
        return runtimeService.createExecutionQuery()
                .processInstanceId(runtimeTask.getProcessInstanceId())
                .executionId(execution.getParentId()).singleResult();
    }

    private List<SequenceFlow> getElementIncomingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof org.flowable.bpmn.model.Task) {
            sequenceFlows = ((org.flowable.bpmn.model.Task) source).getIncomingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getIncomingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getIncomingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getIncomingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getIncomingFlows();
        }
        return sequenceFlows;
    }

    private List<SequenceFlow> getElementOutgoingFlows(FlowElement source) {
        List<SequenceFlow> sequenceFlows = null;
        if (source instanceof org.flowable.bpmn.model.Task) {
            sequenceFlows = ((org.flowable.bpmn.model.Task) source).getOutgoingFlows();
        } else if (source instanceof Gateway) {
            sequenceFlows = ((Gateway) source).getOutgoingFlows();
        } else if (source instanceof SubProcess) {
            sequenceFlows = ((SubProcess) source).getOutgoingFlows();
        } else if (source instanceof StartEvent) {
            sequenceFlows = ((StartEvent) source).getOutgoingFlows();
        } else if (source instanceof EndEvent) {
            sequenceFlows = ((EndEvent) source).getOutgoingFlows();
        }
        return sequenceFlows;
    }

    private FlowableListener createListener(String eventName, String listenerClassName) {
        FlowableListener listener = new FlowableListener();
        listener.setEvent(eventName);
        listener.setImplementationType("class");
        listener.setImplementation(listenerClassName);
        return listener;
    }
}
