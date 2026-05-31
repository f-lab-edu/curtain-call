package com.curtaincall.user.dto;

import com.curtaincall.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다") String password,
        @NotNull Role role
) {
}
