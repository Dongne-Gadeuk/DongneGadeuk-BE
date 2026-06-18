package org.example.dongnegadeuk.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.entity.Placements;
import org.example.dongnegadeuk.repository.PlacementsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlacementReorderService {

    private final PlacementsRepository placementsRepository;

    @Transactional
    public int normalizeForUser(Long userId) {
        List<Placements> list = placementsRepository.findAllByUserIdOrderByZOrder(userId);
        int z = 1, changed = 0;
        for (Placements p : list) {
            if (p.getZOrder() != z) { p.setZOrder(z); changed++; }
            z++;
        }
        return changed;
    }
}
