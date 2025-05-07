package com.homechef.CartService.service;

import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;
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


    public Cart createCart(UUID customerId) {
        Cart cart1=new Cart.Builder()
                .id(UUID.randomUUID())
                .customerId( customerId )
                .cartItems(new ArrayList<>())
                .notes("")
                .promo(false)
                .build();
        return cartRepository.save(cart1);
    }

//    public Cart updateCart(String cartID, Cart cart) {
//        UUID cartid = UUID.fromString(cartID);
//        cart.setId(cartid);
//        return cartRepository.save(cart);
//    }

//    public Cart addProduct(UUID customerId , ){
//
//    }


    public Cart removeProduct(UUID customerId , UUID productId){
        Cart cart = cartRepository.findByCustomerId(customerId);
        List<CartItem> newCartItems = new ArrayList<>();
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if(!(cart.getCartItems().get(i).getProductId().equals(productId))){
                newCartItems.add(cart.getCartItems().get(i));
            }
        }
        cart.setCartItems(newCartItems);
        return cartRepository.save(cart);
    }

    public Cart updatePromo(String cartID , boolean promo) {
        Cart customerCart = getCartById(cartID);
        Cart newCart = new Cart.Builder().from(customerCart).promo(promo).build();
        return cartRepository.save(newCart);
    }

    public Cart updateNotes(String cartID, String notes) {
        Cart customerCart = getCartById(cartID);
        Cart newCart = new Cart.Builder().from(customerCart).notes(notes).build();
        return cartRepository.save(newCart);
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
