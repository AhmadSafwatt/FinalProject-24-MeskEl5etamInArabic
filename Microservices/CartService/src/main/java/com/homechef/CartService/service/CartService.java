package com.homechef.CartService.service;

import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.repository.CartRepository;

import java.util.UUID;

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


    public Cart createCart(Cart cart) {
        return cartRepository.save(cart);
    }

    public Cart updateCart(String cartID, Cart cart) {
        UUID cartid = UUID.fromString(cartID);
        cart.setId(cartid);
        return cartRepository.save(cart);
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
        return c;
    }



}
