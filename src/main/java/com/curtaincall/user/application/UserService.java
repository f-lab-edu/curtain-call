package com.curtaincall.user.application;

import com.curtaincall.common.service.PasswordManager;
import com.curtaincall.user.domain.User;
import com.curtaincall.user.domain.UserRepository;
import com.curtaincall.user.exception.DuplicateEmailException;
import com.curtaincall.user.infrastructure.entity.UserEntity;
import com.curtaincall.user.ui.dto.UserSignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordManager passwordManager;

    /**
     * 회원가입 기능을 제공합니다.
     *
     * @param request 유저 회원가입 정보
     */
    @Transactional
    public void signUp(UserSignUpRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }

        User user = new User(request.name(), request.email(), request.password());
        UserEntity entity = new UserEntity(user.getName(), user.getEmail(), passwordManager.encrypt(user.getPassword()));

        userRepository.save(entity);
    }
}
