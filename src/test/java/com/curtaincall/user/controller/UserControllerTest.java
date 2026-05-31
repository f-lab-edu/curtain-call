package com.curtaincall.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.curtaincall.config.SecurityConfig;
import com.curtaincall.user.exception.DuplicateEmailException;
import com.curtaincall.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("유효한 회원가입 요청은 201과 생성된 userId를 반환한다")
    void signupReturns201() throws Exception {
        given(userService.signup(any())).willReturn(1L);
        Map<String, Object> body = Map.of(
                "name", "홍길동",
                "email", "hong@example.com",
                "password", "password123",
                "role", "MEMBER");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    @DisplayName("이메일 형식이 올바르지 않으면 400을 반환한다")
    void signupReturns400OnInvalidEmail() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "홍길동",
                "email", "not-an-email",
                "password", "password123",
                "role", "MEMBER");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("비밀번호가 8자 미만이면 400을 반환한다")
    void signupReturns400OnShortPassword() throws Exception {
        Map<String, Object> body = Map.of(
                "name", "홍길동",
                "email", "hong@example.com",
                "password", "short",
                "role", "MEMBER");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("이미 사용 중인 이메일이면 409를 반환한다")
    void signupReturns409OnDuplicateEmail() throws Exception {
        willThrow(new DuplicateEmailException("dup@example.com"))
                .given(userService).signup(any());
        Map<String, Object> body = Map.of(
                "name", "홍길동",
                "email", "dup@example.com",
                "password", "password123",
                "role", "MEMBER");

        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}
