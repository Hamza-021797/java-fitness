package com.fitness.ai;

import java.time.Instant;

public record WorkoutCompletedEvent(Long activityId, String userId, String type, int durationMinutes,
                                    Double distanceKm, Integer caloriesBurned, String notes, Instant occurredAt) {}
