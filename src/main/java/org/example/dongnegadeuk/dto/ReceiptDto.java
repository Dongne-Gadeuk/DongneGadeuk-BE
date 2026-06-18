package org.example.dongnegadeuk.dto;

import lombok.*;

@Data
@Getter
@Builder
@Setter
@AllArgsConstructor
public class ReceiptDto{
        private String businessNumber;  // 사업자번호
        private String storeName;   // 가게 이름
        private String storeAddress;     // 가게 주소
        private String transactionDate; // 거래일시
        private Integer totalAmount;  // 총금액
}