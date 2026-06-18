package org.example.dongnegadeuk.controller;

import lombok.RequiredArgsConstructor;
import org.example.dongnegadeuk.service.PlacementReorderJob;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReorderDevController {

    private final PlacementReorderJob job;

    @PostMapping("/dev/reorder")
    public String trigger() {
        job.run();
        return "done";
    }
}
