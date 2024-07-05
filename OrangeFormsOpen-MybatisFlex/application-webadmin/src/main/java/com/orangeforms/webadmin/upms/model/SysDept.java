package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.Table;
import lombok.Data;

import java.util.Date;

/**
 * 部门管理实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_dept")
public class SysDept {

    /**
     * 部门Id。
     */
    @Id(value = "dept_id")
    private Long deptId;

    /**
     * 部门名称。
     */
    @Column(value = "dept_name")
    private String deptName;

    /**
     * 显示顺序。
     */
    @Column(value = "show_order")
    private Integer showOrder;

    /**
     * 父部门Id。
     */
    @Column(value = "parent_id")
    private Long parentId;

    /**
     * 逻辑删除标记字段(1: 正常 -1: 已删除)。
     */
    @Column(value = "deleted_flag", isLogicDelete = true)
    private Integer deletedFlag;

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
}
