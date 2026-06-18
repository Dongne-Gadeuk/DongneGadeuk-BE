package org.example.dongnegadeuk.common.exception.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.dongnegadeuk.common.exception.model.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptErrorCode implements BaseErrorCode {

    // 회원가입
    RECEIPT_REQUIRED_FIELD_MISSING("RECEIPT001", "필수 정보를 읽지 못했어요. 다시 촬영해주세요.", HttpStatus.UNPROCESSABLE_ENTITY),
    OCR_FAILED("RECEIPT002", "영수증 인식에 실패했어요. 다시 시도해주세요.",HttpStatus.INTERNAL_SERVER_ERROR),;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
