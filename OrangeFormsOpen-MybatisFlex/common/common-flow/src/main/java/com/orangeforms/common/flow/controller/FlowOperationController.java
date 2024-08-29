package com.orangeforms.common.flow.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import io.swagger.v3.oas.annotations.tags.Tag;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.annotation.DisableDataFilter;
import com.orangeforms.common.core.annotation.MyRequestBody;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.object.*;
import com.orangeforms.common.core.util.MyPageUtil;
import com.orangeforms.common.core.util.MyModelUtil;
import com.orangeforms.common.log.annotation.OperationLog;
import com.orangeforms.common.log.model.constant.SysOperationLogType;
import com.orangeforms.common.flow.constant.FlowApprovalType;
import com.orangeforms.common.flow.exception.FlowOperationException;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.constant.FlowTaskStatus;
import com.orangeforms.common.flow.model.*;
import com.orangeforms.common.flow.service.*;
import com.orangeforms.common.flow.util.FlowCustomExtFactory;
import com.orangeforms.common.flow.util.FlowOperationHelper;
import com.orangeforms.common.flow.vo.FlowTaskCommentVo;
import com.orangeforms.common.flow.vo.FlowTaskVo;
import com.orangeforms.common.flow.vo.FlowUserInfoVo;
import com.orangeforms.common.flow.vo.TaskInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskInfo;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 工作流流程操作接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Tag(name = "工作流流程操作接口")
@Slf4j
@RestController
@RequestMapping("${common-flow.urlPrefix}/flowOperation")
@ConditionalOnProperty(name = "common-flow.operationEnabled", havingValue = "true")
public class FlowOperationController {

    @Autowired
    private FlowEntryService flowEntryService;
    @Autowired
    private FlowTaskCommentService flowTaskCommentService;
    @Autowired
    private FlowTaskExtService flowTaskExtService;
    @Autowired
    private FlowApiService flowApiService;
    @Autowired
    private FlowWorkOrderService flowWorkOrderService;
    @Autowired
    private FlowMessageService flowMessageService;
    @Autowired
    private FlowOperationHelper flowOperationHelper;
    @Autowired
    private FlowCustomExtFactory flowCustomExtFactory;
    @Autowired
    private FlowMultiInstanceTransService flowMultiInstanceTransService;

    private static final String ACTIVE_MULTI_INST_TASK = "activeMultiInstanceTask";
    private static final String SHOW_NAME = "showName";
    private static final String INSTANCE_ID = "processInstanceId";

    /**
     * 根据指定流程的主版本，发起一个流程实例。
     *
     * @param processDefinitionKey 流程标识。
     * @return 应答结果对象。
     */
    @SaCheckPermission("flowOperation.all")
    @OperationLog(type = SysOperationLogType.START_FLOW)
    @PostMapping("/startOnly")
    public ResponseResult<Void> startOnly(@MyRequestBody(required = true) String processDefinitionKey) {
        // 1. 验证流程数据的合法性。
        ResponseResult<FlowEntry> flowEntryResult = flowOperationHelper.verifyAndGetFlowEntry(processDefinitionKey);
        if (!flowEntryResult.isSuccess()) {
            return ResponseResult.errorFrom(flowEntryResult);
        }
        // 2. 验证流程一个用户任务的合法性。
        FlowEntryPublish flowEntryPublish = flowEntryResult.getData().getMainFlowEntryPublish();
        ResponseResult<TaskInfoVo> taskInfoResult =
                flowOperationHelper.verifyAndGetInitialTaskInfo(flowEntryPublish, false);
        if (!taskInfoResult.isSuccess()) {
            return ResponseResult.errorFrom(taskInfoResult);
        }
        flowApiService.start(flowEntryPublish.getProcessDefinitionId(), null);
        return ResponseResult.success();
    }

    /**
     * 获取开始节点之后的第一个任务节点的数据。
     *
     * @param processDefinitionKey 流程标识。
     * @return 任务节点的自定义对象数据。
     */
    @GetMapping("/viewInitialTaskInfo")
    public ResponseResult<TaskInfoVo> viewInitialTaskInfo(@RequestParam String processDefinitionKey) {
        ResponseResult<FlowEntry> flowEntryResult = flowOperationHelper.verifyAndGetFlowEntry(processDefinitionKey);
        if (!flowEntryResult.isSuccess()) {
            return ResponseResult.errorFrom(flowEntryResult);
        }
        FlowEntryPublish flowEntryPublish = flowEntryResult.getData().getMainFlowEntryPublish();
        String initTaskInfo = flowEntryPublish.getInitTaskInfo();
        TaskInfoVo taskInfo = StrUtil.isBlank(initTaskInfo)
                ? null : JSON.parseObject(initTaskInfo, TaskInfoVo.class);
        if (taskInfo != null) {
            String loginName = TokenData.takeFromRequest().getLoginName();
            taskInfo.setAssignedMe(StrUtil.equalsAny(
                    taskInfo.getAssignee(), loginName, FlowConstant.START_USER_NAME_VAR));
        }
        return ResponseResult.success(taskInfo);
    }

    /**
     * 获取流程运行时指定任务的信息。
     *
     * @param processDefinitionId 流程引擎的定义Id。
     * @param processInstanceId   流程引擎的实例Id。
     * @param taskId              流程引擎的任务Id。
     * @return 任务节点的自定义对象数据。
     */
    @GetMapping("/viewRuntimeTaskInfo")
    public ResponseResult<TaskInfoVo> viewRuntimeTaskInfo(
            @RequestParam String processDefinitionId,
            @RequestParam String processInstanceId,
            @RequestParam String taskId) {
        Task task = flowApiService.getProcessInstanceActiveTask(processInstanceId, taskId);
        ResponseResult<TaskInfoVo> taskInfoResult = flowOperationHelper.verifyAndGetRuntimeTaskInfo(task);
        if (!taskInfoResult.isSuccess()) {
            return ResponseResult.errorFrom(taskInfoResult);
        }
        TaskInfoVo taskInfoVo = taskInfoResult.getData();
        FlowTaskExt flowTaskExt =
                flowTaskExtService.getByProcessDefinitionIdAndTaskId(processDefinitionId, taskInfoVo.getTaskKey());
        if (flowTaskExt != null) {
            if (StrUtil.isNotBlank(flowTaskExt.getOperationListJson())) {
                taskInfoVo.setOperationList(JSON.parseArray(flowTaskExt.getOperationListJson(), JSONObject.class));
            }
            if (StrUtil.isNotBlank(flowTaskExt.getVariableListJson())) {
                taskInfoVo.setVariableList(JSON.parseArray(flowTaskExt.getVariableListJson(), JSONObject.class));
            }
        }
        return ResponseResult.success(taskInfoVo);
    }

    /**
     * 获取流程运行时指定任务的信息。
     *
     * @param processDefinitionId 流程引擎的定义Id。
     * @param processInstanceId   流程引擎的实例Id。
     * @param taskId              流程引擎的任务Id。
     * @return 任务节点的自定义对象数据。
     */
    @GetMapping("/viewHistoricTaskInfo")
    public ResponseResult<TaskInfoVo> viewHistoricTaskInfo(
            @RequestParam String processDefinitionId,
            @RequestParam String processInstanceId,
            @RequestParam String taskId) {
        String errorMessage;
        HistoricTaskInstance taskInstance = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
        String loginName = TokenData.takeFromRequest().getLoginName();
        if (!StrUtil.equals(taskInstance.getAssignee(), loginName)) {
            errorMessage = "数据验证失败，当前用户不是指派人！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        TaskInfoVo taskInfoVo = JSON.parseObject(taskInstance.getFormKey(), TaskInfoVo.class);
        FlowTaskExt flowTaskExt =
                flowTaskExtService.getByProcessDefinitionIdAndTaskId(processDefinitionId, taskInstance.getTaskDefinitionKey());
        if (flowTaskExt != null) {
            if (StrUtil.isNotBlank(flowTaskExt.getOperationListJson())) {
                taskInfoVo.setOperationList(JSON.parseArray(flowTaskExt.getOperationListJson(), JSONObject.class));
            }
            if (StrUtil.isNotBlank(flowTaskExt.getVariableListJson())) {
                taskInfoVo.setVariableList(JSON.parseArray(flowTaskExt.getVariableListJson(), JSONObject.class));
            }
        }
        return ResponseResult.success(taskInfoVo);
    }

    /**
     * 获取第一个提交表单数据的任务信息。
     *
     * @param processInstanceId 流程实例Id。
     * @return 任务节点的自定义对象数据。
     */
    @GetMapping("/viewInitialHistoricTaskInfo")
    public ResponseResult<TaskInfoVo> viewInitialHistoricTaskInfo(@RequestParam String processInstanceId) {
        String errorMessage;
        List<FlowTaskComment> taskCommentList =
                flowTaskCommentService.getFlowTaskCommentList(processInstanceId);
        if (CollUtil.isEmpty(taskCommentList)) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        FlowTaskComment taskComment = taskCommentList.get(0);
        HistoricTaskInstance task = flowApiService.getHistoricTaskInstance(processInstanceId, taskComment.getTaskId());
        if (StrUtil.isBlank(task.getFormKey())) {
            errorMessage = "数据验证失败，指定任务的formKey属性不存在，请重新修改流程图！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        TaskInfoVo taskInfo = JSON.parseObject(task.getFormKey(), TaskInfoVo.class);
        taskInfo.setTaskKey(task.getTaskDefinitionKey());
        return ResponseResult.success(taskInfo);
    }

    /**
     * 获取任务的用户信息列表。
     *
     * @param processDefinitionId 流程定义Id。
     * @param processInstanceId   流程实例Id。
     * @param taskId              流程任务Id。
     * @param historic            是否为历史任务。
     * @return 任务相关的用户信息列表。
     */
    @DisableDataFilter
    @GetMapping("/viewTaskUserInfo")
    public ResponseResult<List<FlowUserInfoVo>> viewTaskUserInfo(
            @RequestParam String processDefinitionId,
            @RequestParam String processInstanceId,
            @RequestParam String taskId,
            @RequestParam Boolean historic) {
        TaskInfo taskInfo;
        HistoricTaskInstance hisotricTask;
        if (BooleanUtil.isFalse(historic)) {
            taskInfo = flowApiService.getTaskById(taskId);
            if (taskInfo == null) {
                hisotricTask = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
                taskInfo = hisotricTask;
                historic = true;
            }
        } else {
            hisotricTask = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
            taskInfo = hisotricTask;
        }
        if (taskInfo == null) {
            String errorMessage = "数据验证失败，任务Id不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST, errorMessage);
        }
        String taskKey = taskInfo.getTaskDefinitionKey();
        FlowTaskExt taskExt = flowTaskExtService.getByProcessDefinitionIdAndTaskId(processDefinitionId, taskKey);
        boolean isMultiInstanceTask = flowApiService.isMultiInstanceTask(taskInfo.getProcessDefinitionId(), taskKey);
        List<FlowUserInfoVo> resultUserInfoList =
                flowTaskExtService.getCandidateUserInfoList(processInstanceId, taskExt, taskInfo, isMultiInstanceTask, historic);
        if (BooleanUtil.isTrue(historic) || isMultiInstanceTask) {
            List<FlowTaskComment> taskCommentList = buildApprovedFlowTaskCommentList(taskInfo, isMultiInstanceTask);
            Map<String, FlowUserInfoVo> resultUserInfoMap =
                    resultUserInfoList.stream().collect(Collectors.toMap(FlowUserInfoVo::getLoginName, c -> c));
            for (FlowTaskComment taskComment : taskCommentList) {
                FlowUserInfoVo flowUserInfoVo = resultUserInfoMap.get(taskComment.getCreateLoginName());
                if (flowUserInfoVo != null) {
                    flowUserInfoVo.setLastApprovalTime(taskComment.getCreateTime());
                }
            }
        }
        return ResponseResult.success(resultUserInfoList);
    }

    /**
     * 获取多实例会签任务的指派人列表。
     * NOTE: 白名单接口。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            多实例任务的上一级任务Id。
     * @return 应答结果，指定会签任务的指派人列表。
     */
    @GetMapping("/listMultiSignAssignees")
    public ResponseResult<List<JSONObject>> listMultiSignAssignees(
            @RequestParam String processInstanceId, @RequestParam String taskId) {
        ResponseResult<JSONObject> verifyResult = this.doVerifyMultiSign(processInstanceId, taskId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        Task activeMultiInstanceTask =
                verifyResult.getData().getObject(ACTIVE_MULTI_INST_TASK, Task.class);
        String multiInstanceExecId = flowApiService.getExecutionVariableStringWithSafe(
                activeMultiInstanceTask.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
        FlowMultiInstanceTrans trans =
                flowMultiInstanceTransService.getWithAssigneeListByMultiInstanceExecId(multiInstanceExecId);
        List<FlowTaskComment> commentList =
                flowTaskCommentService.getFlowTaskCommentListByMultiInstanceExecId(multiInstanceExecId);
        List<String> assigneeList = StrUtil.split(trans.getAssigneeList(), ",");
        Set<String> approvedAssigneeSet = commentList.stream()
                .map(FlowTaskComment::getCreateLoginName).collect(Collectors.toSet());
        List<JSONObject> resultList = new LinkedList<>();
        Map<String, String> usernameMap =
                flowCustomExtFactory.getFlowIdentityExtHelper().mapUserShowNameByLoginName(new HashSet<>(assigneeList));
        for (String assignee : assigneeList) {
            JSONObject resultData = new JSONObject();
            resultData.put("assignee", assignee);
            resultData.put(SHOW_NAME, usernameMap.get(assignee));
            resultData.put("approved", approvedAssigneeSet.contains(assignee));
            resultList.add(resultData);
        }
        return ResponseResult.success(resultList);
    }

    /**
     * 提交多实例加签或减签。
     * NOTE: 白名单接口。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            多实例任务的上一级任务Id。
     * @param newAssignees      加签减签人列表，多个指派人之间逗号分隔。
     * @param isAdd             是否为加签，如果没有该参数，为了保持兼容性，缺省值为true。
     * @return 应答结果。
     */
    @PostMapping("/submitConsign")
    public ResponseResult<Void> submitConsign(
            @MyRequestBody(required = true) String processInstanceId,
            @MyRequestBody(required = true) String taskId,
            @MyRequestBody(required = true) String newAssignees,
            @MyRequestBody Boolean isAdd) {
        String errorMessage;
        ResponseResult<JSONObject> verifyResult = this.doVerifyMultiSign(processInstanceId, taskId);
        if (!verifyResult.isSuccess()) {
            return ResponseResult.errorFrom(verifyResult);
        }
        HistoricTaskInstance taskInstance =
                verifyResult.getData().getObject("taskInstance", HistoricTaskInstance.class);
        Task activeMultiInstanceTask =
                verifyResult.getData().getObject(ACTIVE_MULTI_INST_TASK, Task.class);
        String multiInstanceExecId = flowApiService.getExecutionVariableStringWithSafe(
                activeMultiInstanceTask.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
        JSONArray assigneeArray = JSON.parseArray(newAssignees);
        if (isAdd == null) {
            isAdd = true;
        }
        if (!isAdd) {
            List<FlowTaskComment> commentList =
                    flowTaskCommentService.getFlowTaskCommentListByMultiInstanceExecId(multiInstanceExecId);
            if (CollUtil.isNotEmpty(commentList)) {
                Set<String> approvedAssigneeSet = commentList.stream()
                        .map(FlowTaskComment::getCreateLoginName).collect(Collectors.toSet());
                String loginName = this.findExistAssignee(approvedAssigneeSet, assigneeArray);
                if (loginName != null) {
                    errorMessage = "数据验证失败，用户 [" + loginName + "] 已经审批，不能减签该用户！";
                    return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
                }
            }
        } else {
            // 避免同一人被重复加签。
            FlowMultiInstanceTrans trans =
                    flowMultiInstanceTransService.getWithAssigneeListByMultiInstanceExecId(multiInstanceExecId);
            Set<String> assigneeSet = new HashSet<>(StrUtil.split(trans.getAssigneeList(), ","));
            String loginName = this.findExistAssignee(assigneeSet, assigneeArray);
            if (loginName != null) {
                errorMessage = "数据验证失败，用户 [" + loginName + "] 已经是会签人，不能重复指定！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
        }
        try {
            flowApiService.submitConsign(taskInstance, activeMultiInstanceTask, newAssignees, isAdd);
        } catch (FlowOperationException e) {
            errorMessage = e.getMessage();
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        return ResponseResult.success();
    }

    /**
     * 返回当前用户待办的任务列表。
     *
     * @param processDefinitionKey  流程标识。
     * @param processDefinitionName 流程定义名 (模糊查询)。
     * @param taskName              任务名称 (模糊查询)。
     * @param pageParam             分页对象。
     * @return 返回当前用户待办的任务列表。如果指定流程标识，则仅返回该流程的待办任务列表。
     */
    @DisableDataFilter
    @PostMapping("/listRuntimeTask")
    public ResponseResult<MyPageData<FlowTaskVo>> listRuntimeTask(
            @MyRequestBody String processDefinitionKey,
            @MyRequestBody String processDefinitionName,
            @MyRequestBody String taskName,
            @MyRequestBody(required = true) MyPageParam pageParam) {
        String username = TokenData.takeFromRequest().getLoginName();
        MyPageData<Task> pageData = flowApiService.getTaskListByUserName(
                username, processDefinitionKey, processDefinitionName, taskName, pageParam);
        List<FlowTaskVo> flowTaskVoList = flowApiService.convertToFlowTaskList(pageData.getDataList());
        return ResponseResult.success(MyPageUtil.makeResponseData(flowTaskVoList, pageData.getTotalCount()));
    }

    /**
     * 返回当前用户待办的任务数量。
     *
     * @return 返回当前用户待办的任务数量。
     */
    @PostMapping("/countRuntimeTask")
    public ResponseResult<Long> countRuntimeTask() {
        String username = TokenData.takeFromRequest().getLoginName();
        long totalCount = flowApiService.getTaskCountByUserName(username);
        return ResponseResult.success(totalCount);
    }

    /**
     * 主动驳回当前的待办任务到开始节点，只用当前待办任务的指派人或者候选者才能完成该操作。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            待办任务Id。
     * @param taskComment       驳回备注。
     * @return 操作应答结果。
     */
    @PostMapping("/rejectToStartUserTask")
    public ResponseResult<Void> rejectToStartUserTask(
            @MyRequestBody(required = true) String processInstanceId,
            @MyRequestBody(required = true) String taskId,
            @MyRequestBody(required = true) String taskComment) {
        ResponseResult<Task> taskResult =
                flowOperationHelper.verifySubmitAndGetTask(processInstanceId, taskId, null);
        if (!taskResult.isSuccess()) {
            return ResponseResult.errorFrom(taskResult);
        }
        FlowTaskComment firstTaskComment = flowTaskCommentService.getFirstFlowTaskComment(processInstanceId);
        CallResult result = flowApiService.backToRuntimeTask(
                taskResult.getData(), firstTaskComment.getTaskKey(), true, taskComment);
        if (!result.isSuccess()) {
            return ResponseResult.errorFrom(result);
        }
        return ResponseResult.success();
    }

    /**
     * 主动驳回当前的待办任务，只用当前待办任务的指派人或者候选者才能完成该操作。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            待办任务Id。
     * @param taskComment       驳回备注。
     * @return 操作应答结果。
     */
    @PostMapping("/rejectRuntimeTask")
    public ResponseResult<Void> rejectRuntimeTask(
            @MyRequestBody(required = true) String processInstanceId,
            @MyRequestBody(required = true) String taskId,
            @MyRequestBody(required = true) String taskComment) {
        String errorMessage;
        ResponseResult<Task> taskResult =
                flowOperationHelper.verifySubmitAndGetTask(processInstanceId, taskId, null);
        if (!taskResult.isSuccess()) {
            return ResponseResult.errorFrom(taskResult);
        }
        CallResult result = flowApiService.backToRuntimeTask(taskResult.getData(), null, true, taskComment);
        if (!result.isSuccess()) {
            return ResponseResult.errorFrom(result);
        }
        return ResponseResult.success();
    }

    /**
     * 撤回当前用户提交的，但是尚未被审批的待办任务。只有已办任务的指派人才能完成该操作。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            待撤回的已办任务Id。
     * @param taskComment       撤回备注。
     * @return 操作应答结果。
     */
    @PostMapping("/revokeHistoricTask")
    public ResponseResult<Void> revokeHistoricTask(
            @MyRequestBody(required = true) String processInstanceId,
            @MyRequestBody(required = true) String taskId,
            @MyRequestBody(required = true) String taskComment) {
        String errorMessage;
        if (!flowApiService.existActiveProcessInstance(processInstanceId)) {
            errorMessage = "数据验证失败，当前流程实例已经结束，不能执行撤回！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        HistoricTaskInstance taskInstance = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
        if (taskInstance == null) {
            errorMessage = "数据验证失败，当前任务不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!StrUtil.equals(taskInstance.getAssignee(), TokenData.takeFromRequest().getLoginName())) {
            errorMessage = "数据验证失败，任务指派人与当前用户不匹配！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        FlowTaskComment latestComment = flowTaskCommentService.getLatestFlowTaskComment(processInstanceId);
        if (latestComment == null) {
            errorMessage = "数据验证失败，当前实例没有任何审批提交记录！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!latestComment.getTaskId().equals(taskId)) {
            errorMessage = "数据验证失败，当前审批任务已被办理，不能撤回！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        List<Task> activeTaskList = flowApiService.getProcessInstanceActiveTaskList(processInstanceId);
        if (CollUtil.isEmpty(activeTaskList)) {
            errorMessage = "数据验证失败，当前流程没有任何待办任务！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (latestComment.getApprovalType().equals(FlowApprovalType.TRANSFER)) {
            if (activeTaskList.size() > 1) {
                errorMessage = "数据验证失败，转办任务数量不能多于1个！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            // 如果是转办任务，无需节点跳转，将指派人改为当前用户即可。
            Task task = activeTaskList.get(0);
            task.setAssignee(TokenData.takeFromRequest().getLoginName());
        } else {
            CallResult result =
                    flowApiService.backToRuntimeTask(activeTaskList.get(0), null, false, taskComment);
            if (!result.isSuccess()) {
                return ResponseResult.errorFrom(result);
            }
        }
        return ResponseResult.success();
    }

    /**
     * 获取当前流程任务的审批列表。
     *
     * @param processInstanceId 当前运行时的流程实例Id。
     * @return 当前流程实例的详情数据。
     */
    @GetMapping("/listFlowTaskComment")
    public ResponseResult<List<FlowTaskCommentVo>> listFlowTaskComment(@RequestParam String processInstanceId) {
        List<FlowTaskComment> flowTaskCommentList =
                flowTaskCommentService.getFlowTaskCommentList(processInstanceId);
        List<FlowTaskCommentVo> resultList = MyModelUtil.copyCollectionTo(flowTaskCommentList, FlowTaskCommentVo.class);
        return ResponseResult.success(resultList);
    }

    /**
     * 获取指定流程定义的流程图。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 流程图。
     */
    @GetMapping("/viewProcessBpmn")
    public ResponseResult<String> viewProcessBpmn(@RequestParam String processDefinitionId) throws IOException {
        BpmnXMLConverter converter = new BpmnXMLConverter();
        BpmnModel bpmnModel = flowApiService.getBpmnModelByDefinitionId(processDefinitionId);
        byte[] xmlBytes = converter.convertToXML(bpmnModel);
        InputStream in = new ByteArrayInputStream(xmlBytes);
        return ResponseResult.success(StreamUtils.copyToString(in, StandardCharsets.UTF_8));
    }

    /**
     * 获取指定流程定义的指定任务Id的formKey。
     *
     * @param processInstanceId 流程实例Id。
     * @param taskId            流程任务Id。
     * @return formKey数据。
     */
    @GetMapping("/viewTaskFormKey")
    public ResponseResult<String> viewTaskFormKey(
            @RequestParam String processInstanceId, @RequestParam String taskId) throws IOException {
        TaskInfo task = flowApiService.getTaskById(taskId);
        if (task == null) {
            task = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
            if (task == null) {
                return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
            }
        }
        UserTask userTask = flowApiService.getUserTask(task.getProcessDefinitionId(), task.getTaskDefinitionKey());
        if (userTask == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        return ResponseResult.success(userTask.getFormKey());
    }

    /**
     * 获取流程图高亮数据。
     *
     * @param processInstanceId 流程实例Id。
     * @return 流程图高亮数据。
     */
    @GetMapping("/viewHighlightFlowData")
    public ResponseResult<JSONObject> viewHighlightFlowData(@RequestParam String processInstanceId) {
        List<HistoricActivityInstance> activityInstanceList =
                flowApiService.getHistoricActivityInstanceList(processInstanceId);
        Set<String> finishedTaskSet = activityInstanceList.stream()
                .filter(s -> !StrUtil.equals(s.getActivityType(), "sequenceFlow"))
                .map(HistoricActivityInstance::getActivityId).collect(Collectors.toSet());
        Set<String> finishedSequenceFlowSet = activityInstanceList.stream()
                .filter(s -> StrUtil.equals(s.getActivityType(), "sequenceFlow"))
                .map(HistoricActivityInstance::getActivityId).collect(Collectors.toSet());
        //获取流程实例当前正在待办的节点
        List<HistoricActivityInstance> unfinishedInstanceList =
                flowApiService.getHistoricUnfinishedInstanceList(processInstanceId);
        Set<String> unfinishedTaskSet = new LinkedHashSet<>();
        for (HistoricActivityInstance unfinishedActivity : unfinishedInstanceList) {
            unfinishedTaskSet.add(unfinishedActivity.getActivityId());
        }
        JSONObject jsonData = new JSONObject();
        jsonData.put("finishedTaskSet", finishedTaskSet);
        jsonData.put("finishedSequenceFlowSet", finishedSequenceFlowSet);
        jsonData.put("unfinishedTaskSet", unfinishedTaskSet);
        return ResponseResult.success(jsonData);
    }

    /**
     * 获取当前用户的已办理的审批任务列表。
     *
     * @param processDefinitionName 流程名。
     * @param beginDate             流程发起开始时间。
     * @param endDate               流程发起结束时间。
     * @param pageParam             分页对象。
     * @return 查询结果应答。
     */
    @DisableDataFilter
    @PostMapping("/listHistoricTask")
    public ResponseResult<MyPageData<Map<String, Object>>> listHistoricTask(
            @MyRequestBody String processDefinitionName,
            @MyRequestBody String beginDate,
            @MyRequestBody String endDate,
            @MyRequestBody(required = true) MyPageParam pageParam) throws ParseException {
        MyPageData<HistoricTaskInstance> pageData =
                flowApiService.getHistoricTaskInstanceFinishedList(processDefinitionName, beginDate, endDate, pageParam);
        List<Map<String, Object>> resultList = new LinkedList<>();
        pageData.getDataList().forEach(instance -> resultList.add(BeanUtil.beanToMap(instance)));
        List<HistoricTaskInstance> taskInstanceList = pageData.getDataList();
        if (CollUtil.isNotEmpty(taskInstanceList)) {
            Set<String> instanceIdSet = taskInstanceList.stream()
                    .map(HistoricTaskInstance::getProcessInstanceId).collect(Collectors.toSet());
            List<HistoricProcessInstance> instanceList = flowApiService.getHistoricProcessInstanceList(instanceIdSet);
            Set<String> loginNameSet = instanceList.stream()
                    .map(HistoricProcessInstance::getStartUserId).collect(Collectors.toSet());
            List<FlowUserInfoVo> userInfoList = flowCustomExtFactory
                    .getFlowIdentityExtHelper().getUserInfoListByUsernameSet(loginNameSet);
            Map<String, FlowUserInfoVo> userInfoMap =
                    userInfoList.stream().collect(Collectors.toMap(FlowUserInfoVo::getLoginName, c -> c));
            Map<String, HistoricProcessInstance> instanceMap =
                    instanceList.stream().collect(Collectors.toMap(HistoricProcessInstance::getId, c -> c));
            List<FlowWorkOrder> workOrderList =
                    flowWorkOrderService.getInList(INSTANCE_ID, instanceIdSet);
            Map<String, FlowWorkOrder> workOrderMap =
                    workOrderList.stream().collect(Collectors.toMap(FlowWorkOrder::getProcessInstanceId, c -> c));
            resultList.forEach(result -> {
                String instanceId = result.get(INSTANCE_ID).toString();
                HistoricProcessInstance instance = instanceMap.get(instanceId);
                result.put("processDefinitionKey", instance.getProcessDefinitionKey());
                result.put("processDefinitionName", instance.getProcessDefinitionName());
                result.put("startUser", instance.getStartUserId());
                FlowUserInfoVo userInfo = userInfoMap.get(instance.getStartUserId());
                result.put(SHOW_NAME, userInfo.getShowName());
                result.put("headImageUrl", userInfo.getHeadImageUrl());
                result.put("businessKey", instance.getBusinessKey());
                FlowWorkOrder flowWorkOrder = workOrderMap.get(instanceId);
                if (flowWorkOrder != null) {
                    result.put("workOrderCode", flowWorkOrder.getWorkOrderCode());
                }
            });
            Set<String> taskIdSet =
                    taskInstanceList.stream().map(HistoricTaskInstance::getId).collect(Collectors.toSet());
            List<FlowTaskComment> commentList = flowTaskCommentService.getFlowTaskCommentListByTaskIds(taskIdSet);
            Map<String, List<FlowTaskComment>> commentMap =
                    commentList.stream().collect(Collectors.groupingBy(FlowTaskComment::getTaskId));
            resultList.forEach(result -> {
                List<FlowTaskComment> comments = commentMap.get(result.get("id").toString());
                if (CollUtil.isNotEmpty(comments)) {
                    result.put("approvalType", comments.get(0).getApprovalType());
                    comments.remove(0);
                }
            });
        }
        return ResponseResult.success(MyPageUtil.makeResponseData(resultList, pageData.getTotalCount()));
    }

    /**
     * 根据输入参数查询，当前用户的历史流程数据。
     *
     * @param processDefinitionName 流程名。
     * @param beginDate             流程发起开始时间。
     * @param endDate               流程发起结束时间。
     * @param pageParam             分页对象。
     * @return 查询结果应答。
     */
    @DisableDataFilter
    @PostMapping("/listHistoricProcessInstance")
    public ResponseResult<MyPageData<Map<String, Object>>> listHistoricProcessInstance(
            @MyRequestBody String processDefinitionName,
            @MyRequestBody String beginDate,
            @MyRequestBody String endDate,
            @MyRequestBody(required = true) MyPageParam pageParam) throws ParseException {
        String loginName = TokenData.takeFromRequest().getLoginName();
        MyPageData<HistoricProcessInstance> pageData = flowApiService.getHistoricProcessInstanceList(
                null, processDefinitionName, loginName, beginDate, endDate, pageParam, true);
        Set<String> loginNameSet = pageData.getDataList().stream()
                .map(HistoricProcessInstance::getStartUserId).collect(Collectors.toSet());
        List<FlowUserInfoVo> userInfoList = flowCustomExtFactory
                .getFlowIdentityExtHelper().getUserInfoListByUsernameSet(loginNameSet);
        if (CollUtil.isEmpty(userInfoList)) {
            userInfoList = new LinkedList<>();
        }
        Map<String, FlowUserInfoVo> userInfoMap =
                userInfoList.stream().collect(Collectors.toMap(FlowUserInfoVo::getLoginName, c -> c));
        Set<String> instanceIdSet = pageData.getDataList().stream()
                .map(HistoricProcessInstance::getId).collect(Collectors.toSet());
        List<FlowWorkOrder> workOrderList =
                flowWorkOrderService.getInList(INSTANCE_ID, instanceIdSet);
        Map<String, FlowWorkOrder> workOrderMap =
                workOrderList.stream().collect(Collectors.toMap(FlowWorkOrder::getProcessInstanceId, c -> c));
        List<Map<String, Object>> resultList = new LinkedList<>();
        pageData.getDataList().forEach(instance -> {
            Map<String, Object> data = BeanUtil.beanToMap(instance);
            FlowUserInfoVo userInfo = userInfoMap.get(instance.getStartUserId());
            if (userInfo != null) {
                data.put(SHOW_NAME, userInfo.getShowName());
                data.put("headImageUrl", userInfo.getHeadImageUrl());
            }
            FlowWorkOrder workOrder = workOrderMap.get(instance.getId());
            if (workOrder != null) {
                data.put("workOrderCode", workOrder.getWorkOrderCode());
                data.put("flowStatus", workOrder.getFlowStatus());
            }
            resultList.add(data);
        });
        return ResponseResult.success(MyPageUtil.makeResponseData(resultList, pageData.getTotalCount()));
    }

    /**
     * 根据输入参数查询，所有历史流程数据。
     *
     * @param processDefinitionName 流程名。
     * @param startUser             流程发起用户。
     * @param beginDate             流程发起开始时间。
     * @param endDate               流程发起结束时间。
     * @param pageParam             分页对象。
     * @return 查询结果。
     */
    @PostMapping("/listAllHistoricProcessInstance")
    public ResponseResult<MyPageData<Map<String, Object>>> listAllHistoricProcessInstance(
            @MyRequestBody String processDefinitionName,
            @MyRequestBody String startUser,
            @MyRequestBody String beginDate,
            @MyRequestBody String endDate,
            @MyRequestBody(required = true) MyPageParam pageParam) throws ParseException {
        MyPageData<HistoricProcessInstance> pageData = flowApiService.getHistoricProcessInstanceList(
                null, processDefinitionName, startUser, beginDate, endDate, pageParam, false);
        List<Map<String, Object>> resultList = new LinkedList<>();
        pageData.getDataList().forEach(instance -> resultList.add(BeanUtil.beanToMap(instance)));
        List<String> unfinishedProcessInstanceIds = pageData.getDataList().stream()
                .filter(c -> c.getEndTime() == null)
                .map(HistoricProcessInstance::getId)
                .collect(Collectors.toList());
        MyPageData<Map<String, Object>> pageResultData =
                MyPageUtil.makeResponseData(resultList, pageData.getTotalCount());
        if (CollUtil.isEmpty(unfinishedProcessInstanceIds)) {
            return ResponseResult.success(pageResultData);
        }
        Set<String> processInstanceIds = pageData.getDataList().stream()
                .map(HistoricProcessInstance::getId).collect(Collectors.toSet());
        List<Task> taskList = flowApiService.getTaskListByProcessInstanceIds(unfinishedProcessInstanceIds);
        Map<String, List<Task>> taskMap =
                taskList.stream().collect(Collectors.groupingBy(Task::getProcessInstanceId));
        for (Map<String, Object> result : resultList) {
            String processInstanceId = result.get(INSTANCE_ID).toString();
            List<Task> instanceTaskList = taskMap.get(processInstanceId);
            if (instanceTaskList != null) {
                JSONArray taskArray = new JSONArray();
                for (Task task : instanceTaskList) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("taskId", task.getId());
                    jsonObject.put("taskName", task.getName());
                    jsonObject.put("taskKey", task.getTaskDefinitionKey());
                    jsonObject.put("assignee", task.getAssignee());
                    taskArray.add(jsonObject);
                }
                result.put("runtimeTaskInfoList", taskArray);
            }
        }
        return ResponseResult.success(pageResultData);
    }

    /**
     * 催办工单，只有流程发起人才可以催办工单。
     * 催办场景必须要取消数据权限过滤，因为流程的指派很可能是跨越部门的。
     * 既然被指派和催办了，这里就应该禁用工单表的数据权限过滤约束。
     * 如果您的系统没有支持数据权限过滤，DisableDataFilter不会有任何影响，建议保留。
     *
     * @param workOrderId 工单Id。
     * @return 应答结果。
     */
    @DisableDataFilter
    @OperationLog(type = SysOperationLogType.REMIND_TASK)
    @PostMapping("/remindRuntimeTask")
    public ResponseResult<Void> remindRuntimeTask(@MyRequestBody(required = true) Long workOrderId) {
        FlowWorkOrder flowWorkOrder = flowWorkOrderService.getById(workOrderId);
        if (flowWorkOrder == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        String errorMessage;
        if (!flowWorkOrder.getCreateUserId().equals(TokenData.takeFromRequest().getUserId())) {
            errorMessage = "数据验证失败，只有流程发起人才能催办工单!";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.FINISHED)
                || flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.CANCELLED)
                || flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.STOPPED)) {
            errorMessage = "数据验证失败，已经结束的流程，不能催办工单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.DRAFT)) {
            errorMessage = "数据验证失败，流程草稿不能催办工单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        flowMessageService.saveNewRemindMessage(flowWorkOrder);
        return ResponseResult.success();
    }

    /**
     * 取消工作流工单，仅当没有进入任何审批流程之前，才可以取消工单。
     *
     * @param workOrderId  工单Id。
     * @param cancelReason 取消原因。
     * @return 应答结果。
     */
    @OperationLog(type = SysOperationLogType.CANCEL_FLOW)
    @DisableDataFilter
    @PostMapping("/cancelWorkOrder")
    public ResponseResult<Void> cancelWorkOrder(
            @MyRequestBody(required = true) Long workOrderId,
            @MyRequestBody(required = true) String cancelReason) {
        FlowWorkOrder flowWorkOrder = flowWorkOrderService.getById(workOrderId);
        if (flowWorkOrder == null) {
            return ResponseResult.error(ErrorCodeEnum.DATA_NOT_EXIST);
        }
        String errorMessage;
        if (!flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.SUBMITTED)
                && !flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.DRAFT)) {
            errorMessage = "数据验证失败，当前流程已经进入审批状态，不能撤销工单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        if (!flowWorkOrder.getCreateUserId().equals(TokenData.takeFromRequest().getUserId())) {
            errorMessage = "数据验证失败，当前用户不是工单所有者，不能撤销工单！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        CallResult result;
        // 草稿工单直接删除当前工单。
        if (flowWorkOrder.getFlowStatus().equals(FlowTaskStatus.DRAFT)) {
            result = flowWorkOrderService.removeDraft(flowWorkOrder);
        } else {
            result = flowApiService.stopProcessInstance(
                    flowWorkOrder.getProcessInstanceId(), cancelReason, true);
        }
        if (!result.isSuccess()) {
            return ResponseResult.errorFrom(result);
        }
        return ResponseResult.success();
    }

    /**
     * 获取指定流程定义Id的所有用户任务数据列表。
     *
     * @param processDefinitionId 流程定义Id。
     * @return 查询结果。
     */
    @GetMapping("/listAllUserTask")
    public ResponseResult<List<JSONObject>> listAllUserTask(@RequestParam String processDefinitionId) {
        Map<String, UserTask> taskMap = flowApiService.getAllUserTaskMap(processDefinitionId);
        List<JSONObject> resultList = new LinkedList<>();
        for (UserTask t : taskMap.values()) {
            JSONObject data = new JSONObject();
            data.put("id", t.getId());
            data.put("name", t.getName());
            resultList.add(data);
        }
        return ResponseResult.success(resultList);
    }

    /**
     * 终止流程实例，将任务从当前节点直接流转到主流程的结束事件。
     *
     * @param processInstanceId 流程实例Id。
     * @param stopReason        停止原因。
     * @return 执行结果应答。
     */
    @SaCheckPermission("flowOperation.all")
    @OperationLog(type = SysOperationLogType.STOP_FLOW)
    @DisableDataFilter
    @PostMapping("/stopProcessInstance")
    public ResponseResult<Void> stopProcessInstance(
            @MyRequestBody(required = true) String processInstanceId,
            @MyRequestBody(required = true) String stopReason) {
        CallResult result = flowApiService.stopProcessInstance(processInstanceId, stopReason, false);
        if (!result.isSuccess()) {
            return ResponseResult.errorFrom(result);
        }
        return ResponseResult.success();
    }

    /**
     * 删除流程实例。
     *
     * @param processInstanceId 流程实例Id。
     * @return 执行结果应答。
     */
    @SaCheckPermission("flowOperation.all")
    @OperationLog(type = SysOperationLogType.DELETE_FLOW)
    @PostMapping("/deleteProcessInstance")
    public ResponseResult<Void> deleteProcessInstance(@MyRequestBody(required = true) String processInstanceId) {
        flowApiService.deleteProcessInstance(processInstanceId);
        return ResponseResult.success();
    }

    private List<FlowTaskComment> buildApprovedFlowTaskCommentList(TaskInfo taskInfo, boolean isMultiInstanceTask) {
        List<FlowTaskComment> taskCommentList;
        if (isMultiInstanceTask) {
            String multiInstanceExecId;
            FlowMultiInstanceTrans trans =
                    flowMultiInstanceTransService.getByExecutionId(taskInfo.getExecutionId(), taskInfo.getId());
            if (trans != null) {
                multiInstanceExecId = trans.getMultiInstanceExecId();
            } else {
                multiInstanceExecId = flowApiService.getExecutionVariableStringWithSafe(
                        taskInfo.getExecutionId(), FlowConstant.MULTI_SIGN_TASK_EXECUTION_ID_VAR);
            }
            taskCommentList = flowTaskCommentService.getFlowTaskCommentListByMultiInstanceExecId(multiInstanceExecId);
        } else {
            taskCommentList = flowTaskCommentService.getFlowTaskCommentListByExecutionId(
                    taskInfo.getProcessInstanceId(), taskInfo.getId(), taskInfo.getExecutionId());
        }
        return taskCommentList;
    }

    private ResponseResult<JSONObject> doVerifyMultiSign(String processInstanceId, String taskId) {
        String errorMessage;
        if (!flowApiService.existActiveProcessInstance(processInstanceId)) {
            errorMessage = "数据验证失败，当前流程实例已经结束，不能执行加签！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        HistoricTaskInstance taskInstance = flowApiService.getHistoricTaskInstance(processInstanceId, taskId);
        if (taskInstance == null) {
            errorMessage = "数据验证失败，当前任务不存在！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        String loginName = TokenData.takeFromRequest().getLoginName();
        if (!StrUtil.equals(taskInstance.getAssignee(), loginName)) {
            errorMessage = "数据验证失败，任务指派人与当前用户不匹配！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        List<Task> activeTaskList = flowApiService.getProcessInstanceActiveTaskList(processInstanceId);
        Task activeMultiInstanceTask = null;
        Map<String, UserTask> userTaskMap = flowApiService.getAllUserTaskMap(taskInstance.getProcessDefinitionId());
        for (Task activeTask : activeTaskList) {
            UserTask userTask = userTaskMap.get(activeTask.getTaskDefinitionKey());
            if (!userTask.hasMultiInstanceLoopCharacteristics()) {
                errorMessage = "数据验证失败，指定加签任务不存在或已审批完毕！";
                return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
            }
            String startTaskId = flowApiService.getTaskVariableStringWithSafe(
                    activeTask.getId(), FlowConstant.MULTI_SIGN_START_TASK_VAR);
            if (StrUtil.equals(startTaskId, taskId)) {
                activeMultiInstanceTask = activeTask;
                break;
            }
        }
        if (activeMultiInstanceTask == null) {
            errorMessage = "数据验证失败，指定加签任务不存在或已审批完毕！";
            return ResponseResult.error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorMessage);
        }
        JSONObject resultData = new JSONObject();
        resultData.put("taskInstance", taskInstance);
        resultData.put(ACTIVE_MULTI_INST_TASK, activeMultiInstanceTask);
        return ResponseResult.success(resultData);
    }

    private String findExistAssignee(Set<String> assigneeSet, JSONArray assigneeArray) {
        for (int i = 0; i < assigneeArray.size(); i++) {
            String loginName = assigneeArray.getString(i);
            if (assigneeSet.contains(loginName)) {
                return loginName;
            }
        }
        return null;
    }
}
