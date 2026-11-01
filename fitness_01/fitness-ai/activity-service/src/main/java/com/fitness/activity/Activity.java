package com.fitness.activity;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity @Table(name = "activities", indexes = @Index(columnList = "userId"))
@Getter @Setter @NoArgsConstructor
public class Activity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private String userId;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private ActivityType type;
    private int durationMinutes;
    private Double distanceKm;
    private Integer caloriesBurned;
    @Column(length = 500) private String notes;
    private Instant createdAt = Instant.now();
}
