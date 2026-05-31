package com.curtaincall.user.domain;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private Long userId;
    private String name;
    private String email;
    private String password;
    private Role role;
    private Integer balance;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
