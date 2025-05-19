package com.homechef.CartService.rabbitmq;

import com.homechef.CartService.DTO.ProductMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProductRabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public ProductRabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void send(UUID productId, int quantity) {
        ProductMessage productMessage = new ProductMessage();
        productMessage.setProductId(productId);
        productMessage.setAmount(quantity);

        System.out.println("Sending Product Message to Product Service using RabbitMQ: " + productMessage);
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.PRODUCT_EXCHANGE,
                RabbitMQConfig.INCREMENT_ROUTING_KEY,
                productMessage
        );
    }
}
