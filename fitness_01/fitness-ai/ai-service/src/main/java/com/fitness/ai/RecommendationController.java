package com.fitness.ai;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
@RequiredArgsConstructor
public class RecommendationController {
    private final RecommendationRepository repo;

    /** 404 means "not generated yet" - the frontend polls. */
    @GetMapping("/{activityId}")
    public ResponseEntity<Recommendation> get(@RequestHeader("X-User-Id") String uid, @PathVariable Long activityId) {
        return repo.findByActivityId(activityId)
            .filter(r -> r.getUserId().equals(uid))
            .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Recommendation> mine(@RequestHeader("X-User-Id") String uid) {
        return repo.findByUserIdOrderByCreatedAtDesc(uid);
    }
}
