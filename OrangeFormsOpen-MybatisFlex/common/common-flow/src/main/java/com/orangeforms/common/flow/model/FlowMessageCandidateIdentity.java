package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 流程任务消息的候选身份实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_msg_candidate_identity")
public class FlowMessageCandidateIdentity {

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
     * 候选身份类型。
     */
    @Column(value = "candidate_type")
    private String candidateType;

    /**
     * 候选身份Id。
     */
    @Column(value = "candidate_id")
    private String candidateId;
}
