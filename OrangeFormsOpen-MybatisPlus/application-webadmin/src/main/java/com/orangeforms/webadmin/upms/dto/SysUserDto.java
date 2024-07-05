package com.orangeforms.webadmin.upms.dto;

import com.orangeforms.common.core.validator.AddGroup;
import com.orangeforms.common.core.validator.UpdateGroup;
import com.orangeforms.common.core.validator.ConstDictRef;
import com.orangeforms.webadmin.upms.model.constant.SysUserType;
import com.orangeforms.webadmin.upms.model.constant.SysUserStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 用户管理Dto对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Schema(description = "SysUserDto对象")
@Data
public class SysUserDto {

    /**
     * 用户Id。
     */
    @Schema(description = "用户Id。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据验证失败，用户Id不能为空！", groups = {UpdateGroup.class})
    private Long userId;

    /**
     * 登录用户名。
     * NOTE: 可支持等于操作符的列表数据过滤。
     */
    @Schema(description = "登录用户名。可支持等于操作符的列表数据过滤。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据验证失败，登录用户名不能为空！")
    private String loginName;

    /**
     * 用户密码。
     */
    @Schema(description = "用户密码。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据验证失败，用户密码不能为空！", groups = {AddGroup.class})
    private String password;

    /**
     * 用户部门Id。
     * NOTE: 可支持等于操作符的列表数据过滤。
     */
    @Schema(description = "用户部门Id。可支持等于操作符的列表数据过滤。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据验证失败，用户部门Id不能为空！")
    private Long deptId;

    /**
     * 用户显示名称。
     * NOTE: 可支持等于操作符的列表数据过滤。
     */
    @Schema(description = "用户显示名称。可支持等于操作符的列表数据过滤。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "数据验证失败，用户显示名称不能为空！")
    private String showName;

    /**
     * 用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。
     */
    @Schema(description = "用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据验证失败，用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)不能为空！")
    @ConstDictRef(constDictClass = SysUserType.class, message = "数据验证失败，用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)为无效值！")
    private Integer userType;

    /**
     * 用户头像的Url。
     */
    @Schema(description = "用户头像的Url。")
    private String headImageUrl;

    /**
     * 用户状态(0: 正常 1: 锁定)。
     * NOTE: 可支持等于操作符的列表数据过滤。
     */
    @Schema(description = "用户状态(0: 正常 1: 锁定)。可支持等于操作符的列表数据过滤。", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "数据验证失败，用户状态(0: 正常 1: 锁定)不能为空！")
    @ConstDictRef(constDictClass = SysUserStatus.class, message = "数据验证失败，用户状态(0: 正常 1: 锁定)为无效值！")
    private Integer userStatus;

    /**
     * 用户邮箱。
     */
    @Schema(description = "用户邮箱。")
    private String email;

    /**
     * 用户手机。
     */
    @Schema(description = "用户手机。")
    private String mobile;

    /**
     * createTime 范围过滤起始值(>=)。
     * NOTE: 可支持范围操作符的列表数据过滤。
     */
    @Schema(description = "createTime 范围过滤起始值(>=)。可支持范围操作符的列表数据过滤。")
    private String createTimeStart;

    /**
     * createTime 范围过滤结束值(<=)。
     * NOTE: 可支持范围操作符的列表数据过滤。
     */
    @Schema(description = "createTime 范围过滤结束值(<=)。可支持范围操作符的列表数据过滤。")
    private String createTimeEnd;
}
