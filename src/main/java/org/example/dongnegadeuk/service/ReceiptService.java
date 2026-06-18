package org.example.dongnegadeuk.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.dto.ReceiptDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.dongnegadeuk.common.exception.errorCode.ReceiptErrorCode.OCR_FAILED;
import static org.example.dongnegadeuk.common.exception.errorCode.ReceiptErrorCode.RECEIPT_REQUIRED_FIELD_MISSING;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReceiptService {
//
//    private final ReceiptAiVisionService geminiService;
//    private final ReceiptClaudeVisionService claudeService;
    private final ReceiptUpstageVisionService upstageService;

    public ReceiptDto process(byte[] imageBytes) {
//        ReceiptDto geminiResult = geminiService.parse(imageBytes);
//        log.info("[Gemini] {}", geminiResult);

//        ReceiptDto claudeResult = claudeService.parse(imageBytes);
//        log.info("[Claude] {}", claudeResult);

        ReceiptDto upstageResult = upstageService.parse(imageBytes);
        log.info("[Up] {}", upstageResult);

        // 비교 테스트 끝나면 둘 중 하나만 쓰도록 정리
        ReceiptDto data = upstageResult;

        if (data.businessNumber() == null || data.storeName() == null
                || data.storeAddress() == null || data.transactionDate() == null
                || data.totalAmount() == null) {
            throw new CustomException(RECEIPT_REQUIRED_FIELD_MISSING);
        }
        return data;
    }
}