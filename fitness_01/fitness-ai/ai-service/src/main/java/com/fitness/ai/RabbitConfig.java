package com.fitness.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String EXCHANGE = "fitness.exchange";
    public static final String QUEUE = "workout.completed.ai";
    public static final String KEY = "workout.completed";

    @Bean TopicExchange fitnessExchange() { return new TopicExchange(EXCHANGE, true, false); }
    @Bean Queue aiQueue() { return QueueBuilder.durable(QUEUE).build(); }
    @Bean Binding binding(Queue aiQueue, TopicExchange fitnessExchange) {
        return BindingBuilder.bind(aiQueue).to(fitnessExchange).with(KEY);
    }
    @Bean MessageConverter jsonConverter(ObjectMapper om) {
        var c = new Jackson2JsonMessageConverter(om);
        var mapper = new DefaultJackson2JavaTypeMapper();
        mapper.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED); // ignore producer's class name
        mapper.setTrustedPackages("*");
        c.setJavaTypeMapper(mapper);
        return c;
    }
}
