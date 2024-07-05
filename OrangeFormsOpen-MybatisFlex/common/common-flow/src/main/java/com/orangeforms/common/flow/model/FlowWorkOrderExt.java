package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 工作流工单扩展数据实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_work_order_ext")
public class FlowWorkOrderExt {

    /**
     * 主键Id。
     */
    @Id(value = "id")
    private Long id;

    /**
     * 流程工单Id。
     */
    @Column(value = "work_order_id")
    private Long workOrderId;

    /**
     * 草稿数据。
     */
    @Column(value = "draft_data")
    private String draftData;

    /**
     * 业务数据。
     */
    @Column(value = "business_data")
    private String businessData;

    /**
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 更新者Id。
     */
    @Column(value = "update_user_id")
    private Long updateUserId;

    /**
     * 创建时间。
     */
    @Column(value = "create_time")
    private Date createTime;

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

    /**
     * 逻辑删除标记字段(1: 正常 -1: 已删除)。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;
}
