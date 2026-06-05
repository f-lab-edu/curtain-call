package com.curtaincall.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private void signup(String email) throws Exception {
        Map<String, Object> body = Map.of(
                "name", "홍길동", "email", email, "password", "password123", "role", "MEMBER");
        mockMvc.perform(post("/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("로그인하면 세션으로 /me 조회가 가능하고, 로그아웃 후 세션 없는 /me는 401이다")
    void loginSessionFlow() throws Exception {
        signup("flow@example.com");

        Map<String, Object> login = Map.of("email", "flow@example.com", "password", "password123");
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@example.com"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();

        // 세션으로 현재 사용자 조회
        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("flow@example.com"));

        // 로그아웃
        mockMvc.perform(post("/auth/logout").session(session))
                .andExpect(status().isNoContent());

        // 세션 없이 보호 자원 접근 → 401
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("로그인 시 기존 세션이 있으면 세션 고정 보호를 위해 세션 ID를 재발급한다")
    void loginRotatesSessionIdWhenSessionExists() throws Exception {
        signup("fixation@example.com");
        MockHttpSession existingSession = new MockHttpSession();
        String oldSessionId = existingSession.getId();

        Map<String, Object> login = Map.of("email", "fixation@example.com", "password", "password123");
        mockMvc.perform(post("/auth/login")
                        .session(existingSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk());

        assertThat(existingSession.getId()).isNotEqualTo(oldSessionId);
    }

    @Test
    @DisplayName("잘못된 비밀번호로 로그인하면 401과 일반 메시지를 반환한다")
    void loginWithWrongPasswordReturns401() throws Exception {
        signup("wrong@example.com");

        Map<String, Object> login = Map.of("email", "wrong@example.com", "password", "wrongpassword");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인하면 401과 동일한 메시지를 반환한다(계정 열거 방지)")
    void loginWithUnknownEmailReturns401() throws Exception {
        Map<String, Object> login = Map.of("email", "ghost@example.com", "password", "password123");
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("이메일 또는 비밀번호가 일치하지 않습니다."));
    }
}
