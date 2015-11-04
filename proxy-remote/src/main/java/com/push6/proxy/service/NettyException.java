package com.push6.proxy.service;

/**
 * Created by Administrator on 2014/12/11.
 */
public class NettyException extends RuntimeException {

    //错误码
    private final Integer errorCode;

    public NettyException(Integer errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public NettyException(Integer errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public Integer getErrorCode() {
        return errorCode;
    }
}
