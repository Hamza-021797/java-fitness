package com.fitness.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkoutListener {
    private final RecommendationService service;

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void onWorkout(WorkoutCompletedEvent e) { service.generate(e); }
}
