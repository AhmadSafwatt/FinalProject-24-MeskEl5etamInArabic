package com.homechef.CartService.rabbitmq;

import org.springframework.context.annotation.Configuration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


@Configuration
public class CartRabbitMQConfig {

    public static final String CART_QUEUE = "Cart_To_Order_Queue";
    public static final String EXCHANGE = "CART_ORDER_XCHANGE";
    public static final String CART_ROUTING = "cart_routing_key";

    public static final String REORDERING_QUEUE = "reordering-queue";
    public static final String REORDERING_ROUTING = "reordering-routing-key";

    @Bean
    public Queue cartQueue() {
        return new Queue(CART_QUEUE);
    }

    @Bean
    public Queue reorderingQueue() { return new Queue(REORDERING_QUEUE); }

    @Bean
    public TopicExchange cartExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding cartBinding() {
        return BindingBuilder.bind(cartQueue()).to(cartExchange()).with(CART_ROUTING);
    }

    @Bean
    public Binding reorderingBinding() { return BindingBuilder.bind(reorderingQueue()).to(cartExchange()).with(REORDERING_ROUTING); }

    @Bean
    public Jackson2JsonMessageConverter cartMessageConverter() {
        ObjectMapper mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());
        // no default typing, no type info

        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter cartMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(cartMessageConverter);
        return template;
    }
}