package com.homechef.CartService.service;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.repository.CartRepository;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    public Cart getCartByCustomerId(UUID userId) {
        return cartRepository.findByCustomer_Id(userId);
    }
}
