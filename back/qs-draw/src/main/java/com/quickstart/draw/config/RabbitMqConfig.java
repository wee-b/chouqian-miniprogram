package com.quickstart.draw.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "true")
public class RabbitMqConfig {

    public static final String DRAW_EXCHANGE = "qs.draw.exchange";
    public static final String DRAW_JOIN_QUEUE = "qs.draw.join.queue";
    public static final String DRAW_JOIN_ROUTING_KEY = "draw.join.code.generate";
    public static final String DRAW_OPEN_QUEUE = "qs.draw.open.queue";
    public static final String DRAW_OPEN_ROUTING_KEY = "draw.open.execute";

    public static final String DRAW_DLX_EXCHANGE = "qs.draw.dlx.exchange";
    public static final String DRAW_DLX_QUEUE = "qs.draw.dlx.queue";
    public static final String DRAW_DLX_ROUTING_KEY = "draw.dlx";

    @Bean
    public DirectExchange drawExchange() {
        return new DirectExchange(DRAW_EXCHANGE);
    }

    @Bean
    public DirectExchange drawDlxExchange() {
        return new DirectExchange(DRAW_DLX_EXCHANGE);
    }

    @Bean
    public Queue drawJoinQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DRAW_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DRAW_DLX_ROUTING_KEY);
        return new Queue(DRAW_JOIN_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue drawOpenQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DRAW_DLX_EXCHANGE);
        args.put("x-dead-letter-routing-key", DRAW_DLX_ROUTING_KEY);
        return new Queue(DRAW_OPEN_QUEUE, true, false, false, args);
    }

    @Bean
    public Queue drawDlxQueue() {
        return new Queue(DRAW_DLX_QUEUE, true);
    }

    @Bean
    public Binding drawJoinBinding() {
        return BindingBuilder.bind(drawJoinQueue()).to(drawExchange()).with(DRAW_JOIN_ROUTING_KEY);
    }

    @Bean
    public Binding drawOpenBinding() {
        return BindingBuilder.bind(drawOpenQueue()).to(drawExchange()).with(DRAW_OPEN_ROUTING_KEY);
    }

    @Bean
    public Binding drawDlxBinding() {
        return BindingBuilder.bind(drawDlxQueue()).to(drawDlxExchange()).with(DRAW_DLX_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jackson2JsonMessageConverter());
        factory.setAcknowledgeMode(org.springframework.amqp.core.AcknowledgeMode.MANUAL);
        return factory;
    }
}
