package com.fitness.user;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfileController {
    private final UserProfileRepository repo;

    @PostMapping("/users/profile")
    public UserProfile upsert(@RequestHeader("X-User-Id") String uid,
                              @RequestHeader(value = "X-User-Email", required = false) String email,
                              @Valid @RequestBody ProfileRequest r) {
        var p = repo.findByKeycloakId(uid).orElseGet(() -> {
            var n = new UserProfile(); n.setKeycloakId(uid); return n; });
        p.setEmail(email);
        p.setFullName(r.fullName()); p.setAge(r.age());
        p.setHeightCm(r.heightCm()); p.setWeightKg(r.weightKg());
        p.setFitnessGoal(r.fitnessGoal());
        p.setUpdatedAt(Instant.now());
        return repo.save(p);
    }

    @GetMapping("/users/me")
    public ResponseEntity<UserProfile> me(@RequestHeader("X-User-Id") String uid) {
        return repo.findByKeycloakId(uid).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** ADMIN only - enforced at the gateway. */
    @GetMapping("/admin/users")
    public List<UserProfile> all() { return repo.findAll(); }
}
