package com.orangeforms.common.flow.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * 工作流自动化任务的动作类型。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public class FlowAutoActionType {

    /**
     * 添加新数据。
     */
    public static final int ADD_NEW = 0;
    /**
     * 更新数据。
     */
    public static final int UPDATE = 1;
    /**
     * 删除数据。
     */
    public static final int DELETE = 2;
    /**
     * 查询单条数据。
     */
    public static final int SELECT_ONE = 3;
    /**
     * 聚合计算。
     */
    public static final int AGGREGATION_CALC = 5;
    /**
     * 数值计算。
     */
    public static final int NUMBER_CALC = 6;
    /**
     * HTTP请求调用
     */
    public static final int HTTP = 10;

    private static final Map<Integer, String> DICT_MAP = new HashMap<>(2);
    static {
        DICT_MAP.put(ADD_NEW, "添加新数据");
        DICT_MAP.put(UPDATE, "更新数据");
        DICT_MAP.put(DELETE, "删除数据");
        DICT_MAP.put(SELECT_ONE, "查询单条数据");
        DICT_MAP.put(AGGREGATION_CALC, "聚合计算");
        DICT_MAP.put(NUMBER_CALC, "数值计算");
        DICT_MAP.put(HTTP, "HTTP请求调用");
    }

    /**
     * 根据类型值返回显示值。
     *
     * @param flowActionType 类型值。
     * @return 对应的显示名。
     */
    public static String getShowNname(int flowActionType) {
        return DICT_MAP.get(flowActionType);
    }

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private FlowAutoActionType() {
    }
}
