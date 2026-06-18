package org.example.dongnegadeuk.dto;

public record ReceiptDto(
        String businessNumber,   // 사업자번호
        String storeName,        // 가게 이름
        String storeAddress,     // 가게 주소
        String transactionDate,  // 거래일시
        Integer totalAmount      // 총금액
) {}