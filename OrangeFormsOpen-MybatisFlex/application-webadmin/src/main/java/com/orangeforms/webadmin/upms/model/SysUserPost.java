package com.orangeforms.webadmin.upms.model;

import com.mybatisflex.annotation.*;
import lombok.Data;

/**
 * 用户岗位多对多关系实体对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@Table(value = "zz_sys_user_post")
public class SysUserPost {

    /**
     * 用户Id。
     */
    @Column(value = "user_id")
    private Long userId;

    /**
     * 部门岗位Id。
     */
    @Column(value = "dept_post_id")
    private Long deptPostId;

    /**
     * 岗位Id。
     */
    @Column(value = "post_id")
    private Long postId;
}
