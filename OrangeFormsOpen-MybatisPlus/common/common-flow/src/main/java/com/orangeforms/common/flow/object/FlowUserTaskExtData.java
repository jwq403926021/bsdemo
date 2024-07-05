package com.orangeforms.common.flow.object;

import lombok.Data;

import java.util.List;

/**
 * 流程用户任务扩展数据对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class FlowUserTaskExtData {

    public static final String NOTIFY_TYPE_MSG = "message";
    public static final String NOTIFY_TYPE_EMAIL = "email";

    public static final String TIMEOUT_AUTO_COMPLETE = "autoComplete";
    public static final String TIMEOUT_SEND_MSG = "sendMessage";

    public static final String EMPTY_USER_TO_ASSIGNEE = "toAssignee";
    public static final String EMPTY_USER_AUTO_REJECT = "autoReject";
    public static final String EMPTY_USER_AUTO_COMPLETE = "autoComplete";

    /**
     * 拒绝后再提交，走重新审批。
     */
    public static final String REJECT_TYPE_REDO = "0";
    /**
     * 拒绝后再提交，直接回到驳回前的节点。
     */
    public static final String REJECT_TYPE_BACK_TO_SOURCE = "1";

    /**
     * 任务通知类型列表。
     */
    private List<String> flowNotifyTypeList;
    /**
     * 拒绝后再次提交的审批类型。
     */
    private String rejectType = REJECT_TYPE_REDO;
    /**
     * 到期提醒的小时数(从待办任务被创建的时候开始计算)。
     */
    private Integer timeoutHours;
    /**
     * 任务超时的处理方式。
     */
    private String timeoutHandleWay;
    /**
     * 默认审批人。
     */
    private String defaultAssignee;
    /**
     * 空用户审批处理方式。
     */
    private String emptyUserHandleWay;
    /**
     * 空用户审批时设定的审批人。
     */
    private String emptyUserToAssignee;
}
