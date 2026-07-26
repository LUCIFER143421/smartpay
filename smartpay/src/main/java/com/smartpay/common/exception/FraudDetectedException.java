package com.smartpay.common.exception;

public class FraudDetectedException extends RuntimeException{
    public FraudDetectedException(String message) {
        super(message);
    }
}
