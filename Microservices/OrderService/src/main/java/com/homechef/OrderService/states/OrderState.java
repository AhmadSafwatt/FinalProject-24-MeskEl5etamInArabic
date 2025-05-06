package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderStatus;

import java.util.UUID;

// CancelledState,,, CreatedState > PreparingState > PreparedState > OutForDeliveryState > DeliveredState
public interface OrderState {

    default void cancelOrder(Order order) {
        throw new IllegalStateException("Order is in "+ order.getState().getClass().getSimpleName() + " state and cannot be cancelled");
    }
    void setOrderState(Order order, OrderState state);

    default void updateItemNote(Order order, UUID productId, String note) {
        throw new IllegalStateException("Cannot update item note in " + order.getState().getClass().getSimpleName() + " state");
    }

    default OrderStatus getOrderStatus() {
        throw new IllegalArgumentException("Unknown order state: " + this.getClass().getSimpleName());
    }
}
