package com.fitness.ai;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecommendationRepository extends JpaRepository<Recommendation, Long> {
    Optional<Recommendation> findByActivityId(Long activityId);
    List<Recommendation> findByUserIdOrderByCreatedAtDesc(String userId);
}
