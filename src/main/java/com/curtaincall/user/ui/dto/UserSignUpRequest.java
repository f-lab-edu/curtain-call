package com.curtaincall.user.ui.dto;

public record UserSignUpRequest(
        String name,
        String email,
        String password
) {
}
