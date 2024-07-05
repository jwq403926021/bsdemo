package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 角色菜单实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_role_menu")
public class SysRoleMenu {

    /**
     * 角色Id。
     */
    @Column(value = "role_id")
    private Long roleId;

    /**
     * 菜单Id。
     */
    @Column(value = "menu_id")
    private Long menuId;
}
