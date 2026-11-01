package com.fitness.ai;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity @Table(name = "recommendations")
@Getter @Setter @NoArgsConstructor
public class Recommendation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private Long activityId;
    @Column(nullable = false) private String userId;
    @Column(columnDefinition = "TEXT", nullable = false) private String content;
    private String source;            // GEMINI or FALLBACK
    private Instant createdAt = Instant.now();
}
