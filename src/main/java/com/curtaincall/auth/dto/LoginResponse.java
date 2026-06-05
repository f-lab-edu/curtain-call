package com.curtaincall.auth.dto;

import com.curtaincall.user.domain.Role;
import com.curtaincall.user.domain.User;

public record LoginResponse(Long userId, String name, String email, Role role) {

    public static LoginResponse from(User user) {
        return new LoginResponse(user.getUserId(), user.getName(), user.getEmail(), user.getRole());
    }
}
