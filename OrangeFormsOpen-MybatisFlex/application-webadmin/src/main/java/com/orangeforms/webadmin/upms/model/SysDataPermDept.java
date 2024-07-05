package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;
import lombok.ToString;

/**
 * 数据权限与部门关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@ToString(of = {"deptId"})
@Table(value = "zz_sys_data_perm_dept")
public class SysDataPermDept {

    /**
     * 数据权限Id。
     */
    @Column(value = "data_perm_id")
    private Long dataPermId;

    /**
     * 关联部门Id。
     */
    @Column(value = "dept_id")
    private Long deptId;
}
