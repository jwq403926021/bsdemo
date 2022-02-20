package com.orangeforms.common.core.annotation;

import java.lang.annotation.*;

/**
 * 主要用于标记通过租户Id进行过滤的字段。
 *
 * @author Jerry
 * @date 2022-02-20
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TenantFilterColumn {

}
