package com.orangeforms.common.core.annotation;

import java.lang.annotation.*;

/**
 * 作为DisableDataFilterAspect的切点。
 * 该注解标记的方法内所有的查询语句，均不会被Mybatis拦截器过滤数据。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DisableDataFilter {

}
