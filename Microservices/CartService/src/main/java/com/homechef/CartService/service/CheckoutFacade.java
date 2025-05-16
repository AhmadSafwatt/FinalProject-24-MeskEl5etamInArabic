package com.homechef.CartService.service;

import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.model.ProductDTO;
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
    public CheckoutFacade(ProductClient productClient) {
        this.productClient = productClient;
    }

    public String execute(String cartId) { // facade design pattern
        Cart cart = findCart(cartId);

        double totalCost = calculateTotalCost(cart);

        sendCartToOrderService(prepareCartCostMap(cart, totalCost));

        clearCart(cart);

        return "Checkout Successful";
    }

    private Cart findCart(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        Cart c = cartRepository.findById(cartUUID).orElse(null);
        if (c == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cart not found for cart ID: %s", cartId));

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
