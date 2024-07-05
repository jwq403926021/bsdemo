package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 部门关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(value = "zz_sys_dept_relation")
public class SysDeptRelation {

    /**
     * 上级部门Id。
     */
    @Column(value = "parent_dept_id")
    private Long parentDeptId;

    /**
     * 部门Id。
     */
    @Column(value = "dept_id")
    private Long deptId;
}
