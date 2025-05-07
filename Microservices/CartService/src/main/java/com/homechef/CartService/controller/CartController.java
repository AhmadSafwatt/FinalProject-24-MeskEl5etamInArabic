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


    @PostMapping("/{customerId}/createCart")
    public Cart createCart(@PathVariable String customerId){
        return cartService.createCart(UUID.fromString(customerId));
    }

//    @PutMapping("/updateCart/{cartID}")
//    public Cart updateCart(@PathVariable String cartID , @RequestBody Cart cart){
//        return cartService.updateCart(cartID , cart);
//
//    }

    @PutMapping("/{customerId}/addProduct")
    public Cart addProduct(@PathVariable String customerId , @RequestBody UUID productID , @RequestBody int quantity , @RequestBody String notes ){
        return cartService.addProduct(UUID.fromString(customerId) , productID , quantity , notes);
    }

    @PutMapping("/{customerId}/{productId}/removeProduct")
    public Cart removeProduct(@PathVariable String customerId , @PathVariable String productId ){
        return cartService.removeProduct(UUID.fromString(customerId) , UUID.fromString(productId));
    }

    @PutMapping("/{cartID}/updatePromo")
    public Cart updateCartPromo(@PathVariable String customerId ,@RequestBody boolean promo){
        return cartService.updatePromo(UUID.fromString(customerId) , promo);
    }

    @PutMapping("/{cartID}/updateNotes")
    public Cart updateCartNotes(@PathVariable String customerId , @RequestBody String notes){
        return cartService.updateNotes(UUID.fromString(customerId) , notes);
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
