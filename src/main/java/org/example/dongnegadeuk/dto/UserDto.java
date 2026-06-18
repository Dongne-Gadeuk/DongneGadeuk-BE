package org.example.dongnegadeuk.dto;
import lombok.*;

public class UserDto {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SignUpRequest {
        private String username;
        private String password;
        private String passwordConfirm;
        private String nickname;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LoginRequest {
        private String username;
        private String password;
    }

    @Getter
    @Builder
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String username;
        private String nickname;
    }

    @Getter
    @Builder
    public static class LogoutResponse {
        private boolean success;
        private String message;
    }
}