package com.homechef.CartService.controller;

import com.homechef.CartService.config.JwtUtil;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.homechef.CartService.config.JwtUtil.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/carts")
public class CartController {

    @Autowired
    private CartService cartService;

    private JwtUtil jwtUtil = JwtUtil.getInstance();

    @PostMapping("/createCart")
    public Cart createCart(@RequestHeader("Authorization") String authHeader){
        String jwt = authHeader.replace("Bearer ", "");
        return cartService.createCart(jwtUtil.getUserClaims(jwt).get("id").toString());
        // "id", "username", "email", "address", "phoneNumber", "role"
    }

    @PutMapping("/addProduct")
    public Cart addProduct(  @RequestHeader("Authorization") String authHeader  , @RequestBody Map<String, Object> payload) {
        String jwt = authHeader.replace("Bearer " , "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();

        String productID = (String) payload.get("productID");
        int quantity = (int) payload.get("quantity");
        String notes = (String) payload.get("notes");

        return cartService.addProduct(customerId, productID, quantity, notes);
    }


    @PutMapping("/{productId}/addNotesToCartItem")
    public Cart addNotesToCartItem( @RequestHeader("Authorization") String authHeader , @PathVariable String productId , @RequestBody Map<String, String> body){
        String jwt = authHeader.replace("Bearer " , "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        return cartService.addNotesToCartItem(customerId, productId, body.get("notes"));
    }

    @PutMapping("/{productId}/removeProduct")
    public Cart removeProduct(@RequestHeader("Authorization") String authHeader , @PathVariable String productId ){
        String jwt = authHeader.replace("Bearer " , "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        return cartService.removeProduct(customerId , productId);
    }

    @PutMapping("/updatePromo")
    public Cart updateCartPromo(@RequestHeader("Authorization") String authHeader, @RequestBody Map<String, Boolean> payload){
        String jwt = authHeader.replace("Bearer " , "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        return cartService.updatePromo(customerId , payload.get("promo"));
    }

    @PutMapping("/updateNotes")
    public Cart updateCartNotes( @RequestHeader("Authorization") String authHeader, @RequestBody HashMap<String, String> payload) {
        String jwt = authHeader.replace("Bearer " , "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        String notes = payload.get("notes");
        return cartService.updateNotes(customerId, notes);
    }


    @GetMapping("/getCart")
    public Cart getCartByCustomerId(@RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.replace("Bearer ", "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        return cartService.getCartByCustomerId(customerId);
    }

    @GetMapping("/{cartId}/cartId")
    public Cart getCartById(@PathVariable String cartId , @RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.replace("Bearer ", "");
        String customerId = jwtUtil.getUserClaims(jwt).get("id").toString();
        return cartService.getCartById(cartId, customerId);
    }

    @DeleteMapping
    public ResponseEntity<String> deleteCart(@RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(cartService.deleteCartByCustomerID(jwtUtil.getUserClaims(jwt).get("id").toString()));
    }

    @PostMapping("/checkout")
    public ResponseEntity<String> checkout(@RequestHeader("Authorization") String authHeader) {
        String jwt = authHeader.replace("Bearer ", "");
        return ResponseEntity.ok(cartService.checkoutCartByCustomerId(jwtUtil.getUserClaims(jwt).get("id").toString()));
    }
}
