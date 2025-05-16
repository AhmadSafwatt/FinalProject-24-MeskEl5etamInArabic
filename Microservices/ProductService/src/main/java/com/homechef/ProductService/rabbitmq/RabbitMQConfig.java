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
@Configuration
public class RabbitMQConfig {

    public static final String PRODUCT_QUEUE = "product-queue";
    public static final String PRODUCT_EXCHANGE = "product-exchange";
    public static final String PRODUCT_ROUTING_KEY = "product-routing-key";
   // public static final String PRODUCT_ROUTING_KEY = "product-routing-key";


    @Bean
    public Queue queue() {
        return new Queue(PRODUCT_QUEUE);
    }
    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(PRODUCT_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(PRODUCT_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }


}
