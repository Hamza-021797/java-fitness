package com.fitness.user;

import jakarta.persistence.*;
import java.time.Instant;
import lombok.*;

@Entity @Table(name = "user_profiles")
@Getter @Setter @NoArgsConstructor
public class UserProfile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true) private String keycloakId;
    private String email;
    private String fullName;
    private Integer age;
    private Double heightCm;
    private Double weightKg;
    private String fitnessGoal;
    private Instant updatedAt = Instant.now();
}
