package com.curtaincall.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.curtaincall.user.domain.Role;
import com.curtaincall.user.domain.User;
import com.curtaincall.user.dto.SignupRequest;
import com.curtaincall.user.exception.DuplicateEmailException;
import com.curtaincall.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

class UserServiceTest {

    private UserMapper userMapper;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userMapper = org.mockito.Mockito.mock(UserMapper.class);
        passwordEncoder = new BCryptPasswordEncoder();
        userService = new UserService(userMapper, passwordEncoder);
    }

    @Test
    @DisplayName("회원가입 시 비밀번호는 BCrypt로 암호화되어 저장되고 기본 잔액은 100000이다")
    void signupEncodesPasswordAndSetsDefaultBalance() {
        SignupRequest request = new SignupRequest("홍길동", "hong@example.com", "password123", Role.MEMBER);
        given(userMapper.existsByEmail("hong@example.com")).willReturn(false);

        userService.signup(request);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(captor.capture());
        User saved = captor.getValue();

        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(passwordEncoder.matches("password123", saved.getPassword())).isTrue();
        assertThat(saved.getBalance()).isEqualTo(100000);
        assertThat(saved.getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    @DisplayName("이미 존재하는 이메일이면 DuplicateEmailException을 던지고 저장하지 않는다")
    void signupRejectsDuplicateEmail() {
        SignupRequest request = new SignupRequest("홍길동", "dup@example.com", "password123", Role.MEMBER);
        given(userMapper.existsByEmail("dup@example.com")).willReturn(true);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(DuplicateEmailException.class);

        verify(userMapper, never()).insert(any());
    }

    @Test
    @DisplayName("ADMIN 역할로는 가입할 수 없다")
    void signupRejectsAdminRole() {
        SignupRequest request = new SignupRequest("관리자", "admin@example.com", "password123", Role.ADMIN);
        given(userMapper.existsByEmail(anyString())).willReturn(false);

        assertThatThrownBy(() -> userService.signup(request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(userMapper, never()).insert(any());
    }
}
