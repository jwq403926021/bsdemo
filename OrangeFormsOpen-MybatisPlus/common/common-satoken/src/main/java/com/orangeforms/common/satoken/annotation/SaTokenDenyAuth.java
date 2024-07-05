package com.orangeforms.common.satoken.annotation;

import java.lang.annotation.*;

/**
 * 所有标记该注解的接口，不能使用SaToken进行权限验证。
 * 必须通过橙单自身的动态验证完成，即基于URL的验证。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SaTokenDenyAuth {
}
