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


    public Cart createCart(String customerId) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart1=new Cart.Builder()
                .id(UUID.randomUUID())
                .customerId( customerIDD )
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

//    public Cart addProduct(String customerId , String productID , int quantity , String notes){
//        UUID customerIDD = UUID.fromString(customerId);
//        UUID productIDD = UUID.fromString(productID);
//        Cart cart = cartRepository.findByCustomerId(customerIDD);
//        CartItem cartItem = new CartItem(productIDD , quantity , LocalDateTime.now() , notes , UUID.randomUUID());
//        List <CartItem> oldCartItems = cart.getCartItems();
//        oldCartItems.add(cartItem);
//        cart.setCartItems(oldCartItems);
//        return cartRepository.save(cart);
//    }

    public Cart addProduct(String customerId , String productID , int quantity , String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);
        Cart cart = cartRepository.findByCustomerId(customerIDD);

        if(cart == null){
           cart =  createCart(customerId);
        }

        boolean found = false;
        ArrayList<CartItem> newCart = new ArrayList<>();
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if((cart.getCartItems().get(i).getProductId().equals(productIDD))){
                cart.getCartItems().get(i).setQuantity(cart.getCartItems().get(i).getQuantity()+quantity);
                cart.getCartItems().get(i).setNotes(cart.getCartItems().get(i).getNotes() + notes);
                found = true;
            }
        }
        if(!found){
            CartItem cartItem = new CartItem(productIDD , quantity , LocalDateTime.now() , notes , UUID.randomUUID());
            List <CartItem> oldCartItems = cart.getCartItems();
            oldCartItems.add(cartItem);
            cart.setCartItems(oldCartItems);
        }
        return cartRepository.save(cart);
    }


    public Cart addNotesToCartItem(String customerId,String productID , String notes){
        UUID customerIDD = UUID.fromString(customerId);
        UUID productIDD = UUID.fromString(productID);

        Cart cart = cartRepository.findByCustomerId(customerIDD);
        for(int i = 0 ; i< cart.getCartItems().size() ; i++){
            if((cart.getCartItems().get(i).getProductId().equals(productIDD))){
                cart.getCartItems().get(i).setNotes( cart.getCartItems().get(i).getNotes() + notes);
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
        cart.setPromo(promo);
        return cartRepository.save(cart);
    }


//    public Cart updatePromo(String cartID , boolean promo) {
//        Cart customerCart = getCartById(cartID);
//        Cart newCart = new Cart.Builder().from(customerCart).promo(promo).build();
//        return cartRepository.save(newCart);
//    }



    public Cart updateNotes(String customerId, String notes) {
        UUID customerIDD = UUID.fromString(customerId);
        Cart cart = cartRepository.findByCustomerId(customerIDD);
        cart.setNotes(notes);
        return cartRepository.save(cart);
    }

//    public Cart updateNotes(String cartID, String notes) {
//        Cart customerCart = getCartById(cartID);
//        Cart newCart = new Cart.Builder().from(customerCart).notes(notes).build();
//        return cartRepository.save(newCart);
//    }

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
        for (CartItem item : c.getCartItems()){
            ProductDTO product = productClient.getProductById(item.getProductId().toString());
            item.setProduct(product);
        }
        return c;
    }


    public String deleteCartById(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        if (!cartRepository.existsById(cartUUID))
            return "Cart Not Found";
        cartRepository.deleteById(cartUUID);
        return "Cart Deleted Successfully";
    }

    public String checkoutCartById(String cartId) { // facade design pattern
        Cart cart = findCart(cartId);
        if (cart == null) return "Cart Not Found";

        double totalCost = calculateTotalCost(cart);
        Map<Cart, Double> cartCostMap = prepareCartCostMap(cart, totalCost);

        sendCartToOrderService(cartCostMap);
        clearCart(cart);

        return "Checkout Successful";
    }

    private Cart findCart(String cartId) {
        UUID cartUUID = UUID.fromString(cartId);
        return cartRepository.findById(cartUUID).orElse(null);
    }

    private double calculateTotalCost(Cart cart) {
        List<CartItem> cartItems = cart.getCartItems();
        double totalCost = 0;
        for (CartItem item : cartItems) {
            ProductDTO product = productClient.getProductById(item.getProductId().toString());
            totalCost += product.getPrice() * item.getQuantity();
        }
        return totalCost;
    }

    private Map<Cart, Double> prepareCartCostMap(Cart cart, double totalCost) {
        Map<Cart, Double> cartCostMap = new HashMap<>();
        cartCostMap.put(cart, totalCost);
        return cartCostMap;
    }

    private void clearCart(Cart cart) {
        cart.setCartItems(new ArrayList<>());
        cartRepository.save(cart);
    }

    private void sendCartToOrderService(Map<Cart, Double> cartCostMap) {
        //orderService.sendCartCheckout(cartCostMap); // Async via RabbitMQ
    }


}
