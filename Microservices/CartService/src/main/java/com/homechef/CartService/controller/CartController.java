package com.homechef.CartService.controller;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;


    @PostMapping("/{customerId}/createCart")
    public Cart createCart(@PathVariable String customerId){
        return cartService.createCart( customerId );
    }

//    @PutMapping("/updateCart/{cartID}")
//    public Cart updateCart(@PathVariable String cartID , @RequestBody Cart cart){
//        return cartService.updateCart(cartID , cart);
//
//    }

    @PutMapping("/{customerId}/addProduct")
    public Cart addProduct(@PathVariable String customerId, @RequestBody Map<String, Object> payload) {
        String productID = (String) payload.get("productID");
        int quantity = (int) payload.get("quantity");
        String notes = (String) payload.get("notes");

        return cartService.addProduct(customerId, productID, quantity, notes);
    }


    @PutMapping("/{customerId}/{productId}/addNotesToCartItem")
    public Cart addNotesToCartItem(@PathVariable String customerId , @PathVariable String productId , @RequestBody Map<String, String> body){
        return cartService.addNotesToCartItem(customerId, productId, body.get("notes"));
    }

    @PutMapping("/{customerId}/{productId}/removeProduct")
    public Cart removeProduct(@PathVariable String customerId , @PathVariable String productId ){
        return cartService.removeProduct(customerId , productId);
    }

    @PutMapping("/{customerId}/updatePromo")
    public Cart updateCartPromo(@PathVariable String customerId, @RequestBody Map<String, Boolean> payload){
        return cartService.updatePromo(customerId , payload.get("promo"));
    }

    @PutMapping("/{customerId}/updateNotes")
    public Cart updateCartNotes(@PathVariable String customerId, @RequestBody HashMap<String, String> payload) {
        String notes = payload.get("notes");
        return cartService.updateNotes(customerId, notes);
    }


    @GetMapping("/customerId/{customerId}")
    public Cart getCartByCustomerId(@PathVariable String customerId) {
        return cartService.getCartByCustomerId(customerId);
    }

    @GetMapping("/{cartId}")
    public Cart getCartById(@PathVariable String cartId) {
        return cartService.getCartById(cartId);
    }

    @DeleteMapping("/{cartId}")
    public ResponseEntity<String> deleteCart(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.deleteCartById(cartId));
    }

    @PostMapping("/{cartId}/checkout")
    public ResponseEntity<String> checkout(@PathVariable String cartId) {
        return ResponseEntity.ok(cartService.checkoutCartById(cartId));
    }
}
