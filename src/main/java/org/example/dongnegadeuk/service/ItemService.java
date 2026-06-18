package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.common.exception.CustomException;
import org.example.dongnegadeuk.dto.ItemReceiveDto;
import org.example.dongnegadeuk.dto.ReceiptDto;
import org.example.dongnegadeuk.entity.*;
import org.example.dongnegadeuk.repository.*;
import org.example.dongnegadeuk.util.GeminiIconUtil;
import org.example.dongnegadeuk.util.GooglePlacesUtil;
import org.example.dongnegadeuk.util.KakaoMapUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.example.dongnegadeuk.common.exception.errorCode.ReceiptErrorCode.*;
import static org.example.dongnegadeuk.entity.Category.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final StoreRepository storeRepository;
    private final ReceiptRepository receiptRepository;
    private final UserItemRepository userItemRepository;
    private final UserStoreVisitRepository userStoreVisitRepository;
    private final KakaoMapUtil kakaoMapUtil;
    private final GooglePlacesUtil googlePlacesUtil;
    private final GeminiIconUtil geminiIconUtil;
    private final S3Service s3Service;

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Transactional
    public ItemReceiveDto receiveItem(Users user, ReceiptDto dto) {

        // 1. Kakao로 가게 검증
        KakaoMapUtil.KakaoPlace p = kakaoMapUtil
                .findNearbyStore(dto.getStoreAddress(), dto.getStoreName())
                .orElseThrow(() -> new CustomException(STORE_NOT_FOUND));

        // 2. 오늘 (어떤 가게든) 이미 적립했는지
        LocalDate today = LocalDate.now(KST);
        boolean existsToday = receiptRepository.existsByUserAndCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                user, today.atStartOfDay(), today.plusDays(1).atStartOfDay());
        if (existsToday) {
            throw new CustomException(EXIST_TODAY_RECEIPT);
        }

        // 3. 가게가 stores에 있나 → 없으면 store + Items 생성
        Optional<Stores> storeOpt = storeRepository
                .findByBusinessNumberAndAddressAndStoreName(dto.getBusinessNumber(), p.roadAddress(), p.name());

        Stores store;
        Items item;

        if (storeOpt.isEmpty()) {
            Category category = mapCategory(p.categoryGroupCode());

            // 아이콘 먼저 (실패 시 throw → 롤백)
            List<byte[]> photos = googlePlacesUtil.fetchStorePhotos(p.name(), p.roadAddress());
            GeminiIconUtil.GeneratedIcon icon = geminiIconUtil.generateIcon(p.name(), category.toString(), photos);
            String iconUrl = s3Service.uploadIcon(icon.image());

            store = storeRepository.save(Stores.builder()
                    .businessNumber(dto.getBusinessNumber())
                    .address(p.roadAddress())
                    .storeName(p.name())
                    .category(category)
                    .build());

            item = itemRepository.save(Items.builder()
                    .itemName(store.getStoreName() + "-" + icon.name())
                    .imageUrl(iconUrl)
                    .requiredVisitCount(1)
                    .store(store)
                    .build());
        } else {
            store = storeOpt.get();
            item = itemRepository.findByStore_StoreId(store.getStoreId())   // 추가 필요
                    .orElseThrow(() -> new CustomException(ITEM_NOT_FOUND));
        }

        // 4. 이 유저가 이 가게 영수증 기록이 있나 (= 첫 방문인가) — 이번 영수증 저장 '전에' 체크
        boolean firstVisit = !receiptRepository
                .existsByUser_UserIdAndStore_StoreId(user.getUserId(), store.getStoreId());   // 추가 필요

        // 이번 영수증 저장
        Receipts receipt = receiptRepository.save(Receipts.builder()
                .user(user)
                .store(store)
                .totalPrice(String.valueOf(dto.getTotalAmount()))
                .build());

        // 4-1. 첫 방문이면 UserItems 지급
        if (firstVisit) {
            userItemRepository.save(UserItems.builder()
                    .user(user)
                    .item(item)
                    .receipt(receipt)
                    .placed(false)
                    .build());
        }

        // 5. 방문 횟수 +1 (없으면 1로 생성)
        UserStoreVisits visits = userStoreVisitRepository
                .findByUser_UserIdAndStore_StoreId(user.getUserId(), store.getStoreId())
                .orElseGet(() -> userStoreVisitRepository.save(UserStoreVisits.builder()
                        .user(user).store(store).visitCount(0).build()));
        visits.increaseVisitCount();

        // 응답
        ItemReceiveDto.StoreInfo storeInfo = ItemReceiveDto.StoreInfo.builder()
                .storeId(store.getStoreId())
                .storeName(store.getStoreName())
                .transactionDate(parseTransactionDate(dto.getTransactionDate()))   // TODO: 영수증 실제 거래일 쓰려면 dto.getTransactionDate() 파싱
                .visitCount(visits.getVisitCount())
                .build();

        if (firstVisit) {
            ItemReceiveDto.RewardItem rewardItem = ItemReceiveDto.RewardItem.builder()
                    .itemId(item.getItemId())
                    .imageUrl(item.getImageUrl())
                    .name(item.getItemName())
                    .build();
            return ItemReceiveDto.builder()
                    .type(ItemReceiveDto.ScanResultType.ITEM_CREATE)
                    .store(storeInfo)
                    .item(rewardItem)
                    .build();
        }

        return ItemReceiveDto.builder()
                .type(ItemReceiveDto.ScanResultType.VISIT_ONLY)
                .store(storeInfo)
                .item(null)
                .build();
    }

    private Category mapCategory(String groupCode) {
        if (Objects.equals(groupCode, "CE7")) return CAFE;
        if (Objects.equals(groupCode, "FD6")) return RESTAURANT;
        return ETC;
    }

    private LocalDate parseTransactionDate(String raw) {
        if (raw == null || raw.isBlank()) return LocalDate.now(KST);
        try {
            // "2026.06.19 22:18" / "2026-06-19 22:18" / "2026/06/19" 등 앞 10자리 추출
            String datePart = raw.trim().substring(0, 10)
                    .replace(".", "-")
                    .replace("/", "-");
            return LocalDate.parse(datePart);
        } catch (Exception e) {
            log.warn("거래일시 파싱 실패, 오늘 날짜로 대체: {}", raw);
            return LocalDate.now(KST);
        }
    }
}