package com.fitness.activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "fitness.exchange";
    public static final String ROUTING_KEY = "workout.completed";

    @Bean TopicExchange fitnessExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean MessageConverter jsonConverter(ObjectMapper om) { return new Jackson2JsonMessageConverter(om); }
}
