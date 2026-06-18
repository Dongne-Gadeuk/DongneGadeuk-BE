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

        if (userItemsRepository.count() > 0) {
            log.info("====== DB에 데이터가 이미 존재하여 시더를 스킵합니다. ======");
            return;
        }

        // 1) 유저 생성
        Users user = em.find(Users.class, 1L);
        if (user == null) {
            user = Users.builder()
                    .username("test_user")
                    .nickname("테스터")
                    .password("password123!")
                    .build();
            em.persist(user);
        }

        // 2) 상점 생성
        Category defaultCategory = Category.values()[0];
        Stores store = Stores.builder()
                .storeName("마을 가구점")
                .address("서울특별시 강남구 테헤란로 123")
                .businessNumber("123-45-67890")
                .storeUrl("https://example-store.com")
                .category(defaultCategory)
                .build();
        em.persist(store);

        // 3) 아이템 생성
        Items item = Items.builder()
                .itemName("기본 침대")
                .imageUrl("https://placehold.co/200x200/png?text=BED")
                .requiredVisitCount(0)
                .store(store)
                .build();
        em.persist(item);

        // 4) 영수증 생성
        Receipts receipt = Receipts.builder()
                .user(user)
                .store(store)
                .totalPrice("15000")
                .visitedAt(LocalDateTime.now())
                .build();
        em.persist(receipt);

        // 5) 보유 아이템 매핑 (UserItems)
        // 여기서는 이미 방에 기본 배치된 상태를 시뮬레이션하기 위해 placed를 true로 두고 시작하겠습니다.
        UserItems ui = UserItems.builder()
                .user(user)
                .item(item)
                .placed(true) // 💡 초기 배치가 존재하므로 true 설정
                .receipt(receipt)
                .build();
        em.persist(ui);

        // 6) 💡 [추가] 방 배치 정보 데이터 생성 (Placements)
        // 프론트엔드가 fetchRoom()을 호출할 때 반환해 줄 데이터 규격을 완성합니다.
        Placements placement = Placements.builder()
                .x(180)          // ROOM.w / 2 (중앙 배치)
                .y(180)          // ROOM.h / 2 (중앙 배치)
                .zOrder(1)       // 가장 안쪽 레이어
                .scale(BigDecimal.valueOf(1.0)) // 기본 크기 비율 1.0
                .topBottom(false)
                .leftRight(false)
                .userItem(ui)    // ⚠️ 위에서 생성한 보유 아이템 객체 매핑
                .build();
        em.persist(placement);
        log.info("초기 가구 배치 데이터 생성 완료");

        em.flush();
        log.info("====== DevDataSeeder 데이터 반영 완료! ======");
    }
}