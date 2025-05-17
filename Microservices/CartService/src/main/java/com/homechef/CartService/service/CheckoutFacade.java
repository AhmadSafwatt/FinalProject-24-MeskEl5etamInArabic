package com.homechef.CartService.service;

import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.model.ProductDTO;
import com.homechef.CartService.rabbitmq.ProductRabbitMQProducer;
import com.homechef.CartService.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class CheckoutFacade {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private final ProductClient productClient;
    @Autowired
    private final ProductRabbitMQProducer productRabbitMQProducer;
    public CheckoutFacade(ProductClient productClient, ProductRabbitMQProducer productRabbitMQProducer) {
        this.productRabbitMQProducer = productRabbitMQProducer;
        this.productClient = productClient;
    }



    public String execute(String customerId) { // facade design pattern
        Cart cart = findCart(customerId);

        double totalCost = calculateTotalCost(cart);

        sendProductsToProductsService(cart);

        sendCartToOrderService(prepareCartCostMap(cart, totalCost));

        clearCart(cart);

        return "Checkout Successful";
    }

    private Cart findCart(String customerId) {
        UUID customerUUID = UUID.fromString(customerId);
        Cart c = cartRepository.findByCustomerId(customerUUID);
        if (c == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cart not found for User ID: %s", customerUUID));

        return c;
    }

    private double calculateTotalCost(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart is empty");

        double totalCost = 0;
        List<String> ids = new ArrayList<>();
        for (CartItem item : cartItems) {
            ids.add(item.getProductId().toString());
        }
        List<ProductDTO> products = productClient.getProductsById(ids);
        for (int i = 0; i < cartItems.size(); i++) {
            if (products.get(i) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No product exists with ID: %s", products.get(i).getId()));

            totalCost += (products.get(i).getPrice() * (1 - products.get(i).getDiscount())) * cartItems.get(i).getQuantity();
        }
        if (cart.isPromo())
            totalCost = totalCost - 0.05*totalCost;
        return totalCost;
    }

    private void sendProductsToProductsService(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();

        for (CartItem cartItem : cartItems) {
            productRabbitMQProducer.send(cartItem.getProductId(), cartItem.getQuantity());
        }
    }

    private Map<Cart, Double> prepareCartCostMap(Cart cart, double totalCost) {
        Map<Cart, Double> cartCostMap = new HashMap<>();
        cartCostMap.put(cart, totalCost);
        return cartCostMap;
    }

    private void clearCart(Cart cart) {
        cart.setCartItems(new ArrayList<>());
        cartRepository.save(cart);
    }

    private void sendCartToOrderService(Map<Cart, Double> cartCostMap) {
        //orderService.sendCartCheckout(cartCostMap); // Async via RabbitMQ
        System.out.println("SENT TO ORDER SERVICE");
    }
}
