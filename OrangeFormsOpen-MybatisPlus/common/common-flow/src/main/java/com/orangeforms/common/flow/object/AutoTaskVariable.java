package com.orangeforms.common.flow.object;

import lombok.Data;

import java.util.List;

/**
 * 自动化任务的变量对象。
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Data
public class AutoTaskVariable {
    /**
     * 流程任务定义。
     */
    private String taskKey;
    /**
     * 流程名称。
     */
    private String taskName;
    /**
     * 执行动作的类型。
     */
    private Integer actionType;
    /**
     * 输出变量名。
     */
    private String outputVariableName;
    /**
     * 输出变量的显示名。
     */
    private String outputVariableShowName;
    /**
     * 选择的字段列表。
     */
    private List<String> fieldList;
    /**
     * HTTP请求的应答体定义。
     */
    private String httpResponseBody;
}
