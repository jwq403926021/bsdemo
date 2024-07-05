package com.orangeforms.common.flow.listener;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.orangeforms.common.core.object.TokenData;
import com.orangeforms.common.core.util.ApplicationContextHolder;
import com.orangeforms.common.flow.constant.FlowApprovalType;
import com.orangeforms.common.flow.constant.FlowConstant;
import com.orangeforms.common.flow.model.FlowTaskComment;
import com.orangeforms.common.flow.model.FlowTaskExt;
import com.orangeforms.common.flow.object.FlowTaskOperation;
import com.orangeforms.common.flow.service.FlowApiService;
import com.orangeforms.common.flow.service.FlowTaskCommentService;
import com.orangeforms.common.flow.service.FlowTaskExtService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.bpmn.model.ExtensionAttribute;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.api.Task;
import org.flowable.task.service.delegate.DelegateTask;

import java.util.*;

/**
 * 流程任务自动审批跳过的监听器。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
public class AutoSkipTaskListener implements TaskListener {

    private final transient FlowTaskCommentService flowTaskCommentService =
            ApplicationContextHolder.getBean(FlowTaskCommentService.class);
    private final transient FlowApiService flowApiService =
            ApplicationContextHolder.getBean(FlowApiService.class);
    private final transient FlowTaskExtService flowTaskExtService =
            ApplicationContextHolder.getBean(FlowTaskExtService.class);

    /**
     * 流程的发起者等于当前任务的Assignee。
     */
    private static final String EQ_START_USER = "0";
    /**
     * 上一步的提交者等于当前任务的Assignee。
     */
    private static final String EQ_PREV_SUBMIT_USER = "1";
    /**
     * 当前任务的Assignee之前提交过审核。
     */
    private static final String EQ_HISTORIC_SUBMIT_USER = "2";

    @Override
    public void notify(DelegateTask t) {
        UserTask userTask = flowApiService.getUserTask(t.getProcessDefinitionId(), t.getTaskDefinitionKey());
        List<ExtensionAttribute> attributes = userTask.getAttributes().get(FlowConstant.USER_TASK_AUTO_SKIP_KEY);
        Set<String> skipTypes = new HashSet<>(StrUtil.split(attributes.get(0).getValue(), ","));
        String assignedUser = this.getAssignedUser(userTask, t.getProcessDefinitionId(), t.getExecutionId());
        if (StrUtil.isBlank(assignedUser)) {
            return;
        }
        for (String skipType : skipTypes) {
            if (this.verifyAndHandle(userTask, t, skipType, assignedUser)) {
                return;
            }
        }
    }

    private boolean verifyAndHandle(UserTask userTask, DelegateTask task, String skipType, String assignedUser) {
        FlowTaskComment comment = null;
        switch (skipType) {
            case EQ_START_USER:
                Object v = task.getVariable(FlowConstant.PROC_INSTANCE_START_USER_NAME_VAR);
                if (ObjectUtil.equal(v, assignedUser)) {
                    comment = flowTaskCommentService.getFirstFlowTaskComment(task.getProcessInstanceId());
                }
                break;
            case EQ_PREV_SUBMIT_USER:
                Object v2 = task.getVariable(FlowConstant.SUBMIT_USER_VAR);
                if (ObjectUtil.equal(v2, assignedUser)) {
                    TokenData tokenData = TokenData.takeFromRequest();
                    comment = new FlowTaskComment();
                    comment.setCreateUserId(tokenData.getUserId());
                    comment.setCreateLoginName(tokenData.getLoginName());
                    comment.setCreateUsername(tokenData.getShowName());
                }
                break;
            case EQ_HISTORIC_SUBMIT_USER:
                List<FlowTaskComment> comments =
                        flowTaskCommentService.getFlowTaskCommentList(task.getProcessInstanceId());
                List<FlowTaskComment> resultComments = new LinkedList<>();
                for (FlowTaskComment c : comments) {
                    if (StrUtil.equals(c.getCreateLoginName(), assignedUser)) {
                        resultComments.add(c);
                    }
                }
                if (CollUtil.isNotEmpty(resultComments)) {
                    comment = resultComments.get(0);
                }
                break;
            default:
                break;
        }
        if (comment != null) {
            FlowTaskExt flowTaskExt = flowTaskExtService
                    .getByProcessDefinitionIdAndTaskId(task.getProcessDefinitionId(), userTask.getId());
            JSONObject taskVariableData = new JSONObject();
            if (StrUtil.isNotBlank(flowTaskExt.getOperationListJson())) {
                List<FlowTaskOperation> taskOperationList =
                        JSONArray.parseArray(flowTaskExt.getOperationListJson(), FlowTaskOperation.class);
                taskOperationList.stream()
                        .filter(op -> op.getType().equals(FlowApprovalType.AGREE))
                        .map(FlowTaskOperation::getLatestApprovalStatus).findFirst()
                        .ifPresent(status -> taskVariableData.put(FlowConstant.LATEST_APPROVAL_STATUS_KEY, status));
            }
            Task t = flowApiService.getTaskById(task.getId());
            comment.fillWith(t);
            comment.setApprovalType(FlowApprovalType.AGREE);
            comment.setTaskComment(StrFormatter.format("自动跳过审批。审批人 [{}], 跳过原因 [{}]。",
                    userTask.getAssignee(), this.getMessageBySkipType(skipType)));
            flowApiService.completeTask(t, comment, taskVariableData);
        }
        return comment != null;
    }

    private String getAssignedUser(UserTask userTask, String processDefinitionId, String executionId) {
        String assignedUser = userTask.getAssignee();
        if (StrUtil.isNotBlank(assignedUser)) {
            if (assignedUser.startsWith("${") && assignedUser.endsWith("}")) {
                String variableName = assignedUser.substring(2, assignedUser.length() - 1);
                assignedUser = flowApiService.getExecutionVariableStringWithSafe(executionId, variableName);
            }
        } else {
            FlowTaskExt flowTaskExt = flowTaskExtService
                    .getByProcessDefinitionIdAndTaskId(processDefinitionId, userTask.getId());
            List<String> candidateUsernames;
            if (StrUtil.isBlank(flowTaskExt.getCandidateUsernames())) {
                candidateUsernames = Collections.emptyList();
            } else if (!StrUtil.equals(flowTaskExt.getCandidateUsernames(), "${" + FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR + "}")) {
                candidateUsernames = StrUtil.split(flowTaskExt.getCandidateUsernames(), ",");
            } else {
                String value = flowApiService
                        .getExecutionVariableStringWithSafe(executionId, FlowConstant.TASK_APPOINTED_ASSIGNEE_VAR);
                candidateUsernames = value == null ? null : StrUtil.split(value, ",");
            }
            if (candidateUsernames != null && candidateUsernames.size() == 1) {
                assignedUser = candidateUsernames.get(0);
            }
        }
        return assignedUser;
    }

    private String getMessageBySkipType(String skipType) {
        return switch (skipType) {
            case EQ_PREV_SUBMIT_USER -> "审批人与上一审批节点处理人相同";
            case EQ_START_USER -> "审批人为发起人";
            case EQ_HISTORIC_SUBMIT_USER -> "审批人审批过";
            default -> "";
        };
    }
}
