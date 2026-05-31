package com.curtaincall.user.mapper;

import com.curtaincall.user.domain.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {

    void insert(User user);

    boolean existsByEmail(String email);

    User findByEmail(String email);
}
