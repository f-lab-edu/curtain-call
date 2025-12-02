package com.curtaincall.user.infrastructure;

import com.curtaincall.user.domain.UserRepository;
import com.curtaincall.user.infrastructure.entity.UserEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends UserRepository {

    boolean existByEmail(String email);

    void save(UserEntity entity);

}
