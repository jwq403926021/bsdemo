package com.orangeforms.common.core.annotation;

import java.lang.annotation.*;

/**
 * 主要用于标记Job实体对象的更新时间字段。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JobUpdateTimeColumn {

}
