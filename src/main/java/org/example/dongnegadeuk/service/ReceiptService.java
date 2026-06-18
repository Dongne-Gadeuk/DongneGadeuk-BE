package org.example.dongnegadeuk.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.dto.ReceiptDto;
import org.example.dongnegadeuk.entity.Users;
import org.example.dongnegadeuk.repository.ReceiptRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static org.example.dongnegadeuk.common.exception.errorCode.ReceiptErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReceiptService {

    private final ReceiptUpstageVisionService upstageService;
    private final ReceiptRepository receiptRepository;
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    public ReceiptDto process(Users user, byte[] imageBytes) {
//        ReceiptDto geminiResult = geminiService.parse(imageBytes);
//        log.info("[Gemini] {}", geminiResult);

//        ReceiptDto claudeResult = claudeService.parse(imageBytes);
//        log.info("[Claude] {}", claudeResult);


        LocalDate today = LocalDate.now(KST);
        boolean existsToday = receiptRepository.existsByUserAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                user, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        if (existsToday) {
            throw new CustomException(EXIST_TODAY_RECEIPT);
        }

        ReceiptDto upstageResult = upstageService.parse(imageBytes);
        log.info("[Up] {}", upstageResult);

        // 비교 테스트 끝나면 둘 중 하나만 쓰도록 정리
        ReceiptDto data = upstageResult;

        if (data.getBusinessNumber() == null || data.getStoreName() == null
                || data.getStoreAddress() == null || data.getTransactionDate() == null
                || data.getTotalAmount() == null) {
            throw new CustomException(RECEIPT_REQUIRED_FIELD_MISSING);
        }
        return data;
    }
}