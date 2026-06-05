package com.curtaincall.auth.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import com.curtaincall.user.domain.Role;
import com.curtaincall.user.domain.User;
import com.curtaincall.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

class CustomUserDetailsServiceTest {

    private UserMapper userMapper;
    private CustomUserDetailsService service;

    @BeforeEach
    void setUp() {
        userMapper = org.mockito.Mockito.mock(UserMapper.class);
        service = new CustomUserDetailsService(userMapper);
    }

    @Test
    @DisplayName("이메일로 사용자를 찾으면 ROLE_ 접두사 권한이 매핑된 UserDetails를 반환한다")
    void loadUserMapsAuthorities() {
        User user = User.builder()
                .userId(1L).name("홍길동").email("hong@example.com")
                .password("hashed").role(Role.MEMBER).balance(100000)
                .build();
        given(userMapper.findByEmail("hong@example.com")).willReturn(user);

        UserDetails details = service.loadUserByUsername("hong@example.com");

        assertThat(details.getUsername()).isEqualTo("hong@example.com");
        assertThat(details.getPassword()).isEqualTo("hashed");
        assertThat(details.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_MEMBER");
    }

    @Test
    @DisplayName("이메일에 해당하는 사용자가 없으면 UsernameNotFoundException을 던진다")
    void loadUserThrowsWhenNotFound() {
        given(userMapper.findByEmail("none@example.com")).willReturn(null);

        assertThatThrownBy(() -> service.loadUserByUsername("none@example.com"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
