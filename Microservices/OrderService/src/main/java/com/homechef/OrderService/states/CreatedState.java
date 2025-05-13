package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;

import java.util.UUID;

public class CreatedState implements OrderState {
    @Override
    public void cancelOrder(Order order) {
        order.setState(OrderStatus.getState(OrderStatus.CANCELLED));
    }

    @Override
    // created can transition to outfordelivery in case the order does not need to
    // be cooked (like dry food or something)
    public void setOrderState(Order order, OrderState state) {
        if (state instanceof PreparingState || state instanceof OutForDeliveryState) {
            order.setState(state);
            return;
        }

        throw new IllegalStateException("Cannot set order state to " + state.getClass().getSimpleName() + " from "
                + this.getClass().getSimpleName());
    }

    @Override
    public void updateItemNote(Order order, UUID productId, String note) {
        for (OrderItem item : order.getItems()) {
            if (item.getProductId().equals(productId)) {
                item.setNotes(note);
                return;
            }
        }
        throw new IllegalArgumentException(
                "couldn't update Item note, because item with productId " + productId
                        + " was not found in order with id " + order.getId());
    }

    @Override
    public OrderStatus getOrderStatus() {
        return OrderStatus.CREATED;
    }
}
