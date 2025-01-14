package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 部门岗位多对多关联实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_dept_post")
public class SysDeptPost {

    /**
     * 部门岗位Id。
     */
    @Id(value = "dept_post_id")
    private Long deptPostId;

    /**
     * 部门Id。
     */
    @Column(value = "dept_id")
    private Long deptId;

    /**
     * 岗位Id。
     */
    @Column(value = "post_id")
    private Long postId;

    /**
     * 部门岗位显示名称。
     */
    @Column(value = "post_show_name")
    private String postShowName;
}
