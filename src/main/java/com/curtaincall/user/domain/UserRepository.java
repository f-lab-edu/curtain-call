package com.curtaincall.user.domain;

import com.curtaincall.user.infrastructure.entity.UserEntity;

public interface UserRepository {

    boolean existByEmail(String email);

    void save(UserEntity entity);
}
