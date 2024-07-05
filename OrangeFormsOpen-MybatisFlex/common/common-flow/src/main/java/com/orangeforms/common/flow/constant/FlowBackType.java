package com.orangeforms.common.flow.constant;

/**
 * 待办任务回退类型。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public final class FlowBackType {

    /**
     * 驳回。
     */
    public static final int REJECT = 0;
    /**
     * 撤回。
     */
    public static final int REVOKE = 1;

    /**
     * 私有构造函数，明确标识该常量类的作用。
     */
    private FlowBackType() {
    }
}
