package com.npci.UPISim.exception;

public class UpiXmlValidationException extends RuntimeException {

    public UpiXmlValidationException(String message) {
        super(message);
    }

    public UpiXmlValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
