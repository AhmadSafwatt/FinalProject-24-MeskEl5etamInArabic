package com.homechef.CartService.service;
import com.homechef.CartService.DTO.CartMessage;
import com.homechef.CartService.client.ProductClient;
import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.homechef.CartService.model.ProductDTO;
import com.homechef.CartService.rabbitmq.CartRabbitMQConfig;
import com.homechef.CartService.repository.CartRepository;
import java.util.*;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
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

    @Autowired
    private CacheManager cacheManager;

    public CartService(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CachePut(value = "cartCache", key = "#result.id")
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
        cacheManager.getCache("user_cart_map").put(customerId, cart1.getId().toString());
        return cartRepository.save(cart1);
    }

    @CachePut(value = "cartCache", key = "#result.id")
    public Cart addProduct(String customerId , String productID , int quantity , String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);
        ProductDTO product = productClient.getProductById(productID);
        if(product == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
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

    @CachePut(value = "cartCache", key = "#result.id")
    public Cart addNotesToCartItem(String customerId, String productID, String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);

        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart does not exist");
        boolean found = false;
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if((cart.getCartItems().get(i).getProductId().equals(productIDD))){
                cart.getCartItems().get(i).setNotes( cart.getCartItems().get(i).getNotes() + ", " + notes);
                found = true;
            }
        }
        if(!found)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart");
        return cartRepository.save(cart);
    }

    @CachePut(value = "cartCache", key = "#result.id")
    public Cart removeProduct(String customerId , String productId){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart does not exist");
        List<CartItem> newCartItems = new ArrayList<>();
        // boolean found = false;
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if(!(cart.getCartItems().get(i).getProductId().equals(productIDD))){
                newCartItems.add(cart.getCartItems().get(i));
                // found = true;
            }
        }
        if(newCartItems.size() == cart.getCartItems().size())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found in cart");
        cart.setCartItems(newCartItems);
        return cartRepository.save(cart);
    }

    @CachePut(value = "cartCache", key = "#result.id")
    public Cart updatePromo(String customerId , boolean promo) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cart does not exist");
        cart.setPromo(promo);
        return cartRepository.save(cart);
    }

    @CachePut(value = "cartCache", key = "#result.id")
    public Cart updateNotes(String customerId, String notes) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        if(cart == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST  , "Cart does not exist");
        cart.setNotes(notes);
        return cartRepository.save(cart);
    }

    public Cart getCartByCustomerId(String customerId) {
        String cartId = cacheManager.getCache("user_cart_map").get(customerId, String.class);
        if (cartId == null) {
            // If the cart ID is not in the cache, fetch it from the database
            Cart cart = cartRepository.findByCustomerId(UUID.fromString(customerId));
            if (cart == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found for customer ID: " + customerId);
            }
            cartId = cart.getId().toString();
            // Store the cart ID in the cache for future use
            cacheManager.getCache("user_cart_map").put(customerId, cartId);
            cacheManager.getCache("cartCache").put(cartId, cart);
        }
        Cart c = cacheManager.getCache("cartCache").get(cartId, Cart.class);
        return getFullProduct(c);
    }

    private Cart getFullProduct (Cart cart){
        List<String> ids = new ArrayList<>();
        for (CartItem item : cart.getCartItems()){
            ids.add(item.getProductId().toString());
        }
        List<ProductDTO> products = new ArrayList<>();
        if (!ids.isEmpty())
            products = productClient.getProductsById(ids);
        for (int i = 0; i < cart.getCartItems().size(); i++) {
             if (products.get(i) == null)
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("No product exists with ID: %s", products.get(i).getId()));
            cart.getCartItems().get(i).setProduct(products.get(i));
        }
        return cart;
    }

    public String deleteCartByCustomerID(String customerId) {
        UUID customerUUID = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerUUID);
        if (cart == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Cart not found for User: %s", customerId));

        cacheManager.getCache("cartCache").evict(cart.getId());
        cacheManager.getCache("user_cart_map").evict(customerId);

        cartRepository.delete(cart);
        return "Cart Deleted Successfully";
    }

    public String checkoutCartByCustomerId(String customerId) { // facade design pattern
        return checkoutFacade.execute(customerId);
    }


    // TODO: check if the logic is correct and respects the current design
    @RabbitListener(queues = CartRabbitMQConfig.REORDERING_QUEUE)
    public void receiveReOrderingCartMessage(CartMessage cartMsg) {
        Cart cart = cartMsg.getCart();
        System.out.println("Received cart message: " + cart);
        Cart c = cartRepository.findByCustomerId(cart.getCustomerId());
        if (c == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cart not found for customer ID: " + cart.getCustomerId());
        }
        List<CartItem> existingCartItems = cart.getCartItems();

        for (CartItem item : existingCartItems) {
            boolean found = false; // to not use duplicate items
            for (CartItem existingItem : c.getCartItems()) {
                if (item.getProductId().equals(existingItem.getProductId())) {
                    existingItem.setQuantity(existingItem.getQuantity() + item.getQuantity());
                    found = true;
                    break;
                }
            }
            if (!found) {
                item.setDateAdded(LocalDateTime.now());
                c.getCartItems().add(item);
            }
        }

        cartRepository.save(c);

    }

}
