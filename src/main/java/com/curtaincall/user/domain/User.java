package com.curtaincall.user.domain;

import com.curtaincall.common.exception.ValidationException;
import lombok.Getter;
import org.springframework.util.StringUtils;

@Getter
public class User {

    private static final String EMAIL_PREFIX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final String PASSWORD_PREFIX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$";

    private Long userId;
    private String name;
    private String email;
    private String password;

    public User(
            final String name,
            final String email,
            final String password
    ) {
        validateContainsWhiteSpace(name);
        validateEmail(email);
        validatePassword(password);
        this.name = name;
        this.email = email;
        this.password = password;
    }

    private void validateEmail(String email) {
        validateContainsWhiteSpace(email);
        if (!email.matches(EMAIL_PREFIX)) {
            throw new ValidationException("올바르지 않은 이메일 양식입니다.");
        }
    }


    private void validateContainsWhiteSpace(String value) {
        if (StringUtils.containsWhitespace(value)) {
            throw new ValidationException("공백을 허용하지 않습니다.");
        }
    }

    private void validatePassword(String password) {
        validateContainsWhiteSpace(password);
        if (!password.matches(PASSWORD_PREFIX)) {
            throw new ValidationException("올바르지 않은 비밀번호 양식입니다.");
        }
    }
}
