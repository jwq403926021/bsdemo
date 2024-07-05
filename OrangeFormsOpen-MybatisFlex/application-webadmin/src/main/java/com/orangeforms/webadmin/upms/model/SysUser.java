package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import com.orangeforms.webadmin.upms.model.constant.SysUserType;
import com.orangeforms.webadmin.upms.model.constant.SysUserStatus;
import com.orangeforms.common.core.upload.UploadStoreTypeEnum;
import com.orangeforms.common.core.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.Map;
import java.util.List;

/**
 * 用户管理实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_user")
public class SysUser {

    /**
     * 用户Id。
     */
    @Id(value = "user_id")
    private Long userId;

    /**
     * 登录用户名。
     */
    @Column(value = "login_name")
    private String loginName;

    /**
     * 用户密码。
     */
    private String password;

    /**
     * 用户部门Id。
     */
    @Column(value = "dept_id")
    private Long deptId;

    /**
     * 用户显示名称。
     */
    @Column(value = "show_name")
    private String showName;

    /**
     * 用户类型(0: 管理员 1: 系统管理用户 2: 系统业务用户)。
     */
    @Column(value = "user_type")
    private Integer userType;

    /**
     * 用户头像的Url。
     */
    @UploadFlagColumn(storeType = UploadStoreTypeEnum.LOCAL_SYSTEM)
    @Column(value = "head_image_url")
    private String headImageUrl;

    /**
     * 用户状态(0: 正常 1: 锁定)。
     */
    @Column(value = "user_status")
    private Integer userStatus;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户手机。
     */
    private String mobile;

    /**
     * 创建者Id。
     */
    @Column(value = "create_user_id")
    private Long createUserId;

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
     * 更新时间。
     */
    @Column(value = "update_time")
    private Date updateTime;

    /**
     * 逻辑删除标记字段(1: 正常 -1: 已删除)。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;

    /**
     * createTime 范围过滤起始值(>=)。
     */
    @Column(ignore = true)
    private String createTimeStart;

    /**
     * createTime 范围过滤结束值(<=)。
     */
    @Column(ignore = true)
    private String createTimeEnd;

    /**
     * 多对多用户部门岗位数据集合。
     */
    @RelationManyToMany(
            relationMasterIdField = "userId",
            relationModelClass = SysUserPost.class)
    @Column(ignore = true)
    private List<SysUserPost> sysUserPostList;

    /**
     * 多对多用户角色数据集合。
     */
    @RelationManyToMany(
            relationMasterIdField = "userId",
            relationModelClass = SysUserRole.class)
    @Column(ignore = true)
    private List<SysUserRole> sysUserRoleList;

    /**
     * 多对多用户数据权限数据集合。
     */
    @RelationManyToMany(
            relationMasterIdField = "userId",
            relationModelClass = SysDataPermUser.class)
    @Column(ignore = true)
    private List<SysDataPermUser> sysDataPermUserList;

    @RelationDict(
            masterIdField = "deptId",
            slaveModelClass = SysDept.class,
            slaveIdField = "deptId",
            slaveNameField = "deptName")
    @Column(ignore = true)
    private Map<String, Object> deptIdDictMap;

    @RelationConstDict(
            masterIdField = "userType",
            constantDictClass = SysUserType.class)
    @Column(ignore = true)
    private Map<String, Object> userTypeDictMap;

    @RelationConstDict(
            masterIdField = "userStatus",
            constantDictClass = SysUserStatus.class)
    @Column(ignore = true)
    private Map<String, Object> userStatusDictMap;
}
