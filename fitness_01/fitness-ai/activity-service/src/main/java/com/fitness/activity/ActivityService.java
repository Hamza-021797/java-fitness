package com.fitness.activity;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ActivityService {
    private final ActivityRepository repo;
    private final ApplicationEventPublisher events;

    @Transactional
    public Activity create(String uid, ActivityRequest r) {
        var a = new Activity();
        a.setUserId(uid); a.setType(r.type()); a.setDurationMinutes(r.durationMinutes());
        a.setDistanceKm(r.distanceKm()); a.setCaloriesBurned(r.caloriesBurned()); a.setNotes(r.notes());
        a = repo.save(a);
        events.publishEvent(WorkoutCompletedEvent.from(a)); // sent to RabbitMQ only after DB commit
        return a;
    }

    public List<Activity> list(String uid) { return repo.findByUserIdOrderByCreatedAtDesc(uid); }
}
