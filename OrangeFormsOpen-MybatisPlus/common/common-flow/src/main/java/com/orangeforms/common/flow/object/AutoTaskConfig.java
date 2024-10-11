package com.orangeforms.common.flow.object;

import lombok.Data;

import java.util.List;

/**
 * 自动化任务的执行配置数据。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class AutoTaskConfig {

    public static final String SRC_FILTER_SQL = "sql";
    public static final String SRC_FILTER_FIELD = "field";
    /**
     * 固定值。
     */
    public static final int FIXED_VALUE = 0;
    /**
     * 表字段值。
     */
    public static final int COLUMN_VALUE = 1;
    /**
     * 流程变量。
     */
    public static final int VARIABLE_VALUE = 2;

    /**
     * 任务标识，同时也是当前任务的输出变量名。
     */
    private String taskKey;
    /**
     * 任务名称。
     */
    private String taskName;
    /**
     * 执行动作的类型。
     */
    private Integer actionType;
    /**
     * 数据库链接Id。
     */
    private Long srcDblinkId;
    /**
     * 源数据库类型。
     */
    private Integer srcDblinkType;
    /**
     * 源表名称。
     */
    private String srcTableName;
    /**
     * 源数据的过滤类型，可能值为sql/field。
     */
    private String srcFilterType;
    /**
     * 源数据过滤条件列表。
     */
    private List<FilterInfo> srcFilterList;
    /**
     * 源数据过滤sql。
     */
    private String srcFilterSql;
    /**
     * 目标数据库链接Id。
     */
    private Long destDblinkId;
    /**
     * 目标数据库类型。
     */
    private Integer destDblinkType;
    /**
     * 目标表名称。
     */
    private String destTableName;
    /**
     * 目标数据的过滤类型，可能值为sql/field。
     */
    private String destFilterType;
    /**
     * 目标数据过滤条件列表。
     */
    private List<FilterInfo> destFilterList;
    /**
     * 目标数据过滤sql。
     */
    private String destFilterSql;
    /**
     * 逻辑删除字段。
     */
    private String logicDeleteField;
    /**
     * 数据插入对象
     */
    private List<ValueInfo> insertDataList;
    /**
     * 数据插入对象
     */
    private List<ValueInfo> updateDataList;
    /**
     * SELECT查询的字段。
     */
    private List<String> selectFieldList;
    /**
     * 聚合数据列表。
     */
    private List<AggregationInfo> aggregationDataList;
    /**
     * 数值计算表达式。
     */
    private String calculateFormula;
    /**
     * HTTP请求信息。
     */
    private AutoHttpRequestInfo httpRequestInfo;
    /**
     * HTTP应答数据。
     */
    private AutoHttpResponseData httpResponnseData;

    @Data
    public static class ValueInfo {

        /**
         * HTTP任务中使用。
         */
        private String key;
        /**
         * 目标字段名。
         */
        private String destColumnName;
        /**
         * 值的类型，参考当前对象的常量。
         */
        private Integer type;
        /**
         * 值。
         */
        private String srcValue;
    }

    @Data
    public static class FilterInfo {
        /**
         * 过滤字段名。
         */
        private String filterColumnName;
        /**
         * 过滤类型。
         */
        private Integer filterType;
        /**
         * 过滤值类型。
         */
        private Integer valueType;
        /**
         * 过滤值。
         */
        private String filterValue;
    }

    @Data
    public static class AggregationInfo {
        /**
         * 聚合函数，如SUM/COUNT/AVERAGE/MIN/MAX。
         */
        private String aggregationFunction;
        /**
         * 聚合字段。
         */
        private String aggregationColumn;
        /**
         * 别名。
         */
        private String alias;
    }
}
