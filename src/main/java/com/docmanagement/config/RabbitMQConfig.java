package com.docmanagement.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String DOCUMENT_QUEUE = "document.queue";
    public static final String DOCUMENT_EXCHANGE = "document.exchange";
    public static final String DOCUMENT_ROUTING_KEY = "document.routingkey";

    @Bean
    public Queue documentQueue() {
        return new Queue(DOCUMENT_QUEUE, true);
    }

    @Bean
    public DirectExchange documentExchange() {
        return new DirectExchange(DOCUMENT_EXCHANGE);
    }

    @Bean
    public Binding documentBinding(Queue documentQueue, DirectExchange documentExchange) {
        return BindingBuilder.bind(documentQueue).to(documentExchange).with(DOCUMENT_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
