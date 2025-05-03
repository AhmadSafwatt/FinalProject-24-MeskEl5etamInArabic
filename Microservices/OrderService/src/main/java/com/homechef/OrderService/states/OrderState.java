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

    static OrderStatus getOrderStatus(OrderState orderState) {
        if (orderState instanceof CreatedState) {
            return OrderStatus.CREATED;
        } else if (orderState instanceof PreparingState) {
            return OrderStatus.PREPARING;
        } else if (orderState instanceof PreparedState) {
            return OrderStatus.PREPARED;
        } else if (orderState instanceof OutForDeliveryState) {
            return OrderStatus.OUT_FOR_DELIVERY;
        } else if (orderState instanceof DeliveredState) {
            return OrderStatus.DELIVERED;
        } else if (orderState instanceof CancelledState) {
            return OrderStatus.CANCELLED;
        }
        throw new IllegalArgumentException("Unknown order state: " + orderState.getClass().getSimpleName());
    }
}
