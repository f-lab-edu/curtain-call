package com.curtaincall.user.infrastructure.entity;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserEntity {
    private Long userId;
    private String name;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public UserEntity(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }
}
