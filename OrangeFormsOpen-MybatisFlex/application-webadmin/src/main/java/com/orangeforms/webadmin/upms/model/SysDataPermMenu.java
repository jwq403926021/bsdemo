package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;
import lombok.ToString;

/**
 * 数据权限与菜单关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@ToString(of = {"menuId"})
@Table(value = "zz_sys_data_perm_menu")
public class SysDataPermMenu {

    /**
     * 数据权限Id。
     */
    @Column(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 关联菜单Id。
     */
    @Column(value = "menu_id")
    private Long menuId;
}
