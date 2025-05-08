package com.homechef.CartService.rabbitmq;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CartRabbitMQProducer {

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public CartRabbitMQProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendCart(Cart cart, Double price) {
        CartMessage message = new CartMessage();
        message.setCart(cart);
        message.setTotalPrice(price);

        System.out.println("Sending message to RabbitMQ: " + message);
        rabbitTemplate.convertAndSend(
                CartRabbitMQConfig.EXCHANGE,
                CartRabbitMQConfig.CART_ROUTING,
                message
        );
    }
}
