package com.orangeforms.webadmin.upms.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * 用户管理VO视图对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "SysUserVO视图对象")
@Data
public class SysUserVo {

    /**
     * 用户Id。
     */
    @Schema(description = "用户Id")
    private Long userId;

    /**
     * 登录用户名。
     */
    @Schema(description = "登录用户名")
    private String loginName;

    /**
     * 用户部门Id。
     */
    @Schema(description = "用户部门Id")
    private Long deptId;

    /**
     * 用户显示名称。
     */
    @Schema(description = "用户显示名称")
    private String showName;

    /**
     * 用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。
     */
    @Schema(description = "用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)")
    private Integer userType;

    /**
     * 用户头像的Url。
     */
    @Schema(description = "用户头像的Url")
    private String headImageUrl;

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

    /**
     * 多对多用户岗位数据集合。
     */
    @Schema(description = "多对多用户岗位数据集合")
    private List<Map<String, Object>> sysUserPostList;

    /**
     * 多对多用户角色数据集合。
     */
    @Schema(description = "多对多用户角色数据集合")
    private List<Map<String, Object>> sysUserRoleList;

    /**
     * 多对多用户数据权限数据集合。
     */
    @Schema(description = "多对多用户数据权限数据集合")
    private List<Map<String, Object>> sysDataPermUserList;

    /**
     * deptId 字典关联数据。
     */
    @Schema(description = "deptId 字典关联数据")
    private Map<String, Object> deptIdDictMap;

    /**
     * userType 常量字典关联数据。
     */
    @Schema(description = "userType 常量字典关联数据")
    private Map<String, Object> userTypeDictMap;

    /**
     * userStatus 常量字典关联数据。
     */
    @Schema(description = "userStatus 常量字典关联数据")
    private Map<String, Object> userStatusDictMap;
}
