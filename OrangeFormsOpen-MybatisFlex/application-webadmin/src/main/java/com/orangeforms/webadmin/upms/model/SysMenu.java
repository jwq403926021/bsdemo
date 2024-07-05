package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import com.orangeforms.common.core.base.model.BaseModel;
import com.orangeforms.webadmin.upms.bo.SysMenuExtraData;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 菜单实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Table(value = "zz_sys_menu")
public class SysMenu extends BaseModel {

    /**
     * 菜单Id。
     */
    @Id(value = "menu_id")
    private Long menuId;

    /**
     * 父菜单Id，目录菜单的父菜单为null。
     */
    @Column(value = "parent_id")
    private Long parentId;

    /**
     * 菜单显示名称。
     */
    @Column(value = "menu_name")
    private String menuName;

    /**
     * 菜单类型(0: 目录 1: 菜单 2: 按钮 3: UI片段)。
     */
    @Column(value = "menu_type")
    private Integer menuType;

    /**
     * 前端表单路由名称，仅用于menu_type为1的菜单类型。
     */
    @Column(value = "form_router_name")
    private String formRouterName;

    /**
     * 在线表单主键Id，仅用于在线表单绑定的菜单。
     */
    @Column(value = "online_form_id")
    private Long onlineFormId;

    /**
     * 在线表单菜单的权限控制类型，具体值可参考SysOnlineMenuPermType常量对象。
     */
    @Column(value = "online_menu_perm_type")
    private Integer onlineMenuPermType;

    /**
     * 统计页面主键Id，仅用于统计页面绑定的菜单。
     */
    @Column(value = "report_page_id")
    private Long reportPageId;

    /**
     * 仅用于在线表单的流程Id。
     */
    @Column(value = "online_flow_entry_id")
    private Long onlineFlowEntryId;

    /**
     * 菜单显示顺序 (值越小，排序越靠前)。
     */
    @Column(value = "show_order")
    private Integer showOrder;

    /**
     * 菜单图标。
     */
    private String icon;

    /**
     * 附加信息。
     */
    @Column(value = "extra_data")
    private String extraData;

    /**
     * extraData字段解析后的对象数据。
     */
    @Column(ignore = true)
    private SysMenuExtraData extraObject;
}
