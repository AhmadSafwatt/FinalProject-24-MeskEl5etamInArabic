package com.homechef.OrderService.states;

import java.util.UUID;

import com.homechef.OrderService.models.Order;
import com.homechef.OrderService.models.OrderItem;
import com.homechef.OrderService.models.OrderStatus;

public class PreparingState implements OrderState {
    @Override
    public void setOrderState(Order order, OrderState state) {
        if (state instanceof PreparedState) {
            order.setState(state);
            return;
        }
        throw new IllegalStateException("Cannot change state to " + state.getClass().getSimpleName());
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
        return OrderStatus.PREPARING;
    }
}
