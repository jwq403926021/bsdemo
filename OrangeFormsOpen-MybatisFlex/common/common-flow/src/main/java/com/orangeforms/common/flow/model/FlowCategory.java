package com.orangeforms.common.flow.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * 流程分类的实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_flow_category")
public class FlowCategory {

    /**
     * 主键Id。
     */
    @Id(value = "category_id")
    private Long categoryId;

    /**
     * 租户Id。
     */
    @Column(value = "tenant_id")
    private Long tenantId;

    /**
     * 应用编码。为空时，表示非第三方应用接入。
     */
    @Column(value = "app_code")
    private String appCode;

    /**
     * 显示名称。
     */
    @Column(value = "name")
    private String name;

    /**
     * 分类编码。
     */
    @Column(value = "code")
    private String code;

    /**
     * 实现顺序。
     */
    @Column(value = "show_order")
    private Integer showOrder;

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
}
