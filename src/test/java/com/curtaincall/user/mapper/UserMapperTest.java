package com.curtaincall.user.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.curtaincall.user.domain.Role;
import com.curtaincall.user.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserMapperTest {

    @Autowired
    private UserMapper userMapper;

    @Test
    @DisplayName("회원을 저장하면 이메일로 조회할 수 있고 생성된 ID가 채워진다")
    void insertAndFindByEmail() {
        User user = User.builder()
                .name("홍길동")
                .email("hong@example.com")
                .password("encoded-password")
                .role(Role.MEMBER)
                .balance(100000)
                .build();

        userMapper.insert(user);

        assertThat(user.getUserId()).isNotNull();

        User found = userMapper.findByEmail("hong@example.com");
        assertThat(found).isNotNull();
        assertThat(found.getName()).isEqualTo("홍길동");
        assertThat(found.getEmail()).isEqualTo("hong@example.com");
        assertThat(found.getRole()).isEqualTo(Role.MEMBER);
        assertThat(found.getBalance()).isEqualTo(100000);
    }

    @Test
    @DisplayName("존재하는 이메일이면 existsByEmail은 true, 아니면 false")
    void existsByEmail() {
        User user = User.builder()
                .name("김철수")
                .email("kim@example.com")
                .password("encoded-password")
                .role(Role.ORGANIZER)
                .balance(100000)
                .build();
        userMapper.insert(user);

        assertThat(userMapper.existsByEmail("kim@example.com")).isTrue();
        assertThat(userMapper.existsByEmail("none@example.com")).isFalse();
    }
}
