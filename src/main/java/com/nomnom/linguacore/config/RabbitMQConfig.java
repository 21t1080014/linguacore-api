package com.nomnom.linguacore.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    @Bean
    public Queue aiTaskQueue(){
        return new Queue("ai.task.queue", true);
    }
}
