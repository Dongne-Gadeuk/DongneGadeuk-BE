package org.example.dongnegadeuk.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.common.response.BaseResponse;
import org.example.dongnegadeuk.dto.UserDto;
import org.example.dongnegadeuk.service.UserService;
import org.example.dongnegadeuk.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User", description = "회원 인증 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Operation(summary = "회원가입", description = "아이디, 비밀번호, 닉네임으로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public BaseResponse<UserDto.AuthResponse> signUp(
            @Valid @RequestBody UserDto.SignUpRequest request
    ) {
        UserDto.AuthResponse response = userService.signUp(request);
        return BaseResponse.success("회원가입 완료", response);
    }

    @Operation(summary = "로그인", description = "아이디와 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public BaseResponse<UserDto.AuthResponse> login(
            @Valid @RequestBody UserDto.LoginRequest request
    ) {
        UserDto.AuthResponse response = userService.login(request);
        return BaseResponse.success("로그인 완료", response);
    }

    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃합니다.")
    @GetMapping("/logout")
    public BaseResponse<UserDto.LogoutResponse> logout(
            HttpServletRequest request
    ) {
        String token = JwtUtil.getTokenFromHeader(request);

        jwtUtil.validateToken(token);
        Long userId = jwtUtil.getUserIdFromToken(token);

        UserDto.LogoutResponse response = userService.logout(userId);

        return BaseResponse.success("로그아웃 완료", response);
    }
}