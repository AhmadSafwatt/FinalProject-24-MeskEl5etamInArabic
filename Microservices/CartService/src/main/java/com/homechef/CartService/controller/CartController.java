package com.homechef.CartService.controller;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


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
    public Cart addProduct(@PathVariable String customerId , @RequestBody String productID , @RequestBody int quantity , @RequestBody String notes ){
        return cartService.addProduct(customerId , productID , quantity , notes);
    }

    @PutMapping("/{customerId}/{productId}/addNotesToCartItem")
    public Cart addNotesToCartItem(@PathVariable String customerId , @PathVariable String productId , @RequestBody String notes){
        return cartService.addNotesToCartItem(customerId, productId, notes);
    }

    @PutMapping("/{customerId}/{productId}/removeProduct")
    public Cart removeProduct(@PathVariable String customerId , @PathVariable String productId ){
        return cartService.removeProduct(customerId , productId);
    }

    @PutMapping("/{cartID}/updatePromo")
    public Cart updateCartPromo(@PathVariable String customerId ,@RequestBody boolean promo){
        return cartService.updatePromo(customerId , promo);
    }

    @PutMapping("/{cartID}/updateNotes")
    public Cart updateCartNotes(@PathVariable String customerId , @RequestBody String notes){
        return cartService.updateNotes(customerId , notes);
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
    public String deleteCart(@PathVariable String cartId) {
        return cartService.deleteCartById(cartId);
    }

    @PostMapping("/{cartId}/checkout")
    public String checkout(@PathVariable String cartId) {
        return cartService.checkoutCartById(cartId);
    }
}
