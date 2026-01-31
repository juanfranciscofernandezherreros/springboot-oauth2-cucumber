package com.sixgroup.refit.ejemplo.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Slf4j
@Configuration
public class InvitationRabbitConfig {

    public static final String EXCHANGE = "invitation.exchange";
    public static final String QUEUE = "invitation.created.queue";
    public static final String ROUTING_KEY = "invitation.created";

    @Bean
    public TopicExchange invitationExchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE)
                .durable(true)
                .build();
    }

    @Bean
    public Queue invitationQueue() {
        return QueueBuilder
                .durable(QUEUE)
                .build();
    }

    @Bean
    public Binding invitationBinding(Queue invitationQueue, TopicExchange invitationExchange) {
        return BindingBuilder
                .bind(invitationQueue)
                .to(invitationExchange)
                .with(ROUTING_KEY);
    }

    @Bean
    public RabbitAdmin rabbitAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }

    // 🔥 IMPORTANTE: Convierte los objetos a JSON automáticamente
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void logRabbitSetup() {
        log.info("🐰 RabbitMQ Configurado: Exchange={}, Queue={}", EXCHANGE, QUEUE);
    }
}