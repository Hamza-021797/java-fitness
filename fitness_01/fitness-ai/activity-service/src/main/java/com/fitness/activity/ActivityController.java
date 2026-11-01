package com.fitness.activity;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService service;

    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Activity create(@RequestHeader("X-User-Id") String uid, @Valid @RequestBody ActivityRequest r) {
        return service.create(uid, r);
    }

    @GetMapping
    public List<Activity> list(@RequestHeader("X-User-Id") String uid) { return service.list(uid); }
}
