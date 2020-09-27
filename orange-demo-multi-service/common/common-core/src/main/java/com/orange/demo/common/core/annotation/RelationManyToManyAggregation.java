package com.orange.demo.common.core.annotation;

import com.orange.demo.common.core.object.DummyClass;

import java.lang.annotation.*;

/**
 * 主要用于多对多的Model关系。标注通过从表关联字段或者关联表关联字段计算主表聚合计算字段的规则。
 *
 * @author Jerry
 * @date 2020-09-27
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RelationManyToManyAggregation {

    /**
     * 当前对象的关联Id字段名称。
     *
     * @return 当前对象的关联Id字段名称。
     */
    String masterIdField();

    /**
     * 被关联的本地Service对象名称。
     *
     * @return 被关联的本地Service对象名称。
     */
    String slaveServiceName() default "";

    /**
     * 多对多从表Model对象的Class对象。
     *
     * @return 被关联Model对象的Class对象。
     */
    Class<?> slaveModelClass();

    /**
     * 多对多从表Model对象的关联Id字段名称。
     *
     * @return 被关联Model对象的关联Id字段名称。
     */
    String slaveIdField();

    /**
     * 被关联远程调用对象的Class对象。如果为DummyClass.class，通常表示是本地关联。
     *
     * @return 被关联远程调用对象的Class对象。
     */
    Class<?> slaveClientClass() default DummyClass.class;

    /**
     * 多对多关联表Model对象的Class对象。
     *
     * @return 被关联Model对象的Class对象。
     */
    Class<?> relationModelClass();

    /**
     * 多对多关联表Model对象中与主表关联的Id字段名称。
     *
     * @return 被关联Model对象的关联Id字段名称。
     */
    String relationMasterIdField();

    /**
     * 多对多关联表Model对象中与从表关联的Id字段名称。
     *
     * @return 被关联Model对象的关联Id字段名称。
     */
    String relationSlaveIdField();

    /**
     * 聚合计算所在的Model。
     *
     * @return 聚合计算所在Model的Class。
     */
    Class<?> aggregationModelClass();

    /**
     * 聚合类型。具体数值参考AggregationType对象。
     *
     * @return 聚合类型。
     */
    int aggregationType();

    /**
     * 聚合计算所在Model的字段名称。
     *
     * @return 聚合计算所在Model的字段名称。
     */
    String aggregationField();
}
