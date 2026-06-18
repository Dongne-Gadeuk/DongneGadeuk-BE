package org.example.dongnegadeuk.common.exception.errorCode;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.dongnegadeuk.common.exception.model.BaseErrorCode;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ReceiptErrorCode implements BaseErrorCode {

    // 영수증
    RECEIPT_REQUIRED_FIELD_MISSING("RECEIPT001", "필수 정보를 읽지 못했어요. 다시 촬영해주세요.", HttpStatus.UNPROCESSABLE_ENTITY),
    OCR_FAILED("RECEIPT002", "영수증 인식에 실패했어요. 다시 시도해주세요.",HttpStatus.INTERNAL_SERVER_ERROR),
    EXIST_TODAY_RECEIPT("RECEIPT003", "오늘 이미 영수증을 적립했어요.",HttpStatus.CONFLICT),

    // 가게
    STORE_NOT_FOUND("STORE001", "해당 가게가 존재하지 않습니다.",HttpStatus.NOT_FOUND),
    VISIT_NOT_FOUND("VISIT001", "해당 가게에 대한 방문 기록이 존재하지 않습니다.",HttpStatus.NOT_FOUND),

    ITEM_NOT_FOUND("ITEM001", "해당 아이템이 존재하지 않습니다.",HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
