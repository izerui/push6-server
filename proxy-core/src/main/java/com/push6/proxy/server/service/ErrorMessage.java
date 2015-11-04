package com.push6.proxy.server.service;

import com.push6.proxy.service.NettyException;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.UUID;

/**
 * Created by serv on 2014/12/12.
 */
class ErrorMessage implements Serializable {
    private String errorCode;
    private String errorMsg;
    private String nonceStr = StringUtils.replace(UUID.randomUUID().toString(), "-", "");

    public ErrorMessage() {
    }

    public ErrorMessage(Throwable cause) {
        if (cause instanceof NettyException) {
            this.errorCode = String.valueOf(((NettyException) cause).getErrorCode());
            this.errorMsg = cause.getMessage();
        } else {
            if (cause instanceof NullPointerException) {
                this.errorCode = "900";
                this.errorMsg = "空指针";
            } else {
                this.errorCode = "888";
                this.errorMsg = cause.getMessage();
            }
        }
    }


    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }
}