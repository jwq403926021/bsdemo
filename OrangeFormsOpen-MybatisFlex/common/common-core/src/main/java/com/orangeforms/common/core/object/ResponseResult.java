package com.orangeforms.common.core.object;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.orangeforms.common.core.constant.ErrorCodeEnum;
import com.orangeforms.common.core.util.ContextUtil;
import com.orangeforms.common.core.util.MyModelUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 接口返回对象
 *
 * @author Jerry
 * @date 2024-07-02
 */
@Slf4j
@Data
public class ResponseResult<T> {

    /**
     * 为了优化性能，所有没有携带数据的正确结果，均可用该对象表示。
     */
    private static final ResponseResult<Void> OK = new ResponseResult<>();
    /**
     * 是否成功标记。
     */
    private boolean success = true;
    /**
     * 错误码。
     */
    private String errorCode = "NO-ERROR";
    /**
     * 错误信息描述。
     */
    private String errorMessage = "NO-MESSAGE";
    /**
     * 实际数据。
     */
    private T data = null;
    /**
     * HTTP状态码，通常用于内部调用的方法传递，不推荐返回给前端。
     */
    @JSONField(serialize = false)
    private int httpStatus = 200;

    /**
     * 根据参数errorCodeEnum的枚举值，判断创建成功对象还是错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和 getErrorMessage()。
     *
     * @param errorCodeEnum 错误码枚举。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static ResponseResult<Void> create(ErrorCodeEnum errorCodeEnum) {
        return create(errorCodeEnum, errorCodeEnum.getErrorMessage());
    }

    /**
     * 根据参数errorCodeEnum的枚举值，判断创建成功对象还是错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和参数 errorMessage。
     *
     * @param errorCodeEnum 错误码枚举。
     * @param errorMessage  如果该参数为null，错误信息取自errorCodeEnum参数内置的errorMessage，否则使用当前参数。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static ResponseResult<Void> create(ErrorCodeEnum errorCodeEnum, String errorMessage) {
        errorMessage = errorMessage != null ? errorMessage : errorCodeEnum.getErrorMessage();
        return errorCodeEnum == ErrorCodeEnum.NO_ERROR ? success() : error(errorCodeEnum.name(), errorMessage);
    }

    /**
     * 根据参数errorCode是否为空，判断创建成功对象还是错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCode 和参数 errorMessage。
     *
     * @param errorCode    自定义的错误码。
     * @param errorMessage 自定义的错误信息。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static ResponseResult<Void> create(String errorCode, String errorMessage) {
        return errorCode == null ? success() : error(errorCode, errorMessage);
    }

    /**
     * 根据参数errorCodeEnum的枚举值，判断创建成功对象还是错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和参数 errorMessage。
     *
     * @param errorCodeEnum 错误码枚举。
     * @param errorMessage  如果该参数为null，错误信息取自errorCodeEnum参数内置的errorMessage，否则使用当前参数。
     * @param data          如果错误枚举值为NO_ERROR，则返回该数据。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> create(ErrorCodeEnum errorCodeEnum, String errorMessage, T data) {
        errorMessage = errorMessage != null ? errorMessage : errorCodeEnum.getErrorMessage();
        return errorCodeEnum == ErrorCodeEnum.NO_ERROR ? success(data) : error(errorCodeEnum.name(), errorMessage);
    }

    /**
     * 创建成功对象。
     * 如果需要绑定返回数据，可以在实例化后调用setDataObject方法。
     *
     * @return 返回创建的ResponseResult实例对象。
     */
    public static ResponseResult<Void> success() {
        return OK;
    }

    /**
     * 创建带有返回数据的成功对象。
     *
     * @param data 返回的数据对象。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> success(T data) {
        ResponseResult<T> resp = new ResponseResult<>();
        resp.data = data;
        return resp;
    }

    /**
     * 创建带有返回数据的成功对象。
     *
     * @param data  返回的数据对象。
     * @param clazz 目标数据类型。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T, R> ResponseResult<T> success(R data, Class<T> clazz) {
        ResponseResult<T> resp = new ResponseResult<>();
        resp.data = MyModelUtil.copyTo(data, clazz);
        return resp;
    }

    /**
     * 创建错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和 getErrorMessage()。
     *
     * @param errorCodeEnum 错误码枚举。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> error(ErrorCodeEnum errorCodeEnum) {
        return error(errorCodeEnum.name(), errorCodeEnum.getErrorMessage());
    }
    
    /**
     * 创建错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和 getErrorMessage()。
     *
     * @param httpStatus    http状态值。
     * @param errorCodeEnum 错误码枚举。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> error(int httpStatus, ErrorCodeEnum errorCodeEnum) {
        ResponseResult<T> r = error(errorCodeEnum.name(), errorCodeEnum.getErrorMessage());
        r.setHttpStatus(httpStatus);
        return r;
    }

    /**
     * 创建错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和参数 errorMessage。
     *
     * @param errorCodeEnum 错误码枚举。
     * @param errorMessage  自定义的错误信息。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> error(ErrorCodeEnum errorCodeEnum, String errorMessage) {
        return error(errorCodeEnum.name(), errorMessage);
    }

    /**
     * 创建错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCodeEnum 的 name() 和参数 errorMessage。
     *
     * @param httpStatus    http状态值。
     * @param errorCodeEnum 错误码枚举。
     * @param errorMessage  自定义的错误信息。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> error(int httpStatus, ErrorCodeEnum errorCodeEnum, String errorMessage) {
        ResponseResult<T> r = error(errorCodeEnum.name(), errorMessage);
        r.setHttpStatus(httpStatus);
        return r;
    }

    /**
     * 创建错误对象。
     * 如果返回错误对象，errorCode 和 errorMessage 分别取自于参数 errorCode 和参数 errorMessage。
     *
     * @param errorCode    自定义的错误码。
     * @param errorMessage 自定义的错误信息。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> error(String errorCode, String errorMessage) {
        return new ResponseResult<>(errorCode, errorMessage);
    }

    /**
     * 根据参数中出错的ResponseResult，创建新的错误应答对象。
     *
     * @param errorCause 导致错误原因的应答对象。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T, E> ResponseResult<T> errorFrom(ResponseResult<E> errorCause) {
        return error(errorCause.errorCode, errorCause.getErrorMessage());
    }

    /**
     * 根据参数中出错的CallResult，创建新的错误应答对象。
     *
     * @param errorCause 导致错误原因的应答对象。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static <T> ResponseResult<T> errorFrom(CallResult errorCause) {
        return error(ErrorCodeEnum.DATA_VALIDATED_FAILED, errorCause.getErrorMessage());
    }

    /**
     * 根据参数中CallResult，创建新的应答对象。
     *
     * @param result CallResult对象。
     * @return 返回创建的ResponseResult实例对象。
     */
    public static ResponseResult<Void> from(CallResult result) {
        if (result.isSuccess()) {
            return success();
        }
        return error(ErrorCodeEnum.DATA_VALIDATED_FAILED, result.getErrorMessage());
    }

    /**
     * 是否成功。
     *
     * @return true成功，否则false。
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 通过HttpServletResponse直接输出应该信息的工具方法。
     *
     * @param httpStatus     http状态码。
     * @param responseResult 应答内容。
     * @param <T>            数据对象类型。
     * @throws IOException 异常错误。
     */
    public static <T> void output(int httpStatus, ResponseResult<T> responseResult) throws IOException {
        if (httpStatus != HttpServletResponse.SC_OK) {
            log.error(JSON.toJSONString(responseResult));
        } else {
            log.info(JSON.toJSONString(responseResult));
        }
        HttpServletResponse response = ContextUtil.getHttpResponse();
        PrintWriter out = response.getWriter();
        response.setContentType("application/json; charset=utf-8");
        response.setStatus(httpStatus);
        if (responseResult != null) {
            out.print(JSON.toJSONString(responseResult));
        }
        out.flush();
    }

    /**
     * 通过HttpServletResponse直接输出应该信息的工具方法。
     *
     * @param httpStatus http状态码。
     * @throws IOException 异常错误。
     */
    public static void output(int httpStatus) throws IOException {
        output(httpStatus, null);
    }

    /**
     * 通过HttpServletResponse直接输出应该信息的工具方法。Http状态码为200。
     *
     * @param responseResult 应答内容。
     * @param <T>            数据对象类型。
     * @throws IOException 异常错误。
     */
    public static <T> void output(ResponseResult<T> responseResult) throws IOException {
        output(HttpServletResponse.SC_OK, responseResult);
    }

    private ResponseResult() {
    }

    private ResponseResult(String errorCode, String errorMessage) {
        this.success = false;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
