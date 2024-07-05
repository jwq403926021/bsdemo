package com.orangeforms.common.core.annotation;

import java.lang.annotation.*;

/**
 * 业务表中记录流程实例结束标记的字段。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface FlowStatusColumn {

}
