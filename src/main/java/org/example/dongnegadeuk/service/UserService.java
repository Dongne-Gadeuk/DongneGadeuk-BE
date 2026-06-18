package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.common.exception.errorCode.AuthErrorCode;
import org.example.dongnegadeuk.dto.UserDto;
import org.example.dongnegadeuk.entity.Users;
import org.example.dongnegadeuk.repository.UserRepository;
import org.example.dongnegadeuk.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public UserDto.AuthResponse signUp(UserDto.SignUpRequest request) {
        // 아이디(username) 중복 체크
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new CustomException(AuthErrorCode.USERNAME_ALREADY_EXISTS);
        }

        // 닉네임 중복 체크
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new CustomException(AuthErrorCode.NICKNAME_ALREADY_EXISTS);
        }

        // 비밀번호 확인
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            throw new CustomException(AuthErrorCode.PASSWORD_MISMATCH);
        }

        // 사용자 생성
        Users user = Users.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build();

        Users savedUser = userRepository.save(user);
        log.info("회원가입 완료: userId={}, username={}", savedUser.getUserId(), savedUser.getUsername());

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(savedUser.getUserId(), savedUser.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(savedUser.getUserId());

        // Refresh Token DB 저장
        savedUser.setRefreshToken(refreshToken);
        userRepository.save(savedUser);

        return UserDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(savedUser.getUserId())
                .username(savedUser.getUsername())
                .nickname(savedUser.getNickname())
                .build();
    }

    // 로그인
    @Transactional
    public UserDto.AuthResponse login(UserDto.LoginRequest request) {
        // 사용자 조회
        Users user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
        }

        log.info("로그인 성공: userId={}, username={}", user.getUserId(), user.getUsername());

        // JWT 토큰 생성
        String accessToken = jwtUtil.generateAccessToken(user.getUserId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserId());

        // Refresh Token DB 갱신
        user.setRefreshToken(refreshToken);
        userRepository.save(user);

        return UserDto.AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .userId(user.getUserId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .build();
    }

    // 로그아웃
    @Transactional
    public UserDto.LogoutResponse logout(Long userId) {
        try {
            Users user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));

            // Refresh Token 제거
            user.setRefreshToken(null);
            userRepository.save(user);

            log.info("로그아웃 완료: userId={}", userId);

            return UserDto.LogoutResponse.builder()
                    .success(true)
                    .message("로그아웃이 완료되었습니다.")
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생: {}", e.getMessage(), e);
            throw new CustomException(AuthErrorCode.LOGOUT_FAILED);
        }
    }
}