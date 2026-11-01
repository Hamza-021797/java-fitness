package com.fitness.user;

import jakarta.validation.constraints.*;

public record ProfileRequest(
    @NotBlank @Size(max = 100) String fullName,
    @Min(10) @Max(100) Integer age,
    @DecimalMin("50") @DecimalMax("250") Double heightCm,
    @DecimalMin("20") @DecimalMax("400") Double weightKg,
    @Size(max = 100) String fitnessGoal) {}
