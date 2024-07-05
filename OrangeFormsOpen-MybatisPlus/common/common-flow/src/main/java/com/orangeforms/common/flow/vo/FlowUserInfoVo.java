package com.orangeforms.common.flow.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 流程任务的用户信息。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "流程任务的用户信息")
@Data
public class FlowUserInfoVo {

    /**
     * 用户Id。
     */
    @Schema(description = "用户Id")
    private Long userId;

    /**
     * 用户部门Id。
     */
    @Schema(description = "用户部门Id")
    private Long deptId;

    /**
     * 登录用户名。
     */
    @Schema(description = "登录用户名")
    private String loginName;

    /**
     * 用户显示名称。
     */
    @Schema(description = "用户显示名称")
    private String showName;

    /**
     * 用户头像的Url。
     */
    @Schema(description = "用户头像的Url")
    private String headImageUrl;

    /**
     * 用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。
     */
    @Schema(description = "用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)")
    private Integer userType;

    /**
     * 用户状态(0: 正常 1: 锁定)。
     */
    @Schema(description = "用户状态(0: 正常 1: 锁定)")
    private Integer userStatus;

    /**
     * 用户邮箱。
     */
    @Schema(description = "用户邮箱")
    private String email;

    /**
     * 用户手机。
     */
    @Schema(description = "用户手机")
    private String mobile;

    /**
     * 最后审批时间。
     */
    @Schema(description = "最后审批时间")
    private Date lastApprovalTime;
}
