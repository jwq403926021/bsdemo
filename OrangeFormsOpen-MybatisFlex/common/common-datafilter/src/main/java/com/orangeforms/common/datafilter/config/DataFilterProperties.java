package com.orangeforms.common.datafilter.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * common-datafilter模块的配置类。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
@ConfigurationProperties(prefix = "common-datafilter")
public class DataFilterProperties {

    /**
     * 是否启用租户过滤。
     */
    @Value("${common-datafilter.tenant.enabled:false}")
    private Boolean enabledTenantFilter;

    /**
     * 是否启动数据权限过滤。
     */
    @Value("${common-datafilter.dataperm.enabled:false}")
    private Boolean enabledDataPermFilter;

    /**
     * 部门关联表的表名前缀，如zz_。该值主要用在MybatisDataFilterInterceptor拦截器中，
     * 用于拼接数据权限过滤的SQL语句。
     */
    @Value("${common-datafilter.dataperm.deptRelationTablePrefix:}")
    private String deptRelationTablePrefix;

    /**
     * 该值为true的时候，在进行数据权限过滤时，会加上表名，如：zz_sys_user.dept_id = xxx。
     * 为false时，过滤条件不加表名，只是使用字段名，如：dept_id = xxx。该值目前主要适用于
     * Oracle分页SQL使用了子查询的场景。此场景下，由于子查询使用了别名，再在数据权限过滤条件中
     * 加上原有表名时，SQL语法会报错。
     */
    @Value("${common-datafilter.dataperm.addTableNamePrefix:true}")
    private Boolean addTableNamePrefix;

    /**
     * 是否打开menuId和当前url的匹配关系的验证。
     */
    @Value("${common-datafilter.dataperm.enableMenuPermVerify:true}")
    private Boolean enableMenuPermVerify;
}
