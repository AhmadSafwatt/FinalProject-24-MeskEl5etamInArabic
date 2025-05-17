package com.homechef.OrderService.rabbitmq;

import com.homechef.OrderService.DTOs.ProductMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public RabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendProductDecrement(UUID productId, int quantity) {
        ProductMessage productMessage = new ProductMessage();
        productMessage.setProductId(productId);
        productMessage.setAmount(quantity);

        System.out.println("Sending Product Message to Product Service using RabbitMQ: " + productMessage);
        rabbitTemplate.convertAndSend(
                OrderRabbitMQConfig.PRODUCT_EXCHANGE,
                OrderRabbitMQConfig.DECREMENT_ROUTING_KEY,
                productMessage
        );
    }
}