package com.curtaincall.user.service;

import com.curtaincall.user.domain.Role;
import com.curtaincall.user.domain.User;
import com.curtaincall.user.dto.SignupRequest;
import com.curtaincall.user.exception.DuplicateEmailException;
import com.curtaincall.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final int DEFAULT_BALANCE = 100000;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Long signup(SignupRequest request) {
        if (request.role() == Role.ADMIN) {
            throw new IllegalArgumentException("ADMIN 역할로는 가입할 수 없습니다");
        }
        if (userMapper.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .balance(DEFAULT_BALANCE)
                .build();

        userMapper.insert(user);
        return user.getUserId();
    }
}
