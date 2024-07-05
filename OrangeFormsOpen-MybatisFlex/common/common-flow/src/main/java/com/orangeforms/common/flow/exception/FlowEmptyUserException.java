package com.orangeforms.common.flow.exception;

import org.flowable.common.engine.api.FlowableException;

/**
 * 流程空用户异常。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public class FlowEmptyUserException extends FlowableException {

    /**
     * 构造函数。
     *
     * @param msg 错误信息。
     */
    public FlowEmptyUserException(String msg) {
        super(msg);
    }
}
