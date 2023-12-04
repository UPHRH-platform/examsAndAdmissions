package com.tarento.upsmf.examsAndAdmissions.exceptions;

public class UserInfoFetchException extends RuntimeException {

    public UserInfoFetchException(String message) {
        super(message);
    }

    public UserInfoFetchException(String message, Throwable cause) {
        super(message, cause);
    }
}
