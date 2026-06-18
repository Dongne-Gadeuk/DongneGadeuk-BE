package org.example.dongnegadeuk.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.dongnegadeuk.repository.PlacementsRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class PlacementReorderJob {

    private final PlacementsRepository placementsRepository;
    private final PlacementReorderService reorderService;

    // 매일 새벽 4시 (KST)
    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    public void run() {
        List<Long> userIds = placementsRepository.findDistinctUserIds();
        int totalChanged = 0;
        for (Long userId : userIds) {
            try { totalChanged += reorderService.normalizeForUser(userId); }
            catch (Exception e) { log.error("재정렬 실패 userId={}", userId, e); }
        }
        log.info("재정렬 완료: 유저 {}명, 변경 {}건", userIds.size(), totalChanged);
    }
}
