package com.fitness.activity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkoutEventPublisher {
    private final RabbitTemplate rabbit;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void on(WorkoutCompletedEvent e) {
        try {
            rabbit.convertAndSend(RabbitConfig.EXCHANGE, RabbitConfig.ROUTING_KEY, e);
        } catch (AmqpException ex) {
            // The workout is already saved; a broker outage must not fail the request.
            log.warn("Could not publish WorkoutCompleted for activity {}: {}", e.activityId(), ex.getMessage());
        }
    }
}
