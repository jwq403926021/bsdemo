package com.orangeforms.webadmin.upms.model;

import com.baomidou.mybatisplus.annotation.*;
import com.orangeforms.common.core.annotation.RelationManyToMany;
import com.orangeforms.common.core.base.model.BaseModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.*;

/**
 * 角色实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "zz_sys_role")
public class SysRole extends BaseModel {

    /**
     * 角色Id。
     */
    @TableId(value = "role_id")
    private Long roleId;

    /**
     * 角色名称。
     */
    @TableField(value = "role_name")
    private String roleName;

    @RelationManyToMany(
            relationMasterIdField = "roleId",
            relationModelClass = SysRoleMenu.class)
    @TableField(exist = false)
    private List<SysRoleMenu> sysRoleMenuList;
}
