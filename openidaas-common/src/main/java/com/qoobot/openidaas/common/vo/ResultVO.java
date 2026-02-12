package com.qoobot.openidaas.common.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果封装类
 *
 * @param <T> 数据类型
 * @author QooBot
 */
@Data
@Schema(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResultVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "响应码")
    private String code;

    @Schema(description = "响应消息")
    private String message;

    @Schema(description = "响应数据")
    private T data;

    @Schema(description = "时间戳")
    private Long timestamp;

    public ResultVO() {
        this.timestamp = System.currentTimeMillis();
    }

    public ResultVO(String code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public ResultVO(String code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> ResultVO<T> success() {
        return new ResultVO<>("0", "success");
    }

    /**
     * 成功响应带数据
     */
    public static <T> ResultVO<T> success(T data) {
        return new ResultVO<>("0", "success", data);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> ResultVO<T> success(String message, T data) {
        return new ResultVO<>("0", message, data);
    }

    /**
     * 失败响应
     */
    public static <T> ResultVO<T> error(String code, String message) {
        return new ResultVO<>(code, message);
    }

    /**
     * 失败响应（使用错误码枚举）
     */
    public static <T> ResultVO<T> error(com.qoobot.openidaas.common.exception.ErrorCode errorCode) {
        return new ResultVO<>(errorCode.getCode(), errorCode.getMessage());
    }

    /**
     * 失败响应带数据
     */
    public static <T> ResultVO<T> error(String code, String message, T data) {
        return new ResultVO<>(code, message, data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return "0".equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
}