package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程任务消息所属用户的操作表。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_msg_identity_operation")
public class FlowMessageIdentityOperation {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 任务消息Id。
     */
    @Column(value = "message_id")
    private Long messageId;

    /**
     * 用户登录名。
     */
    @Column(value = "login_name")
    private String loginName;

    /**
     * 操作类型。
     * 常量值参考FlowMessageOperationType对象。
     */
    @Column(value = "operation_type")
    private Integer operationType;

    /**
     * 操作时间。
     */
    @Column(value = "operation_time")
    private Date operationTime;
}
