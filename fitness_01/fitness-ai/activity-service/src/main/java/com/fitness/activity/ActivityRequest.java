package com.fitness.activity;

import jakarta.validation.constraints.*;

public record ActivityRequest(
    @NotNull ActivityType type,
    @Min(1) @Max(1440) int durationMinutes,
    @DecimalMin("0") @DecimalMax("500") Double distanceKm,
    @Min(0) @Max(20000) Integer caloriesBurned,
    @Size(max = 500) String notes) {}
