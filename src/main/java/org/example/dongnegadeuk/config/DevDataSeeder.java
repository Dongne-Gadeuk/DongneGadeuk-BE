package org.example.dongnegadeuk.config;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.entity.*;
import org.example.dongnegadeuk.repository.UserItemsRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class DevDataSeeder implements CommandLineRunner {

    @PersistenceContext
    private EntityManager em;
    private final UserItemsRepository userItemsRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("====== DevDataSeeder 실행 시도 ======");

        final Long TARGET_USER_ID = 3L;
        final String BED_IMAGE_URL = "https://placehold.co/200x200/png?text=BED";

        // 1) 로그인으로 생성된 유저 조회 (생성 X)
        Users user = em.find(Users.class, TARGET_USER_ID);
        if (user == null) {
            log.warn("====== userId={} 유저가 아직 없어 시더를 스킵합니다 (로그인 후 재시도) ======", TARGET_USER_ID);
            return;
        }

        // 2) 이 유저가 이미 아이템을 가지고 있으면 스킵
        Long existing = em.createQuery(
                        "select count(ui) from UserItems ui where ui.user.id = :uid", Long.class)
                .setParameter("uid", TARGET_USER_ID)
                .getSingleResult();
        if (existing > 0) {
            log.info("====== userId={} 에 이미 데이터가 있어 시더를 스킵합니다 ======", TARGET_USER_ID);
            return;
        }

        // 3) 아이템은 카탈로그성 데이터 → 있으면 재사용, 없으면 상점+아이템 생성
        Items item = em.createQuery(
                        "select i from Items i where i.imageUrl = :url", Items.class)
                .setParameter("url", BED_IMAGE_URL)
                .getResultStream()
                .findFirst()
                .orElse(null);

        if (item == null) {
            Stores store = Stores.builder()
                    .storeName("마을 가구점")
                    .address("서울특별시 강남구 테헤란로 123")
                    .businessNumber("123-45-67890")
                    .storeUrl("https://example-store.com")
                    .category(Category.values()[0])
                    .build();
            em.persist(store);

            item = Items.builder()
                    .itemName("기본 침대")
                    .imageUrl(BED_IMAGE_URL)
                    .requiredVisitCount(0)
                    .store(store)
                    .build();
            em.persist(item);
            log.info("카탈로그(상점/아이템) 신규 생성");
        } else {
            //log.info("기존 카탈로그 아이템 재사용 (itemId={})", item.getId());
        }

        // 4) 영수증 (이미 존재하는 상점을 재사용)
        Receipts receipt = Receipts.builder()
                .user(user)
                .store(item.getStore())
                .totalPrice("15000")
                .visitedAt(LocalDateTime.now())
                .build();
        em.persist(receipt);

        // 5) 보유 아이템 매핑
        UserItems ui = UserItems.builder()
                .user(user)
                .item(item)
                .placed(true)
                .receipt(receipt)
                .build();
        em.persist(ui);

        // 6) 방 배치 정보
        Placements placement = Placements.builder()
                .x(180)
                .y(180)
                .zOrder(1)
                .scale(BigDecimal.valueOf(1.0))
                .topBottom(false)
                .leftRight(false)
                .userItem(ui)
                .build();
        em.persist(placement);

        em.flush();
        log.info("====== userId={} 에 mock 데이터 반영 완료! ======", TARGET_USER_ID);
    }
}