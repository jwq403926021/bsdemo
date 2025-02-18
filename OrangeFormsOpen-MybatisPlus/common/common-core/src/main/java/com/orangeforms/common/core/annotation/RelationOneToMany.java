package com.orangeforms.common.core.annotation;

import com.orangeforms.common.core.object.DummyClass;

import java.lang.annotation.*;

/**
 * 标识Model之间的一对多关联关系。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RelationOneToMany {

    /**
     * 当前对象的关联Id字段名称。
     *
     * @return 当前对象的关联Id字段名称。
     */
    String masterIdField();

    /**
     * 被关联Model对象的Class对象。
     *
     * @return 被关联Model对象的Class对象。
     */
    Class<?> slaveModelClass();

    /**
     * 被关联Model对象的关联Id字段名称。
     *
     * @return 被关联Model对象的关联Id字段名称。
     */
    String slaveIdField();

    /**
     * 被关联的本地Service对象名称。
     * 该参数的优先级低于 slaveServiceClass()，
     * 如果是空字符串，BaseService会自动拼接为 slaveModelClass().getSimpleName() + "Service"。
     *
     * @return 被关联的本地Service对象名称。
     */
    String slaveServiceName() default "";

    /**
     * 被关联的本地Service对象CLass类型。
     *
     * @return 被关联的本地Service对象CLass类型。
     */
    Class<?> slaveServiceClass() default DummyClass.class;
}
