package com.npci.UPISim.exception;

public class UpiValidationException extends RuntimeException {

    private final String errCode;

    public UpiValidationException(String errCode) {
        super(errCode);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return errCode;
    }
}
