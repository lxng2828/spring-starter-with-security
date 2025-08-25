package com.vtlong.my_spring_boot_project.exception;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getErrorCodeString() {
        return errorCode.getCode();
    }

    @Override
    public String toString() {
        return String.format("AppException{errorCode=%s, message='%s'}",
                errorCode, getMessage());
    }
}