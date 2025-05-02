package com.homechef.CartService.repository;

import com.homechef.CartService.model.Cart;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartRepository extends MongoRepository<Cart, UUID> {

    Cart findByCustomer_id(UUID userId);

}
