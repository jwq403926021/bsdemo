package com.orangeforms.webadmin.upms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 部门管理VO视图对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "SysDeptVO视图对象")
@Data
public class SysDeptVo {

    /**
     * 部门Id。
     */
    @Schema(description = "部门Id")
    private Long deptId;

    /**
     * 部门名称。
     */
    @Schema(description = "部门名称")
    private String deptName;

    /**
     * 显示顺序。
     */
    @Schema(description = "显示顺序")
    private Integer showOrder;

    /**
     * 父部门Id。
     */
    @Schema(description = "父部门Id")
    private Long parentId;

    /**
     * 创建者Id。
     */
    @Schema(description = "创建者Id")
    private Long createUserId;

    /**
     * 更新者Id。
     */
    @Schema(description = "更新者Id")
    private Long updateUserId;

    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private Date createTime;

    /**
     * 更新时间。
     */
    @Schema(description = "更新时间")
    private Date updateTime;
}
