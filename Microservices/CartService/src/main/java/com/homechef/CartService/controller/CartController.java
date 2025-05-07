package com.homechef.CartService.controller;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.service.CartService;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("/createCart/{customerId}")
    public Cart createCart(@PathVariable String customerId){
        return cartService.createCart(UUID.fromString(customerId));
    }

//    @PutMapping("/updateCart/{cartID}")
//    public Cart updateCart(@PathVariable String cartID , @RequestBody Cart cart){
//        return cartService.updateCart(cartID , cart);
//
//    }

    @PutMapping("/removeProduct/{customerId}/{productId}")
    public Cart updatePromo(@PathVariable String customerId , @PathVariable String productId ){
        return cartService.removeProduct(UUID.fromString(customerId) , UUID.fromString(productId));
    }

    @PutMapping("/updatePromo/{cartID}")
    public Cart updatePromo(@PathVariable String cartID , boolean promo){
        return cartService.updatePromo(cartID , promo);
    }

    @PutMapping("/updateNotes/{cartID}")
    public Cart updateNotes(@PathVariable String cartID , String notes){
        return cartService.updateNotes(cartID , notes);
    }


    @GetMapping("/customerId/{customerId}")
    public Cart getCartByCustomerId(@PathVariable String customerId) {
        return cartService.getCartByCustomerId(customerId);
    }

    @GetMapping("/{cartId}")
    public Cart getCartById(@PathVariable String cartId) {
        return cartService.getCartById(cartId);
    }
}
