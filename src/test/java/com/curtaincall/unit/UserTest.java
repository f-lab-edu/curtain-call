package com.curtaincall.unit;

import com.curtaincall.common.exception.ValidationException;
import com.curtaincall.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("[Unit] User 도메인 객체 테스트")
class UserTest {

    @Nested
    @DisplayName("유저 도메인 객체 생성 요청 시")
    class CreateUser {
        @Test
        @DisplayName("모든 필드가 유효하면 User 객체를 생성한다.")
        void createUserSuccess() {
            //given
            String validName = "홍길동";
            String validEmail = "test@example.com";
            String validPassword = "password123";

            //when
            User user = new User(validName, validEmail, validPassword);

            //then
            assertThat(user).isNotNull();
            assertThat(user.getName()).isEqualTo(validName);
            assertThat(user.getEmail()).isEqualTo(validEmail);
            assertThat(user.getPassword()).isEqualTo(validPassword);
        }

        @ParameterizedTest
        @MethodSource("invalidNameProvider")
        @DisplayName("name이 비어있거나 공백이 포함된 경우 예외를 반환한다.")
        void invalidUserName(String name, String expectedMessage) {
            //given
            String validEmail = "test@example.com";
            String validPassword = "password123";

            //when & then
            assertThatThrownBy(() -> new User(name, validEmail, validPassword))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidNameProvider() {
            return Stream.of(
                    Arguments.of("홍 길동", "공백을 허용하지 않습니다."),
                    Arguments.of("홍\t길동", "공백을 허용하지 않습니다."),
                    Arguments.of("홍\n길동", "공백을 허용하지 않습니다.")
            );
        }

        @ParameterizedTest
        @MethodSource("invalidEmailProvider")
        @DisplayName("email의 양식이 올바르지 않으면 예외를 반환한다.")
        void invalidEmail(String email, String expectedMessage) {
            //given
            String validName = "홍길동";
            String validPassword = "password123";

            //when & then
            assertThatThrownBy(() -> new User(validName, email, validPassword))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidEmailProvider() {
            return Stream.of(
                    Arguments.of("test @example.com", "공백을 허용하지 않습니다."),
                    Arguments.of("testexample.com", "올바르지 않은 이메일 양식입니다."),
                    Arguments.of("test@", "올바르지 않은 이메일 양식입니다."),
                    Arguments.of("@example.com", "올바르지 않은 이메일 양식입니다."),
                    Arguments.of("test@example", "올바르지 않은 이메일 양식입니다.")
            );
        }

        @ParameterizedTest
        @MethodSource("invalidPasswordProvider")
        @DisplayName("password의 양식이 올바르지 않으면 예외를 반환한다.")
        void invalidPassword(String password, String expectedMessage) {
            //given
            String validName = "홍길동";
            String validEmail = "test@example.com";

            //when & then
            assertThatThrownBy(() -> new User(validName, validEmail, password))
                    .isInstanceOf(ValidationException.class)
                    .hasMessage(expectedMessage);
        }

        static Stream<Arguments> invalidPasswordProvider() {
            return Stream.of(
                    Arguments.of("password 123", "공백을 허용하지 않습니다."),
                    Arguments.of("12345678", "올바르지 않은 비밀번호 양식입니다."),
                    Arguments.of("password", "올바르지 않은 비밀번호 양식입니다."),
                    Arguments.of("pass123", "올바르지 않은 비밀번호 양식입니다.")
            );
        }

        @ParameterizedTest
        @MethodSource("validEmailProvider")
        @DisplayName("이메일이 다양한 유효한 형식이면 User 객체를 생성한다.")
        void createUserWithVariousValidEmails(String email) {
            //given
            String validName = "홍길동";
            String validPassword = "password123";

            //when
            User user = new User(validName, email, validPassword);

            //then
            assertThat(user).isNotNull();
            assertThat(user.getEmail()).isEqualTo(email);
        }

        static Stream<Arguments> validEmailProvider() {
            return Stream.of(
                    Arguments.of("test@example.com"),
                    Arguments.of("user.name@example.co.kr"),
                    Arguments.of("user+tag@example.com"),
                    Arguments.of("user_name@example-domain.com")
            );
        }
    }

}
