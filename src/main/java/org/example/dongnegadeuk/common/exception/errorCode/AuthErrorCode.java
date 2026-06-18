package org.example.dongnegadeuk.common.exception.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.dongnegadeuk.common.exception.model.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum AuthErrorCode implements BaseErrorCode {

    // 회원가입
    USERNAME_ALREADY_EXISTS("AUTH001", "이미 사용중인 아이디입니다.", HttpStatus.CONFLICT),
    NICKNAME_ALREADY_EXISTS("AUTH002", "이미 사용중인 닉네임입니다.", HttpStatus.CONFLICT),
    INVALID_PASSWORD_FORMAT("AUTH003", "비밀번호 형식이 올바르지 않습니다. (영문 소문자 8~16자, 특수문자 !#$%&*@^ 포함)", HttpStatus.BAD_REQUEST),
    PASSWORD_MISMATCH("AUTH004", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // 로그인
    USER_NOT_FOUND("AUTH009", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    INVALID_CREDENTIALS("AUTH010", "아이디 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
    LOGOUT_FAILED("AUTH013", "로그아웃 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // OAuth
    OAUTH_PROCESSING_FAILED("AUTH012", "소셜 로그인 처리 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_OAUTH_PROVIDER("AUTH013", "지원하지 않는 소셜 로그인 제공자입니다.", HttpStatus.BAD_REQUEST),

    // 온보딩
    ONBOARDING_ALREADY_COMPLETED("AUTH015", "이미 온보딩을 완료한 사용자입니다.",HttpStatus.BAD_REQUEST),
    ONBOARDING_SAVE_FAILED( "AUTH016", "온보딩 정보 저장 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ONBOARDING_LOAD_FAILED( "AUTH017", "온보딩 정보 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    INTERNAL_SERVER_ERROR("AUTH018", "서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);
    private final String code;
    private final String message;
    private final HttpStatus status;
}
