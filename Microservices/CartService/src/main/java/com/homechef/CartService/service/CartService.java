package com.homechef.CartService.service;

import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.model.ProductDTO;
import com.homechef.CartService.repository.CartRepository;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private final ProductClient productClient;

    public CartService(ProductClient productClient) {
        this.productClient = productClient;
    }

    public Cart getCartByCustomerId(String customerId) {
        UUID customerUUID = UUID.fromString(customerId);
        return cartRepository.findByCustomerId(customerUUID);
    }

    public Cart getCartById(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        Cart c = cartRepository.findById(cartUUID).orElse(null);

        if (c == null) {
            return null;
        }
        // Fetch product details from Product Service
        for (CartItem item : c.getCartItems()){
            ProductDTO product = productClient.getProductById(item.getProductId().toString());
            item.setProduct(product);
        }
        return c;
    }

    public String deleteCartById(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        if (!cartRepository.existsById(cartUUID))
            return "Cart Not Found";
        cartRepository.deleteById(cartUUID);
        return "Cart Deleted Successfully";
    }

    public String checkoutCartById(String cartId) { // facade design pattern
        Cart cart = findCart(cartId);
        if (cart == null) return "Cart Not Found";

        double totalCost = calculateTotalCost(cart);
        Map<Cart, Double> cartCostMap = prepareCartCostMap(cart, totalCost);

        sendCartToOrderService(cartCostMap);
        clearCart(cart);

        return "Checkout Successful";
    }

    private Cart findCart(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        return cartRepository.findById(cartUUID).orElse(null);
    }

    private double calculateTotalCost(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        double totalCost = 0;
        for (CartItem item : cartItems) {
            ProductDTO product = productClient.getProductById(item.getProductId().toString());
            totalCost += product.getPrice() * item.getQuantity();
        }
        return totalCost;
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
    }

}
