package com.huya.v.exceptions;

/**
 * 基础业务异常. 所有关心的业务异常都应该通过此类或封装此类的子类抛出，交由显示层按照用户需求进行处理。
 *
 * @author xiaoh
 */
public class BasicBusinessException extends Exception {
    private static final long serialVersionUID = -7311715374370222856L;

    public BasicBusinessException(String message, String code, Throwable t) {
        super(message, t);
        this.code = code;
    }

    public BasicBusinessException(String message, String code) {
        this(message, code, null);
    }

    public BasicBusinessException(String code, Throwable t) {
        this(null, code, t);
    }

    public BasicBusinessException(String message) {
        this(message, null, null);
    }

    // 错误代码
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
