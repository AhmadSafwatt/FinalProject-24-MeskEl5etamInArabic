package com.homechef.OrderService.DTOs;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class CartDTO {


        UUID id;
        UUID customerId;
        List<CartItemDTO> cartItems;

        public CartDTO() {
            this.id = UUID.randomUUID();
        }

        public CartDTO(UUID customerId, List<CartItemDTO> cartItems) {
            this.id = UUID.randomUUID();
            this.customerId = customerId;
            this.cartItems = cartItems;
        }

        public CartDTO(UUID id, UUID customerId, List<CartItemDTO> cartItems) {
            this.id = id;
            this.customerId = customerId;
            this.cartItems = cartItems;
        }

}
