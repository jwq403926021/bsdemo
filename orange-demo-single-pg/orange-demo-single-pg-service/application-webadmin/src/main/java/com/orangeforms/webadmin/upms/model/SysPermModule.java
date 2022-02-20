package com.orangeforms.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.*;

/**
 * 权限模块实体对象。
 *
 * @author Jerry
 * @date 2022-02-20
 */
@Data
@TableName(value = "zz_sys_perm_module")
public class SysPermModule {

    /**
     * 权限模块Id。
     */
    @TableId(value = "module_id")
    private Long moduleId;

    /**
     * 上级权限模块Id。
     */
    @TableField(value = "parent_id")
    private Long parentId;

    /**
     * 权限模块名称。
     */
    @TableField(value = "module_name")
    private String moduleName;

    /**
     * 权限模块类型(0: 普通模块 1: Controller模块)。
     */
    @TableField(value = "module_type")
    private Integer moduleType;

    /**
     * 权限模块在当前层级下的顺序，由小到大。
     */
    @TableField(value = "show_order")
    private Integer showOrder;

    /**
     * 创建者Id。
     */
    @TableField(value = "create_user_id")
    private Long createUserId;

    /**
     * 创建时间。
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 更新者Id。
     */
    @TableField(value = "update_user_id")
    private Long updateUserId;

    /**
     * 更新时间。
     */
    @TableField(value = "update_time")
    private Date updateTime;

    @TableField(exist = false)
    private List<SysPerm> sysPermList;
}
