package com.homechef.CartService.seed;

import com.homechef.CartService.model.Cart;
import com.homechef.CartService.model.CartItem;
import com.homechef.CartService.repository.CartRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class DatabaseSeeder {

    private final CartRepository cartRepository;

    public DatabaseSeeder(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public void seed() {
        // Delete existing data
        cartRepository.deleteAll();

        // Create sample cart items for first cart
        List<CartItem> items1 = new ArrayList<>();
        items1.add(new CartItem(
                UUID.fromString("da85f125-3a17-4eab-a28f-a8ec2c9e18d8"),  // Pizza Product ID
                1,
                LocalDateTime.now(),
                "Extra spicy and sauce",      // notes
                UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13")   // Seller1 ID
        ));
        items1.add(new CartItem(
                UUID.fromString("16b9eb22-301d-4ee9-bb21-88d9f52d08e0"), // Cola Product ID
                2,
                LocalDateTime.now(),
                "No ice",
                UUID.fromString("5a1d1902-8cad-4810-9621-8cb1ded5ff13")   // Seller1 ID
        ));

        // Create first cart
        Cart cart1=new Cart.Builder()
                .id(UUID.randomUUID())
                .customerId( UUID.fromString("ae73c3fd-f444-4a54-9822-fa24ab6747c3") )
                .cartItems(items1)
                .notes("This is the first cart")
                .promo(false)
                .build();
//        cart1.setCustomerId(UUID.fromString("ae73c3fd-f444-4a54-9822-fa24ab6747c3")); // Customer1 ID
//        cart1.setCartItems(items1);

        // Create sample cart items for second cart
        List<CartItem> items2 = new ArrayList<>();
        items2.add(new CartItem(
                UUID.fromString("0248f6fe-33a9-4585-b23f-e7ddf7aab32f"), // Hot Chocolate Product ID
                3,
                LocalDateTime.now(),
                "Extra sugar",
                UUID.fromString("e24f4ca0-d6d5-4a91-b453-45dde29067d5")   // Seller2 ID
        ));

        // Create second cart
        Cart cart2=new Cart.Builder()
                .id(UUID.randomUUID())
                .customerId(UUID.fromString("2f3dc195-587d-4a6a-8da1-b0587c4a2310"))
                .cartItems(items2)
                .notes("This is the second cart")
                .promo(true)
                .build();
//        cart2.setCustomerId(UUID.fromString("2f3dc195-587d-4a6a-8da1-b0587c4a2310")); // Customer2 ID
//        cart2.setCartItems(items2);

        // Save carts to database
        cartRepository.saveAll(List.of(cart1, cart2));
    }
}