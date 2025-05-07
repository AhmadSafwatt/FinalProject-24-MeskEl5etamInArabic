// package com.homechef.CartService.seed;

// import com.homechef.CartService.model.Cart;
// import com.homechef.CartService.model.CartItem;
// import com.homechef.CartService.repository.CartRepository;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.UUID;

// @Component
// public class DatabaseSeeder {

//     private final CartRepository cartRepository;

//     public DatabaseSeeder(CartRepository cartRepository) {
//         this.cartRepository = cartRepository;
//     }

//     public void seed() {
//         // Delete existing data
//         cartRepository.deleteAll();

//         // Create sample cart items for first cart
//         List<CartItem> items1 = new ArrayList<>();
//         items1.add(new CartItem(
//             UUID.randomUUID(),  // Product ID
//             2,
//             LocalDateTime.now(),
//             UUID.randomUUID(),  // Seller ID
//             "Extra spicy"
//         ));
//         items1.add(new CartItem(
//             UUID.randomUUID(),
//             1,
//             LocalDateTime.now(),
//             UUID.randomUUID(),
//             "No onions"
//         ));

//         // Create first cart using setters
//         Cart cart1 = new Cart();
//         cart1.setCustomerId(UUID.randomUUID());
//         cart1.setCartItems(items1);

//         // Create sample cart items for second cart
//         List<CartItem> items2 = new ArrayList<>();
//         items2.add(new CartItem(
//             UUID.randomUUID(),
//             3,
//             LocalDateTime.now(),
//             UUID.randomUUID(),
//             "Medium rare"
//         ));

//         // Create second cart using setters
//         Cart cart2 = new Cart();
//         cart2.setCustomerId(UUID.randomUUID());
//         cart2.setCartItems(items2);

//         // Save carts to database
//         cartRepository.saveAll(List.of(cart1, cart2));
//     }
// }
