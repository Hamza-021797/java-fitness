package com.fitness.activity;

import java.time.Instant;

public record WorkoutCompletedEvent(Long activityId, String userId, String type, int durationMinutes,
                                    Double distanceKm, Integer caloriesBurned, String notes, Instant occurredAt) {
    static WorkoutCompletedEvent from(Activity a) {
        return new WorkoutCompletedEvent(a.getId(), a.getUserId(), a.getType().name(), a.getDurationMinutes(),
            a.getDistanceKm(), a.getCaloriesBurned(), a.getNotes(), a.getCreatedAt());
    }
}
