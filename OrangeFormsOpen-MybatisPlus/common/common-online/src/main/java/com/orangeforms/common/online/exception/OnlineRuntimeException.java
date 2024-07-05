package com.orangeforms.common.online.exception;

import com.orangeforms.common.core.exception.MyRuntimeException;

/**
 * 在线表单运行时异常。
 *
 * @author Jerry
 * @date 2024-07-02
 */
public class OnlineRuntimeException extends MyRuntimeException {

    /**
     * 构造函数。
     */
    public OnlineRuntimeException() {

    }

    /**
     * 构造函数。
     *
     * @param msg 错误信息。
     */
    public OnlineRuntimeException(String msg) {
        super(msg);
    }
}
