package com.homechef.OrderService.rabbitmq;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;

@Configuration
public class OrderRabbitMQConfig {
    public static final String CART_QUEUE = "Cart_To_Order_Queue";
    public static final String CART_ORDER_EXCHANGE = "CART_ORDER_XCHANGE";
    public static final String CART_ROUTING = "cart_routing_key";

    public static final String DECREMENT_QUEUE = "product-decrement-queue";
    public static final String PRODUCT_EXCHANGE = "product-exchange";

    public static final String DECREMENT_ROUTING_KEY = "product.decrement";

    @Bean
    public Queue cartQueue() {
        return new Queue(CART_QUEUE);
    }

    @Bean
    public Queue decrementQueue() { return  new Queue(DECREMENT_QUEUE); }


    @Bean
    public TopicExchange cartExchange() {
        return new TopicExchange(CART_ORDER_EXCHANGE);
    }

    @Bean
    public TopicExchange productExchange() { return new TopicExchange(PRODUCT_EXCHANGE); }


    @Bean
    public Binding cartBinding(Queue cartQueue, TopicExchange cartExchange) {
        return BindingBuilder.bind(cartQueue).to(cartExchange).with(CART_ROUTING);
    }

    @Bean
    public Binding decrementBinding(Queue decrementQueue, TopicExchange productExchange) {
        return BindingBuilder.bind(decrementQueue).to(productExchange).with(DECREMENT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // No default typing, no trusted packages, no type mapping

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter orderMessageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(orderMessageConverter);
        return factory;
    }
}