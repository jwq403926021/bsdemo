package com.orangeforms.webadmin.upms.dao;

import com.orangeforms.common.core.base.dao.BaseDaoMapper;
import com.orangeforms.webadmin.upms.model.SysRole;
import org.apache.ibatis.annotations.Param;

import java.util.*;

/**
 * 角色数据访问操作接口。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public interface SysRoleMapper extends BaseDaoMapper<SysRole> {

    /**
     * 获取对象列表，过滤条件中包含like和between条件。
     *
     * @param sysRoleFilter 过滤对象。
     * @param orderBy       排序字符串，order by从句的参数。
     * @return 对象列表。
     */
    List<SysRole> getSysRoleList(@Param("sysRoleFilter") SysRole sysRoleFilter, @Param("orderBy") String orderBy);
}
