package com.pmp.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {

    private String code;
    public static final String DEFAULT_ERROR_CODE = "BUSINESS_ERROR";

    public BusinessException(String message) {
        super(message);
        this.code = DEFAULT_ERROR_CODE;
    }

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
