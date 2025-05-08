package com.homechef.OrderService.rabbitmq;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.homechef.OrderService.DTOs.CartDTO;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;


import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    public static final String CART_QUEUE = "Cart_To_Order_Queue";
    public static final String EXCHANGE = "CART_ORDER_XCHANGE";
    public static final String CART_ROUTING = "cart_routing_key";

    @Bean
    public Queue queue() {
        return new Queue(CART_QUEUE);
    }

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(CART_ROUTING);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);

        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper() {
            @Override
            public JavaType toJavaType(MessageProperties properties) {
                // Override type mapping for receiver
                if ("cartMap".equals(properties.getHeader("__TypeId__"))) {
                    return objectMapper.getTypeFactory().constructMapType(
                            HashMap.class,
                            CartDTO.class,  // Convert to CartDTO on receive
                            Double.class
                    );
                }
                return super.toJavaType(properties);
            }
        };

        typeMapper.setTrustedPackages("com.homechef.OrderService.models", "com.homechef.OrderService.DTOs", "java.util");

        // Original mapping for sender
        Map<String, Class<?>> idClassMapping = new HashMap<>();
        idClassMapping.put("cartMap", HashMap.class);
        typeMapper.setIdClassMapping(idClassMapping);

        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}