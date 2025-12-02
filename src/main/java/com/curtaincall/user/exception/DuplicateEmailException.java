package com.curtaincall.user.exception;

import com.curtaincall.common.exception.CustomException;

public class DuplicateEmailException extends CustomException {

    private static final String MESSAGE = "중복된 이메일을 가진 계정이 존재합니다.";

    public DuplicateEmailException() {
        super(MESSAGE);
    }
}
