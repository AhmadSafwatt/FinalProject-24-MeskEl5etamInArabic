package com.homechef.CartService.service;
import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.homechef.CartService.model.ProductDTO;
import com.homechef.CartService.repository.CartRepository;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private final ProductClient productClient;

    @Autowired
    private CheckoutFacade checkoutFacade;

    public CartService(ProductClient productClient) {
        this.productClient = productClient;
    }


    public Cart createCart(String customerId) {

        UUID customerIDD = UUID.fromString(customerId);

        if(!(cartRepository.findByCustomerId(customerIDD) == null))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Customer already has a cart");
        Cart cart1=new Cart.Builder()
                .id(UUID.randomUUID())
                .customerId( customerIDD )
                .cartItems(new ArrayList<>())
                .notes("")
                .promo(false)
                .build();
        return cartRepository.save(cart1);
    }

    public Cart addProduct(String customerId , String productID , int quantity , String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);
        ProductDTO product = productClient.getProductById(productID);
        Cart cart = cartRepository.findByCustomerId(customerIDD);

        if(cart == null){
           cart =  createCart(customerId);
        }

        boolean found = false;
        ArrayList<CartItem> newCart = new ArrayList<>();
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if((cart.getCartItems().get(i).getProductId().equals(productIDD))){
                cart.getCartItems().get(i).setQuantity(cart.getCartItems().get(i).getQuantity()+quantity);
                cart.getCartItems().get(i).setNotes(cart.getCartItems().get(i).getNotes() + ", " + notes);
                found = true;
            }
        }
        if(!found){
            cart.getCartItems().add(new CartItem(productIDD, quantity, LocalDateTime.now(), notes, product.getSellerId()));
        }
        return cartRepository.save(cart);
    }

    public Cart addNotesToCartItem(String customerId, String productID, String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);

        Cart cart = cartRepository.findByCustomerId(customerIDD);
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if((cart.getCartItems().get(i).getProductId().equals(productIDD))){
                cart.getCartItems().get(i).setNotes( cart.getCartItems().get(i).getNotes() + ", " + notes);
            }
        }
        return cartRepository.save(cart);
    }

    public Cart removeProduct(String customerId , String productId){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        List<CartItem> newCartItems = new ArrayList<>();
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if(!(cart.getCartItems().get(i).getProductId().equals(productIDD))){
                newCartItems.add(cart.getCartItems().get(i));
            }
        }
        cart.setCartItems(newCartItems);
        return cartRepository.save(cart);
    }

    public Cart updatePromo(String customerId , boolean promo) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart does not exist");
        cart.setPromo(promo);
        return cartRepository.save(cart);
    }

    public Cart updateNotes(String customerId, String notes) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST  , "Cart does not exist");
        cart.setNotes(notes);
        return cartRepository.save(cart);
    }

    public Cart getCartByCustomerId(String customerId) {
        UUID customerUUID = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerUUID);
        if (cart == null) {
            String errorMessage = "Cart not found for customer ID: " + customerId;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        return cart;
    }

    public Cart getCartById(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        Cart c = cartRepository.findById(cartUUID).orElse(null);

        if (c == null) {
            String errorMessage = "Cart not found for cart ID: " + cartId;
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorMessage);
        }
        List<String> ids = new ArrayList<>();
        // Fetch product details from Product Service
        for (CartItem item : c.getCartItems()){
            ids.add(item.getProductId().toString());
        }
        List<ProductDTO> products = productClient.getProductsById(ids);
        // Update cart items with product details
        for (int i = 0; i < c.getCartItems().size(); i++) {
            c.getCartItems().get(i).setProduct(products.get(i));
        }
        return c;
    }

    public String deleteCartByCustomerID(String customerId) {
        UUID customerUUID = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerUUID);
        if (cart == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cart not found for User: %s", customerId));

        cartRepository.delete(cart);
        return "Cart Deleted Successfully";
    }

    public String checkoutCartByCustomerId(String customerId) { // facade design pattern
        return checkoutFacade.execute(customerId);
    }
}
