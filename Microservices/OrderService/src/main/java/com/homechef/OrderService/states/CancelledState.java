package com.homechef.OrderService.states;

import com.homechef.OrderService.models.Order;

public class CancelledState implements OrderState{
    @Override
    public void cancelOrder(Order order) {
        throw new IllegalStateException("Order is already cancelled");
    }

    @Override
    public void setOrderState(Order order, OrderState state) {
        throw new IllegalStateException("Cannot set order state to " + state.getClass().getSimpleName() + " from CancelledState");
    }
}
