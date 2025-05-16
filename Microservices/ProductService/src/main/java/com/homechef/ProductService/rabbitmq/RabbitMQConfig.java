package com.homechef.ProductService.rabbitmq;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;

@Configuration
public class RabbitMQConfig {

    public static final String INCREMENT_QUEUE = "product-increment-queue";
    public static final String DECREMENT_QUEUE = "product-decrement-queue";
    public static final String PRODUCT_EXCHANGE = "product-exchange";

    public static final String INCREMENT_ROUTING_KEY = "product.increment";
    public static final String DECREMENT_ROUTING_KEY = "product.decrement";


    @Bean
    public Queue incrementQueue() {
        return new Queue(INCREMENT_QUEUE);
    }

    @Bean
    public Queue decrementQueue() {
        return new Queue(DECREMENT_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    @Bean
    public Binding incrementBinding(Queue incrementQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(incrementQueue)
                .to(exchange)
                .with(INCREMENT_ROUTING_KEY);
    }

    @Bean
    public Binding decrementBinding(Queue decrementQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(decrementQueue)
                .to(exchange)
                .with(DECREMENT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter productMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // No default typing, no trusted packages, no type mapping

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter productMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(productMessageConverter);
        return factory;
    }





}
